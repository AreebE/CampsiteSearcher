package com.example.campsitesearcher;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.libraries.places.api.model.Place;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link InformationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InformationFragment extends Fragment {

    private static final String TAG = "InformationFragment";
    private Place p;

    /**
     * private static final ArrayList<Place.Field> ITEMS = new ArrayList<Place.Field>()
     *     {{
     *         this.add(Place.Field.NAME);
     *         this.add(Place.Field.ADDRESS);
     *         this.add(Place.Field.PHONE_NUMBER);
     *         this.add(Place.Field.WEBSITE_URI);
     *         this.add(Place.Field.RATING);
     *         this.add(Place.Field.LAT_LNG);
     *         this.add(Place.Field.ICON_URL);
     *     }};
     */
    private RequestHandler requestHandler;

    private TextView nameView;
    private TextView phoneNumberView;
    private ImageView iconView;
    private TextView ratingView;
    private ImageButton callButton;
    private Button websiteOpener;
    private Uri uri;

    private Handler handler = new Handler();

    public InformationFragment() {
        // Required empty public constructor
    }


    public static InformationFragment newInstance() {
        InformationFragment fragment = new InformationFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }


    @Override
    public void onResume() {
        super.onResume();
        requestHandler = new RequestHandler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_information, container, false);
        nameView = (TextView) v.findViewById(R.id.name);
        phoneNumberView = (TextView) v.findViewById(R.id.phone_number);
        ratingView = (TextView) v.findViewById(R.id.rating);
        iconView = (ImageView) v.findViewById(R.id.icon);
        websiteOpener = (Button) v.findViewById(R.id.open_website);
        callButton = (ImageButton) v.findViewById(R.id.call_number);
        callButton.setImageTintList(getResources().getColorStateList(R.color.info_call_button_image_tint));
        callButton.setBackgroundTintList(getResources().getColorStateList(R.color.info_call_button_background_tint));


        updatePlace(null);
        return v;
    }


    public void updatePlace(Place p)
    {
        if (p == null)
        {
            nameView.setText(null);
            phoneNumberView.setText(null);
            callButton.setEnabled(false);
            ratingView.setText(null);
            this.uri = null;
            iconView.setImageDrawable(null);
            websiteOpener.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getContext(), R.string.noPlaceLoaded, Toast.LENGTH_SHORT).show();
                }
            });
            if (getChildFragmentManager().findFragmentById(R.id.weatherDesc) != null)
            {
                getChildFragmentManager().beginTransaction()
                        .replace(R.id.weatherReport, null)
                        .commit();
            }
            return;
        }
        this.p = p;
        getChildFragmentManager().beginTransaction()
                .replace(R.id.weatherReport, WeatherFragment.newInstance(p.getLatLng()))
                .commit();
        requestHandler.handleRequest(0, false, new Runnable() {
            @Override
            public void run() {
                try
                {
                    InputStream input = new URL(p.getIconUrl()).openStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(input);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            iconView.setImageBitmap(bitmap);
                        }
                    });
                } catch (IOException mue)
                {
                    Toast.makeText(getContext(), R.string.failedToLoadImage, Toast.LENGTH_SHORT).show();
                }
            }
        });
        nameView.setText(p.getName());
        phoneNumberView.setText(p.getPhoneNumber());
        callButton.setEnabled(p.getPhoneNumber() != null);
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_DIAL);
                i.setData(Uri.parse("tel:" + p.getPhoneNumber()));
                startActivity(i);
            }
        });
        Log.d(TAG, p.getRating() + ", ");
        if (p.getRating() != null)
        {

            ratingView.setText("Rating: " + p.getRating().toString());
        }
        this.uri = p.getWebsiteUri();
        websiteOpener.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try
                {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(uri);
                    startActivity(i);
                } catch (Exception e)
                {

                    Toast.makeText(getContext(), R.string.failedToOpenWebsite, Toast.LENGTH_SHORT).show();
                }

            }
        });


    }

    @Override
    public void onPause() {
        super.onPause();
        requestHandler.stopAllRequests();
        handler.removeCallbacks(null);
    }
}