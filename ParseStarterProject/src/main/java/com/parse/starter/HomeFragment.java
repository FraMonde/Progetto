package com.parse.starter;

import android.bluetooth.BluetoothAdapter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;


public class HomeFragment extends Fragment implements View.OnClickListener {

    CheckBox bluetoothButton;
    TextView home_TextView;

    private OnHomeFragmentInteractionListener myListener;
    private SharedPreferences pref;

    public static HomeFragment newInstance() {

        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        bluetoothButton = (CheckBox) view.findViewById(R.id.bluetotth_cb);
        bluetoothButton.setOnClickListener(this);
        
        home_TextView = (TextView) view.findViewById(R.id.home_advice);
        Typeface face = Typeface.createFromAsset(getActivity().getAssets(), "fonts/GOTHAM-BOLD.TTF");
        home_TextView.setTypeface(face);

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        myListener = null;
    }

    @Override
    public void onPause() {
        super.onPause();

        pref.edit().putBoolean("CHECKED", bluetoothButton.isChecked()).apply();
    }

    @Override
    public void onClick(View view) {

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            bluetoothButton.setChecked(false);
            Toast.makeText(getActivity(), "Bluetooth non supportato!", Toast.LENGTH_SHORT).show();
            return;
        }

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            bluetoothButton.setChecked(false);
            Toast.makeText(getActivity(), "Accendi il Bluetooth!", Toast.LENGTH_SHORT).show();
            return;
        }

        myListener = (OnHomeFragmentInteractionListener) getActivity();
        if (myListener != null) {
            myListener.onBluetoothButtonClick(bluetoothButton.isChecked());
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        boolean checked = pref.getBoolean("CHECKED", false);
        bluetoothButton.setChecked(checked);
        myListener = (OnHomeFragmentInteractionListener) getActivity();
        if((myListener != null) && checked) {
            myListener.onBluetoothButtonClick(checked);
        }
    }

    public interface OnHomeFragmentInteractionListener {
        public void onBluetoothButtonClick(boolean enable);
    }

}
