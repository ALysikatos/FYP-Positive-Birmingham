package com.example.positivebirmingham;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.example.positivebirmingham.MapsActivity.loadingDialog;

/**
 * AsyncTask to parse JSON data and draw the route on the map using polylines
 * Courtesy : https://github.com/Vysh01/android-maps-directions/blob/master/app/src/main/java/com/thecodecity/mapsdirection/directionhelpers/PointsParser.java
 * Shrestha, V. (2018). Android-maps-directions [online]. [Accessed 30 March 2019].
 * Customized method
 */
public class PointsParser extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

    static ArrayList<Float> distanceList = new ArrayList<>();
    static ArrayList<String> durationList = new ArrayList<>();
    private String directionMode;
    private static int x = 0;
    private Double miles;

    public PointsParser(String directionMode) {
        this.directionMode = directionMode;
    }

    // Parsing the data in non-ui thread/background thread
    @Override
    protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

        JSONObject jObject;
        List<List<HashMap<String, String>>> routes = null;

        try {
            jObject = new JSONObject(jsonData[0]);
            Log.d("mylog", jsonData[0].toString());
            DataParser parser = new DataParser();
            Log.d("mylog", parser.toString());

            // Starts parsing data
            routes = parser.parse(jObject);
            Log.d("mylog", "Executing routes");
            Log.d("mylog", routes.toString());

        } catch (Exception e) {
            Log.d("mylog", e.toString());
            e.printStackTrace();
        }
        return routes;
    }

    // Executes in UI thread, after the parsing process
    @Override
    protected void onPostExecute(List<List<HashMap<String, String>>> result) {
        ArrayList<LatLng> points;
        PolylineOptions lineOptions = null;
        String distance = "";
        String duration = "";

        // Traversing through all the routes
        for (int i = 0; i < result.size(); i++) {
            points = new ArrayList<>();
            lineOptions = new PolylineOptions();
            // Fetching i-th route
            List<HashMap<String, String>> path = result.get(i);
            // Fetching all the points in i-th route
            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);
                // Get distance from the list
                if (j == 0) {
                    distance = (String) point.get("distance");
                    continue;
                } else if (j == 1) { // Get duration from the list
                    duration = (String) point.get("duration");
                    continue;
                }

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);
                points.add(position);
            }
            // Adding all the points in the route to LineOptions
            lineOptions.addAll(points);
            if (directionMode.equalsIgnoreCase("walking")) {
                lineOptions.width(10);
                lineOptions.color(Color.BLUE);
            } else {
                lineOptions.width(20);
                lineOptions.color(Color.RED);
            }
            Log.d("mylog", "onPostExecute lineoptions decoded");
        }

        // Drawing polyline in the Google Map for the i-th route
        if (lineOptions != null) {
            x++;
            //convert distance into miles
            Float distanceInMiles = Float.parseFloat(distance.substring(0, 3));
            miles = distanceInMiles.doubleValue() * 0.62137;
            DecimalFormat df = new DecimalFormat("0.0");
            miles = Double.valueOf(df.format(miles));
            if (x <= 35) {
                //add all architecture distances and durations into arraylists
                distanceList.add(miles.floatValue());
                durationList.add(duration);
                if (x == 35) {
                    loadingDialog.dismiss();
                }
                return;
            }
            if (MapsActivity.currentPolyline != null)
                MapsActivity.currentPolyline.remove();
            //draw route on map
            MapsActivity.currentPolyline = MapsActivity.mMap.addPolyline(lineOptions);

            String theSnippet = "Distance: " + miles + " miles, " + duration + " walk";
            if (MapsActivity.destinationMarker != null) {
                //open destination marker info window displaying distance and duration away from current location
                MapsActivity.destinationMarker.setSnippet(theSnippet);
                MapsActivity.destinationMarker.showInfoWindow();
            }
        } else {
            Log.d("mylog", "without Polylines drawn");
        }
    }
}
