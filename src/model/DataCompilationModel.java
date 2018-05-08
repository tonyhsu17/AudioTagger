package model;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.base.InformationBase;
import model.base.TagBase;
import modules.controllers.AudioFilesController;
import modules.controllers.DatabaseController;
import modules.controllers.EditorDataController;
import modules.controllers.VGMDBParser;
import support.Genres;
import support.Logger;
import support.structure.EditorComboBoxMeta;
import support.structure.TagDetails;
import support.util.StringUtil;
import support.util.Utilities.EditorTag;



public class DataCompilationModel implements Logger {
    static final String TEMPFOLDER =
        "D:\\Music\\Japanese\\Anime Album Collection\\Accel World";

    public interface DataCompilationModelCallback {
        public void done(Object obj);
    }

    public enum ImageFrom {
        FILE, URL, CLIPBOARD, VGMDB
    }

    private EditorDataController editorMap; // ComboBox controller (editor text and drop down)

    private ListProperty<String> fileNamesList; // currently working files
    private DatabaseController dbManagement; // database for prediction of common tag fields
    private AudioFilesController audioFilesModel; // audio files meta

    private VGMDBParser vgmdbModel; // data handler for vgmdb website


    public DataCompilationModel(DatabaseController dbManagement, EditorDataController editorMap) {
        this.dbManagement = dbManagement;
        this.editorMap = editorMap;
        audioFilesModel = new AudioFilesController();

        fileNamesList = new SimpleListProperty<String>();
        fileNamesList.set(FXCollections.observableArrayList());

        audioFilesModel.setWorkingDirectory(TEMPFOLDER);
    }

    public void reset() {
        info("Clearing everthing");
        audioFilesModel.setWorkingDirectory("");
        fileNamesList.clear();
        editorMap.clearAllTags();
    }

    public void setVGMDBParser(VGMDBParser parser) {
        vgmdbModel = parser;
        setPossibleKeywordTag();
        editorMap.autoCompleter().updateAutoFillRules();
    }

    // get the tag data for the selected index
    public void requestDataFor(List<Integer> indices, DataCompilationModelCallback cb) {
        editorMap.clearAllTags();
        audioFilesModel.selectTags(indices, (tagDetails) -> {
            for(EditorTag tag : EditorTag.values()) {
                editorMap.setDataForTag(tag, tagDetails.get(tag)); // No formatting needed since its read from file
            }
            editorMap.setAlbumArt(tagDetails.getAlbumArt());
            cb.done("DONE");
        });
    }


    /**
     * Update editor text for selected file
     * 
     * @param tag
     * @param text
     * @param cb
     */
    public void requestDropdownForTag(EditorTag tag, String text, List<String> originalDropDown, DataCompilationModelCallback cb) {
        if(tag.equals(EditorTag.ALBUM_ART) || tag.equals(EditorTag.ALBUM_ART_META)) {
            cb.done(0);
        }
        else {
            List<String> possibleValues = getPossibleDataForTag(tag, text, ""); // TODO defaults add file defaults?
            //            int size = addPossibleDataForTag(tag, text);
            ObservableList<String> list = FXCollections.observableArrayList(possibleValues);
            cb.done(list);
        }
    }

    private List<String> getPossibleDataForTag(EditorTag tag, String compareAgainst, String... defaults) {
        List<String> dropDownList = new ArrayList<String>();
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
     * @param additional optional args if needed
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
            String temp = vgmdbModel.getDataForTag(tag, "");
            if(tag == EditorTag.TITLE) { // special case, where more info is needed for tag
                temp = vgmdbModel.getDataForTag(EditorTag.TRACK, audioFilesModel.getDataForTag(EditorTag.TRACK));
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

        saveDelimTagInDB();
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
