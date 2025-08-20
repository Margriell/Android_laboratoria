package com.example.laboratorium_and;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

// Aktywność do wyświetlania wybranego obrazu
public class ViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Pokaż strzałkę cofania
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Wstaw fragment tylko przy pierwszym uruchomieniu
        if (savedInstanceState == null) {
            long imageId = getIntent().getLongExtra("imageId", -1);
            ImageFragment fragment = ImageFragment.newInstance(imageId);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.view_container, fragment)
                    .commit();
        }
    }

    // Obsługa kliknięcia strzałki cofania
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
