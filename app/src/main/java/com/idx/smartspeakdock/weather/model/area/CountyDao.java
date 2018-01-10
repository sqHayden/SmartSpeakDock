package com.idx.smartspeakdock.weather.model.area;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Created by steve on 12/18/17.
 */
@Dao
public interface CountyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveCounty(County counties);

    @Query("Select * FROM county where cityId=:cityId")
    List<County> queryCounty(int cityId);
}
