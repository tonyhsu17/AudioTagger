import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import application.Configuration;
import models.dataSuggestors.AudioFiles;
import support.Utilities.Tag;

public class AudioFileJUnitTest
{
    public AudioFiles audio = new AudioFiles();
  
  @Before
  public void setUp()
  {
      audio.setWorkingDirectory("TestResources");
      Configuration.getInstance().turnOffPropagateSave();
  }
  
  @After
  public void tearDown()
  {
      // reset data points
  }
  
  @Test
  public void testWorkingDirectory()
  {
      audio.setWorkingDirectory("TestResources/TestFolderA");
      List<String> files = audio.getFileNames();
      
      assertEquals(3, files.size());
      assertTrue(files.contains("testFileA.mp3"));
      assertTrue(files.contains("testFileB.mp3"));
  }
  
  @Test
  public void testAppendDirectoryAndRecurseFolders()
  {
     audio.setWorkingDirectory("TestResources/TestFolderA");
     audio.appendWorkingDirectory(new File("TestResources/TestFolderB").listFiles());
     List<String> files = audio.getFileNames();
     assertEquals(7, files.size());
     assertTrue(files.contains("testFileC.mp3"));
     assertTrue(files.contains("testFileD.mp3"));
  }
  
  @Test
  public void testSetTagFields()
  {
      List<String> files = audio.getFileNames();
      assertEquals("", audio.getDataForTag(Tag.FileName));
      audio.selectTag(1);
      assertEquals(files.get(1), audio.getDataForTag(Tag.FileName));
  }
  
  @Test
  public void testSetMultipleTagFields()
  {
      List<String> fileNames = audio.getFileNames();
      List<Integer> indicies = new ArrayList<Integer>();
      indicies.add(fileNames.indexOf("testFileA.mp3"));
      indicies.add(fileNames.indexOf("testFileB.mp3"));
      audio.selectTag(indicies.get(0)); // will always select 1 before multiple
      audio.selectTags(indicies);
      assertEquals("<Different Values>", audio.getDataForTag(Tag.FileName));
      assertEquals("<Different Values>", audio.getDataForTag(Tag.Title));
      assertEquals("<Different Values>", audio.getDataForTag(Tag.Artist));
      assertEquals("TestB", audio.getDataForTag(Tag.Album));
      assertEquals("TestCollection", audio.getDataForTag(Tag.AlbumArtist));
      assertEquals("<Different Values>", audio.getDataForTag(Tag.Track));
      assertEquals("2011", audio.getDataForTag(Tag.Year));
      assertEquals("Anime", audio.getDataForTag(Tag.Genre));
      assertEquals("comment goes here", audio.getDataForTag(Tag.Comment));
      assertNull(audio.getAlbumArt());
  }
  
  @Test
  public void testSaveTagsSingle()
  {
      List<String> fileNames = audio.getFileNames();
      audio.selectTag(fileNames.indexOf("testFileA.mp3"));
      assertEquals("testFileA.mp3", audio.getDataForTag(Tag.FileName, ""));
      assertEquals("TestA", audio.getDataForTag(Tag.Title, ""));
      assertEquals("Various Artists", audio.getDataForTag(Tag.Artist, ""));
      assertEquals("TestCollection", audio.getDataForTag(Tag.AlbumArtist, ""));
      assertEquals("TestB", audio.getDataForTag(Tag.Album, ""));
      assertEquals("2011", audio.getDataForTag(Tag.Year));
      assertEquals("1", audio.getDataForTag(Tag.Track, ""));
      assertEquals("Anime", audio.getDataForTag(Tag.Genre, ""));
      assertEquals("comment goes here", audio.getDataForTag(Tag.Comment, ""));

//      audio.setDataForTag(Tag.FileName, "2000");
      audio.setDataForTag(Tag.Title, "title");
      audio.setDataForTag(Tag.Artist, "artist");
      audio.setDataForTag(Tag.AlbumArtist, "albumArtist");
      audio.setDataForTag(Tag.Album, "album");
      audio.setDataForTag(Tag.Year, "1999");
      audio.setDataForTag(Tag.Track, "2");
      audio.setDataForTag(Tag.Genre, "pop");
      audio.setDataForTag(Tag.Comment, "comment");
            
      audio.save();
      audio.setWorkingDirectory("TestResources"); // refresh the data
      fileNames = audio.getFileNames();
      audio.selectTag(fileNames.indexOf("testFileA.mp3"));
      
//      assertEquals("testFileA.mp3", audio.getDataForTag(Tag.FileName, ""));
      assertEquals("title", audio.getDataForTag(Tag.Title, ""));
      assertEquals("artist", audio.getDataForTag(Tag.Artist, ""));
      assertEquals("albumArtist", audio.getDataForTag(Tag.AlbumArtist, ""));
      assertEquals("album", audio.getDataForTag(Tag.Album, ""));
      assertEquals("1999", audio.getDataForTag(Tag.Year));
      assertEquals("2", audio.getDataForTag(Tag.Track, ""));
      assertEquals("Pop", audio.getDataForTag(Tag.Genre, ""));
      assertEquals("comment", audio.getDataForTag(Tag.Comment, ""));
      
      // reset data back to original probably 
      // better to make a copy?
      audio.setDataForTag(Tag.FileName, "testFileA.mp3");
      audio.setDataForTag(Tag.Title, "TestA");
      audio.setDataForTag(Tag.Artist, "Various Artists");
      audio.setDataForTag(Tag.AlbumArtist, "TestCollection");
      audio.setDataForTag(Tag.Album, "TestB");
      audio.setDataForTag(Tag.Year, "2011");
      audio.setDataForTag(Tag.Track, "1");
      audio.setDataForTag(Tag.Genre, "Anime");
      audio.setDataForTag(Tag.Comment, "comment goes here");
            
      audio.save();
  }
  
  @Test
  public void testSaveTagsMultiple()
  {
      List<String> fileNames = audio.getFileNames();
      List<Integer> indicies = new ArrayList<Integer>();
      indicies.add(fileNames.indexOf("testFileA.mp3"));
      indicies.add(fileNames.indexOf("testFileB.mp3"));
      audio.selectTag(indicies.get(0)); // will always select 1 before multiple
      audio.selectTags(indicies);
      
      audio.setDataForTag(Tag.Artist, "NowSame Artist");
      assertEquals("NowSame Artist", audio.getDataForTag(Tag.Artist));
      audio.save();
     
      audio.setWorkingDirectory("TestResources"); // refresh the data
      fileNames = audio.getFileNames();
      indicies.clear();
      indicies.add(fileNames.indexOf("testFileA.mp3"));
      indicies.add(fileNames.indexOf("testFileB.mp3"));
      System.out.println("indicies: " + Arrays.toString(indicies.toArray(new Integer[0])));
      audio.selectTag(indicies.get(0)); // will always select 1 before multiple
      assertEquals("NowSame Artist", audio.getDataForTag(Tag.Artist));
      audio.selectTag(indicies.get(1)); // will always select 1 before multiple
      assertEquals("NowSame Artist", audio.getDataForTag(Tag.Artist));
      
      
      // revert data
      audio.selectTag(indicies.get(0));
      audio.setDataForTag(Tag.Artist, "Various Artists");
      audio.save();
      
      audio.selectTag(indicies.get(1));
      audio.setDataForTag(Tag.Artist, "Various Artist");
      audio.save();
      
  }
}
