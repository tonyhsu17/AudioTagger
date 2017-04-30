package models;

import models.dataSuggestors.AudioFiles;

/**
 * Interprets preferred formatting using settings.
 * @author Ikersaro
 *
 */
public class KeywordInterpreter
{
    Settings settings;
    
    public static final String TRACK_NUM = "%TRACK%";
    public static final String TITLE = "%TITLE%";
    public static final String ANIME = "%ANIME%";
    public static final String ARTIST = "%ARTIST%";
    public static final String SONG_TYPE = "%TYPE%";
    
    public KeywordInterpreter()
    {
        settings = Settings.getInstance();
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
    
    
    public void test()
    {
        
    }
    
}
