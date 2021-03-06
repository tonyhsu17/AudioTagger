package support.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import support.Constants;



public class StringUtil {
    /**
     * Split name into first and last name. Firstname is everything except last word.
     *
     * @param fullName Fullname to split
     * @return Array with [0]=first, [1]=last
     */
    public static String[] splitName(String fullName) {
        String[] splitName = {"", ""};
        if(fullName == null || fullName.isEmpty()) {
            return splitName;
        }
        splitName = fullName.split(" ");
        String lastName = "";
        String firstName = "";
        if(splitName.length > 1) {
            lastName = splitName[splitName.length - 1]; // only last part is last name
            firstName = splitName[0];
            for(int i = 1; i < splitName.length - 1; i++) // skip last part as thats last name
            {
                firstName += " " + splitName[i];
            }
        }
        else {
            firstName = splitName[0];
        }

        return new String[] {firstName, lastName};
    }

    /**
     * Parses comma + & dividers into an array.
     *
     * @param string String to parse
     * @return List of values that was separated
     */
    public static String[] splitBySeparators(String string) {
        String[] splitArtists = string.split(Constants.REGEX_SEPARATORS);
        // TODO get feat and etc to split by too
        return splitArtists;
    }

    public static String createQuestionMarks(int num) {
        String comma = "";
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < num; i++) {
            sb.append(comma + "?");
            comma = ", ";
        }
        return sb.toString();
    }

    /**
     * @return Same given value or "Different"
     */
    public static String getComparedName(String s1, String s2) {
        if(s1 == s2) {
            return s1;
        }
        else if(s1 == null || s2 == null || !s1.equals(s2)) {
            return Constants.KEYWORD_DIFF_VALUE;
        }
        else {
            return s1;
        }
    }

    public static boolean isKeyword(String str) {
        if(str.equals(Constants.KEYWORD_DIFF_VALUE)) {
            return true;
        }
        else {
            return false;
        }
    }

    public static String getCommaSeparatedStringWithAnd(List<String> list) {
        if(list.isEmpty()) {
            return "";
        }
        else if(list.size() == 1) {
            return list.get(0);
        }
        else if(list.size() == 2) {
            return list.get(0) + " & " + list.get(1);
        }

        StringBuilder sb = new StringBuilder();
        int i = 0;
        for(; i < list.size() - 2; i++) {
            sb.append(list.get(i) + ", ");
        }
        sb.append(list.get(i) + " & " + list.get(i + 1));
        return sb.toString();
    }

    /**
     * Retrieves text within delimiters
     *
     * @param search String to search
     * @return List of text including delimiters matched
     */
    public static List<String> getStrInDelim(String search) {
        List<String> results = new ArrayList<String>();
        if(search == null || search.isEmpty()) {
            return results;
        }

        Pattern pattern = Pattern.compile(Constants.REGEX_DELIM);
        Matcher match = pattern.matcher(search);

        while(match.find()) {
            results.add(match.group());
        }

        return results;
    }

    /**
     * Retrieves a list of different values within the delimiters
     *
     * @param before Original string
     * @param after  Modified string
     * @return List of arrays containing original and modified string. array[0] = original, array[1]
     * = modified
     */
    public static List<String[]> getDiffInDelim(String before, String after) {
        List<String[]> results = new ArrayList<String[]>();

        List<String> beforeResults = StringUtil.getStrInDelim(before);
        List<String> afterResults = StringUtil.getStrInDelim(after);
        if(beforeResults.isEmpty() || afterResults.isEmpty() || beforeResults.size() != afterResults.size()) {
            return results;
        }

        for(int i = 0; i < beforeResults.size(); i++) {
            if(!beforeResults.get(i).equals(afterResults.get(i))) {
                results.add(new String[] {beforeResults.get(i), afterResults.get(i)});
            }
        }

        return results;
    }

    //    /**
    //     * Creates a number of %s comman separtered etc
    //     *
    //     * @param size
    //     * @return
    //     */
    //    public static String getSchemaStringFormatter(int size) {
    //        StringBuilder builder = new StringBuilder();
    //        for(int i = 0; i < size; i++) {
    //            builder.append("%s %s" + ((i != (size - 1)) ? "," : ""));
    //        }
    //        return builder.toString();
    //    }
}
