package com.parse.starter;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MapActivity extends AppCompatActivity implements SensorEventListener, CustomView.CustomViewListener {

    CustomView customView;
    View semaforo_rosso;
    View semaforo_verde;
    TextView max_distance;

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
    private List<ParseUser> parseUserList = new ArrayList<>();
    private ArrayList<Float> latitudini;
    private ArrayList<Float> longitudini;
    private ArrayList<Float> ultimeLatitudini = new ArrayList<>();
    private ArrayList<Float> ultimeLongitudini = new ArrayList<>();
    private ArrayList<String> colori = new ArrayList<>();
    private boolean coloreSemaforo = false;
    private boolean cambiatoAzimut = false;
    private boolean coordCambiate;
    private boolean aggiornato = false;
    private float lastAzimuthRadians;
    private final float ALPHA = 0.2f;
    private int orientation;
    private int numeroAmici = 0;
    private Timer timer;
    private ParseObject myGroup;


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


        customView = (CustomView) findViewById(R.id.cv);
        customView.setOrientation(orientation);
        semaforo_rosso = (View) findViewById(R.id.semaforo_rosso);
        semaforo_verde = (View) findViewById(R.id.semaforo_verde);
        semaforo_rosso.getBackground().setAlpha(255);
        semaforo_verde.getBackground().setAlpha(100);
        max_distance = (TextView) findViewById(R.id.distance_tv);

        lastAzimuthRadians = 100;

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mGrav = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagn = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        latitudini = new ArrayList<>();
        longitudini = new ArrayList<>();
        colori = new ArrayList<>();

        coordCambiate = false;

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

                ParseQuery<ParseObject> query = ParseQuery.getQuery("Group");
                query.whereEqualTo("members", ParseUser.getCurrentUser());

                query.findInBackground(new FindCallback<ParseObject>() {

                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {

                        if (e == null) {
                            for (ParseObject group : objects) {
                                myGroup = group;
                                //Find the member of the group.
                                ParseRelation r = group.getRelation("members");
                                ParseQuery query = r.getQuery();
                                query.whereNotEqualTo(UserKey.USERNAME_KEY, ParseUser.getCurrentUser().getUsername());
                                try {
                                    List<ParseUser> members = query.find();
                                    parseUserList.clear();
                                    for (ParseUser m : members) {
                                        // Add just user that have accepted.
                                        if (m.getBoolean(UserKey.GROUP_KEY))
                                            parseUserList.add(m);
                                    }
                                } catch (ParseException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                    }
                });
                if (parseUserList != null && parseUserList.size() > 0) {

                    latitudini.clear();
                    longitudini.clear();
                    colori.clear();
                    for (int i = 0; i < parseUserList.size(); i++) {


                        latitudini.add((float) parseUserList.get(i).getDouble(UserKey.LAT_KEY));
                        longitudini.add((float) parseUserList.get(i).getDouble(UserKey.LNG_KEY));
                        colori.add(parseUserList.get(i).getString(UserKey.COLORS_KEY));

                    }

                    if (coloreSemaforo) {

                        if (latitudini.size() != numeroAmici) {
                            numeroAmici = latitudini.size();
                            ultimeLongitudini.clear();
                            ultimeLatitudini.clear();
                            ultimeLongitudini.addAll(longitudini);
                            ultimeLatitudini.addAll(latitudini);
                            customView.setPoint(longitudini, latitudini, colori);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    customView.invalidate();
                                }
                            });


                        } else {
                            if (ultimeLatitudini.size() > 0) {
                                for (int i = 0; i < ultimeLatitudini.size(); i++) {
                                    if (Math.abs(ultimeLatitudini.get(i) - latitudini.get(i)) > 0.0004 || Math.abs(ultimeLongitudini.get(i) - longitudini.get(i)) > 0.0007) { //cambiamento di coord
                                        ultimeLatitudini.clear();
                                        ultimeLongitudini.clear();
                                        ultimeLongitudini.addAll(longitudini);
                                        ultimeLatitudini.addAll(latitudini);
                                        coordCambiate = true;
                                        break;
                                    }
                                }
                                if (coordCambiate || cambiatoAzimut) {
                                    aggiornato = true;
                                    cambiatoAzimut = false;
                                    coordCambiate = false;
                                    customView.setPoint(longitudini, latitudini, colori);
                                    aggiornato = false;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            customView.invalidate();
                                        }
                                    });
                                }
                            }
                        }

                    }

                }
            }
        };
        timer = new Timer();
        timer.schedule(timerTask, 0, 2000);

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
            SensorManager.getRotationMatrix(mR, mI, mLastAccelerometer, mLastMagnetometer);
            float inclination = (float) Math.round(Math.toDegrees(Math.acos(mR[8])));
            if (inclination < 15) {
                if (!coloreSemaforo) {
                    semaforo_rosso.getBackground().setAlpha(100);
                    semaforo_verde.getBackground().setAlpha(255);
                    coloreSemaforo = true;
                }
                switch (orientation) {
                    case 0:
                        getAzimuth(mR);
                        break;
                    case 1:
                        SensorManager.remapCoordinateSystem(mR, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, outR);
                        getAzimuth(outR);
                        break;
                    case 3:
                        SensorManager.remapCoordinateSystem(mR, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X, outR);
                        getAzimuth(outR);
                        break;
                    default:
                        return;
                }

            } else {
                if (coloreSemaforo) {

                    semaforo_rosso.getBackground().setAlpha(255);
                    semaforo_verde.getBackground().setAlpha(100);
                    coloreSemaforo = false;
                    Toast.makeText(getApplicationContext(), "Tieni il cellulare piatto!", Toast.LENGTH_SHORT).show();
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

    private void getAzimuth(float[] R) {
        if (aggiornato) {
            cambiatoAzimut = false;
        }
        SensorManager.getOrientation(R, mOrientation);
        float azimuthInRadians = mOrientation[0];
        if (Math.abs(lastAzimuthRadians - azimuthInRadians) > 0.1745) {  //  20 gradi ---> 0.3491   30 gradi ---> 0.5236   10 gradi ---> 0.1745
            lastAzimuthRadians = azimuthInRadians;
            customView.setAzimuth(lastAzimuthRadians);
            cambiatoAzimut = true;
            Log.d("lastAzimut", String.valueOf(lastAzimuthRadians));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //CustomViewListener method.

    @Override
    public void maxDistanceChange(final float d) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                max_distance.setText("Amico più lontano: " + String.valueOf((int) d) + " m");
            }
        });
    }


}
