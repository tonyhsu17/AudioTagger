package modules.database.tables;

import modules.database.FieldBase;
import modules.database.TableBase;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.List;



public class NonCapitalization implements TableBase {
    private static final NonCapitalization self = new NonCapitalization();

    public enum Fields implements FieldBase {
        WORD {
            @Override
            public String fieldName() {
                return "word";
            }

            @Override
            public String type() {
                return "VARCHAR(255)";
            }
        }
    }

    private NonCapitalization() {

    }

    public static NonCapitalization instance() {
        return self;
    }

    @Override
    public String tableName() {
        return "NonCapitalization";
    }

    @Override
    public List<FieldBase> primaryKeys() {
        return Arrays.asList(Fields.WORD);
    }

    @Override
    public List<FieldBase> uniqueKeys() {
        return null;
    }

    @Override
    public List<SimpleEntry<FieldBase, SimpleEntry<TableBase, FieldBase>>> foreignKeys() {
        return null;
    }

    @Override
    public NonCapitalization.Fields[] fields() {
        return NonCapitalization.Fields.values();
    }

    @Override
    public FieldBase id() {
        return null;
    }
}
