package modules.controllers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.scene.image.Image;
import model.base.InformationBase;
import model.base.TagBase;
import modules.database.tables.AlbumArtist;
import modules.database.tables.Artist;
import modules.database.tables.ArtistToGroup;
import modules.database.tables.GroupArtist;
import modules.database.tables.WordReplacement;
import support.Logger;
import support.structure.TagDetails;
import support.util.StringUtil;
import support.util.Utilities.EditorTag;



/**
 * @author Ikersaro
 *         Group = More than one artist
 *         Artist = Single artist or Group Name
 */
public class DatabaseController implements InformationBase, Logger {
    private Statement statement;
    private Connection conn = null;

    //    
    public enum Table {
        ALBUM_ARTIST,
        ARTIST,
        GROUP_ARTIST,
        ARTIST_TO_GROUP,
        WORD_REPLACEMENT;
    }

    public enum AdditionalTag implements TagBase<AdditionalTag> {
        REPLACE_WORD
    }

    /**
     * @param name Name of db, empty for default
     */
    public DatabaseController(String name) {
        initializeDB(name.isEmpty() ? "defaultDB" : name);
        initializeTables();
    }

    private void initializeDB(String name) {
        String driver = "org.apache.derby.jdbc.EmbeddedDriver";
        String dbName = name; // the name of the database
        String protocol = "jdbc:derby:";

        try {
            Class.forName(driver).newInstance();
            conn = DriverManager.getConnection(protocol + dbName + ";create=true", null);
            if(conn != null) {
                info("Connected to the database: " + dbName);
            }
            // We want to control transactions manually. Autocommit is on by default in JDBC.
            //            conn.setAutoCommit(false);
            statement = conn.createStatement();
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void initializeTables() {
        boolean sucess = true;
        sucess &= createTableIfNotExist(Table.ALBUM_ARTIST +
                                        "(" +
                                        AlbumArtist.ID +
                                        " INT PRIMARY KEY," +
                                        AlbumArtist.ANIME_NAME +
                                        " VARCHAR(255) UNIQUE," +
                                        AlbumArtist.USE_FREQUENCY +
                                        " INT)");
        //        dumpTable(Table.ANIME);
        sucess &= createTableIfNotExist(Table.ARTIST +
                                        "(" +
                                        Artist.ID +
                                        " INT PRIMARY KEY," +
                                        Artist.ARTIST_FIRST +
                                        " VARCHAR(255)," +
                                        Artist.ARTIST_LAST +
                                        " VARCHAR(255)," +
                                        Artist.USE_FREQUENCY +
                                        " INT," +
                                        " UNIQUE (" +
                                        Artist.ARTIST_FIRST +
                                        ", " +
                                        Artist.ARTIST_LAST +
                                        "))");
        //        dumpTable(Table.ARTIST);
        sucess &= createTableIfNotExist(Table.GROUP_ARTIST +
                                        "(" +
                                        GroupArtist.ID +
                                        " INT PRIMARY KEY," +
                                        GroupArtist.GROUP_NAME +
                                        " VARCHAR(255)," +
                                        GroupArtist.ARTIST_ID_HASH +
                                        " VARCHAR(255)," +
                                        GroupArtist.USE_FREQUENCY +
                                        " INT)");
        //        dumpTable(Table.GROUP_ARTIST);
        sucess &= createTableIfNotExist(Table.ARTIST_TO_GROUP +
                                        "(" +
                                        ArtistToGroup.ARTIST_ID +
                                        " INT," +
                                        ArtistToGroup.GROUP_ID +
                                        " INT," +
                                        " PRIMARY KEY (" +
                                        ArtistToGroup.ARTIST_ID +
                                        ", " +
                                        ArtistToGroup.GROUP_ID +
                                        ")," +
                                        " FOREIGN KEY (" +
                                        ArtistToGroup.ARTIST_ID +
                                        ") REFERENCES " +
                                        Table.ARTIST +
                                        "(" +
                                        Artist.ID +
                                        ")," +
                                        " FOREIGN KEY (" +
                                        ArtistToGroup.GROUP_ID +
                                        ") REFERENCES " +
                                        Table.GROUP_ARTIST +
                                        "(" +
                                        GroupArtist.ID +
                                        "))");
        //        dumpTable(Table.ARTIST_TO_GROUP);
        sucess &= createTableIfNotExist(Table.WORD_REPLACEMENT +
                                        "(" +
                                        WordReplacement.BEFORE +
                                        " VARCHAR(255) PRIMARY KEY," +
                                        WordReplacement.AFTER +
                                        " VARCHAR(255))");
        //        dumpTable(Table.WORD_REPLACEMENT);
        if(sucess) {
            info("Connected to db");
        }
    }

    private boolean createTableIfNotExist(String tableSchema) {
        try {
            statement.execute("CREATE TABLE " + tableSchema);
        }
        catch (SQLException e) {
            if(e.getSQLState().equals("X0Y32")) // table already exist
            {
                return true;
            }
            else {
                error(e.getMessage());
                error("schema: " + tableSchema);
                return false;
            }
        }
        return true;
    }

    public void cleanup() {
        try {
            conn.commit();
            conn.close();
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Retrieve artist if in database
     * 
     * @param firstName First name of artist
     * @param lastName Last name of artist
     * @return First and last name of artist if it exist in db.
     */
    private String getArtist(String firstName, String lastName) {
        if(firstName == null || firstName.isEmpty()) {
            return "";
        }
        else {
            firstName = firstName.toLowerCase(); // ignore case for comparing
            lastName = lastName.toLowerCase();

            PreparedStatement statements;
            ResultSet rs;
            try {
                // Individuals: first - dbFirst, last - dbLast
                statements = conn.prepareStatement(
                    String.format("SELECT %s, %s, %s FROM %s WHERE LOWER(%s) = ? AND LOWER(%s) = ?",
                        Artist.ARTIST_FIRST, Artist.ARTIST_LAST, Artist.ID,
                        Table.ARTIST, Artist.ARTIST_FIRST, Artist.ARTIST_LAST));
                statements.setString(1, firstName);
                statements.setString(2, lastName);
                rs = statements.executeQuery();
                if(rs.next()) {
                    return (rs.getString(1) + " " + rs.getString(2)).trim();
                }
            }
            catch (SQLException e) {

            }
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
    private List<String> getResultsForArtist(String firstName, String lastName) {
        List<String> possibleArtists = new ArrayList<>();

        if(firstName == null || firstName.isEmpty()) {
            return new ArrayList<String>(); // change to select without where
        }
        else {
            firstName = firstName.toLowerCase(); // ignore case for comparing
            lastName = lastName.toLowerCase();

            PreparedStatement statements;
            ResultSet rs;
            try {
                String artistQuery = String.format("SELECT %s, %s, %s FROM %s " +
                                                   "WHERE LOWER(%s) like ? AND LOWER(%s) like ? " +
                                                   "ORDER BY %s DESC FETCH NEXT 10 ROWS ONLY",
                    Artist.ARTIST_FIRST, Artist.ARTIST_LAST, Artist.ID, Table.ARTIST,
                    Artist.ARTIST_FIRST, Artist.ARTIST_LAST, Artist.USE_FREQUENCY);
                String groupQuery = String.format("SELECT %s, %s FROM %s " +
                                                  "WHERE LOWER(%s) like ? " +
                                                  "ORDER BY %s DESC FETCH NEXT 10 ROWS ONLY",
                    GroupArtist.GROUP_NAME, GroupArtist.ID, Table.GROUP_ARTIST,
                    GroupArtist.GROUP_NAME, GroupArtist.USE_FREQUENCY);
                List<Integer> artistId = new ArrayList<>();
                List<Integer> groupId = new ArrayList<>();
                // Individuals: first - dbFirst, last - dbLast
                statements = conn.prepareStatement(artistQuery);
                statements.setString(1, firstName + '%');
                statements.setString(2, lastName + '%');
                rs = statements.executeQuery();
                while(rs.next()) {
                    String name = (rs.getString(1) + " " + rs.getString(2)).trim();
                    int id = rs.getInt(3);
                    if(!name.isEmpty() && !artistId.contains(id)) {
                        possibleArtists.add(name);
                        artistId.add(id);
                    }
                }
                rs.close();

                // Individuals: last - dbFirst, first - dbLast
                statements = conn.prepareStatement(artistQuery);
                statements.setString(1, lastName + '%');
                statements.setString(2, firstName + '%');
                rs = statements.executeQuery();
                while(rs.next()) {
                    String name = (rs.getString(1) + " " + rs.getString(2)).trim();
                    int id = rs.getInt(3);
                    if(!name.isEmpty() && !artistId.contains(id)) {
                        possibleArtists.add(name);
                        artistId.add(id);
                    }
                }
                rs.close();

                // Individuals: first - dbFirst, first - dbLast
                statements = conn.prepareStatement(artistQuery);
                statements.setString(1, firstName + '%');
                statements.setString(2, firstName + '%');
                rs = statements.executeQuery();
                while(rs.next()) {
                    String name = (rs.getString(1) + " " + rs.getString(2)).trim();
                    int id = rs.getInt(3);
                    if(!name.isEmpty() && !artistId.contains(id)) {
                        possibleArtists.add(name);
                        artistId.add(id);
                    }
                }
                rs.close();

                // Group name - first + last
                statements = conn.prepareStatement(groupQuery);
                statements.setString(1, '%' + firstName + " " + lastName + '%');
                rs = statements.executeQuery();
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
                statements = conn.prepareStatement(groupQuery);
                statements.setString(1, '%' + firstName + '%');
                rs = statements.executeQuery();
                while(rs.next()) {
                    String name = (rs.getString(1)).trim();
                    int id = rs.getInt(2);
                    if(!name.isEmpty() && !groupId.contains(id)) {
                        possibleArtists.add(name);
                        groupId.add(id);
                    }
                }
                rs.close();

                // Group by artist
                for(int id : artistId) {
                    // for each artistId, grab groupName that the artist is in
                    statements = conn.prepareStatement(
                        String.format("SELECT %s FROM %s WHERE %s = ? FETCH NEXT 10 ROWS ONLY",
                            ArtistToGroup.GROUP_ID, Table.ARTIST_TO_GROUP, ArtistToGroup.ARTIST_ID));
                    statements.setInt(1, id);
                    rs = statements.executeQuery();
                    while(rs.next()) {
                        // for each groupId found, get the name
                        int tempGroupId = rs.getInt(1);
                        if(!groupId.contains(tempGroupId)) {
                            statements = conn.prepareStatement(
                                String.format("SELECT %s, %s FROM %s WHERE %s = ?",
                                    GroupArtist.GROUP_NAME, GroupArtist.ID, Table.GROUP_ARTIST, GroupArtist.ID));
                            statements.setInt(1, tempGroupId);
                            ResultSet innerRS = statements.executeQuery();
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
                error("termination");
                e.printStackTrace();
                System.exit(1);
            }
        }
        return possibleArtists;
    }

    private String getAnimeInDB(String anime) {
        String result = "";
        if(anime == null || anime.isEmpty()) {
            return result;
        }
        else {
            anime = anime.toLowerCase(); // ignore case for comparing

            PreparedStatement statements;
            ResultSet rs;
            try {
                // Individuals: first - dbFirst, last - dbLast
                statements = conn.prepareStatement(
                    String.format("SELECT %s FROM %s WHERE LOWER(%s) = ?", AlbumArtist.ANIME_NAME, Table.ALBUM_ARTIST,
                        AlbumArtist.ANIME_NAME));
                statements.setString(1, anime);
                rs = statements.executeQuery();
                if(rs.next()) {
                    result = rs.getString(1).trim();
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private List<String> getResultsForAnime(String compareValue) {
        List<String> possibleAnimes = new ArrayList<>();

        if(compareValue == null || compareValue.isEmpty()) {
            return new ArrayList<String>(); // change to select without where
        }
        else {
            compareValue = compareValue.toLowerCase(); // ignore case for comparing

            PreparedStatement statements;

            try {
                //                List<Integer> artistId = new ArrayList<>();
                // Individuals
                statements = conn.prepareStatement(
                    String.format("SELECT %s FROM %s WHERE LOWER(%s) LIKE ? " +
                                  "ORDER BY %s DESC FETCH NEXT 10 ROWS ONLY",
                        AlbumArtist.ANIME_NAME, Table.ALBUM_ARTIST, AlbumArtist.ANIME_NAME, AlbumArtist.USE_FREQUENCY));
                statements.setString(1, '%' + compareValue + '%');
                ResultSet rs = statements.executeQuery();
                while(rs.next()) {
                    possibleAnimes.add((rs.getString(1)).trim());
                }
                rs.close();
                //                System.out.println("Animes: " + Arrays.toString(possibleAnimes.toArray(new String[0])));               
            }
            catch (SQLException e) {
                // TODO Auto-generated catch block
                error("termination");
                e.printStackTrace();
                System.exit(1);
            }
        }
        return possibleAnimes;
    }

    private String getReplacementWord(String before) {
        String result = "";
        if(before == null || before.isEmpty()) {
            return result;
        }
        else {
            before = before.toLowerCase(); // ignore case for comparing

            PreparedStatement statements;
            ResultSet rs;
            try {
                // Individuals: first - dbFirst, last - dbLast
                statements = conn.prepareStatement(
                    String.format("SELECT %s FROM %s WHERE LOWER(%s) = ?", WordReplacement.AFTER, Table.WORD_REPLACEMENT,
                        WordReplacement.BEFORE));
                statements.setString(1, before);
                rs = statements.executeQuery();
                if(rs.next()) {
                    result = rs.getString(1).trim();
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Add as new values or increases use count
     * 
     * @param table DatabaseController.TableName
     * @param values <br>
     *        TableNames.Artist = "firstName", "lastName"
     *        <br>
     *        TableNames.Anime = "animeName"
     *        <br>
     *        TableNames.Group = "groupName", "artst1", artistX...
     */
    private void add(Table table, String... values) {
        //      System.out.println("--DB: Add TableNames." + table + " values: " + Arrays.toString(values));
        int id = getId(table, values);
        if(id == -1) { // if no id found
            prepareInsertThenExecute(table, values);
        }
        else if(id != -1 && table != Table.WORD_REPLACEMENT) { // if id found
            // execute update statement
            // currently Word_Replacement table will not allow overrides or updating
            update(table, id);
        }
        try {
            conn.commit();
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Updates use frequency of id
     * 
     * @param table DatabaseController.TableNames
     * @param values id
     */
    private void update(Table table, int id) {
        //        System.out.println("--DB: Update TableNames." + table + " id: " + id);
        PreparedStatement statements;
        try {
            // get current useFrequency TODO find way to abstract it
            statements = conn.prepareStatement("SELECT USE_FREQUENCY, ID FROM " + table + " where Id = ? FETCH FIRST ROWS ONLY");
            //            String.format("SELECT %s, %s FROM %s WHERE %s = ? FETCH ROWS ONLY", args)
            statements.setInt(1, id);
            ResultSet rs = statements.executeQuery();
            if(rs.next()) {
                // increase useFrequency count by one
                statements = conn.prepareStatement("UPDATE " + table + " SET USE_FREQUENCY = ?" + " where ID = ?");
                statements.setInt(1, rs.getInt(1) + 1);
                statements.setInt(2, id);
            }
            rs.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Prepares values to execute insert, will convert group artist into idHash
     * 
     * @param table DatabaseController.TableNames
     * @param values <br>
     *        TableNames.Artist = "firstName", "lastName"
     *        <br>
     *        TableNames.Anime = "animeName"
     *        <br>
     *        TableNames.Group = "groupName", "artistName1", artistNameX...
     *        <br>
     *        TableNames.ArtistToGroup = "artistId", "groupId"
     *        <br>
     *        TableNames.AlbumSearchIgnoreList = "keyword"
     */
    private int prepareInsertThenExecute(Table table, String... values) {
        //        System.out.println("--DB: prepareInsertThenExecute TableNames." + table + " values: " + Arrays.toString(values));
        String id = getNextUsableId(table);
        List<String> fullValues = new ArrayList<String>();
        int insertedId = -1;
        switch (table) {
            case ALBUM_ARTIST: // AnimeName
                if(values.length != 1) {
                    error("Invalid num of args for: " + table + " w/ length: " + values.length);
                }
                fullValues.add(id);
                fullValues.add(values[0]);
                fullValues.add("0");
                insertedId = executeInsert(table, fullValues);
                break;
            case ARTIST: // ArtistFirst, ArtistLast
                if(values.length != 2) {
                    error("Invalid num of args for: " + table + " w/ length: " + values.length);
                }
                fullValues.add(id);
                fullValues.add(values[0]);
                fullValues.add(values[1]);
                fullValues.add("0");
                insertedId = executeInsert(table, fullValues);
                break;
            case ARTIST_TO_GROUP:
                //self called to add this one from group insert
                if(values.length != 2) {
                    error("Invalid num of args for: " + table + " w/ length: " + values.length);
                }

                fullValues.add(values[0]);
                fullValues.add(values[1]);
                executeInsert(table, fullValues);
                break;
            case GROUP_ARTIST:
                if(values.length < 2) {
                    error("Invalid num of args for: " + table + " w/ length: " + values.length);
                }
                String idHash = getGroupHash(values);
                fullValues.add(id);
                fullValues.add(StringUtil.getCommaSeparatedStringWithAnd(Arrays.asList(values)));
                fullValues.add(idHash);
                fullValues.add("0");
                insertedId = executeInsert(table, fullValues);

                // populate lookup table
                for(int i : decodeGroupHash(idHash)) {
                    prepareInsertThenExecute(Table.ARTIST_TO_GROUP, i + "", id + "");
                }
                break;
            case WORD_REPLACEMENT:
                if(values.length != 2) {
                    error("Invlid num of args for: " + table + " w/ length: " + values.length);
                }
                fullValues.add(values[0]);
                fullValues.add(values[1]);
                insertedId = executeInsert(table, fullValues);
                break;
            default:
                break;

        }
        return insertedId;
    }

    /**
     * Executes insert sql. Do not call directly, only called from prepareInsertThenExecute().
     * 
     * @param table DatabaseController.TableNames
     * @param values <br>
     *        TableNames.Artist = "id", "firstName", "lastName", "useFrequency"
     *        <br>
     *        TableNames.Anime = "id", "animeName", "useFrequency"
     *        <br>
     *        TableNames.Group = "id", "groupName", "idHash", "useFrequency"
     *        <br>
     *        TableNames.ArtistToGroup = "artistId", "groupId"
     *        <br>
     *        TableNames.WordReplacement = "before", "after"
     * @return Id of row
     */
    private int executeInsert(Table table, List<String> values) {
        info("inserting in: " + table + " values: " + Arrays.toString(values.toArray(new String[0])));
        try {
            PreparedStatement statements;
            int numCols = values.size();

            statements = conn.prepareStatement("INSERT INTO " + table + " VALUES (" + StringUtil.createQuestionMarks(numCols) + ")");
            switch (table) {
                case ALBUM_ARTIST:
                    if(numCols == 3) {
                        statements.setInt(1, Integer.valueOf(values.get(0)));
                        statements.setString(2, values.get(1));
                        statements.setInt(3, Integer.valueOf(values.get(2)));
                    }
                    break;
                case ARTIST:
                    if(numCols == 4) {
                        statements.setInt(1, Integer.valueOf(values.get(0)));
                        statements.setString(2, values.get(1));
                        statements.setString(3, values.get(2));
                        statements.setInt(4, Integer.valueOf(values.get(3)));
                    }
                    break;
                case ARTIST_TO_GROUP:
                    if(numCols == 2) {
                        statements.setInt(1, Integer.valueOf(values.get(0)));
                        statements.setInt(2, Integer.valueOf(values.get(1)));
                    }
                    break;
                case GROUP_ARTIST:
                    if(numCols == 4) {
                        statements.setInt(1, Integer.valueOf(values.get(0)));
                        statements.setString(2, values.get(1));
                        statements.setString(3, values.get(2));
                        statements.setInt(4, Integer.valueOf(values.get(3)));
                    }
                    break;
                case WORD_REPLACEMENT:
                    if(numCols == 2) {
                        statements.setString(1, values.get(0));
                        statements.setString(2, values.get(1));
                    }
                    break;
                default:
                    throw new SQLException("No table found");
            }
            statements.executeUpdate();
            int numInsertted = 1;
            try {
                numInsertted = Integer.valueOf(values.get(0));
            }
            catch (NumberFormatException e) {
                // WORD_REPLACEMENT has no id and will exception will be thrown
            }
            return numInsertted;
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            error("termination inserting in table: " + table);
            dumpTable(table);
            e.printStackTrace();
            System.exit(1);
        }
        return 0;
    }


    /**
     * @param table DatabaseController.TableNames
     * @return next free id
     */
    private String getNextUsableId(Table table) {
        ResultSet rs;
        int usableId = -1;
        try {
            if(table.equals(Table.ALBUM_ARTIST) || table.equals(Table.ARTIST) || table.equals(Table.GROUP_ARTIST)) {
                rs = statement.executeQuery("SELECT Id FROM " + table + " ORDER BY Id DESC FETCH FIRST ROW ONLY");
                if(rs.next()) {
                    usableId = rs.getInt(1) + 1;
                }
                else {
                    usableId = 1;
                }
            }
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //        System.out.println("Next id is: " + usableId + " for table: " + table);
        return usableId + "";
    }

    /**
     * Check if value in db
     * 
     * @param table TableNames
     * @param values <br>
     *        TableNames.Artist = "firstName", "lastName"
     *        <br>
     *        TableNames.Anime = "animeName"
     *        <br>
     *        TableNames.Group = "groupName"
     */
    public boolean containsCaseSensitive(Table table, String... values) {
        return getId(table, values) != -1 ? true : false;
    }

    /**
     * Get Id of value
     * 
     * @param table DatabaseController.TableNames
     * @param values <br>
     *        TableNames.Artist = "firstName", "lastName"
     *        <br>
     *        TableNames.Anime = "animeName"
     *        <br>
     *        TableNames.Group = "groupName"
     */
    private int getId(Table table, String... values) {
        PreparedStatement statements;
        int id = -1;
        //        System.out.println("GetId for TableNames." + table + " " + Arrays.toString(values));
        if(table == Table.ARTIST && values.length == 2) {
            try {
                statements = conn.prepareStatement(
                    String.format("SELECT %s, %s, %s FROM %s WHERE %s = ? AND %s = ? FETCH FIRST ROW ONLY",
                        Artist.ARTIST_FIRST, Artist.ARTIST_LAST, Artist.ID,
                        Table.ARTIST, Artist.ARTIST_FIRST, Artist.ARTIST_LAST));
                statements.setString(1, values[0]);
                statements.setString(2, values[1]);
                ResultSet rs = statements.executeQuery();
                if(rs.next()) {
                    id = rs.getInt(3);
                }
                rs.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        else if(table == Table.GROUP_ARTIST) {
            String hash = "";
            if(values.length == 1) {
                hash = getGroupHash(values[0]);
            } else {
                hash = getGroupHash(values);
            }
            try {
                statements = conn.prepareStatement(
                    String.format("SELECT %s, %s FROM %s WHERE %s = ? FETCH FIRST ROW ONLY",
                        GroupArtist.GROUP_NAME, GroupArtist.ID, Table.GROUP_ARTIST, GroupArtist.ARTIST_ID_HASH));
                statements.setString(1, hash); //
                ResultSet rs = statements.executeQuery();
                if(rs.next()) {
                    id = rs.getInt(2);
                }
                rs.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        else if(table == Table.ALBUM_ARTIST && values.length == 1) {
            try {
                statements = conn.prepareStatement(
                    String.format("SELECT %s, %s FROM %s WHERE %s = ?",
                        AlbumArtist.ANIME_NAME, AlbumArtist.ID, Table.ALBUM_ARTIST, AlbumArtist.ANIME_NAME));
                statements.setString(1, values[0]);
                ResultSet rs = statements.executeQuery();
                if(rs.next()) {
                    id = rs.getInt(2);
                }
                rs.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        else if(table == Table.WORD_REPLACEMENT && values.length >= 1) {
            try {
                statements = conn.prepareStatement(
                    String.format("SELECT %s, %s FROM %s WHERE %s = ?",
                        WordReplacement.BEFORE, WordReplacement.AFTER, Table.WORD_REPLACEMENT, WordReplacement.BEFORE));
                statements.setString(1, values[0]);
                ResultSet rs = statements.executeQuery();
                if(rs.next()) { // return anything so it shows it already exist
                    id = 1;
                }
                rs.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return id;
    }

    /**
     * Generates the hash value for the artists.
     * Will add in new artist entry in db if artist does not exist
     * 
     * @param str Format Artist1, Artist2 & Artist3
     * @return Hash of artists
     */
    private String getGroupHash(String values) {
        // create idHash
        StringBuffer idHash = new StringBuffer("");
        String[] names = StringUtil.splitBySeparators(values);
        for(int i = 0; i < names.length; i++) {
            String[] splitName = StringUtil.splitName(names[i]);
            int artistId = getId(Table.ARTIST, splitName[0], splitName[1]);
            if(artistId == -1) { // if artist not found
                // add new entry for artist
                artistId = prepareInsertThenExecute(Table.ARTIST, splitName[0], splitName[1]);
            }
            else {
                idHash.append(artistId + "-");
            }

        }
        return idHash.toString();
    }

    /**
     * Generates the hash value for the artists.
     * Will add in new artist entry in db if artist does not exist
     * 
     * @param str Format: [Artist1, Artist2, Artist3]
     * @return Hash of artists
     */
    private String getGroupHash(String[] values) {
        // create idHash
        StringBuffer idHash = new StringBuffer("");
        List<Integer> artistIds = new ArrayList<Integer>();
        for(int i = 0; i < values.length; i++) {
            String[] splitName = StringUtil.splitName(values[i]);
            int artistId = getId(Table.ARTIST, splitName[0], splitName[1]);
            if(artistId == -1) {// if artist not found
                // add new entry for artist
                artistId = prepareInsertThenExecute(Table.ARTIST, splitName[0], splitName[1]);
            }
            else {
                idHash.append(artistId + "-");
            }

            artistIds.add(artistId);
        }
        return idHash.toString();
    }

    /**
     * Decode group id hash into artist ids
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
            catch(NumberFormatException e){
               // dont do anything with bad numbers
            }
        }
        return ids;
    }

    public void dumpTable(Table t) {
        PreparedStatement statements;
        try {
            StringBuffer temp = new StringBuffer("");
            statements = conn.prepareStatement("SELECT * FROM " + t);
            ResultSet rs = statements.executeQuery();
            ResultSetMetaData data = rs.getMetaData();


            info("Table Name: " + t + " contents...");

            // header
            temp.append("| ");
            for(int i = 1; i <= data.getColumnCount(); i++) {
                temp.append(data.getColumnLabel(i) + "\t| ");
            }
            info(temp.toString());

            while(rs.next()) {
                temp = new StringBuffer("| ");
                for(int i = 1; i <= data.getColumnCount(); i++) {
                    temp.append(rs.getString(i) + "\t| ");
                }
                info(temp.toString());
            }

            info("| ------------ Table End ------------- |");

            rs.close();
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    // DEBUG USE
    public void deleteAllTables() {
        info("Deleting All Tables");
        try {
            statement.execute("DROP TABLE " + Table.ARTIST_TO_GROUP);
            statement.execute("DROP TABLE " + Table.GROUP_ARTIST);
            statement.execute("DROP TABLE " + Table.ARTIST);
            statement.execute("DROP TABLE " + Table.ALBUM_ARTIST);
            statement.execute("DROP TABLE " + Table.WORD_REPLACEMENT);
            conn.commit();
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
    public void save(TagDetails details) {}

    @Override
    public void setAlbumArt(Object obj) {}

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
            results = getAnimeInDB(extraArgs[0]);
        }
        else if(tag == EditorTag.ARTIST && extraArgs.length == 2) {
            results = getArtist(extraArgs[0], extraArgs[1]);
        }
        else if(tag == AdditionalTag.REPLACE_WORD && extraArgs.length == 1) {
            results = getReplacementWord(extraArgs[0]);
        }
        return results;
    }

    @Override
    public void setDataForTag(TagBase<?> tag, String... values) {
        if(tag == EditorTag.ALBUM_ARTIST && values.length > 0 && !values[0].isEmpty()) {
            add(Table.ALBUM_ARTIST, values[0]);
        }
        else if(tag == EditorTag.ARTIST) {
            if(values.length == 1) {
                // param first, last
                String[] fullName = StringUtil.splitName(values[0]);
                if(!fullName[0].isEmpty()) {
                    add(Table.ARTIST, fullName[0], fullName[1]);
                }
            }
            else {
                // param name, artist1, artist2, ...
                //tagToEditorTextMapping.get(Tag.Artist).get()
                //                System.err.println("GroupSaving not implemented");
                //                System.exit(0);
                add(Table.GROUP_ARTIST, values);
            }
        }
        else if(tag == AdditionalTag.REPLACE_WORD) {
            add(Table.WORD_REPLACEMENT, values);
        }
    }

    @Override
    public List<String> getPossibleDataForTag(TagBase<?> tag, String values) {
        List<String> returnValue = null;
        if(tag == EditorTag.ARTIST) {
            String[] fullName = StringUtil.splitName(values);
            returnValue = getResultsForArtist(fullName[0], fullName[1]);
        }
        else if(tag == EditorTag.ALBUM_ARTIST) {
            returnValue = getResultsForAnime(values);
        }
        else if(tag == AdditionalTag.REPLACE_WORD) {

        }
        return returnValue;
    }
}
