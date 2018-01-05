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
import com.idx.smartspeakdock.calendar.presenter.MyViewHolder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by geno on 22/12/17.
 */

public class MyRecyclerView extends RecyclerView.Adapter{
    List<Schedule> list;
    Context context;
    String date;
    Integer day;
    List<Schedule> mlist = new ArrayList<>();
    public MyRecyclerView(String date, Integer day, Context context , List<Schedule> list) {
        super();
        EventBus.getDefault().register(this);
        this.context = context;
        this.list = list;
        this.date = date;
        this.day = day;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerviewitem, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final MyViewHolder viewHolder = (MyViewHolder) holder;
        if (mlist.get(position).getDate().equals(date)&&mlist.get(position).getDay().equals(day)){
            viewHolder.eventTextView.setText(mlist.get(position).getEvent());
            viewHolder.timeTextView.setText(mlist.get(position).getTime());
        }else{

        }

    }

    @Override
    public int getItemCount() {
        mlist.clear();
        int count = list.size();
        Log.v("1218","listsize" + list.size());
        if (list.size() != 0){
            for(int i = 0;i<list.size();i++){
                if (list.get(i).getDate().equals(date)&&list.get(i).getDay().equals(day)) {
                    mlist.add(list.get(i));
                }else {
                    count = count - 1;
                }
            }
            return count;
        }else {
            return 0;
        }

    }
    public void removeItem(String date,Integer day,String event,String time) {
        for (int i = 0;i<list.size();i++){
            if (list.get(i).getDate().equals(date)&&list.get(i).getDay().equals(day)&&list.get(i).getEvent().equals(event)&&list.get(i).getTime().equals(time)){
                list.remove(i);
            }
        }
        notifyDataSetChanged();
    }

    @Subscribe
    public void onEvent(MessageEvent messageEvent){
        date = messageEvent.getMessage();
        day = messageEvent.getday();
        notifyDataSetChanged();
    }
}
