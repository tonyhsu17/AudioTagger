package application;

import org.tonyhsu17.utilities.Logger;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import model.Settings;
import model.Settings.SettingsKey;
import support.structure.SettingsTableViewMeta;


/** 
 * Singleton class for a centralized location to store settings.
 * 
 * @author Tony Hsu
 */
public class SettingsVC implements Logger
{
    Settings settings;
    
    ObservableList<SettingsTableViewMeta> data;
    ListProperty<String> keywordTags;

    @FXML
    TableView<SettingsTableViewMeta> table;
    @FXML
    TableColumn<SettingsTableViewMeta, String> properties;
    @FXML
    TableColumn<SettingsTableViewMeta, String> values;
    @FXML
    ListView<String> variables;
    
    
    public SettingsVC()
    {
        settings = Settings.getInstance();
        data = FXCollections.observableArrayList();
        
        keywordTags = new SimpleListProperty<String>();
        keywordTags.set(FXCollections.observableArrayList());
    }
    
    public void setStage(Stage stage)
    {
        
    }
    
    @FXML
    private void initialize()
    {
        info("settingsVC");
        properties.setCellValueFactory(
            new PropertyValueFactory<SettingsTableViewMeta, String>("keyDescription"));
        values.setCellValueFactory(
            new PropertyValueFactory<SettingsTableViewMeta, String>("displayValue"));
        values.setEditable(true);
        values.setCellFactory(TextFieldTableCell.forTableColumn());
        table.setItems(data);
        populateSettings();
        keywordTags.addAll(settings.getKeywordTags());
        variables.itemsProperty().bind(keywordTags);
    }
    
    private void populateSettings()
    {
        for(SettingsKey key : SettingsKey.values())
        {
            debug(key.debug());
            data.add(settings.getKeyValuePair(key));
        }
        table.refresh();
    }
    
    @FXML
    private void valueChanged(CellEditEvent<SettingsTableViewMeta, String> t) {
        info("value changed: " + t.getNewValue() + "for " + t.getTableView().getItems().get(
            t.getTablePosition().getRow()).getKeyDescription());
        t.getTableView().getItems().get(
                t.getTablePosition().getRow()).setDisplayValue(t.getNewValue());
    }
    
    @FXML
    private void closeSettings(ActionEvent e)
    {
        settings.revertSettings();
        
        Stage stg = (Stage)((Button)e.getSource()).getScene().getWindow();
        stg.close();
    }
    
    @FXML
    private void saveSettings(ActionEvent e)
    {
        settings.saveSettings();
        
        Stage stg = (Stage)((Button)e.getSource()).getScene().getWindow();
        stg.close();
    }
}
