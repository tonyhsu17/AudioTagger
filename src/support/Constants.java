package support;

import java.util.regex.Pattern;

public class Constants
{
    public static final String KEYWORD_DIFF_VALUE = "<Different Values>";
    public static final String HEADER_ALBUM = "-- Folder: ";
    
    public static final String QUOTED_OPENING_DELIM = Pattern.quote("({[<-~");
    public static final String QUOTED_CLOSING_DELIM = Pattern.quote(")}]>-~");
    // regex for opening delim, other values thats not delim, closing delim
    public static final String REGEX_DELIM = // [({\[<-~][^({\[<-~)}\]>-~]*[)}\]>-~]
        "[" + QUOTED_OPENING_DELIM + "][^" + QUOTED_OPENING_DELIM + QUOTED_CLOSING_DELIM + "]*[" + QUOTED_CLOSING_DELIM + "]";
    public static final String REGEX_SEPARATORS = "(, )|( & )";
}
