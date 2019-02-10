package unit.test;

import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.tonyhsu17.utilities.Logger;

import modules.vgmdb.VGMDBDetails;
import modules.vgmdb.VGMDBParser;
import modules.vgmdb.VGMDBParser.VGMDBParserCB;
import modules.vgmdb.VGMDBSearchDetails;
import support.util.StringUtil;



public class VGMDBParserTest extends ModelInformationTestBase implements Logger {
    private VGMDBParser parser;
    private VGMDBDetails albumDetails;
    private VGMDBSearchDetails searchDetails;
    private Semaphore semaphore;

    @BeforeClass
    public void beforeClass() {
        parser = new VGMDBParser();
        semaphore = new Semaphore(0);
        parser.registerCallback(new VGMDBParserCB() {
            @Override
            public void done(VGMDBDetails details) {
                debug("Album details retrieved: " + details);
                albumDetails = details;
                semaphore.release();
            }

            @Override
            public void done(VGMDBSearchDetails details) {
                debug("Search result: " + details);
                searchDetails = details;
                semaphore.release();
            }
        });
    }

    @AfterClass
    public static void afterClass() {
    }

    @BeforeMethod
    public void beforeMethod() {
        albumDetails = null;
        searchDetails = null;
    }

    @AfterMethod
    public void afterMethod() {
    }

    @Test
    public void testSearch() {
        parser.searchByAlbum("only my railgun");
        waitForData();
        searchAssertion();
    }

    @Test
    public void testAlbum() throws IOException {
        parser.searchByAlbum("only my railgun");
        waitForData();
        parser.retrieveAlbumByIndex(0);
        waitForData();
        albumAssertion();
    }

    private void searchAssertion() {
        assertEquals(searchDetails.getAlbumTitles().size(), 2, "album titles size mismatch");
        assertEquals(searchDetails.getIDs().size(), 2, "ids size mismatch");
        assertEquals(searchDetails.getAlbumTitles().get(0), "only my railgun / fripSide [Limited Edition]");
        assertEquals(searchDetails.getAlbumTitles().get(1), "only my railgun / fripSide");
    }

    private void albumAssertion() {
        assertEquals(albumDetails.getAlbumName(), "only my railgun / fripSide [Limited Edition]");
        assertEquals(albumDetails.getSeries(), "Toaru Kagaku no Railgun");
        assertEquals(StringUtil.getCommaSeparatedStringWithAnd(albumDetails.getArtists()), "fripSide & a2c");
        assertEquals(albumDetails.getReleaseDate(), "2009-11-04");
        assertEquals(albumDetails.getTracks().get(0), "only my railgun");
        assertEquals(albumDetails.getTracks().get(1), "late in autumn");
        assertEquals(albumDetails.getTracks().get(2), "only my railgun -instrumental-");
        assertEquals(albumDetails.getTracks().get(3), "late in autumn -instrumental-");
        assertEquals(albumDetails.getCatalog(), "GNCA-0151");
        assertEquals(albumDetails.getSites(), Arrays.asList(new String[] {"https://vgmdb.net/album/23188?perpage=99999",
            "http://www.cdjapan.co.jp/detailview.html?KEY=GNCA-151",
            "http://www.play-asia.com/paOS-13-71-9x-49-en-70-3iu7.html"}));
    }

    /**
     * Keep sleeping until data is posted
     *
     * @throws InterruptedException
     */
    private void waitForData() throws RuntimeException {
        try {
            semaphore.tryAcquire(10, TimeUnit.SECONDS);
        }
        catch (InterruptedException e1) {
            Assert.fail("Waited too long for data");
        }
    }
}
