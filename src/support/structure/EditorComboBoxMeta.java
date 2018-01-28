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

    public EditorComboBoxMeta()
    {
        dropDownProperty = new SimpleListProperty<String>();
        dropDownProperty.set(FXCollections.observableArrayList());
        textProperty = new SimpleStringProperty();
    }
    
    public ListProperty<String> getDropDownListProperty()
    {
        return dropDownProperty;
    }

    public SimpleStringProperty getTextProperty()
    {
        return textProperty;
    }
    
    public void clear()
    {
        dropDownProperty.clear();
        textProperty.set("");
    }
}
