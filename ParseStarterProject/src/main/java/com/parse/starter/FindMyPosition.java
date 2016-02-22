package com.parse.starter;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.parse.ParseUser;


/**
 * Created by francy on 17/02/16.
 */
public class FindMyPosition extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private ParseUser parseUser;
    private SharedPreferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);  //valutare bilanciato con battery

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());


        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addConnectionCallbacks(FindMyPosition.this)
                .addOnConnectionFailedListener(FindMyPosition.this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();

       // Log.i("ServicePosition", "onCreate");

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onConnected(Bundle bundle) {

        if (mGoogleApiClient.isConnected()) {

            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);


            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, new com.google.android.gms.location.LocationListener() {      // Find my position.
                        @Override
                        public void onLocationChanged(Location location) {

                            Log.i("ServicePosition", "locationChanged");
                            parseUser = ParseUser.getCurrentUser();
                            Location l = location;

                            double lat = l.getLatitude();
                            double longit = l.getLongitude();
                            parseUser.put(UserKey.LAT_KEY, lat);
                            parseUser.put(UserKey.LNG_KEY, longit);
                            parseUser.saveInBackground();

                            preferences.edit().putString(UserKey.PREF_LNG_KEY, String.valueOf(longit)).apply();
                            preferences.edit().putString(UserKey.PREF_LAT_KEY, String.valueOf(lat)).apply();

                        }
                    });

        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i("ServicePosition", "stop");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
