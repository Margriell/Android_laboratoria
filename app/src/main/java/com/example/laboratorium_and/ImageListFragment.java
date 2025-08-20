package com.example.laboratorium_and;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

// Fragment pokazujący listę zapisanych obrazów
public class ImageListFragment extends Fragment {

    // Interfejs do przekazania kliknięcia
    public interface OnImageSelectedListener {
        void onImageSelected(long imageId);
    }

    private OnImageSelectedListener callback;
    private final List<Image> imageList = new ArrayList<>();

    // Sprawdza czy aktywność implementuje callback
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnImageSelectedListener) {
            callback = (OnImageSelectedListener) context;
        } else {
            throw new RuntimeException(context.getClass().getSimpleName() + " musi implementować OnImageSelectedListener");

        }
    }

    // Tworzy widok fragmentu
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_list, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Ładuj obrazy z pamięci
        loadImages();

        // Ustaw adapter z obsługą kliknięć
        ImageAdapter adapter = new ImageAdapter(imageList, image -> {
            if (callback != null) {
                callback.onImageSelected(image.id);
            }
        });
        recyclerView.setAdapter(adapter);

        return view;
    }

    // Pobiera dane z MediaStore (ID + nazwa)
    private void loadImages() {
        Cursor cursor = requireContext().getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME},
                null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                imageList.add(new Image(id, name));
            }
            cursor.close();
        }
    }
}
