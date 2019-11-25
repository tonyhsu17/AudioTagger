package modules.database;

import modules.database.tables.*;
import modules.database.tables.AlbumArtist.Fields;
import org.tonyhsu17.utilities.Logger;
import support.util.StringUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



public class AudioTaggerDB extends Database implements Logger {

    public AudioTaggerDB(String dbName) throws SQLException {
        super(dbName);
    }

    /**
     * Retrieve artist if in database
     * 
     * @param firstName First name of artist
     * @param lastName Last name of artist
     * @return First and last name of artist if it exist in db.
     */
    public String getArtist(String firstName, String lastName) {
        if(firstName == null || firstName.isEmpty()) {
            return "";
        }
        else {
            firstName = firstName.toLowerCase(); // ignore case for comparing
            lastName = lastName.toLowerCase();

            String query = String.format("SELECT %s, %s, %s FROM %s WHERE LOWER(%s) = ? AND LOWER(%s) = ?",
                Artist.Fields.ARTIST_FIRST.fieldName(), Artist.Fields.ARTIST_LAST.fieldName(),
                Artist.Fields.ID.fieldName(), Artist.instance().tableName(), Artist.Fields.ARTIST_FIRST.fieldName(),
                Artist.Fields.ARTIST_LAST.fieldName());
            ResultSet rs = getResults(query, firstName, lastName);

            try {
                if(rs.next()) {
                    String returnVal = (rs.getString(1) + " " + rs.getString(2)).trim();
                    rs.close();
                    return returnVal;
                }
            }
            catch (SQLException e) {
                error(e);
            } ;
        }
        return "";
    }

    /**
     * Search through db to find similar or matching artist
     * 
     * @param firstName First name
     * @param lastName Last name
     * @return List of similar artist
     */
    public List<String> getResultsForArtist(String firstName, String lastName) {
        List<String> possibleArtists = new ArrayList<>();

        if(firstName == null || firstName.isEmpty()) {
            return new ArrayList<String>(); // change to select without where
        }
        else {
            firstName = firstName.toLowerCase(); // ignore case for comparing
            lastName = lastName.toLowerCase();

            ResultSet rs;
            try {
                String artistQuery = String.format("SELECT %s, %s, %s FROM %s " +
                                                   "WHERE LOWER(%s) like ? AND LOWER(%s) like ? " +
                                                   "ORDER BY %s DESC FETCH NEXT 10 ROWS ONLY",
                    Artist.Fields.ARTIST_FIRST.fieldName(), Artist.Fields.ARTIST_LAST.fieldName(),
                    Artist.Fields.ID.fieldName(), Artist.instance().tableName(),
                    Artist.Fields.ARTIST_FIRST.fieldName(), Artist.Fields.ARTIST_LAST.fieldName(),
                    Artist.Fields.USE_FREQUENCY.fieldName());

                String groupQuery = String.format("SELECT %s, %s FROM %s " +
                                                  "WHERE LOWER(%s) like ? " +
                                                  "ORDER BY %s DESC FETCH NEXT 10 ROWS ONLY",
                    GroupArtist.Fields.GROUP_NAME.fieldName(), GroupArtist.Fields.ID.fieldName(),
                    GroupArtist.instance().tableName(), GroupArtist.Fields.GROUP_NAME.fieldName(),
                    GroupArtist.Fields.USE_FREQUENCY.fieldName());


                List<Integer> artistId = new ArrayList<>();
                List<Integer> groupId = new ArrayList<>();

                // Individuals - match: first - dbFirst, last - dbLast
                rs = getResults(artistQuery, firstName, lastName);
                while(rs.next()) {
                    String name = (rs.getString(1) + " " + rs.getString(2)).trim();
                    int id = rs.getInt(3);
                    if(!name.isEmpty() && !artistId.contains(id)) {
                        possibleArtists.add(name);
                        artistId.add(id);
                    }
                }
                rs.close();

                // Individuals - match: last - dbFirst, first - dbLast
                rs = getResults(artistQuery, lastName, firstName);
                while(rs.next()) {
                    String name = (rs.getString(1) + " " + rs.getString(2)).trim();
                    int id = rs.getInt(3);
                    if(!name.isEmpty() && !artistId.contains(id)) {
                        possibleArtists.add(name);
                        artistId.add(id);
                    }
                }
                rs.close();

                // Individuals - fuzzy: first - dbFirst, last - dbLast
                rs = getResults(artistQuery, firstName + '%', lastName + '%');
                while(rs.next()) {
                    String name = (rs.getString(1) + " " + rs.getString(2)).trim();
                    int id = rs.getInt(3);
                    if(!name.isEmpty() && !artistId.contains(id)) {
                        possibleArtists.add(name);
                        artistId.add(id);
                    }
                }
                rs.close();

                // Individuals - fuzzy: last - dbFirst, first - dbLast
                rs = getResults(artistQuery, lastName + '%', firstName + '%');
                while(rs.next()) {
                    String name = (rs.getString(1) + " " + rs.getString(2)).trim();
                    int id = rs.getInt(3);
                    if(!name.isEmpty() && !artistId.contains(id)) {
                        possibleArtists.add(name);
                        artistId.add(id);
                    }
                }
                rs.close();

                // Group name - fuzzy: first + last
                rs = getResults(groupQuery, '%' + firstName + " " + lastName + '%');
                while(rs.next()) {
                    String name = (rs.getString(1)).trim();
                    int id = rs.getInt(2);
                    if(!name.isEmpty() && !groupId.contains(id)) {
                        possibleArtists.add(name);
                        groupId.add(id);
                    }
                }
                rs.close();

                // Group name - first only
                rs = getResults(groupQuery, '%' + firstName + '%');
                while(rs.next()) {
                    String name = (rs.getString(1)).trim();
                    int id = rs.getInt(2);
                    if(!name.isEmpty() && !groupId.contains(id)) {
                        possibleArtists.add(name);
                        groupId.add(id);
                    }
                }
                rs.close();

                // search for groups based on potential artist ids
                // Group by artist
                for(int id : artistId) {
                    // for each artistId, grab groupId that the artist is in
                    String groupLookupQuery = String.format("SELECT %s FROM %s WHERE %s = ? FETCH NEXT 10 ROWS ONLY",
                        ArtistToGroup.Fields.GROUP_ID.fieldName(), ArtistToGroup.instance().tableName(),
                        ArtistToGroup.Fields.ARTIST_ID.fieldName());
                    rs = getResults(groupLookupQuery, id);
                    while(rs.next()) {
                        // for each groupId found, get the name
                        int tempGroupId = rs.getInt(1);
                        if(!groupId.contains(tempGroupId)) {
                            String groupNameQuery = String.format("SELECT %s, %s FROM %s WHERE %s = ?",
                                GroupArtist.Fields.GROUP_NAME.fieldName(), GroupArtist.Fields.ID.fieldName(),
                                GroupArtist.instance().tableName(), GroupArtist.Fields.ID.fieldName());
                            ResultSet innerRS = getResults(groupNameQuery, String.valueOf(tempGroupId));
                            if(innerRS.next()) {
                                String name = (innerRS.getString(1)).trim();
                                possibleArtists.add(name);
                                groupId.add(tempGroupId);
                            }
                            innerRS.close();
                        }
                    }
                    rs.close();
                }
                rs.close();
            }
            catch (SQLException e) {
                error(e);
            }
        }
        return possibleArtists;
    }

    /**
     * Get the Anime name from DB. Uses AlbumArtist
     * 
     * @param anime
     * @return
     */
    public String getAnimeInDB(String anime) {
        String result = "";
        if(anime == null || anime.isEmpty()) {
            return result;
        }
        else {
            anime = anime.toLowerCase(); // ignore case for comparing
            String query = String.format("SELECT %s FROM %s WHERE LOWER(%s) = ?",
                AlbumArtist.Fields.ANIME_NAME.fieldName(), AlbumArtist.instance().tableName(),
                AlbumArtist.Fields.ANIME_NAME.fieldName());
            ResultSet rs;
            try {
                // Individuals: first - dbFirst, last - dbLast
                rs = getResults(query, anime);
                if(rs.next()) {
                    result = rs.getString(1).trim();
                }
                rs.close();
            }
            catch (SQLException e) {
                error(e);
            }
        }
        return result;
    }

    /**
     * Get possible Anime. Uses AlbunArtist
     * 
     * @param compareValue value to match
     * @return
     */
    public List<String> getResultsForAnime(String compareValue) {
        List<String> possibleAnimes = new ArrayList<>();

        if(compareValue == null || compareValue.isEmpty()) {
            return new ArrayList<String>(); // change to select without where
        }
        else {
            compareValue = compareValue.toLowerCase(); // ignore case for comparing
            try {
                // Individuals
                String query = String.format("SELECT %s FROM %s WHERE LOWER(%s) LIKE ? " +
                                             "ORDER BY %s DESC FETCH NEXT 10 ROWS ONLY",
                    AlbumArtist.Fields.ANIME_NAME.fieldName(), AlbumArtist.instance().tableName(),
                    AlbumArtist.Fields.ANIME_NAME.fieldName(), Fields.USE_FREQUENCY.fieldName());
                ResultSet rs = getResults(query, '%' + compareValue + '%');
                while(rs.next()) {
                    possibleAnimes.add((rs.getString(1)).trim());
                }
                rs.close();
            }
            catch (SQLException e) {
                error(e);
            }
        }
        return possibleAnimes;
    }

    /**
     * Get preferred word choice for word
     * 
     * @param before
     * @return
     */
    public String getReplacementWord(String before) {
        String result = "";
        if(before == null || before.isEmpty()) {
            return result;
        }
        else {
            before = before.toLowerCase(); // ignore case for comparing
            ResultSet rs;
            try {
                // Individuals: first - dbFirst, last - dbLast
                String query = String.format("SELECT %s FROM %s WHERE LOWER(%s) = ? ORDER BY %s DESC FETCH FIRST ROW ONLY",
                    WordReplacement.Fields.AFTER.fieldName(), WordReplacement.instance().tableName(),
                    WordReplacement.Fields.BEFORE.fieldName(), WordReplacement.Fields.USE_FREQUENCY);
                rs = getResults(query, before);
                if(rs.next()) {
                    result = rs.getString(1).trim();
                }
                rs.close();
            }
            catch (SQLException e) {
                error(e);
            }
        }
        return result;
    }

    public String getNonCapitalizedWord(String word) {
        String result = "";
        if(word == null || word.isEmpty()) {
            return result;
        }
        else {
            ResultSet rs;
            try {
                // Individuals: first - dbFirst, last - dbLast
                String query = String.format("SELECT %s FROM %s WHERE %s = ?",
                    NonCapitalization.Fields.WORD.fieldName(), NonCapitalization.instance().tableName(),
                    NonCapitalization.Fields.WORD.fieldName());
                rs = getResults(query, word);
                if(rs.next()) {
                    result = rs.getString(1).trim();
                }
                rs.close();
            }
            catch (SQLException e) {
                error(e);
            }
        }
        return result;
    }

    /**
     * Inserts a new value into table or increases use frequency. If it is a group 
     * Prepares values to execute insert, will convert group artist into idHash
     * 
     * @param table {@link TableBase} or {@link AlbumArtist}, {@link Artist}, {@link ArtistToGroup}, {@link GroupArtist},
     *        {@link WordReplacement}
     * @param values
     *        
     */
    public void add(TableBase table, Object... values) {
        //      System.out.println("--DB: Add TableNames." + table + " values: " + Arrays.toString(values));
        int id = getId(table, values);
        if(id == -1) { // if no id found, insert new row
            // special case for GroupArtist, rest are normal
            if(table instanceof GroupArtist) {
                String idHash = getGroupHash(values);
                debug(idHash);
                List<String> typeCasted = Arrays.asList((String[])values);
                id = insertAndRetrieveId(table, StringUtil.getCommaSeparatedStringWithAnd(typeCasted), idHash, 0);

                // populate lookup table
                for(int i : decodeGroupHash(idHash)) {
                    insert(ArtistToGroup.instance(), i, idHash);
                }
            }
            // don't pass in values directly so extra's won't matter
            else if(table instanceof AlbumArtist) {
                insertAndRetrieveId(table, values[0], 0);
            }
            else if(table instanceof Artist) {
                if(values.length == 1) {
                    insertAndRetrieveId(table, values[0], 0);
                }
                else {
                    insertAndRetrieveId(table, values[0], values[1], 0);
                }
            }
            else if(table instanceof ArtistToGroup) {
                insert(table, values[0], values[1]);
            }
            else if(table instanceof WordReplacement) {
                insert(table, values[0], values[1], 0);
            }
            else if(table instanceof NonCapitalization) {
                String result = getNonCapitalizedWord((String)values[0]);
                if(result.isEmpty()) {
                    insert(table, values[0]);
                }
            }
        }
        else if(id != -1 && table.id() != null) { // if id found
            // execute update statement
            updateUseFrequency(table, id);
        }
        try {
            conn.commit();
        }
        catch (SQLException e) {
            error(e);
        }
    }

    /**
     * Increase use frequency of id by 1
     * 
     * @param table {@link TableBase}
     * @param id
     */
    private void updateUseFrequency(TableBase table, int id) {
        try {
            String query = "SELECT %s FROM %s where %s = ? FETCH FIRST ROWS ONLY";
            FieldBase idField = null;
            FieldBase useFreqField = null;
            String tableName = null;
            if(table instanceof AlbumArtist) {
                idField = AlbumArtist.Fields.ID;
                useFreqField = AlbumArtist.Fields.USE_FREQUENCY;
                tableName = AlbumArtist.instance().tableName();
            }
            else if(table instanceof Artist) {
                idField = Artist.Fields.ID;
                useFreqField = Artist.Fields.USE_FREQUENCY;
                tableName = Artist.instance().tableName();
            }
            else if(table instanceof GroupArtist) {
                idField = GroupArtist.Fields.ID;
                useFreqField = GroupArtist.Fields.USE_FREQUENCY;
                tableName = GroupArtist.instance().tableName();
            }
            else if(table instanceof WordReplacement) {
                idField = WordReplacement.Fields.ID;
                useFreqField = WordReplacement.Fields.USE_FREQUENCY;
                tableName = WordReplacement.instance().tableName();
            }
            query = String.format(query,
                useFreqField.fieldName(), tableName, idField.fieldName());

            // get current useFrequency 
            ResultSet rs = getResults(query, id);
            if(rs.next()) {
                // increase useFrequency count by one
                update(table, useFreqField, rs.getInt(1) + 1, idField, id);
            }
            rs.close();
        }
        catch (SQLException e) {
            error(e);
        }
    }

    private int insertAndRetrieveId(TableBase table, Object... values) {
        debug("Table: " + table + " inserting with: " + Arrays.toString(values));
        int id = getNextUsableId(table);
        Object[] fullVal;
        boolean sucess = true;
        if(id == -1) {
            return -1;
        }
        if(table instanceof AlbumArtist) {
            fullVal = new Object[] {id, values[0], values[1]};
            sucess &= insert(table, fullVal);
        }
        else if(table instanceof Artist) {
            fullVal = new Object[] {id, values[0], values[1], values[2]};
            sucess &= insert(table, fullVal);
        }
        else if(table instanceof GroupArtist) {
            fullVal = new Object[] {id, values[0], values[1], values[2]};
            sucess &= insert(table, fullVal);
        }
        return sucess ? id : -1;
    }

    /**
     * @param values <br>
     *        TableNames.Artist = "firstName", "lastName"
     *        <br>
     *        TableNames.Anime = "animeName"
     *        <br>
     *        TableNames.Group = "groupName", "artistName1", artistNameX...
     *        <br>
     *        TableNames.ArtistToGroup = "artistId", "groupId"
     * @param table
     * @param values
     * @return
     */
    @SuppressWarnings("unchecked")
    private int getId(TableBase table, Object... values) {
        debug(table.tableName() + " with params: " + Arrays.toString(values));
        int id;
        if(table instanceof Artist) {
            id = getId(table,
                new AbstractMap.SimpleEntry<>(Artist.Fields.ARTIST_FIRST, values[0]),
                new AbstractMap.SimpleEntry<>(Artist.Fields.ARTIST_LAST, values[1]));
        }
        else if(table instanceof AlbumArtist) {
            id = getId(table, new AbstractMap.SimpleEntry<>(AlbumArtist.Fields.ANIME_NAME, values[0]));
        }
        else if(table instanceof GroupArtist) {
            String groupHash = getGroupHash(values);
            id = getId(table, new AbstractMap.SimpleEntry<>(GroupArtist.Fields.ARTIST_ID_HASH, groupHash));
        }
        else if(table instanceof WordReplacement) {
            id = getId(table,
                new SimpleEntry<>(WordReplacement.Fields.BEFORE, values[0]),
                new SimpleEntry<>(WordReplacement.Fields.AFTER, values[1]));
        }
        else {
            id = -1;
        }
        debug("Table: " + table + " for values " + Arrays.toString(values) + " got id: " + id);
        return id;
    }

    /**
     * @param table DatabaseController.TableNames
     * @return next free id
     */
    private int getNextUsableId(TableBase table) {
        int usableId = -1;
        //        try {
        String query = "SELECT id FROM " + table.tableName() + " ORDER BY id DESC FETCH FIRST ROW ONLY";
        try {
            ResultSet rs = getResults(query);
            if(rs.next()) { // if no results found, then start with initial number
                usableId = rs.getInt(1) + 1;
            }
            else {
                usableId = 0;
            }
        }
        catch (SQLException e) {
            error(e);
        }
        debug(usableId);
        return usableId;
    }

    /**
     * Check if value in db
     * 
     * @param table {@link TableBase}
     * @param values
     */
    public boolean contains(TableBase table, Object... values) {
        return getId(table, values) != -1 ? true : false;
    }

    /**
     * Generates the hash value for the artists.
     * Will add in new artist entry in db if artist does not exist
     * 
     * @param values Format: [Artist1 (full name), Artist2 (full name), ...]
     * @return Hash of artists
     */
    private String getGroupHash(Object[] values) {
        // create idHash
        StringBuffer idHash = new StringBuffer("");
        List<Integer> artistIds = new ArrayList<Integer>();

        // if given group name, try to split it by the artists
        if(values.length == 1) {
            values = StringUtil.splitBySeparators((String)values[0]);
        }

        for(int i = 0; i < values.length; i++) {
            String[] splitName = StringUtil.splitName((String)values[i]);
            @SuppressWarnings("unchecked")
            int artistId = getId(Artist.instance(),
                new AbstractMap.SimpleEntry<FieldBase, Object>(Artist.Fields.ARTIST_FIRST, splitName[0]),
                new AbstractMap.SimpleEntry<FieldBase, Object>(Artist.Fields.ARTIST_LAST, splitName[1]));
            if(artistId == -1) {// if artist not found
                // add new entry for artist
                artistId = insertAndRetrieveId(Artist.instance(), splitName[0], splitName[1], 0);
            }
            idHash.append(artistId + "-");
            artistIds.add(artistId);
        }
        return idHash.toString();
    }

    /**
     * Decode group id hash into artist ids
     * 
     * @param hash GroupHash format is #-#-...-
     * @return List of artist ids
     */
    private List<Integer> decodeGroupHash(String hash) {
        List<Integer> ids = new ArrayList<Integer>();
        String[] splitId = hash.split("-");
        for(String id : splitId) {
            try {
                ids.add(Integer.parseInt(id));
            }
            catch (NumberFormatException e) {
                // dont do anything with bad numbers
            }
        }
        return ids;
    }

    // DEBUG USE
    public void dropAllTables() {
        info("Deleting All Tables");
        try {
            statement.execute("DROP TABLE " + ArtistToGroup.instance().tableName());
            statement.execute("DROP TABLE " + GroupArtist.instance().tableName());
            statement.execute("DROP TABLE " + Artist.instance().tableName());
            statement.execute("DROP TABLE " + AlbumArtist.instance().tableName());
            statement.execute("DROP TABLE " + WordReplacement.instance().tableName());
            conn.commit();
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // DEBUG USE
    public void resetTables() {
        dropAllTables();
        try {
            initializeTables();
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public List<TableBase> getTables() {
        return Arrays.asList(new TableBase[] {AlbumArtist.instance(),
            Artist.instance(),
            GroupArtist.instance(),
            ArtistToGroup.instance(),
            WordReplacement.instance(),
            NonCapitalization.instance()});
    }
}
