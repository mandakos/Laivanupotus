package ryhma3.laivanupotus;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class ShipView extends View implements Runnable {

    /*
    Taisteluruudukot ovat int-tyyppisiä 2-ulotteisia matriiseja. Näitä arvoja käytetään täyttämään matriisi.
    Matriisin avulla piirretään ruudukot ja niiden ruudut väritetään sen mukaan mitä arvoja matriiseissa on.
     */
    final int NOSHIP = 0;
    final int SHIP = 1;
    final int HIT = 2;
    final int MISS = 3;

    final int CELLS = 10; //Ruutuja ruudukon yhdellä sivulla. Ruudukko on siis 10x10 ruutua

    Paint gridPaint = new Paint(); //Tyhjä ruutu
    Paint shipPaint = new Paint(); //ruutu, jossa on laiva
    Paint hitPaint = new Paint(); //ruutu, johon on ammuttu ja osuttu
    Paint missPaint = new Paint(); //Ruutu, johon on ammuttu mutta laukaus ei ole osunut

    //Muuttujat joilla lasketaan mihin ruudukot piirretään ja kuinka isoja ne ovat

    //Laitteen näytön korkeus ja leveys
    float screenWidth = 0.0f;
    float screenHeight = 0.0f;

    //Laitteen näytön pikselitiheys
    float screenWidthDpi = 0.0f;
    float screenHeightDpi = 0.0f;

    //Käyttöliittymän mitat
    float layoutWidth;
    float layoutHeight;

    //Käyttöliittymän viemä tila + tyhjään tilaan varattu korkeus & leveys
    float reservedSpaceX = 0.0f;
    float reservedSpaceY = 0.0f;

    // todellinen ruudukoille käytössä oleva tila
    float availableWidth = 0.0f;
    float availableHeight = 0.0f;

    //Muuttujat, joilla selvitetään laitteen näytön speksit
    WindowManager wm;
    Display display;
    Point size;
    DisplayMetrics metrics;

    //Matriisit omille ja vihollisen laivoille
    int[][] myGrid;
    int[][] enemyGrid;

    //Boolean-muuttujat, joilla tutkitaan onko näkymä tietyssä tilassa
    boolean shipsBeingSet = true; //true = laivojenasettelutila, false = taistelutila. Näkymä alkaa oletuksena asettelutilassa
    boolean myTurn = false; //Oletuksena kummankaan pelaajan vuoro ei ole vielä. Pelaaja voi ampua vain kun on hänen vuoronsa

    public ShipView(Context context) {
        super(context);
    }

    public ShipView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    //Metodia kutsutaan kun näkymä piirretään
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        gridPaint.setStyle(Paint.Style.STROKE); //Piirtää oletuksena valkoisia ruutuja mustalla reunuksella
        shipPaint.setStyle(Paint.Style.FILL_AND_STROKE); //Piirtää oletuksena täysin mustia ruutuja
        hitPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        hitPaint.setColor(Color.RED);
        missPaint.setColor(Color.LTGRAY);

        // Tilan laskentaan tarvittavat loitsut
        wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        display = wm.getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        //HUOM! Näkymä on horisontaalinen, mutta sizeen tallennetaan sivujen pituuden kuin puhelin olisi pystyasennossa
        screenWidth = size.y;
        screenHeight = size.x;
        metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        //Alla olevien avulla voidaan laskea helposti käyttöliittymän viemä tila prosentuaalisesti.
        //TODO: Järkevä tapa laskea käyttöliittymän mitat & käyttöliittymäsuunnittelu
        
        screenWidthDpi = metrics.ydpi;
        screenHeightDpi = metrics.xdpi;

        layoutWidth = findViewById(R.id.linearLayoutPlacement).getWidth();
        layoutHeight = findViewById(R.id.linearLayoutPlacement).getWidth();
    }

    @Override
    public void run() {
        postInvalidate(); //Aja tämä viimeiseksi, jotta näkymä tiedetään piirtää uudestaan
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //TODO: Ruudukon piirtämisen logiikka
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //TODO: Kosketustapahtumien logiikka
        return super.onTouchEvent(event);
    }
}
