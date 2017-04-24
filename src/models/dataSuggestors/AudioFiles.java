package models.dataSuggestors;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.ID3v24Frames;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.ArtworkFactory;

import models.Settings;
import models.Settings.SettingsKey;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import support.GenreMapping;
import support.Utilities;
import support.Utilities.Tag;


//TODO change to using jaudiotagger, supports more than just mp3 formats
public class AudioFiles implements DataSuggestorBase
{
    ArrayList<MP3File> workingMP3Files;
    ArrayList<String> workingDirectories;

    private ListProperty<String> selectedFileNames; 

    // currently selected information
    private String fileName;
    private String title;
    private String artist;
    private String album;
    private String albumArtist;
    private String track;
    private String year;
    private String genre;
    private String comment;
    private Image albumArt;
    private String albumArtMeta;
    
    private List<Integer> selectedIndicies; //index of selected file
    
    public AudioFiles()
    {
        selectedFileNames = new SimpleListProperty<String>();
        selectedFileNames.set(FXCollections.observableArrayList());
        reset();
    }
    
    private void reset()
    {
        selectedIndicies = new ArrayList<Integer>();
        workingMP3Files = new ArrayList<>();
        workingDirectories = new ArrayList<String>();
        
        selectedFileNames.clear();

        fileName = "";
        title = "";
        artist = "";
        album = "";
        albumArtist = "";
        track = "";
        year = "";
        genre = "";
        comment = "";
        albumArtMeta = "";
    }
    
    public void setWorkingDirectory(String folder)
    {
        reset();
        appendWorkingDirectory(new File[] {new File(folder)});
    }
    
    public void appendWorkingDirectory(File[] files)
    {
        List<File> directoriesQueue = new ArrayList<File>();
        List<File> filesInDirQueue = new ArrayList<File>();
        Arrays.sort(files);
        for(File f : files) // for each file
        {
            String fullPath = f.getPath();
            if(f.isDirectory()) // if folder
            {
                directoriesQueue.add(f);
            }
            // else if correct file
            else if(FilenameUtils.getExtension(fullPath).equals("mp3") || 
                FilenameUtils.getExtension(fullPath).equals("m4a")) // TODO the other formats too
            {
                filesInDirQueue.add(f);
            }
        }
        
        if(!filesInDirQueue.isEmpty())
        {
            File firstFile = filesInDirQueue.get(0);
            workingDirectories.add(firstFile.getPath());
            selectedFileNames.add(Utilities.HEADER_ALBUM + FilenameUtils.getName(firstFile.getParent()));
            workingMP3Files.add(null); // add dummy value
            
            for(File sub : filesInDirQueue)
            {
                try
                {
                    MP3File temp = new MP3File(sub);
                    selectedFileNames.add(FilenameUtils.getName(sub.getPath()));
                    workingMP3Files.add(temp);
                }
                catch (IOException | TagException | ReadOnlyFileException | CannotReadException | InvalidAudioFrameException e)
                {
                    System.out.println("failed on: " + sub.getPath());
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            filesInDirQueue.clear();
        }
        
        if(!directoriesQueue.isEmpty())
        {
            for(File dir : directoriesQueue)
            {
                appendWorkingDirectory(dir.listFiles());
            }
        }
    }
    
    private List<Integer> getAllIndexFromAlbum(int n, boolean includeSelf)
    {
        List<Integer> indicies = new ArrayList<Integer>();
        
        int lower = n - 1;
        int upper = n + 1;
        
        if(includeSelf && !selectedFileNames.get(n).startsWith(Utilities.HEADER_ALBUM))
        {
            indicies.add(n);
        }
        
        while(lower >= 0 && !selectedFileNames.get(lower).startsWith(Utilities.HEADER_ALBUM))
        {
            indicies.add(lower);
            lower--;
        }
        while(upper < workingMP3Files.size() && !selectedFileNames.get(upper).startsWith(Utilities.HEADER_ALBUM))
        {
            indicies.add(upper);
            upper++;
        }
        System.out.println("Indicies Selected: " + Arrays.toString(indicies.toArray(new Integer[0])));
        return indicies;
    }
    
    // set fields to the currently opened file
    public void selectTag(int index)
    {
        System.out.println("SelectFeild: " + index);
        if(index >= 0 && index < workingMP3Files.size())
        {
            if(selectedFileNames.get(index).startsWith(Utilities.HEADER_ALBUM))
            {
                selectTag(index + 1); // initially set a tag, 
                // selectMultipleTags instead
                List<Integer> indicies = getAllIndexFromAlbum(index + 1, true);
                selectTags(indicies);
            }
            else
            {
                selectedIndicies.clear();
                selectedIndicies.add(index);
                MP3File f = workingMP3Files.get(index);
                AbstractID3v2Tag tags = f.getID3v2Tag();
                
                fileName = FilenameUtils.getName(f.getFile().getPath());
                title = tags.getFirst(FieldKey.TITLE);
                artist = tags.getFirst(FieldKey.ARTIST);
                album = tags.getFirst(FieldKey.ALBUM);
                albumArtist = tags.getFirst(FieldKey.ALBUM_ARTIST);
                track = tags.getFirst(FieldKey.TRACK);
                year = tags.getFirst(FieldKey.YEAR);
                genre = tags.getFirst(FieldKey.GENRE);
                comment = tags.getFirst(FieldKey.COMMENT);
                
                Image image = getAlbumArt(tags); 
                albumArt = image;
                
                // sizeInBytes= http://stackoverflow.com/questions/6250200/how-to-get-the-size-of-an-image-in-java
                String mimeType = getAlbumImageMimeType(tags);
                if(image != null)
                {
                    albumArtMeta = mimeType + " : " + (int)image.getWidth() + "x" + (int)image.getHeight();
                }
            }
        }
    }
    
    // set fields to the currently opened file
    public void selectTags(List<Integer> indicies)
    {
        selectedIndicies.clear();
        for(int index : indicies)
        {
            if(index >= 0 && index < workingMP3Files.size())
            {
                selectedIndicies.add(index);
                MP3File f = workingMP3Files.get(index);
                AbstractID3v2Tag tags = f.getID3v2Tag();
                
                fileName = Utilities.getComparedName(fileName, FilenameUtils.getName(f.getFile().getPath()));
                title = Utilities.getComparedName(title, tags.getFirst(FieldKey.TITLE));
                artist = Utilities.getComparedName(artist, tags.getFirst(FieldKey.ARTIST));
                album = Utilities.getComparedName(album, tags.getFirst(FieldKey.ALBUM));
                albumArtist = Utilities.getComparedName(albumArtist, tags.getFirst(FieldKey.ALBUM_ARTIST));
                track = Utilities.getComparedName(track, tags.getFirst(FieldKey.TRACK));
                year = Utilities.getComparedName(year, tags.getFirst(FieldKey.YEAR));
                genre = Utilities.getComparedName(genre, tags.getFirst(FieldKey.GENRE));
                comment = Utilities.getComparedName(comment, tags.getFirst(FieldKey.COMMENT));
                Image image = Utilities.getComparedImage(albumArt, getAlbumArt(tags)); 
                albumArt = image;
                
                // sizeInBytes= http://stackoverflow.com/questions/6250200/how-to-get-the-size-of-an-image-in-java
                String mimeType = getAlbumImageMimeType(tags); // could be incorrect if image different
                if(image != null)
                {
                    albumArtMeta = mimeType + " : " + (int)image.getWidth() + "x" + (int)image.getHeight();
                }
            }
        }
    }
    
    private String getAlbumImageMimeType(AbstractID3v2Tag tag)
    {
        List<Artwork> artworkList = tag.getArtworkList();
        if(!artworkList.isEmpty())
        {
            Artwork first = artworkList.get(0);
//            System.out.println("mime: " + first.getMimeType());
            return first.getMimeType();
        }
        return "";
    }
    
    private Image getAlbumArt(AbstractID3v2Tag tag)
    {
        List<Artwork> artworkList = tag.getArtworkList();
           
        try
        {
            if(!artworkList.isEmpty())
            {
                Artwork first = artworkList.get(0);
                return SwingFXUtils.toFXImage((BufferedImage)first.getImage(), null);
            }
        }
        catch (IOException e)
        {
        }
        return null;
    }
     
    /**
     * used for propagating save data to multiple tags 
     * very manual intensive...
     * make copy of checked tag
     * select all files from album
     * set back checked tags 
     * saveTags()
     */
    private void mockMultisave()
    {
        // make copy
        String propArtist = artist;
        String propAlbum = album;
        String propAlbumArtist = albumArtist;
        String propYear = year;
        String propGenre = genre;
        String propComment = comment;
        Image propAlbumArt = albumArt;    
        
        // select all tags from album
        if(Settings.getInstance().isAnyPropagateSaveOn())
        {
            selectTags(getAllIndexFromAlbum(selectedIndicies.get(0), false));
        }
        
        // set selected values back
        if(Settings.getInstance().isPropagateSaveOn(SettingsKey.PROPAGATE_SAVE_ARTIST))
        {
            setDataForTag(Tag.Artist, propArtist);
        }
        if(Settings.getInstance().isPropagateSaveOn(SettingsKey.PROPAGATE_SAVE_ALBUM))
        {
            setDataForTag(Tag.Album, propAlbum);
        }
        if(Settings.getInstance().isPropagateSaveOn(SettingsKey.PROPAGATE_SAVE_ALBUM_ARTIST))
        {
            setDataForTag(Tag.AlbumArtist, propAlbumArtist);
        }
        if(Settings.getInstance().isPropagateSaveOn(SettingsKey.PROPAGATE_SAVE_YEAR))
        {
            setDataForTag(Tag.Year, propYear);
        }
        if(Settings.getInstance().isPropagateSaveOn(SettingsKey.PROPAGATE_SAVE_GENRE))
        {
            setDataForTag(Tag.Genre, propGenre);
        }
        if(Settings.getInstance().isPropagateSaveOn(SettingsKey.PROPAGATE_SAVE_COMMENT))
        {
            setDataForTag(Tag.Comment, propComment);
        }
        if(Settings.getInstance().isPropagateSaveOn(SettingsKey.PROPAGATE_SAVE_ALBUM_ART))
        {
            File temp = Utilities.saveImage(propAlbumArt);
            setAlbumArtFromFile(temp);
            temp.delete();
        }
        save();
    }
    

    
    // ~~~~~~~~~~~~~~~~~ //
    // Getters & Setters //
    // ~~~~~~~~~~~~~~~~~ //
    
    
    public ListProperty<String> fileNamesProperty()
    {
        return selectedFileNames;
    }
    
    public final List<String> getFileNames()
    {
        return selectedFileNames.get();
    }

    public String getSelectedFileType()
    {
        return FilenameUtils.getExtension(fileName);
    }

    // get the info for a specific tag
    @Override
    public String getDataForTag(Tag tag, String... values)
    {
        String returnValue = "";
        switch(tag)
        {
            case Album:
                returnValue = album;
                break;
            case AlbumArtMeta:
                returnValue = albumArtMeta;
                break;
            case AlbumArtist:
                returnValue = albumArtist;
                break;
            case Artist:
                returnValue = artist;
                break;
            case Comment:
                returnValue = comment;
                break;
            case FileName:
                returnValue = fileName;
                break;
            case Genre:
                returnValue = genre;
                break;
            case Title:
                returnValue = title;
                break;
            case Track:
                returnValue = track;
                break;
            case Year:
                returnValue = year;
                break;
            default:
                System.out.println("no data for tag: " + tag);
                break;
        }
        return returnValue;
    }

    // replace tagData with new tagData
    @Override
    public void setDataForTag(Tag tag, String... values)
    {
        switch(tag)
        {
            case Album:
                album = values[0];
                break;
            case AlbumArtist:
                albumArtist = values[0];
                break;
            case Artist:
                artist = values[0];
                break;
            case Comment:
                comment = values[0];
                break;
            case FileName:
                fileName = values[0];
                break;
            case Genre:
                genre = values[0];
                break;
            case Title:
                title = values[0];
                break;
            case Track:
                track = values[0];
                break;
            case Year:
                year = values[0];
                break;
            default:
                break;
        }
    }
    
    @Override
    public void setAlbumArtFromFile(File file)
    {
        try
        {
            BufferedImage buffImage = ImageIO.read(file);
            Image image = SwingFXUtils.toFXImage(buffImage, null);
            albumArt = Utilities.scaleImage(image, 500, 500, true);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @Override
    public void setAlbumArtFromURL(String url)
    {
        try
        {
            BufferedImage buffImage = ImageIO.read(new URL(url));
            Image image = SwingFXUtils.toFXImage(buffImage, null);
            albumArt = Utilities.scaleImage(image, 500, 500, true);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // save new tags
    @Override
    public void save()
    {
        for(int i : selectedIndicies)
        {
            MP3File f = workingMP3Files.get(i);
            AbstractID3v2Tag tags = f.getID3v2Tag(); // could probably do new tag to remove unnecessary tags
//            ID3v23Tag newTags = new ID3v23Tag();
            if(title != null && !title.isEmpty() && !Utilities.isKeyword(title))
            {
                try
                {
                    tags.setField(FieldKey.TITLE, title);
                }
                catch (KeyNotFoundException | FieldDataInvalidException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if(artist != null && !artist.isEmpty() && !Utilities.isKeyword(artist))
            {
                try
                {
                    tags.setField(FieldKey.ARTIST, artist);
                }
                catch (KeyNotFoundException | FieldDataInvalidException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if(album != null && !album.isEmpty() && !Utilities.isKeyword(album))
            {
                try
                {
                    tags.setField(FieldKey.ALBUM, album);
                }
                catch (KeyNotFoundException | FieldDataInvalidException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if(albumArtist != null && !albumArtist.isEmpty() && !Utilities.isKeyword(albumArtist))
            {
                try
                {
                    tags.setField(FieldKey.ALBUM_ARTIST, albumArtist);
                }
                catch (KeyNotFoundException | FieldDataInvalidException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if(track != null && !track.isEmpty() && !Utilities.isKeyword(track))
            {
                try
                {
                    tags.setField(FieldKey.TRACK, track);
                }
                catch (KeyNotFoundException | FieldDataInvalidException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if(year != null && !year.isEmpty() && !Utilities.isKeyword(year))
            {
                try
                {
                    tags.setField(FieldKey.YEAR, year);
                }
                catch (KeyNotFoundException | FieldDataInvalidException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if(genre != null && !genre.isEmpty() && !Utilities.isKeyword(genre))
            {
                try
                {
                    tags.setField(FieldKey.GENRE, genre);
                }
                catch (KeyNotFoundException | FieldDataInvalidException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if(comment != null && !comment.isEmpty() && !Utilities.isKeyword(comment))
            {
                try
                {
                    tags.setField(FieldKey.COMMENT, comment);
                }
                catch (KeyNotFoundException | FieldDataInvalidException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if(albumArt != null && !Utilities.isKeyword(albumArt))
            {
                try
                {
                    File temp = Utilities.saveImage(albumArt);
                    tags.deleteArtworkField();
                    tags.setField(ArtworkFactory.createArtworkFromFile(temp));
                    temp.delete();
                }
                catch (FieldDataInvalidException | IOException e1)
                {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
            
            f.setID3v2Tag(tags);
            
            try
            {
                String originalName = FilenameUtils.getName(f.getFile().getPath());
                String fileNamePrevious = ""; // used if multiple indicies have been selected
                String path = f.getFile().getParentFile().getPath();
                if(Utilities.isKeyword(fileName)) // if keyword
                {
                    // save current name (keyword)
                    fileNamePrevious = fileName;
                    // revert to original name
                    fileName = FilenameUtils.getName(f.getFile().getPath());
                }
                System.out.println("saving: " + path + File.separator + originalName);
                f.save();
                
                if(!fileName.equals(originalName)) // saving to a different name
                {
                    System.out.println("saving new name: " + path + File.separator + fileName);
                    Files.copy(Paths.get(path + File.separator + originalName), 
                        Paths.get(path + File.separator + fileName), 
                        StandardCopyOption.REPLACE_EXISTING);
                    Files.delete(Paths.get(path + File.separator + originalName));
                }
//                f.save(new File(path + File.separator + fileName + ".temp"));
//                Files.copy(Paths.get(path + File.separator + fileName + ".temp"), 
//                        Paths.get(path + File.separator + fileName), 
//                        StandardCopyOption.REPLACE_EXISTING);
                
                if(!fileNamePrevious.isEmpty())
                {
                    // set back fileName to keyword so next loop 
                    // will use original name and not the same name
                    fileName = fileNamePrevious;
                }
            }
            catch (IOException | TagException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if(selectedIndicies.size() == 1 && Settings.getInstance().isAnyPropagateSaveOn())
        {
            mockMultisave();
        }
    }

    @Override
    public List<String> getPossibleDataForTag(Tag tag, String values)
    {
        return null;
    }

    @Override
    public Image getAlbumArt()
    {
        return albumArt;
    }
}
