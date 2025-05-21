package com.example.laboratorium_and;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;

public class StudentFormActivity extends AppCompatActivity {

    private EditText firstName, lastName, gradesNumber;
    private Button submitButton, endButton;
    private TextView averageTextView;
    private boolean isAverageVisible = false;
    private String averageText = "";
    private String endButtonText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_form2);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        }

        TextView header = findViewById(R.id.header);
        header.setText(getString(R.string.header_form));

        firstName = findViewById(R.id.first_name);
        lastName = findViewById(R.id.last_name);
        gradesNumber = findViewById(R.id.grades_number);
        submitButton = findViewById(R.id.submit_button);
        endButton = findViewById(R.id.end_button);
        averageTextView = findViewById(R.id.average_text);

        if (savedInstanceState != null) {
            firstName.setText(savedInstanceState.getString("firstName"));
            lastName.setText(savedInstanceState.getString("lastName"));
            gradesNumber.setText(savedInstanceState.getString("gradesNumber"));
            submitButton.setVisibility(savedInstanceState.getInt("submitButtonVisibility"));
            isAverageVisible = savedInstanceState.getBoolean("isAverageVisible", false);
            averageText = savedInstanceState.getString("averageText", "");
            endButtonText = savedInstanceState.getString("endButtonText", "");

            if (isAverageVisible) {
                averageTextView.setText(averageText);
                averageTextView.setVisibility(View.VISIBLE);
                endButton.setText(endButtonText);
                endButton.setVisibility(View.VISIBLE);
            }

        } else {
            submitButton.setVisibility(View.INVISIBLE);
            averageTextView.setVisibility(View.GONE);
            endButton.setVisibility(View.GONE);
        }

        firstName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validateFields();
            }
        });

        lastName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validateFields();
            }
        });

        gradesNumber.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validateFields();
            }
        });

        submitButton.setOnClickListener(v -> {
            String gradesNumberStr = gradesNumber.getText().toString().trim();
            int numberOfGrades = Integer.parseInt(gradesNumberStr);
            Intent intent = new Intent(StudentFormActivity.this, GradesActivity.class);
            intent.putExtra("numberOfGrades", numberOfGrades);
            startActivityForResult(intent, 1);
        });

        endButton.setOnClickListener(v -> {
            String buttonText = endButton.getText().toString();

            if (buttonText.equals(getString(R.string.end_button_good))) {
                Toast.makeText(this, getString(R.string.toast_congratulations), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, getString(R.string.toast_conditional_pass), Toast.LENGTH_LONG).show();
            }

            endButton.postDelayed(() -> {
                finishAffinity();
                System.exit(0);
            }, 1500);
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void validateFields() {
        boolean valid = true;

        String imie = firstName.getText().toString().trim();
        String nazwisko = lastName.getText().toString().trim();
        String liczbaOcenStr = gradesNumber.getText().toString().trim();

        if (imie.isEmpty()) {
            firstName.setError(getString(R.string.error_first_name));
            valid = false;
        } else {
            firstName.setError(null);
        }

        if (nazwisko.isEmpty()) {
            lastName.setError(getString((R.string.error_last_name)));
            valid = false;
        } else {
            lastName.setError(null);
        }

        int liczbaOcen = 0;
        if (liczbaOcenStr.isEmpty()) {
            gradesNumber.setError(getString(R.string.error_grades_number));
            valid = false;
        } else {
            try {
                liczbaOcen = Integer.parseInt(liczbaOcenStr);
                if (liczbaOcen < 5 || liczbaOcen > 15) {
                    gradesNumber.setError(getString(R.string.error_grades_range));
                    valid = false;
                } else {
                    gradesNumber.setError(null);
                }
            } catch (NumberFormatException e) {
                gradesNumber.setError(getString(R.string.error_invalid_number));
                valid = false;
            }
        }

        if (valid) {
            submitButton.setVisibility(View.VISIBLE);
        } else {
            submitButton.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("firstName", firstName.getText().toString());
        outState.putString("lastName", lastName.getText().toString());
        outState.putString("gradesNumber", gradesNumber.getText().toString());
        outState.putInt("submitButtonVisibility", submitButton.getVisibility());

        outState.putBoolean("isAverageVisible", isAverageVisible);
        outState.putString("averageText", averageTextView.getText().toString());
        outState.putString("endButtonText", endButton.getText().toString());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            double average = data.getDoubleExtra("average", -1);

            if (average != -1) {
                isAverageVisible = true;
                averageText = getString(R.string.average_placeholder, average);
                averageTextView.setVisibility(View.VISIBLE);
                averageTextView.setText(averageText);

                if (average >= 3) {
                    endButton.setText(R.string.end_button_good);
                } else {
                    endButton.setText(R.string.end_button_bad);
                }

                endButtonText = endButton.getText().toString();
                endButton.setVisibility(View.VISIBLE);
            }
        }
    }
}