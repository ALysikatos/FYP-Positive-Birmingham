package com.example.positivebirmingham;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.example.positivebirmingham.MapsActivity.mOverlayDialog;
import static com.example.positivebirmingham.MapsActivity.overlay;
import static com.example.positivebirmingham.MapsActivity.progressBar;
import static com.example.positivebirmingham.MapsActivity.progressBarDialog;
import static com.example.positivebirmingham.MapsActivity.progressBarLayout;
import static com.example.positivebirmingham.MapsActivity.progressBarText;

public class PointsParser extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

    public static ArrayList<Float> distanceList = new ArrayList<>();
    public static ArrayList<String> durationList = new ArrayList<>();
    TaskLoadedCallback taskCallback;
    String directionMode = "walking";
    private static int x =0;
    private Double miles;

    public PointsParser(Context mContext, String directionMode) {
        this.taskCallback = (TaskLoadedCallback) mContext;
        this.directionMode = directionMode;
    }

    // Parsing the data in non-ui thread
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

                if (j == 0) {    // Get distance from the list
                    distance = (String) point.get("distance");
                    continue;
                } else if (j == 1) { // Get duration from the list
                    duration = (String) point.get("duration");
                    continue;
                }
//                    Log.i("distance", distance);
//                    Log.i("mylog", duration);

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
                lineOptions.color(Color.BLUE);
            }
            Log.d("mylog", "onPostExecute lineoptions decoded");
        }

        // Drawing polyline in the Google Map for the i-th route
        if (lineOptions != null) {
            x++;
            Log.i("nemo", "x is " + x);
            Float distanceInMiles = Float.parseFloat(distance.substring(0,3));
            miles =  distanceInMiles.doubleValue() * 0.62137;
            DecimalFormat df = new DecimalFormat("0.00");
            miles = Double.valueOf(df.format(miles));
            if (x<=35) {
              //  distance = distance.substring(0,3);

                distanceList.add(miles.floatValue());
                durationList.add(duration);
                if (x==35) {
                     overlay.dismiss();
                }
                return;
            }
    //            taskCallback.onTaskDone(lineOptions,distance,duration);
           // taskCallback.onTaskDone(distance);
           // MapsActivity.theDistance.add(distance);
            //Log.i("tipsy", String.valueOf(MapsActivity.theDistance.size()));

            if (MapsActivity.currentPolyline != null)
                MapsActivity.currentPolyline.remove();
            MapsActivity.currentPolyline = MapsActivity.mMap.addPolyline(lineOptions);

            String theSnippet = "Distance: " + miles + " miles, " + duration + " walk";
            if (MapsActivity.destinationMarker != null) {
                MapsActivity.destinationMarker.setSnippet(theSnippet);
                MapsActivity.destinationMarker.showInfoWindow();
            }
        } else {
            Log.d("mylog", "without Polylines drawn");
        }
    }
}
