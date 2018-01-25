package com.idx.smartspeakdock.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.idx.smartspeakdock.service.ControllerService;

/**
 * Created by ryan on 18-1-15.
 * Email: Ryan_chan01212@yeah.net
 */

public class BitmapUtils {
    public static Bitmap decodeBitmapFromResources(Context context,int resId){
        Bitmap bitmap = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ALPHA_8 ;
           bitmap = BitmapFactory.decodeResource(context.getResources(),resId,options);
        } catch (OutOfMemoryError e){
            e.printStackTrace();
        }
        return bitmap;
    }

    public static int calculateSampleSize(BitmapFactory.Options options,int reqWidth,int reqHeight){
        int width = options.outWidth;
        int height = options.outHeight;

        int inSampleSize = 1;

        if (width > reqWidth || height > reqHeight){
            width = width/2;
            height = height/2;
        }

        while (width / inSampleSize > reqWidth && height / inSampleSize > reqHeight){
            inSampleSize *= 2;
        }
        return inSampleSize;
    }

    public static Bitmap scaleBitmapFromResources(Context context,int resId,int reqWidth,int reqHeight){
        Bitmap bitmap = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(context.getResources(),resId,options);
            options.inSampleSize = calculateSampleSize(options,reqWidth,reqHeight);
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeResource(context.getResources(),resId,options);
        }catch (OutOfMemoryError error){
            error.printStackTrace();
        }
        return bitmap;
    }
}
