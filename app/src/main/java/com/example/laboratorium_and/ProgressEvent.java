package com.example.laboratorium_and;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

// Klasa przechowująca stan i postęp pobierania z implementacją Parcelable
public class ProgressEvent implements Parcelable {
    // Statusy pobierania
    public static final int OK = 0;
    public static final int IN_PROGRESS = 1;
    public static final int ERROR = 2;

    public int progress; // Liczba pobranych bajtów
    public int total;    // Całkowity rozmiar pliku
    public int result;   // Status pobierania

    // Konstruktor podstawowy
    public ProgressEvent(int progress, int total, int result) {
        this.progress = progress;
        this.total = total;
        this.result = result;
    }

    // Konstruktor Parcelable
    protected ProgressEvent(Parcel in) {
        progress = in.readInt();
        total = in.readInt();
        result = in.readInt();
    }

    // Implementacja Parcelable
    public static final Creator<ProgressEvent> CREATOR = new Creator<ProgressEvent>() {
        @Override
        public ProgressEvent createFromParcel(Parcel in) {
            return new ProgressEvent(in);
        }

        @Override
        public ProgressEvent[] newArray(int size) {
            return new ProgressEvent[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(progress);
        dest.writeInt(total);
        dest.writeInt(result);
    }
}