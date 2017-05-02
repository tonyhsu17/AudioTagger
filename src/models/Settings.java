package models;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;

import javafx.beans.property.SimpleStringProperty;
import models.dataSuggestors.DataSuggestorBase;
import support.TagBase;
import support.Utilities;
import support.Utilities.Tag;


/** 
 * Singleton class for a centralized location to store settings.
 * 
 * @author Tony Hsu
 */
public class Settings
{
    /**
     * Singleton Initialization
     */
    private static Settings self = new Settings();
    
    public static enum SettingsKey {
        PROPAGATE_SAVE_ARTIST("Propagate Save for Artist"),
        PROPAGATE_SAVE_ALBUM("Propagate Save for Artist"),
        PROPAGATE_SAVE_ALBUM_ARTIST("Propagate Save for Artist"),
        PROPAGATE_SAVE_YEAR("Propagate Save for Artist"),
        PROPAGATE_SAVE_GENRE("Propagate Save for Artist"),
        PROPAGATE_SAVE_COMMENT("Propagate Save for Artist"),
        PROPAGATE_SAVE_ALBUM_ART("Propagate Save for Artist"),
        RULE_FILENAME("Autocomplete Filename"),
//        RULE_TITLE("Autocomplete Title with Rule"),
//        RULE_ARTIST("Autocomplete Artist with Rule"),
//        RULE_ALBUM("Autocomplete Album with Rule"),
        RULE_ALBUM_ARTIST("Autocomplete Album Artist"),
//        RULE_TRACK("Autocomplete Track with Rule"),
//        RULE_YEAR("Autocomplete Year with Rule"),
//        RULE_GENRE("Autocomplete Genere wih Rule"),
        RULE_COMMENT("Autocomplete Comment"),;
        
        String description;
        private SettingsKey(String description)
        {
            this.description = description;
        }
        
        public String getDescription()
        {
            return description;
        }
        
        public static SettingsKey toKey(String str) {
            for(SettingsKey key : SettingsKey.values()) {
                if(key.toString().equals(str)) {
                    return key;
                }
            }
            return null;
        }
    }
    
    private final String fileName = "audioTagger.cfg"; 
    private File settingsFile;
    private HashMap<String, KeywordTagMetaData> keywordTagsDataMapping;
    
    private HashMap<SettingsKey, SettingsMap> map;
    
    private class KeywordTagMetaData
    {
        private DataSuggestorBase dataClass;
        private TagBase<?> tag;
        
        public KeywordTagMetaData(DataSuggestorBase dataClass, TagBase<?> tag)
        {
            this.dataClass = dataClass;
            this.tag = tag;
        }
        
        public DataSuggestorBase getSuggestorClass()
        {
            return dataClass;
        }
        
        public TagBase<?> getTag()
        {
            return tag;
        }
    }
    
    /** 
     * Private constructor to prevent instantiating multiple instances.
     *  Use getInstance() to get singleton.
     */
    private Settings()
    {
        map = new HashMap<>();
        
        settingsFile = new File(fileName);
        if(settingsFile.exists())
        {
            resetToDefaults();
//            loadSettings();
        }
        else
        {
            resetToDefaults();
        }
        keywordTagsDataMapping = new HashMap<>();
    }
    
    private void resetToDefaults()
    {
        map.clear();
        map.put(SettingsKey.PROPAGATE_SAVE_ARTIST, new SettingsMap(SettingsKey.PROPAGATE_SAVE_ARTIST, "true"));
        map.put(SettingsKey.PROPAGATE_SAVE_ALBUM, new SettingsMap(SettingsKey.PROPAGATE_SAVE_ALBUM, "true"));
        map.put(SettingsKey.PROPAGATE_SAVE_ALBUM_ARTIST, new SettingsMap(SettingsKey.PROPAGATE_SAVE_ALBUM_ARTIST, "true"));
        map.put(SettingsKey.PROPAGATE_SAVE_YEAR, new SettingsMap(SettingsKey.PROPAGATE_SAVE_YEAR, "true"));
        map.put(SettingsKey.PROPAGATE_SAVE_GENRE, new SettingsMap(SettingsKey.PROPAGATE_SAVE_GENRE, "true"));
        map.put(SettingsKey.PROPAGATE_SAVE_COMMENT, new SettingsMap(SettingsKey.PROPAGATE_SAVE_COMMENT, "true"));
        map.put(SettingsKey.PROPAGATE_SAVE_ALBUM_ART, new SettingsMap(SettingsKey.PROPAGATE_SAVE_ALBUM_ART, "true"));
        map.put(SettingsKey.RULE_FILENAME, new SettingsMap(SettingsKey.RULE_FILENAME, ""));
//        map.add(new SettingsMap(SettingsKey.RULE_TITLE, ""));
//        map.put(SettingsKey.RULE_ARTIST, new SettingsMap(SettingsKey.RULE_ARTIST, ""));
//        map.add(new SettingsMap(SettingsKey.RULE_ALBUM, ""));
        map.put(SettingsKey.RULE_ALBUM_ARTIST, new SettingsMap(SettingsKey.RULE_ALBUM_ARTIST, ""));
//        map.add(new SettingsMap(SettingsKey.RULE_TRACK, ""));
//        map.add(new SettingsMap(SettingsKey.RULE_YEAR, ""));
//        map.add(new SettingsMap(SettingsKey.RULE_GENRE, ""));
        map.put(SettingsKey.RULE_COMMENT, new SettingsMap(SettingsKey.RULE_COMMENT, ""));
    }
    
    /** 
     * Load settings
     */
    private void loadSettings()
    {
        Scanner sc;
        try
        {
            sc = new Scanner(settingsFile);
            while(sc.hasNextLine())
            {
                String line = sc.nextLine(); // Grab each setting line
                String[] splitLine = line.split("=", 2); // parse setting to [key, value]
                
                SettingsKey key = SettingsKey.toKey(splitLine[0]);
                String value = splitLine[1];
                
                if(key != null)
                {
                    map.put(key, new SettingsMap(key, splitLine[1]));
                }
            }
        }
        catch (FileNotFoundException e)
        {
            // Should not come here since file is confirmed first
        }
    }

    /** 
     * @return Singleton of Configuration
     */
    public static Settings getInstance()
    {
        return self;
    }
    
    public SettingsMap getKeyValuePair(SettingsKey key)
    {
        return map.get(key);
    }
    
    public void setSetting(SettingsKey key, String value)
    {
        switch(key)
        {
            case PROPAGATE_SAVE_ALBUM:
            case PROPAGATE_SAVE_ALBUM_ART:
            case PROPAGATE_SAVE_ALBUM_ARTIST:
            case PROPAGATE_SAVE_ARTIST:
            case PROPAGATE_SAVE_COMMENT:
            case PROPAGATE_SAVE_GENRE:
            case PROPAGATE_SAVE_YEAR:
                map.get(key).setValue(Utilities.convertToBoolean(value) + "");
                break;
            case RULE_COMMENT:
            case RULE_FILENAME:
            default:
                map.get(key).setValue(value);
                break;
        }
    }
    
    public boolean isAnyPropagateSaveOn()
    {
        return isPropagateSaveOn(SettingsKey.PROPAGATE_SAVE_ALBUM) || 
            isPropagateSaveOn(SettingsKey.PROPAGATE_SAVE_ALBUM_ART) || 
            isPropagateSaveOn(SettingsKey.PROPAGATE_SAVE_ALBUM_ARTIST) || 
            isPropagateSaveOn(SettingsKey.PROPAGATE_SAVE_ARTIST) || 
            isPropagateSaveOn(SettingsKey.PROPAGATE_SAVE_COMMENT) || 
            isPropagateSaveOn(SettingsKey.PROPAGATE_SAVE_GENRE) || 
            isPropagateSaveOn(SettingsKey.PROPAGATE_SAVE_YEAR);
    }
    
    public boolean isPropagateSaveOn(SettingsKey key)
    {
        boolean flag = false;
        switch(key)
        {
            case PROPAGATE_SAVE_ALBUM:
            case PROPAGATE_SAVE_ALBUM_ART:
            case PROPAGATE_SAVE_ALBUM_ARTIST:
            case PROPAGATE_SAVE_ARTIST:
            case PROPAGATE_SAVE_COMMENT:
            case PROPAGATE_SAVE_GENRE:
            case PROPAGATE_SAVE_YEAR:
                flag = Utilities.convertToBoolean(map.get(key).getValue());
                break;
            default:
                break;
        }
        return flag;
    }
    
   
    
    
    public void setKeywordTags(HashMap<DataSuggestorBase, List<TagBase<?>>> mapping)
    {
        for(Entry<DataSuggestorBase, List<TagBase<?>>> entry : mapping.entrySet())
        {
            for(TagBase<?> tag : entry.getValue())
            {
//                System.out.println("$" + entry.getKey().getDisplayKeywordTagClassName() + "." + tag.name());
                keywordTagsDataMapping.put("$" + entry.getKey().getDisplayKeywordTagClassName() + "." + tag.name(), 
                    new KeywordTagMetaData(entry.getKey(), tag));
            }
        }
    }
    //Akame ga Kill ED Single - Konna Sekai, Shiritaku Nakatta. [Miku Sawai]
    public KeywordInterpreter getRuleFor(Tag tag)
    {
        String rule = "";
        // set rule
        switch(tag)
        {
            case ALBUM:
                break;
            case ALBUM_ART:
                break;
            case ALBUM_ARTIST:
                rule = map.get(SettingsKey.RULE_ALBUM_ARTIST).getValue();
                break;
            case ALBUM_ART_META:
                break;
            case ARTIST:
                break;
            case COMMENT:
                rule = map.get(SettingsKey.RULE_COMMENT).getValue();
                break;
            case FILE_NAME:
                rule = map.get(SettingsKey.RULE_FILENAME).getValue();
                break;
            case GENRE:
                break;
            case TITLE:
                break;
            case TRACK:
                break;
            case YEAR:
                break;
            default:
                break;
            
        }
        
     // $VGMDB.SERIES $VGMDB.THEME Single - $Editor.TITLE [$Editor.ARTIST]
        
        // extrapolate each tag out
        // return string as "%s %s Single - %s [%s]", arr[ of class + tag ] 
        // using string formatter to fill in the valus
        
        KeywordInterpreter builder = new KeywordInterpreter();  // recombination of string
        String[] splitRule = rule.split("[$]"); // split by prefix
        // for each split
        for(String parsed : splitRule)
        {
            // if the split is not empty
            if(!parsed.isEmpty()) {
                // check each keywordTag to find a match
                for(String s : keywordTagsDataMapping.keySet())
                {
                    // if keywordTag matched with parsed text
                    if(parsed.startsWith(s.substring(1), 0))
                    {
                        // replace keywordTag with string formatter %s
                        builder.appendToRule("%s" + parsed.substring(s.length() - 1), 
                            keywordTagsDataMapping.get(s).getSuggestorClass(),
                            keywordTagsDataMapping.get(s).getTag());
                    }
                }
            }
        }
        
        return builder;
    }
    
    public List<String> getKeywordTags()
    {
        return new ArrayList<String>(keywordTagsDataMapping.keySet());
    }
    
    /** 
     * Save Settings
     */
    public void saveSettings()
    {
        for(SettingsMap sm : map.values())
        {
            sm.save();
        }
        writeSettings();
    }
    
    public void revertSettings()
    {
        for(SettingsMap sm : map.values())
        {
            sm.revert();
        }
    }
    
    public void writeSettings()
    {
        try
        {
            BufferedWriter output = new BufferedWriter(new FileWriter(settingsFile));
            for(SettingsMap sm : map.values())
            {
                output.write(sm.getKey() + "=" + sm.getValue());
                output.newLine();
            }
            output.close();
        }
        catch (IOException e)
        {
           
        }
    }
    
    public class SettingsMap {
        private SettingsKey key;
        private String value;
        private final SimpleStringProperty displayValue;
        
        public SettingsMap(SettingsKey key, String value)
        {
            this.key = key;
            this.value = value;
            this.displayValue = new SimpleStringProperty(value);
        }
        
        public SettingsKey getKey() {
            return key;
        }
        
        public String getValue() {
            return value;
        }
        
        private void setValue(String value) {
            this.value = value;
        }
        
        public String getKeyDescription() {
            return key.getDescription();
        }
        
        public String getDisplayValue() {
            return displayValue.get();
        }
        
        public void setDisplayValue(String value) {
            displayValue.set(value);
        }
        
        public void save() {
            System.out.println("saving: " + displayValue.get() + " " + value);
            value = displayValue.get();
        }
        
        public void revert() {
            displayValue.set(value);
        }
    }
}
