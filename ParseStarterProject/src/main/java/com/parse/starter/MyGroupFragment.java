package com.parse.starter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.TabHost.TabSpec;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class MyGroupFragment extends Fragment {

    private String[] data;
    private ArrayAdapter<String> arrayAdapter;
    private ListView lw;

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
        View rootView = inflater.inflate(R.layout.fragment_my_group, container, false);
        lw = (ListView) rootView.findViewById(R.id.memberList);
        //arrayAdapter = new ArrayAdapter<String>()

        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void getGroupMember() {
        final ParseUser currentUser = ParseUser.getCurrentUser();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Group");
        query.whereEqualTo("members", currentUser);

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null) {
                   for(ParseObject group:objects) {
                        //Find the member of the group.
                       ParseRelation r = group.getRelation("members");
                       ParseQuery query = r.getQuery();
                       try {
                           List<ParseUser> members = query.find();
                       } catch (ParseException e1) {
                           e1.printStackTrace();
                       }
                   }
                } else {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

}
