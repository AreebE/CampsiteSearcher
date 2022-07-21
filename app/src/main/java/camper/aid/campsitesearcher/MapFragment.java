package camper.aid.campsitesearcher;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.model.Place;

import java.util.ArrayList;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment
    implements OnMapReadyCallback
{
    public interface PlaceUpdater
    {
        public void updatePlace(Place p);
    }
    private static final String TAG = "MapFragment";

    private GoogleMap map;
    private boolean inSelectMode;
    private RequestHandler requestHandler;
    private PlaceSearcher.Category category;
    private Menu menu;
    private ArrayList<Place> places;
    private Handler handler = new Handler();

    private PlaceUpdater placeUpdater;

    public MapFragment() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        placeUpdater = (PlaceUpdater) getActivity();
    }

    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    public void setCategory(PlaceSearcher.Category c)
    {
        this.category = c;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
        category = PlaceSearcher.Category.CAMPGROUND;
    }

    @Override
    public void onResume() {
        super.onResume();
        requestHandler = new RequestHandler();
        handler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.setHasOptionsMenu(true);
        View v = inflater.inflate(R.layout.fragment_map, container, false);
        MapView mapView =  v.findViewById(R.id.map_view);
        mapView.onCreate(getArguments());
        mapView.onResume();
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        }
        catch (Exception e)
        {
            Toast.makeText(getContext(), R.string.couldNotLoadMap,Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
        mapView.getMapAsync(this);
        return v;
    }


    @Override
    public void onPause() {
        super.onPause();
        requestHandler.stopAllRequests();
        handler.removeCallbacks(null);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.map_controls, menu);
        this.menu = menu;
        for (int i = 0; i < menu.size(); i++)
        {
            MenuItem item = menu.getItem(i);
            setIconToTint(item, getResources().getColor(R.color.primary_complement_color), PorterDuff.Mode.SRC_ATOP);
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.category_selector:
                CategorySelectorFragment.newInstance(category).show(getChildFragmentManager(), TAG);
                break;
            case R.id.map_selector:
                if (inSelectMode)
                {
                    deselect();
                }
                else
                {
                    inSelectMode = true;
                    setIconToTint(item, getResources().getColor(R.color.primary_complement_color), PorterDuff.Mode.SCREEN);
                    Toast.makeText(getContext(), R.string.activatedSelectMode, Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.map = googleMap;
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.090200, -100.712900), 3.5f));
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                if (!inSelectMode)
                {
                     return;
                }
                deselect();
                map.clear();
                placeUpdater.updatePlace(null);
                CircleOptions circle = new CircleOptions();
                circle.center(latLng)
                        .radius(PlaceSearcher.RADIUS + 0.0)
                        .fillColor(getResources().getColor(R.color.primary_color_trans))
                        .strokeColor(getResources().getColor(R.color.primary_complement_color))
                        .strokeWidth(getResources().getDimension(R.dimen.circle_width));
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(circle.getCenter(), 7.75f));
                map.addCircle(circle);
//                Log.d(TAG, latLng.toString());
                requestHandler.handleRequest(1000, false, new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<Place> places = new ArrayList<>();
                        for (int i = 0; i < PlaceSearcher.ITEMS_NUMBER; i++)
                        {
                            places.add(null);
                        }
                        inSelectMode = false;

                        new PlaceSearcher().getItems(
                                getContext(),
                                category,
                                latLng,
                                places,
                                new PlaceSearcher.PlaceLoaderListener() {
                                    @Override
                                    public void onSuccess(String message) {
                                        sendToastMessage(message);
                                        setLocations(places);
                                    }

                                    @Override
                                    public void onFailure(String message) {
                                        sendToastMessage(message);
                                    }
                                }
                        );
                    }
                });

            }
        });
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                String name = marker.getTitle();

                for (int i = 0; i < places.size(); i++)
                {
                    if (places.get(i).getName().equals(name))
                    {
                        placeUpdater.updatePlace(places.get(i));
                        break;
                    }
                }
                return false;
            }
        });
    }

    private void setLocations(ArrayList<Place> places) {
        this.places = places;
        for (int i = 0; i < places.size(); i++)
        {
            Place current = places.get(i);
            LatLng currentCoord = current.getLatLng();
            MarkerOptions marker = new MarkerOptions();
            marker.title(current.getName())
                    .snippet(current.getAddress())
                    .position(currentCoord);
            map.addMarker(marker);
        }
    }

    private void deselect()
    {
        inSelectMode = false;
        MenuItem item = menu.findItem(R.id.map_selector);
        setIconToTint(item, getResources().getColor(R.color.primary_complement_color), PorterDuff.Mode.SRC_ATOP);
        Toast.makeText(getContext(), R.string.deactivatedSelectMode, Toast.LENGTH_SHORT).show();
    }

    private void setIconToTint(MenuItem item, int colorID, PorterDuff.Mode mode)
    {
        Drawable icon = item.getIcon();
        icon.setTint(colorID);
        icon.setTintMode(mode);
        item.setIcon(icon);
    }

    private void sendToastMessage(String message)
    {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}