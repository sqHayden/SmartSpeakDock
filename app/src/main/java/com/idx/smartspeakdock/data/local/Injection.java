package com.idx.smartspeakdock.data.local;

import android.content.Context;

import com.idx.smartspeakdock.data.UserRepository;
import com.idx.smartspeakdock.data.remote.RemoteUserDataSource;
import com.idx.smartspeakdock.utils.AppExecutors;

/**
 * Created by derik on 17-12-8.
 * Email: lionel.lp.wu@mail.foxconn.com
 */

public class Injection {
    public static UserRepository provideUserRepository(Context context){
        SmartDatabase smartDatabase = SmartDatabase.getInstance(context);

        return UserRepository.getInstance(LocalUserDataSource.getInstance(new AppExecutors(),
                smartDatabase.userDao()), new RemoteUserDataSource());
    }
}
