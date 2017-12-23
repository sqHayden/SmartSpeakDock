package com.idx.smartspeakdock.music.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;



import com.idx.smartspeakdock.music.entity.LocalMusic;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class MediaUtils {
    //获取专辑封面的Uri
    private static final Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");
    public static List<LocalMusic> getAudioList(Context context) {
        List<LocalMusic> list = new ArrayList<>();

        ContentResolver contentResolver = context.getApplicationContext().getContentResolver();

        // 获得内部存储的音频
        Cursor cursor = contentResolver.query(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, null, null, null, null);
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            Bundle bundle = new Bundle();
            for (int i = 0; i < cursor.getColumnCount(); ++i) {
                int type = cursor.getType(i);
                String colName = cursor.getColumnName(i);
                switch (type) {
                    case Cursor.FIELD_TYPE_INTEGER:
                        bundle.putInt(colName, cursor.getInt(i));
                        break;
                    case Cursor.FIELD_TYPE_STRING:
                        bundle.putString(colName, cursor.getString(i));
                        break;
                }
            }
            list.add(new LocalMusic(bundle));
        }
        cursor.close();

        // 获得外部存储的音频
        cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            Bundle bundle = new Bundle();
            for (int i = 0; i < cursor.getColumnCount(); ++i) {
                int type = cursor.getType(i);
                String colName = cursor.getColumnName(i);
                switch (type) {
                    case Cursor.FIELD_TYPE_INTEGER:
                        bundle.putInt(colName, cursor.getInt(i));
                        break;
                    case Cursor.FIELD_TYPE_STRING:
                        bundle.putString(colName, cursor.getString(i));
                        break;
                }
            }
            list.add(new LocalMusic(bundle));
        }
        cursor.close();

        return list;
    }
    public static Bitmap getAlbumBitmapDrawable(LocalMusic localMusic) {
        if (localMusic == null) {
            return null;
        }
        return getAlbumBitmapDrawable(localMusic.getPath());
    }

    public static Bitmap getAlbumBitmapDrawable(String path) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(path);

        byte[] art = mediaMetadataRetriever.getEmbeddedPicture();

        return art != null ? BitmapFactory.decodeByteArray(art, 0, art.length) : null;
    }

}
