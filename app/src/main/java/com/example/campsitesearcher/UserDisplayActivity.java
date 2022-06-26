package com.example.campsitesearcher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.text.Html;

import com.google.android.libraries.places.api.model.Place;

public class UserDisplayActivity
        extends AppCompatActivity
        implements MapFragment.PlaceUpdater,
        CategorySelectorFragment.CategoryUpdater {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_display);
        getSupportActionBar().setTitle(Html.fromHtml("<b><font color='"  + getResources().getColor(R.color.primary_complement_color) + "'>" + getResources().getString(R.string.app_name) +  "</font></b>"));
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction()
                .replace(R.id.map_fragment, MapFragment.newInstance())
                .replace(R.id.info_fragment, InformationFragment.newInstance())
                .commit();
    }

    @Override
    public void updatePlace(Place p) {
        ((InformationFragment) getSupportFragmentManager()
                .findFragmentById(R.id.info_fragment))
                .updatePlace(p);
    }

    @Override
    public void updateCategory(PlaceSearcher.Category category) {
        ((MapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment))
                .setCategory(category);
    }
}