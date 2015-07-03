package com.example.sunchaser.app.activity;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sunchaser.R;
import com.example.sunchaser.app.data.Place;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by smee on 03/06/15.
 */
public class PlaceListAdapter extends ArrayAdapter {

    private static final int NUM_VIEW_TYPES = 2;
    private static final int VIEW_TYPE_LIST_ITEM = 0;
    private static final int VIEW_TYPE_LOAD_MORE = 1;

    private String nextPageToken;
    private final PlaceListCallbackHandler callbackHandler;

    public PlaceListAdapter(Context context, List<Place> places, String nextPageToken, PlaceListCallbackHandler callbackHandler) {
        super(context, R.layout.fragment_place_of_interest_list_item, places);

        this.nextPageToken = nextPageToken;
        this.callbackHandler = callbackHandler;
    }

    @Override
    public int getCount() {
        return super.getCount() + (nextPageToken == null ? 0 : 1);
    }

    @Override
    public int getItemViewType(int position) {
        if (position < super.getCount()) {
            return VIEW_TYPE_LIST_ITEM;
        }
        return VIEW_TYPE_LOAD_MORE;
    }

    @Override
    public int getViewTypeCount() {
        return NUM_VIEW_TYPES;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (position >= getCount()) {
            return null;
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());

        int viewType = getItemViewType(position);

        switch (viewType) {
            case VIEW_TYPE_LIST_ITEM: {
                View rowView = inflater.inflate(R.layout.fragment_place_of_interest_list_item, parent, false);

                TextView titleView = (TextView) rowView.findViewById(R.id.list_item_place_title);
                TextView vicinityView = (TextView) rowView.findViewById(R.id.list_item_place_vicinity);
                ImageView imageView = (ImageView) rowView.findViewById(R.id.list_item_place_icon);

                final Place place = (Place) getItem(position);

                titleView.setText(place.getName());
                vicinityView.setText(place.getVicinity());

                // TODO: Move numbers somewhere easier to configure
                Picasso.with(getContext()).load(Uri.parse(place.getListIconUrl())).resize(64, 64).centerInside().into(imageView);

                rowView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        callbackHandler.showPlaceDetail(place.getId());
                    }
                });


                return rowView;
            }

            case VIEW_TYPE_LOAD_MORE: {
                View rowView = inflater.inflate(R.layout.fragment_place_of_interest_load_more, parent, false);

                rowView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // TODO: Set spinny thing going? Check network connection and warn if none?
                        callbackHandler.loadMorePlaces(nextPageToken);
                    }
                });

                return rowView;
            }

            default: return null;
        }

    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }

    public static interface PlaceListCallbackHandler {
        public abstract void loadMorePlaces(String nextPageToken);
        public abstract void showPlaceDetail(String id);
    }
}
