package com.idx.smartspeakdock.Setting;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.idx.smartspeakdock.BaseActivity;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.service.SpeakerService;
import com.idx.smartspeakdock.utils.PreUtils;

/**
 * Created by ryan on 18-1-5.
 * Email: Ryan_chan01212@yeah.net
 */

public class SettingFragment extends Fragment {
    private static final String TAG = SettingFragment.class.getSimpleName();
    private View mView;
    private Switch mSwitch;
    private TextView mAbout;
    public SettingFragment(){}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_setting,container,false);
        initView();
        return mView;
    }

    private void initView() {
        mSwitch = mView.findViewById(R.id.voice_switch);
        mAbout = mView.findViewById(R.id.about);
        boolean isEnable = PreUtils.getItemObject(getContext(), PreUtils.Items.SETTINGS,
                PreUtils.Settings.SPEAK_SERVICE_ENABLE_STATE, Boolean.class, false);
        mSwitch.setChecked(isEnable);
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PreUtils.setItemObject(getContext(), PreUtils.Items.SETTINGS,
                        PreUtils.Settings.SPEAK_SERVICE_ENABLE_STATE, b);
                if (b) {
                    if (!BaseActivity.isServiceRunning(getActivity(), SpeakerService.class.getName())) {
                        getActivity().startService(new Intent(getActivity(), SpeakerService.class));
                    }
                } else {
                    if (BaseActivity.isServiceRunning(getActivity(), SpeakerService.class.getName())) {
                        getActivity().stopService(new Intent(getActivity(), SpeakerService.class));
                    }
                }
            }
        });

        mAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(),AboutActivity.class));
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //判断平台版本
        judgePlatformVersion();
    }

    private void judgePlatformVersion() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            Log.i(TAG, "judgePlatformVersion: 平台版本小于19");
            mSwitch.setTextOn(getResources().getString(R.string.voice_switch_on_text));
            mSwitch.setTextOff(getResources().getString(R.string.voice_switch_off_text));
        }
    }

    @Override
    public void onDestroy() {super.onDestroy();}
}
