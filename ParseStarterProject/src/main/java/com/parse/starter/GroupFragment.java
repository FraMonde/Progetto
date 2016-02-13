package com.parse.starter;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GroupFragment extends Fragment implements View.OnClickListener, GroupMemberAdapter.OnGroupAdapterListener {

    private List<ParseUser> members;
    private OnGroupFragmentInteractionListener myListener;
    private SharedPreferences pref;

    EditText nameText;
    EditText memberText;
    Button addButton;
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
        pref = PreferenceManager.getDefaultSharedPreferences(getContext());
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

        String groupName = pref.getString("NAME", "");
        nameText.setText(groupName);
    }

    @Override
    public void onPause() {
        super.onPause();

        //TODO: salvare la lista
        pref.edit().putString("NAME", nameText.getText().toString()).apply();
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
    private void searchFriend(final String username) {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("username", username);
        //query.orderByAscending(ParseConstants.KEY_USERNAME);
        query.setLimit(200);
        query.findInBackground(new FindCallback<ParseUser>() {

            @Override
            public void done(List<ParseUser> users, ParseException e) {
                if (e == null && users.size() > 0) {
                    //Success we have Users to display
                    //store users in array
                    String[] usernames = new String[users.size()];
                    //Loop Users
                    int i = 0;
                    for (ParseUser user : users) {
                        usernames[i] = user.getUsername();
                        // Check if I've already added the user.
                        if (members.contains(user) || user.equals(ParseUser.getCurrentUser())) {
                            Toast.makeText(getActivity(), "L'utente è già stato aggiunto!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        checkMemberInGroup(user);
                        // Checked if the user already belong to a group.
                        //TODO: Utente già in un gruppo.
                        /*if(user.getBoolean(UserKey.GROUP_KEY)) {
                            Toast.makeText(getActivity(), "L'utente appartiene già ad un gruppo!", Toast.LENGTH_SHORT).show();
                            return;
                        } */
                        members.add(user);
                        memberText.setText("");
                        groupMemberAdapter.refreshEvents(members);
                        i++;
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
            //TODO: non funziona
            u.put(UserKey.GROUP_KEY, true);
            u.saveInBackground();
        }

        group.saveInBackground();
        return true;
    }

    private boolean checkMemberInGroup(ParseUser u) {

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Group");
        query.whereEqualTo("members", u);

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if(objects.size() == 0)
                        Toast.makeText(getActivity(), "Non in un gruppo", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), "In un gruppo", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        return false;
    }

    @Override
    public void memberDeleted(ParseUser user) {
        members.remove(user);
    }

    public interface OnGroupFragmentInteractionListener {
        public void onCreateGroupButtonClick();
    }
}
