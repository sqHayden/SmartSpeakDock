package com.idx.smartspeakdock.data.local;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

/**
 * Created by derik on 17-12-8.
 * Email: lionel.lp.wu@mail.foxconn.com
 */

@Database(entities = {User.class}, version = 1, exportSchema = false)
@TypeConverters(Converters.class)
public abstract class SmartDatabase extends RoomDatabase {

    private static SmartDatabase INSTANCE;

    public abstract UserDao userDao();

    private static final Object sLock = new Object();

    public static SmartDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (sLock) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            SmartDatabase.class, "Speaker.db").build();
                }
            }
        }
        return INSTANCE;
    }

    public void close(){
        if (INSTANCE != null) {
            INSTANCE.close();
        }
    }

}
