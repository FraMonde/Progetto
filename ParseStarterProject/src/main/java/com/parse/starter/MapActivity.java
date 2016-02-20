package com.parse.starter;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MapActivity extends AppCompatActivity implements SensorEventListener {

    //  private ResponseReceiver receiver;
    private CustomView customView;
    private IntentFilter filter;

    private ImageView semaforo;

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
    private float lastAzimuthRadians;
    private final float ALPHA = 0.2f;
    private int orientation;
    private boolean coloreSemaforo = false;
    private boolean cambiatoAzimut;
    public final String array_lat = "array_lat";  //ArrayList utenti Parse
    public final String array_long = "array_long"; //ArrayList utenti Parse

    private List<ParseUser> parseUserList = new ArrayList<ParseUser>();
    private ParseObject myGroup;
    private ArrayList<Float> longitudini;
    private ArrayList<Float> latitudini;
    private ArrayList<String> colori;
    private Timer timer;
    private boolean coordCambiate;
    private int numeroAmici = 0;
    private ArrayList<Float> ultimeLongitudini = new ArrayList<>();
    private ArrayList<Float> ultimeLatitudini = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_map);
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

        /*filter = new IntentFilter(ResponseReceiver.PROCESS_RESPONSE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new ResponseReceiver();
        registerReceiver(receiver, filter);*/

        customView.setOrientation(orientation);
        semaforo = (ImageView) findViewById(R.id.semaforo);

        lastAzimuthRadians = 100;

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mGrav = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagn = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        longitudini = new ArrayList<>();
        latitudini = new ArrayList<>();
        colori = new ArrayList<>();

        //startService(new Intent(MapActivity.this, LocationService.class));

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Log.i("Locationservice", "esecuzione");

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
                                query.whereNotEqualTo(UserKey.USERNAME_KEY,ParseUser.getCurrentUser().getUsername());
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


                    for (int i = 0; i < parseUserList.size(); i++) {

                        latitudini.clear();
                        longitudini.clear();
                        colori.clear();
                        latitudini.add((float) parseUserList.get(i).getDouble(UserKey.LAT_KEY));
                        longitudini.add((float) parseUserList.get(i).getDouble(UserKey.LNG_KEY));
                        colori.add(parseUserList.get(i).getString("colour"));   //TODO: colori

                    }
                    coordCambiate = false;
                    // TextView textView = (TextView)findViewById(R.id.text);
                    //Log.d("coloreSemaforo", String.valueOf(coloreSemaforo));
                    Log.d("cambiatoAzimuthOnCreate", String.valueOf(cambiatoAzimut));

                    if (coloreSemaforo) {
//                Log.d("BroadcastReceiver", "onReceive");
//                Log.d("cambiato azimuth", String.valueOf(cambiatoAzimut));

                        if (latitudini.size() != numeroAmici) {   //TODO: controllare sta mierda!
                            //Log.d("primo", "giro");
                            numeroAmici = latitudini.size();
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
                                       // Log.d("Coordinate", "cambiate");
                                        ultimeLatitudini.clear();
                                        ultimeLongitudini.clear();
                                        ultimeLongitudini.addAll(longitudini);
                                        ultimeLatitudini.addAll(latitudini);
                                        coordCambiate = true;
                                        break;
                                    }
                                }
                                if (coordCambiate) {
                                   // Log.d("receiver", String.valueOf(cambiatoAzimut));
                                    cambiatoAzimut = false;
                                    customView.setPoint(longitudini, latitudini, colori);
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
        timer.schedule(timerTask, 0, 4000);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this, mAccel);
        mSensorManager.unregisterListener(this, mMagn);
        mSensorManager.unregisterListener(this, mGrav);
        //unregisterReceiver(receiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccel, SensorManager.SENSOR_DELAY_NORMAL);  //SENSOR_DELAY_UI??
        mSensorManager.registerListener(this, mMagn, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGrav, SensorManager.SENSOR_DELAY_NORMAL);
        //registerReceiver(receiver, filter);

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

            float[] or = new float[9];
            SensorManager.getOrientation(mR, or);
            float inclination = (float) Math.round(Math.toDegrees(Math.acos(mR[8])));
            if (inclination < 15) {  //TODO: valutare > 155  prima era < 25
                if (!coloreSemaforo) {
                    semaforo.setBackgroundColor(Color.GREEN);
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

            } else if (coloreSemaforo) {
                semaforo.setBackgroundColor(Color.RED);
                coloreSemaforo = false;
                Toast.makeText(getApplicationContext(), "Tieni il cellulare piatto!", Toast.LENGTH_SHORT).show();
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
        SensorManager.getOrientation(R, mOrientation);
        float azimuthInRadians = mOrientation[0];
        // Log.d("azimuth", String.valueOf(azimuthInRadians));
        if (Math.abs(lastAzimuthRadians - azimuthInRadians) > 0.1745) {  //  20 gradi ---> 0.3491   30 gradi ---> 0.5236   10 gradi ---> 0.1745

            lastAzimuthRadians = azimuthInRadians;
            customView.setAzimuth(lastAzimuthRadians);
            if(latitudini.size()>0){
                customView.setPoint(longitudini,latitudini,colori);
                customView.invalidate();
            }


            //receiver.setCambiate(cambiatoAzimut);
        }
        Log.d("cambiatoAzimuth", String.valueOf(cambiatoAzimut));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

        Log.d("accuracy", String.valueOf(accuracy));

    }

    @Override
    protected void onDestroy() {  //TODO: guardare onPause
        super.onDestroy();
        stopService(new Intent(MapActivity.this, LocationService.class));
        //unregisterReceiver(receiver);
    }

   /* public class ResponseReceiver extends BroadcastReceiver {

        public static final String PROCESS_RESPONSE = "com.parse.starter.intent.action.PROCESS_RESPONSE";
        private ArrayList<Float> latitudini;
        private ArrayList<Float> longitudini;
        private ArrayList<Float> ultimeLatitudini = new ArrayList<>();
        private ArrayList<Float> ultimeLongitudini = new ArrayList<>();
        private ArrayList<String> colori;
        private int numeroAmici = 0;
        private boolean coordCambiate;
        private boolean cambiatoAzimut;

        @Override
        public void onReceive(Context context, Intent intent) {

            coordCambiate = false;
            // TextView textView = (TextView)findViewById(R.id.text);
            if (coloreSemaforo) {
//                Log.d("BroadcastReceiver", "onReceive");
//                Log.d("cambiato azimuth", String.valueOf(cambiatoAzimut));
                latitudini = (ArrayList<Float>) intent.getSerializableExtra(array_lat);
                longitudini = (ArrayList<Float>) intent.getSerializableExtra(array_long);
                colori = intent.getStringArrayListExtra("colori");

                if (latitudini.size() != numeroAmici) {   //TODO: controllare sta mierda!
                    Log.d("primo", "giro");
                    numeroAmici = latitudini.size();
                    ultimeLongitudini.addAll(longitudini);
                    ultimeLatitudini.addAll(latitudini);
                    customView.setPoint(longitudini, latitudini, colori);
                    customView.invalidate();
                    latitudini.clear();
                    longitudini.clear();

                } else {
                    if (ultimeLatitudini.size() > 0) {
                        for (int i = 0; i < ultimeLatitudini.size(); i++) {
                            if (Math.abs(ultimeLatitudini.get(i) - latitudini.get(i)) > 0.0004 || Math.abs(ultimeLongitudini.get(i) - longitudini.get(i)) > 0.0007) { //cambiamento di coord
                                Log.d("Coordinate", "cambiate");
                                ultimeLatitudini.clear();
                                ultimeLongitudini.clear();
                                ultimeLongitudini.addAll(longitudini);
                                ultimeLatitudini.addAll(latitudini);
                                coordCambiate = true;
                                break;
                            }
                        }
                        if (coordCambiate || cambiatoAzimut) {
                            Log.d("receiver", String.valueOf(cambiatoAzimut));
                            cambiatoAzimut = false;
                            customView.setPoint(longitudini, latitudini, colori);
                            customView.invalidate();
                            latitudini.clear();
                            longitudini.clear();
                        }
                    }
                }

            }

            //String s = String.valueOf(intent.getExtras().getInt("enne"));
            //textView.setText(latitudini.get(0));

        }

        public void setCambiate(boolean b) {
            cambiatoAzimut = b;
        }

    }*/


}
