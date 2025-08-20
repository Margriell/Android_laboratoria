package com.example.laboratorium_and;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

// ViewModel dla zarządzania danymi telefonów w aplikacji (MVVM)
public class PhoneViewModel extends AndroidViewModel {

    // Repozytorium - pośrednik między ViewModel a bazą danych
    private final PhoneRepository mRepository;

    // LiveData z listą wszystkich telefonów z bazy
    private final LiveData<List<Phone>> mAllPhones;

    // Konstruktor - inicjalizacja repozytorium i pobranie danych
    public PhoneViewModel(Application application) {
        super(application);
        mRepository = new PhoneRepository(application);
        mAllPhones = mRepository.getAllPhones();
    }

    // Udostępnienie LiveData z listą telefonów do obserwacji w UI
    public LiveData<List<Phone>> getAllPhones() {
        return mAllPhones;
    }

    // Operacje CRUD delegowane do repozytorium
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
