package model.information;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import model.base.InformationBase;
import model.base.TagBase;
import support.Constants;
import support.Logger;
import support.structure.AudioFile;
import support.structure.TagDetails;
import support.util.ImageUtil;
import support.util.StringUtil;
import support.util.Utilities.EditorTag;



public class AudioFilesModel implements InformationBase, Logger {
    public interface AudioFilesModelTagInfo {
        public void getTags(TagDetails details);
    }

    private ArrayList<String> songListDirectories;

    // list view display, includes album headers
    private ListProperty<String> songListFileNames;
    // audio files, null placeholders for album headers
    private ArrayList<AudioFile> songListMP3Files;

    // currently selected information //TODO remove, use cb and set value in combobox
    private TagDetails editorMeta;

    private List<Integer> selectedindices; // index of selected file


    public AudioFilesModel() {
        songListFileNames = new SimpleListProperty<String>();
        songListFileNames.set(FXCollections.observableArrayList());
        editorMeta = new TagDetails();
        reset();
    }

    private void reset() {
        selectedindices = new ArrayList<Integer>();
        songListMP3Files = new ArrayList<>();
        songListDirectories = new ArrayList<String>();

        songListFileNames.clear();
        editorMeta.reset();
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
            songListDirectories.add(firstFile.getPath());
            songListFileNames.add(Constants.HEADER_ALBUM + FilenameUtils.getName(firstFile.getParent()));
            songListMP3Files.add(null); // add dummy value

            for(File sub : filesInDirQueue) {
                try {
                    AudioFile temp = new AudioFile(sub);
                    songListFileNames.add(FilenameUtils.getName(sub.getPath()));
                    songListMP3Files.add(temp);
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
     * Returns a list of indices that are part of the selected album.
     * 
     * @param n Selected index, must not be a header
     * @param includeSelf Include selected index in list
     * @return list of indices in the same album
     */
    private List<Integer> getAllIndexFromAlbum(int n, boolean includeSelf) {
        List<Integer> indices = new ArrayList<Integer>();

        int lower = n - 1;
        int upper = n + 1;

        if(includeSelf && songListMP3Files.get(n) != null) {
            indices.add(n);
        }

        while(lower >= 0 && songListMP3Files.get(lower) != null) {
            indices.add(lower);
            lower--;
        }
        while(upper < songListMP3Files.size() && songListMP3Files.get(upper) != null) {
            indices.add(upper);
            upper++;
        }
        debug("indices Selected: " + Arrays.toString(indices.toArray(new Integer[0])));
        return indices;
    }

    /**
     * Returns a list of indices that are part of the album.
     * 
     * @param n Must be a header
     * @return list of indices part of the album
     */
    private List<Integer> getAllIndexForHeader(int n) {
        List<Integer> indices = new ArrayList<Integer>();
        int index = n + 1;
        while(index < songListMP3Files.size() && !songListFileNames.get(index).startsWith(Constants.HEADER_ALBUM)) {
            debug("added: " + index);
            indices.add(index++);

        }
        debug(Arrays.toString(indices.toArray(new Integer[0])));

        return indices;
    }

    /**
     * Returns tag data for specified index
     * 
     * @param indicies Can be 1 or many
     * @param ob Callback with TagInfo
     */
    public void selectTags(List<Integer> indices, AudioFilesModelTagInfo cb) {
        selectedindices = indices;
        TagDetails tagsFinalized = null;
        Set<Integer> processed = new HashSet<Integer>();
        for(int index : indices) {
            // if not array out of bounds, safety check
            if(index >= 0 && index < songListMP3Files.size()) {
                // if selected header
                if(songListFileNames.get(index).startsWith(Constants.HEADER_ALBUM)) {
                    // get all songs from album
                    for(int j : getAllIndexForHeader(index)) {
                        processed.add(j); // add to processed in-case it is part of indices
                        TagDetails temp = getSelectedTag(j);
                        if(tagsFinalized == null) {
                            tagsFinalized = temp;
                        }
                        else {
                            setTagDetails(tagsFinalized, temp);
                        }
                    }
                }
                else if(!processed.contains(index)) {

                    processed.add(index); // add to processed in-case it is part of indices
                    if(tagsFinalized == null) {
                        TagDetails temp = getSelectedTag(index);
                        tagsFinalized = temp;
                    }
                    else {
                        setTagDetails(tagsFinalized, getSelectedTag(index));
                    }

                }
            }
        }
        cb.getTags(tagsFinalized);
    }

    /**
     * Returns tag data for specified index
     * 
     * @param indicies Can be 1 or many
     * @param ob Callback with TagInfo
     */
    public void selectTag(int index, AudioFilesModelTagInfo cb) {
        List<Integer> list = new ArrayList<Integer>();
        list.add(index);
        selectTags(list, cb);
    }

    /**
     * Compares two sets of TagDetails and sets $0 value to "different values" if they do not match
     * 
     * @param original One to modify if different
     * @param newTag Compare to TagDetails
     */
    private void setTagDetails(TagDetails original, TagDetails newTag) {
        // compare with each other and set as "different values" if not matching 
        for(EditorTag tag : EditorTag.values()) {
            if(tag == EditorTag.ALBUM_ART) {
                Image newImage = ImageUtil.getComparedImage(newTag.getAlbumArt(), original.getAlbumArt());
                original.setAlbumArt(newImage);
            }
            else {
                String newVal = StringUtil.getComparedName(newTag.get(tag), original.get(tag));
                //                debug("original: " + original.get(tag) + " new: " + newTag.get(tag));
                if(original.get(tag) == null || !original.get(tag).equals(newVal)) {
                    //                    debug("replacing with new val: " + newVal);
                    original.set(tag, newVal);
                }
            }
        }
    }

    /**
     * Returns tag info for a valid index (not a header)
     * 
     * @param index Index
     * @return TagInfo or null if header or array out of bounds
     */
    private TagDetails getSelectedTag(Integer index) {
        TagDetails tagDetails = null;
        if(index >= 0 && index < songListMP3Files.size() && !songListFileNames.get(index).startsWith(Constants.HEADER_ALBUM)) {
            tagDetails = new TagDetails();
            AudioFile file = songListMP3Files.get(index);
            for(EditorTag editorTag : EditorTag.values()) {
                tagDetails.set(editorTag, file.get(editorTag));
            }
            tagDetails.setAlbumArt(file.getAlbumArt());
        }
        return tagDetails;
    }

    // ~~~~~~~~~~~~~~~~~ //
    // Getters & Setters //
    // ~~~~~~~~~~~~~~~~~ //

    public ListProperty<String> fileNamesProperty() {
        return songListFileNames;
    }

    /**
     * Returns the display list of folders with audio files within them
     * 
     * @return n Folders + n Audio Files
     */
    public final List<String> getFileNames() {
        return songListFileNames.get();
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
            //            albumArt = ImageUtil.scaleImage(image, 500, 500, true);
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
            //            albumArt = ImageUtil.scaleImage(image, 500, 500, true);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    // save new tags
    /**
     * @param index
     * @param tags
     * @param overrideFileName true = save with original name
     */
    private void saveForIndex(int index, TagDetails tags, boolean overrideFileName) {
//        info("Save for [" + index + "] tags: " + tags);
        AudioFile file = songListMP3Files.get(index);
        for(EditorTag tag : EditorTag.values()) {
            String tagVal = tags.get(tag);
            if(tagVal != null && !tagVal.isEmpty() && !StringUtil.isKeyword(tagVal)) {

                file.setField(tag, tagVal);
            }
        }
        if(overrideFileName) {
            file.setField(EditorTag.FILE_NAME, file.getOriginalFileName());
        }
        file.save();
        // update ui list view TODO
        //        songListMP3Files.remove(index); // remove original file
        //        songListMP3Files.add(index, new AudioFile(new File(newNamePath))); // update to new file
        //
        //        songListFileNames.add(i, fileName); // add new filename into list
        //        songListFileNames.remove(i + 1); // remove original filename,
        // (order matters, causes an ui update and triggering a selectIndex which changes selected Index)
    }

    @Override
    public void save(TagDetails tags) {
//        info("Starting Save: " + Arrays.toString(selectedindices.toArray(new Integer[0])));
        Set<Integer> processed = new HashSet<Integer>();
        // go through each index
        for(int index : selectedindices) {
            // if file isnt keyword
            if(!StringUtil.isKeyword(songListFileNames.get(index))) {
                // save with values and ignore fileName if multiple indices selected
                saveForIndex(index, tags, selectedindices.size() > 1 ? true : false);
            }

            processed.add(index);
        }
        // TODO Propagate saving
        //      if(Settings.getInstance().isAnyPropagateSaveOn()) {
        //          List<Integer> sameAlbumIndicies = getAllIndexFromAlbum(index, false);
        //      }
    }

    @Override
    public Image getAlbumArt() {
        return null;
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
        //        if(tag == EditorTag.ALBUM) {
        //            returnValue = album;
        //        }
        //        else if(tag == EditorTag.ALBUM_ART_META) {
        //            returnValue = albumArtMeta;
        //        }
        //        else if(tag == EditorTag.ALBUM_ARTIST) {
        //            returnValue = albumArtist;
        //        }
        //        else if(tag == EditorTag.ARTIST) {
        //            returnValue = artist;
        //        }
        //        else if(tag == EditorTag.COMMENT) {
        //            returnValue = comment;
        //        }
        //        else if(tag == EditorTag.FILE_NAME) {
        //            returnValue = fileName;
        //        }
        //        else if(tag == EditorTag.GENRE) {
        //            returnValue = genre;
        //        }
        //        else if(tag == EditorTag.TITLE) {
        //            returnValue = title;
        //        }
        //        else if(tag == EditorTag.TRACK) {
        //            if(StringUtil.isKeyword(track)) {
        //                returnValue = track;
        //            }
        //            else {
        //                returnValue = String.format("%02d", Integer.valueOf(track));
        //            }
        //        }
        //        else if(tag == EditorTag.YEAR) {
        //            returnValue = year;
        //        }
        //        else {
        //            info("no data for tag: " + tag);
        //        }
        return returnValue;
    }

    // replace tagData with new tagData
    @Override
    public void setDataForTag(TagBase<?> tag, String... values) {
        //        if(tag == EditorTag.ALBUM) {
        //            album = values[0];
        //        }
        //        else if(tag == EditorTag.ALBUM_ART_META) {
        //            albumArtMeta = values[0];
        //        }
        //        else if(tag == EditorTag.ALBUM_ARTIST) {
        //            albumArtist = values[0];
        //        }
        //        else if(tag == EditorTag.ARTIST) {
        //            artist = values[0];
        //        }
        //        else if(tag == EditorTag.COMMENT) {
        //            comment = values[0];
        //        }
        //        else if(tag == EditorTag.FILE_NAME) {
        //            fileName = values[0];
        //        }
        //        else if(tag == EditorTag.GENRE) {
        //            genre = values[0];
        //        }
        //        else if(tag == EditorTag.TITLE) {
        //            title = values[0];
        //        }
        //        else if(tag == EditorTag.TRACK) {
        //            track = values[0];
        //        }
        //        else if(tag == EditorTag.YEAR) {
        //            year = values[0];
        //        }
    }

    @Override
    public List<String> getPossibleDataForTag(TagBase<?> tag, String values) {
        // TODO Auto-generated method stub
        return null;
    }
}