package com.idx.smartspeakdock.data.local;

import android.arch.persistence.room.TypeConverter;

/**
 * Created by derik on 17-12-8.
 * Email: lionel.lp.wu@mail.foxconn.com
 */

public class Converters {
    @TypeConverter
    public static Gender shortToGender(short value) {
        return value == 0 ? Gender.MALE : Gender.FEMALE;
    }

    @TypeConverter
    public static short genderToShort(Gender gender) {
        short index = -1;
        if (gender != null) {
            if (gender.equals(Gender.MALE)) {
                index = 0;
            } else {
                index = 1;
            }
        }
        return index;
    }
}
