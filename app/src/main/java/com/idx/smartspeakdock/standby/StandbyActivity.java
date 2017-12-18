package com.idx.smartspeakdock.standby;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.idx.smartspeakdock.R;


public class StandbyActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private TimeView time_textView;
    private TextView data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        Window window = getWindow();
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        window.setFlags(flag, flag);
        setContentView(R.layout.activity_standby);

        time_textView = findViewById(R.id.time_textView);
        new TimeThread().start();

        data = findViewById(R.id.data_textView);
        data.setText(DataString.StringData());
    }

    class TimeThread extends Thread {
        @Override
        public void run() {
            do {
                try {
                    Thread.sleep(1000);
                    Message msg = new Message();
                    msg.what = 1;
                    mHandler.sendMessage(msg);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            while (true);
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    long sysTime = System.currentTimeMillis();
                    CharSequence sysTimerStr = DateFormat.format("hh:mm", sysTime);
                    time_textView.setText(sysTimerStr);
                    break;
                default:
                    break;
            }
        }
    };
}