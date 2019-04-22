package com.example.positivebirmingham;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.android.volley.VolleyLog.TAG;
//52.486992, -1.890255

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, TaskLoadedCallback,
        ListFragment.OnListFragmentInteractionListener {

    public static GoogleMap mMap;
    public static Polyline currentPolyline;
    public static Marker destinationMarker;
    public static ArrayList<Marker> markersList = new ArrayList<Marker>();
    public static LatLng currentPosition;
    public static TabLayout tabLayout;

    private static final int LOCATION_REQUEST_CODE = 101;
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private final String LIST_FRAGMENT_TAG = "list_fragment_tag";
    private final String SUPPORT_FRAGMENT_TAG = "support_fragment_tag";
    private final String KEY_TAB_INDEX = "tab_index";
    private static final String KEY_MARKERS_ARRAYLIST ="markers_arraylist";


    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location currentLocation;
    private CameraPosition mCameraPosition;

    public FusedLocationProviderClient fusedLocationProviderClient;
    public ListFragment listFragment;
    public SupportMapFragment supportMapFragment;

    public static ArrayList<String> theDistance = new ArrayList<>();

    public static HashMap<String, Bitmap> markerHashmap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {

            supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentByTag(SUPPORT_FRAGMENT_TAG);
            listFragment = (ListFragment) getSupportFragmentManager().findFragmentByTag(LIST_FRAGMENT_TAG);

            currentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
            //markersList = savedInstanceState.getParcelable(KEY_MARKERS_ARRAYLIST);
            int tabIndex = savedInstanceState.getInt(KEY_TAB_INDEX);

        }
        if (supportMapFragment == null) {
            supportMapFragment = new SupportMapFragment();
            supportMapFragment.setRetainInstance(true);
        }
        if (listFragment == null) {
        // only create fragment if they haven't been instantiated already
        listFragment = new ListFragment();
        listFragment.setRetainInstance(true);
        }

        setContentView(R.layout.main_activity);
//        getSupportActionBar().hide();
//        Toolbar myToolbar = findViewById(R.id.my_toolbar);
//        setSupportActionBar(myToolbar);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        //Now initialize the SupportMapFragment object from the activity’s layout file using findFragmentById
        // SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
        //          .findFragmentById(R.id.map);
        //Then attach OnMapReadyCallback listener on the object using getMapAsync(OnMapReadyCallback) API.
        // This listener will notify you when the map is ready by invoking onMapReady along with a GoogleMap object.
        //  GoogleMap is the main class of the Google Maps Android API and is the entry point for all methods
        // related to the map
        //  mapFragment.getMapAsync(this);

    //    supportMapFragment = new SupportMapFragment();
//        if (listFragment == null ){
//        listFragment = new ListFragment();}
        getLocationPermission();
        setupTabLayout();
    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, currentLocation);
            outState.putInt(KEY_TAB_INDEX, tabLayout.getSelectedTabPosition());
//            outState.putParcelable(KEY_MARKERS_ARRAYLIST, (Parcelable) markersList);
            super.onSaveInstanceState(outState);
        }
    }

    private void setupTabLayout() {
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                onTabTapped(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                onTabTapped(tab.getPosition());
            }
        });
        tabLayout.getTabAt(0).select();

//        for(int i=0; i < tabLayout.getTabCount(); i++) {
//            View tab = ((ViewGroup) tabLayout.getChildAt(0)).getChildAt(i);
//            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) tab.getLayoutParams();
//            p.setMargins(0, 0, 50, 0);
//            tab.requestLayout();
//        }
        View root = tabLayout.getChildAt(0);
        if (root instanceof LinearLayout) {
            ((LinearLayout) root).setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
            GradientDrawable drawable = new GradientDrawable();
            drawable.setColorFilter(0xffff0000, PorterDuff.Mode.MULTIPLY );
           // drawable.setColor(getResources().getColor(R.color.colorAccent));
            drawable.setSize(2, 1);
            ((LinearLayout) root).setDividerPadding(10);
            ((LinearLayout) root).setDividerDrawable(drawable);
        }
    }

    private void onTabTapped(int position) {
        switch (position) {
            case 0:
                replaceFragment(supportMapFragment);
                break;
            case 1:
                replaceFragment(listFragment);
                break;
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.addToBackStack(null);
        if (fragment == listFragment){
            Log.i("imalist","y");
        transaction.replace(R.id.fragment_content, fragment, LIST_FRAGMENT_TAG);}
        else if (fragment == supportMapFragment) {
            Log.i("imalist","yNU");
            transaction.replace(R.id.fragment_content, fragment, SUPPORT_FRAGMENT_TAG);
        }
        transaction.commit();
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */

    private void fetchLastLocation() {
        try {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            Task<Location> task = fusedLocationProviderClient.getLastLocation();

            task.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        currentLocation = location;
                        Toast.makeText(MapsActivity.this, currentLocation.getLatitude() + " " + currentLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_content);
                        supportMapFragment.getMapAsync(MapsActivity.this);
                    } else {
                        Toast.makeText(MapsActivity.this, "No Location recorded", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    //Basically before fetching user location you need to check if the user has granted location permissions for this app.
    // If the permission is not granted you can explicity request using ActivityCompat.requestPermissions() API.
    // User will then see a system dialog with two option “YES” and “NO”
    private void getLocationPermission() {
        //Checking if the user has granted location permission for this app
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
        /*
        Requesting the Location permission
        1st Param - Activity
        2nd Param - String Array of permissions requested
        3rd Param -Unique Request code. Used to identify these set of requested permission
        */
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            }, LOCATION_REQUEST_CODE);
        }
        fetchLastLocation();
    }
    /**
     * Prompts the user for permission to use the device location.
     */

    /**
     * Handles the result of the request for location permissions.
     */

//Once any of the option on the dialog is clicked onRequestPermissionResult is invoked. You need to override this
//Override the onRequestPermissionsResult() callback to handle the result of the permission request:
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Permission Granted
                    fetchLastLocation();
                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(MapsActivity.this).create();
                    alertDialog.setTitle("   Location Permission Denied!");
                    alertDialog.setMessage("\nDefault Birmingham Location Used:\n                  Aston University");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();

              //      Toast.makeText(this, "     Location Permission Denied!\nDefault Location Used - Aston University", Toast.LENGTH_LONG).show();
                    Location defaultLocation = new Location("LocationManager.GPS_PROVIDER");
                    defaultLocation.setLatitude(52.486992);
                    defaultLocation.setLongitude(-1.890255);
                    currentLocation = defaultLocation;
                    supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_content);
                    supportMapFragment.getMapAsync(MapsActivity.this);
                    break;
                    //  return;
                }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //currentPosition = new LatLng(52.486992, -1.890255);
        currentPosition = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        Log.i("jarvis", currentLocation.getLatitude() + "," + currentLocation.getLongitude());
        Log.i("jackie", currentPosition.latitude + "," + currentPosition.longitude);
        //MarkerOptions are used to create a new Marker.You can specify location, title etc with MarkerOptions
        MarkerOptions markerOptions = new MarkerOptions()
                .position(currentPosition)
                .title("You are Here")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        //Toast.makeText(this, "HI", Toast.LENGTH_SHORT).show();
        //map.animateCamera(CameraUpdateFactory.zoomTo(16), 2000, null);
        //add marker current location and move camera
        //Adding the created the marker on the map
        mMap.addMarker(markerOptions);
        addArchitectureMarkers();
        //prince();
        mMap.getUiSettings().setZoomControlsEnabled(true);
        //mMap.getUiSettings().setMapToolbarEnabled(False).


//        Log.i("jack", theDistance.get(1));
//        Log.i("jack", theDistance.get(2));
//        Log.i("jack", theDistance.get(3));
//        Log.i("jack", theDistance.get(4));
        mMap.setOnMarkerClickListener(
                new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        Log.i("testing", String.valueOf(currentPosition));
                        Log.i("testing", String.valueOf(marker.getPosition()));
                        if (!marker.getPosition().equals(currentPosition)) {

                            Intent intent = new Intent(MapsActivity.this, PopUpInfoWindow.class);

                            Bundle bundle = new Bundle();
                            bundle.putString("MARKER", marker.toString());
                            bundle.putSerializable("MARKER_TITLE", marker.getTitle());
                            bundle.putParcelable("MARKER_LATLNG", marker.getPosition());
                            bundle.putParcelable("CURRENT_LATLNG", currentPosition);
                            bundle.putString("MARKER_PLACEID", marker.getTag().toString());
                            Log.i("testing", marker.getTag().toString());
                            destinationMarker = marker;

                            intent.putExtras(bundle);
                            startActivity(intent);
                        }

//                        LayoutInflater inflater = (LayoutInflater) MapsActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                        View layout = inflater.inflate(R.layout.popup,null);
//                        PopupWindow pw = new PopupWindow(layout, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//                        pw.showAtLocation(layout, Gravity.CENTER, 0, 0);
                        return false;
                    }
                }
        );
        if (theDistance == null) {
            Log.i("jack", "NULLLL");
        } else {
            Log.i("jack", String.valueOf(theDistance.size()));
            Log.i("jack", "NULLLLNOT");
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            ActivityCompat.requestPermissions(this, new String[]{
//                    android.Manifest.permission.ACCESS_FINE_LOCATION
//            }, LOCATION_REQUEST_CODE);
            // here to request the missing permissions, and then overriding
//            @Override
//               public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{
//                    android.Manifest.permission.ACCESS_FINE_LOCATION
//            }, LOCATION_REQUEST_CODE);
//        }
    }

    private void setCameraPosition(GoogleMap mMap, ArrayList<Marker> markersList) {
        float[] distance = new float[1];
        HashMap<Marker, Float> distanceList = new HashMap<>();

        if (markersList == null) {
            Log.i("greece", "f");
        }
        Log.i("greece", String.valueOf(MapsActivity.markersList.size()));
        for (Marker m : markersList) {

            Location.distanceBetween(currentPosition.latitude, currentPosition.longitude,
                    m.getPosition().latitude, m.getPosition().longitude, distance);
            distanceList.put(m, distance[0]);
        }
        Log.i("greec", String.valueOf(distanceList.size()));


        List<Map.Entry<Marker, Float>> list = new LinkedList<>(distanceList.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<Marker, Float> >() {
            public int compare(Map.Entry<Marker, Float> o1,
                               Map.Entry<Marker, Float> o2)
            {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        Log.i("gree", String.valueOf(list.size()));

        Marker nearestMarker1 = list.get(0).getKey();
        Marker nearestMarker2 = list.get(1).getKey();
        Marker nearestMarker3 = list.get(2).getKey();
        //Marker nearestMarker4 = list.get(3).getKey();

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(nearestMarker1.getPosition());
        builder.include(nearestMarker2.getPosition());
        builder.include(nearestMarker3.getPosition());
     //   builder.include(nearestMarker4.getPosition());

        builder.include(currentPosition);

        LatLngBounds bounds = builder.build();


        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.10);

        //int padding = 0; // offset from edges of the map in pixels
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding));
    }

    private void addArchitectureMarkers() {
        try {
            // Get the text file
            InputStream file = getResources().openRawResource(R.raw.placeids);

            // check if file is not empty
            // if (file.exists() && file.length() != 0) {

            // read the file to get contents
            BufferedReader reader = new BufferedReader(new InputStreamReader(file));
            String line;

            // read every line of the file into the line-variable, on line at the time
            while ((line = reader.readLine()) != null) {
                String[] architecture = line.split(",", 2);
                System.out.println(architecture[0]);
                System.out.println(architecture[1]);

                String architectureName = architecture[0];
                String placeID = architecture[1];

                // Initialize Places.
                Places.initialize(getApplicationContext(), getString(R.string.google_directions_key));

                // Create a new Places client instance.
                PlacesClient placesClient = Places.createClient(this);

                // Specify the fields to return.
                List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG,
                        Place.Field.PHOTO_METADATAS);

                // Construct a request object, passing the place ID and fields array.
                FetchPlaceRequest request = FetchPlaceRequest.builder(placeID, placeFields)
                        .build();

                placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                    Place place = response.getPlace();
                    Log.i("TAG", "Place found: " + place.getName());
                    LatLng latlng = place.getLatLng();

                    //Get the photo metadata.
                    if (place.getPhotoMetadatas() == null) {
                        Log.i("itsnull", "lolzcop");
                    } else {
                        PhotoMetadata photoMetadata = place.getPhotoMetadatas().get(0);
                        if (place.getName().equals("The Cube")) {
                            photoMetadata = place.getPhotoMetadatas().get(2);
                        }
                        if (place.getName().equals("Millennium Point Car Park")) {
                            photoMetadata = place.getPhotoMetadatas().get(1);
                        }
                        if (place.getName().equals("School of Art")) {
                            photoMetadata = place.getPhotoMetadatas().get(1);
                        }
                        if (place.getName().equals("Saint Martin in the Bull Ring")) {
                            photoMetadata = place.getPhotoMetadatas().get(2);
                        }
                        if (place.getName().equals("Bullring & Grand Central")) {
                            photoMetadata = place.getPhotoMetadatas().get(1);
                        }
                        if (place.getName().equals("Mailbox Birmingham")) {
                            photoMetadata = place.getPhotoMetadatas().get(2);
                        }
                        if (place.getName().equals("The International Convention Centre")) {
                            photoMetadata = place.getPhotoMetadatas().get(1);
                        }
                        if (place.getName().equals("Birmingham Museum & Art Gallery")) {
                            photoMetadata = place.getPhotoMetadatas().get(2);
                        }
                        // Create a FetchPhotoRequest.
                        FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                                .setMaxWidth(200)
                                .setMaxHeight(200)
                                .build();

                        placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                            Bitmap bitmap = fetchPhotoResponse.getBitmap();

                            Bitmap bitmapWithBorder = Bitmap.createBitmap(bitmap.getWidth() + 12, bitmap.getHeight()
                                    + 12, bitmap.getConfig());
                            Canvas canvas = new Canvas(bitmapWithBorder);
                            canvas.drawColor(Color.rgb(255, 128, 128));
                            canvas.drawBitmap(bitmap, 6, 6, null);

                            Marker marker = mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(place.getLatLng().latitude, place.getLatLng().longitude))
                                    .title(architectureName)
                                    .icon(BitmapDescriptorFactory.fromBitmap(bitmapWithBorder)));
                            marker.setTag(placeID);
                            String url = getUrl(currentPosition, marker.getPosition(), "walking");
                            new FetchURL(MapsActivity.this).execute(url, "walking");
                            markersList.add(marker);
                            if (bitmap == null){
                                Log.i("Simran", "nay");
                            }
                            markerHashmap.put(marker.getTitle(), bitmap);
                            Log.i("Simran", bitmap.toString());
                            if (markersList.size() ==34){
                                setCameraPosition(mMap, markersList);
                            }
                        }).addOnFailureListener((exception) -> {
                            if (exception instanceof ApiException) {
                                ApiException apiException = (ApiException) exception;
                                int statusCode = apiException.getStatusCode();
                                // Handle error with given status code.
                                Log.e(TAG, "Place not found: " + exception.getMessage());
                            }
                        });
                    }
                }).addOnFailureListener((exception) -> {
                    if (exception instanceof ApiException) {
                        ApiException apiException = (ApiException) exception;
                        int statusCode = apiException.getStatusCode();
                        // Handle error with given status code.
                        Log.e("TAG", "Place not found: " + exception.getMessage());
                    }
                });
            }
            reader.close();
            Log.i("greecejs", String.valueOf(markersList.size()));
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void prince() {
        for (Marker m : markersList) {
            String url = getUrl(currentPosition, m.getPosition(), "walking");
            new FetchURL(getParent()).execute(url, "walking");
        }
    }

    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_directions_key);
        return url;
    }

    @Override
    public void onTaskDone(Object... values) {

        theDistance.add((String) values[0]);

        if (values[0] != null) {
            SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_content);
            supportMapFragment.getMapAsync(MapsActivity.this);
        }


//        if (currentPolyline != null)
//            currentPolyline.remove();
//        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
//
//    //    if (distance!= null)
//        Log.i("distance", values[1].toString());
//        distance = values[1].toString();
//        duration = values[2].toString();
//
//        String theSnip = "Distance: " + distance + ", " + duration + " walk";
//        destination.setSnippet(theSnip);
//        destination.showInfoWindow();
//        Log.i("why", String.valueOf(destination));
//
//        //  if (duration!= null)
//        Log.i("distance", values[2].toString());
//        Log.i("why", String.valueOf(destination));
    }

    @Override
    public void onListFragmentInteraction(Architecture.ArchitectureItem item) {
        Log.i("steg", "3");
        for (Marker m : markersList) {
            Log.i("steg", "2 "+m.getTitle());
            Log.i("steghaha", String.valueOf(markersList.size()));
            if (m.getTitle().equals(item.architectureTitle)) {

                Intent intent = new Intent(this, PopUpInfoWindow.class);
                Log.i("steg", item.architectureTitle);

                Bundle bundle = new Bundle();
                bundle.putString("MARKER", m.toString());
                bundle.putSerializable("MARKER_TITLE", m.getTitle());
                bundle.putParcelable("MARKER_LATLNG", m.getPosition());
                bundle.putParcelable("CURRENT_LATLNG", currentPosition);
                bundle.putString("MARKER_PLACEID", m.getTag().toString());
                destinationMarker = m;

                intent.putExtras(bundle);
                startActivity(intent);
            }
        }
    }
}
