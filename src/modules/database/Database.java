package modules.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import support.Logger;
import support.util.StringUtil;



public abstract class Database implements Logger {
    private String dbName = "defaultDB";
    protected Connection conn;
    protected Statement statement;

    public Database(String dbName) throws SQLException {
        if(!dbName.isEmpty()) {
            this.dbName = dbName;
        }
        initializeDB();
        initializeTables();
    }

    private void initializeDB() {
        String driver = "org.apache.derby.jdbc.EmbeddedDriver";
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
            e.printStackTrace();
        }
    }

    public abstract List<TableBase> getTables();

    protected void initializeTables() throws SQLException {
        boolean sucess = true;
        for(TableBase table : getTables()) {
            sucess &= createTableIfNotExist(table);
        }
        if(sucess) {
            info("tables initialized");
        }
        else {
            throw new SQLException("Tables did not initialize properly");
        }
    }

    /**
     * Pass in any enum value, as the the rest of the table fields will be retrieved from fields()
     * method
     * 
     * @param schema {@link TableBase}
     * @return
     */
    private boolean createTableIfNotExist(TableBase schema) {
        StringBuilder builder = new StringBuilder("CREATE TABLE " + schema.tableName() + "(");

        // handle fields
        FieldBase[] tableFields = schema.fields();
        String comma = "";
        for(FieldBase field : tableFields) {
            builder.append(comma + field.fieldName() + " " + field.type());
            comma = ", ";
        }

        // handle primary keys
        comma = "";
        List<FieldBase> primaryKey = schema.primaryKeys();
        if(primaryKey != null) {
            builder.append(", PRIMARY KEY(");
            for(FieldBase key : primaryKey) {
                builder.append(comma + key.fieldName());
                comma = ",";
            }
            builder.append(")");
        }
        // handle unique keys
        comma = "";
        List<FieldBase> uniqueKey = schema.uniqueKeys();
        if(uniqueKey != null) {
            builder.append(", UNIQUE(");
            for(FieldBase key : uniqueKey) {
                builder.append(comma + key.fieldName());
                comma = ",";
            }
            builder.append(")");
        }

        // handle foreign keys
        comma = "";
        List<SimpleEntry<FieldBase, SimpleEntry<TableBase, FieldBase>>> foreginKey = schema.foreignKeys();
        if(foreginKey != null) {
            for(SimpleEntry<FieldBase, SimpleEntry<TableBase, FieldBase>> key : foreginKey) {
                builder.append(String.format(", FOREIGN KEY(%s) REFERENCES %s(%s)",
                    key.getKey().fieldName(), key.getValue().getKey().tableName(), key.getValue().getValue().fieldName()));
            }
        }

        builder.append(")"); // close off the statement
        debug(builder.toString());
        try {
            statement.execute(builder.toString());
        }
        catch (SQLException e) {
            if(e.getSQLState().equals("X0Y32")) // table already exist
            {
                return true;
            }
            else {
                error(e.getMessage());
                error("schema: " + schema);
                return false;
            }
        }
        return true;
    }

    public void dumpTable(TableBase schema) {
        PreparedStatement statements;
        try {
            StringBuffer temp = new StringBuffer("");
            statements = conn.prepareStatement("SELECT * FROM " + schema.tableName());
            ResultSet rs = statements.executeQuery();
            ResultSetMetaData data = rs.getMetaData();


            info("Table Name: " + schema.tableName() + " contents...");

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
            error(e);
        }
    }

    protected ResultSet getResults(String query, Object... values) {
        debug(query + " with values: " + Arrays.toString(values));
        PreparedStatement statements;
        ResultSet rs = null;
        try {
            statements = conn.prepareStatement(query);
            for(int i = 1; i <= values.length; i++) {
                statements.setObject(i, values[i - 1]);
            }
            rs = statements.executeQuery();

        }
        catch (SQLException e) {
            error(e);
        }
        return rs;
    }

    protected int getNextUsableId(TableBase table, FieldBase field) {
        int usableId = 1;
        try {
            String query = String.format("SELECT %s FROM %s ORDER BY %s DESC FETCH FIRST ROW ONLY",
                field.fieldName(), table.tableName(), field.fieldName());
            ResultSet rs = getResults(query);
            if(rs.next()) {
                usableId = rs.getInt(1) + 1;
            }
            rs.close();
        }
        catch (SQLException e) {
            error(e);
        }
        return usableId;
    }

    /**
     * Get the id of a row
     * 
     * @param table {@link TableBase}
     * @param field {@link FieldBase}
     * @param values field value to match
     * @return id or -1 if not matched
     */
    @SuppressWarnings("unchecked")
    protected int getId(TableBase table, Entry<FieldBase, Object>... entries) {
        StringBuilder query = new StringBuilder("SELECT " + table.id() + " FROM " + table.tableName() + " WHERE");
        int id = -1;
        String and = "";
        Object[] values = new Object[entries.length];
        for(int i = 0; i < entries.length; i++) {
            Entry<FieldBase, Object> entry = entries[i];
            query.append(and + " " + entry.getKey().fieldName() + " = ? ");
            values[i] = entry.getValue();
            and = "AND";
        }
        query.append("FETCH FIRST ROW ONLY");
        debug(query.toString());
        try {
            ResultSet rs = getResults(query.toString(), values);
            if(rs.next()) {
                id = rs.getInt(1);
            }
            rs.close();
        }
        catch (SQLException e) {
            error(e);
        }
        debug("id got: " + id);
        return id;
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
     * @return
     */
    public boolean insert(TableBase table, Object... values) {
        if(values.length != table.fields().length) {
            error("number of fields mismatch for table: " + table + ", given " + values.length + " expected " + table.fields().length);
            return false;
        }
        try {
            String query = String.format("INSERT INTO %s VALUES (%s)", table.tableName(), StringUtil.createQuestionMarks(values.length));
            PreparedStatement statements = conn.prepareStatement(query);
            for(int i = 1; i <= values.length; i++) {
                statements.setObject(i, values[i - 1]);
            }
            statements.executeUpdate();
        }
        catch (SQLException e) {
            error(e);
            return false;
        }
        return true;
    }


    protected boolean update(TableBase table, FieldBase setField, Object setValue, FieldBase matchField, Object matchValue) {
        try {
            String query = String.format("UPDATE %s SET %s = ? where %s = ?",
                table.tableName(), setField.fieldName(), matchField.fieldName());
            PreparedStatement statements = conn.prepareStatement(query);
            statements.setObject(1, setValue);
            statements.setObject(2, matchValue);
            statements.executeUpdate();
        }
        catch (SQLException e) {
            error(e);
            return false;
        }
        return true;
    }


}
