package modules;

import java.util.HashMap;
import java.util.Map.Entry;

import model.KeywordInterpreter;
import model.Settings;
import model.base.InformationBase;
import model.base.TagBase;
import model.information.EditorComboBoxModel;
import support.EventCenter;
import support.EventCenter.Events;
import support.Logger;
import support.structure.EditorComboBoxMeta;
import support.util.Utilities.EditorTag;

public class AutoCompleter implements Logger{
    private HashMap<EditorTag, KeywordInterpreter> editorAutoComplete; // store auto complete fields
    private EditorComboBoxModel editorMap; // Tag to ComboBox data (editor text and drop down)
    private AutoCorrecter autoCorrecter;
    
    public AutoCompleter(EditorComboBoxModel editorMap, AutoCorrecter autoCorecter) {
        this.editorMap = editorMap;
        autoCorrecter = autoCorecter;
        editorAutoComplete = new HashMap<EditorTag, KeywordInterpreter>();
        
        
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
    

    public void triggerAutoFill() {
        for(Entry<EditorTag, KeywordInterpreter> entry : editorAutoComplete.entrySet()) {
            EditorComboBoxMeta meta = editorMap.getMeta(entry.getKey()); // get combo box to modify

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
//                            System.out.println("Entry: " + entry.getKey() + " DecodedString: " + finalValue);
            //                meta.getTextProperty().set(finalValue); // set input box text

            // check db for caps matching text to replace
            autoCorrecter.setTextFormattedFromDB(entry.getKey(), finalValue);

            //TODO create class that does text replacement (ie (karoke) -> (intrumental), (tv edit) -> (tv size) etc) 
        }
    }

}
