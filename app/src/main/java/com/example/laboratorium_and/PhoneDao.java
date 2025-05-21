package com.example.laboratorium_and;import androidx.lifecycle.LiveData;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PhoneDao {

    @Insert
    void insert(Phone phone);

    @Update
    void update(Phone phone);

    @Delete
    void delete(Phone phone);

    @Query("DELETE FROM phone_table")
    void deleteAllPhones();

    @Query("SELECT * FROM phone_table ORDER BY phone_name ASC")
    LiveData<List<Phone>> getAllPhones();
}
