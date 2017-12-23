package com.idx.smartspeakdock.music.util;

import java.io.FilenameFilter;

/**
 * Created by sunny on 17-12-19.
 */

public abstract class MusicFilter implements FilenameFilter {

    public boolean accept(String url, String name){
        return (name.endsWith(".mp3"));
    }
}
