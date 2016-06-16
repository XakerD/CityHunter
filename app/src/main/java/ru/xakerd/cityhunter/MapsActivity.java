package ru.xakerd.cityhunter;


import android.app.Activity;
import android.os.Bundle;
import android.text.Html;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends Activity implements OnMapReadyCallback {
    private String title;
    static final String EXTRA_LATITUDE="latitude",EXTRA_LONGITUDE="longitude";
    private LatLng latLng;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Bundle extras = getIntent().getExtras();
        double latitude = Double.parseDouble(extras.getString(EXTRA_LATITUDE));
        double longitude = Double.parseDouble(extras.getString(EXTRA_LONGITUDE));
        title = Html.fromHtml(extras.getString(InfoActivity.EXTRA_TITLE)).toString();
        latLng = new LatLng(latitude,longitude);
        createMapView();
    }
    private void createMapView(){

                MapFragment mapFragment = (MapFragment) getFragmentManager()
                        .findFragmentById(R.id.maps_view);
                mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(title)
                .draggable(false)
        );
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng).zoom(13).build();
        googleMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));
    }
}
