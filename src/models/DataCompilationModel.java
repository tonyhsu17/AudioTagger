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
    private ListProperty<String> titlesList;
    private SimpleStringProperty titleEditorText;
    private ListProperty<String> artistsList;
    private SimpleStringProperty artistEditorText;
    private ListProperty<String> albumsList;
    private SimpleStringProperty albumEditorText;
    private ListProperty<String> albumArtistsList;
    private SimpleStringProperty albumArtistEditorText;
    private ListProperty<String> tracksList;
    private SimpleStringProperty trackEditorText;
    private ListProperty<String> yearsList;
    private SimpleStringProperty yearEditorText;
    private ListProperty<String> genresList;
    private SimpleStringProperty genreEditorText;
    private ListProperty<String> commentsList;
    private SimpleStringProperty commentEditorText;
    private ObjectProperty<Image> albumArt;
    private SimpleStringProperty albumArtMeta;
    
    private HashMap<Tag, ListProperty<String>> tagToListMapping; // combofield's dropdown list
    private HashMap<Tag, SimpleStringProperty> tagToEditorTextMapping; // combofield's input text
    
    private AudioFiles audioFilesModel;
    private DataSuggestorBase dbManagement;
    private VGMDBParser vgmdbModel;
    
    public DataCompilationModel()
    {        
        fileNamesList = new SimpleListProperty<String>();
        fileNamesList.set(FXCollections.observableArrayList());
        fileNameEditorText = new SimpleStringProperty();
        titlesList = new SimpleListProperty<String>();
        titlesList.set(FXCollections.observableArrayList());
        titleEditorText = new SimpleStringProperty();
        artistsList = new SimpleListProperty<String>();
        artistsList.set(FXCollections.observableArrayList());
        artistEditorText = new SimpleStringProperty();
        albumsList = new SimpleListProperty<String>();
        albumsList.set(FXCollections.observableArrayList());
        albumEditorText = new SimpleStringProperty();
        albumArtistsList = new SimpleListProperty<String>();
        albumArtistsList.set(FXCollections.observableArrayList());
        albumArtistEditorText = new SimpleStringProperty();
        tracksList = new SimpleListProperty<String>();
        tracksList.set(FXCollections.observableArrayList());
        trackEditorText = new SimpleStringProperty();
        yearsList = new SimpleListProperty<String>();
        yearsList.set(FXCollections.observableArrayList());
        yearEditorText = new SimpleStringProperty();
        genresList = new SimpleListProperty<String>();
        genresList.set(FXCollections.observableArrayList());
        genreEditorText = new SimpleStringProperty();
        commentsList = new SimpleListProperty<String>();
        commentsList.set(FXCollections.observableArrayList());
        commentEditorText = new SimpleStringProperty();
        
        albumArt = new SimpleObjectProperty<Image>();
        albumArtMeta = new SimpleStringProperty();
        
        audioFilesModel = new AudioFiles();
        dbManagement = new DatabaseController("");
        
        tagToListMapping = new HashMap<>();
        tagToListMapping.put(Tag.FileName, fileNamesList);
        tagToListMapping.put(Tag.Title, titlesList);
        tagToListMapping.put(Tag.Artist, artistsList);
        tagToListMapping.put(Tag.Album, albumsList);
        tagToListMapping.put(Tag.AlbumArtist, albumArtistsList);
        tagToListMapping.put(Tag.Artist, artistsList);
        tagToListMapping.put(Tag.Track, tracksList);
        tagToListMapping.put(Tag.Year, yearsList);
        tagToListMapping.put(Tag.Genre, genresList);
        tagToListMapping.put(Tag.Comment, commentsList);
//        tagToListMapping.put(Tag.AlbumArt, albumArt);
//        tagToListMapping.put(Tag.AlbumArtMeta, albumArtMeta);
        
        tagToEditorTextMapping = new HashMap<>();
        tagToEditorTextMapping.put(Tag.FileName, fileNameEditorText);
        tagToEditorTextMapping.put(Tag.Title, titleEditorText);
        tagToEditorTextMapping.put(Tag.Artist, artistEditorText);
        tagToEditorTextMapping.put(Tag.Album, albumEditorText);
        tagToEditorTextMapping.put(Tag.AlbumArtist, albumArtistEditorText);
        tagToEditorTextMapping.put(Tag.Artist, artistEditorText);
        tagToEditorTextMapping.put(Tag.Track, trackEditorText);
        tagToEditorTextMapping.put(Tag.Year, yearEditorText);
        tagToEditorTextMapping.put(Tag.Genre, genreEditorText);
        tagToEditorTextMapping.put(Tag.Comment, commentEditorText);
        
        audioFilesModel.setWorkingDirectory(TEMPFOLDER);
    }
    
    public void reset()
    {
        audioFilesModel.setWorkingDirectory("");
        fileNamesList.clear();
        fileNameEditorText.set("");
        titlesList.clear();
        titleEditorText.set("");
        artistsList.clear();
        artistEditorText.set("");
        albumsList.clear();
        albumEditorText.set("");
        albumArtistsList.clear();
        albumArtistEditorText.set("");
        tracksList.clear();
        trackEditorText.set("");
        yearsList.clear();
        yearEditorText.set("");
        genresList.clear();
        genreEditorText.set("");
        commentsList.clear();
        commentEditorText.set("");
        albumArt.set(null);
        albumArtMeta.set("");
    }
    
    public void setVGMDBParser(VGMDBParser parser)
    {
        vgmdbModel = parser;
    }
    
    // get the tag data for the selected index
    public void requestDataFor(int index, DataCompilationModelCallback cb)
    {
        clearAll();
        audioFilesModel.selectTag(index);
        addAudioModelDataToList();
        
        albumArt.set(audioFilesModel.getAlbumArt());
        albumArtMeta.set(audioFilesModel.getDataForTag(Tag.AlbumArtMeta));
        
        cb.done("DONE");
    }
    
    public void requestDataFor(List<Integer> indicies, DataCompilationModelCallback cb)
    {
        clearAll();
        audioFilesModel.selectTags(indicies);
        addAudioModelDataToList();
        
        albumArt.set(audioFilesModel.getAlbumArt());
        albumArtMeta.set(audioFilesModel.getDataForTag(Tag.AlbumArtMeta));
        
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
        String editorText = tagToEditorTextMapping.get(tag).get();
        List<String> dropDownList = tagToListMapping.get(tag);
        dropDownList.clear();
        tagToEditorTextMapping.get(tag).set(editorText);
        
        System.out.println("editor: " + tagToEditorTextMapping.get(tag));
        
        // add original
        for(String str : additional)
        {
            if(str != null && !str.isEmpty() && !dropDownList.contains(str))
            {
                dropDownList.add(str);
            }
        }
        
        // now handle base on specific
        switch(tag)
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
        if(!trackEditorText.get().isEmpty() && !Utilities.isKeyword(trackEditorText.get()))
        {
            String formatted = String.format("%02d", Integer.valueOf(trackEditorText.get())) + " " + titleEditorText.get() + "." + audioFilesModel.getSelectedFileType();
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
                    titlesList.add(temp);
                }
            }
            catch(NumberFormatException e)
            {
            }
        }
    }
    
    // adding compilation
    private void addAdditionalPossibleArtists(List<String> dropDownList)
    {       
        // add from db
        List<String> possibleArtist = dbManagement.getPossibleDataForTag(Tag.Artist, tagToEditorTextMapping.get(Tag.Artist).get());
        if(possibleArtist != null)
        {
            for(String str : possibleArtist)
            {
                if(!dropDownList.contains(str))
                {
                    artistsList.add(str);
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
                    albumsList.add(temp);
                }
            }
            catch(NumberFormatException e)
            {
            }
        }
    }
    
    // adding compilation
    private void addAdditionalPossibleAlbumArtists(List<String> dropDownList)
    {
        // add from db
        List<String> possibleArtist = dbManagement.getPossibleDataForTag(Tag.AlbumArtist, tagToEditorTextMapping.get(Tag.AlbumArtist).get());
        for(String str : possibleArtist)
        {
            if(!dropDownList.contains(str))
            {
                albumArtistsList.add(str);
            }
        }
        
        // add from vgmdb
        if(vgmdbModel != null)
        {
            String temp = vgmdbModel.getDataForTag(Tag.AlbumArtist);
            if(temp != null && !temp.isEmpty() && !dropDownList.contains(temp))
            {
                albumArtistsList.add(temp);
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
                    yearsList.add(temp);
                }
            }
            catch(NumberFormatException e)
            {
            }
        }
    }
    
    private void addAdditionalPossibleGenres(List<String> dropDownList)
    {        
        List<String> possibleGenres = GenreMapping.containsIgnoreCase(tagToEditorTextMapping.get(Tag.Genre).get());
        for(String genre : possibleGenres)
        {
            if(!dropDownList.contains(genre))
            {
                genresList.add(genre);
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
                String formatted = tagToEditorTextMapping.get(Tag.AlbumArtist).get() + " " + theme +
                    " Single - " + tagToEditorTextMapping.get(Tag.Album).get() + 
                    " [" + tagToEditorTextMapping.get(Tag.Artist).get() + "]";
                if(!dropDownList.contains(formatted))
                {
                    commentsList.add(formatted);
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
                ListProperty<String> field = tagToListMapping.get(tag);
                field.add((String)audioFilesModel.getDataForTag(tag));
            }
        }
    }
    
    public void save()
    {
        // go through each element and set tag
        audioFilesModel.setDataForTag(Tag.FileName, tagToEditorTextMapping.get(Tag.FileName).get());
        audioFilesModel.setDataForTag(Tag.Title, tagToEditorTextMapping.get(Tag.Title).get());
        audioFilesModel.setDataForTag(Tag.Artist, tagToEditorTextMapping.get(Tag.Artist).get());
        audioFilesModel.setDataForTag(Tag.Album, tagToEditorTextMapping.get(Tag.Album).get());
        audioFilesModel.setDataForTag(Tag.AlbumArtist, tagToEditorTextMapping.get(Tag.AlbumArtist).get());
        audioFilesModel.setDataForTag(Tag.Track, tagToEditorTextMapping.get(Tag.Track).get());
        audioFilesModel.setDataForTag(Tag.Year, tagToEditorTextMapping.get(Tag.Year).get());
        audioFilesModel.setDataForTag(Tag.Genre, tagToEditorTextMapping.get(Tag.Genre).get());
        audioFilesModel.setDataForTag(Tag.Comment, tagToEditorTextMapping.get(Tag.Comment).get());
        File artwork = Utilities.saveImage(albumArt.get());
        audioFilesModel.setAlbumArtFromFile(artwork);
        artwork.delete();
        audioFilesModel.setAlbumArtFromFile(Utilities.saveImage(albumArt.get()));
        audioFilesModel.save();
        
        dbManagement.setDataForTag(Tag.AlbumArtist, tagToEditorTextMapping.get(Tag.AlbumArtist).get());
        
        String[] splitArtists = Utilities.splitBySeparators(tagToEditorTextMapping.get(Tag.Artist).get());
        dbManagement.setDataForTag(Tag.Artist, splitArtists);
    }
    
    public void clearAll()
    {
        fileNamesList.clear();
        titlesList.clear();
        artistsList.clear();
        albumsList.clear();
        albumArtistsList.clear();
        tracksList.clear();
        yearsList.clear();
        genresList.clear();
        commentsList.clear();
        albumArt.set(null);
        albumArtMeta.set("");
    }
    
    public void setImage(ImageFrom type, String ummm)
    {
        //TODO set meta too
        switch(type)
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
    
    public ListProperty<String> fileNamesProperty()
    {
        return fileNamesList;
    }
    
    public final List<String> getFileNames()
    {
        return fileNamesList.get();
    }
   
    public SimpleStringProperty fileNameTextProperty()
    {
        return fileNameEditorText;
    }
    
    public final String getFileNameText()
    {
        return fileNameEditorText.get();
    }
     
    public ListProperty<String> titlesProperty()
    {
        return titlesList;
    }
    
    public final List<String> getTitles()
    {
        return titlesList.get();
    }
    
    public SimpleStringProperty titleTextProperty()
    {
        return titleEditorText;
    }
    
    public final String getTitleText()
    {
        return titleEditorText.get();
    }
    
    public ListProperty<String> artistsProperty()
    {
        return artistsList;
    }
    
    public final List<String> getArtists()
    {
        return artistsList.get();
    }
    
    public SimpleStringProperty artistTextProperty()
    {
        return artistEditorText;
    }
    
    public final String getArtistText()
    {
        return artistEditorText.get();
    }
    
    public ListProperty<String> albumsProperty()
    {
        return albumsList;
    }
    
    public final List<String> getAlbums()
    {
        return albumsList.get();
    }
    
    public SimpleStringProperty albumTextProperty()
    {
        return albumEditorText;
    }
    
    public final String getAlbumText()
    {
        return albumEditorText.get();
    }
    
    public ListProperty<String> albumArtistsProperty()
    {
        return albumArtistsList;
    }
    
    public final List<String> getAlbumArtists()
    {
        return albumArtistsList.get();
    }
    
    public SimpleStringProperty albumArtistTextProperty()
    {
        return albumArtistEditorText;
    } 
    
    public final String getAlbumArtistText()
    {
        return albumArtistEditorText.get();
    }
    
    public ListProperty<String> tracksProperty()
    {
        return tracksList;
    }
    
    public final List<String> getTracks()
    {
        return tracksList.get();
    }
    
    public SimpleStringProperty trackTextProperty()
    {
        return trackEditorText;
    }
    
    public final String getTrackText()
    {
        return trackEditorText.get();
    }
    
    public ListProperty<String> yearsProperty()
    {
        return yearsList;
    }
    
    public final List<String> getYearss()
    {
        return yearsList.get();
    }
    
    public SimpleStringProperty yearTextProperty()
    {
        return yearEditorText;
    }
    
    public final String getYearText()
    {
        return yearEditorText.get();
    }
    
    public ListProperty<String> genresProperty()
    {
        return genresList;
    }
    
    public final List<String> getGenres()
    {
        return genresList.get();
    }
    
    public SimpleStringProperty genreTextProperty()
    {
        return genreEditorText;
    }
    
    public final String getGenreText()
    {
        return genreEditorText.get();
    }
    
    public ListProperty<String> commentsProperty()
    {
        return commentsList;
    }
    
    public final List<String> getComments()
    {
        return commentsList.get();
    }
    
    public SimpleStringProperty commentTextProperty()
    {
        return commentEditorText;
    }
    
    public final String getCommentText()
    {
        return commentEditorText.get();
    }
    
    public ObjectProperty<Image> albumArtProperty()
    {
        return albumArt;
    }
    
    public final Image getAlbumArt()
    {
        return albumArt.get();
    }

    public SimpleStringProperty albumArtMetaProperty()
    {
        return albumArtMeta;
    }
    
    public final String getAlbumArtMeta()
    {
        return audioFilesModel.getDataForTag(Tag.AlbumArtMeta);
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
}
