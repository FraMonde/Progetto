package com.parse.starter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.parse.ParseObject;

import java.util.List;

/**
 * Created by francy on 10/01/16.
 */
public class LiftAdapter extends BaseAdapter {

    private List<ParseObject> lift;
    private Context context;

    public LiftAdapter(List<ParseObject> lift, Context context) {
        this.lift = lift;
        this.context = context;
    }

    @Override
    public int getCount() {
        return lift.size();
    }

    @Override
    public Object getItem(int i) {
        return lift.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        // Se è null non ho nulla da reciclare, la faccio nuova.
        if (view == null) {
            // false: prendi le misure da parent ma non attaccarlo ancora.
            LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = li.inflate(R.layout.lift_adapter, viewGroup, false);
        }

        Typeface face = Typeface.createFromAsset(context.getAssets(), "fonts/GOTHAM-BOLD.TTF");

        TextView name_tv = (TextView) view.findViewById(R.id.lift_tv);
        String liftName = lift.get(i).getString("Name");
        name_tv.setText(liftName);
        name_tv.setTypeface(face);

        TextView time_tv = (TextView) view.findViewById(R.id.time_tv);
        Number person = lift.get(i).getNumber("Person");
        Number personMin = lift.get(i).getNumber("PerMin");
        Number liftTime = (int)person/(int)personMin;
        time_tv.setText(liftTime.toString()+"min");
        time_tv.setTypeface(face);

        return view;
    }

    public void refreshEvents(List<ParseObject> events) {
        this.lift.clear();
        this.lift.addAll(events);
        notifyDataSetChanged();
    }
}
