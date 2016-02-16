package com.parse.starter;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MyGroupFragment extends Fragment implements View.OnClickListener {

    private static final String MEMBER_NAME_KEY = "MEMBER_NAME";
    private static final String MEMBER_KEY = "members";
    private static final String GROUP_KEY = "Group";

    private List<String> data = new ArrayList<String>();
    private ArrayAdapter<String> arrayAdapter;
    private OnMyGroupFragmentListener myListener;
    private Handler handler;
    private ProgressDialog progressDialog;
    private SharedPreferences pref;
    private ParseObject myGroup;

    private ListView lw;
    private EditText memberText;
    private Button addButton;

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
        myListener = (OnMyGroupFragmentListener) getActivity();
        pref = PreferenceManager.getDefaultSharedPreferences(getContext());
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
        View view = inflater.inflate(R.layout.fragment_my_group, container, false);
        setHasOptionsMenu(true);
        memberText = (EditText) view.findViewById(R.id.newMemberName_et);
        addButton = (Button) view.findViewById(R.id.addInMyGroup_bt);
        addButton.setOnClickListener(this);
        lw = (ListView) view.findViewById(R.id.memberList);
        arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_expandable_list_item_1, data);
        lw.setAdapter(arrayAdapter);

        return view;
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
        new AlertDialog.Builder(getContext())
                .setTitle("Attenzione")
                .setMessage("Sei sicuro di voler abbandonare il gruppo?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        exitGroup();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

        return super.onOptionsItemSelected(item);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String memberName = pref.getString(MEMBER_NAME_KEY, "");
        memberText.setText(memberName);
    }

    @Override
    public void onPause() {
        super.onPause();

        pref.edit().putString(MEMBER_NAME_KEY, memberText.getText().toString()).apply();
    }

    // Load all the members of the current user's group.
    private void getGroupMember() {

        final Message message = handler.obtainMessage();
        message.sendToTarget();
        final ParseUser currentUser = ParseUser.getCurrentUser();

        ParseQuery<ParseObject> query = ParseQuery.getQuery(GROUP_KEY);
        query.whereEqualTo(MEMBER_KEY, currentUser);

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                progressDialog.dismiss();
                if (e == null) {
                    for (ParseObject group : objects) {
                        myGroup = group;
                        getActivity().setTitle(group.getString("Name"));
                        //Find the member of the group.
                        ParseRelation r = group.getRelation(MEMBER_KEY);
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

    // Method to exit from the group.
    private void exitGroup() {

        ParseUser currentUser = ParseUser.getCurrentUser();
        //Find the member of the group.
        ParseRelation r = myGroup.getRelation(MEMBER_KEY);
        r.remove(currentUser);
        myGroup.saveInBackground();
        currentUser.put(UserKey.GROUP_KEY, false);
        currentUser.saveInBackground();
        myListener.onExitGroupButtonClick();

    }

    @Override
    public void onClick(View view) {
        searchFriend(memberText.getText().toString());
    }

    // Search the added friend to verify if It can be added to the group.
    private void searchFriend(final String username) {
        final Message message = handler.obtainMessage();
        message.sendToTarget();
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("username", username);
        //query.orderByAscending(ParseConstants.KEY_USERNAME);
        query.setLimit(200);
        query.findInBackground(new FindCallback<ParseUser>() {

            @Override
            public void done(List<ParseUser> users, ParseException e) {
                progressDialog.dismiss();
                if (e == null && users.size() > 0) {
                    //Success we have Users to display
                    //store users in array
                    String[] usernames = new String[users.size()];
                    //Loop Users
                    final int[] i = {0};
                    for (final ParseUser user : users) {
                        usernames[i[0]] = user.getUsername();
                        // Check if I've already added the user.
                        if (checkAddedUser(user.getUsername()) || user.equals(ParseUser.getCurrentUser())) {

                            Toast.makeText(getActivity(), "L'utente è già stato aggiunto!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // Check if the user's in another group.
                        ParseQuery<ParseObject> query = ParseQuery.getQuery(GROUP_KEY);
                        query.whereEqualTo(MEMBER_KEY, user);
                        final Message message = handler.obtainMessage();
                        message.sendToTarget();
                        query.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> objects, ParseException e) {
                                progressDialog.dismiss();
                                if (e == null) {
                                    if (objects.size() == 0) {
                                        ParseRelation r = myGroup.getRelation(MEMBER_KEY);
                                        r.add(user);
                                        myGroup.saveInBackground();
                                        data.add(user.getUsername());
                                        memberText.setText("");
                                        arrayAdapter.notifyDataSetChanged();
                                        i[0]++;
                                    } else {
                                        Toast.makeText(getActivity(), "L'utente è già in un gruppo", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                } else if (users.size() == 0) {
                    Toast.makeText(getActivity(), "Utente non trovato", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean checkAddedUser(String user) {
        for (String u : data) {
            if (u.equals(user))
                return true;
        }
        return false;
    }

    public interface OnMyGroupFragmentListener {
        public void onExitGroupButtonClick();
    }

}
