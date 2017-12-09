package com.idx.smartspeakdock.data.local;

import android.support.annotation.NonNull;

import com.idx.smartspeakdock.data.UserDataSource;
import com.idx.smartspeakdock.utils.AppExecutors;

import java.util.List;

/**
 * Created by derik on 17-12-7.
 */

public class LocalUserDataSource implements UserDataSource {

    private static volatile LocalUserDataSource INSTANCE;

    private UserDao mUserDao;
    private AppExecutors mAppExecutors;

    private LocalUserDataSource(@NonNull AppExecutors appExecutors,
                                @NonNull UserDao userDao){
        mAppExecutors = appExecutors;
        mUserDao = userDao;
    }

    public static LocalUserDataSource getInstance(@NonNull AppExecutors appExecutors,
                                                  @NonNull UserDao userDao){
        if (INSTANCE == null) {
            synchronized (LocalUserDataSource.class){
                if (INSTANCE == null) {
                    INSTANCE = new LocalUserDataSource(appExecutors, userDao);
                }
            }
        }

        return INSTANCE;
    }

    @Override
    public void getUser(@NonNull final GetUserCallback callback) {
        mAppExecutors.getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                final List<User> users = mUserDao.getUsers();

                mAppExecutors.getMainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (users.isEmpty()){
                            callback.onDataNotAvailable();
                        } else{
                            callback.onUserLoaded(users.get(0));
                        }
                    }
                });
            }
        });

    }

    @Override
    public void getUsers(final LoadUsersCallback callback) {
        mAppExecutors.getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                final List<User> users = mUserDao.getUsers();

                mAppExecutors.getMainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (users.isEmpty()){
                            callback.onDataNotAvailable();
                        } else{
                            callback.onUsersLoaded(users);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void saveUser(@NonNull final User user) {
        mAppExecutors.getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                mUserDao.addUser(user);
            }
        });
    }

    @Override
    public void deleteUser(final String userName) {
        mAppExecutors.getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                mUserDao.deleteUser(userName);
            }
        });
    }

    @Override
    public void deleteUser(final User user) {
        mAppExecutors.getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                mUserDao.deleteUser(user);
            }
        });
    }

    @Override
    public void clear() {
        mAppExecutors.getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                mUserDao.clear();
            }
        });
    }
}
