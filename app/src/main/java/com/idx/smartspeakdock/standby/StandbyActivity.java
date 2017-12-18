package com.idx.smartspeakdock.standby;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.idx.smartspeakdock.BaseActivity;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.Swipe.SwipeActivity;

import java.util.Timer;
import java.util.TimerTask;


public class StandbyActivity extends BaseActivity {
    private static final String TAG = "MainActivity";
    private TimeView time_textView;
    private TextView data;
    private Intent intent;
    private Timer timer;
    private LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_standby);
        init();
        new TimeThread().start();
        if (Info.count != 0){
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(intent);
                }
            });
        }else{
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(intent);
                    timer.cancel();
                    Info.count = 1;
                }
            });
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    startActivity(intent);
                    Info.count = 1;
                }
            };
            timer.schedule(task, 2 * 1000);
        }
    }

    public void init(){
        time_textView = findViewById(R.id.time_textView);
        data = findViewById(R.id.data_textView);
        data.setText(DataString.StringData());
        intent = new Intent(this, SwipeActivity.class);
        timer = new Timer();
        layout = findViewById(R.id.line6);
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