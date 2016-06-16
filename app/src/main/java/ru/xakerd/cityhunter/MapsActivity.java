package ru.xakerd.cityhunter;


import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends Activity {
    private String title;
    static final String EXTRA_LATITUDE="latitude",EXTRA_LONGITUDE="longitude";
    private LatLng latLng;
    private GoogleMap googleMap;
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
