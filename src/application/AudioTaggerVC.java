package application;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
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
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.text.Text;
import model.DataCompilationModel;
import model.DataCompilationModel.ImageFrom;
import modules.controllers.DatabaseController;
import modules.controllers.EditorDataController;
import modules.controllers.VGMDBController;
import support.EventCenter;
import support.EventCenter.Events;
import support.Logger;
import support.Scheduler;
import support.util.Utilities.EditorTag;



/**
 * ViewController for main view. Its job is to handle bindings to view and event listeners.
 * 
 * @author Ikersaro
 *
 */
public class AudioTaggerVC implements Logger {
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

    @FXML
    private Label infoLabel;

    int pressedIndex; // used for mouse dragging range
    // HashMap<String, KeyAndName> idToName; // TextFieldId to SuggestorKey and DisplayName
    private DataCompilationModel model;
    private DatabaseController dbManagement; // database for prediction of common tag fields
    private EditorDataController editorFields; // ComboBox controller (editor text and drop down)
    private VGMDBController vgmdbParserModel;
    private HashMap<ComboBox<String>, EditorTag> comboBoxToTag;

    /**
     * Initializes view controller including the other back-end components.
     * @throws SQLException 
     */
    public AudioTaggerVC() throws SQLException {
        vgmdbParserModel = new VGMDBController();
        dbManagement = new DatabaseController("");
        editorFields = new EditorDataController(dbManagement);
        model = new DataCompilationModel(dbManagement, editorFields);

        pressedIndex = 0;
        model.setVGMDBController(vgmdbParserModel);
    }

    @FXML
    private void initialize() {
        comboBoxToTag = new HashMap<ComboBox<String>, EditorTag>();

        comboBoxToTag.put(fileNameCB, EditorTag.FILE_NAME);
        comboBoxToTag.put(titleCB, EditorTag.TITLE);
        comboBoxToTag.put(artistCB, EditorTag.ARTIST);
        comboBoxToTag.put(albumCB, EditorTag.ALBUM);
        comboBoxToTag.put(albumArtistCB, EditorTag.ALBUM_ARTIST);
        comboBoxToTag.put(trackCB, EditorTag.TRACK);
        comboBoxToTag.put(yearCB, EditorTag.YEAR);
        comboBoxToTag.put(genreCB, EditorTag.GENRE);
        comboBoxToTag.put(commentCB, EditorTag.COMMENT);

        bindProperties();
        addOnMouseClickedListners();
        addOnChangeListeners();

        songListLV.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        initializeAlbumArtMenu();
    }

    /**
     * Property binding
     */
    private void bindProperties() {
        // binds
        songListLV.itemsProperty().bindBidirectional(model.processingFilesProperty());

        fileNameCB.itemsProperty().bindBidirectional(editorFields.getMeta(EditorTag.FILE_NAME).getDropDownListProperty());
        fileNameCB.editorProperty().getValue().textProperty()
            .bindBidirectional(editorFields.getMeta(EditorTag.FILE_NAME).getTextProperty());
        titleCB.itemsProperty().bindBidirectional(editorFields.getMeta(EditorTag.TITLE).getDropDownListProperty());
        titleCB.editorProperty().getValue().textProperty().bindBidirectional(editorFields.getMeta(EditorTag.TITLE).getTextProperty());
        artistCB.itemsProperty().bindBidirectional(editorFields.getMeta(EditorTag.ARTIST).getDropDownListProperty());
        artistCB.editorProperty().getValue().textProperty().bindBidirectional(editorFields.getMeta(EditorTag.ARTIST).getTextProperty());
        albumCB.itemsProperty().bindBidirectional(editorFields.getMeta(EditorTag.ALBUM).getDropDownListProperty());
        albumCB.editorProperty().getValue().textProperty().bindBidirectional(editorFields.getMeta(EditorTag.ALBUM).getTextProperty());
        albumArtistCB.itemsProperty().bindBidirectional(editorFields.getMeta(EditorTag.ALBUM_ARTIST).getDropDownListProperty());
        albumArtistCB.editorProperty().getValue().textProperty()
            .bindBidirectional(editorFields.getMeta(EditorTag.ALBUM_ARTIST).getTextProperty());
        trackCB.itemsProperty().bindBidirectional(editorFields.getMeta(EditorTag.TRACK).getDropDownListProperty());
        trackCB.editorProperty().getValue().textProperty().bindBidirectional(editorFields.getMeta(EditorTag.TRACK).getTextProperty());
        yearCB.itemsProperty().bindBidirectional(editorFields.getMeta(EditorTag.YEAR).getDropDownListProperty());
        yearCB.editorProperty().getValue().textProperty().bindBidirectional(editorFields.getMeta(EditorTag.YEAR).getTextProperty());
        genreCB.itemsProperty().bindBidirectional(editorFields.getMeta(EditorTag.GENRE).getDropDownListProperty());
        genreCB.editorProperty().getValue().textProperty().bindBidirectional(editorFields.getMeta(EditorTag.GENRE).getTextProperty());
        commentCB.itemsProperty().bindBidirectional(editorFields.getMeta(EditorTag.COMMENT).getDropDownListProperty());
        commentCB.editorProperty().getValue().textProperty()
            .bindBidirectional(editorFields.getMeta(EditorTag.COMMENT).getTextProperty());
        albumArtIV.imageProperty().bindBidirectional(editorFields.getAlbumArtProperty());
        albumArtMetaLabel.textProperty().bind(editorFields.getMeta(EditorTag.ALBUM_ART_META).getTextProperty());

        vgmdbInfoLV.itemsProperty().bind(vgmdbParserModel.vgmdbInfoProperty());
        vgmdbAlbumArtIV.imageProperty().bind(vgmdbParserModel.albumArtProperty());
    }

    /**
     * Triggers a cascading effect for saving.
     */
    public void saveTags() {
        model.save();
        infoLabel.setText("Tags Saved");
        new Scheduler(3, () -> {
            Platform.runLater(() -> {
                infoLabel.setText("");
            });
        }).runNTimes(1);
    }

    /**
     * Trigger an auto-fill to replace editor values with pre-defined rules. Notification lasts for
     * 5 seconds.
     */
    public void triggerAutoFill() {
        EventCenter.getInstance().postEvent(Events.TRIGGER_AUTO_FILL, null);

        infoLabel.setText("Autofill Triggered");
        new Scheduler(3, () -> {
            Platform.runLater(() -> {
                infoLabel.setText("");
            });
        }).runNTimes(1);
    }

    // ~~~~~~~~~~~~~~~~~~~ //
    //    Event Handlers   //
    // ~~~~~~~~~~~~~~~~~~~ //

    /**
     * Event listners for album art's right click menu
     */
    private void initializeAlbumArtMenu() {
        final ContextMenu contextMenu = new ContextMenu();

        MenuItem browse = new MenuItem("Browse");
        browse.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                error("To be Implemented");
            }
        });

        MenuItem copy = new MenuItem("Copy");
        copy.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                error("To be Implemented");
            }
        });

        MenuItem paste = new MenuItem("Paste");
        paste.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                error("To be Implemented");
            }
        });

        MenuItem remove = new MenuItem("Remove");
        remove.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                error("To be Implemented");
            }
        });

        MenuItem export = new MenuItem("Export");
        export.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                error("To be Implemented");
            }
        });

        MenuItem replace = new MenuItem("Replace from VGDB");
        replace.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                model.setImage(ImageFrom.VGMDB, "null");
                // albumArtIV.setImage(vgmdbParserModel.getAlbumArt());
            }
        });

        contextMenu.getItems().addAll(browse, copy, paste, remove, export, replace);

        albumArtIV.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(event.isSecondaryButtonDown()) {
                    contextMenu.show(albumArtIV, event.getScreenX(), event.getScreenY());
                }
            }
        });
    }

    /**
     * Event listeners mouse click actions for song list view and editor's combo box
     */
    private void addOnMouseClickedListners() {
        // SongList selecting files - display in editor
        songListLV.getSelectionModel().selectedItemProperty().addListener((mouseEvent) -> {
            List<Integer> indices = songListLV.getSelectionModel().getSelectedIndices();
            model.requestDataFor(indices, (size) -> {
            });
            // trigger a search on vgmdb
            vgmdbParserModel.searchByAlbum(editorFields.getDataForTag(EditorTag.ALBUM));
        });

        // ComboBox dropdown - Show dropdown items when clicked on textfield
//        for(Entry<ComboBox<String>, EditorTag> entry : comboBoxToTag.entrySet()) {
//            ComboBox<String> temp = entry.getKey();
//            TextField tf = temp.getEditor();
//
//            tf.setOnMouseClicked((mouseEvent) -> {
//                // highlighting is removed when showing dropdown, so need to rehighlight
//                Range range = Utilities.getRange(tf.getText(), tf.getCaretPosition(), tf.getSelectedText());
//                model.requestDropdownForTag(entry.getValue(), tf.getText(), temp.getItems(), (size) -> {
//                    tf.selectRange(range.start(), range.end());
//                    temp.show();
//                });
//            });
//        }
    }

    /**
     * Event listeners on change actions for typing in editor's combo box
     */
    @SuppressWarnings("unchecked")
    private void addOnChangeListeners() {
        for(Entry<ComboBox<String>, EditorTag> entry : comboBoxToTag.entrySet()) {
            ComboBox<String> temp = entry.getKey();
            if((temp == artistCB || temp == albumArtistCB || temp == genreCB)) {
                TextField tf = temp.getEditor();
                tf.textProperty().addListener((obs, oldVal, newVal) -> {
                    // refresh dropdown if there is value within textbox
                    if(oldVal != null && !oldVal.isEmpty() && temp.isFocused()) {
                        model.requestDropdownForTag(entry.getValue(), newVal, temp.getItems(), (size) -> {
                            temp.hide();
                            temp.setItems((ObservableList<String>)size);
                            temp.visibleRowCountProperty().set(((ObservableList<String>)size).size());
                            temp.show();
                        });
                    }
                });
            }
        }
    }

    // ~~~~~~~~~~~~~~~~~~~ //
    // FXML Event Handlers //
    // ~~~~~~~~~~~~~~~~~~~ //

    // ~~~~~ songListLV ~~~~~ //
    @FXML
    private void songListLVOnMousePressed(MouseEvent event) {
        try {
            Node textDraggedOver = event.getPickResult().getIntersectedNode(); // get item dragged on
            int indexDraggedOver = model.getSongList().indexOf(((Text)textDraggedOver).getText()); // get index of text of item

            pressedIndex = indexDraggedOver; // keep track of mouse drag start
        }
        catch (ClassCastException | NullPointerException | IndexOutOfBoundsException e) // catch invalid item casting
        {
            pressedIndex = model.getSongList().size();
        }
    }

    @FXML
    private void songListLVOnMouseDragged(MouseEvent event) {
        try {
            Node textDraggedOver = event.getPickResult().getIntersectedNode(); // get item dragged on
            int indexDraggedOver = model.getSongList().indexOf(((Text)textDraggedOver).getText()); // get index of text of item
            songListLV.getSelectionModel().clearSelection(); // reset current selection as selectRange() appends
            // if dragged index is less than start index, reverse them
            if(indexDraggedOver < pressedIndex) {
                songListLV.getSelectionModel().selectRange(indexDraggedOver, pressedIndex + 1);
            }
            else {
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
        if(board.hasFiles()) {
            de.acceptTransferModes(TransferMode.ANY);
        }
    }

    @FXML
    private void songListLVDropped(DragEvent de) {
        Dragboard board = de.getDragboard();
        List<File> phil = board.getFiles();
        // lol don't need threading for toArray, hangs if i include board.getFiles()
        Service<File[]> serv = new Service<File[]>() {
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
        if(board.hasFiles()) {
            de.acceptTransferModes(TransferMode.ANY);
            debug("albumArtIVOnDragOver");
        }
    }

    @FXML
    private void albumArtIVOnDropped(DragEvent de) {
        Dragboard board = de.getDragboard();
        List<File> phil = board.getFiles();
        File f = phil.get(0);

        model.setAlbumArt(f);
    }

    // ~~~~~ FXML vgmdbInfo ~~~~~ //
    @FXML
    private void vgmdbInfoLVOnMouseClicked(MouseEvent event) {
        if(event.getButton().equals(MouseButton.PRIMARY)) {
            if(event.getClickCount() >= 2) {
                vgmdbParserModel.selectResult(vgmdbInfoLV.getSelectionModel().getSelectedIndex());
                debug("Double clicked");
            }
        }
    }

    // ~~~~~ FXML albumSearchTF ~~~~~ //
    @FXML
    private void searchAlbumTFOnEnterPressed(ActionEvent event) {
        info("Manual Search: " + searchAlbumTF.getText());
        vgmdbParserModel.searchByAlbum(searchAlbumTF.getText());
    }

    // ~~~~~ FXML clearListButton ~~~~~ //
    @FXML
    private void clearListOnAction(ActionEvent event) {
        info("List Cleared");
        model.reset();
    }
}
