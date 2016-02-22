package com.parse.starter;

import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by francy on 08/02/16.
 */
public class FindBluetoothService extends Service {

    private Timer timer;
    private BluetoothAdapter bluetoothAdapter;
    private boolean[] passati = new boolean[2];
    private boolean counted; //Used to know if I'm already registered in that lift.
    private HashMap<String, String> lifts;
    private Set keys;
    private Notification notification;
    private boolean finished = true;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("bluettothService", "onCreate");
        lifts = new HashMap<String, String>();
        lifts.put("E0:F8:47:30:19:E5", "XMEgkvbYQC");
        lifts.put("48:5A:B6:67:3A:28", "kailPiuHYB");

        keys = lifts.keySet();

        passati[0] = false;
        passati[1] = false;

        counted = false;

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(mReceiver, filter);

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

                if (finished) {
                    Log.i("ServiceBluetooth", "esecuzione");
                    finished = false;
                    bluetoothAdapter.startDiscovery();
                }

            }
        };
        timer = new Timer();
        timer.schedule(timerTask, 0, 4000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        timer = null;
        unregisterReceiver(mReceiver);
        Log.d("ServiceBluetooth", "stop");
    }


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        private String id;

        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                //discovery starts, we can show progress dialog or perform other tasks
                Log.i("ServiceBluetooth", "discovery started");
                //finished = false;
                if (passati[1]) {
                    passati[0] = true;
                    passati[1] = false;
                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //discovery finishes, dismiss progress dialog
                Log.i("ServiceBluetooth", "discovery finshed");

                finished = true;
                if (passati[0] && !passati[1]) {   // Out of the lift.

                    passati[0] = false;
                    passati[1] = false;
                    counted = false;

                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Lift");
                    query.getInBackground(id, new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject object, ParseException e) {
                            if (e == null) {
                                int numeroPersone = object.getInt("Person");
                                numeroPersone--;
                                object.put("Person", numeroPersone);
                                object.saveInBackground();
                            } else {
                                e.printStackTrace();
                            }
                        }
                    });
                    id = null;

                }

            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found.
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                Iterator i = keys.iterator();
                while (i.hasNext()) {
                    String bluetooth = (String) i.next();
                    if (device.getAddress().equals(bluetooth)) {   //It's an interesting bluetooth.

                        Log.i("ServiceBluetooth", "discovery found");

                        id = (String) lifts.get(bluetooth);
                        passati[1] = true;
                        if (!counted) {
                            //If I haven't found that lift yet I've to increment the number of people.
                            counted = true;
                            ParseQuery<ParseObject> query = ParseQuery.getQuery("Lift");
                            query.getInBackground(id, new GetCallback<ParseObject>() {
                                @Override
                                public void done(ParseObject object, ParseException e) {
                                    if (e == null) {
                                        int personNumber = object.getInt("Person");
                                        personNumber++;
                                        object.put("Person", personNumber);
                                        object.saveInBackground();
                                    } else {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                        break;
                    }

                }

            }
        }
    };
}
