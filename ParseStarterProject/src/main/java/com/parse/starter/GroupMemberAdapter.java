package com.parse.starter;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
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
 * Created by francy on 12/02/16.
 */
public class GroupMemberAdapter extends BaseAdapter implements View.OnClickListener {

    private List<ParseUser> membersList;
    private Context context;
    private OnGroupAdapterListener myListener;

    public GroupMemberAdapter(List<ParseUser> members, Context context, OnGroupAdapterListener listener) {
        this.membersList = members;
        this.context = context;
        this.myListener = listener;
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
            view = li.inflate(R.layout.groupmember_adapter, viewGroup, false);
        }
        Typeface face1 = Typeface.createFromAsset(context.getAssets(), "fonts/GOTHAM-MEDIUM.TTF");
        TextView name_tv = (TextView) view.findViewById(R.id.memberName_tv);
        String userName = membersList.get(i).getUsername();
        name_tv.setText(userName);
        name_tv.setTypeface(face1);

        Button delete_bt = (Button) view.findViewById(R.id.delete_bt);
        delete_bt.setTag(i);
        delete_bt.setOnClickListener(this);

        return view;
    }

    public void refreshEvents(List<ParseUser> events) {
        this.membersList = new ArrayList<ParseUser>();
        this.membersList.addAll(events);
        notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        ParseUser u = this.membersList.get((Integer) view.getTag());
        this.membersList.remove(u);
        notifyDataSetChanged();
        myListener.memberDeleted(u);
    }

    public interface OnGroupAdapterListener {
        public void memberDeleted(ParseUser user);
    }
}
