/*
 * Copyright (C) 2016 huanghaibin_dev <huanghaibin_dev@163.com>
 * WebSite https://github.com/MiracleTimes-Dev
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.idx.calendarview;

import android.content.Context;
import android.content.res.Configuration;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


import java.util.List;

class MonthAdapter extends BaseRecyclerAdapter<Month> {
    private List<Calendar> mSchemes;
    private int mSchemeColor;
    Context context;
    MonthAdapter(Context context) {
        super(context);
        this.context = context;
    }

    void setSchemes(List<Calendar> mSchemes) {
        this.mSchemes = mSchemes;
    }

    void setSchemeColor(int mSchemeColor) {
        this.mSchemeColor = mSchemeColor;
    }

    @Override
    RecyclerView.ViewHolder onCreateDefaultViewHolder(ViewGroup parent, int type) {
        return new MonthViewHolder(mInflater.inflate(R.layout.cv_item_list_month, parent, false),context);
    }

    @Override
    void onBindViewHolder(RecyclerView.ViewHolder holder, Month item, int position) {
        MonthViewHolder h = (MonthViewHolder) holder;
        MonthView view = h.mMonthView;
        view.setSchemes(mSchemes);
        view.setSchemeColor(mSchemeColor);
        view.init(item.getDiff(), item.getCount(), item.getYear(), item.getMonth());
        h.mTextMonth.setText(String.format("%s月", item.getMonth()));
    }

    private static class MonthViewHolder extends RecyclerView.ViewHolder {
        MonthView mMonthView;
        TextView mTextMonth;
        LinearLayout linearLayout;
        DisplayMetrics dm;
        int mwith,mheight;
        MonthViewHolder(View itemView,Context context) {
            super(itemView);
            mMonthView = (MonthView) itemView.findViewById(R.id.selectView);
            mTextMonth = (TextView) itemView.findViewById(R.id.tv_month);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.linear);
            dm = context.getResources().getDisplayMetrics();
            mheight = dm.heightPixels;
            mwith = dm.widthPixels;
            ViewGroup.LayoutParams lp= linearLayout.getLayoutParams();
            // 判断Android当前的屏幕是横屏还是竖屏。横竖屏判断
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                //竖屏

                lp.height = (mheight-context.getResources().getDimensionPixelSize(R.dimen.month_textsize))/4;
            } else {
                //横屏
                lp.height = (mwith-context.getResources().getDimensionPixelSize(R.dimen.month2_textsize))/3;
            }
            linearLayout.setLayoutParams(lp);
        }
    }
}
