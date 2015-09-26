package com.leanfleet.leanfleet;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class DashActivity extends ActionBarActivity {
    public static Socket socket = null;
    String id = "-1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash);
        Intent intent = getIntent();
        id = intent.getStringExtra("DRIVER_ID");
        Log.d("SA :",id);
        TextView tv1 = (TextView)findViewById(R.id.textView44);
        tv1.setText(id);

        try {
            socket = IO.socket("http://leanfleet.com:3001");
            Log.d("SOCKET","success");
        } catch (URISyntaxException e1) {
            e1.printStackTrace();
        }
        socket.emit("asdas","asdsd");
        socket.connect();
        socket.on("sendJob", new Emitter.Listener() {
                    public void call(Object... args) {
                        JSONObject data = (JSONObject) args[0];
                        Log.d("SOCKET", "rcvd data");
                    }
                });
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                Log.d("location",location.toString());
                JSONObject obj = new JSONObject();
                try{
                    obj.put("lat",location.getLatitude());
                    obj.put("lng",location.getLongitude());
                    obj.put("name", "name");
                    obj.put("driver_id", id);
                }catch(JSONException e){
                    Log.d("JSON", "error");
                }

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dash, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
