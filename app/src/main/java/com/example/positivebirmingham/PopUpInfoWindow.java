package com.example.positivebirmingham;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        try {
            getPlaceDetails();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

        TextView title = findViewById(R.id.txtTitle);
        title.setText(markerTitle);


    }

    private void getPlaceDetails() throws IOException {
      ////  String latlng = "latlng=" + markerPosition.latitude + "," + markerPosition.longitude;
     //   Log.i("lol", latlng);

      //  String url ="https://maps.googleapis.com/maps/api/geocode/json?" + latlng +
       //         "&location_type=ROOFTOP&result_type=street_address&key=" + getString(R.string.google_directions_key);
       // Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        //List<Address> addresses = geocoder.getFromLocation(markerPosition.latitude, markerPosition.longitude, 1);
     //   Geocoder geo = new Geocoder();
       // geocoder = new google.maps.Geocoder();
      //  Log.i("lol", String.valueOf(addresses));



    }

}
