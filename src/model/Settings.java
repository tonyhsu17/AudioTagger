package model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;

import model.base.InformationBase;
import model.base.TagBase;
import support.EventCenter;
import support.EventCenter.Events;
import support.Logger;
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
        PROPAGATE_SAVE_ARTIST("Propagate Save for Artist"), PROPAGATE_SAVE_ALBUM("Propagate Save for Artist"),
        PROPAGATE_SAVE_ALBUM_ARTIST("Propagate Save for Artist"), PROPAGATE_SAVE_YEAR("Propagate Save for Artist"),
        PROPAGATE_SAVE_GENRE("Propagate Save for Artist"), PROPAGATE_SAVE_COMMENT("Propagate Save for Artist"),
        PROPAGATE_SAVE_ALBUM_ART("Propagate Save for Artist"), RULE_FILENAME("Autocomplete Filename"),
        // RULE_TITLE("Autocomplete Title with Rule"),
        // RULE_ARTIST("Autocomplete Artist with Rule"),
        // RULE_ALBUM("Autocomplete Album with Rule"),
        RULE_ALBUM_ARTIST("Autocomplete Album Artist"),
        // RULE_TRACK("Autocomplete Track with Rule"),
        // RULE_YEAR("Autocomplete Year with Rule"),
        // RULE_GENRE("Autocomplete Genere wih Rule"),
        RULE_COMMENT("Autocomplete Comment"),;

        String description;

        private SettingsKey(String description) {
            this.description = description;
        }

        public String getDescription() {
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
            // resetToDefaults();
            loadSettings();
        }
        else {
            resetToDefaults();
        }
        keywordTagsDataMapping = new HashMap<>();
    }

    private void resetToDefaults() {
        map.clear();
        map.put(SettingsKey.PROPAGATE_SAVE_ARTIST, new SettingsTableViewMeta(SettingsKey.PROPAGATE_SAVE_ARTIST, "true"));
        map.put(SettingsKey.PROPAGATE_SAVE_ALBUM, new SettingsTableViewMeta(SettingsKey.PROPAGATE_SAVE_ALBUM, "true"));
        map.put(SettingsKey.PROPAGATE_SAVE_ALBUM_ARTIST, new SettingsTableViewMeta(SettingsKey.PROPAGATE_SAVE_ALBUM_ARTIST, "true"));
        map.put(SettingsKey.PROPAGATE_SAVE_YEAR, new SettingsTableViewMeta(SettingsKey.PROPAGATE_SAVE_YEAR, "true"));
        map.put(SettingsKey.PROPAGATE_SAVE_GENRE, new SettingsTableViewMeta(SettingsKey.PROPAGATE_SAVE_GENRE, "true"));
        map.put(SettingsKey.PROPAGATE_SAVE_COMMENT, new SettingsTableViewMeta(SettingsKey.PROPAGATE_SAVE_COMMENT, "true"));
        map.put(SettingsKey.PROPAGATE_SAVE_ALBUM_ART, new SettingsTableViewMeta(SettingsKey.PROPAGATE_SAVE_ALBUM_ART, "true"));
        map.put(SettingsKey.RULE_FILENAME, new SettingsTableViewMeta(SettingsKey.RULE_FILENAME, ""));
        // map.add(new SettingsLabelMeta(SettingsKey.RULE_TITLE, ""));
        // map.put(SettingsKey.RULE_ARTIST, new SettingsLabelMeta(SettingsKey.RULE_ARTIST, ""));
        // map.add(new SettingsLabelMeta(SettingsKey.RULE_ALBUM, ""));
        map.put(SettingsKey.RULE_ALBUM_ARTIST, new SettingsTableViewMeta(SettingsKey.RULE_ALBUM_ARTIST, ""));
        // map.add(new SettingsLabelMeta(SettingsKey.RULE_TRACK, ""));
        // map.add(new SettingsLabelMeta(SettingsKey.RULE_YEAR, ""));
        // map.add(new SettingsLabelMeta(SettingsKey.RULE_GENRE, ""));
        map.put(SettingsKey.RULE_COMMENT, new SettingsTableViewMeta(SettingsKey.RULE_COMMENT, ""));
    }

    /**
     * Load settings
     */
    private void loadSettings() {
        Scanner sc;
        try {
            sc = new Scanner(settingsFile);
            while(sc.hasNextLine()) {
                String line = sc.nextLine(); // Grab each setting line
                String[] splitLine = line.split("=", 2); // parse setting to [key, value]

                SettingsKey key = SettingsKey.toKey(splitLine[0]);
                info(key.toString());

                if(key != null) {
                    map.put(key, new SettingsTableViewMeta(key, splitLine[1]));
                }
            }
        }
        catch (FileNotFoundException e) {
            // Should not come here since file is confirmed first
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
        return isPropagateSaveOn(SettingsKey.PROPAGATE_SAVE_ALBUM) ||
               isPropagateSaveOn(SettingsKey.PROPAGATE_SAVE_ALBUM_ART) ||
               isPropagateSaveOn(SettingsKey.PROPAGATE_SAVE_ALBUM_ARTIST) ||
               isPropagateSaveOn(SettingsKey.PROPAGATE_SAVE_ARTIST) ||
               isPropagateSaveOn(SettingsKey.PROPAGATE_SAVE_COMMENT) ||
               isPropagateSaveOn(SettingsKey.PROPAGATE_SAVE_GENRE) ||
               isPropagateSaveOn(SettingsKey.PROPAGATE_SAVE_YEAR);
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

        if(rule.isEmpty() || keywordTagsDataMapping.keySet().isEmpty()) {
            return null; // return early if no rule found
        }

        // extrapolate each tag out
        // return string as "%s %s Single - %s [%s]", arr[ of class + tag ]
        // using string formatter to fill in the valus

        KeywordInterpreter builder = new KeywordInterpreter(); // recombination of string
        String[] splitRule = rule.split("[$]"); // split by prefix

        int total = 0; // total keywords
        int count = 0; // num of keywords found
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

        EventCenter.getInstance().postEvent(Events.SettingChanged, null);
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
