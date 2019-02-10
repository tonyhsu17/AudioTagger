package modules.database;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;

public interface TableBase {
    public String tableName();
    public FieldBase id();
    public List<FieldBase> primaryKeys();
    public List<FieldBase> uniqueKeys();
    public List<SimpleEntry<FieldBase, SimpleEntry<TableBase, FieldBase>>> foreignKeys();
    public FieldBase[] fields();
}
