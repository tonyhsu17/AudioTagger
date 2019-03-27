package modules.database.tables;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;

import modules.database.FieldBase;
import modules.database.TableBase;



public class WordReplacement implements TableBase {
    private static final WordReplacement self = new WordReplacement();
    
    public enum Fields implements FieldBase {
        BEFORE {
            @Override
            public String fieldName() {
                return "before";
            }

            @Override
            public String type() {
                return "VARCHAR(255)";
            }
        },
        AFTER {
            @Override
            public String fieldName() {
                return "after";
            }

            @Override
            public String type() {
                return "VARCHAR(255)";
            }
        },
        USE_FREQUENCY {
            @Override
            public String fieldName() {
                return "use_frequency";
            }

            @Override
            public String type() {
                return "INT";
            }
        };
    }
    
    private WordReplacement() {
        
    }
    
    public static WordReplacement instance() {
        return self;
    }

    @Override
    public String tableName() {
        return "WordReplacement";
    }

    @Override
    public List<FieldBase> primaryKeys() {
        return null;
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
    public Fields[] fields() {
        return Fields.values();
    }
    
    @Override
    public FieldBase id() {
        return null;
    }
}
