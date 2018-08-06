package support.structure;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.ikersaro.utilities.Logger;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.images.Artwork;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import support.util.Utilities.EditorTag;



/**
 * Extension of MP3File class for extra functionality
 * 
 * @author Ikersaro
 *
 */
public class AudioFile implements Logger {
    private MP3File file;
    private AbstractID3v2Tag tags;
    private String currentFileName;

    public AudioFile(File file) throws IOException, TagException, ReadOnlyFileException, CannotReadException, InvalidAudioFrameException {
        this(new MP3File(file));
    }

    public AudioFile(MP3File file) {
        this.file = file;
        tags = file.getID3v2Tag();
        currentFileName = FilenameUtils.getName(file.getFile().getPath());
    }

    public AbstractID3v2Tag getRawTags() {
        return tags;
    }

    public String get(EditorTag tag) {
        String str = null;
        switch (tag) {
            case ALBUM:
                str = tags.getFirst(FieldKey.ALBUM);
                break;
            case ALBUM_ART:

                break;
            case ALBUM_ARTIST:
                str = tags.getFirst(FieldKey.ALBUM_ARTIST);
                break;
            case ALBUM_ART_META:
                // sizeInBytes=http://stackoverflow.com/questions/6250200/how-to-get-the-size-of-an-image-in-java
                //mimeType + " : " + (int)image.getWidth() + "x" + (int)image.getHeight()
                Image temp = getAlbumArt();
                if(temp != null) {
                    str = getMimeType() + " : " + (int)getAlbumArt().getWidth() + "x" + (int)getAlbumArt().getHeight();
                }
                break;
            case ARTIST:
                str = tags.getFirst(FieldKey.ARTIST);
                break;
            case COMMENT:
                str = tags.getFirst(FieldKey.COMMENT);
                break;
            case FILE_NAME:
                str = getNewFileName();
                break;
            case GENRE:
                str = tags.getFirst(FieldKey.GENRE);
                break;
            case TITLE:
                str = tags.getFirst(FieldKey.TITLE);
                break;
            case TRACK:
                str = String.format("%02d", Integer.parseInt(tags.getFirst(FieldKey.TRACK)));
                break;
            case YEAR:
                str = tags.getFirst(FieldKey.YEAR);
                break;
            default:
                break;
        }
        return str;
    }

    public void setAlbumArt(Artwork img) {
        try {
            tags.setField(img);
        }
        catch (FieldDataInvalidException e) {
            e.printStackTrace();
        }
    }

    public void setField(EditorTag tag, String str) {
        if(str == null) {
            str = "";
        }
        else {
            str = str.trim();
        }
        try {
            switch (tag) {
                case ALBUM:
                    tags.setField(FieldKey.ALBUM, str);
                    break;
                case ALBUM_ART:

                    break;
                case ALBUM_ARTIST:
                    tags.setField(FieldKey.ALBUM_ARTIST, str);;
                    break;
                case ALBUM_ART_META:
                    break;
                case ARTIST:
                    tags.setField(FieldKey.ARTIST, str);
                    break;
                case COMMENT:
                    tags.setField(FieldKey.COMMENT, str);
                    break;
                case FILE_NAME:
                    if(!str.endsWith(getFileType())) {
                        str += "." + getFileType();
                    }
                    currentFileName = str;
                    break;
                case GENRE:
                    tags.setField(FieldKey.GENRE, str);
                    break;
                case TITLE:
                    tags.setField(FieldKey.TITLE, str);
                    break;
                case TRACK:
                    tags.setField(FieldKey.TRACK, str);
                    break;
                case YEAR:
                    tags.setField(FieldKey.YEAR, str);
                    break;
                default:
                    break;
            }
        }
        catch (KeyNotFoundException | FieldDataInvalidException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            file.setID3v2Tag(tags);
            file.save();
            if(!currentFileName.equals(getOriginalFileName())) // saving to a different name
            {
                String path = file.getFile().getParentFile().getPath();
                String newNamePath = path + File.separator + currentFileName; // name of new file
                info("File: " + getOriginalFileName() + " saving as: " + newNamePath);
                if(!file.getFile().renameTo(new File(newNamePath))) {
                    error("unable to save");
                }

                //                // update ui list view
                //                songListMP3Files.remove(i); // remove original file
                //                songListMP3Files.add(i, new MP3File(new File(newNamePath))); // update to new file
                //
                //                songListFileNames.add(i, fileName); // add new filename into list
                //                songListFileNames.remove(i + 1); // remove original filename,
                // (order matters, causes an ui update and triggering a selectIndex which changes selected Index)
            }
        }
        catch (IOException | TagException e) {
            e.printStackTrace();
        }
    }

    /**
     * Extract the album art image
     * 
     * @param tag Audio Tag to extract from
     * @return Image
     */
    public Image getAlbumArt() {
        List<Artwork> artworkList = tags.getArtworkList();

        try {
            if(!artworkList.isEmpty()) {
                Artwork first = artworkList.get(0);
                return SwingFXUtils.toFXImage((BufferedImage)first.getImage(), null);
            }
        }
        catch (IOException e) {
        }
        return null;
    }

    /**
     * @return Album Art Extension Type
     */
    public String getMimeType() {
        String str = "";
        List<Artwork> artworkList = tags.getArtworkList();
        if(!artworkList.isEmpty()) {
            Artwork first = artworkList.get(0);
            str = first.getMimeType();
        }
        return str;
    }


    /**
     * @return Original file name with extension
     */
    public String getOriginalFileName() {
        return FilenameUtils.getName(file.getFile().getPath());
    }

    /**
     * @return Original file extension
     */
    public String getFileType() {
        return FilenameUtils.getExtension(getOriginalFileName());
    }

    /**
     * @return New name of file
     */
    public String getNewFileName() {
        return currentFileName;
    }

    /**
     * @return {@link File}
     */
    public File getFile() {
        return file.getFile();
    }
}
