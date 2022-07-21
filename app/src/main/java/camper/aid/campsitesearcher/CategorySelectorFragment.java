package camper.aid.campsitesearcher;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CategorySelectorFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CategorySelectorFragment extends DialogFragment {

    private static final String TAG = "CategorySelectorFragment";

    public interface CategoryUpdater
    {
        public void updateCategory(PlaceSearcher.Category category);
    }
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String CURRENT_CATEGORY_KEY = "current category";
    private int selectedCategory;

    private PlaceSearcher.Category currentCategory;
    private CategoryUpdater categoryUpdater;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        categoryUpdater = (CategoryUpdater) getActivity();
    }

    public CategorySelectorFragment() {
        // Required empty public constructor
    }

    public static CategorySelectorFragment newInstance(PlaceSearcher.Category category) {
        CategorySelectorFragment fragment = new CategorySelectorFragment();
        Bundle args = new Bundle();
        args.putString(CURRENT_CATEGORY_KEY, category.title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentCategory = PlaceSearcher.Category.getCategory(getArguments().getString(CURRENT_CATEGORY_KEY));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_category_selector, container, false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View v = onCreateView(getLayoutInflater(), null, savedInstanceState);
        ListView listView = (ListView) v.findViewById(R.id.category_list);
        PlaceSearcher.Category[] categories = PlaceSearcher.Category.values();
        for (int i = 0; i < categories.length; i++)
        {
            if (categories[i].equals(currentCategory))
            {
                selectedCategory = i;
            }
        }
        listView.setAdapter(new CategoryAdapter(getContext(), categories));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedCategory = i;
//                Log.d(TAG, "fffff");
                ((ArrayAdapter) listView.getAdapter()).notifyDataSetChanged();
            }
        });
        Button confirmButton = (Button) v.findViewById(R.id.confirm_category);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedCategory == -1)
                {
                    Toast.makeText(getContext(), R.string.selectCategoryError, Toast.LENGTH_SHORT).show();
                    return;
                }
                categoryUpdater.updateCategory(((CategoryAdapter) listView.getAdapter()).getItem(selectedCategory));
                dismiss();
            }
        });
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(Html.fromHtml("<b><font color='"  + getResources().getColor(R.color.primary_complement_color) + "'>" + getResources().getString(R.string.selectCategory) +  "</font></b>"))
                .create();
        return alertDialog;
    }

    private class CategoryAdapter extends ArrayAdapter<PlaceSearcher.Category>
    {

        public CategoryAdapter(@NonNull Context context, PlaceSearcher.Category[] categories) {
            super(context, R.layout.category_display, categories);
//            Log.d(TAG, Arrays.toString(categories));
//            Log.d(TAG, getCount() + "");

        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            if (convertView == null)
            {
                convertView = getLayoutInflater().inflate(R.layout.category_display, null);
            }
            PlaceSearcher.Category current = getItem(position);
            ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
            TextView title = (TextView) convertView.findViewById(R.id.title);
            TextView desc = (TextView) convertView.findViewById(R.id.desc);

            icon.setImageDrawable(getResources().getDrawable(current.photoID));
            icon.setImageTintList(getResources().getColorStateList(R.color.icon_tint));

            title.setText(current.title);
            desc.setText(current.desc);
            if (position == selectedCategory)
            {
                title.setBackgroundColor(getResources().getColor(R.color.primary_complement_color));
                desc.setBackgroundColor(getResources().getColor(R.color.primary_complement_variant));
                title.setTextColor(getResources().getColor(R.color.primary_color));
                desc.setTextColor(getResources().getColor(R.color.primary_color));
                icon.setBackgroundColor(getResources().getColor(R.color.primary_complement_variant));
                icon.setEnabled(true);
            }
            else
            {
                title.setBackgroundColor(getResources().getColor(R.color.primary_color));
                desc.setBackgroundColor(getResources().getColor(R.color.primary_variant));
                title.setTextColor(getResources().getColor(R.color.primary_complement_color));
                desc.setTextColor(getResources().getColor(R.color.primary_complement_color));
                icon.setBackgroundColor(getResources().getColor(R.color.primary_variant));
                icon.setEnabled(false);
            }
            return convertView;
        }
    }
}