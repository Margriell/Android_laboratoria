package com.example.laboratorium_and;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LiveData;

// Główna aktywność do obsługi pobierania plików
public class FileDownloadActivity extends AppCompatActivity {
    private static final int KOD_PROSBY_O_UPRAWNIENIA = 1; // Kod żądania uprawnień
    private EditText urlInput;
    private TextView fileSizeValue;
    private TextView fileTypeValue;
    private TextView bytesDownloadedValue;
    private ProgressBar progressBar;
    private ShortTask shortTask; // Obiekt do zadań w tle

    // Wiązanie z usługą pobierania
    private boolean serviceBound = false;
    private LiveData<ProgressEvent> progressEventLiveData;
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // Połączenie z usługą pobierania i obserwacja postępu
            serviceBound = true;
            DownloadService.DownloadServiceBinder binder = (DownloadService.DownloadServiceBinder) service;
            progressEventLiveData = binder.getProgressEvent();
            progressEventLiveData.observe(FileDownloadActivity.this, FileDownloadActivity.this::updateProgress);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // Odłączenie od usługi
            if (progressEventLiveData != null) {
                progressEventLiveData.removeObservers(FileDownloadActivity.this);
            }
            serviceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Inicjalizacja aktywności i interfejsu
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filedownload);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        }

        urlInput = findViewById(R.id.url_input);
        Button getInfoButton = findViewById(R.id.get_info_button);
        Button downloadButton = findViewById(R.id.download_button);
        fileSizeValue = findViewById(R.id.file_size_value);
        fileTypeValue = findViewById(R.id.file_type_value);
        bytesDownloadedValue = findViewById(R.id.bytes_downloaded_value);
        progressBar = findViewById(R.id.download_progress_bar);

        shortTask = new ShortTask();

        Intent serviceIntent = new Intent(this, DownloadService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra("progress_event")) {
                ProgressEvent event = intent.getParcelableExtra("progress_event");
                if (event != null) {
                    updateProgress(event);
                }
            }
        } else {
            String progressText = savedInstanceState.getString("progress_text");
            int progress = savedInstanceState.getInt("progress");
            int total = savedInstanceState.getInt("total");
            if (progressText != null) {
                bytesDownloadedValue.setText(progressText);
                if (total > 0) {
                    progressBar.setMax(total);
                    progressBar.setProgress(progress);
                }
            }
        }

        getInfoButton.setOnClickListener(v -> {
            // Obsługa pobierania informacji o pliku
            String urlStr = urlInput.getText().toString().trim();
            if (TextUtils.isEmpty(urlStr) || !urlStr.startsWith("https://")) {
                Toast.makeText(this, "Adres URL musi zaczynać się od https://", Toast.LENGTH_SHORT).show();
                return;
            }
            shortTask.executeTask(new ShortTask.ResultCallback() {
                @Override
                public void onSuccess(FileInfo result) {
                    fileSizeValue.setText(getString(R.string.file_size, result.size));
                    fileTypeValue.setText(result.type != null ? result.type : "?");
                }

                @Override
                public void onError(Throwable throwable) {
                    Toast.makeText(FileDownloadActivity.this, "Błąd: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    fileSizeValue.setText(getString(R.string.file_size_unknown));
                    fileTypeValue.setText("?");
                }
            }, urlStr);
        });

        downloadButton.setOnClickListener(v -> {
            // Obsługa pobierania pliku
            String urlStr = urlInput.getText().toString().trim();
            if (TextUtils.isEmpty(urlStr) || !urlStr.startsWith("https://")) {
                Toast.makeText(this, "Adres URL musi zaczynać się od https://", Toast.LENGTH_SHORT).show();
                return;
            }
            poprosOUpraweiniaIUruchomPobieranie(urlStr);
        });
    }

    private void updateProgress(ProgressEvent event) {
        // Aktualizacja postępu pobierania w UI
        if (event == null) return;

        bytesDownloadedValue.setText(getString(R.string.bytes_downloaded, event.progress, event.total));
        if (event.total > 0) {
            progressBar.setMax(event.total);
            progressBar.setProgress(event.progress);
        }

        if (event.result == ProgressEvent.OK) {
            Toast.makeText(this, "Pobieranie zakończone sukcesem", Toast.LENGTH_SHORT).show();
        } else if (event.result == ProgressEvent.ERROR) {
            Toast.makeText(this, "Błąd pobierania", Toast.LENGTH_SHORT).show();
            progressBar.setProgress(0);
        }
    }

    private void poprosOUpraweiniaIUruchomPobieranie(String urlStr) {
        // Prośba o uprawnienia i uruchomienie pobierania
        String[] wymaganeUprawnienia = pobierzWymaganeUprawnienia();
        boolean wszystkieUprawnienia = true;
        for (String uprawnienie : wymaganeUprawnienia) {
            if (ActivityCompat.checkSelfPermission(this, uprawnienie) != PackageManager.PERMISSION_GRANTED) {
                wszystkieUprawnienia = false;
                break;
            }
        }
        if (!wszystkieUprawnienia) {
            ActivityCompat.requestPermissions(this, wymaganeUprawnienia, KOD_PROSBY_O_UPRAWNIENIA);
        } else {
            uruchomPobieranie(urlStr);
        }
    }

    private String[] pobierzWymaganeUprawnienia() {
        // Określanie wymaganych uprawnień do pobierania w zależności od wersji Androida
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{Manifest.permission.POST_NOTIFICATIONS};
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        }
        return new String[]{};
    }

    private void uruchomPobieranie(String urlStr) {
        // Uruchomienie usługi pobierania
        Intent intent = new Intent(this, DownloadService.class);
        intent.putExtra("url", urlStr);
        startService(intent);
        Toast.makeText(this, "Rozpoczęto pobieranie", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Obsługa wyniku żądania uprawnień
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == KOD_PROSBY_O_UPRAWNIENIA && grantResults.length > 0) {
            boolean wszystkieZgodzone = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    wszystkieZgodzone = false;
                    break;
                }
            }
            if (wszystkieZgodzone) {
                uruchomPobieranie(urlInput.getText().toString().trim());
            } else {
                Toast.makeText(this, "Brak zgody na uprawnienia", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        // Zapis stanu aktywności przed zmianą orientacji
        super.onSaveInstanceState(outState);
        outState.putString("progress_text", bytesDownloadedValue.getText().toString());
        if (progressEventLiveData != null && progressEventLiveData.getValue() != null) {
            outState.putInt("progress", progressEventLiveData.getValue().progress);
            outState.putInt("total", progressEventLiveData.getValue().total);
        }
    }

    @Override
    protected void onDestroy() {
        // Zakończenie aktywności i wiązania z usługą
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound = false;
        }
        shortTask.shutdown();
    }
}