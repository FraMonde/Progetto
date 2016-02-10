package com.parse.starter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class MyGroupFragment extends Fragment {

    public static MyGroupFragment newInstance() {
        MyGroupFragment fragment = new MyGroupFragment();
        return fragment;
    }

    public MyGroupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getGroupMember();
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_group, container, false);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void getGroupMember() {
        ParseUser currentUser = ParseUser.getCurrentUser();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Group");
        query.whereEqualTo("members", currentUser);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null) {
                    Toast.makeText(getActivity(), "oooooook",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}
