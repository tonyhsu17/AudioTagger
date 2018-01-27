package application;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

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
import model.information.VGMDBParser;
import support.Logger;
import support.structure.Range;
import support.util.Utilities;
import support.util.Utilities.EditorTag;



public class MP3TaggerVC implements Logger {
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
    private Label autofillEnabledLabel;

    int pressedIndex; // used for mouse dragging range
    // HashMap<String, KeyAndName> idToName; // TextFieldId to SuggestorKey and DisplayName
    DataCompilationModel model;
    VGMDBParser vgmdbParserModel;
    private HashMap<ComboBox<String>, EditorTag> comboBoxToTag;

    public MP3TaggerVC() {
        model = new DataCompilationModel();
        vgmdbParserModel = new VGMDBParser();
        pressedIndex = 0;

        model.setVGMDBParser(vgmdbParserModel);
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
        addOnKeyReleasedListeners();

        songListLV.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        songListLV.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> showSelectedTag());

        initializeAlbumArtMenu();
    }

    private void bindProperties() {
        // binds
        songListLV.itemsProperty().bindBidirectional(model.processingFilesProperty());

        fileNameCB.itemsProperty().bindBidirectional(model.getPropertyForTag(EditorTag.FILE_NAME).getDropDownListProperty());
        fileNameCB.editorProperty().getValue().textProperty()
            .bindBidirectional(model.getPropertyForTag(EditorTag.FILE_NAME).getTextProperty());
        titleCB.itemsProperty().bindBidirectional(model.getPropertyForTag(EditorTag.TITLE).getDropDownListProperty());
        titleCB.editorProperty().getValue().textProperty().bindBidirectional(model.getPropertyForTag(EditorTag.TITLE).getTextProperty());
        artistCB.itemsProperty().bindBidirectional(model.getPropertyForTag(EditorTag.ARTIST).getDropDownListProperty());
        artistCB.editorProperty().getValue().textProperty().bindBidirectional(model.getPropertyForTag(EditorTag.ARTIST).getTextProperty());
        albumCB.itemsProperty().bindBidirectional(model.getPropertyForTag(EditorTag.ALBUM).getDropDownListProperty());
        albumCB.editorProperty().getValue().textProperty().bindBidirectional(model.getPropertyForTag(EditorTag.ALBUM).getTextProperty());
        albumArtistCB.itemsProperty().bindBidirectional(model.getPropertyForTag(EditorTag.ALBUM_ARTIST).getDropDownListProperty());
        albumArtistCB.editorProperty().getValue().textProperty()
            .bindBidirectional(model.getPropertyForTag(EditorTag.ALBUM_ARTIST).getTextProperty());
        trackCB.itemsProperty().bindBidirectional(model.getPropertyForTag(EditorTag.TRACK).getDropDownListProperty());
        trackCB.editorProperty().getValue().textProperty().bindBidirectional(model.getPropertyForTag(EditorTag.TRACK).getTextProperty());
        yearCB.itemsProperty().bindBidirectional(model.getPropertyForTag(EditorTag.YEAR).getDropDownListProperty());
        yearCB.editorProperty().getValue().textProperty().bindBidirectional(model.getPropertyForTag(EditorTag.YEAR).getTextProperty());
        genreCB.itemsProperty().bindBidirectional(model.getPropertyForTag(EditorTag.GENRE).getDropDownListProperty());
        genreCB.editorProperty().getValue().textProperty().bindBidirectional(model.getPropertyForTag(EditorTag.GENRE).getTextProperty());
        commentCB.itemsProperty().bindBidirectional(model.getPropertyForTag(EditorTag.COMMENT).getDropDownListProperty());
        commentCB.editorProperty().getValue().textProperty()
            .bindBidirectional(model.getPropertyForTag(EditorTag.COMMENT).getTextProperty());
        albumArtIV.imageProperty().bindBidirectional(model.albumArtProperty());
        albumArtMetaLabel.textProperty().bind(model.getPropertyForTag(EditorTag.ALBUM_ART_META).getTextProperty());

        vgmdbInfoLV.itemsProperty().bind(vgmdbParserModel.vgmdbInfoProperty());
        vgmdbAlbumArtIV.imageProperty().bind(vgmdbParserModel.albumArtProperty());
    }
    
    private void showSelectedTag() {
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

        vgmdbParserModel.searchByAlbum(model.getPropertyForTag(EditorTag.ALBUM).getTextProperty().get());
    }

    private void selectFirstIndex() {
        for(Entry<ComboBox<String>, EditorTag> entry : comboBoxToTag.entrySet()) {
            entry.getKey().getSelectionModel().select(0);
        }
    }

    public void saveTags() {
        model.save();
    }

    public void toggleAutoFill() {
        model.toggleAutoFill();
        setAutoFillLabel(model.isAutoFillEnabled() ? "Autofill Enabled" : "Autofill disabled");
    }

    private void setAutoFillLabel(String msg) {
        autofillEnabledLabel.setText(msg);
    }
    
    // ~~~~~~~~~~~~~~~~~~~ //
    //    Event Handlers   //
    // ~~~~~~~~~~~~~~~~~~~ //

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


    private void addOnMouseClickedListners() {
        // Show dropdown items when clicked on textfield
        for(Entry<ComboBox<String>, EditorTag> entry : comboBoxToTag.entrySet()) {
            ComboBox<String> temp = entry.getKey();
            TextField tf = temp.getEditor();
            tf.setOnMouseClicked((mouseEvent) -> {
                // highlighting is removed when showing dropdown, so need to rehighlight
                Range range = Utilities.getRange(tf.getText(), tf.getCaretPosition(), tf.getSelectedText());
                model.updateChoicesForTag(entry.getValue(), tf.getText(), (size) -> {
                    tf.selectRange(range.start(), range.end());
                    temp.show();
                });
            });
        }
    }

    private void addOnKeyReleasedListeners() {
        for(Entry<ComboBox<String>, EditorTag> entry : comboBoxToTag.entrySet()) {
            ComboBox<String> temp = entry.getKey();
            if(temp == artistCB || temp == albumArtistCB || temp == genreCB) {
                TextField tf = temp.getEditor();
                tf.textProperty().addListener((obs, oldVal, newVal) -> {
                    if(oldVal != null) {
                        model.updateChoicesForTag(entry.getValue(), newVal, (size) -> {
                            temp.hide();
                            temp.show();
                        });
                    } else {
                        // first time initialization
                        model.updateChoicesForTag(entry.getValue(), newVal, (size) -> {
                            //noop
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

        model.changeAlbumArtFromFile(f);
    }

    // ~~~~~ FXML vgmdbInfo ~~~~~ //
    @FXML
    private void vgmdbInfoLVOnMouseClicked(MouseEvent event) {
        if(event.getButton().equals(MouseButton.PRIMARY)) {
            if(event.getClickCount() >= 2) {
                vgmdbParserModel.selectOption(vgmdbInfoLV.getSelectionModel().getSelectedIndex());
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
