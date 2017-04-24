package models;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import javafx.beans.property.SimpleStringProperty;
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
        RULE_FILENAME("Autocomplete Filename with Rule"),
//        RULE_TITLE("Autocomplete Title with Rule"),
//        RULE_ARTIST("Autocomplete Artist with Rule"),
//        RULE_ALBUM("Autocomplete Album with Rule"),
//        RULE_ALBUM_ARTIST("Autocomplete Album Artist with Rule"),
//        RULE_TRACK("Autocomplete Track with Rule"),
//        RULE_YEAR("Autocomplete Year with Rule"),
//        RULE_GENRE("Autocomplete Genere wih Rule"),
        RULE_COMMENT("Autocomplete Comment with Rule");
        
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

    public static final byte PROPAGATE_SAVE_ARTIST_MASK = 0x01;
    public static final byte PROPAGATE_SAVE_ALBUM_MASK = 0x02;
    public static final byte PROPAGATE_SAVE_ALBUM_ARTIST_MASK = 0x04;
    public static final byte PROPAGATE_SAVE_YEAR_MASK = 0x08;
    public static final byte PROPAGATE_SAVE_GENRE_MASK = 0x10;
    public static final byte PROPAGATE_SAVE_COMMENT_MASK = 0x20;
    public static final byte PROPAGATE_SAVE_ALBUM_ART_MASK = 0x40;
    
    private final String fileName = "audioTagger.cfg"; 
    private File settingsFile;
    
    private HashMap<SettingsKey, SettingsMap> map;
    
    private byte sessionSettings;
    
    

    /** 
     * Private constructor to prevent instantiating multiple instances.
     *  Use getInstance() to get singleton.
     */
    private Settings()
    {
        map = new HashMap<>();
        
        sessionSettings = 0xF;
        settingsFile = new File(fileName);
        if(settingsFile.exists())
        {
            loadSettings();
        }
        else
        {
            resetToDefaults();
        }
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
//        map.add(new SettingsMap(SettingsKey.RULE_ARTIST, ""));
//        map.add(new SettingsMap(SettingsKey.RULE_ALBUM, ""));
//        map.add(new SettingsMap(SettingsKey.RULE_ALBUM_ARTIST, ""));
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
