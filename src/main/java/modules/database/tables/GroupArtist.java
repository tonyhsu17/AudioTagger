package modules.database.tables;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.List;

import modules.database.FieldBase;
import modules.database.TableBase;



public class GroupArtist implements TableBase {
    private static final GroupArtist self = new GroupArtist();

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
        GROUP_NAME {
            @Override
            public String fieldName() {
                return "group_id";
            }

            @Override
            public String type() {
                return "VARCHAR(255)";
            }
        },
        ARTIST_ID_HASH {
            @Override
            public String fieldName() {
                return "arist_id_name";
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

    private GroupArtist() {

    }

    public static GroupArtist instance() {
        return self;
    }

    @Override
    public String tableName() {
        return "GroupArtist";
    }

    @Override
    public List<FieldBase> primaryKeys() {
        return Arrays.asList(new Fields[] {Fields.ID});
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
        return Fields.ID;
    }
}
