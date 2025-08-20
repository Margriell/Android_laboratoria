package com.example.laboratorium_and;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DatabaseActivity extends AppCompatActivity {

    private PhoneViewModel phoneViewModel;
    private PhoneListAdapter adapter;

    // Launcher do dodawania nowego telefonu
    private final ActivityResultLauncher<Intent> addPhoneLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    String name = data.getStringExtra("name");
                    String brand = data.getStringExtra("brand");
                    String androidVersion = data.getStringExtra("androidVersion");
                    String website = data.getStringExtra("website");

                    Phone phone = new Phone(name, brand, androidVersion, website);
                    phoneViewModel.insert(phone);
                }
            });

    // Launcher do edycji istniejącego telefonu
    private final ActivityResultLauncher<Intent> editPhoneLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    long phoneId = data.getLongExtra("id", -1);
                    String name = data.getStringExtra("name");
                    String brand = data.getStringExtra("brand");
                    String androidVersion = data.getStringExtra("androidVersion");
                    String website = data.getStringExtra("website");

                    Phone updatedPhone = new Phone(name, brand, androidVersion, website);
                    updatedPhone.setId(phoneId);
                    phoneViewModel.update(updatedPhone);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database);

        // Ustaw toolbar i przycisk wstecz
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Zmień kolor ikony menu overflow na biały
        if (toolbar.getOverflowIcon() != null) {
            toolbar.getOverflowIcon().setTint(Color.WHITE);
        }

        // Inicjalizacja RecyclerView (lista telefonów) i adaptera (łączy dane z widokiem listy)
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        adapter = new PhoneListAdapter(this);

        // Kliknięcie na element listy – otwiera ekran edycji wybranego telefonu
        adapter.setOnItemClickListener(phone -> {
            Intent intent = new Intent(DatabaseActivity.this, EditPhoneActivity.class);
            intent.putExtra("id", phone.getId());
            intent.putExtra("name", phone.getName());
            intent.putExtra("brand", phone.getBrand());
            intent.putExtra("androidVersion", phone.getAndroidVersion());
            intent.putExtra("website", phone.getWebsite());
            editPhoneLauncher.launch(intent);
        });

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Pobierz ViewModel i obserwuj zmiany w liście telefonów
        phoneViewModel = new ViewModelProvider(this).get(PhoneViewModel.class);
        phoneViewModel.getAllPhones().observe(this, phones -> adapter.setPhones(phones));

        // Obsługa usuwania telefonu przez przesunięcie elementu
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                        return false; // brak obsługi przesuwania w pionie
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        int position = viewHolder.getAdapterPosition();
                        Phone phone = adapter.getPhoneAtPosition(position);
                        phoneViewModel.delete(phone); // usuń telefon z bazy
                    }
                });
        itemTouchHelper.attachToRecyclerView(recyclerView);

        // FloatingActionButton do dodawania nowego telefonu
        FloatingActionButton fab = findViewById(R.id.fabMain);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(DatabaseActivity.this, AddPhoneActivity.class);
            addPhoneLauncher.launch(intent);
        });

    }

    // Obsługuje kliknięcie strzałki wstecz w toolbarze
    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    // Dodanie menu z opcjami do toolbaru
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_database, menu);
        return true;
    }

    // Obsługa kliknięć w opcje menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_clear_all) {
            phoneViewModel.deleteAllPhones(); // usuń wszystkie telefony z bazy
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
