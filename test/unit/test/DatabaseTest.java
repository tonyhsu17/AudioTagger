package unit.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.sql.SQLException;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import modules.controllers.DatabaseController;
import modules.database.tables.AlbumArtist;
import modules.database.tables.Artist;
import modules.database.tables.GroupArtist;
import support.util.Utilities.EditorTag;



public class DatabaseTest {
    public DatabaseController db;
    //    public TemporaryFolder folder = new TemporaryFolder();
    //    private DestinationFileProcessor destFP;
    //    private File idFile;

    @BeforeClass
    public void beforeClass() {
        try {
            db = new DatabaseController("testDB");
           
        }
        catch (SQLException e) {
            Assert.fail("unable to initialze db");
        }
    }

    @BeforeMethod
    public void beforeMethod() {
        db.resetDatabase();
    }

    @AfterMethod
    public void afterMethod() {
//        db.resetDatabase();
    }

    @AfterClass
    public void afterClass() {
//        db.resetDatabase();
    }

    @Test
    public void testAddAbumArtist() {
        db.setDataForTag(EditorTag.ALBUM_ARTIST, "Anime1");
        db.setDataForTag(EditorTag.ALBUM_ARTIST, "Anime 2");
        db.setDataForTag(EditorTag.ALBUM_ARTIST, "");
        assertTrue(db.containsCaseSensitive(AlbumArtist.instance(), "Anime1"));
        assertTrue(db.containsCaseSensitive(AlbumArtist.instance(), "Anime 2"));
        assertFalse(db.containsCaseSensitive(AlbumArtist.instance(), "anime 2"));
        assertFalse(db.containsCaseSensitive(AlbumArtist.instance(), ""));
    }

    @Test
    public void testAddArtistFirstName() {
        db.setDataForTag(EditorTag.ARTIST, "ArtistFirst");
        assertTrue(db.containsCaseSensitive(Artist.instance(), "ArtistFirst", ""));
    }

    @Test
    public void testAddArtistFullName() {
        db.setDataForTag(EditorTag.ARTIST, "ArtistFirst Last");
        db.setDataForTag(EditorTag.ARTIST, "ArtistFirst Middle Last");

        assertTrue(db.containsCaseSensitive(Artist.instance(), "ArtistFirst", "Last"));
        assertTrue(db.containsCaseSensitive(Artist.instance(), "ArtistFirst Middle", "Last"));
    }

    @Test
    public void testAddGroupWithKnownArtist() {
        db.setDataForTag(EditorTag.ARTIST, "preadded");
        db.setDataForTag(EditorTag.ARTIST, "next preadded");
        db.setDataForTag(EditorTag.ARTIST, "GROUPY2");

        db.setDataForTag(EditorTag.ARTIST, "preadded", "next preadded");
        assertTrue(db.containsCaseSensitive(GroupArtist.instance(), "preadded & next preadded"));

        db.setDataForTag(EditorTag.ARTIST, "GROUPY2", "preadded", "next preadded");
        assertTrue(db.containsCaseSensitive(GroupArtist.instance(), "GROUPY2, preadded & next preadded"));
        assertTrue(db.containsCaseSensitive(GroupArtist.instance(), "GROUPY2", "preadded", "next preadded"));
    }

    @Test
    public void testAddGroupNewArtist() {
        db.setDataForTag(EditorTag.ARTIST, "Ace of Tokiwadai", "Misaka", "Mikoto Misaka");
        assertTrue(db.containsCaseSensitive(GroupArtist.instance(), "Ace of Tokiwadai", "Misaka", "Mikoto Misaka"));
        assertTrue(db.containsCaseSensitive(GroupArtist.instance(), "Ace of Tokiwadai, Misaka & Mikoto Misaka"));
        assertTrue(db.containsCaseSensitive(Artist.instance(), "Misaka", ""));
        assertTrue(db.containsCaseSensitive(Artist.instance(), "Mikoto", "Misaka"));
        assertTrue(db.containsCaseSensitive(Artist.instance(), "Ace of", "Tokiwadai"));
    }

    @Test
    public void testGetPossibleAlbumArtist() {
        db.setDataForTag(EditorTag.ALBUM_ARTIST, "A Certain Scientific Railgun");
        db.setDataForTag(EditorTag.ALBUM_ARTIST, "A Certain Scientific Accelator");
        List<String> str = db.getPossibleDataForTag(EditorTag.ALBUM_ARTIST, "A Certain");
        assertEquals(2, str.size());
        assertTrue(str.contains("A Certain Scientific Railgun"));
        assertTrue(str.contains("A Certain Scientific Accelator"));

        str = db.getPossibleDataForTag(EditorTag.ALBUM_ARTIST, "A Certain");
        assertEquals(2, str.size());
        assertTrue(str.contains("A Certain Scientific Railgun"));
        assertTrue(str.contains("A Certain Scientific Accelator"));
    }

    @Test
    public void testGetNoPossibleAlbumArtist() {
        db.setDataForTag(EditorTag.ALBUM_ARTIST, "A Certain Scientific Railgun");
        db.setDataForTag(EditorTag.ALBUM_ARTIST, "A Certain Scientific Accelator");
        List<String> str = db.getPossibleDataForTag(EditorTag.ALBUM_ARTIST, "balh");
        assertEquals(0, str.size());
    }

    @Test
    public void testGetPossibleArtist() {
        db.setDataForTag(EditorTag.ARTIST, "Misaka");
        db.setDataForTag(EditorTag.ARTIST, "Mikoto Misaka");
        List<String> str = db.getPossibleDataForTag(EditorTag.ARTIST, "Misaka");
        assertEquals(2, str.size());
        assertTrue(str.contains("Misaka"));
        assertTrue(str.contains("Mikoto Misaka"));
    }

    @Test
    public void testGetPossibleArtistWithGroups() {
        db.setDataForTag(EditorTag.ARTIST, "Misaka");
        db.setDataForTag(EditorTag.ARTIST, "Mikoto Misaka");
        db.setDataForTag(EditorTag.ARTIST, "Ace of Tokiwadai", "Misaka", "Mikoto Misaka");

        List<String> str = db.getPossibleDataForTag(EditorTag.ARTIST, "Misaka");
        assertEquals(3, str.size());
        assertTrue(str.contains("Misaka"));
        assertTrue(str.contains("Mikoto Misaka"));
        assertTrue(str.contains("Ace of Tokiwadai, Misaka & Mikoto Misaka"));
    }

    @Test
    public void testAddSameArtist() {
        db.setDataForTag(EditorTag.ARTIST, "Mikoto Misaka");
        db.setDataForTag(EditorTag.ARTIST, "Mikoto Misaka");
        db.setDataForTag(EditorTag.ARTIST, "Mikoto Misaka");

        List<String> str = db.getPossibleDataForTag(EditorTag.ARTIST, "m");
        assertEquals(1, str.size());
        assertTrue(str.contains("Mikoto Misaka"));
    }

    @Test
    public void testAddSameGroup() {
        db.setDataForTag(EditorTag.ARTIST, "Ace of Tokiwadai", "Misaka", "Mikoto Misaka");
        db.setDataForTag(EditorTag.ARTIST, "Ace of Tokiwadai", "Misaka", "Mikoto Misaka");
        db.setDataForTag(EditorTag.ARTIST, "Ace of Tokiwadai", "Misaka", "Mikoto Misaka");

        List<String> str = db.getPossibleDataForTag(EditorTag.ARTIST, "Ace of Tokiwadai, Misaka & Mikoto Misaka");
        assertEquals(1, str.size());
        assertTrue(str.contains("Ace of Tokiwadai, Misaka & Mikoto Misaka"));
    }

    @Test
    public void testGetArtistOrdered() {
        db.setDataForTag(EditorTag.ARTIST, "Ace of Tokiwadai", "Misaka", "Mikoto Misaka");
        db.setDataForTag(EditorTag.ARTIST, "Misaka", "Mikoto Misaka");
        db.setDataForTag(EditorTag.ARTIST, "Ace of Tokiwadai", "Misaka", "Mikoto Misaka");
        db.setDataForTag(EditorTag.ARTIST, "Mikoto Misaka");
//        [Mikoto Misaka, Ace of Tokiwadai, Misaka & Mikoto Misaka, Misaka & Mikoto Misaka]
        List<String> str = db.getPossibleDataForTag(EditorTag.ARTIST, "m");
        assertEquals(str.size(), 4);
        assertEquals(str.get(0), "Mikoto Misaka");
        assertEquals(str.get(1), "Misaka");
        assertEquals(str.get(2), "Ace of Tokiwadai, Misaka & Mikoto Misaka");
        assertEquals(str.get(3), "Misaka & Mikoto Misaka");
        
    }
}
