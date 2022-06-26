package com.example.campsitesearcher;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;


public class WeatherFragment extends Fragment {


    private static final String LAT_KEY = "lat";
    private static final String LONG_KEY = "long";

    private RequestHandler requestHandler = new RequestHandler();
    private Handler handler = new Handler();
    private LatLng coordinates;

    public WeatherFragment() {
        // Required empty public constructor
    }

    public static WeatherFragment newInstance(LatLng latLng) {
        WeatherFragment fragment = new WeatherFragment();
        Bundle args = new Bundle();
        args.putDouble(LAT_KEY, latLng.latitude);
        args.putDouble(LONG_KEY, latLng.longitude);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.coordinates = new LatLng(getArguments().getDouble(LAT_KEY), getArguments().getDouble(LONG_KEY));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.fragment_weather, container, false);

        /*

                int cloudiness,
                double windSpeed,
                double precipitationChance,
                double uvIndex,
                String alertTitle,
                String alertDescription)
         */
        requestHandler.handleRequest(0, false, new Runnable() {
            @Override
            public void run() {
                new WeatherSearcher().getWeatherData
                        (
                                getContext(),
                                coordinates,
                                new WeatherSearcher.WeatherSearcherListener() {
                                    @Override
                                    public void onSuccess(String message, WeatherSearcher.WeatherItem weatherItem) {
                                        createToastMessage(message);
                                       updateItems(weatherItem, v);
                                    }

                                    @Override
                                    public void onFailure(String message) {
                                        createToastMessage(message);
                                    }
                                }
                        );
            }
        });

        return v;
    }


    @Override
    public void onPause() {
        super.onPause();
        requestHandler.stopAllRequests();
    }


    @Override
    public void onResume() {
        super.onResume();
        requestHandler = new RequestHandler();
    }

    public void createToastMessage(String message)
    {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try
                {
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                }
                catch (NullPointerException npe)
                {

                }
            }
        });
    }

    private void updateItems(WeatherSearcher.WeatherItem weatherItem, View v)
    {
        handler.post(new Runnable() {
            @Override
            public void run() {
                TextView weatherTitle = (TextView) v.findViewById(R.id.weatherTitle);
                TextView weatherDesc = (TextView) v.findViewById(R.id.weatherDesc);
                TextView tempDisplay = (TextView) v.findViewById(R.id.tempDisplay);
                TextView maxTemp = (TextView) v.findViewById(R.id.maxTemp);
                TextView minTemp = (TextView) v.findViewById(R.id.minTemp);
                TextView humidity = (TextView) v.findViewById(R.id.humidity);
                TextView dewPoint = (TextView) v.findViewById(R.id.dewPoint);
                TextView cloudiness = (TextView) v.findViewById(R.id.cloudinessPercentage);
                TextView windSpeed = (TextView) v.findViewById(R.id.windSpeed);
                TextView precipitationChance = (TextView) v.findViewById(R.id.precipitationChance);
                TextView uvIndex = (TextView) v.findViewById(R.id.uvIndex);

                weatherTitle.setText("Weather: " + weatherItem.typeOfWeather );
                weatherDesc.setText(weatherItem.weatherDescription);
                tempDisplay.setText("Temperature is about " + weatherItem.exactTemp + " F");
                maxTemp.setText("Max Temperature: " + weatherItem.maxTemp + " F");
                minTemp.setText("Min Temperature: " + weatherItem.minTemp + " F");
                humidity.setText("Humidity: " + weatherItem.humidity);
                dewPoint.setText("Dew point: " + weatherItem.dewPoint + " F");
                cloudiness.setText("Percentage of clouds: " + weatherItem.cloudiness + "%");
                windSpeed.setText("Wind speed: " + weatherItem.windSpeed + " miles/hour");
                precipitationChance.setText("Chance of Rain: " + weatherItem.precipitationChance * 100 + "%" );
                uvIndex.setText("UV index: " + weatherItem.uvIndex);

            }
        });
    }
}