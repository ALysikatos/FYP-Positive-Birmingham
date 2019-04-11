package com.example.positivebirmingham;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.android.volley.VolleyLog.TAG;

public class PopUpInfoWindow extends Activity {
    LinearLayout linearLayout1;

    LinearLayout layoutOfPopup;
    PopupWindow popupMessage;
    Button popupButton, insidePopupButton;
    TextView popupText;
    String markerClicked;
    String markerTitle;
    LatLng markerPosition;
    String markerPlaceID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        //linearLayout1 = (LinearLayout) findViewById(R.id.linearLayout1);

        Bundle bundle = intent.getExtras();
        markerClicked = bundle.getString("MARKER");
        markerTitle = bundle.getString("MARKER_TITLE");
        markerPosition = bundle.getParcelable("MARKER_LATLNG");
        markerPlaceID = bundle.getString("MARKER_PLACEID");
        Log.i("lol", markerPlaceID);
        Log.i("lol", String.valueOf(markerPosition));
      //  ArrayList<Marker> allMarkers = (ArrayList<Marker>) bundle.getSerializable("MARKER_ARRAY");
        //Log.i("lol", String.valueOf(allMarkers));

       setContentView(R.layout.popup);
        //getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setLayout();
        place();
        getPhoto();
    }

    private void setLayout(){

        Button close = findViewById(R.id.closePopup);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
//        TextView txtclose = findViewById(R.id.txtclose);
//        txtclose.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });

 //       TextView title = findViewById(R.id.txtTitle);
 //       title.setText(markerTitle);


    }

    private void getPlaceDetails() throws IOException {
      ////  String latlng = "latlng=" + markerPosition.latitude + "," + markerPosition.longitude;
     //   Log.i("lol", latlng);

      //  String url ="https://maps.googleapis.com/maps/api/geocode/json?" + latlng +
       //         "&location_type=ROOFTOP&result_type=street_address&key=" + getString(R.string.google_directions_key);
    }

    private void place(){
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

        // Add a listener to handle the response.
        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            Log.i(TAG, "Place found: " + place.getName());
            if (!place.getName().isEmpty()) {
                TextView title = findViewById(R.id.txtTitle);
                title.setText(place.getName());
            }
            Log.i(TAG, "Place found: " + place.getWebsiteUri());
            if (!(place.getWebsiteUri() == null)){
                TextView url = findViewById(R.id.txtLink);
                url.setText(place.getWebsiteUri().toString());
            }
            if (!(place.getRating() == null)) {
                int rate = (int) Math.round(place.getRating());
                RatingBar rateBar = findViewById(R.id.ratingBar);
                rateBar.setRating(rate);
                rateBar.setIsIndicator(true);
            }
            if (!(place.getUserRatingsTotal() == null)) {
                TextView noOfRatings = findViewById(R.id.txtRatings);
                noOfRatings.setText("(Average rating from " + place.getUserRatingsTotal() + " google reviews)");
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

    private void getPhoto(){
        List<Place.Field> fields = Arrays.asList(Place.Field.PHOTO_METADATAS);
        FetchPlaceRequest placeRequest = FetchPlaceRequest.builder(markerPlaceID, fields).build();
        PlacesClient placesClient = Places.createClient(this);
        placesClient.fetchPlace(placeRequest).addOnSuccessListener((response) -> {
            Place place = response.getPlace();

            // Get the photo metadata.
            PhotoMetadata photoMetadata = place.getPhotoMetadatas().get(0);

            // Get the attribution text.
            String attributions = photoMetadata.getAttributions();

            // Create a FetchPhotoRequest.
            FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                    .setMaxWidth(700) // Optional.
                    .setMaxHeight(700) // Optional.
                    .build();
            placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                Bitmap bitmap = fetchPhotoResponse.getBitmap();
                ImageView image = findViewById(R.id.archImage);
                image.setImageBitmap(bitmap);
            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    int statusCode = apiException.getStatusCode();
                    // Handle error with given status code.
                    Log.e(TAG, "Place not found: " + exception.getMessage());
                }
            });
        });


    }
}
