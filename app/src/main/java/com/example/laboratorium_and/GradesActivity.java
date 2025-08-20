package com.example.laboratorium_and;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class GradesActivity extends AppCompatActivity {

    private LinearLayout gradesLayout;
    private int numberOfGrades;
    private int[] savedGrades = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grades);

        // Ustawienie paska narzędzi i przycisku wstecz
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        }

        gradesLayout = findViewById(R.id.grades_layout);
        Button calculateButton = findViewById(R.id.calculate_button);

        // Odczytanie liczby ocen i ich stanu (po obrocie ekranu)
        if (savedInstanceState != null) {
            numberOfGrades = savedInstanceState.getInt("numberOfGrades", 5);
            savedGrades = savedInstanceState.getIntArray("selectedGrades");
        } else {
            numberOfGrades = getIntent().getIntExtra("numberOfGrades", 5);
        }

        // Pobranie tablicy przedmiotów
        String[] subjects = getResources().getStringArray(R.array.subjects);

        // Tworzenie widoków ocen (tekst + RadioGroup)
        for (int i = 0; i < numberOfGrades; i++) {
            // Pobranie nazwy przedmiotu z tablicy
            String subjectName = subjects[i];

            TextView subjectTextView = new TextView(this);
            subjectTextView.setText(subjectName);
            subjectTextView.setTextSize(18);
            gradesLayout.addView(subjectTextView);

            RadioGroup radioGroup = new RadioGroup(this);
            radioGroup.setOrientation(LinearLayout.HORIZONTAL);

            // Dodanie przycisków do zaznaczenia oceny
            for (int j = 2; j <= 5; j++) {
                RadioButton radioButton = new RadioButton(this);
                radioButton.setText(String.valueOf(j));
                radioButton.setId(View.generateViewId());

                // Przywracanie wcześniej zaznaczonej oceny (jeśli istnieje)
                if (savedGrades != null && savedGrades.length > i && savedGrades[i] == j) {
                    radioButton.setChecked(true);
                }
                radioGroup.addView(radioButton);
            }
            gradesLayout.addView(radioGroup);
        }

        // Obsługa kliknięcia "Oblicz średnią"
        calculateButton.setOnClickListener(v -> {
            int total = 0;
            int count = 0;

            // Przechodzenie po wszystkich RadioGroup i sumowanie ocen
            for (int i = 0; i < gradesLayout.getChildCount(); i++) {
                View view = gradesLayout.getChildAt(i);
                if (view instanceof RadioGroup) {
                    RadioGroup group = (RadioGroup) view;
                    int selectedId = group.getCheckedRadioButtonId();

                    if (selectedId != -1) {
                        RadioButton selectedButton = findViewById(selectedId);
                        int grade = Integer.parseInt(selectedButton.getText().toString());
                        total += grade;
                        count++;
                    }
                }
            }

            // Jeżeli wszystkie oceny zostały wybrane — oblicz średnią i wróć
            if (count == numberOfGrades) {
                double average = total / (double) count;

                Intent resultIntent = new Intent();
                resultIntent.putExtra("average", average);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                // Komunikat jeśli nie wszystkie przedmioty mają ocenę
                Toast.makeText(this, getString(R.string.toast_all_subjects_required), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Obsługa kliknięcia strzałki wstecz na pasku narzędzi
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Zapisywanie zaznaczonych ocen przy zmianie stanu (obrót ekranu)
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        int[] selectedGrades = new int[numberOfGrades];
        int index = 0;

        for (int i = 0; i < gradesLayout.getChildCount(); i++) {
            View view = gradesLayout.getChildAt(i);
            if (view instanceof RadioGroup) {
                RadioGroup group = (RadioGroup) view;
                int selectedId = group.getCheckedRadioButtonId();

                if (selectedId != -1) {
                    RadioButton selectedButton = findViewById(selectedId);
                    selectedGrades[index++] = Integer.parseInt(selectedButton.getText().toString());
                } else {
                    selectedGrades[index++] = -1;
                }
            }
        }

        outState.putIntArray("selectedGrades", selectedGrades);
        outState.putInt("numberOfGrades", numberOfGrades);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }
}
