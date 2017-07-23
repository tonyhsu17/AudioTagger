


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import model.database.DatabaseController;
import model.database.DatabaseController.Table;
import support.util.Utilities.EditorTag;


public class DatabaseJUnitTest
{
    public DatabaseController db;
//    public TemporaryFolder folder = new TemporaryFolder();
//    private DestinationFileProcessor destFP;
//    private File idFile;
    
    @Before
    public void setUp()
    {
        db = new DatabaseController("testDB");
    }
    
    @After
    public void tearDown()
    {
        db.deleteAllTables();
        db.cleanup();
    }
    
    @Test
    public void testAddAnime()
    {
        db.setDataForTag(EditorTag.ALBUM_ARTIST, "Anime1");
        assertTrue(db.containsCaseSensitive(Table.ANIME, "Anime1"));
    }
    
    public void testAddArtistFirst()
    {
        db.setDataForTag(EditorTag.ARTIST, "ArtistFirst");
        assertTrue(db.containsCaseSensitive(Table.ARTIST, "ArtistFirst", ""));
    }
    
    public void testAddArtistFull()
    {
        db.setDataForTag(EditorTag.ARTIST, "ArtistFirst Last");
        assertTrue(db.containsCaseSensitive(Table.ARTIST, "ArtistFirst", "Last"));
    }
    
    @Test
    public void testAddGroupWithKnownArtist()
    {
        db.setDataForTag(EditorTag.ARTIST, "preadded");
        db.setDataForTag(EditorTag.ARTIST, "next preadded");
        db.setDataForTag(EditorTag.ARTIST, "GROUPY2");
        
        db.setDataForTag(EditorTag.ARTIST, "preadded", "next preadded");
        assertTrue(db.containsCaseSensitive(Table.GROUP_ARTIST, "preadded & next preadded"));
        
        db.setDataForTag(EditorTag.ARTIST, "GROUPY2", "preadded", "next preadded");
        assertTrue(db.containsCaseSensitive(Table.GROUP_ARTIST, "GROUPY2, preadded & next preadded"));
        
        assertTrue(db.containsCaseSensitive(Table.ARTIST, "GROUPY2", ""));
        assertTrue(db.containsCaseSensitive(Table.ARTIST, "preadded", ""));
        assertTrue(db.containsCaseSensitive(Table.ARTIST, "next", "preadded"));
    }
    
   @Test 
    public void testAddGroupNewArtist()
    {
        db.setDataForTag(EditorTag.ARTIST, "Ace of Tokiwadai", "Misaka", "Mikoto Misaka");
        assertTrue(db.containsCaseSensitive(Table.GROUP_ARTIST, "Ace of Tokiwadai, Misaka & Mikoto Misaka"));
        assertTrue(db.containsCaseSensitive(Table.ARTIST, "Misaka", ""));
        assertTrue(db.containsCaseSensitive(Table.ARTIST, "Mikoto", "Misaka"));
        assertTrue(db.containsCaseSensitive(Table.ARTIST, "Ace of", "Tokiwadai"));
    }
    
    @Test
    public void testGetPossibleAnime()
    {
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
    public void testGetNoPossibleAnime()
    {
        db.setDataForTag(EditorTag.ALBUM_ARTIST, "A Certain Scientific Railgun");
        db.setDataForTag(EditorTag.ALBUM_ARTIST, "A Certain Scientific Accelator");
        List<String> str = db.getPossibleDataForTag(EditorTag.ALBUM_ARTIST, "balh");
        assertEquals(0, str.size());
    }
    
    @Test
    public void testGetPossibleArtist()
    {
        db.setDataForTag(EditorTag.ARTIST, "Misaka");
        db.setDataForTag(EditorTag.ARTIST, "Mikoto Misaka");
        List<String> str = db.getPossibleDataForTag(EditorTag.ARTIST, "Misaka");
        assertEquals(2, str.size());
        assertTrue(str.contains("Misaka"));
        assertTrue(str.contains("Mikoto Misaka"));
    }
}
