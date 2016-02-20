package com.parse.starter;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.ParseException;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Depa on 21/12/2015.
 */

public class LocationService extends Service {

    private Timer timer;
    private boolean start;
    private List<ParseUser> parseUserList;
    private ArrayList<Float> latitudini = new ArrayList<Float>();
    private ArrayList<Float> longitudini = new ArrayList<Float>();
    private ArrayList<String> colori = new ArrayList<>();
    private ParseObject myGroup;
    public final String array_lat = "array_lat";  //ArrayList utenti Parse
    public final String array_long = "array_long"; //ArrayList utenti Parse


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }




    @Override
    public void onCreate() {


        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Log.i("Locationservice", "esecuzione");

                ParseQuery<ParseObject> query = ParseQuery.getQuery("Group");
                query.whereEqualTo(UserKey.USERNAME_KEY, ParseUser.getCurrentUser());

                query.findInBackground(new FindCallback<ParseObject>() {

                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {

                        if (e == null) {
                            for (ParseObject group : objects) {
                                myGroup = group;
                                //Find the member of the group.
                                ParseRelation r = group.getRelation("members");
                                ParseQuery query = r.getQuery();
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
                    Intent broadcastIntent = new Intent();    //prova a fare new Intent(ResponseReceiver)
                    //broadcastIntent.setAction(MapActivity.ResponseReceiver.PROCESS_RESPONSE);
                    broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);

                    for (int i = 0; i < parseUserList.size(); i++) {

                        latitudini.add((float) parseUserList.get(i).getDouble(UserKey.LAT_KEY));
                        longitudini.add((float) parseUserList.get(i).getDouble(UserKey.LNG_KEY));
                        colori.add(parseUserList.get(i).getString("Colour"));   //TODO: colori

                    }

                    broadcastIntent.putExtra(array_lat, latitudini);
                    broadcastIntent.putExtra(array_long, longitudini);
                    broadcastIntent.putStringArrayListExtra("colori", colori);
                    sendBroadcast(broadcastIntent);

                    latitudini.clear();
                    longitudini.clear();
                    colori.clear();

                }
            }
        };
        timer = new Timer();
        timer.schedule(timerTask, 0, 4000);



    }

    @Override
    public void onDestroy() {
        Log.d("Location Service", "Distruzione Service");
        latitudini.clear();
        longitudini.clear();
        colori.clear();
        timer.cancel();
        timer = null;
        //Toast.makeText(getApplicationContext(), "Exit", Toast.LENGTH_SHORT).show();
    }

}


