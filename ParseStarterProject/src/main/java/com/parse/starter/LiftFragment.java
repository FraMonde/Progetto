package com.parse.starter;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LiftFragment extends Fragment implements AbsListView.OnItemClickListener {

    // The fragment's ListView/GridView.
    private AbsListView listView;
    private LiftAdapter liftAdapter;
    private List<ParseObject> liftList = new ArrayList<ParseObject>();
    private Timer timer;
    private ProgressDialog pdia;

    public static LiftFragment newInstance() {
        LiftFragment fragment = new LiftFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public LiftFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lift, container, false);

        // Set the adapter
        listView = (AbsListView) view.findViewById(android.R.id.list);
        liftAdapter = new LiftAdapter(liftList, getContext());
        listView.setAdapter(liftAdapter);

        // Network call is called every 20 seconds.
       TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

                GetLift lift = new GetLift();
                lift.execute();
            }
       };
        timer = new Timer();
        timer.schedule(timerTask, 0, 20000);

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        //timer.cancel();
        //timer = null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
    class GetLift extends AsyncTask<Object, Void, List<ParseObject>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pdia = new ProgressDialog(getContext());
                    pdia.setMessage("Loading...");
                    pdia.show();
                }
            });
        }
        //TODO: first time e prova a ruotare.

        @Override
        protected List<ParseObject> doInBackground(Object... objects) {
            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Lift");
            try {
                liftList = query.find();
                return liftList;
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(List<ParseObject> result) {
            super.onPostExecute(result);

            if(result != null)
                liftAdapter.refreshEvents(liftList);

            pdia.dismiss();
        }
    }

}
