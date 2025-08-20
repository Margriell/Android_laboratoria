package com.example.laboratorium_and;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class BrowseActivity extends AppCompatActivity implements ImageListFragment.OnImageSelectedListener {

    private boolean isTwoPane = false; // flaga określająca czy jest widok dwupanelowy (poziomy)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Włączenie przycisku "wstecz" w toolbarze
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Sprawdzenie, czy jest widoczny fragment z obrazem (tryb poziomy)
        if (findViewById(R.id.imageViewFragment) != null) {
            isTwoPane = true;
        }
    }

    // Obsługa kliknięcia w przycisk wstecz
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Callback wywoływany po wybraniu obrazka z listy
    @Override
    public void onImageSelected(long imageId) {
        if (isTwoPane) {
            // Jeśli tryb poziomy - aktualizuj fragment z obrazem po prawej
            ImageFragment fragment = (ImageFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.imageViewFragment);
            if (fragment != null) {
                fragment.displayImage(imageId);
            }
        } else {
            // Jeśli tryb pionowy - uruchom nową aktywność z obrazem
            Intent intent = new Intent(this, ViewActivity.class);
            intent.putExtra("imageId", imageId);
            startActivity(intent);
        }
    }
}
