package application;

import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.input.KeyEvent;
import support.Utilities.Tag;

public class MenuVC
{
    private MP3TaggerVC taggerVC;
    
    
    @FXML
    CheckMenuItem propSaveArtist;
    
    public MenuVC()
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
    
    @FXML
    private void togglePropSaveArtistConfig()
    {
        Configuration.getInstance().togglePropagateSave(Tag.Artist);
    }
    
    @FXML
    private void togglePropSaveAlbumConfig()
    {
        Configuration.getInstance().togglePropagateSave(Tag.Album);
    }
    
    @FXML
    private void togglePropSaveAlbumArtistConfig()
    {
        Configuration.getInstance().togglePropagateSave(Tag.AlbumArt);
    }
    
    @FXML
    private void togglePropSaveYearConfig()
    {
        Configuration.getInstance().togglePropagateSave(Tag.Year);
    }
    
    @FXML
    private void togglePropSaveGenreConfig()
    {
        Configuration.getInstance().togglePropagateSave(Tag.Genre);
    }
    
    @FXML
    private void togglePropSaveCommentConfig()
    {
        Configuration.getInstance().togglePropagateSave(Tag.Comment);
    }
    
    @FXML
    private void togglePropSaveAlbumArtConfig()
    {
        Configuration.getInstance().togglePropagateSave(Tag.AlbumArt);
    }
}
