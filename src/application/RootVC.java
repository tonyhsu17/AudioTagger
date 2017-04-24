package application;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Settings;
import support.Utilities.Tag;

public class RootVC
{
    private MP3TaggerVC taggerVC;
    
    
    @FXML
    CheckMenuItem propSaveArtist;
    
    public RootVC()
    {
    }
    
    public void setMP3TaggerVC(MP3TaggerVC tagger)
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
            System.out.println("SAVING");
            taggerVC.saveTags();
        }
        
    }
    
    @FXML
    private void keyPressed(KeyEvent e)
    {
        if(e.isControlDown() && e.getText().toLowerCase().equals("s"))
        {
            System.out.println("Hotkey Save");
            taggerVC.saveTags();
        }
    }
    
    @FXML
    private void openSettings(ActionEvent event)
    {
        try
        {
            Stage stage = new Stage();
            Parent loader = FXMLLoader.load(getClass().getResource("../application/SettingsView.fxml"));
            
            stage.setScene(new Scene(loader));
            stage.setTitle("Modal Window");
            stage.initModality(Modality.WINDOW_MODAL);
//            stage.initOwner(((MenuItem)event.getSource()).getScene().getWindow());
            stage.show();
            
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
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