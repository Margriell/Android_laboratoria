package com.example.laboratorium_and;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

// Fragment pokazujący jeden obraz
public class ImageFragment extends Fragment {

    private static final String ARG_IMAGE_ID = "imageId";
    private long imageId = -1;
    private ImageView imageView;

    // Tworzy nową instancję fragmentu z ID obrazu
    public static ImageFragment newInstance(long imageId) {
        ImageFragment fragment = new ImageFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_IMAGE_ID, imageId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Pobierz ID obrazu z argumentów
        if (getArguments() != null) {
            imageId = getArguments().getLong(ARG_IMAGE_ID, -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image, container, false);
        imageView = view.findViewById(R.id.imageView);

        // Pokaż obraz jeśli ID jest poprawne
        if (imageId != -1) {
            displayImage(imageId);
        }

        return view;
    }

    // Ładuje obraz o danym ID do ImageView
    public void displayImage(long imageId) {
        this.imageId = imageId;
        if (imageView != null) {
            Uri imageUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(imageId));
            imageView.setImageURI(imageUri);
        }
    }
}

