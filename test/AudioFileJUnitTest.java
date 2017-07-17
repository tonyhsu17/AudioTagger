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

import model.Settings;
import model.Settings.SettingsKey;
import model.information.AudioFiles;
import support.util.Utilities.Tag;

public class AudioFileJUnitTest
{
    public AudioFiles audio = new AudioFiles();
  
  @Before
  public void setUp()
  {
      audio.setWorkingDirectory("TestResources");
      Settings.getInstance().setSetting(SettingsKey.PROPAGATE_SAVE_ALBUM, "false");
      Settings.getInstance().setSetting(SettingsKey.PROPAGATE_SAVE_ALBUM_ART, "false");
      Settings.getInstance().setSetting(SettingsKey.PROPAGATE_SAVE_ALBUM_ARTIST, "false");
      Settings.getInstance().setSetting(SettingsKey.PROPAGATE_SAVE_ARTIST, "false");
      Settings.getInstance().setSetting(SettingsKey.PROPAGATE_SAVE_COMMENT, "false");
      Settings.getInstance().setSetting(SettingsKey.PROPAGATE_SAVE_GENRE, "false");
      Settings.getInstance().setSetting(SettingsKey.PROPAGATE_SAVE_YEAR, "false");
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
      assertEquals("", audio.getDataForTag(Tag.FILE_NAME));
      audio.selectTag(1);
      assertEquals(files.get(1), audio.getDataForTag(Tag.FILE_NAME));
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
      assertEquals("<Different Values>", audio.getDataForTag(Tag.FILE_NAME));
      assertEquals("<Different Values>", audio.getDataForTag(Tag.TITLE));
      assertEquals("<Different Values>", audio.getDataForTag(Tag.ARTIST));
      assertEquals("TestB", audio.getDataForTag(Tag.ALBUM));
      assertEquals("TestCollection", audio.getDataForTag(Tag.ALBUM_ARTIST));
      assertEquals("<Different Values>", audio.getDataForTag(Tag.TRACK));
      assertEquals("2011", audio.getDataForTag(Tag.YEAR));
      assertEquals("Anime", audio.getDataForTag(Tag.GENRE));
      assertEquals("comment goes here", audio.getDataForTag(Tag.COMMENT));
      assertNull(audio.getAlbumArt());
  }
  
  @Test
  public void testSaveTagsSingle()
  {
      List<String> fileNames = audio.getFileNames();
      audio.selectTag(fileNames.indexOf("testFileA.mp3"));
      assertEquals("testFileA.mp3", audio.getDataForTag(Tag.FILE_NAME, ""));
      assertEquals("TestA", audio.getDataForTag(Tag.TITLE, ""));
      assertEquals("Various Artists", audio.getDataForTag(Tag.ARTIST, ""));
      assertEquals("TestCollection", audio.getDataForTag(Tag.ALBUM_ARTIST, ""));
      assertEquals("TestB", audio.getDataForTag(Tag.ALBUM, ""));
      assertEquals("2011", audio.getDataForTag(Tag.YEAR));
      assertEquals("01", audio.getDataForTag(Tag.TRACK, ""));
      assertEquals("Anime", audio.getDataForTag(Tag.GENRE, ""));
      assertEquals("comment goes here", audio.getDataForTag(Tag.COMMENT, ""));

//      audio.setDataForTag(Tag.FileName, "2000");
      audio.setDataForTag(Tag.TITLE, "title");
      audio.setDataForTag(Tag.ARTIST, "artist");
      audio.setDataForTag(Tag.ALBUM_ARTIST, "albumArtist");
      audio.setDataForTag(Tag.ALBUM, "album");
      audio.setDataForTag(Tag.YEAR, "1999");
      audio.setDataForTag(Tag.TRACK, "2");
      audio.setDataForTag(Tag.GENRE, "pop");
      audio.setDataForTag(Tag.COMMENT, "comment");
            
      audio.save();
      audio.setWorkingDirectory("TestResources"); // refresh the data
      fileNames = audio.getFileNames();
      audio.selectTag(fileNames.indexOf("testFileA.mp3"));
      
//      assertEquals("testFileA.mp3", audio.getDataForTag(Tag.FileName, ""));
      assertEquals("title", audio.getDataForTag(Tag.TITLE, ""));
      assertEquals("artist", audio.getDataForTag(Tag.ARTIST, ""));
      assertEquals("albumArtist", audio.getDataForTag(Tag.ALBUM_ARTIST, ""));
      assertEquals("album", audio.getDataForTag(Tag.ALBUM, ""));
      assertEquals("1999", audio.getDataForTag(Tag.YEAR));
      assertEquals("02", audio.getDataForTag(Tag.TRACK, ""));
      assertEquals("Pop", audio.getDataForTag(Tag.GENRE, ""));
      assertEquals("comment", audio.getDataForTag(Tag.COMMENT, ""));
      
      // reset data back to original probably 
      // better to make a copy?
      audio.setDataForTag(Tag.FILE_NAME, "testFileA.mp3");
      audio.setDataForTag(Tag.TITLE, "TestA");
      audio.setDataForTag(Tag.ARTIST, "Various Artists");
      audio.setDataForTag(Tag.ALBUM_ARTIST, "TestCollection");
      audio.setDataForTag(Tag.ALBUM, "TestB");
      audio.setDataForTag(Tag.YEAR, "2011");
      audio.setDataForTag(Tag.TRACK, "1");
      audio.setDataForTag(Tag.GENRE, "Anime");
      audio.setDataForTag(Tag.COMMENT, "comment goes here");
            
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
      
      audio.setDataForTag(Tag.ARTIST, "NowSame Artist");
      assertEquals("NowSame Artist", audio.getDataForTag(Tag.ARTIST));
      audio.save();
     
      audio.setWorkingDirectory("TestResources"); // refresh the data
      fileNames = audio.getFileNames();
      indicies.clear();
      indicies.add(fileNames.indexOf("testFileA.mp3"));
      indicies.add(fileNames.indexOf("testFileB.mp3"));
      System.out.println("indicies: " + Arrays.toString(indicies.toArray(new Integer[0])));
      audio.selectTag(indicies.get(0)); // will always select 1 before multiple
      assertEquals("NowSame Artist", audio.getDataForTag(Tag.ARTIST));
      audio.selectTag(indicies.get(1)); // will always select 1 before multiple
      assertEquals("NowSame Artist", audio.getDataForTag(Tag.ARTIST));
      
      
      // revert data
      audio.selectTag(indicies.get(0));
      audio.setDataForTag(Tag.ARTIST, "Various Artists");
      audio.save();
      
      audio.selectTag(indicies.get(1));
      audio.setDataForTag(Tag.ARTIST, "Various Artist");
      audio.save();
      
  }
}
