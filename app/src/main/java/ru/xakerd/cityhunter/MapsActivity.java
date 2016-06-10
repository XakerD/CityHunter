package ru.xakerd.cityhunter;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by User on 03.06.2016.
 */
public class MapsActivity extends AppCompatActivity {
    static String title;

    LatLng latLng;
    GoogleMap googleMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps);
        Bundle extras = getIntent().getExtras();
        double dlatitude = Double.parseDouble(extras.getString("latitude"));
        double dlongitude = Double.parseDouble(extras.getString("longitude"));
        latLng = new LatLng(dlatitude,dlongitude);
        title = Html.fromHtml(extras.getString("title")).toString();
        createMapView();
        addMarker();
    }
    private void createMapView(){
        /**
         * Catch the null pointer exception that
         * may be thrown when initialising the map
         */
        try {
            if(null == googleMap){
                googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                        R.id.maps_view)).getMap();

                /**
                 * If the map is still null after attempted initialisation,
                 * show an error to the user
                 */
                if(null == googleMap) {
                    Toast.makeText(getApplicationContext(),
                            "Error creating map", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (NullPointerException exception){
            Log.e("mapApp", exception.toString());
        }
    }

    private void addMarker(){

        /** Make sure that the map has been initialised **/
        if(null != googleMap){

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
}
