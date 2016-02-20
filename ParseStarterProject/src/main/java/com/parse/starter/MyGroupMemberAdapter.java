package com.parse.starter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by francy on 18/02/16.
 */
public class MyGroupMemberAdapter extends BaseAdapter {

    private List<ParseUser> membersList;
    private Context context;

    public MyGroupMemberAdapter(List<ParseUser> members, Context context) {
        this.membersList = members;
        this.context = context;
    }

    @Override
    public int getCount() {
        return membersList.size();
    }

    @Override
    public Object getItem(int i) {
        return membersList.get(i);
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
            view = li.inflate(R.layout.my_group_member_adapter, viewGroup, false);
        }
        Typeface face1 = Typeface.createFromAsset(context.getAssets(), "fonts/GOTHAM-MEDIUM.TTF");
        TextView name_tv = (TextView) view.findViewById(R.id.myGroupMemberName_tv);
        String userName = membersList.get(i).getUsername();
        name_tv.setText(userName);
        name_tv.setTypeface(face1);

        Typeface face2 = Typeface.createFromAsset(context.getAssets(), "fonts/GOTHAM-LIGHT.TTF");
        TextView wait_tv = (TextView) view.findViewById(R.id.wait_tv);
        wait_tv.setTypeface(face2);
        if (membersList.get(i).getBoolean(UserKey.GROUP_KEY))
            wait_tv.setVisibility(View.GONE);
        else
            wait_tv.setVisibility(View.VISIBLE);

        return view;
    }

    public void refreshEvents(List<ParseUser> events) {
        this.membersList = new ArrayList<ParseUser>();
        this.membersList.addAll(events);
        notifyDataSetChanged();
    }

}
