package com.example.android.rural;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public final class QueryUtils {

    static int len;

    public static final String LOG_TAG = QueryUtils.class.getSimpleName();



    public static List<Locations> fetchLocationData(String requestUrl) {
        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error closing input stream", e);
        }

        // Extract relevant fields from the JSON response and create an {@link Event} object
        List<Locations> locations = extractLocations(jsonResponse);

        // Return the {@link Event}
        return locations;
    }

    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error with creating URL ", e);
        }
        return url;
    }

    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        if (url==null)return jsonResponse;
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.connect();
            if (urlConnection.getResponseCode()==200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            }
            else Log.e(LOG_TAG,"Received response:"+urlConnection.getResponseCode());
        } catch (IOException e) {
            Log.e(LOG_TAG,"IOException",e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // function must handle java.io.IOException here
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    private QueryUtils() {
    }

    public static ArrayList<Locations> extractLocations(String locationJSONResponse) {
        ArrayList<Locations> locations = new ArrayList<>();
        try{
            JSONObject root=new JSONObject(locationJSONResponse);
            JSONArray results=root.getJSONArray("results");
            len=results.length();
            for (int i=0 ;i<results.length();i++){
                JSONObject feature=results.getJSONObject(i);
                JSONObject geometry=feature.getJSONObject("geometry");
                JSONObject location=geometry.getJSONObject("location");
                double lat=location.getDouble("lat");
                double lng=location.getDouble("lng");
                String name=feature.getString("name");
                locations.add(new Locations(lng,lat,name));
            }
        }
        catch (JSONException e) {
            Log.e("QueryUtils", "Problem parsing the locations JSON results", e);
        }
        return locations;
    }
}