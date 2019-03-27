package support.structure;

import javafx.scene.image.Image;
import support.util.Utilities.EditorTag;



/**
 * Class to hold tag values
 * 
 * @author Ikersaro
 *
 */
public class TagDetails {
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

    public TagDetails() {
        reset();
    }

    public void reset() {
        fileName = "";
        title = "";
        artist = "";
        album = "";
        albumArtist = "";
        track = "";
        year = "";
        genre = "";
        comment = "";
        albumArtMeta = null;
    }

    public String get(EditorTag tag) {
        String str = null;
        switch (tag) {
            case ALBUM:
                str = album;
                break;
            case ALBUM_ART:

                break;
            case ALBUM_ARTIST:
                str = albumArtist;
                break;
            case ALBUM_ART_META:
                str = albumArtMeta;
                break;
            case ARTIST:
                str = artist;
                break;
            case COMMENT:
                str = comment;
                break;
            case FILE_NAME:
                str = fileName;
                break;
            case GENRE:
                str = genre;
                break;
            case TITLE:
                str = title;
                break;
            case TRACK:
                str = track;
                break;
            case YEAR:
                str = year;
                break;
            default:
                break;

        }
        return str;
    }

    public void set(EditorTag tag, String value) {
        switch (tag) {
            case ALBUM:
                album = value;
                break;
            case ALBUM_ART:
                //noop
                break;
            case ALBUM_ARTIST:
                albumArtist = value;
                break;
            case ALBUM_ART_META:
                albumArtMeta = value;
                break;
            case ARTIST:
                artist = value;
                break;
            case COMMENT:
                comment = value;
                break;
            case FILE_NAME:
                fileName = value;
                break;
            case GENRE:
                genre = value;
                break;
            case TITLE:
                title = value;
                break;
            case TRACK:
                track = value;
                break;
            case YEAR:
                year = value;
                break;
            default:
                break;

        }
    }

    public Image getAlbumArt() {
        return albumArt;
    }

    public void setAlbumArt(Image albumArt) {
        this.albumArt = albumArt;
    }

    @Override
    public String toString() {
        return "FILE_NAME: " + fileName + ", TITILE: " + title + ", ARTIST: " + artist + ", ALBUM: " + album + 
            ", ALBUM_ARTIST: " + albumArtist + ", TRACK:" + track + ", YEAR: " + year + ", GENRE: " + genre + 
            ", COMMENT: " + comment + " HAS_ALBUM_ART: " + (albumArt != null ? true : false);
    }
}
