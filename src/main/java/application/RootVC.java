package application;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.tonyhsu17.utilities.Logger;

import java.io.IOException;



/**
 * @author Tony Hsu
 */
public class RootVC implements Logger
{
    private AudioTaggerVC taggerVC;
    
    public RootVC()
    {
    }
    
    public void setAudioTaggerVC(AudioTaggerVC tagger)
    {
        taggerVC = tagger;
    }
    
    @FXML
    private void initialize()
    {
        
    }
    
    @FXML
    public void saveTags()
    {
        if(taggerVC != null)
        {
            info("SAVING");
            taggerVC.saveTags();
        }
        
    }
    
    @FXML
    private void keyPressed(KeyEvent e)
    {
        if(e.isControlDown())
        {
            switch(e.getText().toLowerCase())
            {
                case "s":
                    info("Hotkey Save");
                    taggerVC.saveTags();
                    break;
                case "r":
                    info("Hotkey AutoFill Toggle");
                    taggerVC.triggerAutoFill();
                    break;
                case "e":
                    info("Hotkey Autocorrect Triggered");
                    taggerVC.triggerAutoCorrect();
                    break;
            }
        }
    }
    
    @FXML
    private void openSettings(ActionEvent event)
    {
        try
        {
            info("Opening Settings");
            Stage stage = new Stage();
            Parent loader = FXMLLoader.load(getClass().getClassLoader().getResource("SettingsView.fxml"));
            
            stage.setScene(new Scene(loader));
            stage.setTitle("Preferences");
            stage.initModality(Modality.WINDOW_MODAL);
//            stage.initOwner(((MenuItem)event.getSource()).getScene().getWindow());
            stage.show();
            
        }
        catch (IOException e)
        {
            error(e);
        }
    }

    @FXML
    private void openHotkeysPanel(ActionEvent event) {
        try
        {
            info("Opening hotkeys");
            Stage stage = new Stage();
            Parent loader = FXMLLoader.load(getClass().getClassLoader().getResource("HotkeysView.fxml"));

            stage.setScene(new Scene(loader));
            stage.setTitle("Hotkeys");
            stage.initModality(Modality.WINDOW_MODAL);
            //            stage.initOwner(((MenuItem)event.getSource()).getScene().getWindow());
            stage.show();

        }
        catch (IOException e)
        {
            error(e);
        }
    }
    
    @FXML
    private void toggleAllPropagateSaveConfig()
    {
        // meh options are still checked/not updated
//        togglePropSaveArtistConfig();
//        togglePropSaveAlbumConfig();
//        togglePropSaveAlbumArtistConfig();
//        togglePropSaveYearConfig();
//        togglePropSaveGenreConfig();
//        togglePropSaveCommentConfig();
//        togglePropSaveAlbumArtConfig();
    }
    
}
