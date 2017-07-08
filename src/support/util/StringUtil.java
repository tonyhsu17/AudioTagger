package support.util;

import java.util.List;

import support.Constants;

public class StringUtil
{
    public static String[] splitName(String fullName)
    {
        String[] splitName = {"", ""};
        if(fullName == null || fullName.isEmpty())
        {
            return splitName;
        }
        splitName = fullName.split(" ");
        String lastName = "";
        String firstName = "";
        if(splitName.length > 1)
        {
            lastName = splitName[splitName.length - 1]; // only last part is last name
            firstName = splitName[0];
            for(int i = 1; i < splitName.length - 1; i++) // skip last part as thats last name
            {
                firstName += " " + splitName[i];
            }
        }
        else
        {
            firstName = splitName[0];
        }
        
        return new String[] {firstName, lastName};
    }
    
    public static String[] splitBySeparators(String string)
    {
        String[] splitArtists = string.split("(, )|( & )");
        // TODO get feat and etc to split by too
        return splitArtists;
    }
    
    public static String createQuestionMarks(int num)
    {
        if(num == 0)
        {
            return "";
        }
        else if(num == 1)
        {
            return "?";
        }
        else if(num == 2)
        {
            return "?, ?";
        }

        StringBuilder sb = new StringBuilder();
        int i = 0;
        for(; i < num - 1; i++)
        {
            sb.append("?, ");
        }
        sb.append("?");
        return sb.toString();
    }
    
    /**
     * @return Same given value or "Different"
     */
    public static String getComparedName(String s1, String s2)
    {
        if(s1 == null || s2 == null || !s1.equals(s2))
        {
            return Constants.KEYWORD_DIFF_VALUE;
        }
        else
        {
            return s1;
        }
    }
    
    public static boolean isKeyword(String str)
    {
        if(str.equals(Constants.KEYWORD_DIFF_VALUE))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    public static String getCommaSeparatedStringWithAnd(List<String> list)
    {
        if(list.isEmpty())
        {
            return "";
        }
        else if(list.size() == 1)
        {
            return (String)list.get(0);
        }
        else if(list.size() == 2)
        {
            return list.get(0) + " & " + list.get(1);
        }

        StringBuilder sb = new StringBuilder();
        int i = 0;
        for(; i < list.size() - 2; i++)
        {
            sb.append(list.get(i) + ", ");
        }
        sb.append(list.get(i) + " & " + list.get(i + 1));
        return sb.toString();
    }
    

}
