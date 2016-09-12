package com.tesseract.taxisharing.ui.activity;
//https://github.com/akexorcist/Android-GoogleDirectionLibrary
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tesseract.taxisharing.R;
import com.tesseract.taxisharing.model.UserLocation;

import java.text.DateFormat;
import java.util.Date;

import fr.quentinklein.slt.LocationTracker;
import fr.quentinklein.slt.TrackerSettings;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    public Integer count=1;

    FirebaseDatabase db;
    DatabaseReference ref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.tesseract.taxisharing.R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        db = FirebaseDatabase.getInstance();
        ref = db.getReference("userlocations");
        /*UserLocation userLocation=new UserLocation("amit",String.valueOf(20.04),String.valueOf(90.67), DateFormat.getTimeInstance().format(new Date()));
        ref.push().setValue(userLocation);
        UserLocation userLocation1=new UserLocation("Sonet",String.valueOf(21.04),String.valueOf(90.67), DateFormat.getTimeInstance().format(new Date()));
        ref.push().setValue(userLocation);*/



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

        TrackerSettings settings =
                new TrackerSettings()
                        .setUseGPS(true)
                        .setUseNetwork(true)
                        .setUsePassive(true)
                        .setTimeBetweenUpdates(1000)
                        .setMetersBetweenUpdates(1);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationTracker tracker = new LocationTracker(this, settings) {

            @Override
            public void onLocationFound(Location location) {
                // Do some stuff when a new location has been found.

                //mMap.clear();

                Toast.makeText(MapsActivity.this,"Location :"+location.describeContents()+location.getSpeed()+"\n"+location.getAltitude()+"\n"+location.getLatitude()+"\n"+location.getLongitude()+"\n"+location.getProvider()+"\n"+location.getAccuracy(), Toast.LENGTH_SHORT).show();

                updateLocation(location);
                //updateLocationInMap(location);
                setLocationInMapFromFireBase();

            }

            @Override
            public void onTimeout() {

                Toast.makeText(MapsActivity.this, "Time out", Toast.LENGTH_SHORT).show();
            }
        };
        tracker.startListening();


    }

    private void setLocationInMapFromFireBase() {

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mMap.clear();
                //Log.d("location", dataSnapshot.getValue().toString());
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    UserLocation pr = child.getValue(UserLocation.class);
                    setMarker(pr,pr.getUsername(),17.0f);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setMarker(UserLocation userLocation,String markerName,float zoom) {
        // Add a marker in Sydney and move the camera
        LatLng latLng = new LatLng(Double.parseDouble(userLocation.getLatitude()), Double.parseDouble(userLocation.getLongitude()));
        mMap.addMarker(new MarkerOptions().position(latLng).title(markerName));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera( CameraUpdateFactory.zoomTo( zoom) );
    }

    private void updateLocationInMap(Location location) {
        LatLng newlocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.addMarker(new MarkerOptions()
                        .position(newlocation)
                        .title(count.toString())
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher))

                //reference : http://stackoverflow.com/questions/14811579/how-to-create-a-custom-shaped-bitmap-marker-with-android-map-api-v2
        );
        count++;
        mMap.moveCamera(CameraUpdateFactory.newLatLng(newlocation));
        mMap.animateCamera( CameraUpdateFactory.zoomTo( 20.0f ) );
    }


    private void updateLocation(final Location location) {

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Log.d("location", dataSnapshot.getValue().toString());
                for (DataSnapshot child : dataSnapshot.getChildren()) {


                    UserLocation pr = child.getValue(UserLocation.class);



                    if(pr.getUsername().equals("amit")){
                        pr.setLatitude(String.valueOf(location.getLatitude()));
                        pr.setLongitude(String.valueOf(location.getLongitude()));
                        child.getRef().setValue(pr);
                        Toast.makeText(getApplicationContext(),"Sucessfully Updated",Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
