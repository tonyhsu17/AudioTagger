package support.structure;

import ealvatag.audio.AudioFileIO;
import ealvatag.audio.exceptions.CannotReadException;
import ealvatag.audio.exceptions.CannotWriteException;
import ealvatag.audio.exceptions.InvalidAudioFrameException;
import ealvatag.tag.FieldDataInvalidException;
import ealvatag.tag.FieldKey;
import ealvatag.tag.Tag;
import ealvatag.tag.TagException;
import ealvatag.tag.images.Artwork;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.apache.commons.io.FilenameUtils;
import org.tonyhsu17.utilities.Logger;
import org.tonyhsu17.utilities.StringUtils;
import support.util.Utilities.EditorTag;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;



/**
 * Extension of MP3File class for extra functionality
 *
 * @author Ikersaro
 */
public class AudioFile implements Logger {
    private ealvatag.audio.AudioFile file;
    private Tag tags;
    private String currentFileName;

    public AudioFile(File file) throws IOException, TagException, CannotReadException, InvalidAudioFrameException {
        this(AudioFileIO.read(file));

    }

    public AudioFile(ealvatag.audio.AudioFile file) {
        this.file = file;
        tags = file.getTag().orNull();
        currentFileName = FilenameUtils.getName(file.getFile().getPath());
    }

    public Tag getRawTags() {
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
                try {
                    str = String.format("%02d", Integer.parseInt(tags.getFirst(FieldKey.TRACK)));
                }
                catch (NumberFormatException e) {
                    str = "";
                }
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
            tags = tags.deleteArtwork().addArtwork(img);
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
                    tags.setField(FieldKey.ALBUM_ARTIST, str);
                    ;
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
                    debug(tags.getValue(FieldKey.YEAR));
                    tags.setField(FieldKey.YEAR, str);
                    break;
                default:
                    break;
            }
        }
        catch (FieldDataInvalidException e) {
            e.printStackTrace();
        }
    }

    public void deleteTags() {
        tags = file.setNewDefaultTag();
    }

    public void save() {
        try {
            //            file.setID3v2Tag(tags);
            if(!currentFileName.equals(getOriginalFileName())) // saving to a different name
            {
                String oldPath = file.getFile().getPath();
                String parent = file.getFile().getParentFile().getPath();
                String newNamePath = parent + File.separator + getFileNameNoExtention(StringUtils.santizeFileName(currentFileName, ""), getFileType()); // name of new file
                info("File: " + getOriginalFileName() + " saving as: " + newNamePath);
                file.saveAs(newNamePath);
                Files.delete(Paths.get(oldPath));
            }
            else {
                file.save();
            }
        }
        catch (CannotWriteException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Extract the album art image
     *
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

    private String getFileNameNoExtention(String str, String ext) {
        return str.substring(0, str.length() - ext.length() - (ext.startsWith(".") ? 0 : 1));
    }
}
