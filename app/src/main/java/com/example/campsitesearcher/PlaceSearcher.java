package com.example.campsitesearcher;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class PlaceSearcher
{

    private static final String RESULT_KEY = "results";
    private static final String TAG = "PlaceSearcher";

    public interface PlaceLoaderListener
    {
        public void onSuccess(String message);
        public void onFailure(String message);
    }

    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/place/textsearch/json?";

    private static final String QUERY_KEY = "query";
    private static final String LANGUAGE_KEY = "language";
    private static final String LOCATION_KEY = "location";
    private static final String RADIUS_KEY = "radius";
    private static final String TYPE_KEY = "type";
    private static final String API_KEY = "key";

    private static final String RESULT_PLACE_ID_KEY = "place_id";

    private static final String ENGLISH = "en";
    public static final Integer RADIUS = 75000;
    public static final Integer ITEMS_NUMBER = 10;

    private static final String SEARCH_CAMPGROUND = "campground";
    private static final String SEARCH_ELECTRONICS = "electronics_store";
    private static final String SEARCH_CLOTHING = "clothing_store";
    private static final String SEARCH_DEPARTMENT = "department_store";
    private static final String SEARCH_HARDWARE = "hardware_store";
    private static final String SEARCH_DRUGSTORE = "drugstore";
    private static final String SEARCH_AIRPORT = "airport";
    private static final String SEARCH_CAR = "car_rental";
    private static final String SEARCH_CONVENIENCE = "convenience_store";

    public enum Category
    {
        CAMPGROUND("Campgrounds", "campgrounds", SEARCH_CAMPGROUND, "Search up available campgrounds near you.", R.drawable.map_image),
        HIKING_CLOTHES("Hiking Clothes", "hiking clothes", SEARCH_CLOTHING, "Look up clothes for hiking.", R.drawable.hiking_backpack),
        CAMPING_EQUIPMENT("General Equipment", "tents \"camping pillows\" tents", SEARCH_DEPARTMENT, "Find general equipment you may need, like tents and sleeping bags.", R.drawable.tent),
        TOOLS("Physical Assets", "hammer axe pellets", SEARCH_HARDWARE, "Look up physical tools like axes and hammers.", R.drawable.axe),
        LIGHTING("Light Sources", "lantern flashlight", SEARCH_HARDWARE, "Find some light sources, such as flashlights and lanterns.", R.drawable.lantern),
        STOVE("Stove", "stove grill fuel", SEARCH_HARDWARE, "Get some equipment for cooking, like a grill or camping stove.", R.drawable.stove),
        COOKING_AIDS("Cooking Aids", "knife plates pots pans spatulas", SEARCH_DEPARTMENT, "Find some equipment for cookings, like knives and pans.", R.drawable.knife),
        FIRST_AID("First Aid Kits", "\"first aid kits\"", SEARCH_DRUGSTORE, "Find a first aid kit for emergencies.", R.drawable.first_aid_kit),
        AIRPORT("Airports", "airports", SEARCH_AIRPORT, "If you need to fly, look for an airport.", R.drawable.airplane),
        RV("RV", "rv car", SEARCH_CAR, "Go rent an RV or car for outdoor campers.", R.drawable.rv),
        FOOD_AND_WATER("Food and Water", "food \"granola bars\" water", SEARCH_CONVENIENCE, "Look for food items you may need, like water and granola bars.", R.drawable.water_bottle),
        ELECTRONICS("Electronics", "watches gps cameras", SEARCH_ELECTRONICS, "Get any electronics you may need, like watches, cameras, and gps's", R.drawable.radio);

        public final String title;
        public final String query;
        public final String category;
        public final String desc;
        public final int photoID;

        Category(
                String title,
                String queryTerm,
                String specificCategory,
                String desc,
                int photoID)
        {
            this.title = title;
            this.query = queryTerm;
            this.category = specificCategory;
            this.desc = desc;
            this.photoID = photoID;
        }

        public static Category getCategory(String string)
        {
            switch (string)
            {
                case "Campgrounds":
                    return CAMPGROUND;
                case "Hiking Clothes":
                    return HIKING_CLOTHES;
                case "General Equipment":
                    return CAMPING_EQUIPMENT;
                case "Physical Assets":
                    return TOOLS;
                case "Light Sources":
                    return LIGHTING;
                case "Stove":
                    return STOVE;
                case "Cooking Aids":
                    return COOKING_AIDS;
                case "First Aid Kits":
                    return FIRST_AID;
                case "Airports":
                    return AIRPORT;
                case "RV":
                    return RV;
                case "Food and Water":
                    return FOOD_AND_WATER;
                case "Electronics":
                    return ELECTRONICS;
            }
            return null;
        }
    }

    private static final ArrayList<Place.Field> ITEMS = new ArrayList<Place.Field>()
    {{
        this.add(Place.Field.NAME);
        this.add(Place.Field.ADDRESS);
        this.add(Place.Field.PHONE_NUMBER);
        this.add(Place.Field.WEBSITE_URI);
        this.add(Place.Field.RATING);
        this.add(Place.Field.LAT_LNG);
        this.add(Place.Field.ICON_URL);
    }};


    public void getItems(
            Context c,
            Category category,
            LatLng location,
            ArrayList<Place> items,
            PlaceLoaderListener listener
    )
    {
        if (!connectedToInternet(c))
        {
            listener.onFailure(c.getResources().getString(R.string.no_connection));
            return;
        }
        Places.initialize(c, c.getResources().getString(R.string.PLACES_KEY));
        PlacesClient placesClient = Places.createClient(c);
        try
        {
            String url = constructURL(category, location, c.getResources().getString(R.string.PLACES_KEY));
            String jsonBody = conductSearch(url);
            Log.d(TAG, location.toString() + ", " + url);
            String[] placeIDs = getPlaceIDs(jsonBody);
            final int[] itemsLoaded = new int[1];
            for (int i = 0; i < placeIDs.length; i++)
            {
                final int position = i;
                placesClient.fetchPlace
                        (
                                FetchPlaceRequest.builder(placeIDs[i],
                                        ITEMS).build()
                        )
                        .addOnCompleteListener(new OnCompleteListener<FetchPlaceResponse>() {
                            @Override
                            public void onComplete(@NonNull Task<FetchPlaceResponse> task) {
                                items.set(position, task.getResult().getPlace());
                                itemsLoaded[0]++;
                                if (itemsLoaded[0] == ITEMS_NUMBER)
                                {
                                    listener.onSuccess(c.getResources().getString(R.string.loaded_all_items));
                                }
                            }
                        });
            }
        }
        catch (IOException|JSONException urlException)
        {
            listener.onFailure(c.getResources().getString(R.string.link_issue));
        }
        catch (NumberFormatException nfe)
        {
            listener.onFailure(c.getResources().getString(R.string.not_enough_results));
        }

    }

    private String[] getPlaceIDs (
            String jsonBody
    )
            throws JSONException,
            NumberFormatException
    {
        JSONTokener tokener = new JSONTokener(jsonBody);
        JSONObject wrapper = (JSONObject) tokener.nextValue();
        JSONArray items = wrapper.getJSONArray(RESULT_KEY);
        String[] placeIDs = new String[ITEMS_NUMBER];
        if (items.length() < ITEMS_NUMBER)
        {
            throw new NumberFormatException();
        }
        for (int i = 0; i < ITEMS_NUMBER; i++)
        {
            placeIDs[i] = items.getJSONObject(i).getString(RESULT_PLACE_ID_KEY);
        }
        return placeIDs;
    }


    private String constructURL(
            Category c,
            LatLng location,
            String apiKey)
    {
        return Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter(QUERY_KEY, c.query)
                .appendQueryParameter(LANGUAGE_KEY, ENGLISH)
                .appendQueryParameter(API_KEY, apiKey)
                .appendQueryParameter(TYPE_KEY, c.category)
                .appendQueryParameter(LOCATION_KEY, location.latitude + "," + location.longitude)
                .appendQueryParameter(RADIUS_KEY, RADIUS.toString())
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


    public boolean connectedToInternet(
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
