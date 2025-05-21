package com.example.laboratorium_and;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class AddPhoneActivity extends AppCompatActivity {

    private EditText addName, addBrand, addVersion, addWebsite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addphone);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        addName = findViewById(R.id.addName);
        addBrand = findViewById(R.id.addBrand);
        addVersion = findViewById(R.id.addVersion);
        addWebsite = findViewById(R.id.addWebsite);

        findViewById(R.id.buttonAddCancel).setOnClickListener(v -> finish());

        findViewById(R.id.buttonAddSave).setOnClickListener(v -> {
            String name = addName.getText().toString().trim();
            String brand = addBrand.getText().toString().trim();
            String version = addVersion.getText().toString().trim();
            String website = addWebsite.getText().toString().trim();

            boolean hasError = false;

            if (name.isEmpty()) {
                addName.setError("Pole nazwa nie może być puste");
                hasError = true;
            }
            if (brand.isEmpty()) {
                addBrand.setError("Pole marka nie może być puste");
                hasError = true;
            }
            if (version.isEmpty()) {
                addVersion.setError("Pole wersja Androida nie może być puste");
                hasError = true;
            }
            if (website.isEmpty()) {
                addWebsite.setError("Pole strona internetowa nie może być puste");
                hasError = true;
            }

            if (hasError) return;

            Intent resultIntent = new Intent();
            resultIntent.putExtra("name", name);
            resultIntent.putExtra("brand", brand);
            resultIntent.putExtra("androidVersion", version);
            resultIntent.putExtra("website", website);
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        findViewById(R.id.buttonAddWebsite).setOnClickListener(v -> {
            String url = addWebsite.getText().toString();
            if (!url.startsWith("http")) url = "http://" + url;
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
