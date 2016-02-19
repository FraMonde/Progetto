package com.parse.starter;

import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class MapActivity extends AppCompatActivity implements SensorEventListener {

    private static final String MEMBER_KEY = "members";
    private static final String GROUP_KEY = "Group";

    private ParseObject myGroup;
    private Timer timer;

    MapView customView;

    private SensorManager mSensorManager;
    private Sensor mGrav;
    private Sensor mAccel;
    private Sensor mMagn;
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private float[] mR = new float[9];
    private float[] outR = new float[9];
    private float[] mI = new float[9];
    private float[] mOrientation = new float[3];
    private List<ParseUser> parseUserList;
    private ArrayList<Float> latitudini;
    private ArrayList<Float> longitudini;
    private ArrayList<String> colori = new ArrayList<>();
    private ArrayList<String> nomi = new ArrayList<>();
    private float lastAzimuthRadians;
    private final float ALPHA = 0.2f;
    private int orientation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        Display display = getWindowManager().getDefaultDisplay();
        orientation = display.getRotation();
        switch (orientation) {
            case 0:
                orientation = 0;   //portrait
                break;
            case 1:
                orientation = 1;   //landscape +90
                break;
            case 3:
                orientation = 3;  //landscape -90
                break;
            default:
                return;
        }

        customView = (MapView) findViewById(R.id.cv);
        customView.setOrientation(orientation);

        lastAzimuthRadians = 100;

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mGrav = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagn = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this, mAccel);
        mSensorManager.unregisterListener(this, mMagn);
        mSensorManager.unregisterListener(this, mGrav);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccel, SensorManager.SENSOR_DELAY_NORMAL);  //SENSOR_DELAY_UI??
        mSensorManager.registerListener(this, mMagn, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGrav, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        switch (event.sensor.getType()) {
            case Sensor.TYPE_GRAVITY:
                mLastAccelerometer = lowPass(event.values, mLastAccelerometer);
                mLastAccelerometerSet = true;
                break;
            case Sensor.TYPE_ACCELEROMETER:
                mLastAccelerometer = lowPass(event.values, mLastAccelerometer);
                mLastAccelerometerSet = true;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mLastMagnetometer = lowPass(event.values, mLastMagnetometer);
                mLastMagnetometerSet = true;
                break;
            default:
                return;
        }

        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            boolean friends = getFriendsPosition();
            if (friends) {
                SensorManager.getRotationMatrix(mR, mI, mLastAccelerometer, mLastMagnetometer);

                float inclination = (float) Math.round(Math.toDegrees(Math.acos(mR[8])));
                Log.d("incl", String.valueOf(inclination));

                if (inclination < 25 || inclination > 155) {  //TODO: valutare > 155
                    // Log.d("incl", String.valueOf(inclination));
                    switch (orientation) {
                        case 0:
                            getAzimuthFlatPortrait();
                            break;
                        case 1:
                            getAzimuthFlatNinety();
                            break;
                        case 3:
                            getAzimuthFlatMinusNinety();
                            break;
                        default:
                            return;
                    }

                } else {
                    switch (orientation) {
                        case 0:
                            getAzimuthNoFlatPortrait();
                            break;
                        case 1:
                            getAzimuthNoFlatNinety();
                            break;
                        case 3:
                            getAzimuthNoFlatMinusNinety();
                            break;
                        default:
                            return;
                    }


                }

            }


        }

    }

    private float[] lowPass(float[] values, float[] accelerometer) {
        if (accelerometer == null) {
            return values;
        }
        for (int i = 0; i < values.length; i++) {

            accelerometer[i] = accelerometer[i] + ALPHA * (values[i] - accelerometer[i]);
        }
        return accelerometer;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    // Get all members of mine group.
    private boolean getFriendsPosition() {
        final ParseUser currentUser = ParseUser.getCurrentUser();

        ParseQuery<ParseObject> query = ParseQuery.getQuery(GROUP_KEY);
        query.whereEqualTo(MEMBER_KEY, currentUser);

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {

                if (e == null) {
                    for (ParseObject group : objects) {
                        myGroup = group;
                        //Find the member of the group.
                        ParseRelation r = group.getRelation(MEMBER_KEY);
                        ParseQuery query = r.getQuery();
                        try {
                            List<ParseUser> members = query.find();
                            parseUserList.clear();
                            for (ParseUser m : members) {
                                parseUserList.add(m);
                            }
                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }
                    }
                } else {
                    Toast.makeText(MapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (parseUserList != null && parseUserList.size() > 0) {
            longitudini = new ArrayList<>();
            latitudini = new ArrayList<>();
            for (int i = 0; i < parseUserList.size(); i++) {

                latitudini.add((float) parseUserList.get(i).getDouble(UserKey.LAT_KEY));
                longitudini.add((float) parseUserList.get(i).getDouble(UserKey.LNG_KEY));
                //TODO:????
                colori.add(parseUserList.get(i).getString("Colour"));
                nomi.add(parseUserList.get(i).getString("username"));   //TODO: valutare i nomi
            }
            return true;
        } else
            return false;
    }

    private void getAzimuthFlatPortrait() {
        getAzimuth(mR);

    }

    private void getAzimuthFlatNinety() {
        SensorManager.remapCoordinateSystem(mR, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, outR);
        getAzimuth(outR);

    }

    private void getAzimuthFlatMinusNinety() {
        SensorManager.remapCoordinateSystem(mR, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X, outR);
        getAzimuth(outR);

    }

    private void getAzimuthNoFlatPortrait() {
        SensorManager.remapCoordinateSystem(mR, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);
        getAzimuth(outR);

    }

    private void getAzimuthNoFlatNinety() {
        SensorManager.remapCoordinateSystem(mR, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);
        SensorManager.remapCoordinateSystem(outR, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_MINUS_X, outR);
        getAzimuth(outR);

    }

    private void getAzimuthNoFlatMinusNinety() {
        SensorManager.remapCoordinateSystem(mR, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);
        SensorManager.remapCoordinateSystem(outR, SensorManager.AXIS_Y, SensorManager.AXIS_X, outR);
        getAzimuth(outR);

    }

    private void getAzimuth(float[] R) {
        SensorManager.getOrientation(R, mOrientation);
        float azimuthInRadians = mOrientation[0];
        if (Math.abs(lastAzimuthRadians - azimuthInRadians) > 0.3491) {  //  20 gradi ---> 0.3491   30 gradi ---> 0.5236
            lastAzimuthRadians = azimuthInRadians;

            //textRadians.setText(String.valueOf(lastAzimuthRadians));
            Float objectAzimuth = new Float(lastAzimuthRadians);
            customView.setAzimuth(objectAzimuth);

            customView.setPoint(longitudini, latitudini, colori, nomi);
            customView.invalidate();
            latitudini.clear();
            longitudini.clear();
            colori.clear();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
