package models.dataSuggestors;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.scene.image.Image;
import support.Utilities;
import support.Utilities.Tag;

/**
 * @author Ikersaro
 * Group = More than one artist 
 * Artist = Single artist or Group Name
 */
public class DatabaseController implements DataSuggestorBase
{
    public interface SuggestorCallback
    {
        public void topChoice(String str);
    }

    private Statement statement;
    Connection conn = null;
    public static enum TableNames {
        Anime {
            @Override
            public String toString() {
                return "AnimeName";
              }
        },
        Artist {
            @Override
            public String toString() {
                return "ArtistInfo";
              }
        },
        Group {
            @Override
            public String toString() {
                return "GroupName";
              }
        },
        ArtistToGroup {
            @Override
            public String toString() {
                return "ArtistIDToGroupID";
              }
        },
        AlbumSearchIgnoreList {
            @Override
            public String toString() {
                return "AlbumSearchIgnoreList";
              }
        }
    }
    
    public DatabaseController(String name)
    {
        initializeDB(name.isEmpty() ? "defaultDB" : name);
//        try
//        {
//            deleteAllTables();
//            conn.commit();
//        }
//        catch (SQLException e)
//        {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
        initializeTables();
//        insertDummyValues();
    }
    
    private void initializeDB(String name)
    {
        String driver = "org.apache.derby.jdbc.EmbeddedDriver";
        String dbName = name; // the name of the database
        String protocol = "jdbc:derby:";
        
        try {
            Class.forName(driver).newInstance();
            conn = DriverManager.getConnection(protocol + dbName + ";create=true", null);
            if (conn != null) {
                System.out.println("Connected to the database: " + dbName);
            }
            // We want to control transactions manually. Autocommit is on by default in JDBC.
//            conn.setAutoCommit(false);
            statement = conn.createStatement();
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
    }
    
    private void initializeTables()
    {
        boolean sucess = true;
//        sucess &= createTableIfNotExist(TableNames.ParentSeries + "(" +
//            "Id INT PRIMARY KEY, " +
//            "AnimeSeries VARCHAR(255) UNIQUE," +
//            " UseFrequency INT)");
        sucess &= createTableIfNotExist(TableNames.Anime + "(" +
            " Id INT PRIMARY KEY," +
            " AnimeName VARCHAR(255) UNIQUE," +
            " UseFrequency INT)");
        sucess &= createTableIfNotExist(TableNames.Artist + "(" +
            " Id INT PRIMARY KEY," +
            " ArtistFirst VARCHAR(255)," +
            " ArtistLast VARCHAR(255)," +
            " UseFrequency INT," +
            " UNIQUE (ArtistFirst, ArtistLast))");
        sucess &= createTableIfNotExist(TableNames.Group + "(" +
            " Id INT PRIMARY KEY," +
            " GroupName VARCHAR(255)," +
            " ArtistIDHash VARCHAR(255)," +
            " UseFrequency INT)");
        sucess &= createTableIfNotExist(TableNames.ArtistToGroup + "(" +
            " ArtistId INT," +
            " GroupId INT," +
            " PRIMARY KEY (ArtistId, GroupId)," +
            " FOREIGN KEY (ArtistId) REFERENCES ArtistInfo (Id)," +
            " FOREIGN KEY (GroupId) REFERENCES GroupName (Id))");
//        sucess &= createTableIfNotExist(TableNames.AlbumSearchIgnoreList + "(" +
//            " AlbumSearchIgnoreList VARCHAR(255) PRIMARY KEY)");
        if(sucess)
        {
            System.out.println("YAY");
        }
    }
    
    private boolean createTableIfNotExist(String tableSchema)
    {
        try
        {
            statement.execute("CREATE TABLE " + tableSchema);
        }
        catch(SQLException e)
        {
            if(e.getSQLState().equals("X0Y32")) // table already exist
            {
                return true;
            }
            else
            {
                System.out.println(e.getMessage());
                return false;
            }
           
        }
        return true;
    }

    public void cleanup()
    {
        try
        {
            conn.commit();
            conn.close();
        }
        catch (SQLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
        
    private List<String> getDBResultsForArtist(String firstName, String lastName)
    {
        List<String> possibleArtists = new ArrayList<>();
        
        if(firstName == null || firstName.isEmpty())
        {
            return new ArrayList<String>(); // change to select without where
        }
        else
        {
            firstName = firstName.toLowerCase(); // ignore case for comparing
            lastName = lastName.toLowerCase();
            
            PreparedStatement statements;
            ResultSet rs; 
            try
            {
                List<Integer> artistId = new ArrayList<>();
                List<Integer> groupId = new ArrayList<>();
                // Individuals: first - dbFirst, last - dbLast
                statements = conn.prepareStatement("SELECT ArtistFirst, ArtistLast, Id FROM " + TableNames.Artist + 
                    " where LOWER(ArtistFirst) like ? OR LOWER(ArtistLast) like ? ORDER BY UseFrequency DESC FETCH NEXT 10 ROWS ONLY");
                statements.setString(1, firstName + '%');
                statements.setString(2, lastName + '%');
                rs = statements.executeQuery();
                while(rs.next())
                {
                    String name = (rs.getString(1) + " " + rs.getString(2)).trim();
                    int id = rs.getInt(3);
                    if(!name.isEmpty() && !artistId.contains(id))
                    {
                        possibleArtists.add(name);
                        artistId.add(id);
                    }
                }
                rs.close();
                
                // Individuals: last - dbFirst, first - dbLast
                statements = conn.prepareStatement("SELECT ArtistFirst, ArtistLast, Id FROM " + TableNames.Artist + 
                    " where LOWER(ArtistFirst) like ? OR LOWER(ArtistLast) like ? ORDER BY UseFrequency DESC FETCH NEXT 10 ROWS ONLY");
                statements.setString(1, lastName + '%');
                statements.setString(2, firstName + '%');
                rs = statements.executeQuery();
                while(rs.next())
                {
                    String name = (rs.getString(1) + " " + rs.getString(2)).trim();
                    int id = rs.getInt(3);
                    if(!name.isEmpty() && !artistId.contains(id))
                    {
                        possibleArtists.add(name);
                        artistId.add(id);
                    }
                }
                rs.close();
                
                // Individuals: first - dbFirst, first - dbLast
                statements = conn.prepareStatement("SELECT ArtistFirst, ArtistLast, Id FROM " + TableNames.Artist + 
                    " where LOWER(ArtistFirst) like ? OR LOWER(ArtistLast) like ? ORDER BY UseFrequency DESC FETCH NEXT 10 ROWS ONLY");
                statements.setString(1, firstName + '%');
                statements.setString(2, firstName + '%');
                rs = statements.executeQuery();
                while(rs.next())
                {
                    String name = (rs.getString(1) + " " + rs.getString(2)).trim();
                    int id = rs.getInt(3);
                    if(!name.isEmpty() && !artistId.contains(id))
                    {
                        possibleArtists.add(name);
                        artistId.add(id);
                    }
                }
                rs.close();
                
                // Group name 
                statements = conn.prepareStatement("SELECT GroupName, Id FROM " + TableNames.Group + 
                    " where LOWER(GroupName) like ? ORDER BY UseFrequency DESC FETCH NEXT 10 ROWS ONLY");
                statements.setString(1, '%' + firstName + " " + lastName + '%');
                rs = statements.executeQuery();
                while(rs.next())
                {
                    String name = (rs.getString(1)).trim();
                    int id = rs.getInt(2);
                    if(!name.isEmpty() && !groupId.contains(id))
                    {
                        possibleArtists.add(name);
                        groupId.add(id);
                    }
                }
                rs.close();
                
                statements = conn.prepareStatement("SELECT GroupName, Id FROM " + TableNames.Group + 
                    " where LOWER(GroupName) like ? ORDER BY UseFrequency DESC FETCH NEXT 10 ROWS ONLY");
                statements.setString(1, '%' + firstName + '%');
                rs = statements.executeQuery();
                while(rs.next())
                {
                    String name = (rs.getString(1)).trim();
                    int id = rs.getInt(2);
                    if(!name.isEmpty() && !groupId.contains(id))
                    {
                        possibleArtists.add(name);
                        groupId.add(id);
                    }
                }
                rs.close();
                
                // Group by artist
                for(int id : artistId)
                {
                    // for each artistId, grab groupName that the artist is in
                    statements = conn.prepareStatement("SELECT GroupID FROM " + TableNames.ArtistToGroup + 
                        " where ArtistId = ? FETCH NEXT 10 ROWS ONLY");
                    statements.setInt(1, id);
                    rs = statements.executeQuery();
                    while(rs.next())
                    {
                        // for each groupId found, get the name
                        int tempGroupId = rs.getInt(1);
                        if(!groupId.contains(tempGroupId))
                        {
                            statements = conn.prepareStatement("SELECT GroupName, Id FROM " + TableNames.Group + 
                                " where Id = ? FETCH FIRST ROW ONLY");
                            statements.setInt(1, tempGroupId);
                            rs = statements.executeQuery();
                            if(rs.next())
                            {
                                String name = (rs.getString(1)).trim();
                                possibleArtists.add(name);
                                groupId.add(tempGroupId);
                            }
                            rs.close();
                        }
                    }
                }
                System.out.println("DB Artist Search Results:\n" + Arrays.toString(possibleArtists.toArray(new String[0]))); 
                rs.close();
            }
            catch (SQLException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
                System.out.println("ERROR");
                System.exit(1);
            } 
        }
        return possibleArtists;
    }
    
    private List<String> getDBResultsForAnime(String compareValue)
    {
        List<String> possibleAnimes = new ArrayList<>();
        
        if(compareValue == null || compareValue.isEmpty())
        {
            return new ArrayList<String>(); // change to select without where
        }
        else
        {
            compareValue = compareValue.toLowerCase(); // ignore case for comparing
            
            PreparedStatement statements;
            
            try
            {
//                List<Integer> artistId = new ArrayList<>();
                // Individuals
                statements = conn.prepareStatement("SELECT AnimeName FROM " + TableNames.Anime + 
                    " where LOWER(AnimeName) like ? ORDER BY UseFrequency DESC FETCH NEXT 10 ROWS ONLY");
                statements.setString(1, '%' + compareValue + '%');
                ResultSet rs = statements.executeQuery();
                while(rs.next())
                {
                    possibleAnimes.add((rs.getString(1)).trim());
                }
                rs.close();
//                System.out.println("Animes: " + Arrays.toString(possibleAnimes.toArray(new String[0])));               
            }
            catch (SQLException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
                System.out.println("ERROR");
                System.exit(1);
            } 
        }
        return possibleAnimes;
    }
    
    /**
     * Add as new values or increases use count
     * @param table DatabaseController.TableName
     * @param values <br>TableNames.Artist = "firstName", "lastName"
     *               <br>TableNames.Anime = "animeName"
     *               <br>TableNames.Group = "groupName", "artst1", artistX...
     */
    private void add(TableNames table, String... values)
    {
//        System.out.println("--DB: Add TableNames." + table + " values: " + Arrays.toString(values));
        int id = getId(table, values);
        if(id != -1) // if id found
        {
            // execute update statement
            update(table, id);
        }
        else
        {
            prepareInsertThenExecute(table, values);
        } 
        try
        {
            conn.commit();
        }
        catch (SQLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /**
     * Updates use frequency of id
     * @param table DatabaseController.TableNames
     * @param values id
     */
    private void update(TableNames table, int id)
    {
//        System.out.println("--DB: Update TableNames." + table + " id: " + id);
        PreparedStatement statements;
        try
        {
            // get current useFrequency
            statements = conn.prepareStatement("SELECT UseFrequency, Id FROM " + table + 
                " where Id = ? FETCH FIRST ROWS ONLY");
            statements.setInt(1, id);
            ResultSet rs = statements.executeQuery();
            if(rs.next())
            {
                // increase useFrequency count by one
                statements = conn.prepareStatement("UPDATE " + table + " SET UseFrequency = ?" + 
                    " where Id = ?");
//                System.out.println("--DB: Got use frequency: " + rs.getInt(1) + " id: " + rs.getInt(2));
                statements.setInt(1, rs.getInt(1) + 1);
                statements.setInt(2, id);
                int status = statements.executeUpdate();
//                System.out.println("--DB: update status: " + status);
            }  
            rs.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        } 
    }
    
    /**
     * Prepares values to execute insert, will convert group artist into idHash 
     * @param table DatabaseController.TableNames
     * @param values <br>TableNames.Artist = "firstName", "lastName"
     *               <br>TableNames.Anime = "animeName"
     *               <br>TableNames.Group = "groupName", "artistName1", artistNameX...
     *               <br>TableNames.ArtistToGroup = "artistId", "groupId"
     *               <br>TableNames.AlbumSearchIgnoreList = "keyword"
     */
    private int prepareInsertThenExecute(TableNames table, String... values)
    {
//        System.out.println("--DB: prepareInsertThenExecute TableNames." + table + " values: " + Arrays.toString(values));
        String id = getNextUsableId(table);
        List<String> fullValues = new ArrayList<String>();
        int insertedId = -1;
        switch(table)
        {
            case AlbumSearchIgnoreList: // AlbumSearchIgnoreList VARCHAR(255)
                if(values.length != 1)
                {
                    System.out.println("Invalid num of args for: " + table + " w/ length: " + values.length);
                }
                fullValues.add(values[0]);
                executeInsert(table, fullValues);
                break;
            case Anime: // AnimeName
                if(values.length != 1)
                {
                    System.out.println("Invalid num of args for: " + table + " w/ length: " + values.length);
                }
                fullValues.add(id);
                fullValues.add(values[0]);
                fullValues.add("0");
                insertedId = executeInsert(table, fullValues);
                break;
            case Artist: // ArtistFirst, ArtistLast
                if(values.length != 2)
                {
                    System.out.println("Invalid num of args for: " + table + " w/ length: " + values.length);
                }
                fullValues.add(id);
                fullValues.add(values[0]);
                fullValues.add(values[1]);
                fullValues.add("0");
                insertedId = executeInsert(table, fullValues);
                break;
            case ArtistToGroup:
                //self called to add this one from group insert
                if(values.length != 2)
                {
                    System.out.println("Invalid num of args for: " + table + " w/ length: " + values.length);
                }
                
                fullValues.add(values[0]);
                fullValues.add(values[1]);
                executeInsert(table, fullValues);
                break;
            case Group:
                if(values.length < 2)
                {
                    System.out.println("Invalid num of args for: " + table + " w/ length: " + values.length);
                }
                
                // create idHash
                StringBuffer idHash = new StringBuffer("");
                List<Integer> artistIds = new ArrayList<Integer>();
                for(int i = 0; i < values.length; i++)
                {
                    String[] splitName = Utilities.splitName(values[i]);
                    int artistId = getId(TableNames.Artist, splitName[0], splitName[1]);
                    if(artistId == -1) // if artist not found
                    {
                        // add new entry for artist
                        artistId = prepareInsertThenExecute(TableNames.Artist, splitName[0], splitName[1]);
                    }
                    idHash.append(artistId + "-");
                    artistIds.add(artistId);
                }                
                fullValues.add(id);
                System.out.println("@@@: " + Utilities.getCommaSeparatedStringWithAnd(Arrays.asList(values)));
                fullValues.add(Utilities.getCommaSeparatedStringWithAnd(Arrays.asList(values)));
                fullValues.add(idHash.toString());
                fullValues.add("0");
                insertedId = executeInsert(table, fullValues);
                
                // populate lookup table
                for(int i : artistIds)
                {
                    prepareInsertThenExecute(TableNames.ArtistToGroup, i + "", id + "");
                }
                break;
            default:
                break;
            
        }
        return insertedId;
    }
   
    /**
     * Executes insert sql. Do not call directly, only called from prepareInsertThenExecute().
     * @param table DatabaseController.TableNames
     * @param values <br>TableNames.Artist = "id", "firstName", "lastName", "useFrequency"
     *               <br>TableNames.Anime = "id", "animeName", "useFrequency"
     *               <br>TableNames.Group = "id", "groupName", "idHash", "useFrequency"
     *               <br>TableNames.ArtistToGroup = "artistId", "groupId"
     *               <br>TableNames.AlbumSearchIgnoreList = "keyword"
     * @return Id of row
     */
    private int executeInsert(TableNames table, List<String> values)
    { 
        try
        {
            PreparedStatement statements;
            int numCols = values.size();
            
            statements = conn.prepareStatement("INSERT INTO " + table + " VALUES (" + 
                Utilities.createQuestionMarks(numCols) + ")");
            switch(table)
            {
                case AlbumSearchIgnoreList:
                    if(numCols == 1)
                    {
                        statements.setString(1, values.get(0));
                    }
                    break;
                case Anime:
                    if(numCols == 3)
                    {
                        statements.setInt(1, Integer.valueOf(values.get(0)));
                        statements.setString(2, values.get(1));
                        statements.setInt(3, Integer.valueOf(values.get(2))); 
                    }
                    break;
                case Artist:
                    if(numCols == 4)
                    {
                        statements.setInt(1, Integer.valueOf(values.get(0)));
                        statements.setString(2, values.get(1));
                        statements.setString(3, values.get(2));
                        statements.setInt(4, Integer.valueOf(values.get(3))); 
                    }
                    break;
                case ArtistToGroup:
                    if(numCols == 2)
                    {
                        statements.setInt(1, Integer.valueOf(values.get(0)));
                        statements.setInt(2, Integer.valueOf(values.get(1))); 
                    }
                    break;
                case Group:
                    if(numCols == 4)
                    {
                        statements.setInt(1, Integer.valueOf(values.get(0)));
                        statements.setString(2, values.get(1));
                        statements.setString(3, values.get(2));
                        statements.setInt(4, Integer.valueOf(values.get(3))); 
                    }
                    break;
                default:
                    throw new SQLException("No table found");
            }
            statements.executeUpdate();
            return Integer.valueOf(values.get(0));
        }
        catch (SQLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println(values.size());
            System.exit(1);
        }
        return 0;
    }
    
    
    /**
     * @param table DatabaseController.TableNames
     * @return next free id
     */
    private String getNextUsableId(TableNames table)
    { 
        ResultSet rs;
        int usableId = -1;
        try
        {
            if(table.equals(TableNames.Anime) ||
                table.equals(TableNames.Artist) ||
                table.equals(TableNames.Group))
            {
                rs = statement.executeQuery("SELECT Id FROM " + table + " ORDER BY Id DESC FETCH FIRST ROW ONLY");
                if(rs.next())
                {
                    usableId = rs.getInt(1) + 1; 
                }
                else
                {
                    usableId = 1;
                }
            }
        }
        catch (SQLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
//        System.out.println("Next id is: " + usableId + " for table: " + table);
        return usableId + "";
    }
    
    /**
     * Check if value in db
     * @param table TableNames
     * @param values <br>TableNames.Artist = "firstName", "lastName"
     *               <br>TableNames.Anime = "animeName"
     *               <br>TableNames.Group = "groupName"
     */
    public boolean containsCaseSensitive(TableNames table, String... values)
    {
        return getId(table, values) != -1 ? true : false;
    }
    
    /**
     * Get Id of value
     * @param table DatabaseController.TableNames
     * @param values <br>TableNames.Artist = "firstName", "lastName"
     *               <br>TableNames.Anime = "animeName"
     *               <br>TableNames.Group = "groupName"
     */
    private int getId(TableNames table, String... values)
    {
        PreparedStatement statements;
        int id = -1;
//        System.out.println("GetId for TableNames." + table + " " + Arrays.toString(values));
        if(table.equals(TableNames.Artist) && values.length == 2)
        {
            try
            {
                statements = conn.prepareStatement("SELECT ArtistFirst, ArtistLast, Id FROM " + TableNames.Artist + 
                    " where ArtistFirst = ? AND ArtistLast = ? FETCH FIRST ROW ONLY");
                statements.setString(1, values[0]);
                statements.setString(2, values[1]);
                ResultSet rs = statements.executeQuery();
                if(rs.next())
                {
                    id = rs.getInt(3);
                }  
                rs.close();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            } 
        }
        else if(table.equals(TableNames.Group) && values.length == 1)
        {
            try
            {
                statements = conn.prepareStatement("SELECT GroupName, Id FROM " + TableNames.Group + 
                    " where GroupName = ? FETCH FIRST ROW ONLY");
                statements.setString(1, values[0]);
                ResultSet rs = statements.executeQuery();
                if(rs.next())
                {
                    id = rs.getInt(2);
                }  
                rs.close();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            } 
        }
        else if(table.equals(TableNames.Anime) && values.length == 1)
        {
            try
            {
                statements = conn.prepareStatement("SELECT AnimeName, Id FROM " + TableNames.Anime + 
                    " where AnimeName = ? FETCH FIRST ROW ONLY");
                statements.setString(1, values[0]);
                ResultSet rs = statements.executeQuery();
                if(rs.next())
                {
                    id = rs.getInt(2);
                }  
                rs.close();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            } 
        }
        return id;
    }
    

    // DEBUG USE
    public void deleteAllTables()
    {
        System.out.println("--DB: Deleting All Tables");
        try
        {
//            statement.execute("DROP TABLE " + TableNames.AlbumSearchIgnoreList);
            statement.execute("DROP TABLE " + TableNames.ArtistToGroup);
            statement.execute("DROP TABLE " + TableNames.Group);
            statement.execute("DROP TABLE " + TableNames.Artist);
            statement.execute("DROP TABLE " + TableNames.Anime);
//            statement.execute("DROP TABLE " + TableNames.ParentSeries);
        }
        catch (SQLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @Override
    public String getDataForTag(Tag tag, String... values)
    {
        return "";
    }

    @Override
    public Image getAlbumArt()
    {
        return null;
    }

    @Override
    public void setDataForTag(Tag tag, String... values)
    {        
        switch (tag)
        {
            case AlbumArtist:
                add(TableNames.Anime, values[0]); 
                break;
            case Artist:
                if(values.length == 1)
                {
                    // param first, last
                    String[] fullName = Utilities.splitName(values[0]);
                    if(!fullName[0].isEmpty()) 
                    {
                        add(TableNames.Artist, fullName[0], fullName[1]);
                    }
                } 
                else
                {
                    // param name, artist1, artist2, ...
                    //tagToEditorTextMapping.get(Tag.Artist).get()
//                    System.err.println("GroupSaving not implemented");
//                    System.exit(0);
                    add(TableNames.Group, values); 
                }                 
                break;
            default:
                break;
        }
    }
    
    public void save()
    {
    }

    @Override
    public void setAlbumArtFromFile(File file)
    {
        
    }
    
    @Override
    public void setAlbumArtFromURL(String url)
    {
        
    }

    @Override
    public List<String> getPossibleDataForTag(Tag tag, String string)
    {
        List<String> returnValue = null;
        switch (tag)
        {
            case Artist:
                String[] fullName = Utilities.splitName(string);
                returnValue = getDBResultsForArtist(fullName[0], fullName[1]);
                break;
            case AlbumArtist:
                returnValue = getDBResultsForAnime(string);
            default:
                break;
        }
        return returnValue;
    }
    
    // ~~~~~~~~~~~~~~~~~ //
    // Getters & Setters //
    // ~~~~~~~~~~~~~~~~~ //
    
}
