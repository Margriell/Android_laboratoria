package com.example.laboratorium_and;import androidx.lifecycle.LiveData;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

// DAO (Data Access Object) dla tabeli 'phone_table' w Room – operacje CRUD na encji Phone
@Dao
public interface PhoneDao {

    @Insert
    void insert(Phone phone);

    @Update
    void update(Phone phone);

    @Delete
    void delete(Phone phone);

    // Usuwa wszystkie rekordy z tabeli
    @Query("DELETE FROM phone_table")
    void deleteAllPhones();

    // Zwraca wszystkie telefony posortowane rosnąco po nazwie
    @Query("SELECT * FROM phone_table ORDER BY phone_name ASC")
    LiveData<List<Phone>> getAllPhones();
}
