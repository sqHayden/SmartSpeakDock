package com.idx.smartspeakdock.start;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.idx.smartspeakdock.BaseActivity;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.data.local.Injection;
import com.idx.smartspeakdock.service.SpeakerService;
import com.idx.smartspeakdock.utils.ActivityUtils;

public class StartActivity extends BaseActivity implements StartFragment.OnFragmentInteractionListener {

    private static final String TAG = StartActivity.class.getName();
    private StartFragment startFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        startFragment = (StartFragment) getSupportFragmentManager().findFragmentById(R.id.container);
        if (startFragment == null) {
            startFragment = StartFragment.newInstance("Hello", "Please Continue");
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), startFragment, R.id.container);
        }

        if (!isServiceRunning(this, "com.idx.smartspeakdock.start.SpeakerService")) {
            startService(new Intent(this, SpeakerService.class));
        }

        new StartPresenter(Injection.provideUserRepository(getApplicationContext()), startFragment);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        Log.d(TAG, "onFragmentInteraction: " + uri);
    }

}
