package support.util;

import model.base.TagBase;
import support.structure.Range;



public class Utilities {
    public enum EditorTag implements TagBase<EditorTag> {
        FILE_NAME, TITLE, ARTIST, ALBUM, ALBUM_ARTIST, TRACK, YEAR, GENRE, COMMENT, ALBUM_ART, ALBUM_ART_META,;
    }


    /**
     * Returns first and last index of a selected string.
     * 
     * @param fullText Text to index reference
     * @param caretPosition Starting position of selected text
     * @param selectedText Substring of text that is selected
     * @return Range of the selected text
     */
    public static Range getRange(String fullText, int caretPosition, String selectedText) {
        if(selectedText.isEmpty()) {
            return new Range(caretPosition, caretPosition);
        }
        else {
            int start = fullText.indexOf(selectedText);
            return new Range(start, start + selectedText.length());
        }
    }

    /**
     * Converts string numbers including number suffix into integer values.
     * 
     * @param str Text containing the number
     * @return Integer value of number or -1 if no integer found.
     */
    public static int findIntValueWithSuffix(String str) {
        int value = -1;

        if(str == null) {
            return value;
        }

        String[] splitStr = str.split(" ");
        for(int i = splitStr.length - 1; i >= 0; i--) {
            String parseStr = splitStr[i];
            int endIndex = 0;
            if((((endIndex = splitStr[i].toLowerCase().indexOf("th")) != -1) ||
                ((endIndex = splitStr[i].toLowerCase().indexOf("st")) != -1) ||
                ((endIndex = splitStr[i].toLowerCase().indexOf("nd")) != -1) ||
                ((endIndex = splitStr[i].toLowerCase().indexOf("rd")) != -1))) {
                parseStr = splitStr[i].substring(0, endIndex);
            }

            try {
                value = Integer.valueOf(parseStr);
            }
            catch (NumberFormatException e) {
            }
        }

        return value;
    }

    public static boolean convertToBoolean(String str) {
        if(str.toLowerCase().startsWith("y") || str.toLowerCase().startsWith("t")) {
            return true;
        }
        else {
            return false;
        }
    }
}
