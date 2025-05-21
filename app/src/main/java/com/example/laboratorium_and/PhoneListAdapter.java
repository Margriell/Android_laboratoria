package com.example.laboratorium_and;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PhoneListAdapter extends RecyclerView.Adapter<PhoneListAdapter.PhoneViewHolder> {

    private final LayoutInflater mInflater;

    private List<Phone> mPhones;

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
    @Override
    public PhoneViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.phone_item, parent, false);
        return new PhoneViewHolder(itemView);
    }

    // Wiązanie danych z widokiem
    @Override
    public void onBindViewHolder(PhoneViewHolder holder, int position) {
        if (mPhones != null) {
            Phone current = mPhones.get(position);
            holder.bind(current.getName(), current.getBrand());

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(current);
                }
            });
        }
    }

    // Ustawia listę telefonów i odświeża widok
    public void setPhones(List<Phone> phones) {
        mPhones = phones;
        notifyDataSetChanged(); // informuje, że dane się zmieniły
    }

    // Zwraca liczbę elementów
    @Override
    public int getItemCount() {
        return mPhones == null ? 0 : mPhones.size();
    }

    // Zwraca obiekt Phone z danej pozycji
    public Phone getPhoneAtPosition(int position) {
        return mPhones != null ? mPhones.get(position) : null;
    }

    class PhoneViewHolder extends RecyclerView.ViewHolder {

        private TextView nameTextView;
        private TextView brandTextView;

        public PhoneViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.phone_name);
            brandTextView = itemView.findViewById(R.id.phone_brand);
        }

        public void bind(String name, String brand) {
            nameTextView.setText(name);
            brandTextView.setText(brand);
        }
    }
}