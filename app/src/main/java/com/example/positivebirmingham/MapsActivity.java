package com.example.positivebirmingham;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location currentLocation;
    private CameraPosition mCameraPosition;

    public FusedLocationProviderClient fusedLocationProviderClient;
    public ListFragment listFragment;
    public SupportMapFragment supportMapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            currentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        setContentView(R.layout.main_activity);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        //Now initialize the SupportMapFragment object from the activity’s layout file using findFragmentById
        // SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
        //          .findFragmentById(R.id.map);
        //Then attach OnMapReadyCallback listener on the object using getMapAsync(OnMapReadyCallback) API.
        // This listener will notify you when the map is ready by invoking onMapReady along with a GoogleMap object.
        //  GoogleMap is the main class of the Google Maps Android API and is the entry point for all methods
        // related to the map
        //  mapFragment.getMapAsync(this);

        getLocationPermission();
        supportMapFragment = new SupportMapFragment();
        listFragment = new ListFragment();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLastLocation();
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
        transaction.replace(R.id.fragment_content, fragment);
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
                        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_content);
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
                } else
                    Toast.makeText(this, "Location Permission Denied", Toast.LENGTH_SHORT).show();
                break;
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
        //MarkerOptions are used to create a new Marker.You can specify location, title etc with MarkerOptions
        MarkerOptions markerOptions = new MarkerOptions()
                .position(currentPosition)
                .title("You are Here")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 16));
        //map.animateCamera(CameraUpdateFactory.zoomTo(16), 2000, null);
        //add marker current location and move camera
        //Adding the created the marker on the map
        mMap.addMarker(markerOptions);
        addArchitectureMarkers();
        mMap.getUiSettings().setZoomControlsEnabled(true);
        //mMap.getUiSettings().setMapToolbarEnabled(False).
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

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
                List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG);

                // Construct a request object, passing the place ID and fields array.
                FetchPlaceRequest request = FetchPlaceRequest.builder(placeID, placeFields)
                        .build();

                placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                    Place place = response.getPlace();
                    Log.i("TAG", "Place found: " + place.getName());
                    LatLng latlng = place.getLatLng();

                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(place.getLatLng().latitude, place.getLatLng().longitude))
                            .title(architectureName)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
                    marker.setTag(placeID);
                    markersList.add(marker);

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
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
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
        for (Marker m : markersList) {
            if (m.getTitle().equals(item.architectureTitle)) {

                Intent intent = new Intent(MapsActivity.this, PopUpInfoWindow.class);

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
