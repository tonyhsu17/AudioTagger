package modules.controllers;

import javafx.scene.image.Image;
import modules.controllers.base.InformationBase;
import modules.controllers.base.TagBase;
import modules.database.AudioTaggerDB;
import modules.database.TableBase;
import modules.database.tables.*;
import org.tonyhsu17.utilities.Logger;
import support.structure.TagDetails;
import support.util.StringUtil;
import support.util.Utilities.EditorTag;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



/**
 * @author Ikersaro
 * Group = More than one artist
 * Artist = Single artist or Group Name
 */
public class DatabaseController implements InformationBase, Logger {
    AudioTaggerDB db;


    public enum AdditionalTag implements TagBase<AdditionalTag> {
        REPLACE_WORD, NON_CAPITALIZED
    }

    /**
     * @param dbName Name of db, empty for default
     * @throws SQLException
     */
    public DatabaseController(String dbName) throws SQLException {
        db = new AudioTaggerDB(dbName);
    }

    /**
     * Check if value in db
     *
     * @param table  TableNames
     * @param values <br>
     *               TableNames.Artist = "firstName", "lastName"
     *               <br>
     *               TableNames.Anime = "animeName"
     *               <br>
     *               TableNames.Group = "groupName"
     */
    public boolean containsCaseSensitive(TableBase table, Object... values) {
        return db.contains(table, values);
    }

    public void resetDatabase() {
        db.resetTables();
    }

    @Override
    public String getDisplayKeywordTagClassName() {
        return "Database";
    }

    @Override
    public Image getAlbumArt() {
        return null;
    }

    @Override
    public void save(TagDetails details) {
    }

    @Override
    public void setAlbumArt(Object obj) {
    }

    @Override
    public TagBase<?>[] getAdditionalTags() {
        return AdditionalTag.values();
    }

    @Override
    public List<TagBase<?>> getKeywordTags() {
        List<TagBase<?>> keywords = new ArrayList<>();
        keywords.add(EditorTag.ARTIST);
        keywords.add(EditorTag.ALBUM_ARTIST);
        return keywords;
    }

    @Override
    public String getDataForTag(TagBase<?> tag, String... extraArgs) {
        String results = "";
        if(tag == EditorTag.ALBUM_ARTIST && extraArgs.length == 1) {
            results = db.getAnimeInDB(extraArgs[0]);
        }
        else if(tag == EditorTag.ARTIST && extraArgs.length == 2) {
            results = db.getArtist(extraArgs[0], extraArgs[1]);
        }
        else if(tag == AdditionalTag.REPLACE_WORD && extraArgs.length == 1) {
            results = db.getReplacementWord(extraArgs[0]);
        }
        else if(tag == AdditionalTag.NON_CAPITALIZED && extraArgs.length == 1) {
            results = db.getNonCapitalizedWord(extraArgs[0]);
        }
        return results;
    }

    @Override
    public void setDataForTag(TagBase<?> tag, String... values) {
        debug("Setting : " + tag + " with " + Arrays.toString(values));
        if(tag == EditorTag.ALBUM_ARTIST && values.length > 0 && !values[0].isEmpty()) {
            db.add(AlbumArtist.instance(), values[0]);
        }
        else if(tag == EditorTag.ARTIST) {
            if(values.length == 1) {
                // param first, last
                String[] fullName = StringUtil.splitName(values[0]);
                if(!fullName[0].isEmpty()) {
                    db.add(Artist.instance(), fullName[0], fullName[1]);
                }
            }
            else {
                db.add(GroupArtist.instance(), (Object[])values);
            }
        }
        else if(tag == AdditionalTag.REPLACE_WORD) {
            db.add(WordReplacement.instance(), (Object[])values);
        }
        else if(tag == AdditionalTag.NON_CAPITALIZED) {
            db.add(NonCapitalization.instance(), values[0]);
        }

    }

    @Override
    public List<String> getPossibleDataForTag(TagBase<?> tag, String values) {
        List<String> returnValue = null;
        if(tag == EditorTag.ARTIST) {
            String[] fullName = StringUtil.splitName(values);
            returnValue = db.getResultsForArtist(fullName[0], fullName[1]);
        }
        else if(tag == EditorTag.ALBUM_ARTIST) {
            returnValue = db.getResultsForAnime(values);
        }
        else if(tag == AdditionalTag.REPLACE_WORD) {

        }
        else if(tag == AdditionalTag.NON_CAPITALIZED) {

        }
        return returnValue;
    }
}
