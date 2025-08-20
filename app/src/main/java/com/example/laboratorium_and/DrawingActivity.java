package com.example.laboratorium_and;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DrawingActivity extends AppCompatActivity {
    private static final String TAG = "DrawingActivity";
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1001;
    private DrawingSurface drawingSurface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Toolbar z ikoną powrotu i białym kolorem ikon menu
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (toolbar.getOverflowIcon() != null) {
            toolbar.getOverflowIcon().setTint(Color.WHITE);
        }

        // Powierzchnia rysowania
        drawingSurface = findViewById(R.id.drawing_surface);

        // Przyciski wyboru koloru i czyszczenia
        findViewById(R.id.btn_red).setOnClickListener(v ->
                drawingSurface.setPaintColor(Color.RED));
        findViewById(R.id.btn_yellow).setOnClickListener(v ->
                drawingSurface.setPaintColor(Color.YELLOW));
        findViewById(R.id.btn_green).setOnClickListener(v ->
                drawingSurface.setPaintColor(Color.GREEN));
        findViewById(R.id.btn_blue).setOnClickListener(v ->
                drawingSurface.setPaintColor(Color.BLUE));
        findViewById(R.id.btn_clear).setOnClickListener(v ->
                drawingSurface.clearScreen());
    }

    // Dodanie menu z opcjami zapisu i przeglądania
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_drawing, menu);
        return true;
    }

    // Obsługa menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            // Obsługa strzałki cofania
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        } else if (id == R.id.action_save_image) {
            // Sprawdzenie uprawnień i zapisanie aktualnego rysunku do pliku
            checkPermissionAndSaveImage();
            return true;
        } else if (id == R.id.action_browse_images) {
            // Uruchomienie BrowseActivity – lista zapisanych obrazów
            Intent intent = new Intent(this, BrowseActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Sprawdzenie uprawnień i zapis pliku
    private void checkPermissionAndSaveImage() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) { // Android 9 i starsze
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // Prośba o uprawnienie
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_WRITE_EXTERNAL_STORAGE);
            } else {
                // Uprawnienie już przyznane
                saveImage();
            }
        } else {
            // Android 10+ nie potrzebuje uprawnień do zapisu w MediaStore
            saveImage();
        }
    }

    //Sprawdzenie czy uprawnienie zostało przyznane, jeśli tak zapis obrazu
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveImage();
            } else {
                Toast.makeText(this, "Brak uprawnień do zapisu plików", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Zapis obrazu do MediaStore
    private void saveImage() {
        Log.d(TAG, "save image");
        ContentResolver resolver = getApplicationContext().getContentResolver();

        // Wybierz właściwą lokalizację zapisu w zależności od wersji Androida
        Uri imageCollection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            imageCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            imageCollection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        ContentValues imageDetails = new ContentValues();

        // Tworzymy unikalną nazwę pliku na podstawie daty i czasu
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String fileName = "IMG_" + timeStamp + ".png";
        imageDetails.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            imageDetails.put(MediaStore.Images.Media.IS_PENDING, 1);
        }

        Uri imageUri = resolver.insert(imageCollection, imageDetails);
        if (imageUri == null) {
            Toast.makeText(this, "Błąd: Nie udało się utworzyć pliku", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Nie udało się utworzyć URI w MediaStore");
            return;
        }

        // Zapis bitmapy do pliku
        try (ParcelFileDescriptor pfd = resolver.openFileDescriptor(imageUri, "w", null)){
            if (pfd == null) {
                Toast.makeText(this, "Błąd: Nie udało się otworzyć pliku", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Nie udało się otworzyć deskryptora pliku dla URI: " + imageUri);
                return;
            }
            try (FileOutputStream fos = new FileOutputStream(pfd.getFileDescriptor())) {
                Bitmap bitmap = drawingSurface.getBitmap();
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)) {
                    Toast.makeText(this, "Błąd podczas kompresji obrazu", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Obraz zapisany jako " + fileName, Toast.LENGTH_SHORT).show();
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Błąd zapisu pliku: " + e.getMessage());
            Toast.makeText(this, "Błąd zapisu pliku", Toast.LENGTH_SHORT).show();
        }

        // Zakończenie zapisu (dla Android 10+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            imageDetails.clear();
            imageDetails.put(MediaStore.Images.Media.IS_PENDING, 0);
            resolver.update(imageUri, imageDetails, null, null);
        }
    }
}
