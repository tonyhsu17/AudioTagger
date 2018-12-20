package model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;

import org.tonyhsu17.utilities.EventCenter;
import org.tonyhsu17.utilities.EventCenter.Events;
import org.tonyhsu17.utilities.Logger;

import modules.KeywordInterpreter;
import modules.controllers.base.InformationBase;
import modules.controllers.base.TagBase;
import support.structure.SettingsTableViewMeta;
import support.util.Utilities;
import support.util.Utilities.EditorTag;



/**
 * Singleton class for a centralized location to store settings.
 * 
 * @author Tony Hsu
 */
public class Settings implements Logger {
    /**
     * Singleton Initialization
     */
    private static Settings self = new Settings();

    public static enum SettingsKey {
        PROPAGATE_SAVE_ARTIST("Propagate Save for Artist"), PROPAGATE_SAVE_ALBUM("Propagate Save for Album"),
        PROPAGATE_SAVE_ALBUM_ARTIST("Propagate Save for Album Artist"), PROPAGATE_SAVE_YEAR("Propagate Save for Year"),
        PROPAGATE_SAVE_GENRE("Propagate Save for Genre"), PROPAGATE_SAVE_COMMENT("Propagate Save for Comment"),
        PROPAGATE_SAVE_ALBUM_ART("Propagate Save for Album Art"),
        RULE_FILENAME("Autocomplete Filename"), RULE_TITLE("Autocomplete Title with Rule"),
        RULE_ARTIST("Autocomplete Artist with Rule"), RULE_ALBUM("Autocomplete Album with Rule"),
        RULE_ALBUM_ARTIST("Autocomplete Album Artist"), RULE_TRACK("Autocomplete Track with Rule"),
        RULE_YEAR("Autocomplete Year with Rule"), RULE_GENRE("Autocomplete Genere wih Rule"),
        RULE_COMMENT("Autocomplete Comment"),;

        String description;

        private SettingsKey(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public String debug() {
            return "SettingsKey [name=" + name() + ", description=" + description + "]";
        }

        public static SettingsKey toKey(String str) {
            for(SettingsKey key : SettingsKey.values()) {
                if(key.toString().equals(str)) {
                    return key;
                }
            }
            return null;
        }

        public static SettingsKey[] getPropagates() {
            return new SettingsKey[] {PROPAGATE_SAVE_ALBUM,
                PROPAGATE_SAVE_ALBUM_ART,
                PROPAGATE_SAVE_ALBUM_ARTIST,
                PROPAGATE_SAVE_ARTIST,
                PROPAGATE_SAVE_COMMENT,
                PROPAGATE_SAVE_GENRE,
                PROPAGATE_SAVE_YEAR};
        }

        public static SettingsKey[] getRules() {
            return new SettingsKey[] {RULE_FILENAME, RULE_TITLE, RULE_ARTIST, RULE_ALBUM, 
                RULE_ALBUM_ARTIST, RULE_TRACK, RULE_YEAR, RULE_GENRE, RULE_COMMENT};
        }
    }

    private final String fileName = "audioTagger.cfg";
    private File settingsFile;
    private HashMap<String, KeywordTagMetaData> keywordTagsDataMapping;

    private HashMap<SettingsKey, SettingsTableViewMeta> map;

    private class KeywordTagMetaData {
        private InformationBase dataClass;
        private TagBase<?> tag;

        public KeywordTagMetaData(InformationBase dataClass, TagBase<?> tag) {
            this.dataClass = dataClass;
            this.tag = tag;
        }

        public InformationBase getSuggestorClass() {
            return dataClass;
        }

        public TagBase<?> getTag() {
            return tag;
        }
    }

    /**
     * Private constructor to prevent instantiating multiple instances.
     * Use getInstance() to get singleton.
     */
    private Settings() {
        map = new HashMap<>();

        settingsFile = new File(fileName);
        if(settingsFile.exists()) {
             resetToDefaults();
            loadSettings();
        }
        else {
            resetToDefaults();
        }
        keywordTagsDataMapping = new HashMap<>();
    }

    private void resetToDefaults() {
        map.clear();
        for(SettingsKey key : SettingsKey.values()) {
            map.put(key, new SettingsTableViewMeta(key, "true"));
        }
    }

    /**
     * Load settings
     */
    private void loadSettings() {
        Scanner sc = null;
        try {
            sc = new Scanner(settingsFile);
            while(sc.hasNextLine()) {
                String line = sc.nextLine(); // Grab each setting line
                String[] splitLine = line.split("=", 2); // parse setting to [key, value]
                info(Arrays.toString(splitLine));
                SettingsKey key = SettingsKey.toKey(splitLine[0]);
                if(key != null) {
                    map.put(key, new SettingsTableViewMeta(key, splitLine[1]));
                }
            }
        }
        catch (FileNotFoundException e) {
            // Should not come here since file is confirmed first
        }
        finally {
            if(sc != null) {
                sc.close();
            }
        }
    }

    /**
     * @return Singleton of Configuration
     */
    public static Settings getInstance() {
        return self;
    }

    public SettingsTableViewMeta getKeyValuePair(SettingsKey key) {
        return map.get(key);
    }

    public void setSetting(SettingsKey key, String value) {
        switch (key) {
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

    public boolean isAnyPropagateSaveOn() {
        boolean flag = true;
        for(SettingsKey key : SettingsKey.getPropagates()) {
            flag |= isPropagateSaveOn(key);
        }
        return flag;
    }

    public boolean isPropagateSaveOn(SettingsKey key) {
        boolean flag = false;
        switch (key) {
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

    public void setKeywordTags(HashMap<InformationBase, List<TagBase<?>>> mapping) {
        for(Entry<InformationBase, List<TagBase<?>>> entry : mapping.entrySet()) {
            for(TagBase<?> tag : entry.getValue()) {
                // System.out.println("$" + entry.getKey().getDisplayKeywordTagClassName() + "." + tag.name());
                keywordTagsDataMapping.put("$" + entry.getKey().getDisplayKeywordTagClassName() + "." + tag.name(),
                    new KeywordTagMetaData(entry.getKey(), tag));
            }
        }
    }

    // Akame ga Kill ED Single - Konna Sekai, Shiritaku Nakatta. [Miku Sawai]
    public KeywordInterpreter getRuleFor(EditorTag tag) {
        String rule = "";
        // set rule
        switch (tag) {
            case ALBUM:
                rule = map.get(SettingsKey.RULE_ALBUM).getValue();
                break;
            case ALBUM_ART:
                break;
            case ALBUM_ARTIST:
                rule = map.get(SettingsKey.RULE_ALBUM_ARTIST).getValue();
                break;
            case ALBUM_ART_META:
                break;
            case ARTIST:
                rule = map.get(SettingsKey.RULE_ARTIST).getValue();
                break;
            case COMMENT:
                rule = map.get(SettingsKey.RULE_COMMENT).getValue();
                break;
            case FILE_NAME:
                rule = map.get(SettingsKey.RULE_FILENAME).getValue();
                break;
            case GENRE:
                rule = map.get(SettingsKey.RULE_GENRE).getValue();
                break;
            case TITLE:
                rule = map.get(SettingsKey.RULE_TITLE).getValue();
                break;
            case TRACK:
                break;
            case YEAR:
                rule = map.get(SettingsKey.RULE_YEAR).getValue();
                break;
            default:
                break;
        }

        if(rule == null || rule.isEmpty()) {
            return null; // return early if no rule found
        }

        // extrapolate each tag out
        // return string as "%s %s Single - %s [%s]", arr[ of class + tag ]
        // using string formatter to fill in the valus

        KeywordInterpreter builder = new KeywordInterpreter(); // recombination of string
        String[] splitRule;
        int total = 0; // total keywords
        int count = 0; // num of keywords found
        
        if(rule.contains("$")) {
            // case - rule
            splitRule = rule.split("[$]"); // split by prefix
            
            // for each split
            for(String parsed : splitRule) {
                // if the split word (parsed) is not empty
                if(!parsed.isEmpty()) {
                    total++;
                    // check each keywordTag in Mapping to find a match
                    for(String s : keywordTagsDataMapping.keySet()) {
                        // if keywordTag matched with parsed text
                        if(parsed.startsWith(s.substring(1), 0)) {
                            count++;
                            // replace keywordTag with string formatter %s
                            builder.appendToRule("%s" + parsed.substring(s.length() - 1), keywordTagsDataMapping.get(s).getSuggestorClass(),
                                keywordTagsDataMapping.get(s).getTag());
                            break;
                        }
                    }
                }
            }
        } else {
            // case - only text
            splitRule = new String[0];
            builder.appendToRule(rule);
        }
        
        // return builder only is same number of keywords
        return total == count ? builder : null;
    }

    public List<String> getKeywordTags() {
        ArrayList<String> keys = new ArrayList<String>(keywordTagsDataMapping.keySet());
        Collections.sort(keys);
        return keys;
    }

    /**
     * Save Settings
     */
    public void saveSettings() {
        for(SettingsTableViewMeta sm : map.values()) {
            sm.save();
        }
        writeSettings();

        EventCenter.getInstance().postEvent(Events.SETTINGS_CHANGED, null);
    }

    public void revertSettings() {
        for(SettingsTableViewMeta sm : map.values()) {
            sm.revert();
        }
    }

    public void writeSettings() {
        try {
            BufferedWriter output = new BufferedWriter(new FileWriter(settingsFile));
            for(SettingsTableViewMeta sm : map.values()) {
                System.out.println(sm.getKey() + "=" + sm.getValue());
                output.write(sm.getKey() + "=" + sm.getValue());
                output.newLine();
            }
            output.close();
        }
        catch (IOException e) {

        }
    }
}
