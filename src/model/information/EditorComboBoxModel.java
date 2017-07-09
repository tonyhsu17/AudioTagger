package model.information;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javafx.scene.image.Image;
import model.base.InformationBase;
import model.base.TagBase;
import support.structure.EditorComboBoxMeta;
import support.util.Utilities.Tag;

public class EditorComboBoxModel implements InformationBase
{
    private HashMap<Tag, EditorComboBoxMeta> tagToData;
    
    public EditorComboBoxModel()
    {
        tagToData = new HashMap<Tag, EditorComboBoxMeta>();
        
        for(Tag t : Tag.values())
        {
            tagToData.put(t, new EditorComboBoxMeta());
        }
    }
    public EditorComboBoxMeta getMeta(Tag t)
    {
        return tagToData.get(t);
    }
    
    @Override
    public String getDataForTag(TagBase<?> tag, String... extraArgs)
    {
        return tagToData.get(tag).getTextProperty().get();
    }

    @Override
    public Image getAlbumArt()
    {
        return null;
    }

    @Override
    public void setDataForTag(TagBase<?> tag, String... values)
    {
    }

    @Override
    public void setAlbumArtFromFile(File file)
    {
    }

    @Override
    public void setAlbumArtFromURL(String url)
    {
    }

    @Override
    public void save()
    {
    }

    @Override
    public List<String> getPossibleDataForTag(TagBase<?> tag, String values)
    {
        return null;
    }

    @Override
    public String getDisplayKeywordTagClassName()
    {
        return "Editor";
    }

    @Override
    public List<TagBase<?>> getKeywordTags()
    {
        List<TagBase<?>> keywords = new ArrayList<>();
        keywords.add(Tag.ALBUM);
        keywords.add(Tag.ALBUM_ARTIST);
        keywords.add(Tag.ARTIST);
        keywords.add(Tag.COMMENT);
        keywords.add(Tag.FILE_NAME);
        keywords.add(Tag.GENRE);
        keywords.add(Tag.TITLE);
        keywords.add(Tag.TRACK);
        keywords.add(Tag.YEAR);
        return keywords;
    }

    @Override
    public TagBase<?>[] getAdditionalTags()
    {
        return null;
    }
}
