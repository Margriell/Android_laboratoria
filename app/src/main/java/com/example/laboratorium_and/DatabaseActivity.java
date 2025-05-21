package com.example.laboratorium_and;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class DatabaseActivity extends AppCompatActivity {

    private PhoneViewModel phoneViewModel;

    private PhoneListAdapter adapter;

    // Launcher do uruchamiania aktywności dodawania nowego telefonu
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

    // Launcher do uruchamiania aktywności edytowania istniejącego telefonu
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

        // Konfiguracja paska narzędzi z możliwością powrotu - strzałka
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Konfiguracja RecyclerView, które będzie wyświetlało listę telefonów
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        adapter = new PhoneListAdapter(this);

        // Kliknięcie na element listy – otwiera ekran edycji
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

        phoneViewModel = new ViewModelProvider(this).get(PhoneViewModel.class);

        // Aktualizacja listy telefonów po każdej zmianie danych w bazie
        phoneViewModel.getAllPhones().observe(this, new Observer<List<Phone>>() {
            @Override
            public void onChanged(List<Phone> phones) {
                adapter.setPhones(phones);
            }
        });

        // Usuwanie telefonu przez przesunięcie
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                        int position = viewHolder.getAdapterPosition();
                        Phone phone = adapter.getPhoneAtPosition(position);
                        phoneViewModel.delete(phone);
                    }
                });
        itemTouchHelper.attachToRecyclerView(recyclerView);

        // FloatingActionButton do dodawania nowego telefonu
        FloatingActionButton fab = findViewById(R.id.fabMain);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(DatabaseActivity.this, AddPhoneActivity.class);
            addPhoneLauncher.launch(intent);
        });

        // Menu rozwijane
        ImageView menuIcon = findViewById(R.id.menu_icon);
        menuIcon.setOnClickListener(this::showPopupMenu);
    }

    // Wyświetla popup menu z opcją czyszczenia bazy danych
    private void showPopupMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_database, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_clear_all) {
                phoneViewModel.deleteAllPhones();
                return true;
            }
            return false;
        });

        popup.show();
    }

    // Obsługuje kliknięcie strzałki wstecz w pasku narzędzi
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Dodanie opcji do paska menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_database, menu);
        return true;
    }

    // Obsługuje kliknięcie opcji z menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_clear_all) {
            phoneViewModel.deleteAllPhones();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
