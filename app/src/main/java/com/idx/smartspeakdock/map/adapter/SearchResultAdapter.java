package com.idx.smartspeakdock.map.adapter;

/**
 * Created by hayden on 18-1-17.
 */

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.google.gson.Gson;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.SpeakerApplication;
import com.idx.smartspeakdock.map.CalculateRouteActivity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 *
 */
public class SearchResultAdapter extends BaseAdapter {

    private List<PoiItem> data;
    private Context context;
    private AMapLocation aMapLocation;
    private LatLng start_location;
    private LatLonPoint end_location;

    private int selectedPosition = 0;

    public SearchResultAdapter(Context context, AMapLocation aMapLocation) {
        this.context = context;
        this.aMapLocation = aMapLocation;
        start_location = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
        data = new ArrayList<>();
    }

    public void setData(List<PoiItem> data) {
        this.data = data;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.map_view_holder_result, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.bindView(position);
        return convertView;
    }

    class ViewHolder {
        TextView textTitle;
        TextView textSubTitle;
        LinearLayout linearLayout;

        public ViewHolder(View view) {
            textTitle = (TextView) view.findViewById(R.id.text_title);
            textSubTitle = (TextView) view.findViewById(R.id.text_title_sub);
            linearLayout = (LinearLayout) view.findViewById(R.id.route_make);
        }

        public void bindView(int position) {
            if (position >= data.size())
                return;
            final PoiItem poiItem = data.get(position);
            textTitle.setText(poiItem.getTitle());
            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //启动导航activity
                    Intent intent = new Intent(SpeakerApplication.getContext(),CalculateRouteActivity.class);
                    intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    //添加起点及终点信息
                    Gson gson = new Gson();
                    intent.putExtra("start_location",gson.toJson(start_location));
                    intent.putExtra("end_location",gson.toJson(new LatLng(end_location.getLatitude(),end_location.getLongitude())));
                    intent.putExtra("end_name",poiItem.getTitle());
                    intent.putExtra("pathWay","驾车");
                    SpeakerApplication.getContext().startActivity(intent);
                }
            });
            //获取距离
            end_location = poiItem.getLatLonPoint();
            LatLng end = new LatLng(end_location.getLatitude(),end_location.getLongitude());
            float distance = AMapUtils.calculateLineDistance(start_location, end);
            if (distance > 1000) {
                double dis = distance / 1000;
                BigDecimal b = new BigDecimal(dis);
                double f1 = b.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                textSubTitle.setText("路线总长度：" + f1 + "公里" + "    " + poiItem.getSnippet());
            } else {
                BigDecimal b = new BigDecimal(distance);
                double f1 = b.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                textSubTitle.setText("路线总长度：" + f1 + "米" + "    " + poiItem.getSnippet());
            }
        }
    }
}