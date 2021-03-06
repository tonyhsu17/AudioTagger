package modules;

import model.Settings;
import modules.controllers.base.InformationBase;
import modules.controllers.base.TagBase;
import org.tonyhsu17.utilities.EventCenter;
import org.tonyhsu17.utilities.EventCenter.Events;
import org.tonyhsu17.utilities.Logger;
import support.util.Utilities.EditorTag;

import java.util.HashMap;
import java.util.Map.Entry;



/**
 * Helper class to fill in fields based on setting configurations.
 */
public class AutoCompleter implements Logger{
    private HashMap<EditorTag, KeywordInterpreter> editorAutoComplete; // store auto complete fields


    /**
     * Callback for when data has been
     */
    public interface AutoCompleterCB {
        public void result(EditorTag tag, String finalVal);
    }

    /**
     * Helper class to fill in fields based on setting configurations.
     */
    public AutoCompleter() {
        editorAutoComplete = new HashMap<EditorTag, KeywordInterpreter>();
        
        // Subscribe to events
        EventCenter.getInstance().subscribeEvent(Events.SETTINGS_CHANGED, this, (obj) -> {
            updateAutoFillRules(); // if settings updates, update rule
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
     * For each EditorTag with a rule, Fetch and constructs data based on the rule.
     * @param cb AutoCompleterCB, Callback of (EditorTag tag, String constructedText)
     */
    public void triggerAutoFill(AutoCompleterCB cb) {
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
            cb.result(entry.getKey(), finalValue);
        }
    }

}
