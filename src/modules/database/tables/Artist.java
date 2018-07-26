package modules.database.tables;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.List;

import modules.database.FieldBase;
import modules.database.TableBase;



public class Artist implements TableBase {
    private static final Artist self = new Artist();

    public enum Fields implements FieldBase {
        ID {
            @Override
            public String fieldName() {
                return "id";
            }

            @Override
            public String type() {
                return "INT";
            }
        },
        ARTIST_FIRST {
            @Override
            public String fieldName() {
                return "artist_first";
            }

            @Override
            public String type() {
                return "VARCHAR(255)";
            }
        },
        ARTIST_LAST {
            @Override
            public String fieldName() {
                return "artist_last";
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

    private Artist() {

    }

    public static Artist instance() {
        return self;
    }

    @Override
    public String tableName() {
        return "Artist";
    }

    @Override
    public List<FieldBase> primaryKeys() {
        return Arrays.asList(new Fields[] {Fields.ID});
    }

    @Override
    public List<FieldBase> uniqueKeys() {
        return Arrays.asList(new Fields[] {Fields.ARTIST_FIRST, Fields.ARTIST_LAST});
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

