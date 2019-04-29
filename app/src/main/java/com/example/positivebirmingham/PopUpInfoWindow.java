package com.example.positivebirmingham;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.Arrays;
import java.util.List;

import static com.android.volley.VolleyLog.TAG;
import static com.example.positivebirmingham.MapsActivity.destinationMarker;
import static com.example.positivebirmingham.MapsActivity.mMap;
import static com.example.positivebirmingham.MapsActivity.markerHashmap;
import static com.example.positivebirmingham.MapsActivity.markersList;
import static com.example.positivebirmingham.MapsActivity.tabLayout;

public class PopUpInfoWindow extends Activity {

    private String markerTitle;
    private LatLng markerPosition;
    private LatLng currentPosition;
    private String markerPlaceID;
    private Bitmap markerImage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        //extract data sent in intent
        markerTitle = bundle.getString("MARKER_TITLE");
        markerPosition = bundle.getParcelable("MARKER_LATLNG");
        currentPosition = bundle.getParcelable("CURRENT_LATLNG");
        markerPlaceID = bundle.getString("MARKER_PLACEID");

        for (Marker m : markersList) {
            //get architecture picture
            if (m.getTitle().equals(markerTitle)) {
                Bitmap smallBitmap = markerHashmap.get(markerTitle);
                markerImage = Bitmap.createScaledBitmap(smallBitmap, (int) (smallBitmap.getWidth() * 3),
                        (int) (smallBitmap.getHeight() * 3), true);
            }
        }
        setContentView(R.layout.popup);
        setLayout();
        getPlaceInfo();
    }

    //get url to use Google Directions API
    private String getUrl(LatLng origin, LatLng destination, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + destination.latitude + "," + destination.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the Google Directions API web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_directions_key);
        return url;
    }

    //Set up pop-up windows layout
    private void setLayout() {
        ImageView image = findViewById(R.id.archImage);
        image.setImageBitmap(markerImage);

        TextView architectureDate = findViewById(R.id.txtDate);
        architectureDate.setText(Html.fromHtml("<b>Year Built:</b> " + MapsActivity.architectureDateHashmap.get(markerTitle)));

        TextView architectureInfo = findViewById(R.id.txtInfo);
        architectureInfo.setText(MapsActivity.architectureInfoHashmap.get(markerTitle));

        TextView architectureStyle = findViewById(R.id.txtStyle);
        architectureStyle.setText(Html.fromHtml("<b>Architectural Style:</b> " + MapsActivity.architectureStyleHashmap.get(markerTitle)));

        TextView architects = findViewById(R.id.txtArchitect);
        architects.setText(Html.fromHtml("<b>Architect:</b> " + MapsActivity.architectHashmap.get(markerTitle)));

        Button close = findViewById(R.id.closePopup);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //button to get directions from current location to architecture chosen
        Button getDirections = findViewById(R.id.directions);
        getDirections.setOnClickListener(v -> {
            String url = getUrl(currentPosition, markerPosition, "walking");
            //get route and show path on map
            new FetchURL(getParent()).execute(url, "walking");
            //move camera position to the destination marker to display info window with distance and duration
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destinationMarker.getPosition(), 16));

            if (tabLayout.getSelectedTabPosition() == 1) {
                tabLayout.getTabAt(0).select();
            }
            finish();
        });
    }

    /**
     * Use Google Places SDK to retrieve location details for architecture clicked
     * Reference used for help:
     * https://developers.google.com/places/android-sdk/client-migration
     */
    public void getPlaceInfo() {
        // Initialize Places.
        Places.initialize(getApplicationContext(), getString(R.string.google_directions_key));

        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(this);

        // Specify the fields to return.
        List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.WEBSITE_URI,
                Place.Field.RATING, Place.Field.USER_RATINGS_TOTAL);

        // Construct a request object, passing the place ID and fields array.
        FetchPlaceRequest request = FetchPlaceRequest.builder(markerPlaceID, placeFields)
                .build();

        // Add a listener to handle the response
        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            Log.i(TAG, "Place found: " + place.getName());
            if (!place.getName().isEmpty()) {
                TextView title = findViewById(R.id.txtTitle);
                SpannableString content = new SpannableString(place.getName());
                content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                title.setText(content);
            }
            if (!(place.getWebsiteUri() == null)) {
                TextView url = findViewById(R.id.txtLink);
                url.setMovementMethod(LinkMovementMethod.getInstance());
                String link = "<a href='" + place.getWebsiteUri().toString() + "'> Link To Website </a>";
                url.setText(Html.fromHtml(link));
            }
            if (!(place.getRating() == null)) {
                int rate = (int) Math.round(place.getRating());
                RatingBar rateBar = findViewById(R.id.ratingBar);
                rateBar.setRating(rate);
                rateBar.setIsIndicator(true);
            }
            if (!(place.getUserRatingsTotal() == null)) {
                TextView noOfRatings = findViewById(R.id.txtRatings);
                String ratingText = "(Average rating from " + place.getUserRatingsTotal() + " google reviews)";
                noOfRatings.setText(ratingText);
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
}
