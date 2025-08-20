package com.example.laboratorium_and;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

// Adapter RecyclerView do wyświetlania listy telefonów w bazie danych
public class PhoneListAdapter extends RecyclerView.Adapter<PhoneListAdapter.PhoneViewHolder> {

    private final LayoutInflater mInflater;
    private final List<Phone> mPhones = new ArrayList<>(); // Inicjalizacja pustą listą dla bezpieczeństwa
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Phone phone);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public PhoneListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    // Tworzenie nowego ViewHoldera (z xml)
    @NonNull
    @Override
    public PhoneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.phone_item, parent, false);
        return new PhoneViewHolder(itemView);
    }

    // Wiązanie danych z widokiem
    @Override
    public void onBindViewHolder(@NonNull PhoneViewHolder holder, int position) {
        Phone current = mPhones.get(position);
        holder.bind(current.getName(), current.getBrand());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(current);
            }
        });
    }

    // Zwraca liczbę elementów
    @Override
    public int getItemCount() {
        return mPhones.size();
    }

    // Zwraca obiekt Phone z danej pozycji
    public Phone getPhoneAtPosition(int position) {
        return position >= 0 && position < mPhones.size() ? mPhones.get(position) : null;
    }

    // Aktualizuje listę telefonów z użyciem DiffUtil dla optymalnych powiadomień
    public void setPhones(List<Phone> newPhones) {
        List<Phone> oldPhones = new ArrayList<>(mPhones); // Kopia starej listy
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new PhoneDiffCallback(oldPhones, newPhones != null ? newPhones : new ArrayList<>()));
        mPhones.clear();
        mPhones.addAll(newPhones != null ? newPhones : new ArrayList<>());
        diffResult.dispatchUpdatesTo(this);
    }

    public static class PhoneViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameTextView;
        private final TextView brandTextView;

        public PhoneViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.phone_name);
            brandTextView = itemView.findViewById(R.id.phone_brand);
        }

        public void bind(String name, String brand) {
            nameTextView.setText(name);
            brandTextView.setText(brand);
        }
    }

    // DiffUtil do porównywania starych i nowych danych
    private static class PhoneDiffCallback extends DiffUtil.Callback {
        private final List<Phone> oldList;
        private final List<Phone> newList;

        public PhoneDiffCallback(List<Phone> oldList, List<Phone> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            // Porównujemy po ID, które jest unikalne
            return oldList.get(oldItemPosition).getId() == newList.get(newItemPosition).getId();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Phone oldPhone = oldList.get(oldItemPosition);
            Phone newPhone = newList.get(newItemPosition);
            // Porównujemy zawartość telefonu
            return oldPhone.getName().equals(newPhone.getName())
                    && oldPhone.getBrand().equals(newPhone.getBrand())
                    && oldPhone.getAndroidVersion().equals(newPhone.getAndroidVersion())
                    && oldPhone.getWebsite().equals(newPhone.getWebsite());
        }
    }
}