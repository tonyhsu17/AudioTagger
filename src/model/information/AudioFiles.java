package model.information;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
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
import org.jaudiotagger.tag.images.ArtworkFactory;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import model.Settings;
import model.Settings.SettingsKey;
import model.base.InformationBase;
import model.base.TagBase;
import support.Constants;
import support.Logger;
import support.util.ImageUtil;
import support.util.StringUtil;
import support.util.Utilities.EditorTag;



public class AudioFiles implements InformationBase, Logger {
    private ArrayList<String> workingDirectories;

    // list view display, includes album headers
    private ListProperty<String> selectedFileNames;
    // audio files, null placeholders for album headers
    private ArrayList<MP3File> workingMP3Files;

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

    private List<Integer> selectedindices; // index of selected file
    private List<Integer> selectedindicesCopy; // copy of index of selected file, to revert back after saving

    public AudioFiles() {
        selectedFileNames = new SimpleListProperty<String>();
        selectedFileNames.set(FXCollections.observableArrayList());

        reset();
    }

    private void reset() {
        selectedindices = new ArrayList<Integer>();
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

    /**
     * Set the working directory and load in all audio files within
     * 
     * @param folder Directory Path
     */
    public void setWorkingDirectory(String folder) {
        reset();
        appendWorkingDirectory(new File[] {new File(folder)});
    }

    /**
     * Append more directories to the list
     * 
     * @param files Directories or files
     */
    public void appendWorkingDirectory(File[] files) {
        List<File> directoriesQueue = new ArrayList<File>();
        List<File> filesInDirQueue = new ArrayList<File>();
        Arrays.sort(files);
        for(File f : files) {// for each file
            String fullPath = f.getPath();
            if(f.isDirectory()) {// if folder
                directoriesQueue.add(f);
            }
            // else if correct file
            else if(FilenameUtils.getExtension(fullPath).equals("mp3") || FilenameUtils.getExtension(fullPath).equals("m4a")) {// TODO the other formats too
                filesInDirQueue.add(f);
            }
        }

        if(!filesInDirQueue.isEmpty()) {
            File firstFile = filesInDirQueue.get(0);
            workingDirectories.add(firstFile.getPath());
            selectedFileNames.add(Constants.HEADER_ALBUM + FilenameUtils.getName(firstFile.getParent()));
            workingMP3Files.add(null); // add dummy value

            for(File sub : filesInDirQueue) {
                try {
                    MP3File temp = new MP3File(sub);
                    selectedFileNames.add(FilenameUtils.getName(sub.getPath()));
                    workingMP3Files.add(temp);
                }
                catch (IOException | TagException | ReadOnlyFileException | CannotReadException | InvalidAudioFrameException e) {
                    error("failed on: " + sub.getPath());
                    e.printStackTrace();
                }
            }
            filesInDirQueue.clear();
        }

        if(!directoriesQueue.isEmpty()) {
            for(File dir : directoriesQueue) {
                appendWorkingDirectory(dir.listFiles());
            }
        }
    }

    /**
     * Returns a list of indices that are part of the selected album
     * 
     * @param n Selected index
     * @param includeSelf Include selected index in list
     * @return list of indices in the same album
     */
    private List<Integer> getAllIndexFromAlbum(int n, boolean includeSelf) {
        List<Integer> indices = new ArrayList<Integer>();

        int lower = n - 1;
        int upper = n + 1;

        if(includeSelf && workingMP3Files.get(n) != null) {
            indices.add(n);
        }

        while(lower >= 0 && workingMP3Files.get(lower) != null) {
            indices.add(lower);
            lower--;
        }
        while(upper < workingMP3Files.size() && workingMP3Files.get(upper) != null) {
            indices.add(upper);
            upper++;
        }
        debug("indices Selected: " + Arrays.toString(indices.toArray(new Integer[0])));
        return indices;
    }

    /**
     * Set fields to the currently selected index
     * 
     * @param index Audio file to open up and modify
     */
    public void selectTag(int index) {
        // should probably condense this with selectTag(int indices)
        debug("SelectFeild: " + index);
        if(index >= 0 && index < workingMP3Files.size()) {
            if(selectedFileNames.get(index).startsWith(Constants.HEADER_ALBUM)) {
                selectTag(index + 1); // initially set a tag,
                // selectMultipleTags instead
                List<Integer> indices = getAllIndexFromAlbum(index + 1, true);
                selectTags(indices);
            }
            else {
                selectedindices.clear();
                selectedindices.add(index);
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

                // sizeInBytes=http://stackoverflow.com/questions/6250200/how-to-get-the-size-of-an-image-in-java
                String mimeType = getAlbumImageMimeType(tags);
                if(image != null) {
                    albumArtMeta = mimeType + " : " + (int)image.getWidth() + "x" + (int)image.getHeight();
                }
            }
        }
    }

    /**
     * Set fields to the currently selected indices
     * 
     * @param indices Audio file to open up and modify
     */
    public void selectTags(List<Integer> indices) {
        selectedindices.clear();

        List<Integer> temp = new ArrayList<Integer>();
        // sanitize the indices to contain only valid audio indices (ie convert folder index to
        // file indices)
        for(int index : indices) {
            if(index >= 0 &&
               index < workingMP3Files.size() &&
               selectedFileNames.get(index).startsWith(Constants.HEADER_ALBUM)) {
                List<Integer> albumSelectedindices = getAllIndexFromAlbum(index + 1, true);
                temp.addAll(albumSelectedindices);
            }
            else {
                temp.add(index);
            }
        }

        for(int index : temp) {
            if(index >= 0 && index < workingMP3Files.size()) {
                selectedindices.add(index);
                MP3File f = workingMP3Files.get(index);
                AbstractID3v2Tag tags = f.getID3v2Tag();

                fileName = StringUtil.getComparedName(fileName, FilenameUtils.getName(f.getFile().getPath()));
                title = StringUtil.getComparedName(title, tags.getFirst(FieldKey.TITLE));
                artist = StringUtil.getComparedName(artist, tags.getFirst(FieldKey.ARTIST));
                album = StringUtil.getComparedName(album, tags.getFirst(FieldKey.ALBUM));
                albumArtist = StringUtil.getComparedName(albumArtist, tags.getFirst(FieldKey.ALBUM_ARTIST));
                track = StringUtil.getComparedName(track, tags.getFirst(FieldKey.TRACK));
                year = StringUtil.getComparedName(year, tags.getFirst(FieldKey.YEAR));
                genre = StringUtil.getComparedName(genre, tags.getFirst(FieldKey.GENRE));
                comment = StringUtil.getComparedName(comment, tags.getFirst(FieldKey.COMMENT));
                Image image = ImageUtil.getComparedImage(albumArt, getAlbumArt(tags));
                albumArt = image;

                // sizeInBytes=http://stackoverflow.com/questions/6250200/how-to-get-the-size-of-an-image-in-java
                String mimeType = getAlbumImageMimeType(tags); // could be incorrect if image
                                                               // different
                if(image != null) {
                    albumArtMeta = mimeType + " : " + (int)image.getWidth() + "x" + (int)image.getHeight();
                }
            }
        }
    }

    /**
     * Extract the album art image
     * 
     * @param tag Audio Tag to extract from
     * @return Image
     */
    private Image getAlbumArt(AbstractID3v2Tag tag) {
        List<Artwork> artworkList = tag.getArtworkList();

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
     * Extract the album art mime type
     * 
     * @param tag Audio Tag to extract from
     * @return mime type
     */
    private String getAlbumImageMimeType(AbstractID3v2Tag tag) {
        List<Artwork> artworkList = tag.getArtworkList();
        if(!artworkList.isEmpty()) {
            Artwork first = artworkList.get(0);
            return first.getMimeType();
        }
        return "";
    }

    /**
     * Used for propagating save data to multiple tags, very manual intensive...
     * make copy of checked tag, select all files from album, set back checked tags
     * saveTags()
     */
    private void mockMultisave() {
        // make copy
        String propArtist = artist;
        String propAlbum = album;
        String propAlbumArtist = albumArtist;
        String propYear = year;
        String propGenre = genre;
        String propComment = comment;
        Image propAlbumArt = albumArt;

        // select all tags from album
        if(Settings.getInstance().isAnyPropagateSaveOn()) {
            selectTags(getAllIndexFromAlbum(selectedindices.get(0), false));
        }

        // set selected values back
        if(Settings.getInstance().isPropagateSaveOn(SettingsKey.PROPAGATE_SAVE_ARTIST)) {
            setDataForTag(EditorTag.ARTIST, propArtist);
        }
        if(Settings.getInstance().isPropagateSaveOn(SettingsKey.PROPAGATE_SAVE_ALBUM)) {
            setDataForTag(EditorTag.ALBUM, propAlbum);
        }
        if(Settings.getInstance().isPropagateSaveOn(SettingsKey.PROPAGATE_SAVE_ALBUM_ARTIST)) {
            setDataForTag(EditorTag.ALBUM_ARTIST, propAlbumArtist);
        }
        if(Settings.getInstance().isPropagateSaveOn(SettingsKey.PROPAGATE_SAVE_YEAR)) {
            setDataForTag(EditorTag.YEAR, propYear);
        }
        if(Settings.getInstance().isPropagateSaveOn(SettingsKey.PROPAGATE_SAVE_GENRE)) {
            setDataForTag(EditorTag.GENRE, propGenre);
        }
        if(Settings.getInstance().isPropagateSaveOn(SettingsKey.PROPAGATE_SAVE_COMMENT)) {
            setDataForTag(EditorTag.COMMENT, propComment);
        }
        if(Settings.getInstance().isPropagateSaveOn(SettingsKey.PROPAGATE_SAVE_ALBUM_ART)) {
            File temp = ImageUtil.saveImage(propAlbumArt);
            setAlbumArtFromFile(temp);
            temp.delete();
        }
        save();
    }

    // ~~~~~~~~~~~~~~~~~ //
    // Getters & Setters //
    // ~~~~~~~~~~~~~~~~~ //

    public ListProperty<String> fileNamesProperty() {
        return selectedFileNames;
    }

    /**
     * Returns the display list of folders with audio files within them
     * 
     * @return n Folders + n Audio Files
     */
    public final List<String> getFileNames() {
        return selectedFileNames.get();
    }

    public String getSelectedFileType() {
        return FilenameUtils.getExtension(fileName);
    }

    @Override
    public String getDisplayKeywordTagClassName() {
        return "Audio";
    }

    @Override
    public void setAlbumArtFromFile(File file) {
        try {
            BufferedImage buffImage = ImageIO.read(file);
            Image image = SwingFXUtils.toFXImage(buffImage, null);
            albumArt = ImageUtil.scaleImage(image, 500, 500, true);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setAlbumArtFromURL(String url) {
        try {
            BufferedImage buffImage = ImageIO.read(new URL(url));
            Image image = SwingFXUtils.toFXImage(buffImage, null);
            albumArt = ImageUtil.scaleImage(image, 500, 500, true);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    // save new tags
    @Override
    public void save() {
        info("Starting Save: " + Arrays.toString(selectedindices.toArray(new Integer[0])));
        // if save triggered, copy selected indices for later reverting back incase multisave is on
        if(selectedindicesCopy == null) {
            selectedindicesCopy = new ArrayList<Integer>();
            selectedindicesCopy.addAll(selectedindices);
        }

        for(int i : selectedindices) {
            MP3File f = workingMP3Files.get(i);
            AbstractID3v2Tag tags = f.getID3v2Tag(); // could probably do new tag to remove
                                                     // unnecessary tags
                                                     // ID3v23Tag newTags = new ID3v23Tag();
            if(title != null && !title.isEmpty() && !StringUtil.isKeyword(title)) {
                try {
                    tags.setField(FieldKey.TITLE, title);
                }
                catch (KeyNotFoundException | FieldDataInvalidException e) {
                    e.printStackTrace();
                }
            }
            if(artist != null && !artist.isEmpty() && !StringUtil.isKeyword(artist)) {
                try {
                    tags.setField(FieldKey.ARTIST, artist);
                }
                catch (KeyNotFoundException | FieldDataInvalidException e) {
                    e.printStackTrace();
                }
            }
            if(album != null && !album.isEmpty() && !StringUtil.isKeyword(album)) {
                try {
                    tags.setField(FieldKey.ALBUM, album);
                }
                catch (KeyNotFoundException | FieldDataInvalidException e) {
                    e.printStackTrace();
                }
            }
            if(albumArtist != null && !albumArtist.isEmpty() && !StringUtil.isKeyword(albumArtist)) {
                try {
                    tags.setField(FieldKey.ALBUM_ARTIST, albumArtist);
                }
                catch (KeyNotFoundException | FieldDataInvalidException e) {
                    e.printStackTrace();
                }
            }
            if(track != null && !track.isEmpty() && !StringUtil.isKeyword(track)) {
                try {
                    tags.setField(FieldKey.TRACK, track);
                }
                catch (KeyNotFoundException | FieldDataInvalidException e) {
                    e.printStackTrace();
                }
            }
            if(year != null && !year.isEmpty() && !StringUtil.isKeyword(year)) {
                try {
                    tags.setField(FieldKey.YEAR, year);
                }
                catch (KeyNotFoundException | FieldDataInvalidException e) {
                    e.printStackTrace();
                }
            }
            if(genre != null && !genre.isEmpty() && !StringUtil.isKeyword(genre)) {
                try {
                    tags.setField(FieldKey.GENRE, genre);
                }
                catch (KeyNotFoundException | FieldDataInvalidException e) {
                    e.printStackTrace();
                }
            }
            if(comment != null && !comment.isEmpty() && !StringUtil.isKeyword(comment)) {
                try {
                    tags.setField(FieldKey.COMMENT, comment);
                }
                catch (KeyNotFoundException | FieldDataInvalidException e) {
                    e.printStackTrace();
                }
            }
            if(albumArt != null && !ImageUtil.isKeyword(albumArt)) {
                try {
                    File temp = ImageUtil.saveImage(albumArt);
                    tags.deleteArtworkField();
                    tags.setField(ArtworkFactory.createArtworkFromFile(temp));
                    temp.delete();
                }
                catch (FieldDataInvalidException | IOException e1) {
                    e1.printStackTrace();
                }
            }

            f.setID3v2Tag(tags);

            try {
                String originalName = FilenameUtils.getName(f.getFile().getPath());
                String fileNamePrevious = ""; // used if multiple indices have been selected
                String path = f.getFile().getParentFile().getPath();
                if(StringUtil.isKeyword(fileName) || selectedindices.size() != 1) // if keyword
                {
                    // save current name (keyword)
                    fileNamePrevious = fileName;
                    // revert to original name
                    fileName = FilenameUtils.getName(f.getFile().getPath());
                }
                info("saving: " + path + File.separator + originalName);
                f.save();

                String extension = ""; // add back extension if missing
                if(!fileName.endsWith(FilenameUtils.getExtension(originalName))) {
                    extension = "." + FilenameUtils.getExtension(originalName);
                }
                fileName += extension;

                if(!fileName.equals(originalName)) // saving to a different name
                {
                    // copy file to new file ame
                    String newNamePath = path + File.separator + fileName;
                    info("saving new name: " + newNamePath);
                    Files.copy(Paths.get(path + File.separator + originalName), Paths.get(newNamePath),
                        StandardCopyOption.REPLACE_EXISTING);
                    // delete original file
                    Files.delete(Paths.get(path + File.separator + originalName));

                    // update ui list view
                    workingMP3Files.remove(i); // remove original file
                    workingMP3Files.add(i, new MP3File(new File(newNamePath))); // update to new file

                    selectedFileNames.add(i, fileName); // add new filename into list
                    selectedFileNames.remove(i + 1); // remove original filename,
                    // (order matters, causes an ui update and triggering a selectIndex which changes selected Index)
                }

                if(!fileNamePrevious.isEmpty()) {
                    // set back fileName to keyword so next loop
                    // will use original name and not the same name
                    fileName = fileNamePrevious;
                }
            }
            catch (IOException | TagException | ReadOnlyFileException | CannotReadException | InvalidAudioFrameException e) {
                e.printStackTrace();
            }
        }
        if(selectedindices.size() == 1 && Settings.getInstance().isAnyPropagateSaveOn()) {
            mockMultisave();
        }
        // now revert indices to original
        // need to check for null as original call + mockMultiSave will trigger it twice
        if(selectedindicesCopy != null) {
            selectedindices.clear();
            selectedindices.addAll(selectedindicesCopy);
            selectedindicesCopy = null;
        }

    }

    @Override
    public Image getAlbumArt() {
        return albumArt;
    }

    @Override
    public EditorTag[] getAdditionalTags() {
        return null;
    }

    @Override
    public List<TagBase<?>> getKeywordTags() {
        List<TagBase<?>> keywords = new ArrayList<>();
        keywords.add(EditorTag.ALBUM);
        keywords.add(EditorTag.ALBUM_ARTIST);
        keywords.add(EditorTag.ARTIST);
        keywords.add(EditorTag.COMMENT);
        keywords.add(EditorTag.FILE_NAME);
        keywords.add(EditorTag.GENRE);
        keywords.add(EditorTag.TITLE);
        keywords.add(EditorTag.TRACK);
        keywords.add(EditorTag.YEAR);
        return keywords;
    }

    // get the info for a specific tag
    @Override
    public String getDataForTag(TagBase<?> tag, String... extraArgs) {
        String returnValue = "";
        if(tag == EditorTag.ALBUM) {
            returnValue = album;
        }
        else if(tag == EditorTag.ALBUM_ART_META) {
            returnValue = albumArtMeta;
        }
        else if(tag == EditorTag.ALBUM_ARTIST) {
            returnValue = albumArtist;
        }
        else if(tag == EditorTag.ARTIST) {
            returnValue = artist;
        }
        else if(tag == EditorTag.COMMENT) {
            returnValue = comment;
        }
        else if(tag == EditorTag.FILE_NAME) {
            returnValue = fileName;
        }
        else if(tag == EditorTag.GENRE) {
            returnValue = genre;
        }
        else if(tag == EditorTag.TITLE) {
            returnValue = title;
        }
        else if(tag == EditorTag.TRACK) {
            if(StringUtil.isKeyword(track)) {
                returnValue = track;
            }
            else {
                returnValue = String.format("%02d", Integer.valueOf(track));
            }
        }
        else if(tag == EditorTag.YEAR) {
            returnValue = year;
        }
        else {
            info("no data for tag: " + tag);
        }
        return returnValue;
    }

    // replace tagData with new tagData
    @Override
    public void setDataForTag(TagBase<?> tag, String... values) {
        if(tag == EditorTag.ALBUM) {
            album = values[0];
        }
        else if(tag == EditorTag.ALBUM_ART_META) {
            albumArtMeta = values[0];
        }
        else if(tag == EditorTag.ALBUM_ARTIST) {
            albumArtist = values[0];
        }
        else if(tag == EditorTag.ARTIST) {
            artist = values[0];
        }
        else if(tag == EditorTag.COMMENT) {
            comment = values[0];
        }
        else if(tag == EditorTag.FILE_NAME) {
            fileName = values[0];
        }
        else if(tag == EditorTag.GENRE) {
            genre = values[0];
        }
        else if(tag == EditorTag.TITLE) {
            title = values[0];
        }
        else if(tag == EditorTag.TRACK) {
            track = values[0];
        }
        else if(tag == EditorTag.YEAR) {
            year = values[0];
        }
    }

    @Override
    public List<String> getPossibleDataForTag(TagBase<?> tag, String values) {
        // TODO Auto-generated method stub
        return null;
    }
}
