package spotifyplayer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;


public class SpotifyController
{
    final static private String SPOTIFY_CLIENT_ID     = "";
    final static private String SPOTIFY_CLIENT_SECRET = "";
    
    public static String getArtistId(String artistNameQuery)
    {
        String artistId = "";
        
        try
        {
            String endpoint = "https://api.spotify.com/v1/search";
            String params = "type=artist&q=" + artistNameQuery;
            String jsonOutput = spotifyEndpointToJson(endpoint, params);
            
            JsonObject root = new JsonParser().parse(jsonOutput).getAsJsonObject();
            JsonObject artists = root.get("artists").getAsJsonObject();
            JsonArray items = artists.get("items").getAsJsonArray();
            
            if(items.size() > 0)
            {
                JsonObject item = items.get(0).getAsJsonObject();
                String id = item.get("id").getAsString();
                artistId = id;
                return artistId;
            }
            else
            {
                System.err.print("Could not retrieve artist ID"); 
                return null;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return artistId;
    }
    
    public static ArrayList<String> getAlbumIdsFromArtist(String artistId)
    {
        ArrayList<String> albumIds = new ArrayList<>();
        
        try
        {
            String endpoint = "https://api.spotify.com/v1/artists/" + artistId + "/albums";
            String params = "market=CA&limit=50";
            String jsonOutput = spotifyEndpointToJson(endpoint, params);
            
            JsonObject root = new JsonParser().parse(jsonOutput).getAsJsonObject();
            
            JsonArray itemsArray = root.get("items").getAsJsonArray();
            
            if(itemsArray.size() > 0)
            {
                for(int i=0; i < itemsArray.size(); i++)
                {
                    JsonObject albumIdObj = itemsArray.get(i).getAsJsonObject();
                    String albumId = albumIdObj.get("id").getAsString();
                    albumIds.add(albumId);
                }
            }
            else
            {
                System.err.print("Could not retrieve album IDs"); 
                return null;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return albumIds;
    }
    
    public static ArrayList<Album> getAlbumDataFromArtist(String artistId)
    {
        ArrayList<String> albumIds = getAlbumIdsFromArtist(artistId);
        ArrayList<Album> albums = new ArrayList<>();
        
        for(String albumId : albumIds)
        {
            try
            {
                ArrayList<Track> albumTracks = new ArrayList<>();
                String coverUrl = "";
                String artistName = "";
                String previewUrl = "";
                String trackName = "";
                int timeInMs = 0;
                int timeInS = 0;
                int trackNum = 0;
                
                String endpoint = "https://api.spotify.com/v1/albums/" + albumId;
                String params = "market=CA";
                String jsonOutput = spotifyEndpointToJson(endpoint, params);
            
                JsonObject root = new JsonParser().parse(jsonOutput).getAsJsonObject();
                
                JsonArray imagesArray = root.get("images").getAsJsonArray();
                
                if(imagesArray.size() > 0)
                {
                    JsonObject imageObj = imagesArray.get(0).getAsJsonObject();
                        
                    //Album Cover
                    coverUrl = imageObj.get("url").getAsString();
                }

                JsonObject tracksObj = root.get("tracks").getAsJsonObject();
                JsonArray itemsArray = tracksObj.get("items").getAsJsonArray();
                
                if (itemsArray.size() > 0) 
                {
                    for (int i = 0; i < itemsArray.size(); i++) 
                    {
                        //Artists
                        JsonObject itemObj = itemsArray.get(i).getAsJsonObject();
                        JsonArray artistArray = itemObj.get("artists").getAsJsonArray();
                        
                        if (artistArray.size() > 0) 
                        {
                            for (int j = 0; j < artistArray.size(); j++) 
                            {
                                JsonObject artistObj = artistArray.get(j).getAsJsonObject();
                                artistName = artistObj.get("name").getAsString();
                            }
                        }
                        
                        //Track Time
                        timeInMs = itemObj.get("duration_ms").getAsInt();
                        timeInS = timeInMs / 1000;
                        
                        //Preview Track
                        if (itemObj.get("preview_url").isJsonNull() == false) 
                        {
                            previewUrl = itemObj.get("preview_url").getAsString();
                        }
                        
                        //Track Name
                        trackName = itemObj.get("name").getAsString();
                        
                        trackNum = itemObj.get("track_number").getAsInt();
                        
                        albumTracks.add(new Track(trackNum, trackName, timeInS, previewUrl));
                    }
                }

                //Album Name
                String albumName = root.get("name").getAsString();
                
                albums.add(new Album(artistName, albumName, coverUrl, albumTracks));             
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }            
        }
        return albums;
    }

    private static String spotifyEndpointToJson(String endpoint, String params)
    {
        params = params.replace(' ', '+');

        try
        {
            String fullURL = endpoint;
            if (params.isEmpty() == false)
            {
                fullURL += "?"+params;
            }
            
            URL requestURL = new URL(fullURL);
            
            HttpURLConnection connection = (HttpURLConnection)requestURL.openConnection();
            String bearerAuth = "Bearer " + getSpotifyAccessToken();
            connection.setRequestProperty ("Authorization", bearerAuth);
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String inputLine;
            String jsonOutput = "";
            while((inputLine = in.readLine()) != null)
            {
                jsonOutput += inputLine;
            }
            in.close();
            
            return jsonOutput;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return "";
    }
     
    private static String getSpotifyAccessToken()
    {
        try
        {
            URL requestURL = new URL("https://accounts.spotify.com/api/token");
            
            HttpURLConnection connection = (HttpURLConnection)requestURL.openConnection();
            String keys = SPOTIFY_CLIENT_ID+":"+SPOTIFY_CLIENT_SECRET;
            String postData = "grant_type=client_credentials";
            
            String basicAuth = "Basic " + new String(Base64.getEncoder().encode(keys.getBytes()));
            
            // Send header parameter
            connection.setRequestProperty ("Authorization", basicAuth);
            
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            // Send body parameters
            OutputStream os = connection.getOutputStream();
            os.write( postData.getBytes() );
            os.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            
            String inputLine;
            String jsonOutput = "";
            while((inputLine = in.readLine()) != null)
            {
                jsonOutput += inputLine;
            }
            in.close();
            
            JsonElement jelement = new JsonParser().parse(jsonOutput);
            JsonObject rootObject = jelement.getAsJsonObject();
            String token = rootObject.get("access_token").getAsString();

            return token;
        }
        catch(Exception e)
        {
            System.out.println("Something wrong here... make sure you set your Client ID and Client Secret properly!");
            e.printStackTrace();
        }
        
        return "";
    }

    public static String getArtistPicture(String artistName) 
    {
        
        String artistPictureString = "";
        
        try
        {
            String endpoint = "https://api.spotify.com/v1/search";
            String params = "type=artist&q=" + artistName;
            String jsonOutput = spotifyEndpointToJson(endpoint, params);
            
            JsonObject root = new JsonParser().parse(jsonOutput).getAsJsonObject();
            JsonObject artists = root.get("artists").getAsJsonObject();
            JsonArray items = artists.get("items").getAsJsonArray();
            
            if(items.size() > 0)
            {
                JsonObject item = items.get(0).getAsJsonObject();
                JsonArray imagesArray = item.get("images").getAsJsonArray();
                        
                if (imagesArray.size() > 0) 
                {
                    JsonObject artistObj = imagesArray.get(0).getAsJsonObject();
                    artistPictureString = artistObj.get("url").getAsString();
                }
            }
            else
            {
                System.err.println("Could not retrieve artist picture"); 
                return null;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return artistPictureString;
    }
}
