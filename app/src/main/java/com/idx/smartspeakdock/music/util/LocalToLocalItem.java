package com.idx.smartspeakdock.music.util;


import com.idx.smartspeakdock.music.entity.LocalMusic;
import com.idx.smartspeakdock.music.entity.LocalMusicItem;


public interface LocalToLocalItem {
    LocalMusicItem apply(LocalMusic localMusic);
}
