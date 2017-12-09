package com.idx.smartspeakdock.data;

import com.idx.smartspeakdock.data.local.LocalUserDataSource;
import com.idx.smartspeakdock.data.local.User;
import com.idx.smartspeakdock.data.remote.RemoteUserDataSource;

/**
 * Created by derik on 17-12-8.
 * Email: lionel.lp.wu@mail.foxconn.com
 */

public class UserRepository implements UserDataSource {

    private static UserRepository INSTANCE = null;
    private LocalUserDataSource localUserDataSource;
    private RemoteUserDataSource remoteUserDataSource;

    // 暂仅实现本地存储
    private UserRepository(LocalUserDataSource localUserDataSource, RemoteUserDataSource remoteUserDataSource){
        this.localUserDataSource = localUserDataSource;
        this.remoteUserDataSource = remoteUserDataSource;
    }

    public static UserRepository getInstance(LocalUserDataSource localUserDataSource, RemoteUserDataSource remoteUserDataSource){
        if (INSTANCE == null) {
            INSTANCE = new UserRepository(localUserDataSource, remoteUserDataSource);
        }
        return INSTANCE;
    }

    @Override
    public void getUser(GetUserCallback callback) {
        localUserDataSource.getUser(callback);
    }

    @Override
    public void getUsers(LoadUsersCallback callback) {
        localUserDataSource.getUsers(callback);
    }

    @Override
    public void saveUser(User user) {
        localUserDataSource.saveUser(user);
    }

    @Override
    public void deleteUser(String userName) {
        localUserDataSource.deleteUser(userName);
    }

    @Override
    public void deleteUser(User user) {
        localUserDataSource.deleteUser(user);
    }

    @Override
    public void clear() {
        localUserDataSource.clear();
    }
}
