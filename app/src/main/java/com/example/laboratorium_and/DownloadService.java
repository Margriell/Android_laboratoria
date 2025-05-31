package com.example.laboratorium_and;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.ContentValues;
import android.net.Uri;

// Klasa usługi do pobierania plików w tle
public class DownloadService extends Service {
    private static final String TAG = "UsługaPobierania"; // Tag do logowania
    private static final String CHANNEL_ID = "KanałPobierania"; // Identyfikator kanału powiadomień
    private static final int NOTIFICATION_ID_PROGRESS = 1; // ID powiadomienia w trakcie pobierania
    private static final int NOTIFICATION_ID_COMPLETE = 2; // ID powiadomienia po zakończeniu
    private NotificationManager notificationManager; // Zarządzanie powiadomieniami
    private HandlerThread handlerThread; // Wątek do obsługi pobierania
    private Handler handler; // Obsługa zadań w wątku
    private boolean isForeground = false; // Flaga stanu usługi pierwszoplanowej

    // LiveData do przesyłania postępu pobierania
    private MutableLiveData<ProgressEvent> progressLiveData = new MutableLiveData<>(null);

    // Binder do wiązania usługi z aktywnością
    public class DownloadServiceBinder extends android.os.Binder {
        LiveData<ProgressEvent> getProgressEvent() {
            return progressLiveData;
        }
    }

    private final IBinder binder = new DownloadServiceBinder(); // Instancja bindera

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // Metoda zwracająca binder do wiązania usługi
        return binder;
    }

    @Override
    public void onCreate() {
        // Inicjalizacja usługi i wątku pobierania
        super.onCreate();
        handlerThread = new HandlerThread("WątekPobierania");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        utworzKanalPowiadomien();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Rozpoczęcie usługi z pobraniem pliku
        String urlStr = intent.getStringExtra("url");
        if (!isForeground) {
            startForeground(NOTIFICATION_ID_PROGRESS, budujPowiadomienie("Pobieranie...", true, 0, 0, android.R.drawable.stat_sys_download).build());
            isForeground = true;
        }
        handler.post(() -> {
            try {
                pobierzPlik(urlStr); // Wywołanie metody pobierania z obsługą wyjątków
            } catch (IOException e) {
                Log.e(TAG, "Błąd podczas pobierania: " + e.getMessage()); // Logowanie błędu
                progressLiveData.postValue(new ProgressEvent(0, 0, ProgressEvent.ERROR)); // Ustawienie stanu błędu
                stopForeground(true); // Zatrzymanie usługi pierwszoplanowej
                isForeground = false;
                notificationManager.notify(NOTIFICATION_ID_COMPLETE,
                        budujPowiadomienie("Pobieranie nieudane: " + e.getMessage(), false, 0, 0, android.R.drawable.stat_notify_error).build()); // Powiadomienie o błędzie
            }
        });
        return START_NOT_STICKY;
    }

    private void utworzKanalPowiadomien() {
        // Tworzenie kanału powiadomień dla Androida 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Kanał Pobierania",
                    NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @SuppressLint("NewApi")
    private void pobierzPlik(String urlStr) throws IOException {
        // Wykonywanie pobierania pliku w tle
        BufferedInputStream input = null;
        FileOutputStream output = null;
        HttpURLConnection connection = null;
        Uri uri = null;
        String fileName = urlStr.substring(urlStr.lastIndexOf('/') + 1);

        try {
            URL url = new URL(urlStr); // Tworzenie obiektu URL
            connection = (HttpURLConnection) url.openConnection(); // Nawiązywanie połączenia
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 10)");
            connection.setInstanceFollowRedirects(true);
            connection.connect();

            int responseCode = connection.getResponseCode(); // Pobieranie kodu odpowiedzi
            Log.d(TAG, "Kod odpowiedzi HTTP: " + responseCode);
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("Serwer zwrócił kod: " + responseCode); // Rzucanie wyjątku przy błędzie
            }

            int contentLength = connection.getContentLength(); // Pobieranie rozmiaru pliku
            Log.d(TAG, "Połączenie z " + urlStr + " nawiązano. Rozmiar treści: " + contentLength + " bajtów");

            input = new BufferedInputStream(connection.getInputStream()); // Otwieranie strumienia wejściowego

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues(); // Tworzenie wartości dla MediaStore
                values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                values.put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream");
                values.put(MediaStore.Downloads.IS_PENDING, 1);
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri == null) {
                    throw new IOException("Nie udało się utworzyć wpisu w MediaStore."); // Rzucanie wyjątku przy błędzie
                }
                output = (FileOutputStream) getContentResolver().openOutputStream(uri); // Otwieranie strumienia wyjściowego
                if (output == null) {
                    throw new IOException("Nie udało się otworzyć strumienia zapisu dla URI: " + uri); // Rzucanie wyjątku przy błędzie
                }
            } else {
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS); // Pobieranie katalogu Downloads
                File file = new File(downloadsDir, fileName);
                if (file.exists()) {
                    file.delete(); // Usuwanie istniejącego pliku
                }
                output = new FileOutputStream(file); // Otwieranie strumienia wyjściowego dla starszych wersji
            }

            byte[] buffer = new byte[4096]; // Bufor do czytania danych
            int bytesRead;
            int totalBytes = 0;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead); // Zapis danych do pliku
                totalBytes += bytesRead;
                Log.d(TAG, "Pobrano " + bytesRead + " bajtów. Łącznie: " + totalBytes + " bajtów");

                progressLiveData.postValue(new ProgressEvent(totalBytes, contentLength, ProgressEvent.IN_PROGRESS)); // Aktualizacja postępu
                notificationManager.notify(NOTIFICATION_ID_PROGRESS,
                        budujPowiadomienie("Pobieranie...", true, totalBytes, contentLength, android.R.drawable.stat_sys_download).build()); // Aktualizacja powiadomienia
            }
            output.flush(); // Wymuszenie zapisu bufora

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && uri != null) {
                ContentValues values = new ContentValues(); // Aktualizacja stanu w MediaStore
                values.put(MediaStore.Downloads.IS_PENDING, 0);
                getContentResolver().update(uri, values, null, null);
                Log.d(TAG, "MediaStore zaktualizowano dla pliku " + fileName);
            }

            if (totalBytes > 0) {
                Log.d(TAG, "Plik " + fileName + " zapisany poprawnie. Rozmiar: " + totalBytes + " bajtów."); // Logowanie sukcesu
                progressLiveData.postValue(new ProgressEvent(totalBytes, contentLength, ProgressEvent.OK)); // Ustawienie stanu sukcesu
                stopForeground(true); // Zatrzymanie usługi pierwszoplanowej
                isForeground = false;
                notificationManager.notify(NOTIFICATION_ID_COMPLETE,
                        budujPowiadomienie("Pobieranie zakończone", false, totalBytes, contentLength, android.R.drawable.stat_sys_download_done).build()); // Powiadomienie o zakończeniu
            } else {
                throw new IOException("Plik ma 0 bajtów!"); // Rzucanie wyjątku przy zerowym rozmiarze
            }
        } catch (Exception e) {
            Log.e(TAG, "Błąd pobierania: " + e.getMessage()); // Logowanie błędu
            progressLiveData.postValue(new ProgressEvent(0, 0, ProgressEvent.ERROR)); // Ustawienie stanu błędu
            stopForeground(true); // Zatrzymanie usługi pierwszoplanowej
            isForeground = false;
            notificationManager.notify(NOTIFICATION_ID_COMPLETE,
                    budujPowiadomienie("Pobieranie nieudane: " + e.getMessage(), false, 0, 0, android.R.drawable.stat_notify_error).build()); // Powiadomienie o błędzie
        } finally {
            // Zamykanie zasobów z obsługą wyjątków
            try {
                if (input != null) {
                    input.close(); // Zamykanie strumienia wejściowego
                }
                if (output != null) {
                    output.flush(); // Wymuszenie zapisu bufora
                    output.close(); // Zamykanie strumienia wyjściowego
                }
                if (connection != null) {
                    connection.disconnect(); // Zamykanie połączenia
                }
            } catch (IOException e) {
                Log.e(TAG, "Błąd zamykania zasobów: " + e.getMessage()); // Logowanie błędu przy zamykaniu
            }
        }
        if (progressLiveData.getValue() == null || progressLiveData.getValue().result != ProgressEvent.IN_PROGRESS) {
            stopSelf(); // Zatrzymanie usługi, jeśli nie ma postępu
        }
    }

    private NotificationCompat.Builder budujPowiadomienie(String tresc, boolean isOngoing, int progress, int total, int icon) {
        // Budowanie powiadomienia z postępem i intencją powrotu
        Intent notificationIntent = new Intent(this, FileDownloadActivity.class);
        notificationIntent.putExtra("progress_event", new ProgressEvent(progress, total, isOngoing ? ProgressEvent.IN_PROGRESS : (progress > 0 ? ProgressEvent.OK : ProgressEvent.ERROR)));

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
        taskStackBuilder.addParentStack(FileDownloadActivity.class);
        taskStackBuilder.addNextIntent(notificationIntent);

        PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Pobieranie pliku")
                .setContentText(tresc)
                .setSmallIcon(icon)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(!isOngoing)
                .setContentIntent(pendingIntent);

        if (isOngoing && total > 0) {
            builder.setProgress(total, progress, false);
        }

        if (isOngoing) {
            builder.setOngoing(true);
        }

        return builder;
    }
}