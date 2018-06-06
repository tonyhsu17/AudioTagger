package modules.vgmdb;

import java.util.List;

public class VGMDBDetails {
    private String series;
    private List<String> artists;
    private String albumName;
    private String releaseDate;
    private List<String> tracks;
    private String catalog;
    private String additionNotes;
    private String albumArtThumb;
    private String albumArtFull;
    private List<String> otherSites;
    private String notes;

    public String getSeries() {
        return series;
    }
    public void setSeries(String series) {
        this.series = series;
    }
    public List<String> getArtists() {
        return artists;
    }
    public void setArtists(List<String> artists) {
        this.artists = artists;
    }
    public String getAlbumName() {
        return albumName;
    }
    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }
    public String getReleaseDate() {
        return releaseDate;
    }
    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }
    public List<String> getTracks() {
        return tracks;
    }
    public void setTracks(List<String> tracks) {
        this.tracks = tracks;
    }
    public String getCatalog() {
        return catalog;
    }
    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }
    public String getAdditionNotes() {
        return additionNotes;
    }
    public void setAdditionNotes(String additionNotes) {
        this.additionNotes = additionNotes;
    }
    public String getAlbumArtThumb() {
        return albumArtThumb;
    }
    public void setAlbumArtThumb(String albumArtThumb) {
        this.albumArtThumb = albumArtThumb;
    }
    public String getAlbumArtFull() {
        return albumArtFull;
    }
    public void setAlbumArtFull(String albumArtFull) {
        this.albumArtFull = albumArtFull;
    }
    public List<String> getSites() {
        return otherSites;
    }
    public void setOtherSites(List<String> otherSites) {
        this.otherSites = otherSites;
    }
    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    @Override
    public String toString() {
        return "VGMDBDetails [series=" +
               series +
               ", artists=" +
               artists +
               ", albumName=" +
               albumName +
               ", releaseDate=" +
               releaseDate +
               ", tracks=" +
               tracks +
               ", catalog=" +
               catalog +
               ", additionNotes=" +
               additionNotes +
               ", albumArtThumb=" +
               albumArtThumb +
               ", albumArtFull=" +
               albumArtFull +
               "]";
    }
}
