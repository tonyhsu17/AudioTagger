package unit.test;


import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import model.Settings;
import model.Settings.SettingsKey;
import modules.controllers.AudioFilesController;
import modules.controllers.base.TagBase;
import support.Constants;
import support.structure.TagDetails;
import support.util.Utilities.EditorTag;

// TODO include album art

public class AudioFilesTest extends ModelInformationTestBase {
    private static String WORKING_DIR;
    private static final String FOLDER_A = "FolderA";
    private static final String FOLDER_B = "FolderB";
    private static final String FOLDER_C = "FolderC";
    private static final String FILE_A = "testFileA.mp3";
    private static final String FILE_B = "testFileB.mp3";
    private static final String FILE_C = "testFileC.mp3";
    private static final String FILE_D = "testFileD.mp3";
    private static String TEMP_DIR;

    private File tempDir;
    private AudioFilesController audioList;

    @BeforeClass
    public void setUp() throws URISyntaxException {
        WORKING_DIR = getClass().getClassLoader().getResource("Audios/").toURI().getPath();
        TEMP_DIR = new File(WORKING_DIR).getParent() + "/Temp";
        tempDir = new File(TEMP_DIR);
        try {
            FileUtils.copyDirectory(new File(WORKING_DIR), tempDir);
        }
        catch (IOException e) {
            fail("Unable to copy test resources");
        }
        Settings.getInstance().setSetting(SettingsKey.PROPAGATE_SAVE_ALBUM, "false");
        Settings.getInstance().setSetting(SettingsKey.PROPAGATE_SAVE_ALBUM_ART, "false");
        Settings.getInstance().setSetting(SettingsKey.PROPAGATE_SAVE_ALBUM_ARTIST, "false");
        Settings.getInstance().setSetting(SettingsKey.PROPAGATE_SAVE_ARTIST, "false");
        Settings.getInstance().setSetting(SettingsKey.PROPAGATE_SAVE_COMMENT, "false");
        Settings.getInstance().setSetting(SettingsKey.PROPAGATE_SAVE_GENRE, "false");
        Settings.getInstance().setSetting(SettingsKey.PROPAGATE_SAVE_YEAR, "false");
    }

    @BeforeMethod
    public void beforeMethod() {
        audioList = new AudioFilesController();
    }

    @AfterClass
    public void afterClass() {
        try {
            FileUtils.deleteDirectory(tempDir);
        }
        catch (IOException e) {
        }
    }

    @Test
    public void testWorkingDirectory() {
        System.out.println(WORKING_DIR + FOLDER_A);
        System.out.println(new File(WORKING_DIR + FOLDER_A).listFiles().length);
        audioList.setWorkingDirectory(WORKING_DIR + FOLDER_A);
        List<String> files = audioList.getFileNames();

        assertEquals(files.size(), 3);
        assertEquals(files.get(0), Constants.HEADER_ALBUM + FOLDER_A);
        assertEquals(files.get(1), FILE_A);
        assertEquals(files.get(2), FILE_B);
    }

    @Test
    public void testWorkingDirectoryWithSubDir() {
        audioList.setWorkingDirectory(WORKING_DIR + FOLDER_B);
        List<String> files = audioList.getFileNames();

        assertEquals(files.size(), 4);
        assertEquals(files.get(0), Constants.HEADER_ALBUM + FOLDER_B);
        assertEquals(files.get(1), FILE_C);
        assertEquals(files.get(2), Constants.HEADER_ALBUM + FOLDER_C);
        assertEquals(files.get(3), FILE_D);
    }

    @Test
    public void testAppendDirectoryAndRecurseFolders() {
        audioList.setWorkingDirectory(WORKING_DIR + FOLDER_A);
        audioList.appendWorkingDirectory(new File(WORKING_DIR + FOLDER_B).listFiles());
        List<String> files = audioList.getFileNames();

        assertEquals(files.size(), 7);
        assertEquals(files.get(0), Constants.HEADER_ALBUM + FOLDER_A);
        assertEquals(files.get(1), FILE_A);
        assertEquals(files.get(2), FILE_B);
        assertEquals(files.get(3), Constants.HEADER_ALBUM + FOLDER_B);
        assertEquals(files.get(4), FILE_C);
        assertEquals(files.get(5), Constants.HEADER_ALBUM + FOLDER_C);
        assertEquals(files.get(6), FILE_D);
    }

    @DataProvider(name = "audioMeta")
    public static Object[][] audioMeta() {


        return new Object[][] {
            {"Selected folder with multiple",
                makeTagDetails(Constants.KEYWORD_DIFF_VALUE,
                    Constants.KEYWORD_DIFF_VALUE,
                    "ArtistA",
                    "AlbumA",
                    "TestCollection",
                    Constants.KEYWORD_DIFF_VALUE,
                    "2011",
                    "Anime",
                    "FolderA Comments"),
                0},
            {"Selected single",
                makeTagDetails("testFileA.mp3",
                    "TestA",
                    "ArtistA",
                    "AlbumA",
                    "TestCollection",
                    "01",
                    "2011",
                    "Anime",
                    "FolderA Comments"),
                1},
            {"Selected single",
                makeTagDetails("testFileB.mp3",
                    "TestB",
                    "ArtistA",
                    "AlbumA",
                    "TestCollection",
                    "02",
                    "2011",
                    "Anime",
                    "FolderA Comments"),
                2},
            {"Selected folder with single",
                makeTagDetails("testFileC.mp3",
                    "TestC",
                    "ArtistB",
                    "TestB",
                    "TestCollectionB",
                    "01",
                    "2011",
                    "Anime",
                    "FolderB Comments"),
                3},
            {"Selected single",
                makeTagDetails("testFileC.mp3",
                    "TestC",
                    "ArtistB",
                    "TestB",
                    "TestCollectionB",
                    "01",
                    "2011",
                    "Anime",
                    "FolderB Comments"),
                4},
            {"Selected folder with single",
                makeTagDetails("testFileD.mp3",
                    "TestD",
                    "ArtistC",
                    "TestD",
                    "TestCollectionB",
                    "01",
                    "2011",
                    "Anime",
                    "FolderC Comments"),
                5},
            {"Selected single",
                makeTagDetails("testFileD.mp3",
                    "TestD",
                    "ArtistC",
                    "TestD",
                    "TestCollectionB",
                    "01",
                    "2011",
                    "Anime",
                    "FolderC Comments"),
                6},
            {"Selected singles same folder",
                makeTagDetails(Constants.KEYWORD_DIFF_VALUE,
                    Constants.KEYWORD_DIFF_VALUE,
                    "ArtistA",
                    "AlbumA",
                    "TestCollection",
                    Constants.KEYWORD_DIFF_VALUE,
                    "2011",
                    "Anime",
                    "FolderA Comments"),
                0},
            {"Selected singles diff folder",
                makeTagDetails(Constants.KEYWORD_DIFF_VALUE,
                    Constants.KEYWORD_DIFF_VALUE,
                    Constants.KEYWORD_DIFF_VALUE,
                    Constants.KEYWORD_DIFF_VALUE,
                    "TestCollectionB",
                    "01",
                    "2011",
                    "Anime",
                    Constants.KEYWORD_DIFF_VALUE),
                4,
                6},
            {"Selected 2 folders",
                makeTagDetails(Constants.KEYWORD_DIFF_VALUE,
                    Constants.KEYWORD_DIFF_VALUE,
                    Constants.KEYWORD_DIFF_VALUE,
                    Constants.KEYWORD_DIFF_VALUE,
                    "TestCollectionB",
                    "01",
                    "2011",
                    "Anime",
                    Constants.KEYWORD_DIFF_VALUE),
                3,
                5},
            {"Selected folder and single",
                makeTagDetails(Constants.KEYWORD_DIFF_VALUE,
                    Constants.KEYWORD_DIFF_VALUE,
                    Constants.KEYWORD_DIFF_VALUE,
                    Constants.KEYWORD_DIFF_VALUE,
                    Constants.KEYWORD_DIFF_VALUE,
                    Constants.KEYWORD_DIFF_VALUE,
                    "2011",
                    "Anime",
                    Constants.KEYWORD_DIFF_VALUE),
                0,
                3}};
    }

    private static TagDetails makeTagDetails(String fileName, String title, String artist, String album, String albumArtist,
        String track, String year, String genre, String comment) {
        TagDetails info = new TagDetails();
        info.set(EditorTag.FILE_NAME, fileName);
        info.set(EditorTag.TITLE, title);
        info.set(EditorTag.ARTIST, artist);
        info.set(EditorTag.ALBUM, album);
        info.set(EditorTag.ALBUM_ARTIST, albumArtist);
        info.set(EditorTag.TRACK, track);
        info.set(EditorTag.YEAR, year);
        info.set(EditorTag.GENRE, genre);
        info.set(EditorTag.COMMENT, comment);
        return info;
    }

    @Test(dataProvider = "audioMeta")
    public void testSetTagFields(String testDesc, TagDetails expctTags, int... indicies) {
        audioList.setWorkingDirectory(WORKING_DIR);
        if(indicies.length == 0) {
            return;
        }
        //        audioList.selectTag(indicies[0]); // will always select 1 before multiple
        if(indicies.length > 1) {
            List<Integer> selected = new ArrayList<Integer>();
            for(int i : indicies) {
                selected.add(i);
            }
            audioList.selectTags(selected, (info) -> {
                TagDetails details = info;
                for(EditorTag tag : EditorTag.values()) {
                    assertEquals(details.get(tag), expctTags.get(tag), tag + ": " + testDesc);
                }
            });
        }
        // TODO verify image
        //        Image im = audioList.getAlbumArt();
        //        assertEquals(audioList.getDataForTag(EditorTag.ALBUM_ART_META), "TODO");
    }

    @DataProvider(name = "audioSaveMeta")
    public static Object[][] audioSaveMeta() {
        return new Object[][] {
            {"Save single same name",
                makeTagDetails("testFileA.mp3",
                    "RenamedTestA",
                    "RenamedArtistA",
                    "RenamedAlbumA",
                    "RenamedTestCollection",
                    "05",
                    "2017",
                    "JPop",
                    "RenamedFolderA Comments"),
                1},
            {"Save single diff name",
                makeTagDetails("renamedTestFileA.mp3",
                    "RenamedTestA",
                    "RenamedArtistA",
                    "RenamedAlbumA",
                    "RenamedTestCollection",
                    "05",
                    "2017",
                    "JPop",
                    "RenamedFolderA Comments"),
                1}
        };
    }

    @Test(dataProvider = "audioSaveMeta")
    public void testSaveTags(String testDesc, TagDetails expctTags, int... indicies) {
        try {
            FileUtils.copyDirectory(new File(WORKING_DIR), tempDir);
        }
        catch (IOException e) {
            fail("Unable to copy test resources");
        }

        audioList.setWorkingDirectory(TEMP_DIR);
        audioList.selectTag(1, (info) -> {
        });

        audioList.save(expctTags);

        audioList.setWorkingDirectory(TEMP_DIR);
        audioList.selectTag(1, (info) -> {
            TagDetails details = info;

            for(EditorTag tag : EditorTag.values()) {
                assertEquals(details.get(tag), expctTags.get(tag), tag + testDesc);
            }
        });
    }

    @DataProvider(name = "audioSaveMetaMulti")
    public static Object[][] audioSaveMetaMulti() {
        return new Object[][] {
            {"file name - new",
                EditorTag.FILE_NAME,
                "new file name.mp3",
                "testFileA.mp3",
                "testFileB.mp3",
                1,
                2},
            {"title - new",
                EditorTag.TITLE,
                "new title",
                "",
                "",
                1,
                2},
            {"artist - new",
                EditorTag.ARTIST,
                "new Artist",
                "",
                "",
                1,
                2},
            {"album - new",
                EditorTag.ALBUM,
                "new album",
                "",
                "",
                1,
                2},
            {"album artist - new",
                EditorTag.ALBUM_ARTIST,
                "new album artist",
                "",
                "",
                1,
                2},
            {"track - new",
                EditorTag.TRACK,
                "07",
                "",
                "",
                1,
                2},
            {"genre - new",
                EditorTag.GENRE,
                "JPop",
                "",
                "",
                1,
                2},
            {"year - new",
                EditorTag.YEAR,
                "2017",
                "",
                "",
                1,
                2},
            {"comment - new",
                EditorTag.COMMENT,
                "gibberish comment",
                "",
                "",
                1,
                2},
            {"file name - diff",
                EditorTag.FILE_NAME,
                Constants.KEYWORD_DIFF_VALUE,
                "testFileA.mp3",
                "testFileB.mp3",
                1,
                2},
            {"title - diff",
                EditorTag.TITLE,
                Constants.KEYWORD_DIFF_VALUE,
                "TestA",
                "TestB",
                1,
                2},
            {"artist - diff",
                EditorTag.ARTIST,
                Constants.KEYWORD_DIFF_VALUE,
                "ArtistA",
                "ArtistA",
                1,
                2},
            {"album - diff",
                EditorTag.ALBUM,
                Constants.KEYWORD_DIFF_VALUE,
                "AlbumA",
                "AlbumA",
                1,
                2},
            {"album artisst - diff",
                EditorTag.ALBUM_ARTIST,
                Constants.KEYWORD_DIFF_VALUE,
                "TestCollection",
                "TestCollection",
                1,
                2},
            {"track - diff",
                EditorTag.TRACK,
                Constants.KEYWORD_DIFF_VALUE,
                "01",
                "02",
                1,
                2},
            {"genre - diff",
                EditorTag.GENRE,
                Constants.KEYWORD_DIFF_VALUE,
                "Anime",
                "Anime",
                1,
                2},
            {"year - diff",
                EditorTag.YEAR,
                Constants.KEYWORD_DIFF_VALUE,
                "2011",
                "2011",
                1,
                2},
            {"comment - diff",
                EditorTag.COMMENT,
                Constants.KEYWORD_DIFF_VALUE,
                "FolderA Comments",
                "FolderA Comments",
                1,
                2}
            // No need to test selecting folder at it gets converted to indicies 
        };
    }

    @Test(dataProvider = "audioSaveMetaMulti")
    public void testSaveMultiTags(String testDesc, EditorTag tag, String newVal, String origVal1, String origVal2, int index1, int index2) {
        List<Integer> indicies = new ArrayList<Integer>();
        indicies.add(index1);
        indicies.add(index2);

        try {
            FileUtils.copyDirectory(new File(WORKING_DIR), tempDir);

            audioList.setWorkingDirectory(TEMP_DIR);
            audioList.selectTags(indicies, (info) -> {
                info.set(tag, newVal);
                audioList.save(info);

                audioList.setWorkingDirectory(TEMP_DIR); // refresh the data // shouldnt need to in future

                audioList.selectTag(index1, (info2) -> {
                    if(newVal.equals(Constants.KEYWORD_DIFF_VALUE) || tag == EditorTag.FILE_NAME) {
                        assertEquals(info2.get(tag), origVal1, "arg1 - " + testDesc); // value that should stay original
                    }
                    else {
                        assertEquals(info2.get(tag), newVal, "arg1 - " + testDesc);
                    }
                });

                audioList.selectTag(index2, (info3) -> {
                    if(newVal.equals(Constants.KEYWORD_DIFF_VALUE) || tag == EditorTag.FILE_NAME) {
                        assertEquals(info3.get(tag), origVal2, "arg2 - " + testDesc); // value that should stay original
                    }
                    else {
                        assertEquals(info3.get(tag), newVal, "arg2 - " + testDesc);
                    }
                });
            });


            FileUtils.deleteDirectory(tempDir);
        }
        catch (IOException e) {
            fail("Unable to copy test resources");
        }
    }
    
    @Test
    public void testGetNonHeadersOnly() {
        audioList.setWorkingDirectory(WORKING_DIR + FOLDER_A);
        audioList.selectTags(Arrays.asList(0), (info)-> {});
        List<Integer> indicies = audioList.removeHeaderIndicies();
        assertEquals(indicies.size(), 2);
        
        audioList.selectTags(Arrays.asList(0, 1, 2), (info)-> {});
        indicies = audioList.removeHeaderIndicies();
        assertEquals(indicies.size(), 2);
    }

    @Test
    public void testSaveByHeader() {
        try {
            FileUtils.copyDirectory(new File(WORKING_DIR), tempDir);
        }
        catch (IOException e) {
            fail("Unable to copy test resources");
        }

        audioList.setWorkingDirectory(TEMP_DIR);
        audioList.selectTag(0, (info) -> {
        });

        TagDetails expectedTags = makeTagDetails("testFileA.mp3",
            "testA",
            "ArtistA",
            "AlbumA",
            "TestCollection",
            "02",
            "2011",
            "Anime",
            "FolderA Comments");
        audioList.save(expectedTags);

        audioList.setWorkingDirectory(TEMP_DIR);
        audioList.selectTag(1, (info) -> {
            TagDetails details = info;

            for(EditorTag tag : EditorTag.values()) {
                assertEquals(details.get(tag), expectedTags.get(tag), "failed to save for header");
            }
        });
    }

    @Test
    public void testKeywords() {
        TagBase<?>[] tags = {EditorTag.ALBUM,
            EditorTag.ALBUM_ARTIST,
            EditorTag.ARTIST,
            EditorTag.COMMENT,
            EditorTag.FILE_NAME,
            EditorTag.GENRE,
            EditorTag.TITLE,
            EditorTag.TRACK,
            EditorTag.YEAR};
        List<TagBase<?>> expected = Arrays.asList(tags);

        assertEquals(audioList.getDisplayKeywordTagClassName(), "Audio");
        assertEquals(keywordsTest(audioList.getKeywordTags(), expected), "");
    }
}
