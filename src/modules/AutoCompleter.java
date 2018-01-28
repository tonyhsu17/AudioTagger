package modules;

import java.util.HashMap;
import java.util.Map.Entry;

import model.KeywordInterpreter;
import model.Settings;
import model.base.InformationBase;
import model.base.TagBase;
import support.EventCenter;
import support.EventCenter.Events;
import support.Logger;
import support.util.Utilities.EditorTag;

public class AutoCompleter implements Logger{
    private HashMap<EditorTag, KeywordInterpreter> editorAutoComplete; // store auto complete fields
    private AutoCorrecter autoCorrecter;
    
    public AutoCompleter(AutoCorrecter autoCorecter) {
        autoCorrecter = autoCorecter;
        editorAutoComplete = new HashMap<EditorTag, KeywordInterpreter>();
        
        // Subscribe to events
        EventCenter.getInstance().subscribeEvent(Events.SETTINGS_CHANGED, this, (obj) -> {
            updateAutoFillRules();
        });
        EventCenter.getInstance().subscribeEvent(Events.TRIGGER_AUTO_FILL, this, (obj) -> {
            triggerAutoFill();
        });
    }
    
    public void updateAutoFillRules() {
        editorAutoComplete.clear(); // clear list and add/re-add rules
        for(EditorTag t : EditorTag.values()) // for each tag
        {
            KeywordInterpreter temp = null;
            // if there is a rule, add to list
            if((temp = Settings.getInstance().getRuleFor(t)) != null) {
                info("Adding rule for: " + t);
                editorAutoComplete.put(t, temp);
            }
        }
    }

    /**
     * Triggers a fetch of data based on rule and applies formatted text
     */
    // TODO mark if build has empty values and dont autofill?
    public void triggerAutoFill() {
        for(Entry<EditorTag, KeywordInterpreter> entry : editorAutoComplete.entrySet()) {
            KeywordInterpreter builder = entry.getValue();
            InformationBase classObj;
            TagBase<?> tag;

            // pass values into builder to construct the value with given tags and info
            for(int i = 0; i < builder.getCount(); i++) {
                classObj = builder.getClass(i);
                tag = builder.getTag(i);
                builder.setValue(i, classObj.getDataForTag(tag, "")); // pass data to builder
            }
           
            String finalValue = builder.buildString(); // get the final results

            // send data to autoCorrect to be displayed
            autoCorrecter.setTextFormattedFromDB(entry.getKey(), finalValue);

            //TODO create class that does text replacement (ie (karoke) -> (intrumental), (tv edit) -> (tv size) etc) 
        }
    }

}
