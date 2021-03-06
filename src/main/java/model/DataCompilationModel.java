package model;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import modules.controllers.AudioFilesController;
import modules.controllers.DatabaseController;
import modules.controllers.EditorDataController;
import modules.controllers.VGMDBController;
import modules.controllers.base.InformationBase;
import modules.controllers.base.TagBase;
import org.tonyhsu17.utilities.Logger;
import support.Genres;
import support.structure.EditorComboBoxMeta;
import support.structure.TagDetails;
import support.util.StringUtil;
import support.util.Utilities.EditorTag;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;



/**
 * Handles the compiling of multiple sources into one source.
 *
 * @author Tony Hsu
 */
public class DataCompilationModel implements Logger {
    static final String TEMPFOLDER =
        "D:\\Music\\Japanese\\Anime Album Collection\\Accel World";


    /**
     * Callback for {@link DataCompilationModel} when data is ready.
     */
    public interface DataCompilationModelCallback {
        public void done(Object obj);
    }


    /**
     * Options to retrieve image from.
     */
    public enum ImageFrom {
        FILE, URL, CLIPBOARD, VGMDB
    }


    private EditorDataController editorMap; // ComboBox controller (editor text and drop down)

    private ListProperty<String> fileNamesList; // currently working files
    private DatabaseController dbManagement; // database for prediction of common tag fields
    private AudioFilesController audioFilesModel; // audio files meta

    private VGMDBController vgmdbModel; // data handler for vgmdb website

    /**
     * @param dbManagement Controller for database, {@link DatabaseController}.
     * @param editorMap    Controller for editor textfields, {@link EditorDataController}.
     */
    public DataCompilationModel(DatabaseController dbManagement, EditorDataController editorMap) {
        this.dbManagement = dbManagement;
        this.editorMap = editorMap;
        audioFilesModel = new AudioFilesController();

        fileNamesList = new SimpleListProperty<String>();
        fileNamesList.set(FXCollections.observableArrayList());

        audioFilesModel.setWorkingDirectory(TEMPFOLDER);
    }

    /**
     * Initiate a reset on data sources.
     */
    public void reset() {
        info("Clearing everthing");
        audioFilesModel.reset();
        fileNamesList.clear();
        editorMap.clearAllTags();
    }

    /**
     * Set vgmdb controller.
     *
     * @param parser {@link VGMDBController}
     */
    public void setVGMDBController(VGMDBController parser) {
        vgmdbModel = parser;
        setPossibleKeywordTag();
        editorMap.autoCompleter().updateAutoFillRules();
    }

    /**
     * Display tag meta for the selected index.
     *
     * @param indices
     * @param cb      callback, {@link DataCompilationModelCallback}
     */
    public void requestDataFor(List<Integer> indices, DataCompilationModelCallback cb) {
        editorMap.clearAllTags();
        audioFilesModel.selectTags(indices, (tagDetails) -> {
            if(tagDetails != null) {
                for(EditorTag tag : EditorTag.values()) {
                    editorMap.setDataForTag(tag, tagDetails.get(tag)); // No formatting needed since its read from file
                }
                editorMap.setAlbumArt(tagDetails.getAlbumArt());
            }
            cb.done("DONE");
        });
    }


    /**
     * Provide an editor dropdown with fuzzy matching.
     *
     * @param tag  Which text field, {@link EditorTag}
     * @param text Fuzzy match with text
     * @param cb   callback, {@link DataCompilationModelCallback}
     */
    public void requestDropdownForTag(EditorTag tag, String text, List<String> originalDropDown, DataCompilationModelCallback cb) {
        if(tag.equals(EditorTag.ALBUM_ART) || tag.equals(EditorTag.ALBUM_ART_META)) {
            cb.done(0);
        }
        else {
            List<String> possibleValues = getPossibleValuesForString(tag, text, "");
            // TODO defaults add file defaults?
            //            int size = addPossibleDataForTag(tag, text);
            ObservableList<String> list = FXCollections.observableArrayList(possibleValues);
            cb.done(list);
        }
    }

    /**
     * Populate list with all possible values.
     *
     * @param tag            Which text field, {@link EditorTag}
     * @param compareAgainst Fuzzy match with text
     * @param defaults       Values to add automatiical
     * @return
     */
    private List<String> getPossibleValuesForString(EditorTag tag, String compareAgainst, String... defaults) {
        List<String> dropDownList = new ArrayList<>();
        // add defaults
        for(String str : defaults) {
            if(str != null && !str.isEmpty()) {
                dropDownList.add(str);
            }
        }

        // format the current text
        editorMap.setFormattedDataForTag(tag, compareAgainst);

        // now handle base on specific
        switch (tag) {
            case ALBUM:
            case ALBUM_ARTIST:
            case ARTIST:
            case COMMENT:
            case FILE_NAME:
            case TITLE:
            case YEAR:
                dropDownList.addAll(getPossibleValues(tag));
                break;
            case GENRE:
                dropDownList.addAll(addPossibleGenres());
                break;
            default:
                break;
        }
        debug(tag + " size: " + dropDownList.size() + " : " + Arrays.toString(dropDownList.toArray(new String[0])));
        return dropDownList;
    }

    /**
     * Centralized method for setting editor and dropdown text from different sources
     *
     * @param tag EditorTag
     * @return size of dropdown
     */
    private List<String> getPossibleValues(EditorTag tag) {
        List<String> returnList = new ArrayList<String>();
        EditorComboBoxMeta field = editorMap.getMeta(tag);
        String textFieldText = field.getTextProperty().get();

        // add from db
        List<String> possibleArtist = dbManagement.getPossibleDataForTag(tag, textFieldText);
        if(possibleArtist != null) {
            returnList.addAll(possibleArtist);
        }

        // add from vgmdb
        if(vgmdbModel != null) {
            String temp;
            if(tag == EditorTag.TITLE) { // special case, where more info is needed for tag
                temp = vgmdbModel.getDataForTag(EditorTag.TRACK, audioFilesModel.getDataForTag(EditorTag.TRACK));
            }
            else {
                temp = vgmdbModel.getDataForTag(tag, "");
            }

            if(temp != null && !temp.isEmpty() && !returnList.contains(temp)) {
                returnList.add(temp);
            }
        }

        String audioOriginal = audioFilesModel.getDataForTag(tag);
        if(audioOriginal != null && !audioOriginal.isEmpty() && !returnList.contains(audioOriginal)) {
            returnList.add(audioFilesModel.getDataForTag(tag)); // add original value 
        }
        return returnList;
    }

    private List<String> addPossibleGenres() {
        EditorComboBoxMeta field = editorMap.getMeta(EditorTag.GENRE);
        String textFieldText = field.getTextProperty().get();

        List<String> possibleGenres = Genres.containsIgnoreCase(textFieldText);
        if(!possibleGenres.contains(audioFilesModel.getDataForTag(EditorTag.GENRE))) {
            possibleGenres.add(audioFilesModel.getDataForTag(EditorTag.GENRE)); // add original value 
        }
        return possibleGenres;
    }

    public void save() {
        // store meta info to db - start
        dbManagement.setDataForTag(EditorTag.ALBUM_ARTIST, editorMap.getMeta(EditorTag.ALBUM_ARTIST).getTextProperty().get());

        String[] splitArtists = StringUtil.splitBySeparators(editorMap.getMeta(EditorTag.ARTIST).getTextProperty().get());
        dbManagement.setDataForTag(EditorTag.ARTIST, splitArtists);

        // save delim tags into db
        List<Integer> selected = audioFilesModel.removeHeaderIndicies();
        for(int index : selected) {
            // get audio file's info and compare against vgmdb's info
            TagDetails info = audioFilesModel.getTagDetails(index);
            addDelimsToDB(EditorTag.ARTIST, info);
            addDelimsToDB(EditorTag.TITLE, info);
            addDelimsToDB(EditorTag.ALBUM, info);
            addDelimsToDB(EditorTag.ALBUM_ARTIST, info);
        }

        saveNonCapitalizationInDB();
        // store meta info to db - end

        // go through each element and set tag
        TagDetails details = new TagDetails();
        for(EditorTag tag : EditorTag.values()) {
            details.set(tag, editorMap.getMeta(tag).getTextProperty().get());
        }
        //        File artwork = ImageUtil.saveImage(albumArt.get());
        //        audioFilesModel.setAlbumArtFromFile(artwork);
        //     artwork.delete();
        details.setAlbumArt(editorMap.getAlbumArt());

        audioFilesModel.save(details);
    }

    /**
     * Check if first letter of each word in title and determine if word is lowercased.
     */
    private void saveNonCapitalizationInDB() {
        EditorComboBoxMeta titleMeta = editorMap.getMeta(EditorTag.TITLE);
        String title = titleMeta.getTextProperty().get();
        for(String word : title.split(" ")) {
            // if first letter is lowercased, save tag
            if(word.substring(0, 1).equals(word.substring(0, 1).toLowerCase())) {
                dbManagement.setDataForTag(DatabaseController.AdditionalTag.NON_CAPITALIZED, word);
            }
        }
    }

    /**
     * Parse editor text and determine if there are delimiter modifications, then store in db.
     *
     * @param tag
     * @param info
     */
    private void addDelimsToDB(EditorTag tag, TagDetails info) {
        List<String[]> delimDiffs;

        if(tag == EditorTag.TITLE) {
            delimDiffs = StringUtil.getDiffInDelim(vgmdbModel.getDataForTag(tag, info.get(EditorTag.TRACK)), info.get(tag));
        }
        else {
            delimDiffs = StringUtil.getDiffInDelim(vgmdbModel.getDataForTag(tag), info.get(tag));
        }

        for(String[] delimDiff : delimDiffs) {
            dbManagement.setDataForTag(DatabaseController.AdditionalTag.REPLACE_WORD, delimDiff[0], delimDiff[1]);
        }
    }


    public void setImage(ImageFrom type, String ummm) {
        // TODO set meta too
        switch (type) {
            case CLIPBOARD:
                Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
                if(transferable != null) {
                    DataFlavor[] flavors = transferable.getTransferDataFlavors();
                    for(int i = flavors.length - 1; i >= 0; i--) {
                        if(flavors[i].isMimeTypeEqual(DataFlavor.imageFlavor)) {
                            try {
                                editorMap
                                    .setAlbumArt(SwingFXUtils.toFXImage((BufferedImage)transferable.getTransferData(flavors[i]), null));
                            }
                            catch (IOException | UnsupportedFlavorException e) {
                                error("failed to get image: " + e.getMessage());
                            }
                        }
                    }
                }
                else {
                    error("That wasn't an image!");
                }
                break;
            case FILE:
                break;
            case URL:
                break;
            case VGMDB:
                editorMap.setAlbumArt(vgmdbModel.getAlbumArt());
                break;
            default:
                break;
        }
    }

    public void setPossibleKeywordTag() {
        // TODO future iteration, abstract VGMDB stuff into its own module (jar file)
        // and load from that, so other sites and also be added in
        HashMap<InformationBase, List<TagBase<?>>> mapping = new HashMap<>();
        mapping.put(audioFilesModel, audioFilesModel.getKeywordTags());
        mapping.put(dbManagement, dbManagement.getKeywordTags());
        mapping.put(vgmdbModel, vgmdbModel.getKeywordTags());
        mapping.put(editorMap, editorMap.getKeywordTags());
        Settings.getInstance().setKeywordTags(mapping);
    }

    // ~~~~~~~~~~~~~~~~~ //
    // Getters & Setters //
    // Accessors for UI //
    // ~~~~~~~~~~~~~~~~~ //
    public ListProperty<String> processingFilesProperty() {
        return audioFilesModel.fileNamesProperty();
    }

    public final List<String> getProcessingFiles() {
        return audioFilesModel.getFileNames();
    }

    public void appendWorkingDirectory(File[] array) {
        audioFilesModel.appendWorkingDirectory(array);
    }

    public void setAlbumArt(Object obj) {
        editorMap.setAlbumArt(obj);
    }

    public List<String> getSongList() {
        return audioFilesModel.getFileNames();
    }


}
