package modules.controllers.base;

import java.util.List;

import javafx.scene.image.Image;
import support.structure.TagDetails;



/**
 * Base class for any tag data suggestions. DataCompilation will utilize the common methods to
 * retrieve and pass data back. Methods will return null if unused. Empty if used but no results.
 * 
 * @author Ikersaro
 *
 */
public interface InformationBase {
    /**
     * Get data for tag. Returns null if unused.
     * 
     * @param tag TagBase<?>
     * @param extraArgs Additional arguments if needed
     * @return Value for tag, Empty string if none found.
     */
    public String getDataForTag(TagBase<?> tag, String... extraArgs);

    /**
     * Set data for tag
     * 
     * @param tag TagBase<?>
     * @param value Value to store to tag
     */
    public void setDataForTag(TagBase<?> tag, String... values);

    /**
     * Get possible data for tag. Returns null if unused.
     * 
     * @param tag TagBase<?>
     * @param values Values to match against
     * @return List of possible values
     */
    public List<String> getPossibleDataForTag(TagBase<?> tag, String values);

    /**
     * Get album art image. Returns null if unused.
     * 
     * @return Image
     */
    public Image getAlbumArt();

    /**
     * Set album art image
     * 
     * @param obj
     */
    public void setAlbumArt(Object obj);

    /**
     * Save data with passed in information. Pass null if saving with currently set data.
     * 
     * @param details TagDetails
     */
    public void save(TagDetails details);

    /**
     * Get user friendly class name. Returns null if unused.
     * 
     * @return String
     */
    public String getDisplayKeywordTagClassName();

    /**
     * Get list of keywords of tags that can be used for string builder. Returns null if unused.
     * 
     * @return List<TagBase<?>>
     */
    public List<TagBase<?>> getKeywordTags(); //  

    /**
     * Extra tags not part of TagBase. Returns null if unused.
     * 
     * @return
     */
    public TagBase<?>[] getAdditionalTags();

}
