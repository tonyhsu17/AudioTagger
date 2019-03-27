package modules;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.tonyhsu17.utilities.Logger;

import modules.controllers.DatabaseController;
import support.util.StringUtil;
import support.util.Utilities.EditorTag;



/**
 * Helper methods to handle text modifications to previously used styles.
 * @author Ikersaro
 *
 */
public class AutoCorrecter implements Logger {
    private DatabaseController dbManagement; // database for prediction of common tag fields

    public AutoCorrecter(DatabaseController dbManagement) {
        this.dbManagement = dbManagement;
    }

    /**
     * Auto-corrects capitalizations and such based on user's historical data
     * 
     * @param type
     * @param value
     */
    public String getFormattedText(EditorTag type, String value) {
        value = getDelimTagReplacement(value);
        if(type == EditorTag.ALBUM_ARTIST) {
            String formattedText = dbManagement.getDataForTag(EditorTag.ALBUM_ARTIST, value);

            if(!formattedText.isEmpty()) {
                value = formattedText;
            }
        }
        else if(type == EditorTag.ARTIST) {
            String[] artists = StringUtil.splitBySeparators(value);
            List<String> formattedArtists = new ArrayList<String>();

            for(String artist : artists) {
                String[] byFirstLast = StringUtil.splitName(artist);
                String formattedText = dbManagement.getDataForTag(type, byFirstLast[0], byFirstLast[1]);
                if(!formattedText.isEmpty()) { // if exist in db use that one
                    formattedArtists.add(formattedText);
                }
                else { // else use original one
                    formattedArtists.add(artist);
                }
            }

            String formattedText = StringUtil.getCommaSeparatedStringWithAnd(formattedArtists);
            if(!formattedText.isEmpty()) {
                value = formattedText;
            }
        }

        return value;
    }

    /**
     * Retrieves standardized tags in delimiters based on user preferences
     * 
     * @return New delimieter tag if found in database, else original
     */
    public String getDelimTagReplacement(String text) {
        List<String> tags = StringUtil.getStrInDelim(text); // all delim tags in text
        ArrayList<String> dbTags = new ArrayList<String>();
        for(String tag : tags) { // get all user stored delim tags that have been replaced
            dbTags.add(dbManagement.getDataForTag(DatabaseController.AdditionalTag.REPLACE_WORD, tag));
        }

        for(int i = 0; i < tags.size(); i++) { // tags.size() == dbTags.size()
            // db returns empty string for non replaced tags
            if(!dbTags.get(i).isEmpty()) {
                info("replacing: " + tags.get(i) + " with: " + dbTags.get(i));
                text = text.replaceFirst(Pattern.quote(tags.get(i)), dbTags.get(i));
            }
        }
        return text;
    }

}
