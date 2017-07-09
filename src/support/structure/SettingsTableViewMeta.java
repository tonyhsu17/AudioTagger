package support.structure;

import javafx.beans.property.SimpleStringProperty;
import model.Settings.SettingsKey;

/**
 * Struct for storing information about settings' list view
 * @author Ikersaro
 *
 */
public class SettingsTableViewMeta
{
    private SettingsKey key;
    private String value;
    private final SimpleStringProperty displayValue;

    public SettingsTableViewMeta(SettingsKey key, String value)
    {
        this.key = key;
        this.value = value;
        this.displayValue = new SimpleStringProperty(value);
    }

    public SettingsKey getKey()
    {
        return key;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public String getKeyDescription()
    {
        return key.getDescription();
    }

    public String getDisplayValue()
    {
        return displayValue.get();
    }

    public void setDisplayValue(String value)
    {
        displayValue.set(value);
    }

    public void save()
    {
        value = displayValue.get();
    }

    public void revert()
    {
        displayValue.set(value);
    }
}
