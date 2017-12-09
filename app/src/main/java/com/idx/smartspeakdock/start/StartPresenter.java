package com.idx.smartspeakdock.start;

import android.util.Log;

import com.idx.smartspeakdock.data.UserDataSource;
import com.idx.smartspeakdock.data.UserRepository;
import com.idx.smartspeakdock.data.local.Gender;
import com.idx.smartspeakdock.data.local.User;

import java.util.List;


/**
 * Created by derik on 17-12-5.
 * Email: lionel.lp.wu@mail.foxconn.com
 */

public class StartPresenter implements StartContract.Presenter {

    private static final String TAG = StartPresenter.class.getName();
    private StartContract.View view;
    private UserRepository userRepository;

    public StartPresenter(UserRepository userRepository, StartContract.View view) {
        this.userRepository = userRepository;
        this.view = view;
        view.setPresenter(this);
    }

    @Override
    public void start() {
        Log.d(TAG, "start: load data");
        userRepository.getUsers(new UserDataSource.LoadUsersCallback(){

            @Override
            public void onUsersLoaded(List<User> users) {
                Log.d(TAG, "onUsersLoaded: " + users.size());
            }

            @Override
            public void onDataNotAvailable() {
                Log.d(TAG, "onDataNotAvailable: ");
                //TODO 调试使用, 待删除
                User user = new User("test2", "123456", "13133854271", Gender.FEMALE);
                userRepository.saveUser(user);
            }
        });

    }

    @Override
    public void updateContent() {
        Log.d(TAG, "updateContent: item clicked");
        userRepository.getUser(new UserDataSource.GetUserCallback() {
            @Override
            public void onUserLoaded(User user) {
                view.showContent(user.getUsername() + ", " + user.getPassword());
            }

            @Override
            public void onDataNotAvailable() {

                Log.d(TAG, "onDataNotAvailable: ");
            }
        });

    }
}
