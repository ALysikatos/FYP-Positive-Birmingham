package com.example.positivebirmingham;

import android.app.Activity;
import android.location.Location;

import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.positivebirmingham.MapsActivity.currentPosition;
import static com.example.positivebirmingham.MapsActivity.markersList;

public class Architecture extends Activity {

    public static final List<ArchitectureItem> ITEMS = new ArrayList<ArchitectureItem>();
    public static final Map<Marker, Float> markerMap = new HashMap<>();

    static {
        for (Marker m : markersList) {
            float[] distance = new float[1];
            Location.distanceBetween(currentPosition.latitude, currentPosition.longitude,
                    m.getPosition().latitude, m.getPosition().longitude, distance);
            ITEMS.add(new ArchitectureItem(m.getTitle(), distance[0]));
        }
        Collections.sort(ITEMS, (o1, o2) -> o1.getArchitectureDistance().compareTo(o2.getArchitectureDistance()));
    }


    /**
     * An architecture item representing a piece of content.
     */
    public static class ArchitectureItem {
        public final String architectureTitle;
        public final Float architectureDistance;

        public Float getArchitectureDistance() {
            return architectureDistance;
        }

        public ArchitectureItem(String architectureTitle, Float architectureDistance) {
            this.architectureTitle = architectureTitle;
            this.architectureDistance = architectureDistance;
        }
    }
}
