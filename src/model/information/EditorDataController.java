package model.information;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.List;

import javafx.scene.image.Image;
import model.base.InformationBase;
import model.base.TagBase;
import model.database.DatabaseController;
import modules.AutoCompleter;
import modules.AutoCorrecter;
import support.EventCenter;
import support.EventCenter.Events;
import support.structure.EditorComboBoxMeta;
import support.structure.TagDetails;
import support.util.Utilities.EditorTag;



public class EditorDataController implements InformationBase {
    private HashMap<EditorTag, EditorComboBoxMeta> tagToData;

    // modules
    private AutoCorrecter autoCorrecter;
    private AutoCompleter autoCompleter;

    public EditorDataController(DatabaseController dbManagement) {
        tagToData = new HashMap<EditorTag, EditorComboBoxMeta>();

        for(EditorTag t : EditorTag.values()) {
            tagToData.put(t, new EditorComboBoxMeta());
        }

        autoCorrecter = new AutoCorrecter(dbManagement);
        autoCompleter = new AutoCompleter();


        EventCenter.getInstance().subscribeEvent(Events.TRIGGER_AUTO_FILL, this, (obj) -> {
            autoFillForAllTags();
        });
    }

    public EditorComboBoxMeta getMeta(EditorTag t) {
        return tagToData.get(t);
    }

    @Override
    public String getDataForTag(TagBase<?> tag, String... extraArgs) {
        return tagToData.get(tag).getTextProperty().get();
    }

    @Override
    public Image getAlbumArt() {
        return null;
    }

    /**
     * Replaces and auto capitalize words based on previous results. Formats tracks as well
     * Text -> Replace delimiters -> replace text capitalization -> shown in editor
     * 
     * @param type EditorTag
     * @param value String to format
     */
    public void setFormattedDataForTag(TagBase<?> tag, String... values) {
        if(values.length != 1 || !(tag instanceof EditorTag)) {
            return;
        }
        EditorTag type = (EditorTag)tag;
        if(tag == EditorTag.TRACK) {
            try {
                getMeta(type).getTextProperty().set(String.format("%02d", values[0]));
            }
            catch (IllegalFormatException e) {
            }
        }
        else {
            values[0] = autoCorrecter.getFormattedText(type, values[0]);
            values[0] = autoCorrecter.getDelimTagReplacement(values[0]);
            getMeta(type).getTextProperty().set(values[0]); // set the final value
        }
    }

    /**
     * Set the value of the editor, no change in text.
     * 
     * @param type EditorTag
     * @param value String
     */
    @Override
    public void setDataForTag(TagBase<?> tag, String... values) {
        if(values.length != 1 || !(tag instanceof EditorTag)) {
            return;
        }
        EditorTag type = (EditorTag)tag;
        getMeta(type).getTextProperty().set(values[0]); // set the final value
    }

    @Override
    public void setAlbumArtFromFile(File file) {}

    @Override
    public void setAlbumArtFromURL(String url) {}

    @Override
    public void save(TagDetails details) {}

    @Override
    public List<String> getPossibleDataForTag(TagBase<?> tag, String values) {
        return null;
    }

    @Override
    public String getDisplayKeywordTagClassName() {
        return "Editor";
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

    @Override
    public TagBase<?>[] getAdditionalTags() {
        return null;
    }

    public AutoCorrecter autoCorrecter() {
        return autoCorrecter;
    }

    public AutoCompleter autoCompleter() {
        return autoCompleter;
    }

    /**
     * Clears all editor text and dropdown
     */
    public void clearAllTags() {
        for(EditorTag t : EditorTag.values()) {
            getMeta(t).clearAll();
        }
    }

    /**
     * Triggers a fetch of data based on rule and applies formatted text
     */
    public void autoFillForAllTags() {
        autoCompleter.triggerAutoFill((type, value) -> {
            setFormattedDataForTag(type, autoCorrecter.getFormattedText(type, value));
        });
    }
}
