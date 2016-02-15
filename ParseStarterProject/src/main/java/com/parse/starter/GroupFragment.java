package com.parse.starter;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
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
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;

public class GroupFragment extends Fragment implements View.OnClickListener, GroupMemberAdapter.OnGroupAdapterListener {

    private static final String GROUP_NAME_KEY = "GROUP_NAME";
    private static final String MEMBER_NAME_KEY = "MEMBER_NAME";
    private static final String LIST_MEMBER_KEY = "LIST_MEMBER";

    private List<ParseUser> members;
    private OnGroupFragmentInteractionListener myListener;
    private SharedPreferences pref;
    private Handler handler;
    private ProgressDialog progressDialog;

    private EditText nameText;
    private EditText memberText;
    private Button addButton;
    private GroupMemberAdapter groupMemberAdapter;
    private ListView lw;

    public static GroupFragment newInstance() {

        Bundle args = new Bundle();

        GroupFragment fragment = new GroupFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public GroupFragment() {
        // Required empty public constructor
    }

    // Fragments life cycle methods.

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        members = new ArrayList<ParseUser>();
        getActivity().setTitle("Nuovo gruppo");
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

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_group, container, false);
        setHasOptionsMenu(true);

        nameText = (EditText) view.findViewById(R.id.groupName_et);
        memberText = (EditText) view.findViewById(R.id.memberName_et);
        addButton = (Button) view.findViewById(R.id.add_bt);
        addButton.setOnClickListener(this);
        lw = (ListView) view.findViewById(R.id.member_lv);
        groupMemberAdapter = new GroupMemberAdapter(members, getContext(), this);
        ((AdapterView<ListAdapter>) lw).setAdapter(groupMemberAdapter);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String groupName = pref.getString(GROUP_NAME_KEY, "");
        nameText.setText(groupName);
        String memberName = pref.getString(MEMBER_NAME_KEY, "");
        memberText.setText(memberName);

        SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getApplicationContext());
        Gson gson = new GsonBuilder().setExclusionStrategies(new ParseExclusion()).create();
        String json = appSharedPrefs.getString(LIST_MEMBER_KEY, "");
        Type type = new TypeToken<List<ParseUser>>() {}.getType();
        /*if (!json.equals(null) && !json.equals("")) {
            members = gson.fromJson(json, type);
            if (members != null)
                groupMemberAdapter.refreshEvents(members);
        }*/
    }

    @Override
    public void onPause() {
        super.onPause();

        pref.edit().putString(GROUP_NAME_KEY, nameText.getText().toString()).apply();
        pref.edit().putString(MEMBER_NAME_KEY, memberText.getText().toString()).apply();

        SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getApplicationContext());
        SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
        Gson gson = new GsonBuilder().setExclusionStrategies(new ParseExclusion()).create();
        String json = gson.toJson(members);
        prefsEditor.putString(LIST_MEMBER_KEY, json);
        prefsEditor.commit();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        myListener = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.create_group_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String groupName = nameText.getText().toString();
        if (createGroup(groupName, members)) {
            myListener = (OnGroupFragmentInteractionListener) getActivity();
            if (myListener != null) {
                myListener.onCreateGroupButtonClick();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        String memberName = memberText.getText().toString();
        searchFriend(memberName);
    }

    // Network calls

    // Search the added friend to obtain the ParseUser object.
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
                        if (checkAddedUser(user) || user.equals(ParseUser.getCurrentUser())) {

                            Toast.makeText(getActivity(), "L'utente è già stato aggiunto!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // Check if the user's in another group.
                        ParseQuery<ParseObject> query = ParseQuery.getQuery("Group");
                        query.whereEqualTo("members", user);
                        final Message message = handler.obtainMessage();
                        message.sendToTarget();
                        query.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> objects, ParseException e) {
                                progressDialog.dismiss();
                                if (e == null) {
                                    if (objects.size() == 0) {
                                        members.add(user);
                                        memberText.setText("");
                                        groupMemberAdapter.refreshEvents(members);
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

    private boolean createGroup(String groupName, List<ParseUser> members) {
        if (members.size() == 0) {
            Toast.makeText(getActivity(), "Aggiungi almeno un utente!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (groupName == null || groupName.trim().equals("")) {
            Toast.makeText(getActivity(), "Scegli un nome per il gruppo!", Toast.LENGTH_SHORT).show();
            return false;
        }


        ParseObject group = new ParseObject("Group");
        group.put("Name", groupName);
        ParseUser user = ParseUser.getCurrentUser();
        members.add(user);
        // Update the user's variable for group.
        user.put(UserKey.GROUP_KEY, true);
        user.saveInBackground();

        ParseRelation<ParseObject> relation = group.getRelation("members");

        for (ParseUser u : members) {
            relation.add(u);
        }

        group.saveInBackground();
        return true;
    }

    private boolean checkAddedUser(ParseUser user) {
        for(ParseUser u:members) {
            if(u.getUsername().equals(user.getUsername()))
                return true;
        }
        return false;
    }

    @Override
    public void memberDeleted(ParseUser user) {
        members.remove(user);
    }

    public interface OnGroupFragmentInteractionListener {
        public void onCreateGroupButtonClick();
    }

    private class ParseExclusion implements ExclusionStrategy {

        public boolean shouldSkipClass(Class<?> arg0) {
            return false;
        }

        public boolean shouldSkipField(FieldAttributes f) {
            return (f.getDeclaredClass() == Lock.class);
        }

    }
}


