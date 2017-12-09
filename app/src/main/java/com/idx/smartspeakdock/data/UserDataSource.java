package com.idx.smartspeakdock.data;

import com.idx.smartspeakdock.data.local.User;

import java.util.List;

/**
 * Created by derik on 17-12-7.
 * Email: lionel.lp.wu@mail.foxconn.com
 */

public interface UserDataSource {

    interface LoadUsersCallback{

        void onUsersLoaded(List<User> users);

        void onDataNotAvailable();
    }

    interface GetUserCallback {

        void onUserLoaded(User user);

        void onDataNotAvailable();
    }


    void getUser(GetUserCallback callback);

    void getUsers(LoadUsersCallback callback);

    void saveUser(User user);

    void deleteUser(String userName);

    void deleteUser(User user);

    void clear();

}
