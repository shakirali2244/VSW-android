package com.leanfleet.leanfleet;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MapsActivity extends FragmentActivity {


    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    public static Socket socket = null;
    public static String name = "no name";
    public static LatLng loc = null;
    public static LatLng me = null;
    public static String id = "-1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Intent intent = getIntent();
        id = intent.getStringExtra("DRIVER_ID");
        Log.d("SA :", id);
        setUpMapIfNeeded();
        try {
            socket = IO.socket("http://leanfleet.com:3001");
        } catch (URISyntaxException e1) {
            e1.printStackTrace();
        }
        socket.on("sendJob", new Emitter.Listener() {
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    //loc = new LatLng(data.getDouble("lat"), data.getDouble("lng"));
                    String url = "https://maps.googleapis.com/maps/api/geocode/json?address="+data.getString("endingPoint");
                    URL url2 = new URL(url);
                    HttpURLConnection conn = (HttpURLConnection) url2.openConnection();
                    conn.setRequestMethod("GET");
                    Log.d("LATLONG",)
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setupMe();
                        setUpMap();
                    }
                });
            }
        });
        Log.v("socketio", "Connection Made");
        if (socket == null) {
            finish();
        }
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

// Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                JSONObject obj = new JSONObject();
                try{
                    obj.put("lat",location.getLatitude());
                    obj.put("lng",location.getLongitude());
                    obj.put("name", "name");
                    obj.put("driver_id", id);
                }catch(JSONException e){
                    Log.d("JSON", "error");
                }
                me = new LatLng(location.getLatitude(),location.getLongitude());
                mMap.clear();
                setupMe();
                setUpMap();
                // Called when a new location is found by the network location provider.
                socket.emit("locationInfo", obj);
                Log.d("MAPS", "SENDING LOCATION TO SERVER");
            }


            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

// Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        socket.connect();

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                mMap.clear();
                setupMe();
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        //Log.d("MAP", "adding marker " + loc.getName());
        if(loc != null && me != null) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            GMapV2Direction md = new GMapV2Direction();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("wut")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // FIRE ZE MISSILES!
                        }
                    })
                    .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            // Create the AlertDialog object and return it
            builder.create();
            Log.d("MAPS", "updating my destination");
            mMap.addMarker(new MarkerOptions().position(loc).title("destination"));
            Log.d("MAPS","me and loc"+ me.toString() + loc.toString());
            Document doc = md.getDocument(me, loc, GMapV2Direction.MODE_DRIVING);

            if ( doc != null ) {
                //Log.d("DOCUMNET",doc.getDocumentURI());
                ArrayList<LatLng> directionPoint = md.getDirection(doc);
                PolylineOptions rectLine = new PolylineOptions().width(3).color(Color.RED);

                for(int i = 0 ; i < directionPoint.size() ; i++) {
                    rectLine.add(directionPoint.get(i));
                }
                mMap.addPolyline(rectLine);
            }
        }
    }
    private void setupMe(){
        if( me != null){
            Log.d("MAPS", "updating my location");
            mMap.addMarker(new MarkerOptions().position(me).title("me").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
        }
    }
}
