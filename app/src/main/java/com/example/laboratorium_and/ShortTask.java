package com.example.laboratorium_and;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.net.ssl.HttpsURLConnection;

// Klasa do wykonywania krótkich zadań w tle
public class ShortTask {
    private static final String TAG = ShortTask.class.getSimpleName(); // Tag do logowania
    private final ExecutorService executorService; // Pula wątków do zadań
    private final Handler mainThreadHandler; // Handler do wątku UI
    private Future<FileInfo> future; // Obiekt przyszłego wyniku

    public ShortTask() {
        // Inicjalizacja puli wątków i handlera
        executorService = Executors.newFixedThreadPool(2);
        mainThreadHandler = new Handler(Looper.getMainLooper());
    }

    // Interfejs dla wywołań zwrotnych
    public interface ResultCallback {
        void onSuccess(FileInfo result); // Wywołanie przy sukcesie
        void onError(Throwable throwable); // Wywołanie przy błędzie
    }

    // Uruchamia zadanie w tle
    public void executeTask(ResultCallback callback, String param) {
        // Jeśli jakieś zadanie już działa — anuluj je
        if (future != null && !future.isDone()) {
            future.cancel(true);
            Log.d(TAG, "Poprzednie zadanie zostało anulowane");
        }

        Log.d(TAG, "Rozpoczynam zadanie dla URL: " + param);

        Callable<FileInfo> asyncTask = () -> {
            HttpsURLConnection connection = null;
            try {
                URL url = new URL(param);
                connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("HEAD");
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpsURLConnection.HTTP_OK) {
                    throw new Exception("Nie udało się połączyć, kod: " + responseCode);
                }

                int contentLength = connection.getContentLength();
                String contentType = connection.getContentType();

                FileInfo result = new FileInfo(contentLength, contentType);

                Log.d(TAG, "Pobrano nagłówki: length=" + contentLength + ", type=" + contentType);

                // Przekazanie wyniku do wątku UI
                mainThreadHandler.post(() -> callback.onSuccess(result));

                return result;

            } catch (Exception e) {
                Log.e(TAG, "Błąd podczas wykonywania zadania", e);
                mainThreadHandler.post(() -> callback.onError(e));
                throw e; // żeby wyjątek był widoczny w logach ExecutorService
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        };

        // Uruchom zadanie w tle
        future = executorService.submit(asyncTask);
    }

    public void shutdown() {
        // Zamykanie puli wątków i anulowanie zadań
        if (future != null && !future.isDone()) {
            future.cancel(true);
            Log.d(TAG, "Zadanie zostało anulowane przed zamknięciem");
        }
        executorService.shutdown();
        Log.d(TAG, "ExecutorService został zamknięty");
    }
}
