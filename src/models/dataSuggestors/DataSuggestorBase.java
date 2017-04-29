package models.dataSuggestors;

import java.io.File;
import java.util.List;

import javafx.scene.image.Image;
import support.TagBase;
import support.Utilities.Tag;

/**
 * Base class for any tag data suggestions. DataCompilation will utilize the common methods to retrieve and pass data back. 
 * @author Ikersaro
 *
 */
public interface DataSuggestorBase
{
    public String getDataForTagTest(TagBase tag, String... values);
    public String getDataForTag(Tag tag, String... values);
    public Image getAlbumArt();
    
    /**
     * Call when data is to be stored or changed.
     * @param tag
     * @param value
     */
    public void setDataForTag(Tag tag, String... values);
    public void setAlbumArtFromFile(File file);
    public void setAlbumArtFromURL(String url);
    public void save();
    
    public List<String> getPossibleDataForTag(Tag tag, String values);
    
    public TagBase[] getAdditionalTags(); // extra tags not in base tags
    public TagBase[] getUsableTags(); // tags that useful
    
}
