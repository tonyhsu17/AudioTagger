package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;



/**
 * Main Application to start AudioTagger.
 *
 * @author Tony Hsu
 */
public class AudioTagger extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;

    private RootVC rootVC;
    private AudioTaggerVC audioTaggerTaggerVC;


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("AudioTagger");

        initRootLayout();
        addAudioTaggerView();
    }

    /**
     * Initializes the root layout.
     */
    public void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getClassLoader().getResource("RootView.fxml"));
            rootLayout = (BorderPane)loader.load();
            rootVC = loader.getController();

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add in the main view to root view.
     */
    public void addAudioTaggerView() {
        try {
            // Load overview.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getClassLoader().getResource("AudioTaggerView.fxml"));
            AnchorPane view = (AnchorPane)loader.load();
            audioTaggerTaggerVC = loader.getController();
            rootVC.setAudioTaggerVC(audioTaggerTaggerVC);

            rootLayout.setCenter(view);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}
