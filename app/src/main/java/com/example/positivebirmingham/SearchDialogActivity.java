package com.example.positivebirmingham;

import android.app.Activity;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import static com.example.positivebirmingham.MapsActivity.currentPosition;
import static com.example.positivebirmingham.MapsActivity.mMap;
import static com.example.positivebirmingham.MapsActivity.markersList;

public class SearchDialogActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            searchApp(query);
            Log.i("lolzco", "working");
        }
    }

    private void searchApp(String query) {

        for (Marker m : markersList) {
            if (m.getTitle().toUpperCase().equals(query.toUpperCase())) {

                Intent intent = new Intent(this.getApplicationContext(), PopUpInfoWindow.class);

                Bundle bundle = new Bundle();
                bundle.putString("MARKER", m.toString());
                bundle.putSerializable("MARKER_TITLE", m.getTitle());
                bundle.putParcelable("MARKER_LATLNG", m.getPosition());
                bundle.putParcelable("CURRENT_LATLNG", currentPosition);
                bundle.putString("MARKER_PLACEID", String.valueOf(m.getTag()));
                MapsActivity.destinationMarker = m;

                intent.putExtras(bundle);
                startActivity(intent);
                this.finish();
                return;
            }
        }
        Toast toast = new Toast(this);
        toast.setDuration(Toast.LENGTH_LONG);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.search_not_found, null);
        toast.setView(view);
        toast.show();


//        Log.i("lalaal", String.valueOf(markersList.size()));
//        Toast.makeText(this, "Architecture Not Found", Toast.LENGTH_LONG).show();
        this.finish();
    }

}
