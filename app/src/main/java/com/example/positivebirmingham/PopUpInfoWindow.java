package com.example.positivebirmingham;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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

import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

public class PopUpInfoWindow extends Activity {
    LinearLayout linearLayout1;

    LinearLayout layoutOfPopup;
    PopupWindow popupMessage;
    Button popupButton, insidePopupButton;
    TextView popupText;
    String markerClicked;
    String markerTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        //linearLayout1 = (LinearLayout) findViewById(R.id.linearLayout1);

        Bundle bundle = intent.getExtras();
        markerClicked = bundle.getString("MARKER");
        markerTitle = bundle.getString("MARKER_TITLE");
        Log.i("lol", markerClicked);
      //  ArrayList<Marker> allMarkers = (ArrayList<Marker>) bundle.getSerializable("MARKER_ARRAY");
        //Log.i("lol", String.valueOf(allMarkers));

       setContentView(R.layout.popup);
        //getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setLayout();
    }

    private void setLayout(){

        TextView title = (TextView) findViewById(R.id.txtTitle);
        title.setText(markerTitle);
    }

}
