package com.parse.starter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.parse.ParseUser;

import java.util.ArrayList;

/**
 * Created by Depa on 23/12/2015.
 */
public class CustomView extends View {

    private CustomViewListener myListener;

    private Paint paintUser, paintFriend, paintNameFriend, paintRadar, paintBackgroundRadar;
    private ArrayList<Float> longitudini;
    private ArrayList<Float> latitudini;
    private ArrayList<String> colori;
    private boolean coordinate;
    private SharedPreferences preferences;
    private Float azimuth;
    private int latoCortoSchermo;
    public final int RAGGIO_TERRA = 6371000;
    private float pixel_Metro;
    private float max;
    private int orientation;
    private float maxPixel;
    private int ultimoNumeroAmici = 0;
    private final double costToRadians = Math.PI / 180;

    public CustomView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public CustomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        myListener = (CustomViewListener) getContext();

        paintUser = new Paint();
        String c = ParseUser.getCurrentUser().getString(UserKey.COLORS_KEY);
        setColor(paintUser, c);

        paintUser.setStyle(Paint.Style.FILL_AND_STROKE);
        paintUser.setStrokeWidth(15);

        paintFriend = new Paint();
        paintFriend.setStyle(Paint.Style.FILL_AND_STROKE);
        paintFriend.setStrokeWidth(20);

        paintNameFriend = new Paint();
        paintNameFriend.setStyle(Paint.Style.STROKE);
        paintNameFriend.setStrokeWidth(2);
        paintNameFriend.setColor(Color.BLACK);

        paintRadar = new Paint();
        paintRadar.setColor(Color.GREEN);
        paintRadar.setStyle(Paint.Style.STROKE);
        paintRadar.setStrokeWidth(2);

        paintBackgroundRadar = new Paint();
        paintBackgroundRadar.setStyle(Paint.Style.FILL_AND_STROKE);
        paintBackgroundRadar.setColor(Color.BLACK);
        paintBackgroundRadar.setAlpha(127);


        longitudini = new ArrayList<>();
        latitudini = new ArrayList<>();
        colori = new ArrayList<>();
        coordinate = false;


    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        myListener = null;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (orientation == 0) {
            latoCortoSchermo = (MeasureSpec.getSize(widthMeasureSpec) - 10) / 2;
        } else {
            latoCortoSchermo = (MeasureSpec.getSize(heightMeasureSpec) - 10) / 2;
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.translate(canvas.getWidth() / 2, canvas.getHeight() / 2);
        drawRadar(canvas);
        if (coordinate) {

            for (int i = 0; i < latitudini.size(); i++) {
                setColor(paintFriend, colori.get(i));
                canvas.drawPoint(longitudini.get(i), latitudini.get(i), paintFriend);

            }
            clearList();
            coordinate = false;
        }
    }

    public void setPoint(ArrayList<Float> array_x, ArrayList<Float> array_y, ArrayList<String> c) {


        coordinate = true;
        colori.addAll(c);

        if (ultimoNumeroAmici != array_x.size()) {
            ultimoNumeroAmici = array_x.size();
            max = -1;
        }

        float myLong = Float.parseFloat(preferences.getString(UserKey.PREF_LNG_KEY, ""));
        float myLat = Float.parseFloat(preferences.getString(UserKey.PREF_LAT_KEY, ""));

        if (azimuth != null) {
            Log.d("azimuthView", String.valueOf(azimuth));
            ArrayList<Float> list_x = new ArrayList<>();
            ArrayList<Float> list_y = new ArrayList<>();

            for (int i = 0; i < array_x.size(); i++) {

                float diffLongRadians = (float) ((array_x.get(i) - myLong) * (costToRadians));
                float sumLatRadians = (float) ((array_y.get(i) + myLat) * (costToRadians));
                float averageLatRadians = sumLatRadians / 2;

                float y = (float) ((array_y.get(i) - myLat) * (costToRadians));
                float x = (float) (diffLongRadians * Math.cos(averageLatRadians));

                float distanceX = RAGGIO_TERRA * x;
                float distanceY = RAGGIO_TERRA * y;

                list_x.add(distanceX);
                list_y.add(distanceY);

                float distance = (float) (RAGGIO_TERRA * Math.sqrt(x * x + y * y));
                if (distance > max) {
                    max = distance;

                    if (myListener != null)
                        myListener.maxDistanceChange(max);
                }
            }

            pixel_Metro = (latoCortoSchermo) / max;
            maxPixel = max * pixel_Metro;

            for (int i = 0; i < list_x.size(); i++) {
                float x1 = (float) (Math.cos(azimuth) * list_x.get(i) - Math.sin(azimuth) * list_y.get(i)); //matrice di rotazione
                float y1 = (float) (Math.sin(azimuth) * list_x.get(i) + Math.cos(azimuth) * list_y.get(i));

                float coordPixel_X = x1 * pixel_Metro;
                float coordPixel_Y = y1 * pixel_Metro;

                longitudini.add(i, coordPixel_X);
                latitudini.add(i, -coordPixel_Y);
            }

        }

    }


    public void setAzimuth(float azimuth) {
        this.azimuth = azimuth;
    }

    public void clearList() {
        latitudini.clear();
        longitudini.clear();
        colori.clear();
    }


    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    private void drawRadar(Canvas canvas) {

        canvas.drawCircle(0, 0, latoCortoSchermo, paintBackgroundRadar);
        float massimoRaggio = latoCortoSchermo;
        float decr = massimoRaggio / 7;
        for (int i = 0; i < 7; i++) {
            canvas.drawCircle(0, 0, massimoRaggio, paintRadar);
            massimoRaggio -= decr;
        }
        canvas.drawLine(0, 0, 0, -latoCortoSchermo, paintRadar);
        canvas.drawLine(0, 0, latoCortoSchermo, 0, paintRadar);
        canvas.drawLine(0, 0, 0, latoCortoSchermo, paintRadar);
        canvas.drawLine(0, 0, -latoCortoSchermo, 0, paintRadar);


        canvas.drawPoint(0, 0, paintUser);
    }

    private void setColor(Paint paint, String color) {
        switch (color) {
            case "BLUE":
                paint.setColor(Color.BLUE);
                break;
            case "GREEN":
                paint.setColor(Color.GREEN);
                break;
            case "MAGENTA":
                paint.setColor(Color.MAGENTA);
                break;
            case "CYAN":
                paint.setColor(Color.CYAN);
                break;
            case "GRAY":
                paint.setColor(Color.GRAY);
                break;
            case "YELLOW":
                paint.setColor(Color.YELLOW);
                break;
            case "RED":
                paint.setColor(Color.RED);
                break;
            case "WHITE":
                paint.setColor(Color.WHITE);
                break;

        }
    }

    public interface CustomViewListener {
        public void maxDistanceChange(float d);
    }
}

