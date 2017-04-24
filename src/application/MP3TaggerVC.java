package application;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONObject;

import com.sun.javafx.iio.ImageStorage.ImageType;

import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.text.Text;
import models.DataCompilationModel;
import models.DataCompilationModel.ImageFrom;
import models.dataSuggestors.AudioFiles;
import models.dataSuggestors.DatabaseController;
import models.dataSuggestors.VGMDBParser;
import support.Utilities;
import support.Utilities.Tag;


public class MP3TaggerVC
{
    @FXML
    private ListView<String> songListLV;
    @FXML
    private ComboBox<String> fileNameCB;
    @FXML
    private ComboBox<String> titleCB;
    @FXML
    private ComboBox<String> artistCB;
    @FXML
    private ComboBox<String> albumCB;
    @FXML
    private ComboBox<String> albumArtistCB;
    @FXML
    private ComboBox<String> trackCB;
    @FXML
    private ComboBox<String> yearCB;
    @FXML
    private ComboBox<String> genreCB;
    @FXML
    private ComboBox<String> commentCB;
    @FXML
    private ImageView albumArtIV;
    @FXML
    private Label albumArtMetaLabel;
    
    @FXML
    private Label suggestionCurrentLabel;
    @FXML
    private TextField suggestionCurrentTF;
    @FXML
    private Label suggestionPredictionLabel;
    @FXML
    private ComboBox<String> suggestionPredictionCB;
    @FXML
    private TextField searchAlbumTF;
    
    @FXML
    private ListView<String> vgmdbInfoLV;
    @FXML
    private ImageView vgmdbAlbumArtIV;
    
    int pressedIndex; // used for mouse dragging range
//    HashMap<String, KeyAndName> idToName; // TextFieldId to SuggestorKey and DisplayName
    DataCompilationModel model;
    VGMDBParser vgmdbParserModel;

    public MP3TaggerVC()
    {
        model = new DataCompilationModel();
        vgmdbParserModel = new VGMDBParser();
        pressedIndex = 0;
        
        model.setVGMDBParser(vgmdbParserModel);
    }
    
    @FXML
    private void initialize()
    {
        bindProperties();
        addListeners();
        
        songListLV.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        songListLV.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> showSelectedTag());      
  
        initializeAlbumArtMenu();
    }
    
    private void reset()
    {
//        fileNameCB.editableProperty().getValue().;
    }

    private void bindProperties()
    {
        // binds
        songListLV.itemsProperty().bindBidirectional(model.processingFilesProperty());

        fileNameCB.itemsProperty().bindBidirectional(model.fileNamesProperty());
        fileNameCB.editorProperty().getValue().textProperty().bindBidirectional(model.fileNameTextProperty());
        titleCB.itemsProperty().bindBidirectional(model.titlesProperty());
        titleCB.editorProperty().getValue().textProperty().bindBidirectional(model.titleTextProperty());
        artistCB.itemsProperty().bindBidirectional(model.artistsProperty());
        artistCB.editorProperty().getValue().textProperty().bindBidirectional(model.artistTextProperty());
        albumCB.itemsProperty().bindBidirectional(model.albumsProperty());
        albumCB.editorProperty().getValue().textProperty().bindBidirectional(model.albumTextProperty());
        albumArtistCB.itemsProperty().bindBidirectional(model.albumArtistsProperty());
        albumArtistCB.editorProperty().getValue().textProperty().bindBidirectional(model.albumArtistTextProperty());
        trackCB.itemsProperty().bindBidirectional(model.tracksProperty());
        trackCB.editorProperty().getValue().textProperty().bindBidirectional(model.trackTextProperty());
        yearCB.itemsProperty().bindBidirectional(model.yearsProperty());
        yearCB.editorProperty().getValue().textProperty().bindBidirectional(model.yearTextProperty());
        genreCB.itemsProperty().bindBidirectional(model.genresProperty());
        genreCB.editorProperty().getValue().textProperty().bindBidirectional(model.genreTextProperty());
        commentCB.itemsProperty().bindBidirectional(model.commentsProperty());
        commentCB.editorProperty().getValue().textProperty().bindBidirectional(model.commentTextProperty());
        
        albumArtIV.imageProperty().bindBidirectional(model.albumArtProperty());
        albumArtMetaLabel.textProperty().bind(model.albumArtMetaProperty());
        
        vgmdbInfoLV.itemsProperty().bind(vgmdbParserModel.vgmdbInfoProperty());
        vgmdbAlbumArtIV.imageProperty().bind(vgmdbParserModel.albumArtProperty());
        
//        suggestionCurrentLabel.textProperty().bind(currentHeader);
//        suggestionCurrentTF.textProperty().bind(sug.currentValueProperty());
//        suggestionPredictionLabel.textProperty().bind(suggestedHeader);
//        suggestionPredictionCB.itemsProperty().bind(sug.suggestedValuesProperty());
    }
    
    
    
    private void addListeners()
    {
        // MouseClicked
        fileNameCB.getEditor().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @SuppressWarnings("unchecked")
            @Override
            public void handle(MouseEvent event)
            {
                TextField cb = (TextField)event.getSource();
                int caretPos = cb.getCaretPosition();
                model.updateChoicesForTag(Tag.FileName, (size) -> {
                    ((ComboBox<String>)cb.getParent()).show();
                    cb.positionCaret(caretPos);
                });
            }
        });
        titleCB.getEditor().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @SuppressWarnings("unchecked")
            @Override
            public void handle(MouseEvent event)
            {
                TextField cb = (TextField)event.getSource();
                int caretPos = cb.getCaretPosition();
                model.updateChoicesForTag(Tag.Title, (size) -> {
                    ((ComboBox<String>)cb.getParent()).show();
                    cb.positionCaret(caretPos);
                });
            }
        });
        artistCB.getEditor().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @SuppressWarnings("unchecked")
            @Override
            public void handle(MouseEvent event)
            {
                TextField cb = (TextField)event.getSource();
                int caretPos = cb.getCaretPosition();
                model.updateChoicesForTag(Tag.Artist, (size) -> {
                    cb.positionCaret(caretPos);
                    ((ComboBox<String>)cb.getParent()).show();
                });
            }
        });
        albumCB.getEditor().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @SuppressWarnings("unchecked")
            @Override
            public void handle(MouseEvent event)
            {
                TextField cb = (TextField)event.getSource();
                int caretPos = cb.getCaretPosition();
                model.updateChoicesForTag(Tag.Album, (size) -> {
                    cb.positionCaret(caretPos);
                    ((ComboBox<String>)cb.getParent()).show();
                }); 
            }
        });
        albumArtistCB.getEditor().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @SuppressWarnings("unchecked")
            @Override
            public void handle(MouseEvent event)
            {
                TextField cb = (TextField)event.getSource();
                int caretPos = cb.getCaretPosition();
                model.updateChoicesForTag(Tag.AlbumArtist, (size) -> {
                    cb.positionCaret(caretPos);
                    ((ComboBox<String>)cb.getParent()).show();
                });
            }
        });
        trackCB.getEditor().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @SuppressWarnings("unchecked")
            @Override
            public void handle(MouseEvent event)
            {
                TextField cb = (TextField)event.getSource();
                int caretPos = cb.getCaretPosition();
                model.updateChoicesForTag(Tag.Track, (size) -> {
                    cb.positionCaret(caretPos);
                    ((ComboBox<String>)cb.getParent()).show();
                });
            }
        });
        yearCB.getEditor().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @SuppressWarnings("unchecked")
            @Override
            public void handle(MouseEvent event)
            {
                TextField cb = (TextField)event.getSource();
                int caretPos = cb.getCaretPosition();
                model.updateChoicesForTag(Tag.Year, (year) -> {
                    cb.positionCaret(caretPos);
                    ((ComboBox<String>)cb.getParent()).show();
                });
            }
        });
        genreCB.getEditor().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @SuppressWarnings("unchecked")
            @Override
            public void handle(MouseEvent event)
            {
                TextField cb = (TextField)event.getSource();
                int caretPos = cb.getCaretPosition();
                model.updateChoicesForTag(Tag.Genre, (size) -> {
                    cb.positionCaret(caretPos);
                    ((ComboBox<String>)cb.getParent()).show();
                });
            }
        });
        commentCB.getEditor().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @SuppressWarnings("unchecked")
            @Override
            public void handle(MouseEvent event)
            {
                TextField cb = (TextField)event.getSource();
                int caretPos = cb.getCaretPosition();
                model.updateChoicesForTag(Tag.Comment, (size) -> {
                    cb.positionCaret(caretPos);
                    ((ComboBox<String>)cb.getParent()).show();
                });
            }
        });
        
        // KeyPressed
        artistCB.getEditor().setOnKeyPressed(new EventHandler<KeyEvent>() {
            @SuppressWarnings("unchecked")
            @Override
            public void handle(KeyEvent event)
            {
                TextField tf = (TextField)event.getSource();
                ComboBox<String> cb = ((ComboBox<String>)tf.getParent());
                model.updateChoicesForTag(Tag.Artist, (size) -> {
                    cb.hide();
                    cb.show();
                });
            }
        });
        albumArtistCB.getEditor().setOnKeyPressed(new EventHandler<KeyEvent>() {
            @SuppressWarnings("unchecked")
            @Override
            public void handle(KeyEvent event)
            {
                TextField tf = (TextField)event.getSource();
                ComboBox<String> cb = ((ComboBox<String>)tf.getParent());
                model.updateChoicesForTag(Tag.AlbumArtist, (size) -> {
                    cb.hide();
                    cb.show();
                });
            }
        });
        genreCB.getEditor().setOnKeyPressed(new EventHandler<KeyEvent>() {
            @SuppressWarnings("unchecked")
            @Override
            public void handle(KeyEvent event)
            {
                TextField tf = (TextField)event.getSource();
                ComboBox<String> cb = ((ComboBox<String>)tf.getParent());
                model.updateChoicesForTag(Tag.Genre, (size) -> {
                    cb.hide();
                    cb.show();
                });
            }
        });
        
        // normal cases, choices don't update unless it had vgmdb info changed
//        titleCB.getEditor().setOnKeyPressed(new EventHandler<KeyEvent>() {
//            @Override
//            public void handle(KeyEvent event)
//            {
//                model.updateChoicesForTag(Tag.Title, tf.getText() + event.getText(), (size) -> {});
//            }
//        });
//        albumCB.getEditor().setOnKeyPressed(new EventHandler<KeyEvent>() {
//            @Override
//            public void handle(KeyEvent event)
//            {
//                TextField tf = (TextField)event.getSource();
////                ComboBox<String> cb = ((ComboBox<String>)tf.getParent());
//                model.inputTextChanged(Tag.Album, tf.getText() + event.getText(), (size) -> {});
//            }
//        });
//        trackCB.getEditor().setOnKeyPressed(new EventHandler<KeyEvent>() {
//            @Override
//            public void handle(KeyEvent event)
//            {
//                TextField tf = (TextField)event.getSource();
////                ComboBox<String> cb = ((ComboBox<String>)tf.getParent());
//                model.inputTextChanged(Tag.Track, tf.getText() + event.getText(), (size) -> {});
//            }
//        });
//        yearCB.getEditor().setOnKeyPressed(new EventHandler<KeyEvent>() {
//            @Override
//            public void handle(KeyEvent event)
//            {
//                TextField tf = (TextField)event.getSource();
////                ComboBox<String> cb = ((ComboBox<String>)tf.getParent());
//                model.inputTextChanged(Tag.Year, tf.getText() + event.getText(), (size) -> {});
//            }
//        });
   }

    private void initializeAlbumArtMenu()
    {
        final ContextMenu contextMenu = new ContextMenu();

        MenuItem browse = new MenuItem("Browse");
        browse.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                System.out.println("To be Implemented");
            }
        });

        MenuItem copy = new MenuItem("Copy");
        copy.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                System.out.println("To be Implemented");
            }
        });

        MenuItem paste = new MenuItem("Paste");
        paste.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                System.out.println("To be Implemented");
            }
        });

        MenuItem remove = new MenuItem("Remove");
        remove.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                System.out.println("To be Implemented");
            }
        });

        MenuItem export = new MenuItem("Export");
        export.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                System.out.println("To be Implemented");
            }
        });

        MenuItem replace = new MenuItem("Replace from VGDB");
        replace.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                model.setImage(ImageFrom.VGMDB, "null");
//                albumArtIV.setImage(vgmdbParserModel.getAlbumArt());
            }
        });

        contextMenu.getItems().addAll(browse, copy, paste, remove, export, replace);

        albumArtIV.setOnMousePressed(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent event)
            {
                if(event.isSecondaryButtonDown())
                {
                    contextMenu.show(albumArtIV, event.getScreenX(), event.getScreenY());
                }
            }
        });
    }

    private void showSelectedTag()
    {
        List<Integer> indices = songListLV.getSelectionModel().getSelectedIndices();
        if(indices.size() > 1) // if multiple selected
        {
            model.requestDataFor(indices, (asd) -> {
                selectFirstIndex();
            });
        }
        else // else only 1 selected
        {
            model.requestDataFor(songListLV.getSelectionModel().getSelectedIndex(), (asd) -> {
                selectFirstIndex();
            });
        }
        
        vgmdbParserModel.searchByAlbum(model.getAlbums().get(0));
    }
    
//    private void inputTextChanged()
//    {
//      //set input fields in model for suggestions
//        model.inputTextChanged(Tag.FileName, fileNameCB.getEditor().getText(), (a) -> {});
//        model.inputTextChanged(Tag.Title, titleCB.getEditor().getText(), (a) -> {});
//        model.inputTextChanged(Tag.Artist, artistCB.getEditor().getText(), (a) -> {});
//        model.inputTextChanged(Tag.Album, albumCB.getEditor().getText(), (a) -> {});
//        model.inputTextChanged(Tag.AlbumArtist, albumArtistCB.getEditor().getText(), (a) -> {});
//        model.inputTextChanged(Tag.Track, trackCB.getEditor().getText(), (a) -> {});
//        model.inputTextChanged(Tag.Year, yearCB.getEditor().getText(), (a) -> {});
//        model.inputTextChanged(Tag.Genre, genreCB.getEditor().getText(), (a) -> {});
//        model.inputTextChanged(Tag.Comment, commentCB.getEditor().getText(), (a) -> {});
//    }
    
    private void selectFirstIndex()
    {
        fileNameCB.getSelectionModel().select(0);
        titleCB.getSelectionModel().select(0);
        artistCB.getSelectionModel().select(0);
        albumCB.getSelectionModel().select(0);
        albumArtistCB.getSelectionModel().select(0);
        trackCB.getSelectionModel().select(0);
        yearCB.getSelectionModel().select(0);
        genreCB.getSelectionModel().select(0);
        commentCB.getSelectionModel().select(0);
    }
    
    public void saveTags()
    {
//        inputTextChanged();
        model.save();
    }

    // ~~~~~~~~~~~~~~~~~~~ // 
    // FXML Event Handlers //
    // ~~~~~~~~~~~~~~~~~~~ //

    // ~~~~~ songListLV ~~~~~ //
    @FXML
    private void songListLVOnMousePressed(MouseEvent event)
    {
        try
        {
            Node textDraggedOver = event.getPickResult().getIntersectedNode(); // get item dragged on
            int indexDraggedOver = model.getSongList().indexOf(((Text)textDraggedOver).getText()); // get index of text of item

            pressedIndex = indexDraggedOver; // keep track of mouse drag start
        }
        catch (ClassCastException | NullPointerException | IndexOutOfBoundsException e) // catch invalid item casting
        {
            pressedIndex = model.getFileNames().size();
        }
    }
    
    @FXML
    private void songListLVOnMouseDragged(MouseEvent event)
    {
        try
        {
            Node textDraggedOver = event.getPickResult().getIntersectedNode(); // get item dragged on
            int indexDraggedOver = model.getSongList().indexOf(((Text)textDraggedOver).getText()); // get index of text of item
            songListLV.getSelectionModel().clearSelection(); // reset current selection as selectRange() appends
            // if dragged index is less than start index, reverse them
            if(indexDraggedOver < pressedIndex)
            {
                songListLV.getSelectionModel().selectRange(indexDraggedOver, pressedIndex + 1);
            }
            else
            {
                songListLV.getSelectionModel().selectRange(pressedIndex, indexDraggedOver + 1);
            }
        }
        catch (ClassCastException | NullPointerException | IndexOutOfBoundsException e) // catch invalid item casting
        {

        }
    }
    
    @FXML
    private void songListLVDragOver(DragEvent de) {
      Dragboard board = de.getDragboard();
      if (board.hasFiles()) {
        de.acceptTransferModes(TransferMode.ANY);
      }
    }
    
    @FXML
    private void songListLVDropped(DragEvent de) {
        Dragboard board = de.getDragboard();
        List<File> phil = board.getFiles();
        // lol don't need threading for toArray, hangs if i include board.getFiles()
        Service<File[]> serv = new Service<File[]>()
        {
            @Override
            protected Task<File[]> createTask() {
                return new Task<File[]>() {
                    @Override
                    protected File[] call() throws Exception {
                        return phil.toArray(new File[0]);
                    }
                };
            }
        };
        serv.setOnSucceeded((status) -> {
            model.appendWorkingDirectory(serv.getValue());
        });
        serv.start();
    }
    
    // ~~~~~ FXML albumArtIV ~~~~~ //
    @FXML
    private void albumArtIVOnDragOver(DragEvent de) {
      Dragboard board = de.getDragboard();
      if (board.hasFiles()) {
        de.acceptTransferModes(TransferMode.ANY);
        System.out.println("albumArtIVOnDragOver");
      }
    }
    
    @FXML
    private void albumArtIVOnDropped(DragEvent de) {
        Dragboard board = de.getDragboard();
        List<File> phil = board.getFiles();
        File f = phil.get(0);
        
        model.changeAlbumArtFromFile(f);
    }
    

    // ~~~~~ FXML vgmdbInfo ~~~~~ //
    @FXML
    private void vgmdbInfoLVOnMouseClicked(MouseEvent event)
    {
        if(event.getButton().equals(MouseButton.PRIMARY)){
            if(event.getClickCount() >= 2){
                vgmdbParserModel.selectAlbum(vgmdbInfoLV.getSelectionModel().getSelectedIndex());
                System.out.println("Double clicked");
            }
        }
    }
    
    // ~~~~~ FXML albumSearchTF ~~~~~ //
    @FXML
    private void searchAlbumTFOnEnterPressed(ActionEvent event)
    {
        System.out.println("Manual Search: " + searchAlbumTF.getText());
        vgmdbParserModel.searchByAlbum(searchAlbumTF.getText());
    }
    
    // ~~~~~ FXML clearListButton ~~~~~ //
    @FXML
    private void clearListOnAction(ActionEvent event)
    {
        System.out.println("List Cleared");
        model.reset();
    }
}