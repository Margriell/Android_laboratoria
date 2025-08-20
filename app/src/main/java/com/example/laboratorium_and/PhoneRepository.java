package com.example.laboratorium_and;
import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;

// Repozytorium pośredniczące między bazą danych Room a ViewModel
// Wykonuje operacje na bazie danych w tle za pomocą executora
public class PhoneRepository {

    private final PhoneDao mPhoneDao;
    private final LiveData<List<Phone>> mAllPhones;

    public PhoneRepository(Application application) {
        PhoneRoomDatabase db = PhoneRoomDatabase.getDatabase(application);
        mPhoneDao = db.phoneDao();
        mAllPhones = mPhoneDao.getAllPhones();
    }

    public LiveData<List<Phone>> getAllPhones() {
        return mAllPhones;
    }

    public void insert(Phone phone) {
        PhoneRoomDatabase.databaseWriteExecutor.execute(() -> mPhoneDao.insert(phone));
    }

    public void update(Phone phone) {
        PhoneRoomDatabase.databaseWriteExecutor.execute(() -> mPhoneDao.update(phone));
    }

    public void delete(Phone phone) {
        PhoneRoomDatabase.databaseWriteExecutor.execute(() -> mPhoneDao.delete(phone));
    }

    public void deleteAllPhones() {
        PhoneRoomDatabase.databaseWriteExecutor.execute(mPhoneDao::deleteAllPhones);
    }
}
