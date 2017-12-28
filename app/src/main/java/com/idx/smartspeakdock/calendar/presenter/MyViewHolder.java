package com.idx.smartspeakdock.calendar.presenter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.idx.smartspeakdock.R;

/**
 * Created by geno on 28/12/17.
 */

public class MyViewHolder extends RecyclerView.ViewHolder {
    public TextView delete;
    public TextView eventTextView;
    public TextView timeTextView;
    public LinearLayout linearLayout;

    public MyViewHolder(View itemView) {
        super(itemView);
        eventTextView = (TextView) itemView.findViewById(R.id.thing);
        timeTextView = (TextView) itemView.findViewById(R.id.time);
        delete = (TextView) itemView.findViewById(R.id.item_delete);
        linearLayout = (LinearLayout) itemView.findViewById(R.id.linearlayout);
    }
}
