package com.example.laboratorium_and;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class EditPhoneActivity extends AppCompatActivity {

    private EditText editName, editBrand, editVersion, editWebsite;
    private long phoneId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editphone);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        editName = findViewById(R.id.editName);
        editBrand = findViewById(R.id.editBrand);
        editVersion = findViewById(R.id.editVersion);
        editWebsite = findViewById(R.id.editWebsite);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("id")) {
            phoneId = intent.getLongExtra("id", -1);
            editName.setText(intent.getStringExtra("name"));
            editBrand.setText(intent.getStringExtra("brand"));
            editVersion.setText(intent.getStringExtra("androidVersion"));
            editWebsite.setText(intent.getStringExtra("website"));
        }

        findViewById(R.id.buttonEditCancel).setOnClickListener(v -> finish());

        findViewById(R.id.buttonEditSave).setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            String brand = editBrand.getText().toString().trim();
            String version = editVersion.getText().toString().trim();
            String website = editWebsite.getText().toString().trim();

            boolean hasError = false;

            if (name.isEmpty()) {
                editName.setError("Pole nazwa nie może być puste");
                hasError = true;
            }
            if (brand.isEmpty()) {
                editBrand.setError("Pole marka nie może być puste");
                hasError = true;
            }
            if (version.isEmpty()) {
                editVersion.setError("Pole wersja Androida nie może być puste");
                hasError = true;
            }
            if (website.isEmpty()) {
                editWebsite.setError("Pole strona internetowa nie może być puste");
                hasError = true;
            }

            if (hasError) return;

            Intent resultIntent = new Intent();
            resultIntent.putExtra("id", phoneId);
            resultIntent.putExtra("name", name);
            resultIntent.putExtra("brand", brand);
            resultIntent.putExtra("androidVersion", version);
            resultIntent.putExtra("website", website);
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        findViewById(R.id.buttonEditWebsite).setOnClickListener(v -> {
            String url = editWebsite.getText().toString();
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
