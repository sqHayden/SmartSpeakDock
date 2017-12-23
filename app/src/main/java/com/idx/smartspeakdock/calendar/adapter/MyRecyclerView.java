package com.idx.smartspeakdock.calendar.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.calendar.bean.Schedule;

import java.util.List;

/**
 * Created by geno on 22/12/17.
 */

public class MyRecyclerView extends RecyclerView.Adapter<MyRecyclerView.MyViewHolder>{
    List<Schedule> list;
    public MyRecyclerView(List<Schedule> list) {
        super();
        this.list = list;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycleritem,parent,false);
        MyViewHolder vh = new MyViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.eventTextView.setText(list.get(position).getEvent());
        holder.timeTextView.setText(list.get(position).getTime());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
    public class MyViewHolder extends RecyclerView.ViewHolder{
        // @BindView(R.id.thing)
        TextView eventTextView;
        //@BindView(R.id.time)
        TextView timeTextView;
        public MyViewHolder(View itemView) {
            super(itemView);
            // ButterKnife.bind(itemView);
            eventTextView = (TextView) itemView.findViewById(R.id.thing);
            timeTextView = (TextView) itemView.findViewById(R.id.time);
        }
    }
}

