package model.base;

import java.io.File;
import java.util.List;

import javafx.scene.image.Image;

/**
 * Base class for any tag data suggestions. DataCompilation will utilize the common methods to retrieve and pass data back. 
 * @author Ikersaro
 *
 */
public interface InformationBase
{
    public String getDataForTag(TagBase<?> tag, String... extraArgs);
    public Image getAlbumArt();
    
    /**
     * Call when data is to be stored or changed.
     * @param tag
     * @param value
     */
    public void setDataForTag(TagBase<?> tag, String... values);
    public void setAlbumArtFromFile(File file);
    public void setAlbumArtFromURL(String url);
    public void save();
    
    public List<String> getPossibleDataForTag(TagBase<?> tag, String values);
    public String getDisplayKeywordTagClassName();
    public List<TagBase<?>> getKeywordTags(); // tags that can be used for string builder 
    
    public TagBase<?>[] getAdditionalTags(); // extra tags not in base tags
    
}
