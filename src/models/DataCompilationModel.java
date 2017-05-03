package models;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import javafx.beans.binding.ListExpression;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import models.dataSuggestors.AudioFiles;
import models.dataSuggestors.AudioTagComboBoxModel;
import models.dataSuggestors.AudioTagComboBoxModel.ComboBoxMeta;
import models.dataSuggestors.DataSuggestorBase;
import models.dataSuggestors.DatabaseController;
import models.dataSuggestors.VGMDBParser;
import models.dataSuggestors.DatabaseController.TableNames;
import models.dataSuggestors.VGMDBParser.AdditionalTag;
import support.GenreMapping;
import support.TagBase;
import support.Utilities;
import support.Utilities.Tag;


public class DataCompilationModel
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
    
    private List<KeywordInterpreter> editorAutoComplete; // store auto complete fields

    public DataCompilationModel()
    {
        fieldMap = new AudioTagComboBoxModel();
        audioFilesModel = new AudioFiles();
        dbManagement = new DatabaseController("");
        
        fileNamesList = new SimpleListProperty<String>();
        fileNamesList.set(FXCollections.observableArrayList());

        albumArt = new SimpleObjectProperty<Image>();

        editorAutoComplete = new ArrayList<KeywordInterpreter>();
        updateAutoComplete();
        
        audioFilesModel.setWorkingDirectory(TEMPFOLDER);
        
//        setPossibleKeywordTag();
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
    }
    
    private void updateAutoComplete()
    {
        editorAutoComplete.clear();
        for(Tag t : Tag.values())
        {
            KeywordInterpreter temp = null;
            if((temp = Settings.getInstance().getRuleFor(t)) != null)
            {
                editorAutoComplete.add(temp);
            }
        }
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
            String originalText = (String)audioFilesModel.getDataForTag(tag);
//            System.out.println("Originaltext: " + originalText);
            int size = addPossibleDataForTag(tag, originalText);
//            System.out.println("updated size: " + size);
            cb.done(size);
        }
    }

    private int addPossibleDataForTag(Tag tag, String... additional)
    {
        String editorText = fieldMap.getMeta(tag).getTextProperty().get();
        List<String> dropDownList = fieldMap.getMeta(tag).getDropDownListProperty().get();
        dropDownList.clear();

        fieldMap.getMeta(tag).getTextProperty().set(editorText);

        // add original
        for(String str : additional)
        {
            if(str != null && !str.isEmpty() && !dropDownList.contains(str))
            {
                dropDownList.add(str);
            }
        }

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
        if(!textFieldText.isEmpty() && !Utilities.isKeyword(textFieldText))
        {
            String formatted = String.format("%02d", Integer.valueOf(textFieldText)) + " " +
                fieldMap.getMeta(Tag.TITLE).getTextProperty().get() + "." + audioFilesModel.getSelectedFileType();
            if(!dropDownList.contains(formatted))
            {
                dropDownList.add(formatted);
            }
        }
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
        List<String> possibleArtist =
            dbManagement.getPossibleDataForTag(Tag.ALBUM_ARTIST, textFieldText);
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
                String formatted = albumArtist + " " + theme + " Single - " +
                    album + " [" + artist + "]";
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
        // go through each element and set tag
        audioFilesModel.setDataForTag(Tag.FILE_NAME, 
            fieldMap.getMeta(Tag.FILE_NAME).getTextProperty().get());
        audioFilesModel.setDataForTag(Tag.TITLE, 
            fieldMap.getMeta(Tag.TITLE).getTextProperty().get());
        audioFilesModel.setDataForTag(Tag.ARTIST, 
            fieldMap.getMeta(Tag.ARTIST).getTextProperty().get());
        audioFilesModel.setDataForTag(Tag.ALBUM, 
            fieldMap.getMeta(Tag.ALBUM).getTextProperty().get());
        audioFilesModel.setDataForTag(Tag.ALBUM_ARTIST, 
            fieldMap.getMeta(Tag.ALBUM_ART).getTextProperty().get());
        audioFilesModel.setDataForTag(Tag.TRACK,
            fieldMap.getMeta(Tag.TRACK).getTextProperty().get());
        audioFilesModel.setDataForTag(Tag.YEAR, 
            fieldMap.getMeta(Tag.YEAR).getTextProperty().get());
        audioFilesModel.setDataForTag(Tag.GENRE, 
            fieldMap.getMeta(Tag.GENRE).getTextProperty().get());
        audioFilesModel.setDataForTag(Tag.COMMENT, 
            fieldMap.getMeta(Tag.COMMENT).getTextProperty().get());
        
        File artwork = Utilities.saveImage(albumArt.get());
        audioFilesModel.setAlbumArtFromFile(artwork);
        artwork.delete();
        audioFilesModel.save();

        dbManagement.setDataForTag(Tag.ALBUM_ARTIST,
            fieldMap.getMeta(Tag.ALBUM_ARTIST).getTextProperty().get());

        String[] splitArtists = Utilities.splitBySeparators(
            fieldMap.getMeta(Tag.ARTIST).getTextProperty().get());
        dbManagement.setDataForTag(Tag.ARTIST, splitArtists);
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
