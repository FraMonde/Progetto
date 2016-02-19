package com.parse.starter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by francy on 19/02/16.
 */
public class MapView extends View {

    private Paint paintUser, paintFriend, paintNameFriend, paintRadar, paintBackgroundRadar;
    private float x, y;
    private ArrayList<Float> longitudini;
    private ArrayList<Float> latitudini;
    private ArrayList<String> colori;
    private ArrayList<String> nomi;
    private ArrayList<Float> distanze;
    private boolean coordinate;
    private SharedPreferences preferences;
    private Float azimuth;
    private int width;
    public final int RAGGIO_TERRA = 6371000;
    private float pixel_Metro;
    private float max = -1;
    private int orientation;
    private float maxPixel;
    private float[] mR = new float[9];
    private boolean rotazione;
    private float pitch;
    private float roll;

    public MapView(Context context) {
        super(context);
    }

    public MapView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public MapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        preferences = PreferenceManager.getDefaultSharedPreferences(context);

        paintUser = new Paint();
        paintUser.setColor(0xffff0000); //red
        paintUser.setStyle(Paint.Style.FILL_AND_STROKE);
        paintUser.setStrokeWidth(10);

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


        longitudini = new ArrayList<>();
        latitudini = new ArrayList<>();
        colori = new ArrayList<>();
        nomi = new ArrayList<>();
        distanze = new ArrayList<>();
        coordinate = false;

        rotazione = false;

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (orientation == 0) {
            width = MeasureSpec.getSize(widthMeasureSpec) - 10;  //TODO: sistemare sto 10
        } else {
            width = MeasureSpec.getSize(heightMeasureSpec) - 10;
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.translate(canvas.getWidth() / 2, canvas.getHeight() / 2);

        if (coordinate) {
            drawRadar(canvas);
            for (int i = 0; i < latitudini.size(); i++) {
                if (colori.get(i).equals("BLUE")) {
                    paintFriend.setColor(Color.BLUE);
                } else if (colori.get(i).equals("GREEN")) {
                    paintFriend.setColor(Color.GREEN);
                }

                canvas.drawPoint(longitudini.get(i), latitudini.get(i), paintFriend);

            }
            clearList();

        }
    }

    public void setPoint(ArrayList<Float> array_x, ArrayList<Float> array_y, ArrayList<String> c, ArrayList<String> n) {

        coordinate = true;
        colori.addAll(c);
        nomi.addAll(n);
        //prendo le mie coordinate
        float myLong = Float.parseFloat(preferences.getString(UserKey.PREF_LNG_KEY, ""));
        float myLat = Float.parseFloat(preferences.getString(UserKey.PREF_LAT_KEY, ""));


        if (azimuth != null) {
            ArrayList<Float> list_x = new ArrayList<>();
            ArrayList<Float> list_y = new ArrayList<>();

            for (int i = 0; i < array_x.size(); i++) {

                float diffLongRadians = (float) ((array_x.get(i) - myLong) * (Math.PI / 180));
                float sumLatRadians = (float) ((array_y.get(i) + myLat) * (Math.PI / 180));
                float averageLatRadians = sumLatRadians / 2;

                float y = (float) ((array_y.get(i) - myLat) * (Math.PI / 180));
                float x = (float) (diffLongRadians * Math.cos(averageLatRadians));

                float distanceX = RAGGIO_TERRA * x;
                float distanceY = RAGGIO_TERRA * y;

                list_x.add(distanceX);
                list_y.add(distanceY);

                float distance = (float) (RAGGIO_TERRA * Math.sqrt(x * x + y * y));
                distanze.add(i, distance);
                if (distance > max) {
                    max = distance;
                }
            }

            pixel_Metro = (width / 2) / max;
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
        distanze.clear();
    }


    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    private void drawRadar(Canvas canvas) {
        canvas.drawCircle(0, 0, maxPixel, paintBackgroundRadar);
        float massimoRaggio = maxPixel;
        float decr = massimoRaggio / 7;
        for (int i = 0; i < 7; i++) {
            canvas.drawCircle(0, 0, massimoRaggio, paintRadar);
            massimoRaggio -= decr;
        }
        canvas.drawLine(0, 0, 0, -maxPixel, paintRadar);
        canvas.drawLine(0, 0, maxPixel, 0, paintRadar);
        canvas.drawLine(0, 0, 0, maxPixel, paintRadar);
        canvas.drawLine(0, 0, -maxPixel, 0, paintRadar);


        canvas.drawPoint(0, 0, paintUser);
    }
    public void setR(float[] mR){
        rotazione = true;
        this.mR = mR;
    }
    public void setPitch(float pitch){
        this.pitch = pitch;
    }

    public void setRoll(float roll) {
        this.roll = roll;
    }
}
