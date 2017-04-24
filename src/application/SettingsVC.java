package application;

import java.awt.TextField;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;
import models.Settings;
import models.Settings.SettingsKey;
import models.Settings.SettingsMap;
import support.Utilities.Tag;


/** 
 * Singleton class for a centralized location to store settings.
 * 
 * @author Tony Hsu
 */
public class SettingsVC
{
    Settings settings;
    @FXML
    Stage self;
    
    ObservableList<Settings.SettingsMap> data;

    @FXML
    TableView<Settings.SettingsMap> table;
    @FXML
    TableColumn<Settings.SettingsMap, String> properties;
    @FXML
    TableColumn<Settings.SettingsMap, String> values;
    
    public SettingsVC()
    {
        settings = Settings.getInstance();
        data = FXCollections.observableArrayList();
    }
    
    public void setStage(Stage stage)
    {
        
    }
    
    @FXML
    private void initialize()
    {
        System.out.println("settingsVC");
//        data.add(Settings.getInstance().getKeyValuePair("Temp"));
        properties.setCellValueFactory(
            new PropertyValueFactory<Settings.SettingsMap, String>("keyDescription"));
        values.setCellValueFactory(
            new PropertyValueFactory<Settings.SettingsMap, String>("displayValue"));
        values.setEditable(true);
        values.setCellFactory(TextFieldTableCell.forTableColumn());
        table.setItems(data);
        populateSettings();
    }
    
    private void populateSettings()
    {
        for(SettingsKey key : SettingsKey.values())
        {
            data.add(settings.getKeyValuePair(key));
        }
        
    }
    
    @FXML
    private void valueChanged(CellEditEvent<Settings.SettingsMap, String> t) {
        System.out.println("value changed: " + t.getNewValue());
        ((SettingsMap)t.getTableView().getItems().get(
                t.getTablePosition().getRow())
                ).setDisplayValue(t.getNewValue());
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
