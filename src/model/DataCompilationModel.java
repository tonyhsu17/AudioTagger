package model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Map.Entry;
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
import support.EventCenter;
import support.EventCenter.Events;
import support.Genres;
import support.Logger;
import support.Scheduler;
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
    private InformationBase dbManagement; // database for prediction of common tag fields
    private VGMDBParser vgmdbModel; // data handler for vgmdb website

    private HashMap<EditorTag, KeywordInterpreter> editorAutoComplete; // store auto complete fields
    private Scheduler editorAutoUpdater; // thread of each polling to update auto compete field

    public DataCompilationModel() {
        editorMap = new EditorComboBoxModel();
        audioFilesModel = new AudioFiles();
        dbManagement = new DatabaseController("");

        fileNamesList = new SimpleListProperty<String>();
        fileNamesList.set(FXCollections.observableArrayList());

        albumArt = new SimpleObjectProperty<Image>();

        editorAutoComplete = new HashMap<EditorTag, KeywordInterpreter>();
        // updateAutoCompleteRules(); // activate when vgmdb parser set

        EventCenter.getInstance().subscribeEvent(Events.SettingChanged, this, (obj) -> {
            updateAutoCompleteRules();
        });

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
        updateAutoCompleteRules();
    }

    private void updateAutoCompleteRules() {
        if(editorAutoUpdater != null) // if thread exist, stop thread
        {
            editorAutoUpdater.stopThread();
            editorAutoUpdater = null;
        }

        editorAutoComplete.clear(); // clear list and add/re-add rules
        for(EditorTag t : EditorTag.values()) // for each tag
        {
            KeywordInterpreter temp = null;
            // if there is a rule, add to list
            if((temp = Settings.getInstance().getRuleFor(t)) != null) {
                info("Adding rule for: " + t);
                editorAutoComplete.put(t, temp);
            }
        }

        // if there is at least one auto-complete, start polling to update field
        if(editorAutoComplete.size() > 0) {
            // create polling in a separate thread
            editorAutoUpdater = new Scheduler(1, () -> {
                updateAutoFills();
            });
            editorAutoUpdater.start();
        }
    }

    private void updateAutoFills() {
        for(Entry<EditorTag, KeywordInterpreter> entry : editorAutoComplete.entrySet()) {
            EditorComboBoxMeta meta = editorMap.getMeta(entry.getKey()); // get combo box to modify

            if(!meta.isPaused() && meta.shouldStopAutoFill()) {
                KeywordInterpreter builder = entry.getValue();
                InformationBase classObj;
                TagBase<?> tag;

                // pass values into builder to construct the value with given tags and info
                for(int i = 0; i < builder.getCount(); i++) {
                    classObj = builder.getClass(i);
                    tag = builder.getTag(i);
                    builder.setValue(i, classObj.getDataForTag(tag, "")); // pass data to builder
                }

                String finalValue = builder.buildString(); // get the final results
                //                System.out.println("Entry: " + entry.getKey() + " DecodedString: " + finalValue);
                //                meta.getTextProperty().set(finalValue); // set input box text

                // check db for caps matching text to replace
                setTextFormattedFromDB(entry.getKey(), finalValue);

                //TODO create class that does text replacement (ie (karoke) -> (intrumental), (tv edit) -> (tv size) etc) 
            }
        }
    }

    public boolean isAutoFillEnabled() {
        for(Entry<EditorTag, KeywordInterpreter> entry : editorAutoComplete.entrySet()) {
            EditorComboBoxMeta meta = editorMap.getMeta(entry.getKey()); // get combo box to modify
            return !meta.isPaused();
        }
        return false;
    }

    private void setPauseAutoFill(boolean flag) {
        for(Entry<EditorTag, KeywordInterpreter> entry : editorAutoComplete.entrySet()) {
            EditorComboBoxMeta meta = editorMap.getMeta(entry.getKey()); // get combo box to modify
            meta.setPaused(flag);
        }
    }

    public void toggleAutoFill() {
        for(Entry<EditorTag, KeywordInterpreter> entry : editorAutoComplete.entrySet()) {
            EditorComboBoxMeta meta = editorMap.getMeta(entry.getKey()); // get combo box to modify
            meta.setPaused(!meta.isPaused());
        }
    }

    private void setTextFormattedFromDB(EditorTag type, String value) {
        if(type == EditorTag.ALBUM_ARTIST) {
            String formattedText = dbManagement.getDataForTag(EditorTag.ALBUM_ARTIST, value);

            if(!formattedText.isEmpty()) {
                value = formattedText;
            }
        }
        else if(type == EditorTag.ARTIST) {
            String[] artists = StringUtil.splitBySeparators(value);
            List<String> formattedArtists = new ArrayList<String>();

            for(String artist : artists) {
                String[] byFirstLast = StringUtil.splitName(artist);
                String formattedText = dbManagement.getDataForTag(type, byFirstLast[0], byFirstLast[1]);
                if(!formattedText.isEmpty()) { // if exist in db use that one
                    formattedArtists.add(formattedText);
                }
                else { // else use original one
                    formattedArtists.add(artist);
                }
            }

            String formattedText = StringUtil.getCommaSeparatedStringWithAnd(formattedArtists);
            if(!formattedText.isEmpty()) {
                value = formattedText;
            }
        }

        getPropertyForTag(type).getTextProperty().set(value); // set the final value
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
        addAudioModelDataToList();

        albumArt.set(audioFilesModel.getAlbumArt());
        editorMap.getMeta(EditorTag.ALBUM_ART_META).getTextProperty().set(audioFilesModel.getDataForTag(EditorTag.ALBUM_ART_META));

        cb.done("DONE");
    }

    public void requestDataFor(List<Integer> indicies, DataCompilationModelCallback cb) {
        clearAllTags();
        audioFilesModel.selectTags(indicies);
        addAudioModelDataToList();

        albumArt.set(audioFilesModel.getAlbumArt());
        editorMap.getMeta(EditorTag.ALBUM_ART_META).getTextProperty().set(audioFilesModel.getDataForTag(EditorTag.ALBUM_ART_META));

        cb.done("DONE");
    }

    public void updateChoicesForTag(EditorTag tag, DataCompilationModelCallback cb) {
        if(tag.equals(EditorTag.ALBUM_ART) || tag.equals(EditorTag.ALBUM_ART_META)) {
            cb.done(0);
        }
        else {
            // stop auto-complete since there is human input
            // unless text is empty then revert back to allow auto-fill
            editorMap.getMeta(tag).setAllowAutoFill(editorMap.getMeta(tag).getTextProperty().get().isEmpty() ? true : false);
            String originalText = audioFilesModel.getDataForTag(tag);

            int size = addPossibleDataForTag(tag, originalText);

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
        dropDownList.clear();

        // add original
        for(String str : additional) {
            if(str != null && !str.isEmpty() && !dropDownList.contains(str)) {
                dropDownList.add(str);
            }
        }

        setTextFormattedFromDB(tag, editorText);

        // now handle base on specific
        switch (tag) {
            case ALBUM:
                addAdditionalPossibleAlbums(dropDownList);
                break;
            case ALBUM_ART:
                break;
            case ALBUM_ART_META:
                break;
            case ALBUM_ARTIST:
                addAdditionalPossibleAlbumArtists(dropDownList);
                break;
            case ARTIST:
                addAdditionalPossibleArtists(dropDownList);
                break;
            case COMMENT:
                addAdditionalPossibleComments(dropDownList);
                break;
            case FILE_NAME:
                addAdditionalPossibleFileNames(dropDownList);
                break;
            case GENRE:
                addAdditionalPossibleGenres(dropDownList);
                break;
            case TITLE:
                addAdditionalPossibleTitles(dropDownList);
                break;
            case TRACK:
                try {
                    editorMap.getMeta(tag).getTextProperty().set(String.format("%02d", editorText));
                }
                catch (IllegalFormatException e) {
                }
                break;
            case YEAR:
                addAdditionalPossibleYears(dropDownList);
                break;
            default:
                break;

        }
        return dropDownList.size();
    }

    private void addAdditionalPossibleFileNames(List<String> dropDownList) {
        //        EditorComboBoxMeta field = editorMap.getMeta(EditorTag.FILE_NAME);
        //        String textFieldText = field.getTextProperty().get();
        //        if(!textFieldText.isEmpty() && !Utilities.isKeyword(textFieldText))
        //        {
        //            String formatted = String.format("%02d", Integer.valueOf(textFieldText)) + " " +
        //                fieldMap.getMeta(Tag.TITLE).getTextProperty().get() + "." + audioFilesModel.getSelectedFileType();
        //            if(!dropDownList.contains(formatted))
        //            {
        //                dropDownList.add(formatted);
        //            }
        //        }
    }

    private void addAdditionalPossibleTitles(List<String> dropDownList) {
        // add from vgmdb
        if(vgmdbModel != null) {
            try {
                String temp = vgmdbModel.getDataForTag(EditorTag.TRACK, audioFilesModel.getDataForTag(EditorTag.TRACK));
                if(temp != null && !temp.isEmpty() && !dropDownList.contains(temp)) {
                    dropDownList.add(temp);
                }
            }
            catch (NumberFormatException e) {
            }
        }
    }

    // adding compilation
    private void addAdditionalPossibleArtists(List<String> dropDownList) {
        EditorComboBoxMeta field = editorMap.getMeta(EditorTag.ARTIST);
        String textFieldText = field.getTextProperty().get();
        // add from db
        List<String> possibleArtist = dbManagement.getPossibleDataForTag(EditorTag.ARTIST, textFieldText);
        if(possibleArtist != null) {
            for(String str : possibleArtist) {
                if(!dropDownList.contains(str)) {
                    dropDownList.add(str);
                }
            }
        }

        // add from vgmdb
        if(vgmdbModel != null) {
            String temp = vgmdbModel.getDataForTag(EditorTag.ARTIST, "");
            if(temp != null && !temp.isEmpty() && !dropDownList.contains(temp)) {
                dropDownList.add(temp);
            }
        }
    }

    private void addAdditionalPossibleAlbums(List<String> dropDownList) {
        // add from vgmdb
        if(vgmdbModel != null) {
            try {
                String temp = vgmdbModel.getDataForTag(EditorTag.ALBUM, "");
                if(temp != null && !temp.isEmpty() && !dropDownList.contains(temp)) {
                    dropDownList.add(temp);
                }
            }
            catch (NumberFormatException e) {
            }
        }
    }

    // adding compilation
    private void addAdditionalPossibleAlbumArtists(List<String> dropDownList) {
        EditorComboBoxMeta field = editorMap.getMeta(EditorTag.ALBUM_ARTIST);
        String textFieldText = field.getTextProperty().get();
        // add from db
        List<String> possibleArtist = dbManagement.getPossibleDataForTag(EditorTag.ALBUM_ARTIST, textFieldText);
        for(String str : possibleArtist) {
            if(!dropDownList.contains(str)) {
                dropDownList.add(str);
            }
        }

        // add from vgmdb
        if(vgmdbModel != null) {
            String temp = vgmdbModel.getDataForTag(EditorTag.ALBUM_ARTIST);
            if(temp != null && !temp.isEmpty() && !dropDownList.contains(temp)) {
                dropDownList.add(temp);
            }
        }
    }

    private void addAdditionalPossibleYears(List<String> dropDownList) {
        // add from vgmdb
        if(vgmdbModel != null) {
            try {
                String temp = vgmdbModel.getDataForTag(EditorTag.YEAR, "");
                if(temp != null && !temp.isEmpty() && !dropDownList.contains(temp)) {
                    dropDownList.add(temp);
                }
            }
            catch (NumberFormatException e) {
            }
        }
    }

    private void addAdditionalPossibleGenres(List<String> dropDownList) {
        EditorComboBoxMeta field = editorMap.getMeta(EditorTag.GENRE);
        String textFieldText = field.getTextProperty().get();

        List<String> possibleGenres = Genres.containsIgnoreCase(textFieldText);
        for(String genre : possibleGenres) {
            if(!dropDownList.contains(genre)) {
                dropDownList.add(genre);
            }
        }
    }

    private void addAdditionalPossibleComments(List<String> dropDownList) {
        // add from vgmdb
        if(vgmdbModel != null) {
            String theme = vgmdbModel.getDataForTag(EditorTag.COMMENT, "");
            if(theme != null) {
                String albumArtist = editorMap.getMeta(EditorTag.ALBUM_ARTIST).getTextProperty().get();
                String album = editorMap.getMeta(EditorTag.ALBUM).getTextProperty().get();
                String artist = editorMap.getMeta(EditorTag.ARTIST).getTextProperty().get();
                String formatted = albumArtist + " " + theme + " Single - " + album + " [" + artist + "]";
                if(!dropDownList.contains(formatted)) {
                    dropDownList.add(formatted);
                }
            }
        }
    }

    // add file's tag to display list
    private void addAudioModelDataToList() {
        for(EditorTag tag : EditorTag.values()) {
            if(!tag.equals(EditorTag.ALBUM_ART) && !tag.equals(EditorTag.ALBUM_ART_META)) {
                editorMap.getMeta(tag).getDropDownListProperty().add(audioFilesModel.getDataForTag(tag));
            }
        }
        albumArt.setValue(audioFilesModel.getAlbumArt());
        editorMap.getMeta(EditorTag.ALBUM_ART_META).getTextProperty().set(audioFilesModel.getDataForTag(EditorTag.ALBUM_ART_META));
    }

    public void save() {
        setPauseAutoFill(true); //stop auto fill to prevent corruption
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


        setPauseAutoFill(false);
    }

    /**
     * hella inelegant code.
     * Check each editor field's final value and store delim tag diff compared to VGMDB and original audio file's tag
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
            editorMap.getMeta(t).clear();
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
