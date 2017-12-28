package com.idx.smartspeakdock.calendar;

import android.view.View;

/**
 * Created by geno on 28/12/17.
 */

public interface OnItemClickListener {
    /**
     * item点击回调
     *
     * @param view
     * @param position
     */
    void onItemClick(View view, int position);

    /**
     * 删除按钮回调
     *
     * @param event
     * @param time
     */
    void onDeleteClick(String event,String time);
}