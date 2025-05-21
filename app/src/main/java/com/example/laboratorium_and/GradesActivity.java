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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class GradesActivity extends AppCompatActivity {

    private LinearLayout gradesLayout;
    private Button calculateButton;
    private int numberOfGrades;
    private int[] savedGrades = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grades);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        }

        gradesLayout = findViewById(R.id.grades_layout);
        calculateButton = findViewById(R.id.calculate_button);

        if (savedInstanceState != null) {
            numberOfGrades = savedInstanceState.getInt("numberOfGrades", 5);
            savedGrades = savedInstanceState.getIntArray("selectedGrades");
        } else {
            numberOfGrades = getIntent().getIntExtra("numberOfGrades", 5);
        }

        for (int i = 0; i < numberOfGrades; i++) {
            String subjectName = getString(getResources().getIdentifier("subject_" + (i + 1), "string", getPackageName()));
            TextView subjectTextView = new TextView(this);
            subjectTextView.setText(subjectName);
            subjectTextView.setTextSize(18);
            gradesLayout.addView(subjectTextView);

            RadioGroup radioGroup = new RadioGroup(this);
            radioGroup.setOrientation(LinearLayout.HORIZONTAL);

            for (int j = 2; j <= 5; j++) {
                RadioButton radioButton = new RadioButton(this);
                radioButton.setText(String.valueOf(j));
                radioButton.setId(View.generateViewId());

                if (savedGrades != null && savedGrades.length > i && savedGrades[i] == j) {
                    radioButton.setChecked(true);
                }
                radioGroup.addView(radioButton);
            }
            gradesLayout.addView(radioGroup);
        }

        calculateButton.setOnClickListener(v -> {
            int total = 0;
            int count = 0;

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

            if (count == numberOfGrades) {
                double average = total / (double) count;

                Intent resultIntent = new Intent();
                resultIntent.putExtra("average", average);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, getString(R.string.toast_all_subjects_required), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
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
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }
}
