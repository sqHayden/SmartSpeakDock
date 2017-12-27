package com.idx.smartspeakdock.calendar.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.idx.calendarview.MessageEvent;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.calendar.bean.Schedule;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by geno on 22/12/17.
 */

public class MyRecyclerView extends RecyclerView.Adapter<MyRecyclerView.MyViewHolder>{
    List<Schedule> list;
    Context context;
    String date;
    Integer day;
    List<Schedule> mlist = new ArrayList<>();
    public MyRecyclerView(String date, Integer day, Context context , List<Schedule> list) {
        super();
        Log.v("1218","myrecyclerview" + list.size());
        EventBus.getDefault().register(this);
        this.context = context;
        this.list = list;
        this.date = date;
        this.day = day;

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerviewitem,parent,false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Log.v("1218","" + mlist.size() + "positon:" + position);
        if (mlist.get(position).getDate().equals(date)&&mlist.get(position).getDay().equals(day)){
            Log.v("1218","aa" + position);
            Log.v("1218","bingview" +mlist.get(position).getEvent());
            holder.eventTextView.setText(mlist.get(position).getEvent());
            holder.timeTextView.setText(mlist.get(position).getTime());
        }else{

        }
    }

    @Override
    public int getItemCount() {
        mlist.clear();
        Log.v("1218","getitemaaa" + list.size());
        int count = list.size();
        for(int i = 0;i<list.size();i++){
            if (list.get(i).getDate().equals(date)&&list.get(i).getDay().equals(day)) {
                mlist.add(list.get(i));
            }else {
                count = count - 1;
            }
        }
          Log.v("1218","getitemcount" + count);
        return count;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView eventTextView;
        TextView timeTextView;
        public MyViewHolder(View itemView) {
            super(itemView);
            Log.v("1218","myholder");
            eventTextView = (TextView) itemView.findViewById(R.id.thing);
            timeTextView = (TextView) itemView.findViewById(R.id.time);
        }
    }
    @Subscribe
    public void onEvent(MessageEvent messageEvent){
        Log.v("1218","adapteronevent");
        date = messageEvent.getMessage();
        day = messageEvent.getday();
        notifyDataSetChanged();
    }
}
