package modules.controllers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.images.ArtworkFactory;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import model.Settings.SettingsKey;
import modules.controllers.base.InformationBase;
import modules.controllers.base.TagBase;
import support.Constants;
import support.Logger;
import support.structure.AudioFile;
import support.structure.TagDetails;
import support.util.ImageUtil;
import support.util.StringUtil;
import support.util.Utilities.EditorTag;



/**
 * Controller class for handling audio files.
 * Stores list of files in workspace and indices for selected to view in editor.
 * 
 * @author Tony
 *
 */
public class AudioFilesController implements InformationBase, Logger {
    /**
     * Call back interface
     */
    public interface AudioFilesModelTagInfo {
        public void getTags(TagDetails details);
    }

    private ArrayList<String> songListDirectories; // directories of files

    private ListProperty<String> songListFileNames; // list view display, includes album headers
    private ArrayList<AudioFile> songListMP3Files; // audio files with null placeholders for album headers

    private List<Integer> selectedindices; // index of selected file
    private TagDetails selectedTagInfo; // info of selected file

    private HashMap<SettingsKey, EditorTag> settingsToEditor;

    public AudioFilesController() {
        songListFileNames = new SimpleListProperty<String>();
        songListFileNames.set(FXCollections.observableArrayList());
        reset();

        settingsToEditor = new HashMap<SettingsKey, EditorTag>();
        settingsToEditor.put(SettingsKey.PROPAGATE_SAVE_ALBUM, EditorTag.ALBUM);
        settingsToEditor.put(SettingsKey.PROPAGATE_SAVE_ALBUM_ART, EditorTag.ALBUM_ART);
        settingsToEditor.put(SettingsKey.PROPAGATE_SAVE_ALBUM_ARTIST, EditorTag.ALBUM_ARTIST);
        settingsToEditor.put(SettingsKey.PROPAGATE_SAVE_ARTIST, EditorTag.ARTIST);
        settingsToEditor.put(SettingsKey.PROPAGATE_SAVE_COMMENT, EditorTag.COMMENT);
        settingsToEditor.put(SettingsKey.PROPAGATE_SAVE_GENRE, EditorTag.GENRE);
        settingsToEditor.put(SettingsKey.PROPAGATE_SAVE_YEAR, EditorTag.YEAR);
    }

    private void reset() {
        songListDirectories = new ArrayList<String>();
        songListFileNames.clear();
        songListMP3Files = new ArrayList<>();
        selectedindices = new ArrayList<Integer>();
        selectedTagInfo = null;
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
     * Returns a list of indices that are part of the album.
     * 
     * @param n Must be a header
     * @return list of indices part of the album
     */
    public List<Integer> getAllIndexForHeader(int n) {
        if(!isHeader(n)) {
            return null;
        }
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
                            tagsFinalized = getCombinedTagDetails(tagsFinalized, temp);
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
                        getCombinedTagDetails(tagsFinalized, getSelectedTag(index));
                    }
                }
            }
        }
        selectedTagInfo = tagsFinalized;
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
     * @return TagDetails
     */
    private TagDetails getCombinedTagDetails(TagDetails original, TagDetails newTag) {
        // compare with each other and set as "different values" if not matching 
        for(EditorTag tag : EditorTag.values()) {
            if(tag == EditorTag.ALBUM_ART) {
                Image newImage = ImageUtil.getComparedImage(newTag.getAlbumArt(), original.getAlbumArt());
                original.setAlbumArt(newImage);
            }
            else {
                String newVal = StringUtil.getComparedName(newTag.get(tag), original.get(tag));
                if(original.get(tag) == null || !original.get(tag).equals(newVal)) {
                    original.set(tag, newVal);
                }
            }
        }
        return original;
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

    /**
     * Workspace file list property
     * 
     * @return ListProperty<String>
     */
    public ListProperty<String> fileNamesProperty() {
        return songListFileNames;
    }

    /**
     * Returns the workspace of audio files and folder names
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
    public void setAlbumArt(Object obj) {
        try {
            // get image based on object type
            BufferedImage buffImage = null;
            if(obj instanceof File) {
                File file = (File)obj;
                buffImage = ImageIO.read(file);
            }
            else if(obj instanceof String) {
                String url = (String)obj;
                buffImage = ImageIO.read(new URL(url));
            }

            // if successfully parsed image
            if(buffImage != null) {
                Image image = SwingFXUtils.toFXImage(buffImage, null);
                if(selectedTagInfo != null) {
                    selectedTagInfo.setAlbumArt(image);
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save new tags
     * 
     * @param index
     * @param tags TagDetails
     * @param overrideFileName true = save with original name
     */
    private void saveForIndex(int index, TagDetails tags, boolean overrideFileName) {
        AudioFile file = songListMP3Files.get(index);
        for(EditorTag tag : EditorTag.values()) {
            String tagVal = tags.get(tag);
            if(tagVal != null && !tagVal.isEmpty() && !StringUtil.isKeyword(tagVal)) {
                file.setField(tag, tagVal);
            }
        }
        // fix file name if multisave
        if(overrideFileName) {
            file.setField(EditorTag.FILE_NAME, file.getOriginalFileName());
        }
        else {
            songListFileNames.set(index, tags.get(EditorTag.FILE_NAME));
        }
        // save album art
        if(tags.getAlbumArt() != null) {
            try {

                File temp = ImageUtil.saveImage(tags.getAlbumArt());
                file.getRawTags().deleteArtworkField();
                file.setAlbumArt(ArtworkFactory.createArtworkFromFile(temp));
                temp.delete();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        file.save();

        try {
            // update reference to file
            songListMP3Files.set(index,
                new AudioFile(new File(file.getFile().getParentFile().getPath() + File.separator + file.getNewFileName())));
        }
        catch (IOException | TagException | ReadOnlyFileException | CannotReadException | InvalidAudioFrameException e) {
        }
    }

    public boolean isHeader(int index) {
        if(songListFileNames.get(index).startsWith(Constants.HEADER_ALBUM)) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Converts header indices into file indices
     * 
     * @param selected
     * @return
     */
    public List<Integer> removeHeaderIndicies() {
        Set<Integer> nonHeaders = new HashSet<Integer>();
        for(int index : selectedindices) {
            if(isHeader(index)) {
                // if header selected, go through each track
                for(int subIndex : getAllIndexForHeader(index)) {
                    nonHeaders.add(subIndex);
                }
            }
            else if(!nonHeaders.contains(index)) {
                nonHeaders.add(index);
            }
        }
        return new ArrayList<Integer>(nonHeaders);
    }

    @Override
    public void save(TagDetails tags) {
        info("Starting Save: " + Arrays.toString(selectedindices.toArray(new Integer[0])));
        List<Integer> indicesToProcess = removeHeaderIndicies();
        // handle propagate saving
        //        if(Settings.getInstance().isAnyPropagateSaveOn()) {
        //            TagDetails tempDetails = new TagDetails();
        //            for(Entry<SettingsKey, EditorTag> entry : settingsToEditor.entrySet()) {
        //                if(Settings.getInstance().isPropagateSaveOn(entry.getKey())) {
        //                    tempDetails.set(entry.getValue(), tags.get(entry.getValue()));
        //                }
        //            }
        //        }

        // go through each index
        for(int index : indicesToProcess) {
            if(!StringUtil.isKeyword(songListFileNames.get(index))) {
                // save with values and ignore fileName if multiple indices selected
                saveForIndex(index, tags, selectedindices.size() > 1 ? true : false);
            }
        }
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

    /**
     * Get meta for a specific tag
     * 
     * @see modules.controllers.base.InformationBase#getDataForTag(modules.controllers.base.TagBase,
     *      java.lang.String[])
     */
    @Override
    public String getDataForTag(TagBase<?> tag, String... extraArgs) {
        // if no file is selected return null
        return selectedTagInfo == null ? "" : selectedTagInfo.get((EditorTag)tag);
    }

    @Override
    public void setDataForTag(TagBase<?> tag, String... values) {}

    @Override
    public List<String> getPossibleDataForTag(TagBase<?> tag, String values) {
        return null;
    }
}
