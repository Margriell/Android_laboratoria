package com.example.laboratorium_and;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Database(entities = {Phone.class}, version = 1, exportSchema = false)
public abstract class PhoneRoomDatabase extends RoomDatabase {

    public static Executor databaseWriteExecutor = Executors.newSingleThreadExecutor();
    private static volatile PhoneRoomDatabase INSTANCE;

    public abstract PhoneDao phoneDao();

    public static PhoneRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (PhoneRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    PhoneRoomDatabase.class, "phone_database")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

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

                private PhoneDao getDatabaseExecutorSafeDao() {
                    try {
                        return INSTANCE.phoneDao();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            };
    }
