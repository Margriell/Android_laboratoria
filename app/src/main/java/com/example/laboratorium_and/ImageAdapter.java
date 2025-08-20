package com.example.laboratorium_and;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// Adapter do listy zapisanych obrazów
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    // Interfejs do obsługi kliknięć
    public interface OnItemClickListener {
        void onItemClick(Image image);
    }

    private final List<Image> imageList;
    private final OnItemClickListener listener;

    public ImageAdapter(List<Image> imageList, OnItemClickListener listener) {
        this.imageList = imageList;
        this.listener = listener;
    }

    // Pojedynczy element listy
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView imageName;

        // Obsługa kliknięcia w cały wiersz
        public ViewHolder(View itemView) {
            super(itemView);
            imageName = itemView.findViewById(R.id.imageName);
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(imageList.get(position));
                }
            });
        }
    }

    // Tworzy nowy widok (wiersz listy)
    @NonNull
    @Override
    public ImageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ViewHolder(v);
    }

    // Podstawia dane pod konkretny wiersz
    @Override
    public void onBindViewHolder(@NonNull ImageAdapter.ViewHolder holder, int position) {
        holder.imageName.setText(imageList.get(position).name);
    }

    // Zwraca liczbę elementów listy
    @Override
    public int getItemCount() {
        return imageList.size();
    }
}
