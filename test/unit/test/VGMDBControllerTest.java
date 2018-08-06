package unit.test;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.ikersaro.utilities.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javafx.application.Application;
import modules.controllers.VGMDBController;
import modules.controllers.base.TagBase;
import support.util.Utilities.EditorTag;



/**
 * @author Ikersaro
 *
 */
public class VGMDBControllerTest extends ModelInformationTestBase implements Logger {//implements VGMDBParserCB {
    private VGMDBController parser;
    private List<String> viewInfo;

    @BeforeClass
    public void beforeClass() {
        Thread t = new Thread("JavaFX Init Thread") {
            @Override
            public void run() {
                Application.launch(NonApplication.class, new String[0]);
            }
        };
        t.setDaemon(true);
        t.start();
    }

    @AfterClass
    public static void afterClass() {}

    @BeforeMethod
    public void beforeMethod() {
        parser = new VGMDBController();
        viewInfo = parser.vgmdbInfoProperty();
    }

    @AfterMethod
    public void afterMethod() {}

    @Test
    public void testSearchList() {
        parser.searchByAlbum("only my railgun");
        waitForData();
        assertSearchView();
    }

    @Test
    public void searchAndSelectAlbumSenarioTest() throws RuntimeException {
        parser.searchByAlbum("only my railgun");
        waitForData();
        assertSearchView();

        parser.selectResult(1);
        waitForData();
        assertAlbumView();

        parser.selectResult(0); // go back;
        assertSearchView(); // no need to wait, should be cached and instant
    }

    private void assertSearchView() {
        assertEquals(viewInfo.size(), 3);
        assertEquals(viewInfo.get(0), "Query: only my railgun");
        assertEquals(viewInfo.get(1), "only my railgun / fripSide [Limited Edition]");
        assertEquals(viewInfo.get(2), "only my railgun / fripSide");
    }

    private void assertAlbumView() {
        viewInfo = parser.vgmdbInfoProperty();
        assertEquals(viewInfo.size(), 13);
        assertEquals(viewInfo.get(0), "Go Back");
        assertEquals(viewInfo.get(1), "Series: Toaru Kagaku no Railgun");
        assertEquals(viewInfo.get(2), "Album: only my railgun");
        assertEquals(viewInfo.get(3), "Artist(s): fripSide & a2c");
        assertEquals(viewInfo.get(4), "Year: 2009");
        assertEquals(viewInfo.get(5), "01 only my railgun");
        assertEquals(viewInfo.get(6), "02 late in autumn");
        assertEquals(viewInfo.get(7), "03 only my railgun -instrumental-");
        assertEquals(viewInfo.get(8), "04 late in autumn -instrumental-");
        assertEquals(viewInfo.get(9), "Catalog: GNCA-0151");
        assertEquals(viewInfo.get(10), "https://vgmdb.net/album/23188?perpage=99999");
        assertEquals(viewInfo.get(11), "http://www.cdjapan.co.jp/detailview.html?KEY=GNCA-151");
        assertEquals(viewInfo.get(12), "http://www.play-asia.com/paOS-13-71-9x-49-en-70-3iu7.html");

        assertEquals(parser.getDataForTag(EditorTag.ALBUM), "only my railgun");
        assertEquals(parser.getDataForTag(EditorTag.ALBUM_ART_META), "");
        assertEquals(parser.getDataForTag(EditorTag.ALBUM_ARTIST), "");
        assertEquals(parser.getDataForTag(EditorTag.ARTIST), "fripSide & a2c");
        assertEquals(parser.getDataForTag(EditorTag.COMMENT), "");
        assertEquals(parser.getDataForTag(EditorTag.GENRE), "");
        assertEquals(parser.getDataForTag(EditorTag.TITLE, "1"), "only my railgun");
        assertEquals(parser.getDataForTag(EditorTag.TRACK, "1"), "only my railgun");
        assertEquals(parser.getDataForTag(EditorTag.YEAR), "2009");
        assertEquals(parser.getDataForTag(VGMDBController.AdditionalTag.THEME), "OP1");
        assertEquals(parser.getDataForTag(VGMDBController.AdditionalTag.SERIES), "Toaru Kagaku no Railgun");
    }

    @Test
    public void testKeywords() {
        TagBase<?>[] tags = {
            VGMDBController.AdditionalTag.SERIES,
            VGMDBController.AdditionalTag.THEME,
            EditorTag.ALBUM,
            EditorTag.ARTIST,
            EditorTag.YEAR};

        List<TagBase<?>> expected = Arrays.asList(tags);

        assertEquals(parser.getDisplayKeywordTagClassName(), "VGMDB");
        assertEquals(keywordsTest(parser.getKeywordTags(), expected), "");
    }

    /**
     * Keep sleeping until data is posted
     * 
     * @throws InterruptedException
     */
    private void waitForData() throws RuntimeException {
        int timer = 20;
        viewInfo.clear();
        while(timer-- > 0) {
            if(!viewInfo.isEmpty()) {
                info("data retrieved");
                return;
            }
            else {
                try {
                    Thread.sleep(500);
                }
                catch (InterruptedException e) {
                }
            }
        }
    }
}
