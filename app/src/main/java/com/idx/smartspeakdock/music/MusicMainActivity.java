package com.idx.smartspeakdock.music;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.TextView;

import com.idx.smartspeakdock.R;

import java.util.List;

public  class MusicMainActivity extends AppCompatActivity  {

    private List<MusicBean> musicBeans = null;
    private MusicAdapter musicadapter;
    private ListView listview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_activity_main);
        listview = (ListView) findViewById(R.id.music_list_view);
        musicBeans  = MusicUtil.getMp3Infos(MusicMainActivity.this);
        musicadapter = new MusicAdapter(this, musicBeans);
        listview.setAdapter(musicadapter);
    }
}
