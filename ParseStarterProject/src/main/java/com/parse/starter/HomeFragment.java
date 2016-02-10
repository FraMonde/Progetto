package com.parse.starter;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;


public class HomeFragment extends Fragment implements View.OnClickListener {

    CheckBox bluetoothButton;
    private OnHomeFragmentInteractionListener myListener;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        bluetoothButton = (CheckBox) view.findViewById(R.id.bluetotth_cb);
        bluetoothButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        myListener = null;
    }

    @Override
    public void onClick(View view) {
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

    public interface OnHomeFragmentInteractionListener {
        public void onBluetoothButtonClick(boolean enable);
    }

}
