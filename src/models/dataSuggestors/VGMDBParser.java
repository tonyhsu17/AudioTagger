package models.dataSuggestors;

import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import javax.imageio.ImageIO;
import javax.rmi.CORBA.Util;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import support.TagBase;
import support.Utilities;
import support.Utilities.Tag;


public class VGMDBParser implements DataSuggestorBase
{
    public enum AdditionalTag implements TagBase<AdditionalTag> {
        SERIES, IMAGE_URL, THEME,
        TRACK01, TRACK02, TRACK03, TRACK04, TRACK05,
        TRACK06, TRACK07, TRACK08, TRACK09, TRACK10,
        TRACK11, TRACK12, TRACK13, TRACK14, TRACK15,
        TRACK16, TRACK17, TRACK18, TRACK19, TRACK20;
        
        public static AdditionalTag getTrackTag(int i)
        {
            String str = "TRACK" + String.format("%02d", i);
            for(AdditionalTag t : AdditionalTag.values())
            {
                if(str.equals(t.name()))
                {
                    return t;
                }
            }
          return null;
        }
    }
    
    
//    public static enum VGMDBTag {
//      ARTIST, ALBUM, TRACK_NUM, YEAR, SERIES, 
//      IMAGE_URL, THEME,
//      TRACK01, TRACK02, TRACK03, TRACK04, TRACK05,
//      TRACK06, TRACK07, TRACK08, TRACK09, TRACK10,
//      TRACK11, TRACK12, TRACK13, TRACK14, TRACK15,
//      TRACK16, TRACK17, TRACK18, TRACK19, TRACK20;
//      
//      public static VGMDBTag getTrackTag(int i)
//      {
//          String str = "TRACK" + String.format("%02d", i);
//          for(VGMDBTag t : VGMDBTag.values())
//          {
//              if(str.equals(t.name()))
//              {
//                  return t;
//              }
//          }
//        return null;
//      }
//    };
    public static final String vgmdbParserURL = "http://vgmdb.info/";
    private HashMap<TagBase<?>, String> tagDataLookup; // mapping of retrieved info
    private CloseableHttpClient httpClient;

    private List<String[]> searchResults; // albumID from search results to be used to display album info

    private boolean isOnAlbumInfo;

    private ListProperty<String> displayInfo;
    private ObjectProperty<Image> albumArtThumb;
    private Image albumArt500x500;
    private String query;
    
    public VGMDBParser()
    {
        httpClient = HttpClients.createDefault();
        tagDataLookup = new HashMap<TagBase<?>, String>();
        displayInfo = new SimpleListProperty<String>();
        displayInfo.set(FXCollections.observableArrayList());
        searchResults = new ArrayList<String[]>();
        albumArtThumb = new SimpleObjectProperty<Image>();
        albumArt500x500 = null;
        query = "";
    }

    public JSONObject handleHttpGet(String url, boolean isSearch) throws IOException
    {
        JSONObject json;
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("Accept", "application/json");

        CloseableHttpResponse response = httpClient.execute(httpGet);
        try
        {
            HttpEntity entity = response.getEntity();
            // convert response into json
            Scanner sc = new Scanner(entity.getContent());
            StringBuffer buffer = new StringBuffer();
            while(sc.hasNextLine())
            {
                buffer.append(sc.nextLine());
            }
            sc.close();
            json = new JSONObject(buffer.toString());

            EntityUtils.consume(entity);
        }
        finally
        {
            response.close();
        }
        return json;
    }

    public void searchByAlbum(String album)
    {
        if(album.toLowerCase().equals(query.toLowerCase()))
        {
            return;
        }
        else
        {
            Service<JSONObject> serv = new Service<JSONObject>()
            {
                @Override
                protected Task<JSONObject> createTask() {
                    return new Task<JSONObject>() {
                        @Override
                        protected JSONObject call() throws Exception {
                            String spacesReplaced = album.replace(" ", "%20");
                            return handleHttpGet(vgmdbParserURL + "search/albums/" + spacesReplaced, true);
                        }
                    };
                }
            };
            serv.setOnSucceeded((status) -> {
                displaySearchedInfo(serv.getValue());
            });
            serv.start();
        }
    }

    private void retrieveAlbumByID(String id)
    {
        Service<JSONObject> serv = new Service<JSONObject>()
        {
            @Override
            protected Task<JSONObject> createTask() {
                return new Task<JSONObject>() {
                    @Override
                    protected JSONObject call() throws Exception {
                        return handleHttpGet(vgmdbParserURL + id, false);
                    }
                };
            }
        };
        serv.setOnSucceeded((status) -> {
            showAlbumInfo(serv.getValue());
        });
        serv.start();
    }
    
    private void displaySearchedInfo(JSONObject json)
    {
        System.out.println(json.toString(4));
        tagDataLookup.clear();
        displayInfo.clear();
        searchResults.clear();
        albumArtThumb.setValue(null);
        isOnAlbumInfo = false;

        JSONObject results = json.getJSONObject("results");
        JSONArray albums = results.getJSONArray("albums");

        query = json.getString("query");
        String queryText = "Query: " + query;
        displayInfo.add(queryText);
        searchResults.add(new String[] {queryText, ""}); // dummy used as placeholder (no -1 for indexing)

        for(int i = 0; i < albums.length(); i++)
        {
            JSONObject album = albums.getJSONObject(i);

            JSONObject titles = album.getJSONObject("titles");
            String engAlbumAndArtist = titles.getString("en");
            displayInfo.add(engAlbumAndArtist);

            String link = album.getString("link");
            searchResults.add(new String[] { engAlbumAndArtist, link });
        }
    }

    private void showAlbumInfo(JSONObject json)
    {
        System.out.println(json.toString(4));
        displayInfo.clear();
        albumArtThumb.setValue(null);
        isOnAlbumInfo = true;
        
        displayInfo.add("Go Back");

        // Get Anime Series
        JSONArray products;
        JSONObject productInfo;
        JSONObject nameVariences;
        String name;
        if(((products = json.optJSONArray("products")) != null) &&
            ((productInfo = products.optJSONObject(0)) != null) &&
            ((nameVariences = productInfo.optJSONObject("names")) != null))
        {
            if(!(name = nameVariences.optString("ja-latn")).isEmpty() ||
                !(name = nameVariences.optString("en")).isEmpty())
            {
                displayInfo.add("Series: " + name);
                tagDataLookup.put(AdditionalTag.SERIES, name);
            }
        }

        // Get Image
        String imageURL;
        if((imageURL = json.optString("picture_small")) != null)
        {
            // String imageURL = Utilities.getString(json, "picture_full"));
            try
            {
                URL url = new URL(imageURL);
                BufferedImage image = ImageIO.read(url);
                albumArtThumb.set(SwingFXUtils.toFXImage(image, null));
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if((imageURL = json.optString("picture_full")) != null)
        {
            try
            {
                BufferedImage buffImage = ImageIO.read(new URL(imageURL));
                Image image = SwingFXUtils.toFXImage(buffImage, null);
                albumArt500x500 = Utilities.scaleImage(image, 500, 500, true);
                
                tagDataLookup.put(AdditionalTag.IMAGE_URL, imageURL);
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // Get album name
        String albumName;
        if((albumName = json.optString("name")) != null)
        {
            // "only my railgun / fripSide [Limited Edition]"
            displayInfo.add("Album: " + albumName.split(" / ", 2)[0]);
            tagDataLookup.put(Tag.ALBUM, albumName.split(" / ", 2)[0]);
        }

        // Get artist
        JSONArray performers;
        List<String> performerList = new ArrayList<String>();
        if((performers = json.getJSONArray("performers")) != null)
        {
            for(int i = 0; i < performers.length(); i++)
            {
                JSONObject artistInfo;
                JSONObject artistVariences;
                String artistName;
                if(((artistInfo = performers.optJSONObject(i)) != null) &&
                    ((artistVariences = artistInfo.optJSONObject("names")) != null) &&
                    (!(artistName = artistVariences.optString("en")).isEmpty() ||
                    !(artistName = artistVariences.optString("ja")).isEmpty()) )
                {
                    performerList.add(artistName);
                }
            }
        }
        displayInfo.add("Artist(s): " + Utilities.getCommaSeparatedStringWithAnd(performerList));
        tagDataLookup.put(Tag.ARTIST, Utilities.getCommaSeparatedStringWithAnd(performerList));

        // Year
        String year;
        if(!(year = json.getString("release_date")).isEmpty())
        {
            displayInfo.add("Year: " + year.substring(0, 4)); // yyyy-mm-dd
            tagDataLookup.put(Tag.YEAR, year.substring(0, 4));
        }

        // Track Info (TODO handle multiple disc in future
        JSONArray discs;
        JSONObject firstDisc;
        JSONArray tracks;
        if(((discs = json.optJSONArray("discs")) != null) && 
            ((firstDisc = discs.optJSONObject(0)) != null) &&
            ((tracks = firstDisc.optJSONArray("tracks")) != null))
        {
            for(int i = 0; i < tracks.length(); i++)
            {
                JSONObject trackInfo;
                JSONObject titleVariences;
                if(((trackInfo = tracks.optJSONObject(i)) != null) &&
                    ((titleVariences = trackInfo.optJSONObject("names")) != null))
                {
                    String title;
                    if(!(title = titleVariences.optString("English")).isEmpty() ||
                        !(title = titleVariences.optString("Romaji")).isEmpty() ||
                        !(title = titleVariences.optString("Japanese")).isEmpty())
                    {
                        displayInfo.add((i + 1) + " " + title);
                        tagDataLookup.put(AdditionalTag.getTrackTag(i + 1), title);
                    }
                }
            }
        }
        // Store Info for additional resources
        JSONArray stores;
        if(((stores = json.optJSONArray("stores")) != null))
        {
            for(int i = 0; i < stores.length(); i++)
            {
                JSONObject store;
                String link;
                if(((store = stores.getJSONObject(i)) != null) &&
                   (store.optString("name").equals("CD Japan") || store.optString("name").equals("Play-Asia")) &&
                    !(link = store.optString("link")).isEmpty())
                {
                    displayInfo.add(link);
                }
            }
        }
        
        // Get theme
        String notes = json.optString("notes");
        if(notes.toLowerCase().contains("opening"))
        {
            int end = notes.toLowerCase().indexOf("opening");
            int value = Utilities.findIntValue(notes.substring(end - 6, end));
            tagDataLookup.put(AdditionalTag.THEME, "OP" + (value != -1 ? value : ""));
        }
        if(notes.toLowerCase().contains("ending"))
        {
            int end = notes.toLowerCase().indexOf("ending");
            int value = Utilities.findIntValue(notes.substring(end - 6, end));
            String theme;
            String foundTheme = "ED" + (value != -1 ? value : "");
            if((theme = tagDataLookup.get(AdditionalTag.THEME)) != null)
            {
                tagDataLookup.put(AdditionalTag.THEME, theme + " + " + foundTheme); 
            }
            else
            {
                tagDataLookup.put(AdditionalTag.THEME, foundTheme);
            }
        }
        if(notes.toLowerCase().contains("insert"))
        {
            int end = notes.toLowerCase().indexOf("insert");
            int value = Utilities.findIntValue(notes.substring(end - 6, end));
            String theme;
            String foundTheme = "IN" + (value != -1 ? value : "");
            if((theme = tagDataLookup.get(AdditionalTag.THEME)) != null)
            {
                tagDataLookup.put(AdditionalTag.THEME, theme + " + " + foundTheme); 
            }
            else
            {
                tagDataLookup.put(AdditionalTag.THEME, foundTheme);
            }
        }
    }

    public void selectAlbum(int index)
    {
        // first index is showing query info not actually a result
        if(!isOnAlbumInfo && index > 0 && index < searchResults.size())
        {
            retrieveAlbumByID(searchResults.get(index)[1]);
            isOnAlbumInfo = true; // reset value so clicking on index does nothing
        }
        else if(isOnAlbumInfo && index == 0) // go back to search results
        {
            displayInfo.clear();
            albumArtThumb.set(null);
            for(String[] str : searchResults)
            {
                displayInfo.add(str[0]);
            }
            isOnAlbumInfo = false; // reset value so clicking on index does nothing
        }
        else
        {
            Clipboard clpbrd = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(displayInfo.get(index));
            clpbrd.setContent(content);
        }
    }

    // ~~~~~~~~~~~~~~~~~ //
    // Getters & Setters //
    // ~~~~~~~~~~~~~~~~~ //

    public ListProperty<String> vgmdbInfoProperty()
    {
        return displayInfo;
    }

    public ObjectProperty<Image> albumArtProperty()
    {
        return albumArtThumb;
    }

    public final Image getAlbumArt()
    {
        return albumArt500x500;
    }
    
    public List<String> getArtistsInGroup()
    {
        return null;
    }

    @Override
    public String getDisplayKeywordTagClassName()
    {
        return "VGMDB";
    }
    
    @Override
    public String getDataForTag(TagBase<?> tag, String... values)
    {
        String returnValue = "";
//        TagBase<?> newTag = Utilities.getEnum(tag, AdditionalTag.class, Tag.class);
        if(tag == Tag.ALBUM) {
            returnValue = tagDataLookup.get(Tag.ALBUM);
        }
        else if(tag == Tag.ALBUM_ARTIST) {
//            returnValue = tagDataLookup.get(VGMDBTag.SERIES);
        }
        else if(tag == Tag.ARTIST) {
            returnValue = tagDataLookup.get(Tag.ARTIST);
        }
        else if(tag == Tag.COMMENT) {
//            returnValue = tagDataLookup.get(AdditionalTag.THEME);
        }
        else if(tag == Tag.TRACK) {
            returnValue = tagDataLookup.get(AdditionalTag.getTrackTag(Integer.valueOf(values[0])));
        }
        else if(tag == Tag.YEAR) {
            returnValue = tagDataLookup.get(Tag.YEAR);
        }
        else if(tag == AdditionalTag.SERIES) {
            returnValue = tagDataLookup.get(AdditionalTag.SERIES);
        }
        else if(tag == AdditionalTag.THEME) {
            returnValue = tagDataLookup.get(AdditionalTag.THEME);
        }
        else if(tag == AdditionalTag.IMAGE_URL) {
            returnValue = tagDataLookup.get(AdditionalTag.IMAGE_URL);
        }
        return returnValue;
    }

    @Override
    public void setDataForTag(TagBase<?> tag, String... values)
    {
    }

    @Override
    public void setAlbumArtFromFile(File file)
    {
    }
    
    @Override
    public void setAlbumArtFromURL(String url)
    {  
    }

    @Override
    public void save()
    {
    }

    @Override
    public List<String> getPossibleDataForTag(TagBase<?> tag, String values)
    {
        return null;
    }
    
    @Override
    public TagBase[] getAdditionalTags()
    {
        return AdditionalTag.values();
    }

    @Override
    public List<TagBase<?>> getKeywordTags()
    {
        List<TagBase<?>> keywords = new ArrayList<>();
        keywords.add(Tag.ALBUM);
        keywords.add(Tag.ARTIST);
        keywords.add(Tag.YEAR);
        for(TagBase t : AdditionalTag.values())
        {
            keywords.add(t);
        }
        return keywords;
    }
}
