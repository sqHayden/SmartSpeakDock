package com.idx.smartspeakdock.data.local;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Created by derik on 17-12-8.
 * Email: lionel.lp.wu@mail.foxconn.com
 */

@Dao
public interface UserDao {

    @Query("SELECT * FROM user")
    List<User> getUsers();

    @Query("SELECT * FROM user WHERE username = :username")
    User getUserbyName(String username);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addUser(User user);

    @Delete
    void deleteUser(User user);

    @Query("DELETE FROM user WHERE username = :username")
    void deleteUser(String username);

    @Query("DELETE FROM user")
    void clear();

}
