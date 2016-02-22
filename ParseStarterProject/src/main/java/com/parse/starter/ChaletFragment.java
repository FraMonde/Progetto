package com.parse.starter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ChaletFragment extends Fragment {

    // The fragment's ListView/GridView.
    private AbsListView listView;
    private ChaletAdapter chaletAdapter;

    private List<ParseObject> chaletList = new ArrayList<ParseObject>();
    private Timer timer;


    public static ChaletFragment newInstance() {

        Bundle args = new Bundle();

        ChaletFragment fragment = new ChaletFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ChaletFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chalet, container, false);

        // Set the adapter
        listView = (AbsListView) view.findViewById(android.R.id.list);
        chaletAdapter = new ChaletAdapter(chaletList, getContext());
        ((AdapterView<ListAdapter>) listView).setAdapter(chaletAdapter);

        // Network call is called every 20 seconds.
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                getChalet();

            }
        };
        timer = new Timer();
        timer.schedule(timerTask, 0, 20000);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        timer = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = listView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    // Network call
    private void getChalet() {

        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Chalet");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, com.parse.ParseException e) {

                if (e == null) {
                    chaletList = objects;
                    chaletAdapter.refreshEvents(chaletList);
                } else {
                    Toast.makeText(getActivity(), e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
