package model.information;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javafx.scene.image.Image;
import model.base.InformationBase;
import model.base.TagBase;
import support.structure.EditorComboBoxMeta;
import support.structure.TagDetails;
import support.util.Utilities.EditorTag;



public class EditorComboBoxModel implements InformationBase {
    private HashMap<EditorTag, EditorComboBoxMeta> tagToData;

    public EditorComboBoxModel() {
        tagToData = new HashMap<EditorTag, EditorComboBoxMeta>();

        for(EditorTag t : EditorTag.values()) {
            tagToData.put(t, new EditorComboBoxMeta());
        }
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

    @Override
    public void setDataForTag(TagBase<?> tag, String... values) {}

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
}
