package com.example.positivebirmingham;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.android.volley.VolleyLog.TAG;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
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

    private Location currentLocation;
    public ListFragment listFragment;
    public SupportMapFragment supportMapFragment;

    public static HashMap<String, Bitmap> markerHashmap = new HashMap<>();
    public static HashMap<String, String> architectureDateHashmap = new HashMap<>();
    public static HashMap<String, String> architectureInfoHashmap = new HashMap<>();
    public static HashMap<String, String> architectureStyleHashmap = new HashMap<>();
    public static HashMap<String, String> architectHashmap = new HashMap<>();
    public static AlertDialog loadingDialog;
    public static InputMethodManager inputManager;
    public static AutoCompleteTextView searchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Saved state for if phone orientation changed
        if (savedInstanceState != null) {
            supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentByTag(SUPPORT_FRAGMENT_TAG);
            listFragment = (ListFragment) getSupportFragmentManager().findFragmentByTag(LIST_FRAGMENT_TAG);
            currentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }
        //only create fragments if they haven't been instantiated already
        if (supportMapFragment == null) {
            supportMapFragment = new SupportMapFragment();
            supportMapFragment.setRetainInstance(true);
        }
        if (listFragment == null) {
            listFragment = new ListFragment();
            listFragment.setRetainInstance(true);
        }
        setContentView(R.layout.main_activity);
        //set up search bar
        searchBar = findViewById(R.id.searchbar);
        inputManager = (InputMethodManager) MapsActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
        Button searchButton = this.findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBar.setVisibility(View.VISIBLE);
                searchBar.requestFocus();
                inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                String[] architectureNames = new String[markersList.size()];
                int counter = 0;
                for (Marker m : markersList) {
                    architectureNames[counter] = m.getTitle();
                    counter++;
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MapsActivity.this, R.layout.search_text, architectureNames);
                searchBar.setAdapter(adapter);
                searchBar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View arg1, int pos, long id) {
                        String input = searchBar.getText().toString();
                        searchBar.setText("");
                        for (Marker m : markersList) {
                            if (m.getTitle().toUpperCase().equals(input.toUpperCase())) {
                                //display pop-up when architecture clicked from search list
                                Intent intent = new Intent(MapsActivity.this, PopUpInfoWindow.class);

                                Bundle bundle = new Bundle();
                                bundle.putSerializable("MARKER_TITLE", m.getTitle());
                                bundle.putParcelable("MARKER_LATLNG", m.getPosition());
                                bundle.putParcelable("CURRENT_LATLNG", currentPosition);
                                bundle.putString("MARKER_PLACEID", String.valueOf(m.getTag()));
                                MapsActivity.destinationMarker = m;

                                intent.putExtras(bundle);
                                startActivity(intent);
                                searchBar.setVisibility(View.GONE);
                                return;
                            }
                        }
                    }
                });
                searchBar.setOnDismissListener(new AutoCompleteTextView.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        searchBar.setVisibility(View.GONE);
                        searchBar.setText("");
                        inputManager.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
                        searchBar.clearFocus();
                    }
                });
            }
        });
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
            String KEY_TAB_INDEX = "tab_index";
            outState.putInt(KEY_TAB_INDEX, tabLayout.getSelectedTabPosition());
            super.onSaveInstanceState(outState);
        }
    }

    //create the Map View and List View tabs
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
        View root = tabLayout.getChildAt(0);
        //create divider between the tabs
        if (root instanceof LinearLayout) {
            ((LinearLayout) root).setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
            GradientDrawable drawable = new GradientDrawable();
            drawable.setColorFilter(0xffff0000, PorterDuff.Mode.MULTIPLY);
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

    //change fragment shown on screen
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.addToBackStack(null);
        if (fragment == listFragment) {
            transaction.replace(R.id.fragment_content, fragment, LIST_FRAGMENT_TAG);
        } else if (fragment == supportMapFragment) {
            transaction.replace(R.id.fragment_content, fragment, SUPPORT_FRAGMENT_TAG);
        }
        transaction.commit();
    }

    /**
     * Gets the current location of the device
     */
    private void fetchLastLocation() {
        try {
            FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            //get last known location of device
            Task<Location> task = fusedLocationProviderClient.getLastLocation();

            task.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        //store current location
                        currentLocation = location;
                        Toast.makeText(MapsActivity.this, currentLocation.getLatitude() + " " + currentLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                        // Obtain the SupportMapFragment
                        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_content);
                        // Attach OnMapReadyCallback listener using getMapAsync(OnMapReadyCallback)
                        // This listener notified when the map is ready by invoking onMapReady along with a GoogleMap object.
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

    private void getLocationPermission() {
        //Checking if the user hasn't granted location permission for this app
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //request location permission
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION
            }, LOCATION_REQUEST_CODE);
        }
        fetchLastLocation();
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE:
                // If request is denied, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Permission Granted
                    fetchLastLocation();
                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(MapsActivity.this).create();
                    alertDialog.setTitle("   Location Permission Denied!");
                    alertDialog.setMessage("\nDefault Birmingham Location Used:\n          New Street Train Station");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                    //Set default current location of New Street
                    Location defaultLocation = new Location("LocationManager.GPS_PROVIDER");
                    defaultLocation.setLatitude(52.478060);
                    defaultLocation.setLongitude(-1.898493);
                    currentLocation = defaultLocation;
                    supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_content);
                    supportMapFragment.getMapAsync(MapsActivity.this);
                    break;
                }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        currentPosition = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        MarkerOptions currentPositionMarker = new MarkerOptions()
                .position(currentPosition)
                .title("You are Here")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
        // Add current location marker to map
        mMap.addMarker(currentPositionMarker);
        addArchitectureMarkers();
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                searchBar.setVisibility(View.GONE);
                searchBar.setText("");
                inputManager.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
            }
        });
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                          @Override
                                          public boolean onMarkerClick(Marker marker) {
                                              Log.i("testing", String.valueOf(marker.getPosition()));
                                              if (!marker.getPosition().equals(currentPosition)) {
                                                  //display pop-up when marker clicked
                                                  Intent intent = new Intent(MapsActivity.this, PopUpInfoWindow.class);

                                                  Bundle bundle = new Bundle();
                                                  bundle.putSerializable("MARKER_TITLE", marker.getTitle());
                                                  bundle.putParcelable("MARKER_LATLNG", marker.getPosition());
                                                  bundle.putParcelable("CURRENT_LATLNG", currentPosition);
                                                  bundle.putString("MARKER_PLACEID", marker.getTag().toString());
                                                  destinationMarker = marker;
                                                  //send data to new activity
                                                  intent.putExtras(bundle);
                                                  startActivity(intent);
                                              }
                                              return false;
                                          }
                                      }
        );
        //create loading screen on start up
        loadingDialog = new AlertDialog.Builder(this).create();
        loadingDialog.setCancelable(false);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.progress_bar_dialog, null);
        loadingDialog.setView(dialogView);
        loadingDialog.show();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    /**
     * Moves camera position on map at start up to show current location marker
     * and the 3 nearest architecture markers.
     */
    private void setCameraPosition(GoogleMap mMap, ArrayList<Marker> markersList) {
        float[] distance = new float[1];
        HashMap<Marker, Float> distanceHashMap = new HashMap<>();

        for (Marker m : markersList) {
            Location.distanceBetween(currentPosition.latitude, currentPosition.longitude,
                    m.getPosition().latitude, m.getPosition().longitude, distance);
            distanceHashMap.put(m, distance[0]);
        }

        //sort by distance shortest to furthest.
        List<Map.Entry<Marker, Float>> list = new LinkedList<>(distanceHashMap.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<Marker, Float>>() {
            public int compare(Map.Entry<Marker, Float> o1,
                               Map.Entry<Marker, Float> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });
        Marker nearestMarker1 = list.get(0).getKey();
        Marker nearestMarker2 = list.get(1).getKey();
        Marker nearestMarker3 = list.get(2).getKey();

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(nearestMarker1.getPosition());
        builder.include(nearestMarker2.getPosition());
        builder.include(nearestMarker3.getPosition());
        builder.include(currentPosition);

        LatLngBounds bounds = builder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.10);
        //move camera into position
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding));
    }

    //Add all architecture markers to the map
    private void addArchitectureMarkers() {
        try {
            // Get the text file
            InputStream file = getResources().openRawResource(R.raw.architecture);

            // Read the file to get contents
            BufferedReader reader = new BufferedReader(new InputStreamReader(file));
            String line;

            // Read every line of the file one line at a time
            while ((line = reader.readLine()) != null) {
                String[] architecture = line.split(";", 6);

                String architectureName = architecture[0];
                String placeID = architecture[1];
                String architectureDate = architecture[2];
                String architectureInfo = architecture[3];
                String architectureStyle = architecture[4];
                String architect = architecture[5];

                // Initialize Places.
                Places.initialize(getApplicationContext(), getString(R.string.google_directions_key));
                // Create a new Places client instance.
                PlacesClient placesClient = Places.createClient(this);
                // Specify the fields to return.
                List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG,
                        Place.Field.PHOTO_METADATAS);
                // Construct a request object, passing the place ID and fields array.
                FetchPlaceRequest request = FetchPlaceRequest.builder(placeID, placeFields).build();

                placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                    Place place = response.getPlace();
                    Log.i("TAG", "Place found: " + place.getName());
                    LatLng latlng = place.getLatLng();

                    //Get the photo metadata.
                    if (place.getPhotoMetadatas() == null) {
                        Log.i("mylog", "no photo");
                    } else {
                        //choose better default pictures for these buildings
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
                            photoMetadata = place.getPhotoMetadatas().get(1);
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
                            // Add border to image
                            Bitmap bitmapWithBorder = Bitmap.createBitmap(bitmap.getWidth() + 12, bitmap.getHeight()
                                    + 12, bitmap.getConfig());
                            Canvas canvas = new Canvas(bitmapWithBorder);
                            canvas.drawColor(Color.rgb(255, 128, 128));
                            canvas.drawBitmap(bitmap, 6, 6, null);
                            //Addd marker onto map
                            Marker marker = mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(latlng.latitude, latlng.longitude))
                                    .title(architectureName)
                                    .icon(BitmapDescriptorFactory.fromBitmap(bitmapWithBorder)));
                            marker.setTag(placeID);
                            String url = getUrl(currentPosition, marker.getPosition(), "walking");
                            new FetchURL(MapsActivity.this).execute(url, "walking");
                            //Store marker info
                            markersList.add(marker);
                            markerHashmap.put(marker.getTitle(), bitmap);
                            architectureDateHashmap.put(marker.getTitle(), architectureDate);
                            architectureInfoHashmap.put(marker.getTitle(), architectureInfo);
                            architectureStyleHashmap.put(marker.getTitle(), architectureStyle);
                            architectHashmap.put(marker.getTitle(), architect);
                            if (markersList.size() == 34) {
                                //Set camera position once all markers loaded in
                                setCameraPosition(mMap, markersList);
                            }
                        }).addOnFailureListener((exception) -> {
                            if (exception instanceof ApiException) {
                                ApiException apiException = (ApiException) exception;
                                int statusCode = apiException.getStatusCode();
                                // Handle error with given status code.
                                Log.e(TAG, "Photo not found: " + exception.getMessage());
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
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Fetch URL from Google Directions API web service to get route path
    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode of transport
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_directions_key);
        return url;
    }

    /**
     * Display pop-up with details when an architectural building is clicked on the list view.
     */
    @Override
    public void onListFragmentInteraction(Architecture.ArchitectureItem item) {
        for (Marker m : markersList) {
            if (m.getTitle().equals(item.architectureTitle)) {
                Intent intent = new Intent(this, PopUpInfoWindow.class);

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
