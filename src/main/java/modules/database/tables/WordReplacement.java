package modules.database.tables;

import modules.database.FieldBase;
import modules.database.TableBase;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.List;



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
        },
        ID {
            @Override
            public String fieldName() {
                return "ID";
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
        return Arrays.asList(Fields.ID);
    }

    @Override
    public List<FieldBase> uniqueKeys() {
        return Arrays.asList(Fields.BEFORE, Fields.AFTER);
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
        return Fields.ID;
    }
}
