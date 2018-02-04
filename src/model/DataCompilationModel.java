package model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.regex.Pattern;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.image.Image;
import model.base.InformationBase;
import model.base.TagBase;
import model.database.DatabaseController;
import model.information.AudioFiles;
import model.information.EditorComboBoxModel;
import model.information.VGMDBParser;
import modules.AutoCompleter;
import modules.AutoCorrecter;
import support.Genres;
import support.Logger;
import support.structure.EditorComboBoxMeta;
import support.util.ImageUtil;
import support.util.StringUtil;
import support.util.Utilities.EditorTag;



public class DataCompilationModel implements Logger {
    static final String TEMPFOLDER =
        "D:\\Music\\Japanese - Test\\Anime Album Collection\\To Aru Kagaku no Railgun [Collection]\\To Aru Kagaku no Railgun\\Only My Railgun";

    public interface DataCompilationModelCallback {
        public void done(Object obj);
    }

    public enum ImageFrom {
        FILE, URL, CLIPBOARD, VGMDB
    }

    private ListProperty<String> fileNamesList; // currently working files

    private ObjectProperty<Image> albumArt; // album art pic

    private EditorComboBoxModel editorMap; // Tag to ComboBox data (editor text and drop down)
    private AudioFiles audioFilesModel; // audio files meta
    private DatabaseController dbManagement; // database for prediction of common tag fields
    private VGMDBParser vgmdbModel; // data handler for vgmdb website

    // modules
    private AutoCorrecter autoCorrecter;
    private AutoCompleter autoCompleter;


    public DataCompilationModel() {
        editorMap = new EditorComboBoxModel();
        audioFilesModel = new AudioFiles();
        dbManagement = new DatabaseController("");

        fileNamesList = new SimpleListProperty<String>();
        fileNamesList.set(FXCollections.observableArrayList());

        albumArt = new SimpleObjectProperty<Image>();

        // updateAutoCompleteRules(); // activate when vgmdb parser set

        autoCorrecter = new AutoCorrecter(editorMap, dbManagement);
        autoCompleter = new AutoCompleter(autoCorrecter);


        audioFilesModel.setWorkingDirectory(TEMPFOLDER);
    }

    public void reset() {
        audioFilesModel.setWorkingDirectory("");
        fileNamesList.clear();

        clearAllTags();
    }

    public void setVGMDBParser(VGMDBParser parser) {
        vgmdbModel = parser;
        setPossibleKeywordTag();
        autoCompleter.updateAutoFillRules();
    }


    /**
     * Retrieves standardized tags in delimiters based on user preferences
     * 
     * @return New delimieter tag if found in database, else original
     */
    private String getDelimTagReplacement(String text) {
        List<String> tags = StringUtil.getStrInDelim(text); // all delim tags in text
        ArrayList<String> dbTags = new ArrayList<String>();
        for(String tag : tags) { // get all user stored delim tags that have been replaced
            dbTags.add(dbManagement.getDataForTag(DatabaseController.AdditionalTag.REPLACE_WORD, tag));
        }

        for(int i = 0; i < tags.size(); i++) { // tags.size() == dbTags.size()
            // db returns empty string for non replaced tags
            if(!dbTags.get(i).isEmpty()) {
                info("replacing: " + tags.get(i) + " with: " + dbTags.get(i));
                //text.re
                text = text.replaceFirst(Pattern.quote(tags.get(i)), dbTags.get(i));
            }
        }
        return text;
    }


    // get the tag data for the selected index
    public void requestDataFor(int index, DataCompilationModelCallback cb) {
        clearAllTags();
        audioFilesModel.selectTag(index);
        addAudioModelDataToEditor();

        albumArt.set(audioFilesModel.getAlbumArt());
        editorMap.getMeta(EditorTag.ALBUM_ART_META).getTextProperty().set(audioFilesModel.getDataForTag(EditorTag.ALBUM_ART_META));

        cb.done("DONE");
    }

    public void requestDataFor(List<Integer> indicies, DataCompilationModelCallback cb) {
        clearAllTags();
        audioFilesModel.selectTags(indicies);
        addAudioModelDataToEditor();

        albumArt.set(audioFilesModel.getAlbumArt());
        editorMap.getMeta(EditorTag.ALBUM_ART_META).getTextProperty().set(audioFilesModel.getDataForTag(EditorTag.ALBUM_ART_META));

        cb.done("DONE");
    }

    public void updateChoicesForTag(EditorTag tag, String text, DataCompilationModelCallback cb) {
        if(tag.equals(EditorTag.ALBUM_ART) || tag.equals(EditorTag.ALBUM_ART_META)) {
            cb.done(0);
        }
        else {
            // stop auto-complete since there is human input
            // unless text is empty then revert back to allow auto-fill
            // TODO get rid of stop autofill on click?
            //editorMap.getMeta(tag).setAllowAutoFill(editorMap.getMeta(tag).getTextProperty().get().isEmpty() ? true : false);
            //            String originalText = audioFilesModel.getDataForTag(tag); // unneeded now

            int size = addPossibleDataForTag(tag, text);

            cb.done(size);
        }
    }

    /**
     * Centralized method for setting editor and dropdown text from different sources
     * 
     * @param tag EditorTag
     * @param additional optional args if needed
     * @return size of dropdown
     */
    private int addPossibleDataForTag(EditorTag tag, String... additional) {
        String editorText = getDelimTagReplacement(editorMap.getMeta(tag).getTextProperty().get());
        List<String> dropDownList = editorMap.getMeta(tag).getDropDownListProperty().get();
        List<String> newDropdownList = new ArrayList<String>();
        //        editorMap.getMeta(tag).clearDropdown();
        //        editorMap.getComboBox(tag).getSelectionModel().clearSelection();
        // add original
        for(String str : additional) {
            if(str != null && !str.isEmpty() && !dropDownList.contains(str)) {
                //                dropDownList.add(str);
            }
        }

        autoCorrecter.setTextFormattedFromDB(tag, editorText);

        // now handle base on specific
        switch (tag) {
            case ALBUM:
            case ALBUM_ARTIST:
            case ARTIST:
            case COMMENT:
            case FILE_NAME:
            case TITLE:
            case YEAR:
                newDropdownList = getPossibleValues(tag);
                break;
            case ALBUM_ART:
                break;
            case ALBUM_ART_META:
                break;
            case GENRE:
                newDropdownList = addPossibleGenres();
                break;
            case TRACK:
                try {
                    editorMap.getMeta(tag).getTextProperty().set(String.format("%02d", editorText));
                }
                catch (IllegalFormatException e) {
                }
                break;
            default:
                break;

        }
        //        debug("dropDownList: " + Arrays.toString(dropDownList.toArray(new String[0])));
        //        debug("newDropdownList: " + Arrays.toString(newDropdownList.toArray(new String[0])));

        List<String> toRemove = new ArrayList<String>();
        List<String> toAdd = new ArrayList<String>();
        //        debug("to remove:" + Arrays.toString(toRemove.toArray(new String[0])));
        //        debug("to add: " + Arrays.toString(toAdd.toArray(new String[0])));
        // TODO cleanup (selecting dropdown works, typing in location work
        // broken: when changing selection with keyboard it sets editor test too...
        for(String str : dropDownList) {
            if(!newDropdownList.contains(str)) {
                toRemove.add(str);
            }
        }
        for(String str : newDropdownList) {
            if(!dropDownList.contains(str)) {
                toAdd.add(str);
            }
        }
        dropDownList.removeAll(toRemove);
        dropDownList.addAll(toAdd);
        return dropDownList.size();
    }

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
            String temp = vgmdbModel.getDataForTag(tag, "");
            if(tag == EditorTag.TITLE) { // special case, where more info is needed for tag
                temp = vgmdbModel.getDataForTag(EditorTag.TRACK, audioFilesModel.getDataForTag(EditorTag.TRACK));
            }

            if(temp != null && !temp.isEmpty() && !returnList.contains(temp)) {
                returnList.add(temp);
            }
        }

        if(!returnList.contains(audioFilesModel.getDataForTag(tag))) {
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
    
    /**
     * Add audio meta data to editor view
     */
    private void addAudioModelDataToEditor() {
        for(EditorTag tag : EditorTag.values()) {
            if(!tag.equals(EditorTag.ALBUM_ART) && !tag.equals(EditorTag.ALBUM_ART_META)) {
                editorMap.getMeta(tag).getTextProperty().set(audioFilesModel.getDataForTag(tag));
            }
        }
        albumArt.setValue(audioFilesModel.getAlbumArt());
        editorMap.getMeta(EditorTag.ALBUM_ART_META).getTextProperty().set(audioFilesModel.getDataForTag(EditorTag.ALBUM_ART_META));
    }

    public void save() {
        // store meta info to db - start
        dbManagement.setDataForTag(EditorTag.ALBUM_ARTIST, editorMap.getMeta(EditorTag.ALBUM_ARTIST).getTextProperty().get());

        String[] splitArtists = StringUtil.splitBySeparators(editorMap.getMeta(EditorTag.ARTIST).getTextProperty().get());
        dbManagement.setDataForTag(EditorTag.ARTIST, splitArtists);

        saveDelimTagInDB();
        // store meta info to db - end

        // go through each element and set tag
        audioFilesModel.setDataForTag(EditorTag.FILE_NAME, editorMap.getMeta(EditorTag.FILE_NAME).getTextProperty().get());
        audioFilesModel.setDataForTag(EditorTag.TITLE, editorMap.getMeta(EditorTag.TITLE).getTextProperty().get());
        audioFilesModel.setDataForTag(EditorTag.ARTIST, editorMap.getMeta(EditorTag.ARTIST).getTextProperty().get());
        audioFilesModel.setDataForTag(EditorTag.ALBUM, editorMap.getMeta(EditorTag.ALBUM).getTextProperty().get());
        audioFilesModel.setDataForTag(EditorTag.ALBUM_ARTIST, editorMap.getMeta(EditorTag.ALBUM_ARTIST).getTextProperty().get());
        audioFilesModel.setDataForTag(EditorTag.TRACK, editorMap.getMeta(EditorTag.TRACK).getTextProperty().get());
        audioFilesModel.setDataForTag(EditorTag.YEAR, editorMap.getMeta(EditorTag.YEAR).getTextProperty().get());
        audioFilesModel.setDataForTag(EditorTag.GENRE, editorMap.getMeta(EditorTag.GENRE).getTextProperty().get());
        audioFilesModel.setDataForTag(EditorTag.COMMENT, editorMap.getMeta(EditorTag.COMMENT).getTextProperty().get());

        File artwork = ImageUtil.saveImage(albumArt.get());
        audioFilesModel.setAlbumArtFromFile(artwork);
        artwork.delete();
        audioFilesModel.save();
    }

    /**
     * hella inelegant code.
     * Check each editor field's final value and store delim tag diff compared to VGMDB and original
     * audio file's tag
     */
    private void saveDelimTagInDB() {
        List<String[]> delimDiffs = StringUtil.getDiffInDelim(vgmdbModel.getDataForTag(EditorTag.FILE_NAME),
            editorMap.getMeta(EditorTag.FILE_NAME).getTextProperty().get());
        for(String[] delimDiff : delimDiffs) {
            dbManagement.setDataForTag(DatabaseController.AdditionalTag.REPLACE_WORD, delimDiff[0], delimDiff[1]);
        }
        delimDiffs = StringUtil.getDiffInDelim(audioFilesModel.getDataForTag(EditorTag.FILE_NAME),
            editorMap.getMeta(EditorTag.FILE_NAME).getTextProperty().get());
        for(String[] delimDiff : delimDiffs) {
            dbManagement.setDataForTag(DatabaseController.AdditionalTag.REPLACE_WORD, delimDiff[0], delimDiff[1]);
        }

        int trackNum = 1;
        try {
            trackNum = Integer.valueOf(editorMap.getDataForTag(EditorTag.TRACK));
        }
        catch (NumberFormatException e) {
            info("track number: " + trackNum + " isn't an int, default to 1");
        }
        delimDiffs = StringUtil.getDiffInDelim(vgmdbModel.getDataForTag(EditorTag.TRACK, trackNum + ""),
            editorMap.getMeta(EditorTag.TITLE).getTextProperty().get());
        for(String[] delimDiff : delimDiffs) {
            dbManagement.setDataForTag(DatabaseController.AdditionalTag.REPLACE_WORD, delimDiff[0], delimDiff[1]);
        }
        delimDiffs = StringUtil.getDiffInDelim(audioFilesModel.getDataForTag(EditorTag.TITLE),
            editorMap.getMeta(EditorTag.TITLE).getTextProperty().get());
        for(String[] delimDiff : delimDiffs) {
            dbManagement.setDataForTag(DatabaseController.AdditionalTag.REPLACE_WORD, delimDiff[0], delimDiff[1]);
        }

        delimDiffs = StringUtil.getDiffInDelim(vgmdbModel.getDataForTag(EditorTag.ARTIST),
            editorMap.getMeta(EditorTag.ARTIST).getTextProperty().get());
        for(String[] delimDiff : delimDiffs) {
            dbManagement.setDataForTag(DatabaseController.AdditionalTag.REPLACE_WORD, delimDiff[0], delimDiff[1]);
        }
        delimDiffs = StringUtil.getDiffInDelim(audioFilesModel.getDataForTag(EditorTag.ARTIST),
            editorMap.getMeta(EditorTag.ARTIST).getTextProperty().get());
        for(String[] delimDiff : delimDiffs) {
            dbManagement.setDataForTag(DatabaseController.AdditionalTag.REPLACE_WORD, delimDiff[0], delimDiff[1]);
        }

        delimDiffs = StringUtil.getDiffInDelim(vgmdbModel.getDataForTag(EditorTag.ALBUM),
            editorMap.getMeta(EditorTag.ALBUM).getTextProperty().get());
        for(String[] delimDiff : delimDiffs) {
            dbManagement.setDataForTag(DatabaseController.AdditionalTag.REPLACE_WORD, delimDiff[0], delimDiff[1]);
        }
        delimDiffs = StringUtil.getDiffInDelim(audioFilesModel.getDataForTag(EditorTag.ALBUM),
            editorMap.getMeta(EditorTag.ALBUM).getTextProperty().get());
        for(String[] delimDiff : delimDiffs) {
            dbManagement.setDataForTag(DatabaseController.AdditionalTag.REPLACE_WORD, delimDiff[0], delimDiff[1]);
        }

        delimDiffs = StringUtil.getDiffInDelim(vgmdbModel.getDataForTag(EditorTag.ALBUM_ARTIST),
            editorMap.getMeta(EditorTag.ALBUM_ARTIST).getTextProperty().get());
        for(String[] delimDiff : delimDiffs) {
            dbManagement.setDataForTag(DatabaseController.AdditionalTag.REPLACE_WORD, delimDiff[0], delimDiff[1]);
        }
        delimDiffs = StringUtil.getDiffInDelim(audioFilesModel.getDataForTag(EditorTag.ALBUM_ARTIST),
            editorMap.getMeta(EditorTag.ALBUM_ARTIST).getTextProperty().get());
        for(String[] delimDiff : delimDiffs) {
            dbManagement.setDataForTag(DatabaseController.AdditionalTag.REPLACE_WORD, delimDiff[0], delimDiff[1]);
        }
    }

    public void clearAllTags() {
        for(EditorTag t : EditorTag.values()) {
            editorMap.getMeta(t).clearAll();
        }
        albumArt.set(null);
    }

    public void setImage(ImageFrom type, String ummm) {
        // TODO set meta too
        switch (type) {
            case CLIPBOARD:
                break;
            case FILE:
                break;
            case URL:
                break;
            case VGMDB:
                albumArt.set(vgmdbModel.getAlbumArt());
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

    public ObjectProperty<Image> albumArtProperty() {
        return albumArt;
    }

    public final Image getAlbumArt() {
        return albumArt.get();
    }

    public void appendWorkingDirectory(File[] array) {
        audioFilesModel.appendWorkingDirectory(array);
    }

    public void changeAlbumArtFromFile(File f) {
        audioFilesModel.setAlbumArtFromFile(f);
    }

    public void changeAlbumArtFromURL(String url) {
        audioFilesModel.setAlbumArtFromURL(url);
    }

    public List<String> getSongList() {
        return audioFilesModel.getFileNames();
    }

    public EditorComboBoxMeta getPropertyForTag(EditorTag t) {
        return editorMap.getMeta(t);
    }
}
