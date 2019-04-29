package com.example.positivebirmingham;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.example.positivebirmingham.MapsActivity.markerHashmap;
import static com.example.positivebirmingham.MapsActivity.markersList;
import static com.example.positivebirmingham.PointsParser.distanceList;
import static com.example.positivebirmingham.PointsParser.durationList;

public class Architecture {

    public static final List<ArchitectureItem> ARCHITECTURE_ITEMS = new ArrayList<ArchitectureItem>();

    static {
        int counter = 0;

        for (Marker m : markersList) {
            Bitmap markerBitmap = markerHashmap.get(m.getTitle());
            markerBitmap = Bitmap.createScaledBitmap(markerBitmap, (int) (markerBitmap.getWidth() * 3), (int) (markerBitmap.getHeight() * 3), true);
            //add architecture from markers to arraylist
            ARCHITECTURE_ITEMS.add(new ArchitectureItem(m.getTitle(), distanceList.get(counter), durationList.get(counter), markerBitmap, MapsActivity.architectureStyleHashmap.get(m.getTitle())));
            counter++;
        }
        //order the arraylist by distance and duration - nearest to furthest
        Collections.sort(ARCHITECTURE_ITEMS, (o1, o2) -> o1.getArchitectureDistance().compareTo(o2.getArchitectureDistance()));
        Collections.sort(ARCHITECTURE_ITEMS, (o1, o2) -> o1.getArchitectureDuration().compareTo(o2.getArchitectureDuration()));
    }

    /**
     * An architecture item representing one architectural building.
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

        //Sort by duration in minutes
        public Integer getArchitectureDuration() {
            architectureDuration.trim();
            if (architectureDuration.length() == 5 || architectureDuration.length() == 6) {
                return Integer.parseInt(architectureDuration.substring(0, 1).trim());
            }
            if (architectureDuration.length() == 7) {
                return Integer.parseInt(architectureDuration.substring(0, 2).trim());
            } else
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
