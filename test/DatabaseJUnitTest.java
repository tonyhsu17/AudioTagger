


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import model.information.DatabaseController;
import model.information.DatabaseController.TableNames;
import support.util.Utilities.Tag;


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
        db.setDataForTag(Tag.ALBUM_ARTIST, "Anime1");
        assertTrue(db.containsCaseSensitive(TableNames.Anime, "Anime1"));
    }
    
    public void testAddArtistFirst()
    {
        db.setDataForTag(Tag.ARTIST, "ArtistFirst");
        assertTrue(db.containsCaseSensitive(TableNames.Artist, "ArtistFirst", ""));
    }
    
    public void testAddArtistFull()
    {
        db.setDataForTag(Tag.ARTIST, "ArtistFirst Last");
        assertTrue(db.containsCaseSensitive(TableNames.Artist, "ArtistFirst", "Last"));
    }
    
    @Test
    public void testAddGroupWithKnownArtist()
    {
        db.setDataForTag(Tag.ARTIST, "preadded");
        db.setDataForTag(Tag.ARTIST, "next preadded");
        db.setDataForTag(Tag.ARTIST, "GROUPY2");
        
        db.setDataForTag(Tag.ARTIST, "preadded", "next preadded");
        assertTrue(db.containsCaseSensitive(TableNames.Group, "preadded & next preadded"));
        
        db.setDataForTag(Tag.ARTIST, "GROUPY2", "preadded", "next preadded");
        assertTrue(db.containsCaseSensitive(TableNames.Group, "GROUPY2, preadded & next preadded"));
        
        assertTrue(db.containsCaseSensitive(TableNames.Artist, "GROUPY2", ""));
        assertTrue(db.containsCaseSensitive(TableNames.Artist, "preadded", ""));
        assertTrue(db.containsCaseSensitive(TableNames.Artist, "next", "preadded"));
    }
    
   @Test 
    public void testAddGroupNewArtist()
    {
        db.setDataForTag(Tag.ARTIST, "Ace of Tokiwadai", "Misaka", "Mikoto Misaka");
        assertTrue(db.containsCaseSensitive(TableNames.Group, "Ace of Tokiwadai, Misaka & Mikoto Misaka"));
        assertTrue(db.containsCaseSensitive(TableNames.Artist, "Misaka", ""));
        assertTrue(db.containsCaseSensitive(TableNames.Artist, "Mikoto", "Misaka"));
        assertTrue(db.containsCaseSensitive(TableNames.Artist, "Ace of", "Tokiwadai"));
    }
    
    @Test
    public void testGetPossibleAnime()
    {
        db.setDataForTag(Tag.ALBUM_ARTIST, "A Certain Scientific Railgun");
        db.setDataForTag(Tag.ALBUM_ARTIST, "A Certain Scientific Accelator");
        List<String> str = db.getPossibleDataForTag(Tag.ALBUM_ARTIST, "A Certain");
        assertEquals(2, str.size());
        assertTrue(str.contains("A Certain Scientific Railgun"));
        assertTrue(str.contains("A Certain Scientific Accelator"));
        
        str = db.getPossibleDataForTag(Tag.ALBUM_ARTIST, "A Certain");
        assertEquals(2, str.size());
        assertTrue(str.contains("A Certain Scientific Railgun"));
        assertTrue(str.contains("A Certain Scientific Accelator"));
    }
    
    @Test
    public void testGetNoPossibleAnime()
    {
        db.setDataForTag(Tag.ALBUM_ARTIST, "A Certain Scientific Railgun");
        db.setDataForTag(Tag.ALBUM_ARTIST, "A Certain Scientific Accelator");
        List<String> str = db.getPossibleDataForTag(Tag.ALBUM_ARTIST, "balh");
        assertEquals(0, str.size());
    }
    
    @Test
    public void testGetPossibleArtist()
    {
        db.setDataForTag(Tag.ARTIST, "Ace of Tokiwadai");
        db.setDataForTag(Tag.ARTIST, "Misaka");
        db.setDataForTag(Tag.ARTIST, "Mikoto Misaka");
        List<String> str = db.getPossibleDataForTag(Tag.ARTIST, "Misaka");
        assertEquals(3, str.size());
        assertTrue(str.contains("Ace of Tokiwadai"));
        assertTrue(str.contains("Misaka"));
        assertTrue(str.contains("Mikoto Misaka"));
    }
}
