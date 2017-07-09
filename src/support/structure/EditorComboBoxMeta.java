package support.structure;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;

/**
 * Struct for storing information about editor's combo box
 * @author Ikersaro
 *
 */
public class EditorComboBoxMeta
{
    private ListProperty<String> dropDownProperty;
    private SimpleStringProperty textProperty;
    private boolean allowAutoFill;
    private boolean paused;

    public EditorComboBoxMeta()
    {
        dropDownProperty = new SimpleListProperty<String>();
        dropDownProperty.set(FXCollections.observableArrayList());
        textProperty = new SimpleStringProperty();
        allowAutoFill = true;
        paused = true;
    }
    
    public ListProperty<String> getDropDownListProperty()
    {
        return dropDownProperty;
    }

    public SimpleStringProperty getTextProperty()
    {
        return textProperty;
    }
    
    public boolean shouldStopAutoFill()
    {
        return allowAutoFill;
    }
    
    public void setAllowAutoFill(boolean flag)
    {
        allowAutoFill = flag;
    }
    
    public boolean isPaused()
    {
        return paused;
    }
    
    public void setPaused(boolean flag)
    {
        paused = flag;
    }
    
    public void clear()
    {
        dropDownProperty.clear();
        textProperty.set("");
    }
}
