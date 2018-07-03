package com.example.android.quakereport;

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

public class QueryUtils {

    private static String LOG_TAG = QueryUtils.class.getSimpleName();

    /** Sample JSON response for a USGS query */
    private QueryUtils()
    {}

    public static ArrayList<Earthquake> extractEarthquakes(String JsonString)
    {


        URL url = createUrl(JsonString);
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG,"error making http request",e);
        }

       ArrayList<Earthquake> earthquakes = extractFromJson(jsonResponse);

        return earthquakes;
    }

    private static ArrayList<Earthquake> extractFromJson(String jsonResponse) {

        ArrayList<Earthquake> earthquakes = new ArrayList<>();

        try {
            JSONObject jsonRootObject = new JSONObject(jsonResponse);
            JSONArray features = jsonRootObject.optJSONArray("features");
            for(int i =0;i<features.length();i++)
            {
                JSONObject currentEarthquake = features.optJSONObject(i);
                JSONObject properties = currentEarthquake.optJSONObject("properties");
                double magnitude = properties.optDouble("mag");
                String place = properties.optString("place");
                long time = properties.getLong("time");
                String url = properties.getString("url");
                Earthquake earthquake = new Earthquake(magnitude,place,time,url);
                earthquakes.add(earthquake);
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG,"error while json parsing",e);
        }
        return earthquakes;

    }

    private static String makeHttpRequest(URL url) throws IOException {

        String jsonResponse = "";
        if(url == null)
            return jsonResponse;

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            if(urlConnection.getResponseCode() == 200)
            {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromInputStream(inputStream);
            }
            else
            {
                Log.e(LOG_TAG,"error response code "+ urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG,"problem in URL connection",e);
        }
        finally {
            if(urlConnection!=null)
            {
                urlConnection.disconnect();
            }
            if(inputStream!=null)
            {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static String readFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if(inputStream!=null)
        {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String input = bufferedReader.readLine();
            while(input!=null)
            {
                output.append(input);
                input = bufferedReader.readLine();
            }
        }
        return output.toString();
    }

    private static URL createUrl(String sampleJsonResponse) {
        URL url = null;
        try {
            url = new URL(sampleJsonResponse);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG,"error creating URL",e);
        }
        return url;
    }
}
