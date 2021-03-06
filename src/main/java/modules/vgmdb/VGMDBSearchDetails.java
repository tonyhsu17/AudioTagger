package modules.vgmdb;

import java.util.List;



public class VGMDBSearchDetails {
    private String query;
    private List<String> albumTitles;
    private List<String> ids;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<String> getAlbumTitles() {
        return albumTitles;
    }

    /**
     * @param albumTitles List of album names
     */
    public void setAlbumTitles(List<String> albumTitles) {
        this.albumTitles = albumTitles;
    }

    public List<String> getIDs() {
        return ids;
    }

    /**
     * @param urls List of urls to album
     */
    public void setIDs(List<String> urls) {
        ids = urls;
    }

    @Override
    public String toString() {
        return "VGMDBSearchDetails [query=" + query + ", albumTitles=" + albumTitles + ", ids=" + ids + "]";
    }
}
