package modules;

import java.util.ArrayList;
import java.util.List;

import model.database.DatabaseController;
import model.information.EditorComboBoxModel;
import support.util.StringUtil;
import support.util.Utilities.EditorTag;

public class AutoCorrect {
    private EditorComboBoxModel editorMap; // Tag to ComboBox data (editor text and drop down)
    private DatabaseController dbManagement; // database for prediction of common tag fields
    
    public AutoCorrect(EditorComboBoxModel editorMap, DatabaseController dbManagement) {
        this.editorMap = editorMap;
        this.dbManagement = dbManagement;
    }
    
    /**
     * Auto-corrects capitalizations and such based on user's historical data
     * @param type
     * @param value
     */
    public void setTextFormattedFromDB(EditorTag type, String value) {
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

        editorMap.getMeta(type).getTextProperty().set(value); // set the final value
    }
}
