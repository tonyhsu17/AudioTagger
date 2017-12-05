package model;

import java.util.ArrayList;
import java.util.List;

import model.base.InformationBase;
import model.base.TagBase;
import support.Constants;
import support.util.StringUtil;

/**
 * Interprets preferred formatting using settings.
 * @author Ikersaro
 *
 */
public class KeywordInterpreter
{
    StringBuffer rule;
    List<Object[]> replaceValues;
    List<String> replaceStrings;
    
    public KeywordInterpreter()
    {
        rule = new StringBuffer();
        replaceValues = new ArrayList<Object[]>();
        replaceStrings = new ArrayList<String>();
    }
    
    public boolean hasRule()
    {
        return !rule.toString().isEmpty();
    }
    
    public String getFormatRule()
    {
        return rule.toString();
    }
    
    public void appendToRule(String str)
    {
        rule.append(str);
    }
    
    public void appendToRule(String str, InformationBase objRef, TagBase<?> tag)
    {
        rule.append(str);
        replaceValues.add(new Object[] {objRef, tag});
    }
    
    public InformationBase getClass(int i)
    {
        if(i >= 0 && i < replaceValues.size())
        {
            return (InformationBase)replaceValues.get(i)[0];
        }
        return null;
    }
    
    public TagBase<?> getTag(int i)
    {
        if(i >= 0 && i < replaceValues.size())
        {
            return (TagBase<?>)replaceValues.get(i)[1];
        }
        return null;
    }
    
    public void setValue(int index, String value)
    {
        if(value == null)
        {
            value = "";
        }
//        System.out.println("setValue: " + index + " " + value);
        replaceStrings.add(index, value);
    }
    
    public String buildString()
    {
        String temp = rule.toString();
        String replaceVal;
//        System.out.println("buildstr: " + replaceValues.size() + " stringReplace: " + replaceValues.size() );
        for(int i = 0; i < replaceValues.size(); i++)
        {
            // if keyword and not valid value, immediately finish string
            if(StringUtil.isKeyword(replaceStrings.get(i))) {
                temp = Constants.KEYWORD_DIFF_VALUE;
                break;
            }
            replaceVal = replaceStrings.get(i);
            temp = temp.replaceFirst("%s", replaceVal != null ? replaceVal : "");
        }
        
        return temp;
    }
    
    public int getCount()
    {
        return replaceValues.size();
    }
    // KeywordInterpreter?
    // when viewed path
    //  dataCompilationModel passes all possible keywordTags to SettingsModel
    //  settingsModel uses that data to create a readable list for settingsVC to display to view
    //  once formula is written and saved,
    //  settingsModel will construct a KeywordTagBuilder
    // when dataCompilation wants the data
    //  goes to settingsModel and grabs the constructed keywordTagBuilder
    //  to build the string with proper values
    
}
