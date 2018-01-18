package com.idx.smartspeakdock.Setting;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.idx.smartspeakdock.R;

/**
 * Created by ryan on 18-1-5.
 * Email: Ryan_chan01212@yeah.net
 */

public class SettingFragment extends Fragment {
    private static final String TAG = SettingFragment.class.getSimpleName();
    View mView;
    private Switch mSwitch;
    private TextView mAbout;

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

        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

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
    public void onDestroy() {super.onDestroy();}
}
