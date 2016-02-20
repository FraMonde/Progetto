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
 * Created by francy on 08/02/16.
 */
public class ChaletAdapter extends BaseAdapter {

    private List<ParseObject> chalet;
    private Context context;

    public ChaletAdapter(List<ParseObject> chalet, Context context) {
        this.chalet = chalet;
        this.context = context;
    }


    @Override
    public int getCount() {
        return chalet.size();
    }

    @Override
    public Object getItem(int i) {
        return chalet.get(i);
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
            view = li.inflate(R.layout.chalet_adapter, viewGroup, false);
        }

        Typeface face = Typeface.createFromAsset(context.getAssets(), "fonts/GOTHAM-BOLD.TTF");

        TextView name_tv = (TextView) view.findViewById(R.id.chalet_tv);
        String liftName = chalet.get(i).getString("Name");
        name_tv.setText(liftName);
        name_tv.setTypeface(face);

        TextView time_tv = (TextView) view.findViewById(R.id.person_tv);
        Number totalSeat = chalet.get(i).getNumber("TotalSeat");
        Number busySeat = chalet.get(i).getNumber("BusySeat");
        Number freeSeat = (int)totalSeat - (int)busySeat;
        if((int)freeSeat<0)
            freeSeat = 0;
        time_tv.setText(freeSeat.toString() + " posti liberi");
        time_tv.setTypeface(face);

        return view;
    }

    public void refreshEvents(List<ParseObject> events) {
        this.chalet.clear();
        this.chalet.addAll(events);
        notifyDataSetChanged();
    }
}
