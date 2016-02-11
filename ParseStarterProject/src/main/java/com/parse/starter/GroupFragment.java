package com.parse.starter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class GroupFragment extends Fragment implements View.OnClickListener {

    private List<ParseUser> members = new ArrayList<ParseUser>();

    EditText nameText;
    EditText memberText;
    TextView memberAddedText;
    Button createButton;
    Button addButton;

    private OnGroupFragmentInteractionListener myListener;

    public static GroupFragment newInstance() {

        Bundle args = new Bundle();

        GroupFragment fragment = new GroupFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public GroupFragment() {
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
        View view = inflater.inflate(R.layout.fragment_group, container, false);

        nameText = (EditText) view.findViewById(R.id.groupName_et);
        memberText = (EditText) view.findViewById(R.id.memberName_et);
        memberAddedText = (TextView) view.findViewById(R.id.member_tv);
        createButton = (Button) view.findViewById(R.id.createGroup_bt);
        createButton.setOnClickListener(this);
        addButton = (Button) view.findViewById(R.id.add_bt);
        addButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        myListener = null;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_bt:
                String memberName = memberText.getText().toString();
                searchFriend(memberName);
                break;
            case R.id.createGroup_bt:
                String groupName = nameText.getText().toString();
                createGroup(groupName, members);
                myListener = (OnGroupFragmentInteractionListener)getActivity();
                if(myListener != null) {
                    myListener.onCreateGroupButtonClick();
                }
                break;
            default:
                throw new RuntimeException("Unknow button ID");
        }
    }

    // Network calls
    private void searchFriend(String username) {
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
                        // Checked if the user already belong to a group.
                        //TODO: Utente già in un gruppo.
                        /*if(user.getBoolean(UserKey.GROUP_KEY)) {
                            Toast.makeText(getActivity(), "L'utente appartiene già ad un gruppo!", Toast.LENGTH_SHORT).show();
                            return;
                        } */
                        members.add(user);
                        String text = memberAddedText.getText().toString();
                        memberAddedText.setText(text+usernames[i]+"\n");
                        memberText.setText("");
                        i++;
                    }
                }
                else if(users.size() == 0) {
                    Toast.makeText(getActivity(), "Utente non trovato", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void createGroup(String groupName, List<ParseUser> members) {
        if(members.size() == 0) {
            Toast.makeText(getActivity(), "Aggiungi almeno un utente!", Toast.LENGTH_SHORT).show();
            return;
        }

        if(groupName == null || groupName.trim().equals("")) {
            Toast.makeText(getActivity(), "Scegli un nome per il gruppo!", Toast.LENGTH_SHORT).show();
            return;
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
    }

    private boolean checkMemberInGroup(ParseUser u) {
        return false;
    }

    public interface OnGroupFragmentInteractionListener {
        public void onCreateGroupButtonClick();
    }
}
