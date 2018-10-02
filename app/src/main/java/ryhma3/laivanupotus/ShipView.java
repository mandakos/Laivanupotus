package ryhma3.laivanupotus;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

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

    final String TAG = "ShipView";

    //Ruudukon piirtämiseen tarvittavat värit
    Paint gridPaint = new Paint(); //Tyhjä ruutu
    Paint shipPaint = new Paint(); //ruutu, jossa on laiva
    Paint hitPaint = new Paint(); //ruutu, johon on ammuttu ja osuttu
    Paint missPaint = new Paint(); //Ruutu, johon on ammuttu mutta laukaus ei ole osunut
    Paint targetPaint = new Paint(); //Ruutu, jonka päällä tähtäin on


    //Muuttujat joilla lasketaan mihin ruudukot piirretään ja kuinka isoja ne ovat
    /*
    //Laitteen näytön korkeus ja leveys
    float screenWidth = 0.0f;
    float screenHeight = 0.0f;
    */

    // Tämän näkymän mitat
    float viewWidth = 0.0f;
    float viewHeight = 0.0f;

    //Marginaalien leveys ja korkeus
    float marginWidth = 0.0f;
    float marginHeight = 0.0f;

    // todellinen ruudukoille käytössä oleva tila (vähennetään näkymän mitoista tyhjä tila ja marginaalit
    float availableWidth = 0.0f;
    float availableHeight = 0.0f;

    //Ruudukon ja yhden ruudun sivujen pituudet
    float gridSideLength = 0.0f;
    float cellSideLength = 0.0f;

    //Muuttujat, joilla selvitetään laitteen näytön speksit
    /*
    WindowManager wm;
    Display display;
    Point size;
    DisplayMetrics metrics;
    */

    //Matriisit omille ja vihollisen laivoille
    int[][] myGrid;
    int[][] enemyGrid;

    //Tähtäimen koordinaatit. Tähtäin piirtyy vakiona ruutuun 0,0 vihollisen ruudukossa
    int targetingX;
    int targetingY;

    //Boolean-muuttujat, joilla tutkitaan onko näkymä tietyssä tilassa
    boolean shipsBeingSet = true; //true = laivojenasettelutila, false = taistelutila. Näkymä alkaa oletuksena asettelutilassa
    boolean myTurn = false; //Oletuksena kummankaan pelaajan vuoro ei ole vielä. Pelaaja voi ampua vain kun on hänen vuoronsa

    Ship battleship, cruiser, destroyer;

    private String selectedOrientation; //Aktiviteetista saatava laivan asento
    private int selectedShipType; //Aktiviteetista saatavat

    public ShipView(Context context) {
        super(context);
    }

    public ShipView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setSelectedOrientation(String selectedOrientation) {
        this.selectedOrientation = selectedOrientation;
    }

    public void setSelectedShipType(int selectedShipType) {
        this.selectedShipType = selectedShipType;
    }

    //Metodia kutsutaan kun näkymä piirretään
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        gridPaint.setStyle(Paint.Style.STROKE); //Piirtää oletuksena valkoisia ruutuja mustalla reunuksella
        shipPaint.setStyle(Paint.Style.FILL_AND_STROKE); //Piirtää oletuksena täysin mustia ruutuja (tarkemmin mustia ruutuja mustalla reunuksella)
        hitPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        missPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        targetPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        hitPaint.setColor(Color.RED);
        missPaint.setColor(Color.LTGRAY);
        targetPaint.setColor(Color.GREEN);

        // Legacy-versio ruudukon piirtämiseen tarvittavasta tilan laskennasta.
        // Tähän versioon voidaan joutua palaamaan jos ruudukot piirtyvätkin väärin joillakin laitteilla.
        /*
        wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        display = wm.getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        //HUOM! Näkymä on horisontaalinen, mutta sizeen tallennetaan sivujen pituuden kuin puhelin olisi pystyasennossa
        screenWidth = size.y;
        System.out.println("Screen Width: " + screenWidth);
        screenHeight = size.x;
        System.out.println("Screen Height: " + screenHeight);
        metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);

        availableWidth = (float) (screenWidth - (2.5 * layoutWidth));
        gridSideLength = availableWidth / 2; // Ruudukkoja on kaksi eli vapaa leveys tulee jakaa niiden kesken
        System.out.println("Grid side length: " + gridSideLength);
        cellSideLength = gridSideLength / CELLS;
        System.out.println("Cell side length: " + cellSideLength);
        */

        viewWidth = this.getWidth();
        viewHeight = this.getHeight();

        marginWidth = viewWidth * 0.05f; //marginaali on 5% koko näkymän leveydestä.
        marginHeight = viewHeight * 0.10f; //marginaali 10% näkymän korkeudesta

        availableWidth = viewWidth - 3 * marginWidth; // <-[]<->[]-> ruudukot näyttävät suunnilleen tältä, marginaaleja siis 3
        availableHeight = viewHeight - 2 * marginHeight; // marginaalit tulevat ruudukkojen ylä- ja alapuolelle

        gridSideLength = availableWidth / 2;
        cellSideLength = gridSideLength / 10;

        myGrid = new int[CELLS][CELLS];
        enemyGrid = new int[CELLS][CELLS];

    }

    @Override
    public void run() {
        postInvalidate(); //Aja tämä viimeiseksi, jotta näkymä tiedetään piirtää uudestaan
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //Oma ruudukko
        for(int i = 0; i < CELLS; i++) {
            float yPlacement = marginHeight + ((i + 1) * (cellSideLength));
            for(int j = 0; j < CELLS; j++){
                float xPlacement = ((j + 1) * (cellSideLength));
                if(myGrid[j][i] == SHIP){
                    canvas.drawRect(xPlacement, yPlacement, xPlacement + cellSideLength, yPlacement + cellSideLength, shipPaint);
                }else{
                    canvas.drawRect(xPlacement, yPlacement, xPlacement + cellSideLength, yPlacement + cellSideLength, gridPaint);
                }
            }
        }
        //vihollisen ruudukko
        for(int i = 0; i < CELLS; i++) {
            float yPlacement = marginHeight + ((i + 1) * (cellSideLength));
            for (int j = 0; j < CELLS; j++) {
                float xPlacement = (gridSideLength + marginWidth * 2) + (j + 1) * (cellSideLength);
                if(j == targetingX && i == targetingY){
                    canvas.drawRect(xPlacement, yPlacement, xPlacement + cellSideLength, yPlacement + cellSideLength, targetPaint);
                }else{
                    canvas.drawRect(xPlacement, yPlacement, xPlacement + cellSideLength, yPlacement + cellSideLength, gridPaint);
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        /*
            Jos ollaan laivojenasettelutilassa, haetaan painallustapahtuman koordinaatit
            ja tunnettujen mittojen avulla lasketaan mihin ruutuun laivaa yritetään asettaa
         */
        if(shipsBeingSet){
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                if(event.getX() < (marginWidth + gridSideLength)) {
                    try {
                        int x = (int) ((event.getX() - marginWidth) / cellSideLength);
                        int y = (int) ((event.getY() - marginHeight) / cellSideLength) - 1;
                        myGrid[x][y] = SHIP;
                        Log.d(TAG, "You tried to create a ship with index  " + selectedShipType + " with " + selectedOrientation + " orientation.");
                        switch(selectedShipType){
                            case 0:
                                if(battleship == null){
                                    //battleship = new Ship(orientation, selectedShipType, x, y);
                                }else{
                                    Log.d(TAG, "Ship already created");
                                }
                                break;
                            case 1:
                                if(cruiser == null){
                                    //cruiser = new Ship(orientation, selectedShipType, x, y);
                                }else{
                                    Log.d(TAG, "Ship already created");
                                }
                                break;
                            case 2:
                                if(destroyer == null){
                                    //destroyer = new Ship(orientation, selectedShipType, x, y);
                                }else{
                                    Log.d(TAG, "Ship already created");
                                }
                                break;
                        }
                        invalidate();
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        return true;
                    }
                }
            }
        }
        /*
        TODO: Tähän ehtolause, jonka avulla rajoitetaan tähtäinasettelu vihollisruudukkoon vain,
        jos on pelaajan vuoro eikä olla laivojenasettelutilassa.
         */
        // Tähtäimen asettaminen vihollisruudukkoon

        if(event.getAction() == MotionEvent.ACTION_DOWN){
            Log.d(TAG,event.getX() + "," + event.getY());
            if(event.getX() > (marginWidth * 2 + gridSideLength)) {
                try {
                    targetingX = (int) ((event.getX() - marginWidth * 2 - gridSideLength) / cellSideLength) - 1;
                    targetingY = (int) ((event.getY() - marginHeight) / cellSideLength) - 1;
                    Log.d(TAG, targetingX + "," + targetingY);
                    invalidate();
                } catch (ArrayIndexOutOfBoundsException ex) {
                    return true;
                }
            }
        }
        return true;
    }
}
