package com.parse.starter;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;


public class HomeFragment extends Fragment implements View.OnClickListener {

    ImageButton bluetoothButton;
    private OnHomeFragmentInteractionListener myListener;

    public static HomeFragment newInstance() {

        Bundle args = new Bundle();

        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
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
        bluetoothButton = (ImageButton) view.findViewById(R.id.bluetotth_cb);
        bluetoothButton.setOnClickListener(this);
        bluetoothButtonImage();

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        myListener = null;
    }

    @Override
    public void onClick(View view) {
        bluetoothButtonImage();
        myListener = (OnHomeFragmentInteractionListener) getActivity();
        if (myListener != null) {
            myListener.onBluetoothButtonClick();
        }
    }

    private void bluetoothButtonImage() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            bluetoothButton.setBackgroundResource(R.drawable.bl_notenabled);
        } else {
            bluetoothButton.setBackgroundResource(R.drawable.bl_enabled);
        }
    }

    public interface OnHomeFragmentInteractionListener {
        public void onBluetoothButtonClick();
    }

}
