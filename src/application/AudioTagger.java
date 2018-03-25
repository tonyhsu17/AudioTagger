package application;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


public class AudioTagger extends Application
{
   
    private Stage primaryStage;
    private BorderPane rootLayout;
    
    RootVC rootVC;
    AudioTaggerVC audioTaggerTaggerVC;

    @Override
    public void start(Stage primaryStage)
    {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("AudioTagger");

        initRootLayout();
        showMP3TaggerView();
    }

    /**
     * Initializes the root layout.
     */
    public void initRootLayout()
    {
        try
        {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("../application/RootView.fxml"));
            rootLayout = (BorderPane)loader.load();
            rootVC = loader.getController();
            
            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void showMP3TaggerView()
    {
        try
        {
            // Load overview.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("../application/AudioTaggerView.fxml"));
            AnchorPane view = (AnchorPane)loader.load();
            audioTaggerTaggerVC = loader.getController();
            rootVC.setAudioTaggerVC(audioTaggerTaggerVC);
            
            rootLayout.setCenter(view);

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        launch(args);
        // mp3MetaData asd = new mp3MetaData("asd");
    }
}
