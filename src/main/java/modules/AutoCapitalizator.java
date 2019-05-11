package modules;

import modules.controllers.DatabaseController;
import modules.controllers.DatabaseController.AdditionalTag;
import org.tonyhsu17.utilities.Logger;
import support.util.Utilities.EditorTag;



/**
 * Helper class to handle capitalizing the first letter of each word unless it was previously told not to.
 *
 * @author Tony Hsu
 */
public class AutoCapitalizator implements Logger {
    private DatabaseController dbManagement; // database for prediction of common tag fields

    /**
     * Helper class to handle capitalizing the first letter of each word unless it was previously told not to.
     *
     * @param dbManagement {@link DatabaseController}
     */
    public AutoCapitalizator(DatabaseController dbManagement) {
        this.dbManagement = dbManagement;
    }

    /**
     * Capitalizes each word unless told not to.
     *
     * @param type  {@link EditorTag}.TITLE only, others are skipped
     * @param value original text
     * @return Modifed text or same
     */
    public String getFormattedText(EditorTag type, String value) {
        StringBuilder builder = null;
        if(type == EditorTag.TITLE) {
            boolean isFirstWord = true;
            info("Before: " + value);
            builder = new StringBuilder();
            for(String word : value.split(" ")) {
                if(isFirstWord) {
                    // ignore first word since it is special
                    isFirstWord = false;
                    builder.append(word + " ");
                    continue;
                }
                String nonCapitalized = dbManagement.getDataForTag(AdditionalTag.NON_CAPITALIZED,
                    word.substring(0, 1).toLowerCase() + word.substring(1));
                debug("word: " + word + " found in db: " + nonCapitalized);
                if(!nonCapitalized.isEmpty() && word.equalsIgnoreCase(nonCapitalized)) {
                    // if word found, and is same without case then use new value
                    builder.append(word.replace(word, nonCapitalized));
                }
                else {
                    // if word isnt found, capitlize it
                    builder.append(word.substring(0, 1).toUpperCase() + word.substring(1));
                }
                builder.append(" ");
            }
            builder.deleteCharAt(builder.length() - 1); // remove extra spacing at end
            info("After: " + builder.toString());
        }
        return builder == null ? value : builder.toString();
    }
}
