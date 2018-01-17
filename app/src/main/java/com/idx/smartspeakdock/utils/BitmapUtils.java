package com.idx.smartspeakdock.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by ryan on 18-1-15.
 * Email: Ryan_chan01212@yeah.net
 */

public class BitmapUtils {
    public static Bitmap decodeBitmapFromResources(Context context,int resId){
        Bitmap bitmap = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;
           bitmap = BitmapFactory.decodeResource(context.getResources(),resId,options);
        } catch (OutOfMemoryError e){
            e.printStackTrace();
        }
        return bitmap;
    }
}
