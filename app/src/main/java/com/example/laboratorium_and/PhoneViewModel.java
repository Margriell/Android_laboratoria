package com.example.laboratorium_and;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

public class PhoneViewModel extends AndroidViewModel {

    private PhoneRepository mRepository;
    private LiveData<List<Phone>> mAllPhones;

    public PhoneViewModel(Application application) {
        super(application);
        mRepository = new PhoneRepository(application);
        mAllPhones = mRepository.getAllPhones();
    }

    public LiveData<List<Phone>> getAllPhones() {
        return mAllPhones;
    }

    public void insert(Phone phone) {
        mRepository.insert(phone);
    }

    public void update(Phone phone) {
        mRepository.update(phone);
    }

    public void delete(Phone phone) {
        mRepository.delete(phone);
    }

    public void deleteAllPhones() {
        mRepository.deleteAllPhones();
    }
}
