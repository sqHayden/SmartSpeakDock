package com.idx.smartspeakdock.music.entity;

import android.text.format.DateUtils;

import java.io.Serializable;
import java.util.Locale;

/**
 * Created by sunny on 18-1-4.
 */

public class Music implements Serializable {

    private long id; // 歌曲ID
    private String title; // 歌曲名称
    private String album; // 专辑
    private int  albumId;//专辑ID
    private String displayName; //显示名称
    private String artist; // 歌手名称
    private long duration; // 歌曲时长
    private long size; // 歌曲大小
    private String url; // 歌曲路径
    private String lrcTitle; // 歌词名称
    private String lrcSize; // 歌词大小
    private int position;
    public Music(){
        super();
    }
    public Music(long id, String title, String album, int albumId,
                 String displayName, String artist, long duration, long size,
                 String url, String lrcTitle, String lrcSize,int position) {
        super();
        this.id = id;
        this.title = title;
        this.album = album;
        this.albumId = albumId;
        this.displayName = displayName;
        this.artist = artist;
        this.duration = duration;
        this.size = size;
        this.url = url;
        this.lrcTitle = lrcTitle;
        this.lrcSize = lrcSize;
        this.position=position;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public int  getAlbumId() {
        return albumId;
    }

    public void setAlbumId(int albumId) {
        this.albumId = albumId;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long  duration) {
        this.duration = duration;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLrcTitle() {
        return lrcTitle;
    }

    public void setLrcTitle(String lrcTitle) {
        this.lrcTitle = lrcTitle;
    }

    public String getLrcSize() {
        return lrcSize;
    }

    public void setLrcSize(String lrcSize) {
        this.lrcSize = lrcSize;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public String toString() {
        StringBuilder sb=new StringBuilder("音乐信息:\n");
        sb.append("id:").append(id).append("\n");
        sb.append("title:").append(title).append("\n");
        sb.append("album:").append(album).append("\n");
        sb.append("albumId").append(albumId).append("\n");
        sb.append("displayname").append(displayName).append("\n");
        sb.append("artist:").append(artist).append("\n");
        sb.append("duration").append(duration).append("\n");
        sb.append("size").append(size).append("\n");
        sb.append("url").append(url).append("\n");
        sb.append("lrcTitle").append(lrcTitle).append("\n");
        sb.append("lrcsize").append(lrcSize).append("\n");
        return sb.toString();
    }

    //将long类型的时间转化成 mm:ss类型
    public static String formatTime(String pattern, long milli) {
        int m = (int) (milli / DateUtils.MINUTE_IN_MILLIS);
        int s = (int) ((milli / DateUtils.SECOND_IN_MILLIS) % 60);
        String mm = String.format(Locale.getDefault(), "%02d", m);
        String ss = String.format(Locale.getDefault(), "%02d", s);
        return pattern.replace("mm", mm).replace("ss", ss);
    }

}
