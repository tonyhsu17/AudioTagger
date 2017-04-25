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
import models.dataSuggestors.VGMDBParser.VGMDBTag;
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
    private SimpleStringProperty fileNameEditorText;

    private ObjectProperty<Image> albumArt;

    private HashMap<Tag, DataCompliationField> fieldMap;

    private AudioFiles audioFilesModel;
    private DataSuggestorBase dbManagement;
    private VGMDBParser vgmdbModel;

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
        fileNameEditorText.set("");

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
        fieldMap.get(Tag.AlbumArtMeta).getTextProperty().set(audioFilesModel.getDataForTag(Tag.AlbumArtMeta));

        cb.done("DONE");
    }

    public void requestDataFor(List<Integer> indicies, DataCompilationModelCallback cb)
    {
        clearAllTags();
        audioFilesModel.selectTags(indicies);
        addAudioModelDataToList();

        albumArt.set(audioFilesModel.getAlbumArt());
        fieldMap.get(Tag.AlbumArtMeta).getTextProperty().set(audioFilesModel.getDataForTag(Tag.AlbumArtMeta));

        cb.done("DONE");
    }

    public void updateChoicesForTag(Tag tag, DataCompilationModelCallback cb)
    {
        if(tag.equals(Tag.AlbumArt) || tag.equals(Tag.AlbumArtMeta))
        {
            cb.done(0);
        }
        else
        {
            String originalText = (String)audioFilesModel.getDataForTag(tag);
            System.out.println("Originaltext: " + originalText);
            int size = addPossibleDataForTag(tag, originalText);
            System.out.println("updated size: " + size);
            cb.done(size);
        }
    }

    private int addPossibleDataForTag(Tag tag, String... additional)
    {
        String editorText = fieldMap.get(tag).getTextProperty().get();
        List<String> dropDownList = fieldMap.get(tag).getDropDownListProperty().get();
        dropDownList.clear();

        fieldMap.get(tag).getTextProperty().set(editorText);

        System.out.println("editor: " + fieldMap.get(tag).getTextProperty().get());

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
            case Album:
                addAdditionalPossibleAlbums(dropDownList);
                break;
            case AlbumArt:
                break;
            case AlbumArtMeta:
                break;
            case AlbumArtist:
                addAdditionalPossibleAlbumArtists(dropDownList);
                break;
            case Artist:
                addAdditionalPossibleArtists(dropDownList);
                break;
            case Comment:
                addAdditionalPossibleComments(dropDownList);
                break;
            case FileName:
                addAdditionalPossibleFileNames(dropDownList);
                break;
            case Genre:
                addAdditionalPossibleGenres(dropDownList);
                break;
            case Title:
                addAdditionalPossibleTitles(dropDownList);
                break;
            case Track:
                break;
            case Year:
                addAdditionalPossibleYears(dropDownList);
                break;
            default:
                break;

        }
        return dropDownList.size();
    }

    private void addAdditionalPossibleFileNames(List<String> dropDownList)
    {
        DataCompliationField field = fieldMap.get(Tag.FileName);
        String textFieldText = field.getTextProperty().get();
        if(!textFieldText.isEmpty() && !Utilities.isKeyword(textFieldText))
        {
            String formatted = String.format("%02d", Integer.valueOf(textFieldText)) + " " +
                fieldMap.get(Tag.Title).getTextProperty().get() + "." + audioFilesModel.getSelectedFileType();
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
                String temp = vgmdbModel.getDataForTag(Tag.Track, audioFilesModel.getDataForTag(Tag.Track));
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
        DataCompliationField field = fieldMap.get(Tag.Artist);
        String textFieldText = field.getTextProperty().get();
        // add from db
        List<String> possibleArtist = dbManagement.getPossibleDataForTag(Tag.Artist, textFieldText);
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
            String temp = vgmdbModel.getDataForTag(Tag.Artist, "");
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
                String temp = vgmdbModel.getDataForTag(Tag.Album, "");
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
        DataCompliationField field = fieldMap.get(Tag.AlbumArtist);
        String textFieldText = field.getTextProperty().get();
        // add from db
        List<String> possibleArtist =
            dbManagement.getPossibleDataForTag(Tag.AlbumArtist, textFieldText);
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
            String temp = vgmdbModel.getDataForTag(Tag.AlbumArtist);
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
                String temp = vgmdbModel.getDataForTag(Tag.Year, "");
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
        DataCompliationField field = fieldMap.get(Tag.Genre);
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
            String theme = vgmdbModel.getDataForTag(Tag.Comment, "");
            if(theme != null)
            {
                String albumArtist = fieldMap.get(Tag.AlbumArtist).getTextProperty().get();
                String album = fieldMap.get(Tag.Album).getTextProperty().get();
                String artist = fieldMap.get(Tag.Artist).getTextProperty().get();
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
            if(!tag.equals(Tag.AlbumArt) && !tag.equals(Tag.AlbumArtMeta))
            {
                fieldMap.get(tag).dropDownProperty.add(audioFilesModel.getDataForTag(tag));
            }
        }
        albumArt.setValue(audioFilesModel.getAlbumArt());
        fieldMap.get(Tag.AlbumArtMeta).getTextProperty().set(audioFilesModel.getDataForTag(Tag.AlbumArtMeta));
    }

    public void save()
    {
        // go through each element and set tag
        audioFilesModel.setDataForTag(Tag.FileName, 
            fieldMap.get(Tag.FileName).getTextProperty().get());
        audioFilesModel.setDataForTag(Tag.Title, 
            fieldMap.get(Tag.Title).getTextProperty().get());
        audioFilesModel.setDataForTag(Tag.Artist, 
            fieldMap.get(Tag.Artist).getTextProperty().get());
        audioFilesModel.setDataForTag(Tag.Album, 
            fieldMap.get(Tag.Album).getTextProperty().get());
        audioFilesModel.setDataForTag(Tag.AlbumArtist, 
            fieldMap.get(Tag.AlbumArt).getTextProperty().get());
        audioFilesModel.setDataForTag(Tag.Track,
            fieldMap.get(Tag.Track).getTextProperty().get());
        audioFilesModel.setDataForTag(Tag.Year, 
            fieldMap.get(Tag.Year).getTextProperty().get());
        audioFilesModel.setDataForTag(Tag.Genre, 
            fieldMap.get(Tag.Genre).getTextProperty().get());
        audioFilesModel.setDataForTag(Tag.Comment, 
            fieldMap.get(Tag.Comment).getTextProperty().get());
        
        File artwork = Utilities.saveImage(albumArt.get());
        audioFilesModel.setAlbumArtFromFile(artwork);
        artwork.delete();
        audioFilesModel.save();

        dbManagement.setDataForTag(Tag.AlbumArtist,
            fieldMap.get(Tag.AlbumArtist).getTextProperty().get());

        String[] splitArtists = Utilities.splitBySeparators(
            fieldMap.get(Tag.Artist).getTextProperty().get());
        dbManagement.setDataForTag(Tag.Artist, splitArtists);
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
