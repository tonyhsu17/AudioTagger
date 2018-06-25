package modules.vgmdb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

import net.minidev.json.JSONArray;
import support.Logger;



public class VGMDBParser implements Logger {

    /**
     * Callback to indicate results have been posted
     */
    public interface VGMDBParserCB {
        public void done(VGMDBSearchDetails details);

        public void done(VGMDBDetails details);
    }

    /**
     * Callback to retrieve the json reponse
     */
    private interface RequestResponse {
        public void requestComplete(String json);
    }

    public static final String vgmdbParserURL = "http://vgmdb.info/";
    private CloseableHttpClient httpClient;

    private VGMDBSearchDetails searchDetails; // cache the last results
    private VGMDBDetails albumDetails; // cache the last results
    private int selectedIndex; // cache the last index

    private List<VGMDBParserCB> callbacks;

    public VGMDBParser() {
        httpClient = HttpClients.createDefault();
        callbacks = new ArrayList<VGMDBParserCB>();
        selectedIndex = -1;
    }

    /**
     * Allow multiple callbacks to registered
     * 
     * @param cb {@link VGMDBParserCB}
     */
    public void registerCallback(VGMDBParserCB cb) {
        callbacks.add(cb);
    }

    /**
     * Unregister callback
     * 
     * @param cb {@link VGMDBParserCB}
     * @return True if removed
     */
    public boolean unregisterCallback(VGMDBParserCB cb) {
        return callbacks.remove(cb);
    }

    /**
     * Remove all callbacks
     */
    public void removeAllCallbacks() {
        callbacks.clear();
    }

    /**
     * Creates a new thread to grab json from url and callback to return
     * 
     * @param url url to grab json from
     * @param isSearch true if is a search, false for album
     * @param cb {@link RequestResponse}
     */
    private void handleHttpGet(String url, boolean isSearch, RequestResponse cb) {
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("Accept", "application/json");

        new Thread(new Runnable() {
            @Override
            public void run() {
                CloseableHttpResponse response = null;
                try {
                    response = httpClient.execute(httpGet);
                    HttpEntity entity = response.getEntity();
                    Scanner sc = new Scanner(entity.getContent());
                    StringBuffer buffer = new StringBuffer();
                    while(sc.hasNextLine()) {
                        buffer.append(sc.nextLine());
                    }
                    sc.close();
                    String jsonStr = buffer.toString();

                    EntityUtils.consume(entity);
                    cb.requestComplete(jsonStr);
                }
                catch (IOException e) {
                }
                finally {
                    try {
                        response.close();
                    }
                    catch (IOException | NullPointerException e) {
                    }
                }
            }
        }).start();
    }

    /**
     * Search vgmdb by album name
     * 
     * @param album search query
     */
    public void searchByAlbum(String album) {
        // if there is a search cache, use it
        if(searchDetails != null && searchDetails.getQuery().toLowerCase().equals(album.toLowerCase())) {
            for(VGMDBParserCB cb : callbacks) {
                cb.done(searchDetails);
            }
        }
        // else do a search
        else {
            String spacesReplaced = album.replace(" ", "%20");
            handleHttpGet(vgmdbParserURL + "search/albums/" + spacesReplaced, true, (json) -> {
                VGMDBSearchDetails details = parseSearchQuesry(json);
                searchDetails = details;
                albumDetails = null; // reset album cache since a search was made
                selectedIndex = -1; // reset album cache since a search was made
                debug("search result: " + details);
                for(VGMDBParserCB cb : callbacks) {
                    cb.done(details);
                }
            });
        }
    }

    /**
     * Get album information based on index of search result
     * 
     * @param index Index of search result to select
     * @throws IOException if search not done first or index invalid
     */
    public void retrieveAlbumByIndex(int index) throws IOException {
        // if out of bounds and such
        if(searchDetails == null || index < 0 || index > searchDetails.getIDs().size()) {
            throw new IOException("Invalid index or no search has been made initially");
        }
        else if(selectedIndex == index) {
            for(VGMDBParserCB cb : callbacks) {
                cb.done(albumDetails);
            }
        }
        else {
            debug(searchDetails.getIDs().get(index));
            handleHttpGet(vgmdbParserURL + searchDetails.getIDs().get(index), true, (json) -> {
                VGMDBDetails details = parseAlbum(json);
                albumDetails = details;
                selectedIndex = index;
                debug("album result: " + details);
                for(VGMDBParserCB cb : callbacks) {
                    cb.done(details);
                }
            });
        }
    }

    /**
     * Parse search query json
     * 
     * @param json
     * @return {@link VGMDBSearchDetails}
     */
    private VGMDBSearchDetails parseSearchQuesry(String json) {
        debug(json);

        VGMDBSearchDetails search = new VGMDBSearchDetails();
        search.setQuery(JsonPath.read(json, VGMDBPaths.SEARCH_QUERY.path()));
        search.setAlbumTitles(JsonPath.read(json, VGMDBPaths.SEARCH_ALBUM_TITLES.path()));
        search.setIDs(JsonPath.read(json, VGMDBPaths.SEARCH_ALBUM_ID.path()));
        return search;
    }

    /**
     * Parse album json
     * 
     * @param json album json
     * @return {@link VGMDBDetails}
     */
    private VGMDBDetails parseAlbum(String json) {
        debug(json);
        VGMDBDetails details = new VGMDBDetails();

        try {
            details.setSeries(((JSONArray)JsonPath.read(json, VGMDBPaths.SERIES.varience(Variences.JAPANESE).path())).get(0).toString());
        }
        catch (PathNotFoundException | IndexOutOfBoundsException e) {
            details.setSeries(((JSONArray)JsonPath.read(json, VGMDBPaths.SERIES.varience(Variences.ENGLISH).path())).get(0).toString());
        }

        details.setAlbumArtThumbUrl(JsonPath.read(json, VGMDBPaths.ALBUM_ART_THUMB_URL.path()));
        details.setAlbumArtFull(JsonPath.read(json, VGMDBPaths.ALBUM_ART_FULL_URL.path()));
        details.setAlbumName(JsonPath.read(json, VGMDBPaths.ALBUM_NAME.path()));

        try {
            details.setArtists(JsonPath.read(json, VGMDBPaths.ARTIST.varience(Variences.ENGLISH).path()));
        }
        catch (PathNotFoundException e) {
            details.setArtists(JsonPath.read(json, VGMDBPaths.ARTIST.varience(Variences.JAPANESE).path()));
        }

        details.setAlbumName(JsonPath.read(json, VGMDBPaths.ALBUM_NAME.path()));
        try {
            details.setReleaseDate(JsonPath.read(json, VGMDBPaths.RELEAST_DATE.path()));
        }
        catch (PathNotFoundException e) {
        } 
        
        try {
            details.setTracks(JsonPath.read(json, VGMDBPaths.TRACKS.varience(Variences.JAPANESE).path()));
            // somehow if path not found it still returned an empty list, so throw an error to be caught
            if(details.getTracks().isEmpty()) {
                throw new PathNotFoundException();
            }
        }
        catch (PathNotFoundException e) {
            try {
                details.setTracks(JsonPath.read(json, VGMDBPaths.TRACKS.varience(Variences.ENGLISH).path()));
            }
            catch (PathNotFoundException e2) {
                details.setTracks(JsonPath.read(json, VGMDBPaths.TRACKS.varience(Variences.ROMANJI).path()));
            }
        }
        details.setCatalog(JsonPath.read(json, VGMDBPaths.CATALOG.path()));
        details.setAdditionNotes(JsonPath.read(json, VGMDBPaths.ADDITIONAL_NOTES.path()));

        List<String> sites = new ArrayList<String>();
        sites.add(JsonPath.read(json, VGMDBPaths.SITE_URL.path()));
        try {
            List<String> siteNames = JsonPath.read(json, VGMDBPaths.OTHER_SITE_NAMES.path());
            List<String> siteUrls = JsonPath.read(json, VGMDBPaths.OTHER_SITE_URLS.path());
            for(int i = 0; i < siteNames.size(); i++) {
                if(siteNames.get(i).contains("CD Japan") || siteNames.get(i).contains("Play-Asia")) {
                    sites.add(siteUrls.get(i));
                }
            }
        }
        catch (PathNotFoundException e) {
        }
        details.setOtherSites(sites);

        return details;
    }
}
