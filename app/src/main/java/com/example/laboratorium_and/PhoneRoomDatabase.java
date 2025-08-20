package com.example.laboratorium_and;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Database(entities = {Phone.class}, version = 1, exportSchema = false)
public abstract class PhoneRoomDatabase extends RoomDatabase {

    // Tag do logowania
    private static final String TAG = "PhoneRoomDatabase";

    // Wykonywanie operacji na bazie danych w tle
    public static Executor databaseWriteExecutor = Executors.newSingleThreadExecutor();

    // Singleton instancji bazy
    private static volatile PhoneRoomDatabase INSTANCE;

    // Abstrakcyjna metoda do pobrania DAO
    public abstract PhoneDao phoneDao();

    // Tworzenie lub zwracanie instancji bazy danych
    public static PhoneRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (PhoneRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    PhoneRoomDatabase.class, "phone_database")
                            .addCallback(sRoomDatabaseCallback) // Callback przy tworzeniu bazy
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // Callback do inicjalizacji bazy przykładowymi danymi
    private static final RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback() {
                @Override
                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                    super.onCreate(db);
                    databaseWriteExecutor.execute(() -> {
                        PhoneDao dao = getDatabaseExecutorSafeDao();
                        if (dao != null) {
                            dao.insert(new Phone("Pixel 9", "Google", "14", "https://store.google.com"));
                            dao.insert(new Phone("Pixel 9 Pro", "Google", "14", "https://store.google.com"));
                            dao.insert(new Phone("Pixel 9 Pro XL", "Google", "14", "https://store.google.com"));
                            dao.insert(new Phone("Pixel 9a", "Google", "14", "https://store.google.com"));
                        }
                    });
                }

                // Bezpieczne pobranie DAO w executorze
                private PhoneDao getDatabaseExecutorSafeDao() {
                    try {
                        return INSTANCE.phoneDao();
                    } catch (Exception e) {
                        Log.e(TAG, "Błąd podczas pobierania PhoneDao: " + e.getMessage());
                        return null;
                    }
                }
            };
    }
