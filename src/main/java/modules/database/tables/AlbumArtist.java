package modules.database.tables;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.List;

import modules.database.FieldBase;
import modules.database.TableBase;



public class AlbumArtist implements TableBase {
    private static final AlbumArtist self = new AlbumArtist();
    
    public enum Fields implements FieldBase {
        ID() {
            @Override
            public String fieldName() {
                return "id";
            }

            @Override
            public String type() {
                return "INT";
            }
        },
        ANIME_NAME {
            @Override
            public String fieldName() {
                return "anime_name";
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
        }
    }
    
    public static AlbumArtist instance() {
        return self;
    }
    
    @Override
    public String tableName() {
        return "AlbumArtist";
    }

    @Override
    public List<FieldBase> primaryKeys() {
        return Arrays.asList(new Fields[] {Fields.ID});
    }

    @Override
    public List<FieldBase> uniqueKeys() {
        return Arrays.asList(new Fields[] {Fields.ANIME_NAME});
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
