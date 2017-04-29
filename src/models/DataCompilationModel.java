package models;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
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
import models.dataSuggestors.DataSuggestorBase;
import models.dataSuggestors.DatabaseController;
import models.dataSuggestors.VGMDBParser;
import models.dataSuggestors.DatabaseController.TableNames;
import support.GenreMapping;
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

    private ListProperty<String> fileNamesList;

    private ObjectProperty<Image> albumArt;

    private HashMap<Tag, DataCompliationField> fieldMap;

    private AudioFiles audioFilesModel;
    private DataSuggestorBase dbManagement;
    private VGMDBParser vgmdbModel;
    private Interpreter interpreter;

    public DataCompilationModel()
    {
        fieldMap = new HashMap<>();
        for(Tag t : Tag.values())
        {
            fieldMap.put(t, new DataCompliationField(t));
        }
        fileNamesList = new SimpleListProperty<String>();
        fileNamesList.set(FXCollections.observableArrayList());

        albumArt = new SimpleObjectProperty<Image>();

        audioFilesModel = new AudioFiles();
        dbManagement = new DatabaseController("");
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
    }

    // get the tag data for the selected index
    public void requestDataFor(int index, DataCompilationModelCallback cb)
    {
        clearAllTags();
        audioFilesModel.selectTag(index);
        addAudioModelDataToList();

        albumArt.set(audioFilesModel.getAlbumArt());
        fieldMap.get(Tag.ALBUM_ART_META).getTextProperty().set(audioFilesModel.getDataForTag(Tag.ALBUM_ART_META));

        cb.done("DONE");
    }

    public void requestDataFor(List<Integer> indicies, DataCompilationModelCallback cb)
    {
        clearAllTags();
        audioFilesModel.selectTags(indicies);
        addAudioModelDataToList();

        albumArt.set(audioFilesModel.getAlbumArt());
        fieldMap.get(Tag.ALBUM_ART_META).getTextProperty().set(audioFilesModel.getDataForTag(Tag.ALBUM_ART_META));

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
        String editorText = fieldMap.get(tag).getTextProperty().get();
        List<String> dropDownList = fieldMap.get(tag).getDropDownListProperty().get();
        dropDownList.clear();

        fieldMap.get(tag).getTextProperty().set(editorText);

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
        DataCompliationField field = fieldMap.get(Tag.FILE_NAME);
        String textFieldText = field.getTextProperty().get();
        if(!textFieldText.isEmpty() && !Utilities.isKeyword(textFieldText))
        {
            String formatted = String.format("%02d", Integer.valueOf(textFieldText)) + " " +
                fieldMap.get(Tag.TITLE).getTextProperty().get() + "." + audioFilesModel.getSelectedFileType();
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
        DataCompliationField field = fieldMap.get(Tag.ARTIST);
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
        DataCompliationField field = fieldMap.get(Tag.ALBUM_ARTIST);
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
        DataCompliationField field = fieldMap.get(Tag.GENRE);
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
                String albumArtist = fieldMap.get(Tag.ALBUM_ARTIST).getTextProperty().get();
                String album = fieldMap.get(Tag.ALBUM).getTextProperty().get();
                String artist = fieldMap.get(Tag.ARTIST).getTextProperty().get();
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
                fieldMap.get(tag).dropDownProperty.add(audioFilesModel.getDataForTag(tag));
            }
        }
        albumArt.setValue(audioFilesModel.getAlbumArt());
        fieldMap.get(Tag.ALBUM_ART_META).getTextProperty().set(audioFilesModel.getDataForTag(Tag.ALBUM_ART_META));
    }

    public void save()
    {
        // go through each element and set tag
        audioFilesModel.setDataForTag(Tag.FILE_NAME, 
            fieldMap.get(Tag.FILE_NAME).getTextProperty().get());
        audioFilesModel.setDataForTag(Tag.TITLE, 
            fieldMap.get(Tag.TITLE).getTextProperty().get());
        audioFilesModel.setDataForTag(Tag.ARTIST, 
            fieldMap.get(Tag.ARTIST).getTextProperty().get());
        audioFilesModel.setDataForTag(Tag.ALBUM, 
            fieldMap.get(Tag.ALBUM).getTextProperty().get());
        audioFilesModel.setDataForTag(Tag.ALBUM_ARTIST, 
            fieldMap.get(Tag.ALBUM_ART).getTextProperty().get());
        audioFilesModel.setDataForTag(Tag.TRACK,
            fieldMap.get(Tag.TRACK).getTextProperty().get());
        audioFilesModel.setDataForTag(Tag.YEAR, 
            fieldMap.get(Tag.YEAR).getTextProperty().get());
        audioFilesModel.setDataForTag(Tag.GENRE, 
            fieldMap.get(Tag.GENRE).getTextProperty().get());
        audioFilesModel.setDataForTag(Tag.COMMENT, 
            fieldMap.get(Tag.COMMENT).getTextProperty().get());
        
        File artwork = Utilities.saveImage(albumArt.get());
        audioFilesModel.setAlbumArtFromFile(artwork);
        artwork.delete();
        audioFilesModel.save();

        dbManagement.setDataForTag(Tag.ALBUM_ARTIST,
            fieldMap.get(Tag.ALBUM_ARTIST).getTextProperty().get());

        String[] splitArtists = Utilities.splitBySeparators(
            fieldMap.get(Tag.ARTIST).getTextProperty().get());
        dbManagement.setDataForTag(Tag.ARTIST, splitArtists);
    }

    public void clearAllTags()
    {
        for(Tag t : Tag.values())
        {
            fieldMap.get(t).clear();
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

    public DataCompliationField getPropertyForTag(Tag t)
    {
        return fieldMap.get(t);
    }

    public class DataCompliationField
    {
        private Tag type;
        private ListProperty<String> dropDownProperty;
        private SimpleStringProperty textProperty;

        public DataCompliationField(Tag type)
        {
            this.type = type;
            dropDownProperty = new SimpleListProperty<String>();
            dropDownProperty.set(FXCollections.observableArrayList());
            textProperty = new SimpleStringProperty();
        }

        public Tag getType()
        {
            return type;
        }

        public ListProperty<String> getDropDownListProperty()
        {
            return dropDownProperty;
        }

        public SimpleStringProperty getTextProperty()
        {
            return textProperty;
        }
        
        public void clear()
        {
            dropDownProperty.clear();
            textProperty.set("");
        }
    }
}
