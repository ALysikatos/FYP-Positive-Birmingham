package com.example.positivebirmingham;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.Marker;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.android.volley.VolleyLog.TAG;
import static com.example.positivebirmingham.MapsActivity.currentPosition;
import static com.example.positivebirmingham.MapsActivity.markerHashmap;
import static com.example.positivebirmingham.MapsActivity.markersList;
import static com.example.positivebirmingham.PointsParser.distanceList;
import static com.example.positivebirmingham.PointsParser.durationList;

//The Singleton's purpose is to control object creation, limiting the number of objects to only one.

public class Architecture extends Activity {

    private static Architecture singleInstance = null;

    public static final List<ArchitectureItem> getITEMS() {
        return ITEMS;
    }

    public static final List<ArchitectureItem> ITEMS = new ArrayList<ArchitectureItem>();
    private static final List<Bitmap> ITEM_IMAGES = new ArrayList<>();
    public Context mContext;

    private Architecture(Context context) {
        this.mContext = context;
        setUpArchitectureItems();
    }

    public static Architecture getInstance(Context theContext) {
        if (singleInstance == null) {
            singleInstance = new Architecture(theContext);
            Log.i("will", "naggggh");
        }
        Log.i("will", "nah");
        Log.i("will", String.valueOf(ITEMS.size()));
        return singleInstance;
    }

    public void setUpArchitectureItems() {
        int counter = 0;

        for (Marker m : markersList) {
                Log.i("yaya", "count " +counter + "array " + distanceList.size() + " " + markersList.size() + "dur"+durationList.size());
                float[] distance = new float[1];
                Location.distanceBetween(currentPosition.latitude, currentPosition.longitude,
                        m.getPosition().latitude, m.getPosition().longitude, distance);
                Bitmap markerBitmap = markerHashmap.get(m.getTitle());
                markerBitmap = Bitmap.createScaledBitmap(markerBitmap,(int)(markerBitmap.getWidth()*3), (int)(markerBitmap.getHeight()*3), true);

                ITEMS.add(new ArchitectureItem(m.getTitle(), distanceList.get(counter), durationList.get(counter), markerBitmap, MapsActivity.architectureStyleHashmap.get(m.getTitle())));
                counter++;
            }
        Collections.sort(ITEMS, (o1, o2) -> o1.getArchitectureDistance().compareTo(o2.getArchitectureDistance()));
        Collections.sort(ITEMS, (o1, o2) -> o1.getArchitectureDuration().compareTo(o2.getArchitectureDuration()));
        }

    /**
     * An architecture item representing a piece of content.
     */
    public static class ArchitectureItem {
        public final String architectureTitle;
        public final Float architectureDistance;
        public final String architectureDuration;
        public final Bitmap architectureImage;
        public final String architectureSnippet;

        public Float getArchitectureDistance() {
            return architectureDistance;
        }

        public Integer getArchitectureDuration() {
            architectureDuration.trim();
            Log.i("josep", String.valueOf(architectureDuration.length()));
            if (architectureDuration.length() == 5 ||architectureDuration.length() == 6){
                //Log.i("josep", String.valueOf(Integer.parseInt(architectureDuration.substring(0,1).trim())));
                return Integer.parseInt(architectureDuration.substring(0,1).trim());
            }
            if (architectureDuration.length() == 7) {
                Log.i("josephhh", String.valueOf(Integer.parseInt(architectureDuration.substring(0,2).trim())));
                return Integer.parseInt(architectureDuration.substring(0,2).trim());
            }else
                return 100;
        }

        public ArchitectureItem(String architectureTitle, Float architectureDistance, String
                                architectureDuration, Bitmap architectureImage, String architectureSnippet) {
            this.architectureTitle = architectureTitle;
            this.architectureDistance = architectureDistance;
            this.architectureDuration = architectureDuration;
            this.architectureImage = architectureImage;
            this.architectureSnippet = architectureSnippet;
        }
    }
}
