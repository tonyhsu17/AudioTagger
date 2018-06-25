package modules.database.tables;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import modules.database.FieldBase;
import modules.database.TableBase;



public class ArtistToGroup implements TableBase {
    private static final ArtistToGroup self = new ArtistToGroup();

    public enum Fields implements FieldBase {
        ARTIST_ID {
            @Override
            public String fieldName() {
                return "id";
            }

            @Override
            public String type() {
                return "INT";
            }
        },
        GROUP_ID {
            @Override
            public String fieldName() {
                return "group_id";
            }

            @Override
            public String type() {
                return "INT";
            }
        };
    }

    private ArtistToGroup() {

    }

    public static ArtistToGroup instance() {
        return self;
    }

    @Override
    public String tableName() {
        return "ArtistToGroup";
    }

    @Override
    public List<FieldBase> primaryKeys() {
        return Arrays.asList(new Fields[] {Fields.ARTIST_ID, Fields.GROUP_ID});
    }

    @Override
    public List<FieldBase> uniqueKeys() {
        return null;
    }

    @Override
    public List<SimpleEntry<FieldBase, SimpleEntry<TableBase, FieldBase>>> foreignKeys() {
        List<SimpleEntry<FieldBase, SimpleEntry<TableBase, FieldBase>>> entries =
            new ArrayList<SimpleEntry<FieldBase, SimpleEntry<TableBase, FieldBase>>>();
        entries.add(new SimpleEntry<>(ArtistToGroup.Fields.ARTIST_ID, new SimpleEntry<>(Artist.instance(), Artist.Fields.ID)));
        entries.add(new SimpleEntry<>(ArtistToGroup.Fields.GROUP_ID, new SimpleEntry<>(GroupArtist.instance(), GroupArtist.Fields.ID)));
        return entries;
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
