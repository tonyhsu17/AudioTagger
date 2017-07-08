package models;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Map.Entry;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.image.Image;
import models.dataSuggestors.AudioFiles;
import models.dataSuggestors.AudioTagComboBoxModel;
import models.dataSuggestors.AudioTagComboBoxModel.ComboBoxMeta;
import models.dataSuggestors.DataSuggestorBase;
import models.dataSuggestors.DatabaseController;
import models.dataSuggestors.VGMDBParser;
import support.EventCenter;
import support.EventCenter.Events;
import support.GenreMapping;
import support.Logger;
import support.Scheduler;
import support.TagBase;
import support.util.ImageUtil;
import support.util.StringUtil;
import support.util.Utilities.Tag;


public class DataCompilationModel implements Logger
{
    static final String TEMPFOLDER =
        "D:\\Music\\Japanese - Test\\Anime Album Collection\\To Aru Kagaku no Railgun [Collection]\\To Aru Kagaku no Railgun\\Only My Railgun";

    public interface DataCompilationModelCallback
    {
        public void done(Object obj);
    }

    public enum ImageFrom
    {
        FILE, URL, CLIPBOARD, VGMDB
    }

    private ListProperty<String> fileNamesList; // currently working files

    private ObjectProperty<Image> albumArt; // album art pic

    private AudioTagComboBoxModel fieldMap; // TODO Tag to ComboBox data (editor text and drop down)
    private AudioFiles audioFilesModel; // audio files meta
    private DataSuggestorBase dbManagement; // database for prediction of common tag fields
    private VGMDBParser vgmdbModel; // data handler for vgmdb website

    private HashMap<Tag, KeywordInterpreter> editorAutoComplete; // store auto complete fields
    private Scheduler editorAutoUpdater; // thread of each polling to update auto compete field

    public DataCompilationModel()
    {
        fieldMap = new AudioTagComboBoxModel();
        audioFilesModel = new AudioFiles();
        dbManagement = new DatabaseController("");

        fileNamesList = new SimpleListProperty<String>();
        fileNamesList.set(FXCollections.observableArrayList());

        albumArt = new SimpleObjectProperty<Image>();

        editorAutoComplete = new HashMap<Tag, KeywordInterpreter>();
        // updateAutoCompleteRules(); // activate when vgmdb parser set
       
        EventCenter.getInstance().subscribeEvent(Events.SettingChanged, this, (obj) ->
        {
            updateAutoCompleteRules();
        });

        audioFilesModel.setWorkingDirectory(TEMPFOLDER);
    }

    public void reset()
    {
        audioFilesModel.setWorkingDirectory("");
        fileNamesList.clear();

        clearAllTags();
    }

    public void setVGMDBParser(VGMDBParser parser)
    {
        vgmdbModel = parser;
        setPossibleKeywordTag();
        updateAutoCompleteRules();
    }

    private void updateAutoCompleteRules()
    {
        if(editorAutoUpdater != null) // if thread exist, stop thread
        {
            editorAutoUpdater.stopThread();
            editorAutoUpdater = null;
        }

        editorAutoComplete.clear(); // clear list and add/re-add rules
        for(Tag t : Tag.values()) // for each tag
        {
            KeywordInterpreter temp = null;
            // if there is a rule, add to list
            if((temp = Settings.getInstance().getRuleFor(t)) != null)
            {
                info("Adding rule for: " + t);
                editorAutoComplete.put(t, temp);
            }
        }

        // if there is at least one auto-complete, start polling to update field
        if(editorAutoComplete.size() > 0)
        {
            // create polling in a separate thread
            editorAutoUpdater = new Scheduler(1, () ->
            {
                updateAutoFills();
            });
            editorAutoUpdater.start();
        }
    }

    private void updateAutoFills()
    {
        for(Entry<Tag, KeywordInterpreter> entry : editorAutoComplete.entrySet())
        {
            ComboBoxMeta meta = fieldMap.getMeta(entry.getKey()); // get combo box to modify
            
            if(!meta.isPaused() && meta.shouldStopAutoFill())
            {
                KeywordInterpreter builder = entry.getValue();
                DataSuggestorBase classObj;
                TagBase<?> tag;

                // pass values into builder to construct the value with given tags and info
                for(int i = 0; i < builder.getCount(); i++) 
                {
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
    
    public boolean isAutoFillEnabled()
    {
        for(Entry<Tag, KeywordInterpreter> entry : editorAutoComplete.entrySet())
        {
            ComboBoxMeta meta = fieldMap.getMeta(entry.getKey()); // get combo box to modify
            return !meta.isPaused();
        }
        return false;
    }

    private void setPauseAutoFill(boolean flag)
    {
        for(Entry<Tag, KeywordInterpreter> entry : editorAutoComplete.entrySet())
        {
            ComboBoxMeta meta = fieldMap.getMeta(entry.getKey()); // get combo box to modify
            meta.setPaused(flag);
        }
    }
    
    public void toggleAutoFill()
    {
        for(Entry<Tag, KeywordInterpreter> entry : editorAutoComplete.entrySet())
        {
            ComboBoxMeta meta = fieldMap.getMeta(entry.getKey()); // get combo box to modify
            meta.setPaused(!meta.isPaused());
        }
    }
    
    private void setTextFormattedFromDB(Tag type, String value)
    {
        if(type == Tag.ALBUM_ARTIST)
        {
            String formattedText = dbManagement.getDataForTag(Tag.ALBUM_ARTIST, value);
            
            if(!formattedText.isEmpty()) {
                value = formattedText;
            }
        }
        else if(type == Tag.ARTIST)
        {
            String[] artists = StringUtil.splitBySeparators(value);
            List<String> formattedArtists = new ArrayList<String>();
            
            for(String artist : artists) {
                String[] byFirstLast = StringUtil.splitName(artist);
                String formattedText = dbManagement.getDataForTag(type, byFirstLast[0], byFirstLast[1]);
                if(!formattedText.isEmpty()) { // if exist in db use that one
                    debug("ARTIST formattedText: " + formattedText);
                    formattedArtists.add(formattedText);
                } else { // else use original one
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

    
    // get the tag data for the selected index
    public void requestDataFor(int index, DataCompilationModelCallback cb)
    {
        clearAllTags();
        audioFilesModel.selectTag(index);
        addAudioModelDataToList();

        albumArt.set(audioFilesModel.getAlbumArt());
        fieldMap.getMeta(Tag.ALBUM_ART_META).getTextProperty().set(audioFilesModel.getDataForTag(Tag.ALBUM_ART_META));

        cb.done("DONE");
    }

    public void requestDataFor(List<Integer> indicies, DataCompilationModelCallback cb)
    {
        clearAllTags();
        audioFilesModel.selectTags(indicies);
        addAudioModelDataToList();

        albumArt.set(audioFilesModel.getAlbumArt());
        fieldMap.getMeta(Tag.ALBUM_ART_META).getTextProperty().set(audioFilesModel.getDataForTag(Tag.ALBUM_ART_META));

        cb.done("DONE");
    }

    public void updateChoicesForTag(Tag tag, DataCompilationModelCallback cb)
    {
        if(tag.equals(Tag.ALBUM_ART) || tag.equals(Tag.ALBUM_ART_META))
        {
            cb.done(0);
        }
        else
        {
         // stop auto-complete since there is human input
            // unless text is empty then revert back to allow auto-fill
            fieldMap.getMeta(tag).setAllowAutoFill(fieldMap.getMeta(tag).getTextProperty().get().isEmpty() ? true : false);
            String originalText = (String)audioFilesModel.getDataForTag(tag);

            int size = addPossibleDataForTag(tag, originalText);
            
            cb.done(size);
        }
    }

    private int addPossibleDataForTag(Tag tag, String... additional)
    {
        String editorText = fieldMap.getMeta(tag).getTextProperty().get();
        List<String> dropDownList = fieldMap.getMeta(tag).getDropDownListProperty().get();
        dropDownList.clear();
        
        // add original
        for(String str : additional)
        {
            if(str != null && !str.isEmpty() && !dropDownList.contains(str))
            {
                dropDownList.add(str);
            }
        }
        
        setTextFormattedFromDB(tag, editorText);
        
        // now handle base on specific
        switch (tag)
        {
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
                try
                {
                    fieldMap.getMeta(tag).getTextProperty().set(String.format("%02d", editorText));
                }
                catch(IllegalFormatException e)
                {
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

    private void addAdditionalPossibleFileNames(List<String> dropDownList)
    {
        ComboBoxMeta field = fieldMap.getMeta(Tag.FILE_NAME);
        String textFieldText = field.getTextProperty().get();
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

    private void addAdditionalPossibleTitles(List<String> dropDownList)
    {
        // add from vgmdb
        if(vgmdbModel != null)
        {
            try
            {
                String temp = vgmdbModel.getDataForTag(Tag.TRACK, audioFilesModel.getDataForTag(Tag.TRACK));
                if(temp != null && !temp.isEmpty() && !dropDownList.contains(temp))
                {
                    dropDownList.add(temp);
                }
            }
            catch (NumberFormatException e)
            {
            }
        }
    }

    // adding compilation
    private void addAdditionalPossibleArtists(List<String> dropDownList)
    {
        ComboBoxMeta field = fieldMap.getMeta(Tag.ARTIST);
        String textFieldText = field.getTextProperty().get();
        // add from db
        List<String> possibleArtist = dbManagement.getPossibleDataForTag(Tag.ARTIST, textFieldText);
        if(possibleArtist != null)
        {
            for(String str : possibleArtist)
            {
                if(!dropDownList.contains(str))
                {
                    dropDownList.add(str);
                }
            }
        }

        // add from vgmdb
        if(vgmdbModel != null)
        {
            String temp = vgmdbModel.getDataForTag(Tag.ARTIST, "");
            if(temp != null && !temp.isEmpty() && !dropDownList.contains(temp))
            {
                dropDownList.add(temp);
            }
        }
    }

    private void addAdditionalPossibleAlbums(List<String> dropDownList)
    {
        // add from vgmdb
        if(vgmdbModel != null)
        {
            try
            {
                String temp = vgmdbModel.getDataForTag(Tag.ALBUM, "");
                if(temp != null && !temp.isEmpty() && !dropDownList.contains(temp))
                {
                    dropDownList.add(temp);
                }
            }
            catch (NumberFormatException e)
            {
            }
        }
    }

    // adding compilation
    private void addAdditionalPossibleAlbumArtists(List<String> dropDownList)
    {
        ComboBoxMeta field = fieldMap.getMeta(Tag.ALBUM_ARTIST);
        String textFieldText = field.getTextProperty().get();
        // add from db
        List<String> possibleArtist = dbManagement.getPossibleDataForTag(Tag.ALBUM_ARTIST, textFieldText);
        for(String str : possibleArtist)
        {
            if(!dropDownList.contains(str))
            {
                dropDownList.add(str);
            }
        }

        // add from vgmdb
        if(vgmdbModel != null)
        {
            String temp = vgmdbModel.getDataForTag(Tag.ALBUM_ARTIST);
            if(temp != null && !temp.isEmpty() && !dropDownList.contains(temp))
            {
                dropDownList.add(temp);
            }
        }
    }

    private void addAdditionalPossibleYears(List<String> dropDownList)
    {
        // add from vgmdb
        if(vgmdbModel != null)
        {
            try
            {
                String temp = vgmdbModel.getDataForTag(Tag.YEAR, "");
                if(temp != null && !temp.isEmpty() && !dropDownList.contains(temp))
                {
                    dropDownList.add(temp);
                }
            }
            catch (NumberFormatException e)
            {
            }
        }
    }

    private void addAdditionalPossibleGenres(List<String> dropDownList)
    {
        ComboBoxMeta field = fieldMap.getMeta(Tag.GENRE);
        String textFieldText = field.getTextProperty().get();

        List<String> possibleGenres = GenreMapping.containsIgnoreCase(textFieldText);
        for(String genre : possibleGenres)
        {
            if(!dropDownList.contains(genre))
            {
                dropDownList.add(genre);
            }
        }
    }

    private void addAdditionalPossibleComments(List<String> dropDownList)
    {
        // add from vgmdb
        if(vgmdbModel != null)
        {
            String theme = vgmdbModel.getDataForTag(Tag.COMMENT, "");
            if(theme != null)
            {
                String albumArtist = fieldMap.getMeta(Tag.ALBUM_ARTIST).getTextProperty().get();
                String album = fieldMap.getMeta(Tag.ALBUM).getTextProperty().get();
                String artist = fieldMap.getMeta(Tag.ARTIST).getTextProperty().get();
                String formatted = albumArtist + " " + theme + " Single - " + album + " [" + artist + "]";
                if(!dropDownList.contains(formatted))
                {
                    dropDownList.add(formatted);
                }
            }
        }
    }

    // add file's tag to display list
    private void addAudioModelDataToList()
    {
        for(Tag tag : Tag.values())
        {
            if(!tag.equals(Tag.ALBUM_ART) && !tag.equals(Tag.ALBUM_ART_META))
            {
                fieldMap.getMeta(tag).getDropDownListProperty().add(audioFilesModel.getDataForTag(tag));
            }
        }
        albumArt.setValue(audioFilesModel.getAlbumArt());
        fieldMap.getMeta(Tag.ALBUM_ART_META).getTextProperty().set(audioFilesModel.getDataForTag(Tag.ALBUM_ART_META));
    }

    public void save()
    {
        setPauseAutoFill(true); //stop auto fill to prevent corruption
        
        // go through each element and set tag
        audioFilesModel.setDataForTag(Tag.FILE_NAME, fieldMap.getMeta(Tag.FILE_NAME).getTextProperty().get());
        audioFilesModel.setDataForTag(Tag.TITLE, fieldMap.getMeta(Tag.TITLE).getTextProperty().get());
        audioFilesModel.setDataForTag(Tag.ARTIST, fieldMap.getMeta(Tag.ARTIST).getTextProperty().get());
        audioFilesModel.setDataForTag(Tag.ALBUM, fieldMap.getMeta(Tag.ALBUM).getTextProperty().get());
        audioFilesModel.setDataForTag(Tag.ALBUM_ARTIST, fieldMap.getMeta(Tag.ALBUM_ARTIST).getTextProperty().get());
        audioFilesModel.setDataForTag(Tag.TRACK, fieldMap.getMeta(Tag.TRACK).getTextProperty().get());
        audioFilesModel.setDataForTag(Tag.YEAR, fieldMap.getMeta(Tag.YEAR).getTextProperty().get());
        audioFilesModel.setDataForTag(Tag.GENRE, fieldMap.getMeta(Tag.GENRE).getTextProperty().get());
        audioFilesModel.setDataForTag(Tag.COMMENT, fieldMap.getMeta(Tag.COMMENT).getTextProperty().get());

        File artwork = ImageUtil.saveImage(albumArt.get());
        audioFilesModel.setAlbumArtFromFile(artwork);
        artwork.delete();
        audioFilesModel.save();

        
        dbManagement.setDataForTag(Tag.ALBUM_ARTIST, fieldMap.getMeta(Tag.ALBUM_ARTIST).getTextProperty().get());

        String[] splitArtists = StringUtil.splitBySeparators(fieldMap.getMeta(Tag.ARTIST).getTextProperty().get());
        dbManagement.setDataForTag(Tag.ARTIST, splitArtists);
        
        setPauseAutoFill(false);
    }

    public void clearAllTags()
    {
        for(Tag t : Tag.values())
        {
            fieldMap.getMeta(t).clear();
        }
        albumArt.set(null);
    }

    public void setImage(ImageFrom type, String ummm)
    {
        // TODO set meta too
        switch (type)
        {
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

    public void setPossibleKeywordTag()
    {
        // TODO future iteration, abstract VGMDB stuff into its own module (jar file)
        // and load from that, so other sites and also be added in
        HashMap<DataSuggestorBase, List<TagBase<?>>> mapping = new HashMap<>();
        mapping.put(audioFilesModel, audioFilesModel.getKeywordTags());
        mapping.put(dbManagement, dbManagement.getKeywordTags());
        mapping.put(vgmdbModel, vgmdbModel.getKeywordTags());
        mapping.put(fieldMap, fieldMap.getKeywordTags());
        Settings.getInstance().setKeywordTags(mapping);
    }

    // ~~~~~~~~~~~~~~~~~ //
    // Getters & Setters //
    // Accessors for UI //
    // ~~~~~~~~~~~~~~~~~ //
    public ListProperty<String> processingFilesProperty()
    {
        return audioFilesModel.fileNamesProperty();
    }

    public final List<String> getProcessingFiles()
    {
        return audioFilesModel.getFileNames();
    }

    public ObjectProperty<Image> albumArtProperty()
    {
        return albumArt;
    }

    public final Image getAlbumArt()
    {
        return albumArt.get();
    }

    public void appendWorkingDirectory(File[] array)
    {
        audioFilesModel.appendWorkingDirectory(array);
    }

    public void changeAlbumArtFromFile(File f)
    {
        audioFilesModel.setAlbumArtFromFile(f);
    }

    public void changeAlbumArtFromURL(String url)
    {
        audioFilesModel.setAlbumArtFromURL(url);
    }

    public List<String> getSongList()
    {
        return audioFilesModel.getFileNames();
    }

    public ComboBoxMeta getPropertyForTag(Tag t)
    {
        return fieldMap.getMeta(t);
    }
}
