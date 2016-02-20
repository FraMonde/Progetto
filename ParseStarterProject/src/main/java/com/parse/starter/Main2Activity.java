package com.parse.starter;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Main2Activity extends AppCompatActivity implements HomeFragment.OnHomeFragmentInteractionListener, GroupFragment.OnGroupFragmentInteractionListener, MyGroupFragment.OnMyGroupFragmentListener {

    private static final String MEMBER_KEY = "members";
    private static final String GROUP_KEY = "Group";
    private static final String BOOL_GROUP_KEY = "hasGroup";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final String ITEM_SELECTED_KEY = "Menu";

    private DrawerLayout mDrawer;
    private NavigationView nvDrawer;
    private Toolbar toolbar;
    private TextView usernameText;
    private TextView emailText;

    private BluetoothAdapter bluetoothAdapter;
    private int itemIdSelected;
    private Timer timer;
    private SharedPreferences pref;
    private ProgressDialog pdia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        pref = PreferenceManager.getDefaultSharedPreferences(Main2Activity.this);

        // Check If the user belongs to a group.
        // Network call is called every 30 seconds.
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                hasGroup();

            }
        };
        timer = new Timer();
        timer.schedule(timerTask, 0, 30000);

        if(pref.getBoolean(BOOL_GROUP_KEY,false)) {
            startService(new Intent(Main2Activity.this, FindMyPosition.class));
        }

        // Set a Toolbar to replace the ActionBar.
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Find our drawer view
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Find our drawer view
        nvDrawer = (NavigationView) findViewById(R.id.nvView);
        // Setup drawer view
        setupDrawerContent(nvDrawer);

        View header = nvDrawer.getHeaderView(0);
        usernameText = (TextView) header.findViewById(R.id.username_text);
        usernameText.setText(ParseUser.getCurrentUser().getUsername());
        emailText = (TextView) header.findViewById(R.id.email_text);
        emailText.setText(ParseUser.getCurrentUser().getEmail());

        Fragment fragment = HomeFragment.newInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
        itemIdSelected = R.id.nav_home;

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {

            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            // Ensures Bluetooth is available on the device and it is enabled. If not,
            // displays a dialog requesting user permission to enable Bluetooth.
            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        pref.edit().putInt(ITEM_SELECTED_KEY, itemIdSelected).apply();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        int p = pref.getInt(ITEM_SELECTED_KEY, R.id.nav_home);
        MenuItem i = nvDrawer.getMenu().findItem(p);
        if(i == null)
            i = nvDrawer.getMenu().findItem(R.id.nav_home);
        selectDrawerItem(i);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        timer = null;
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the planet to show based on
        // position
        Fragment fragment = null;

        Class fragmentClass = null;
        switch (menuItem.getItemId()) {
            case R.id.nav_home:
                fragmentClass = HomeFragment.class;
                break;
            case R.id.nav_lift:
                fragmentClass = LiftFragment.class;
                break;
            case R.id.nav_chalet:
                fragmentClass = ChaletFragment.class;
                break;
            case R.id.nav_group:
                if (ParseUser.getCurrentUser().getBoolean("group"))
                    fragmentClass = MyGroupFragment.class;
                else
                    fragmentClass = GroupFragment.class;
                break;
           /* case R.id.nav_settings:
                fragmentClass = HomeFragment.class;
                break; */
            case R.id.nav_logout:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pdia = new ProgressDialog(Main2Activity.this);
                        pdia.setMessage("Loading...");
                        pdia.show();
                    }
                });
                ParseUser.logOut();
                Intent intent = new Intent(Main2Activity.this, DispatchActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                if (pdia.isShowing()) {
                    pdia.dismiss();
                }
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                startActivity(intent);
                return;
            default:
                fragmentClass = HomeFragment.class;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

        // Highlight the selected item, update the title, and close the drawer
        menuItem.setChecked(true);
        setTitle(menuItem.getTitle());
        mDrawer.closeDrawers();
        itemIdSelected = menuItem.getItemId();
    }

    // Used to check If the user's been added to a group.
    private void hasGroup() {

        // Check if the user's group.
        if (!(ParseUser.getCurrentUser().getBoolean(UserKey.GROUP_KEY))) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery(GROUP_KEY);
            query.whereEqualTo(MEMBER_KEY, ParseUser.getCurrentUser());
            pref.getBoolean(BOOL_GROUP_KEY, false);

            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(final List<ParseObject> objects, ParseException e) {
                    if (e == null) {
                        if (objects.size() > 0) {
                            // The user is in a group but he's not accepted yet.
                            new AlertDialog.Builder(Main2Activity.this)
                                    .setTitle("Avviso")
                                    .setMessage("Sei invitato ad entrare nel gruppo " + objects.get(0).getString("Name") + ". Accetti l'invito?")
                                    .setPositiveButton(R.string.accept_group, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) { // Accept.
                                            ParseUser.getCurrentUser().put(UserKey.GROUP_KEY, true);
                                            ParseUser.getCurrentUser().saveInBackground();
                                            // Start the location service.
                                            startService(new Intent(Main2Activity.this, FindMyPosition.class));
                                            pref.edit().putBoolean(BOOL_GROUP_KEY, true).apply();
                                            if (itemIdSelected == R.id.nav_group)
                                                selectDrawerItem(nvDrawer.getMenu().findItem(itemIdSelected));
                                        }
                                    })
                                    .setNegativeButton(R.string.refuse_group, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) { //Refuse.
                                            ParseRelation r = objects.get(0).getRelation("members");
                                            r.remove(ParseUser.getCurrentUser());
                                            objects.get(0).saveInBackground();
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        }
                    } else {
                        Toast.makeText(Main2Activity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    //Interface OnHomeFragmentInteractionListener's method. Called when I tap the checkbox for the service.
    @Override
    public void onBluetoothButtonClick(boolean enable) {

        if (enable) {
            startService(new Intent(Main2Activity.this, FindBluetoothService.class));
        } else {
            stopService(new Intent(Main2Activity.this, FindBluetoothService.class));
        }
    }

    //Interface OnGroupFragmentInteractionListener's method. Calles when I create a group.
    @Override
    public void onCreateGroupButtonClick() {
        MyGroupFragment fragment = MyGroupFragment.newInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
        // Start the location service.
        startService(new Intent(Main2Activity.this, FindMyPosition.class));
        pref.edit().putBoolean(BOOL_GROUP_KEY, true).apply();
    }

    //Interface OnMyGroupFragmentListener's method. Called when I exit from a group.
    @Override
    public void onExitGroupButtonClick() {
        GroupFragment fragment = GroupFragment.newInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
        // Stop the location service.
        stopService(new Intent(Main2Activity.this, FindMyPosition.class));
        pref.edit().putBoolean(BOOL_GROUP_KEY, false).apply();
    }

    @Override
    public void onBackPressed() {
        // Used to go to the Home screen and not to the DispatchActivity.
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}
