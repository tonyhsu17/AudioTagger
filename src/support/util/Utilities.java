package support.util;

import model.base.TagBase;
import support.structure.Range;


public class Utilities
{
    public enum Tag implements TagBase<Tag> {
        FILE_NAME, TITLE, ARTIST, ALBUM, ALBUM_ARTIST, TRACK, YEAR, GENRE, COMMENT, ALBUM_ART, ALBUM_ART_META,;
    }
    
    
    public static Range getRange(String fullText, int caretPosition, String selectedText)
    {
        if(selectedText.isEmpty()) {
            return new Range(caretPosition, caretPosition);
        } 
        else
        {
            int start = fullText.indexOf(selectedText);
            return new Range(start, start + selectedText.length());
        }
    }
    
    public static int findIntValue(String str)
    {
        int endIndex = -1;
        if(((endIndex = str.toLowerCase().indexOf("th")) != -1) ||
            ((endIndex = str.toLowerCase().indexOf("st")) != -1) ||
            ((endIndex = str.toLowerCase().indexOf("nd")) != -1) ||
            ((endIndex = str.toLowerCase().indexOf("rd")) != -1))
        {
            str = str.substring(0, endIndex);
            String[] splitStr = str.split(" ");
            for(int i = splitStr.length - 1; i >= 0; i--)
            {
                try
                {
                    return Integer.valueOf(splitStr[i]);
                }
                catch(NumberFormatException e)
                {
                    
                } 
            }
        }
        return -1;
    }
    
    public static boolean convertToBoolean(String str)
    {
        if(str.toLowerCase().startsWith("y") || str.toLowerCase().startsWith("t"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
