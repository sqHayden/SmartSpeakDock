package com.idx.smartspeakdock.map.adapter;

/**
 * Created by hayden on 18-1-17.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.amap.api.services.core.PoiItem;
import com.idx.smartspeakdock.R;

import java.util.List;

/**
 * 输入提示adapter，展示item名称和地址
 * Created by ligen on 16/11/25.
 */
public class InputItemsAdapter extends BaseAdapter {
    private Context mContext;
    private List<PoiItem> mListItems;

    public InputItemsAdapter(Context context, List<PoiItem> itemList) {
        mContext = context;
        mListItems = itemList;
    }

    @Override
    public int getCount() {
        if (mListItems != null) {
            return mListItems.size();
        }
        return 0;
    }


    @Override
    public Object getItem(int i) {
        if(mListItems != null){
           return mListItems.get(i);
        }
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Holder holder;
        if (view == null) {
            holder = new Holder();
            view = LayoutInflater.from(mContext).inflate(R.layout.map_adapter_inputtips, null);
            holder.mName = (TextView) view.findViewById(R.id.name);
            holder.mAddress = (TextView) view.findViewById(R.id.adress);
            view.setTag(holder);
        } else{
            holder = (Holder)view.getTag();
        }
        if(mListItems == null){
            return view;
        }
        holder.mName.setText(mListItems.get(i).getTitle());
        String address = mListItems.get(i).getSnippet();
        if(address == null || address.equals("")){
            holder.mAddress.setVisibility(View.GONE);
        }else{
            holder.mAddress.setVisibility(View.VISIBLE);
            holder.mAddress.setText(address);
        }
        return view;
    }

    class Holder {
        TextView mName;
        TextView mAddress;
    }
}