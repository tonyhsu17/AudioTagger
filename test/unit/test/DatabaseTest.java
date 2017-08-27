package unit.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import model.database.DatabaseController;
import model.database.DatabaseController.Table;
import support.util.Utilities.EditorTag;



public class DatabaseTest {
    public DatabaseController db;
    //    public TemporaryFolder folder = new TemporaryFolder();
    //    private DestinationFileProcessor destFP;
    //    private File idFile;

    @BeforeMethod
    public void beforeMethod() {
        db = new DatabaseController("testDB");
    }

    @AfterMethod
    public void afterMethod() {
        db.deleteAllTables();
        db.cleanup();
    }

    @Test
    public void testAddAbumArtist() {
        db.setDataForTag(EditorTag.ALBUM_ARTIST, "Anime1");
        db.setDataForTag(EditorTag.ALBUM_ARTIST, "Anime 2");
        db.setDataForTag(EditorTag.ALBUM_ARTIST, "");
        assertTrue(db.containsCaseSensitive(Table.ALBUM_ARTIST, "Anime1"));
        assertTrue(db.containsCaseSensitive(Table.ALBUM_ARTIST, "Anime 2"));
        assertFalse(db.containsCaseSensitive(Table.ALBUM_ARTIST, "anime 2"));
        assertFalse(db.containsCaseSensitive(Table.ALBUM_ARTIST, ""));
    }

    @Test
    public void testAddArtistFirstName() {
        db.setDataForTag(EditorTag.ARTIST, "ArtistFirst");
        assertTrue(db.containsCaseSensitive(Table.ARTIST, "ArtistFirst", ""));
    }

    @Test
    public void testAddArtistFullName() {
        db.setDataForTag(EditorTag.ARTIST, "ArtistFirst Last");
        db.setDataForTag(EditorTag.ARTIST, "ArtistFirst Middle Last");
        
        assertTrue(db.containsCaseSensitive(Table.ARTIST, "ArtistFirst", "Last"));
        assertTrue(db.containsCaseSensitive(Table.ARTIST, "ArtistFirst Middle", "Last"));
    }

    @Test
    public void testAddGroupWithKnownArtist() {
        db.setDataForTag(EditorTag.ARTIST, "preadded");
        db.setDataForTag(EditorTag.ARTIST, "next preadded");
        db.setDataForTag(EditorTag.ARTIST, "GROUPY2");

        db.setDataForTag(EditorTag.ARTIST, "preadded", "next preadded");
        assertTrue(db.containsCaseSensitive(Table.GROUP_ARTIST, "preadded & next preadded"));

        db.setDataForTag(EditorTag.ARTIST, "GROUPY2", "preadded", "next preadded");
        assertTrue(db.containsCaseSensitive(Table.GROUP_ARTIST, "GROUPY2, preadded & next preadded"));
        assertTrue(db.containsCaseSensitive(Table.GROUP_ARTIST, "GROUPY2", "preadded","next preadded"));
    }

    @Test
    public void testAddGroupNewArtist() {
        db.setDataForTag(EditorTag.ARTIST, "Ace of Tokiwadai", "Misaka", "Mikoto Misaka");
        assertTrue(db.containsCaseSensitive(Table.GROUP_ARTIST, "Ace of Tokiwadai", "Misaka", "Mikoto Misaka"));
        assertTrue(db.containsCaseSensitive(Table.GROUP_ARTIST, "Ace of Tokiwadai, Misaka & Mikoto Misaka"));
        assertTrue(db.containsCaseSensitive(Table.ARTIST, "Misaka", ""));
        assertTrue(db.containsCaseSensitive(Table.ARTIST, "Mikoto", "Misaka"));
        assertTrue(db.containsCaseSensitive(Table.ARTIST, "Ace of", "Tokiwadai"));
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
        
        List<String> str = db.getPossibleDataForTag(EditorTag.ARTIST, "m");
        assertEquals(str.size(), 4);
        assertEquals(str.get(0), "Mikoto Misaka");
        assertEquals(str.get(1), "Misaka");
        assertEquals(str.get(2), "Misaka & Mikoto Misaka");
        assertEquals(str.get(3), "Ace of Tokiwadai, Misaka & Mikoto Misaka");
    }
}
