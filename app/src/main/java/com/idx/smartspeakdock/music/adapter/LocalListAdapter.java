package com.idx.smartspeakdock.music.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.music.entity.LocalMusic;
import com.idx.smartspeakdock.music.entity.LocalMusicItem;

import java.util.List;


public class LocalListAdapter extends BaseAdapter {
    private Context mContext;
    private int mResource;
    private LocalMusicItem localMusicItem1;
    private LayoutInflater mInflater;
    private List<LocalMusicItem> mList;


    private static class ViewHolder {
        TextView title;
        TextView artist;
        TextView duration;
        ImageView album;
        ImageView status;
        TextView classification;
        int originClassificationHeight;
    }

    public LocalListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<LocalMusicItem> objects) {
        mContext = context;
        mResource = resource;
        mInflater = LayoutInflater.from(mContext);
        mList = objects;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public LocalMusicItem getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(mResource, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.title = (TextView) convertView.findViewById(R.id.title);
            viewHolder.artist = (TextView) convertView.findViewById(R.id.artist);
            viewHolder.duration = (TextView) convertView.findViewById(R.id.duration);
            viewHolder.album = (ImageView) convertView.findViewById(R.id.album);
            viewHolder.status = (ImageView) convertView.findViewById(R.id.status);
            viewHolder.classification = (TextView) convertView.findViewById(R.id.classification);
            viewHolder.originClassificationHeight = viewHolder.classification.getHeight();
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        LocalMusicItem localMusicItem = getItem(position);
        LocalMusic localMusic = localMusicItem.getAudio();

//        Bitmap bitmap=MediaUtils.getArtwork(mContext,localMusic.getId(),localMusic.getAlbumId(),true,true);
//        viewHolder.album.setImageBitmap(bitmap);

        viewHolder.title.setText(localMusic.getTitle());
        viewHolder.artist.setText(localMusic.getArtist());

        int totalSecond = localMusic.getDuration() / 1000;
        int minute = totalSecond / 60;
        int second = totalSecond % 60;

        viewHolder.duration.setText(String.format("%02d:%02d", minute, second));

        if (position == 0 || localMusicItem.getClassificationId() != getItem(position - 1).getClassificationId()) {
            viewHolder.classification.setText(localMusicItem.getClassificationName());
            viewHolder.classification.setHeight(viewHolder.originClassificationHeight);
            viewHolder.classification.setVisibility(View.VISIBLE);
        } else {
            viewHolder.classification.setText("");
            viewHolder.classification.setVisibility(View.GONE);
        }

        return convertView;
    }
}

