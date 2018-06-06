package modules.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import modules.controllers.base.InformationBase;
import modules.controllers.base.TagBase;
import modules.vgmdb.VGMDBDetails;
import modules.vgmdb.VGMDBParser;
import modules.vgmdb.VGMDBParser.VGMDBParserCB;
import modules.vgmdb.VGMDBSearchDetails;
import support.Logger;
import support.structure.TagDetails;
import support.util.StringUtil;
import support.util.Utilities;
import support.util.Utilities.EditorTag;



/**
 * Class to handle the logic of the vgmdb view
 * 
 * @author Ikersaro
 *
 */
public class VGMDBController implements InformationBase, Logger {
    private VGMDBParser parser;
    private VGMDBDetails albumDetails;
    private VGMDBSearchDetails searchResults;
    private ListProperty<String> displayInfo;
    private ObjectProperty<Image> albumArtThumb;
    private Image albumArt500x500;
    private String query;
    private boolean isOnAlbumInfo;

    private HashMap<TagBase<?>, String> tagDataLookup; // mapping of retrieved info

    public enum AdditionalTag implements TagBase<AdditionalTag> {
        SERIES, IMAGE_URL, THEME,
        TRACK01, TRACK02, TRACK03, TRACK04, TRACK05,
        TRACK06, TRACK07, TRACK08, TRACK09, TRACK10,
        TRACK11, TRACK12, TRACK13, TRACK14, TRACK15,
        TRACK16, TRACK17, TRACK18, TRACK19, TRACK20;

        public static AdditionalTag getTrackTag(int i) {
            String str = "TRACK" + String.format("%02d", i);
            for(AdditionalTag t : AdditionalTag.values()) {
                if(str.equals(t.name())) {
                    return t;
                }
            }
            return null;
        }
    }

    public VGMDBController() {
        parser = new VGMDBParser();
        albumDetails = null;
        searchResults = null;
        displayInfo = new SimpleListProperty<String>();
        displayInfo.set(FXCollections.observableArrayList());
        albumArtThumb = new SimpleObjectProperty<Image>();
        albumArt500x500 = null;
        query = "";
        isOnAlbumInfo = false;
        tagDataLookup = new HashMap<TagBase<?>, String>();
        

        parser.registerCallback(new VGMDBParserCB() {
            @Override
            public void done(VGMDBDetails details) {
                debug("Album details retrieved: " + details);
                albumDetails = details;
                displayAlbum();
            }

            @Override
            public void done(VGMDBSearchDetails details) {
                debug("search results retrieved: " + details);
                searchResults = details;
                searchResults.getAlbumTitles().add(0, "Query: " + searchResults.getQuery());
                searchResults.getIDs().add(0, "");
                displaySearch();
            }
        });
    }

    /**
     * Reset view info
     */
    private void resetDisplay() {
        displayInfo.clear();
        albumArtThumb.set(null);
    }

    /**
     * Calls a search for album
     * @param query
     */
    public void search(String query) {
        this.query = query;
        parser.searchByAlbum(query);
    }

    /**
     * Displays search view
     */
    public void displaySearch() {
        resetDisplay();
        if(searchResults != null) {
            isOnAlbumInfo = false;
            displayInfo.setAll(searchResults.getAlbumTitles());
        }
    }

    /**
     * Displays album view
     */
    public void displayAlbum() {
        isOnAlbumInfo = true;
        // sanitize pom data to specific formatting and details
        albumDetails.setAlbumName(albumDetails.getAlbumName().split(" / ", 2)[0]);
        albumDetails.setReleaseDate(albumDetails.getReleaseDate().substring(0, 4));
        String notes = albumDetails.getAdditionNotes();
        String newNote = "";
        if(notes.toLowerCase().contains("opening")) {
            int end = notes.toLowerCase().indexOf("opening");
            int value = Utilities.findIntValueWithSuffix(notes.substring(end - 6, end));
            newNote = "OP" + (value != -1 ? value : "");
        }
        if(notes.toLowerCase().contains("ending")) {
            int end = notes.toLowerCase().indexOf("ending");
            int value = Utilities.findIntValueWithSuffix(notes.substring(end - 6, end));
            String foundTheme = "ED" + (value != -1 ? value : "");
            newNote = newNote.isEmpty() ? foundTheme : newNote + " + " + foundTheme;
        }
        if(notes.toLowerCase().contains("insert")) {
            int end = notes.toLowerCase().indexOf("insert");
            int value = Utilities.findIntValueWithSuffix(notes.substring(end - 6, end));
            String foundTheme = "IN" + (value != -1 ? value : "");
            newNote = newNote.isEmpty() ? foundTheme : newNote + " + " + foundTheme;
        }
        albumDetails.setAdditionNotes(newNote);
        
        // set info on view
        resetDisplay();
        displayInfo.add("Go Back");
        displayInfo.add("Series: " + albumDetails.getSeries());
        displayInfo.add("Album: " + albumDetails.getAlbumName());
        displayInfo.add("Artist(s): " + StringUtil.getCommaSeparatedStringWithAnd(albumDetails.getArtists()));
        displayInfo.add("Year: " + albumDetails.getReleaseDate());
        for(int i = 1; i <= albumDetails.getTracks().size(); i++) {
            displayInfo.add(String.format("%02d %s", i, albumDetails.getTracks().get(i - 1)));
        }
        displayInfo.add("Catalog: " + albumDetails.getCatalog());
        for(String str : albumDetails.getSites()) {
            displayInfo.add(str);
        }
    }

    /**
     * Interact with vgmdb parser state
     * 
     * @param index Case 01: If on search results, select an album (0 is noop) </br>
     *        Case 02: If on album info, selecting index copies content to clipboard (0 to return to
     *        search results)
     */
    public void selectResult(int index) {
        // first index is showing query info not actually a result
        debug(displayInfo.size());
        if(!isOnAlbumInfo && index > 0 && index < displayInfo.size()) {
            try {
                parser.retrieveAlbumByIndex(index);
            }
            catch (IOException e) {
                error("Failed: " + e);
            } // cb handles the rest
        }
        else if(isOnAlbumInfo && index == 0) // go back to search results
        {
            displaySearch();
        }
        else {
            Clipboard clpbrd = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(displayInfo.get(index));
            clpbrd.setContent(content);
        }
    }

    // ~~~~~~~~~~~~~~~~~ //
    // Getters & Setters //
    // ~~~~~~~~~~~~~~~~~ //

    public ListProperty<String> vgmdbInfoProperty() {
        return displayInfo;
    }

    @Override
    public String getDisplayKeywordTagClassName() {
        return "VGMDB";
    }

    @Override
    public String getDataForTag(TagBase<?> tag, String... values) {
        String returnValue = "";
        if(albumDetails == null) {
            return null;
        }
        if(tag == EditorTag.ALBUM) {
            returnValue = albumDetails.getAlbumName();
        }
        else if(tag == EditorTag.ALBUM_ARTIST) {
            // returnValue = tagDataLookup.get(VGMDBTag.SERIES);
        }
        else if(tag == EditorTag.ARTIST) {
            returnValue = StringUtil.getCommaSeparatedStringWithAnd(albumDetails.getArtists());
        }
        else if(tag == EditorTag.COMMENT) {
            // returnValue = tagDataLookup.get(AdditionalTag.THEME);
        }
        else if(tag == EditorTag.TRACK || tag == EditorTag.TITLE) {
            if(values.length == 0) {
                error("Please pass in int for which track title");
                return returnValue;
            }
            returnValue = albumDetails.getTracks().get(Integer.valueOf(values[0]) - 1);
        }
        else if(tag == EditorTag.YEAR) {
            returnValue = albumDetails.getReleaseDate();
        }
        else if(tag == AdditionalTag.SERIES) {
            returnValue = albumDetails.getSeries();
        }
        else if(tag == AdditionalTag.THEME) {
            returnValue = albumDetails.getAdditionNotes();
        }
        else if(tag == AdditionalTag.IMAGE_URL) {
            returnValue = albumDetails.getAlbumArtFull();
        }
        return returnValue;
    }

    @Override
    public void setDataForTag(TagBase<?> tag, String... values) {}

    @Override
    public void setAlbumArt(Object obj) {}

    @Override
    public void save(TagDetails details) {}

    @Override
    public List<String> getPossibleDataForTag(TagBase<?> tag, String values) {
        return null;
    }

    @Override
    public TagBase<?>[] getAdditionalTags() {
        return AdditionalTag.values();
    }

    @Override
    public List<TagBase<?>> getKeywordTags() {
        List<TagBase<?>> keywords = new ArrayList<>();
        keywords.add(EditorTag.ALBUM);
        keywords.add(EditorTag.ARTIST);
        keywords.add(EditorTag.YEAR);
        keywords.add(AdditionalTag.SERIES);
        keywords.add(AdditionalTag.THEME);
        // other values are not necessary for autofill tags
        return keywords;
    }

    @Override
    public Image getAlbumArt() {
        // TODO Auto-generated method stub
        return null;
    }

    public ObservableValue<? extends Image> albumArtProperty() {
        return albumArtThumb;
    }

    public void searchByAlbum(String dataForTag) {
        parser.searchByAlbum(dataForTag);
    }
}
