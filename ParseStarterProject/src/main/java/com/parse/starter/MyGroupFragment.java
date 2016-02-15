package com.parse.starter;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class MyGroupFragment extends Fragment {

    private List<String> data = new ArrayList<String>();
    private ArrayAdapter<String> arrayAdapter;
    private OnMyGroupFragmentListener myListener;
    private Handler handler;
    private ProgressDialog progressDialog;

    private ListView lw;
    private Button exitBt;

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
        myListener = (OnMyGroupFragmentListener)getActivity();
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                progressDialog = ProgressDialog.show(getActivity(), null, "Loading…");
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getGroupMember();
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_my_group, container, false);
        setHasOptionsMenu(true);
        lw = (ListView) rootView.findViewById(R.id.memberList);
        arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_expandable_list_item_1, data);
        lw.setAdapter(arrayAdapter);

        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        myListener = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.my_group_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        exitGroup();

        return super.onOptionsItemSelected(item);
    }

    // Load all the members of the current user's group.
    private void getGroupMember() {

        final Message message = handler.obtainMessage();
        message.sendToTarget();
        final ParseUser currentUser = ParseUser.getCurrentUser();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Group");
        query.whereEqualTo("members", currentUser);

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                progressDialog.dismiss();
                if (e == null) {
                    for (ParseObject group : objects) {
                        getActivity().setTitle(group.getString("Name"));
                        //Find the member of the group.
                        ParseRelation r = group.getRelation("members");
                        ParseQuery query = r.getQuery();
                        try {
                            List<ParseUser> members = query.find();
                            for (ParseUser m : members) {
                                data.add(m.getUsername());
                            }
                            arrayAdapter.notifyDataSetChanged();
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

    // Method to exit the group.
    private void exitGroup() {
        final ParseUser currentUser = ParseUser.getCurrentUser();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Group");
        query.whereEqualTo("members", currentUser);

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    for (ParseObject group : objects) {
                        //Find the member of the group.
                        ParseRelation r = group.getRelation("members");
                        r.remove(currentUser);
                        group.saveInBackground();
                        currentUser.put(UserKey.GROUP_KEY, false);
                        currentUser.saveInBackground();
                        myListener.onExitGroupButtonClick();
                    }
                } else {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public interface OnMyGroupFragmentListener {
        public void onExitGroupButtonClick();
    }

}
