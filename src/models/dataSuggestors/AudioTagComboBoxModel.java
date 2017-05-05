package models.dataSuggestors;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.image.Image;
import support.TagBase;
import support.Utilities.Tag;

public class AudioTagComboBoxModel implements DataSuggestorBase
{
    private HashMap<Tag, ComboBoxMeta> tagToData;
    
    public AudioTagComboBoxModel()
    {
        tagToData = new HashMap<Tag, ComboBoxMeta>();
        
        for(Tag t : Tag.values())
        {
            tagToData.put(t, new ComboBoxMeta());
        }
    }
    public ComboBoxMeta getMeta(Tag t)
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
    
    public class ComboBoxMeta
    {
        private ListProperty<String> dropDownProperty;
        private SimpleStringProperty textProperty;
        private boolean allowAutoFill;

        public ComboBoxMeta()
        {
            dropDownProperty = new SimpleListProperty<String>();
            dropDownProperty.set(FXCollections.observableArrayList());
            textProperty = new SimpleStringProperty();
            allowAutoFill = true;
        }
        
        public ListProperty<String> getDropDownListProperty()
        {
            return dropDownProperty;
        }

        public SimpleStringProperty getTextProperty()
        {
            return textProperty;
        }
        
        public boolean shouldStopAutoFill()
        {
            return allowAutoFill;
        }
        
        public void setAllowAutoFill(boolean flag)
        {
            allowAutoFill = flag;
        }
        
        public void clear()
        {
            dropDownProperty.clear();
            textProperty.set("");
        }
    }
}
