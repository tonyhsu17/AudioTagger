package models;

import java.util.ArrayList;
import java.util.List;

import models.dataSuggestors.AudioFiles;
import models.dataSuggestors.DataSuggestorBase;
import support.TagBase;

/**
 * Interprets preferred formatting using settings.
 * @author Ikersaro
 *
 */
public class KeywordInterpreter
{
    StringBuffer rule;
    List<Object[]> replaceValues;
    
    public KeywordInterpreter()
    {
        rule = new StringBuffer();
        replaceValues = new ArrayList<Object[]>();
    }
    
    public String getFormatRule()
    {
        return rule.toString();
    }
    
    public void appendToRule(String str)
    {
        rule.append(str);
    }
    
    public void appendToRule(String str, DataSuggestorBase objRef, TagBase<?> tag)
    {
        rule.append(str);
        replaceValues.add(new Object[] {objRef, tag});
    }
    
    public DataSuggestorBase getClass(int i)
    {
        if(i > 0 && i < replaceValues.size())
        {
            return (DataSuggestorBase)replaceValues.get(i)[0];
        }
        return null;
    }
    
    public TagBase<?> getTag(int i)
    {
        if(i > 0 && i < replaceValues.size())
        {
            return (TagBase<?>)replaceValues.get(i)[1];
        }
        return null;
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
