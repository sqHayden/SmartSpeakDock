package com.idx.smartspeakdock.music.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.music.entity.Music;
import com.idx.smartspeakdock.music.service.MusicService;
import com.idx.smartspeakdock.music.util.MusicUtil;

import java.util.ArrayList;

/**
 * Created by sunny on 18-1-4.
 */

public class MusicAdapter extends BaseAdapter {


    private Music music;
    private int mPlayingPosition;

    ArrayList<Music> list=new ArrayList<Music>();

    //所有控件对象引用
    public class ViewHolder {
        //专辑图片
        public ImageView albumImage;
        //音乐标题
        public TextView musicTitle;
        //音乐时长
        public TextView musicDuration;
        //音乐艺术家
        public TextView musicArtist;
    }

    @Override
    public int getCount() {
        return MusicUtil.getMusic().size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if(convertView == null)
        {
            viewHolder = new ViewHolder();
            convertView= LayoutInflater.from(parent.getContext()).inflate(R.layout.music_holder_list,null);
            viewHolder.musicTitle = (TextView) convertView.findViewById(R.id.tv_title);
//            viewHolder.musicArtist = (TextView) convertView.findViewById(R.id.tv_artist);
            viewHolder.musicDuration=(TextView)convertView.findViewById(R.id.tv_duration) ;
            viewHolder.albumImage = (ImageView) convertView.findViewById(R.id.iv_cover);
            convertView.setTag(viewHolder);			//表示给View添加一个格外的数据，
        }
        else {
            viewHolder = (ViewHolder)convertView.getTag();//通过getTag的方法将数据取出来
        }

        for (String key:MusicUtil.getMusic().keySet()){
            list.add(MusicUtil.getMusic().get(key));
        }

        music =list.get(position) ;

        viewHolder.musicTitle.setText( music.getTitle());		//显示标题
//        viewHolder.musicArtist.setText( music.getArtist());		//显示艺术家
//        viewHolder.albumImage.setImageBitmap(music.getAlbum());
//        viewHolder.musicDuration.setText(music.getDuration());
        return convertView;
    }

    public void updatePlayingPosition(MusicService musicService) {
        if (musicService.getPlayingMusic() != null ) {
            mPlayingPosition = musicService.getPlayingPosition();
        } else {
            mPlayingPosition = -1;
        }
    }
}
