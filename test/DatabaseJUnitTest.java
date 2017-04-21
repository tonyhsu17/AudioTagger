


import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import models.dataSuggestors.DatabaseController;
import models.dataSuggestors.DatabaseController.TableNames;
import support.Utilities.Tag;


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
        db.setDataForTag(Tag.AlbumArtist, "Anime1");
        assertTrue(db.containsCaseSensitive(TableNames.Anime, "Anime1"));
    }
    
    public void testAddArtistFirst()
    {
        db.setDataForTag(Tag.Artist, "ArtistFirst");
        assertTrue(db.containsCaseSensitive(TableNames.Artist, "ArtistFirst", ""));
    }
    
    public void testAddArtistFull()
    {
        db.setDataForTag(Tag.Artist, "ArtistFirst Last");
        assertTrue(db.containsCaseSensitive(TableNames.Artist, "ArtistFirst", "Last"));
    }
    
    @Test
    public void testAddGroupWithKnownArtist()
    {
        db.setDataForTag(Tag.Artist, "preadded");
        db.setDataForTag(Tag.Artist, "next preadded");
        db.setDataForTag(Tag.Artist, "GROUPY2");
        
        db.setDataForTag(Tag.Artist, "preadded", "next preadded");
        assertTrue(db.containsCaseSensitive(TableNames.Group, "preadded & next preadded"));
        
        db.setDataForTag(Tag.Artist, "GROUPY2", "preadded", "next preadded");
        assertTrue(db.containsCaseSensitive(TableNames.Group, "GROUPY2, preadded & next preadded"));
        
        assertTrue(db.containsCaseSensitive(TableNames.Artist, "GROUPY2", ""));
        assertTrue(db.containsCaseSensitive(TableNames.Artist, "preadded", ""));
        assertTrue(db.containsCaseSensitive(TableNames.Artist, "next", "preadded"));
    }
    
   @Test 
    public void testAddGroupNewArtist()
    {
        db.setDataForTag(Tag.Artist, "Ace of Tokiwadai", "Misaka", "Mikoto Misaka");
        assertTrue(db.containsCaseSensitive(TableNames.Group, "Ace of Tokiwadai, Misaka & Mikoto Misaka"));
        assertTrue(db.containsCaseSensitive(TableNames.Artist, "Misaka", ""));
        assertTrue(db.containsCaseSensitive(TableNames.Artist, "Mikoto", "Misaka"));
        assertTrue(db.containsCaseSensitive(TableNames.Artist, "Ace of", "Tokiwadai"));
    }
    
    @Test
    public void testGetPossibleAnime()
    {
        db.setDataForTag(Tag.AlbumArtist, "A Certain Scientific Railgun");
        db.setDataForTag(Tag.AlbumArtist, "A Certain Scientific Accelator");
        List<String> str = db.getPossibleDataForTag(Tag.AlbumArtist, "A Certain");
        assertEquals(2, str.size());
        assertTrue(str.contains("A Certain Scientific Railgun"));
        assertTrue(str.contains("A Certain Scientific Accelator"));
        
        str = db.getPossibleDataForTag(Tag.AlbumArtist, "A Certain");
        assertEquals(2, str.size());
        assertTrue(str.contains("A Certain Scientific Railgun"));
        assertTrue(str.contains("A Certain Scientific Accelator"));
    }
    
    @Test
    public void testGetNoPossibleAnime()
    {
        db.setDataForTag(Tag.AlbumArtist, "A Certain Scientific Railgun");
        db.setDataForTag(Tag.AlbumArtist, "A Certain Scientific Accelator");
        List<String> str = db.getPossibleDataForTag(Tag.AlbumArtist, "balh");
        assertEquals(0, str.size());
    }
    
    @Test
    public void testGetPossibleArtist()
    {
        db.setDataForTag(Tag.Artist, "Ace of Tokiwadai");
        db.setDataForTag(Tag.Artist, "Misaka");
        db.setDataForTag(Tag.Artist, "Mikoto Misaka");
        List<String> str = db.getPossibleDataForTag(Tag.Artist, "Misaka");
        assertEquals(3, str.size());
        assertTrue(str.contains("Ace of Tokiwadai"));
        assertTrue(str.contains("Misaka"));
        assertTrue(str.contains("Mikoto Misaka"));
    }
}
