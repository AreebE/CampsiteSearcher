package com.example.campsitesearcher;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class WeatherSearcher {

    private static final String TAG = "WeatherSearcher";

    public interface WeatherSearcherListener
    {
        public void onSuccess(String message, WeatherItem weatherItem);
        public void onFailure(String message);
    }

    public class WeatherItem
    {
        public final String typeOfWeather;
        public final String weatherDescription;
        public final double exactTemp;
        public final double approximateTemp;
        public final double minTemp;
        public final double maxTemp;
        public final int humidity;
        public final double dewPoint;
        public final int cloudiness;
        public final double windSpeed;
        public final double precipitationChance;
        public final double uvIndex;

        public WeatherItem(
                String typeOfWeather,
                String weatherDescription,
                double exactTemp,
                double approximateTemp,
                double minTemp,
                double maxTemp,
                int humidity,
                double dewPoint,
                int cloudiness,
                double windSpeed,
                double precipitationChance,
                double uvIndex)
        {
            this.typeOfWeather = typeOfWeather;
            this.weatherDescription = weatherDescription;
            this.exactTemp = exactTemp;
            this.approximateTemp = approximateTemp;
            this.minTemp = minTemp;
            this.maxTemp = maxTemp;
            this.humidity = humidity;
            this.dewPoint = dewPoint;
            this.cloudiness = cloudiness;
            this.windSpeed = windSpeed;
            this.precipitationChance = precipitationChance;
            this.uvIndex = uvIndex;

        }
    }
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/onecall?";

    private static final String API_KEY = "appid";
    private static final String LAT_KEY = "lat";
    private static final String LONG_KEY = "lon";
    private static final String EXCLUDE_KEY = "exclude";
    private static final String UNITS_KEY = "units";

    private static final String EXCLUSION_CATEGORIES = "hourly,current,minutely";
    private static final String UNIT_CATEGORY = "imperial";

    private static final String RESULT_DAILY_KEY = "daily";
    private static final String RESULT_TEMP_KEY = "temp";
    private static final String RESULT_DAY_KEY = "day";
    private static final String RESULT_MIN_KEY = "min";
    private static final String RESULT_MAX_KEY = "max";
    private static final String RESULT_FEELS_LIKE_KEY = "feels_like";
    private static final String RESULT_HUMIDITY_KEY = "humidity";
    private static final String RESULT_DEW_POINT_KEY = "dew_point";
    private static final String RESULT_WIND_SPEED_KEY = "wind_speed";
    private static final String RESULT_WEATHER_KEY = "weather";
    private static final String RESULT_WEATHER_TITLE_KEY = "main";
    private static final String RESULT_WEATHER_DESC_KEY = "description";
    private static final String RESULT_CLOUDS_KEY = "clouds";
    private static final String RESULT_PRECIPITATION_KEY = "pop";
    private static final String RESULT_UVI_KEY = "uvi";



    public void getWeatherData(
            Context c,
            LatLng coordinates,
            WeatherSearcherListener listener
    )
    {
        if (!connectedToInternet(c))
        {
            listener.onFailure(c.getResources().getString(R.string.no_connection));
            return;
        }
        try {
            String jsonBody = conductSearch(constructURL(c, coordinates));
            Log.d(TAG, jsonBody);
            WeatherItem item = constructItem(jsonBody);
            listener.onSuccess(c.getResources().getString(R.string.compileWeatherReport), item);
        } catch (IOException| JSONException ioException) {
            Log.d(TAG, ioException.toString());
            listener.onFailure(c.getResources().getString(R.string.failedToStartWeatherReport));
        }
    }

    private String constructURL(Context c, LatLng coordinates)
    {
        return Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter(API_KEY, c.getResources().getString(R.string.WEATHER_KEY))
                .appendQueryParameter(LAT_KEY, String.valueOf(coordinates.latitude))
                .appendQueryParameter(LONG_KEY, String.valueOf(coordinates.longitude))
                .appendQueryParameter(EXCLUDE_KEY, EXCLUSION_CATEGORIES)
                .appendQueryParameter(UNITS_KEY, UNIT_CATEGORY)
                .toString();
    }

    private String conductSearch(
            String urlRequest
    )
            throws MalformedURLException,
            IOException
    {
        URL url = new URL(urlRequest);
        HttpsURLConnection internet = (HttpsURLConnection) url.openConnection();
        ByteArrayOutputStream jsonResponses = new ByteArrayOutputStream();
        InputStream unparsedJson = internet.getInputStream();
        if (internet.getResponseCode() != HttpsURLConnection.HTTP_OK)
        {
            return null;
        }
        int bytesRead = 0;
        byte[] buffer = new byte[1024];
        bytesRead = unparsedJson.read(buffer);
        while (bytesRead > 0)
        {
            jsonResponses.write(buffer, 0, bytesRead);
            bytesRead = unparsedJson.read(buffer);
        }
        unparsedJson.close();
        jsonResponses.close();
        return new String(jsonResponses.toByteArray());
    }

    private WeatherItem constructItem(String jsonBody)
            throws JSONException
    {
        JSONTokener jsonTokener = new JSONTokener(jsonBody);
        JSONObject contents = (JSONObject) jsonTokener.nextValue();
        JSONObject dailyForecast = contents.getJSONArray(RESULT_DAILY_KEY).getJSONObject(0);
        JSONObject tempInformation = dailyForecast.getJSONObject(RESULT_TEMP_KEY);
        JSONObject weatherInformation = dailyForecast.getJSONArray(RESULT_WEATHER_KEY).getJSONObject(0);

        String typeOfWeather = weatherInformation.getString(RESULT_WEATHER_TITLE_KEY);
        String weatherDescription = weatherInformation.getString(RESULT_WEATHER_DESC_KEY);
        double exactTemp = tempInformation.getDouble(RESULT_DAY_KEY);
        double approximateTemp = dailyForecast.getJSONObject(RESULT_FEELS_LIKE_KEY).getDouble(RESULT_DAY_KEY);
        double minTemp = tempInformation.getDouble(RESULT_MIN_KEY);
        double maxTemp = tempInformation.getDouble(RESULT_MAX_KEY);
        int humidity = dailyForecast.getInt(RESULT_HUMIDITY_KEY);
        double dewPoint = dailyForecast.getDouble(RESULT_DEW_POINT_KEY);
        int cloudiness = dailyForecast.getInt(RESULT_CLOUDS_KEY);
        double windSpeed = dailyForecast.getDouble(RESULT_WIND_SPEED_KEY);
        double precipitationChance = dailyForecast.getDouble(RESULT_PRECIPITATION_KEY);
        double uvIndex = dailyForecast.getDouble(RESULT_UVI_KEY);



        return new WeatherItem
                (
                        typeOfWeather,
                        weatherDescription,
                        exactTemp,
                        approximateTemp,
                        minTemp,
                        maxTemp,
                        humidity,
                        dewPoint,
                        cloudiness,
                        windSpeed,
                        precipitationChance,
                        uvIndex
                );
    }

    private boolean connectedToInternet(
            Context c)
    {
        try
        {
            ConnectivityManager manager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
            Network network = manager.getActiveNetwork();
            if (network != null)
            {
                return manager.getNetworkCapabilities(network).hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        && manager.getNetworkCapabilities(network).hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
            }
            return false;
        } catch (NullPointerException npe)
        {
            return false;
        }
    }
}
