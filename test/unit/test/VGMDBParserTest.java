package unit.test;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javafx.application.Application;
import model.base.TagBase;
import modules.controllers.VGMDBParser;
import modules.controllers.VGMDBParser.VGMDBParserCB;
import support.util.Utilities.EditorTag;



/**
 * @author Ikersaro
 *
 */
public class VGMDBParserTest extends ModelInformationTestBase implements VGMDBParserCB {
    VGMDBParser parser;
    boolean isDataRetrieved;

    @BeforeClass
    public void beforeClass() throws Exception {
        Thread t = new Thread("JavaFX Init Thread") {
            @Override
            public void run() {
                Application.launch(NonApplication.class, new String[0]);
            }
        };
        t.setDaemon(true);
        t.start();
        parser = new VGMDBParser();
        parser.setCallback(this);
        isDataRetrieved = false;
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {}

    @BeforeMethod
    public void setUp() throws Exception {
        //        TagBase<?>[] addtional = parser.getAdditionalTags();
        //        for(TagBase<?> t : addtional)
        //        {
        //            System.out.println("Tag: " + t + " value: " + parser.getDataForTag(t, ""));
        //        }

    }

    @AfterMethod
    public void tearDown() throws Exception {}

    @Test
    public void searchAndSelectAlbumSenarioTest() throws RuntimeException {
        parser.searchByAlbum("only my railgun");
        waitForData();
        searchAlbumInfoTest();
        
        selectAlbumTest();
        parser.selectOption(0); // go back;
        
        searchAlbumInfoTest();
    }
    
    private void searchAlbumInfoTest() {
        List<String> searchedInfo = parser.vgmdbInfoProperty();
        assertEquals(searchedInfo.size(), 3);
        assertEquals(searchedInfo.get(0), "Query: only my railgun");
        assertEquals(searchedInfo.get(1), "only my railgun / fripSide [Limited Edition]");
        assertEquals(searchedInfo.get(2), "only my railgun / fripSide");
    }
    
    private void selectAlbumTest() {
        parser.selectOption(1);
        waitForData();
        
        List<String> albumInfo = parser.vgmdbInfoProperty();
        assertEquals(albumInfo.size(), 10);
        assertEquals(albumInfo.get(0), "Go Back");
        assertEquals(albumInfo.get(1), "Series: Toaru Kagaku no Railgun");
        assertEquals(albumInfo.get(2), "Album: only my railgun");
        assertEquals(albumInfo.get(3), "Artist(s): fripSide & a2c");
        assertEquals(albumInfo.get(4), "Year: 2009");
        assertEquals(albumInfo.get(5), "1 only my railgun");
        assertEquals(albumInfo.get(6), "2 late in autumn");
        assertEquals(albumInfo.get(7), "3 only my railgun -instrumental-");
        assertEquals(albumInfo.get(8), "4 late in autumn -instrumental-");
        assertEquals(albumInfo.get(9), "http://www.play-asia.com/paOS-13-71-9x-49-en-70-3iu7.html");
        
        assertEquals(parser.getDataForTag(EditorTag.ALBUM), "only my railgun");
        assertEquals(parser.getDataForTag(EditorTag.ALBUM_ART_META), "");
        assertEquals(parser.getDataForTag(EditorTag.ALBUM_ARTIST), "");
        assertEquals(parser.getDataForTag(EditorTag.ARTIST), "fripSide & a2c");
        assertEquals(parser.getDataForTag(EditorTag.COMMENT), "");
        assertEquals(parser.getDataForTag(EditorTag.GENRE), "");
        assertEquals(parser.getDataForTag(EditorTag.TITLE, "1"), "only my railgun");
        assertEquals(parser.getDataForTag(EditorTag.TRACK, "1"), "only my railgun");
        assertEquals(parser.getDataForTag(EditorTag.YEAR), "2009");
        assertEquals(parser.getDataForTag(VGMDBParser.AdditionalTag.THEME), "OP1");
        assertEquals(parser.getDataForTag(VGMDBParser.AdditionalTag.SERIES), "Toaru Kagaku no Railgun");
    }
    
    @Test
    public void testKeywords() {
        TagBase<?>[] tags = {
            VGMDBParser.AdditionalTag.SERIES,
            VGMDBParser.AdditionalTag.THEME,            
            EditorTag.ALBUM,
            EditorTag.ARTIST,
            EditorTag.YEAR};
    
        List<TagBase<?>> expected = Arrays.asList(tags);
        
        assertEquals(parser.getDisplayKeywordTagClassName(), "VGMDB");
        assertEquals(keywordsTest(parser.getKeywordTags(), expected), "");
    }

    @Override
    public void dataRetrievedForVGMDB() {
        isDataRetrieved = true;
    }

    /**
     * Keep sleeping until data is posted
     */
    private void waitForData() throws RuntimeException {
        int counter = 0;
        while(!isDataRetrieved) {
            try {
                Thread.sleep(100);
            }
            catch (InterruptedException e) {
            }
            finally {
                if(++counter == 50) {
                    throw new RuntimeException("Waited too long for data;");
                }
            }
        }
        isDataRetrieved = false;
    }
}
