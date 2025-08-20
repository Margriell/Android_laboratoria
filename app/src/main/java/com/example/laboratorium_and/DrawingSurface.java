package com.example.laboratorium_and;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

public class DrawingSurface extends SurfaceView implements SurfaceHolder.Callback {
    @SuppressWarnings("FieldCanBeLocal")
    private final SurfaceHolder mHolder; // Obiekt do kontroli powierzchni rysunkowej
    private Bitmap mBitmap; // Bitmapa, na której rysowany jest obraz
    private Canvas mBitmapCanvas; // Kanwa powiązana z bitmapą – faktyczne miejsce rysowania
    private final Paint mPaint; // Obiekt farby – określa kolor, grubość linii, styl rysowania
    private final Path mPath; // Ścieżka reprezentująca linię, którą użytkownik aktualnie rysuje
    private final Paint circlePaint; //Farba dla kółek
    private float startX, startY; // Koordynaty początku rysowanej linii
    private boolean isDrawing = false;

    /**
     * Konstruktor komponentu rysunkowego - wywoływany automatycznie przy tworzeniu XML
     */
    public DrawingSurface(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Pobranie uchwytu do powierzchni i dodanie callbacków do obserwacji zmian
        mHolder = getHolder();
        mHolder.addCallback(this);

        // Włączenie obsługi metody onDraw – umożliwia własne rysowanie
        setWillNotDraw(false);

        // Inicjalizacja farby
        mPaint = new Paint();
        mPaint.setColor(Color.BLUE);
        mPaint.setStrokeWidth(8);
        mPaint.setStyle(Paint.Style.STROKE); // tylko obrys (bez wypełnienia)
        mPaint.setAntiAlias(true); // wygładzenie krawędzi

        // Inicjalizacja ścieżki do rysowania linii
        mPath = new Path();

        // Inicjalizacja farby dla kółek
        circlePaint = new Paint(mPaint);
        circlePaint.setStyle(Paint.Style.FILL);
    }

    /**
     * Obsługa dotyku użytkownika
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = x;
                startY = y;
                isDrawing = true;
                mPath.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                mPath.lineTo(x, y);
                break;
            case MotionEvent.ACTION_UP:
                isDrawing = false;

                // Rysuj narysowaną ścieżkę na bitmapie (trwały rysunek)
                mBitmapCanvas.drawPath(mPath, mPaint);

                // Rysuj wypełnione kółka na początku i końcu linii
                mBitmapCanvas.drawCircle(startX, startY, 10, circlePaint);
                mBitmapCanvas.drawCircle(x, y, 10, circlePaint);

                mPath.reset(); // Czyść tymczasową ścieżkę
                break;
        }

        invalidate(); // Poproś o odrysowanie widoku
        performClick();
        return true;
    }

    /**
     * Rysowanie zawartości – wywoływane automatycznie, gdy invalidate() zostaje użyte
     */
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        // Narysuj bitmapę z dotychczasowym rysunkiem (zapisanym)
        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap, 0, 0, null);
        }

        // Narysuj aktualną ścieżkę, którą użytkownik rysuje
        canvas.drawPath(mPath, mPaint);

        // Jeśli trwa rysowanie, narysuj wypełnione kółko na początku linii
        if (isDrawing) {
            canvas.drawCircle(startX, startY, 10, circlePaint);
        }
    }

    /**
     * Metoda wywoływana automatycznie, gdy powierzchnia rysunkowa zostanie utworzona
     */
    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        // Utworzenie bitmapy o rozmiarze widoku i powiązanej kanwy do rysowania
        mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        mBitmapCanvas = new Canvas(mBitmap);

        clearScreen();
    }

    /**
     * Metoda wywoływana, gdy powierzchnia zmienia swój rozmiar lub format
     * (NIEUŻYWANE)
     */
    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
    }

    /**
     * Metoda wywoływana, gdy powierzchnia jest niszczona
     * (NIEUŻYWANE)
     */
    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
    }

    /**
     * Czyści zawartość bitmapy
     */
    public void clearScreen() {
        if (mBitmapCanvas != null) {
            mBitmapCanvas.drawColor(Color.WHITE);
        }
        mPath.reset();
        isDrawing = false;
        invalidate();
    }

    /**
     * Implementacja wymaganej metody performClick()
     * Zapewnia kompatybilność z Accessibility i unika ostrzeżeń lint
     */
    @Override
    public boolean performClick() {
        return super.performClick();
    }

    /**
     * Zmiana koloru farby po kliknięciu podanego przycisku
     */
    public void setPaintColor(int color) {
        mPaint.setColor(color);
        circlePaint.setColor(color);
    }

    /**
     * Zwraca bitmapę zawierającą aktualny rysunek
     */
    public Bitmap getBitmap() {
        return mBitmap;
    }

}
