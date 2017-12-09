package com.idx.smartspeakdock.data.local;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.UUID;

/**
 * Created by derik on 17-12-8.
 * Email: lionel.lp.wu@mail.foxconn.com
 */

@Entity(tableName = "user")
public class User {
    @NonNull
    @ColumnInfo(name = "_id")
    private String id;

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "username")
    private String username;

    @NonNull
    @ColumnInfo(name = "password")
    private String password;

    @NonNull
    @ColumnInfo(name = "phone")
    private String phoneNumber;

    @Nullable
    @ColumnInfo(name = "gender")
    private Gender gender;

    @Ignore
    public User(@NonNull String username, @NonNull String password, @NonNull String phoneNumber, Gender gender) {
        this(UUID.randomUUID().toString(), username, password, phoneNumber, gender);
    }

    public User(@NonNull String id, @NonNull String username, @NonNull String password, @NonNull String phoneNumber, Gender gender) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    public void setUsername(@NonNull String username) {
        this.username = username;
    }

    @NonNull
    public String getPassword() {
        return password;
    }

    public void setPassword(@NonNull String password) {
        this.password = password;
    }

    @NonNull
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(@NonNull String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Nullable
    public Gender getGender() {
        return gender;
    }

    public void setGender(@Nullable Gender gender) {
        this.gender = gender;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;

        return id.equals(user.id) && username.equals(user.username) &&
                password.equals(user.password) &&
                phoneNumber.equals(user.phoneNumber);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + username.hashCode();
        result = 31 * result + password.hashCode();
        result = 31 * result + phoneNumber.hashCode();
        return result;
    }
}
