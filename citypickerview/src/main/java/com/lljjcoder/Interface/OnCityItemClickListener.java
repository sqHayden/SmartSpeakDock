package com.lljjcoder.Interface;

import com.lljjcoder.bean.CityBean;
import com.lljjcoder.bean.DistrictBean;
import com.lljjcoder.bean.ProvinceBean;

/**
 * 选择城市回调接口
 */
public interface OnCityItemClickListener {
    // 当选择省市区回调，返回选择的省市区
    void onSelected(ProvinceBean province, CityBean city, DistrictBean district);
    //取消选择
    void onCancel();
}
