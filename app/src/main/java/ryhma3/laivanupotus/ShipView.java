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
    boolean threadRunning = false; //Käytetään pelisäikeen turvalliseen käynnistämiseen

    Ship battleship, cruiser, destroyer;

    private String selectedOrientation; //Aktiviteetista saatava laivan asento
    private int selectedShipType; //Aktiviteetista saatavat laivatyypit

    Thread gameThread;

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

    //Pelisäikeen turvallinen käynnistäminen
    public void startGame(){
        if(gameThread == null){
            gameThread = new Thread(this);
            threadRunning = true;
            gameThread.start();
        }
    }

    //Pelisäikeen turvallinen lopettaminen
    public void endGame(){
        if(gameThread != null){
            gameThread.interrupt();
            threadRunning = false;
            gameThread = null;
            //TODO: Mahdollinen ruutu revanssia / pelin tulosten tarkastelua varten
        }
    }


    @Override
    public void run() {
        while(threadRunning){
            //TODO: Pelilogiikka tänne!
            postInvalidate(); //Aja tämä viimeiseksi, jotta näkymä tiedetään piirtää uudestaan
        }
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

    /*
        Laivojen luonti on erityisen bugiherkkää aluetta. Sain osan bugeista liiskattua, mutta jokaisessa
        metodissa on varmuuden vuoksi & ajan säästämiseksi mukana likainen try-catch-mekanismi,
        jolla laiva nullataan tyypillisen bugin sattuessa.
        Ensin jokaisen laivan sisäiset koordinaatit asetetaan luomalla olio. Tämän jälkeen tarkastellaan
        onko mahdollisia törmäyksiä muiden laivojen kanssa. Jos törmäys havaitaan poistetaan laiva, jonka
        kanssa törmäys tapahtui, ruudukosta. Vasta tämän jälkeen ruudukkoon asetetaan arvot, joissa asetettu
        laiva sijaitsee. Näin ruudukkoon ei jää tyhjiä ruutuja, sinne missä törmäys tapahtui. Tulevaisuutta varten:
        Tämä luontilogiikka olisi kannattanut ehkä hoitaa Factory-suunnittelufilosofian mukaisesti.
    */

    public void createBattleship(int centerX, int centerY) {
        try{
            /*
            Laivan luominen, klikkaustapahtuman koordinaatit ovat laivan keskipiste.
            Jos keskipiste on liian lähellä reunoja ei laivaa piirretä.
             */
            if(selectedOrientation == "Horizontal"){
                if(centerX <= 1 || centerX >= 8){
                    battleship = null;
                }else{
                    battleship = new Ship(selectedOrientation, selectedShipType, centerX, centerY);
                }
            }else if(selectedOrientation == "Vertical"){
                if(centerY <= 1 || centerY >= 8){
                    battleship = null;
                }else{
                    battleship = new Ship(selectedOrientation, selectedShipType, centerX, centerY);
                }
            }
            /*
            Törmäysten tarkastelu, kts. compareShipPositions()-metodin kommentti. Törmäyksen aiheuttava
            laiva poistetaan ruudukosta.
             */
            if(battleship != null){
                if(cruiser != null){
                    if(compareShipPositions(battleship.shipCoordinatesX, cruiser.shipCoordinatesX, battleship.shipCoordinatesY, cruiser.shipCoordinatesY)){
                        clearShipFromGrid(cruiser);
                        cruiser = null;
                    }
                }
                if(destroyer != null){
                    if(compareShipPositions(battleship.shipCoordinatesX, destroyer.shipCoordinatesX, battleship.shipCoordinatesY, destroyer.shipCoordinatesY)){
                        clearShipFromGrid(destroyer);
                        destroyer = null;
                    }
                }
            }
            //Ruudukkoon piirtäminen
            if(battleship != null){
                if(selectedOrientation == "Horizontal"){
                    for(int i = 0; i < battleship.getSize(); i++){
                        myGrid[battleship.shipCoordinatesX[i]][centerY] = SHIP;
                    }
                }else if(selectedOrientation == "Vertical"){
                    for(int i = 0; i < battleship.getSize(); i++) {
                        myGrid[centerX][battleship.shipCoordinatesY[i]] = SHIP;
                    }
                }
            }
        }catch(ArrayIndexOutOfBoundsException ex) {
            battleship = null;
        }
    }

    public void createCruiser(int centerX, int centerY){
        try{
            if(selectedOrientation == "Horizontal"){
                if(centerX <= 0 || centerX >= 9){
                    cruiser = null;
                }else{
                    cruiser = new Ship(selectedOrientation, selectedShipType, centerX, centerY);
                }
            }else if(selectedOrientation == "Vertical"){
                if(centerY <= 0 || centerY > 9){
                    cruiser = null;
                }else{
                    cruiser = new Ship(selectedOrientation, selectedShipType, centerX, centerY);
                }
            }
            if(cruiser != null){
                if(battleship != null){
                    if(compareShipPositions(cruiser.shipCoordinatesX, battleship.shipCoordinatesX, cruiser.shipCoordinatesY, battleship.shipCoordinatesY)){
                        clearShipFromGrid(battleship);
                        battleship = null;
                    }
                }
                if(destroyer != null){
                    if(compareShipPositions(cruiser.shipCoordinatesX, destroyer.shipCoordinatesX, cruiser.shipCoordinatesY, destroyer.shipCoordinatesY)){
                        clearShipFromGrid(destroyer);
                        destroyer = null;
                    }
                }
            }
            if(cruiser != null){
                if(selectedOrientation == "Horizontal"){
                    for(int i = 0; i < cruiser.getSize(); i++){
                        myGrid[cruiser.shipCoordinatesX[i]][centerY] = SHIP;
                    }
                }else if(selectedOrientation == "Vertical"){
                    for(int i = 0; i < cruiser.getSize(); i++) {
                        myGrid[centerX][cruiser.shipCoordinatesY[i]] = SHIP;
                    }
                }
            }
        }catch(ArrayIndexOutOfBoundsException ex){
            cruiser = null;
        }
    }

    public void createDestroyer(int centerX, int centerY){
        try{
            destroyer = new Ship(selectedOrientation, selectedShipType, centerX, centerY);
            if(destroyer != null){
                if(battleship != null){
                    if(compareShipPositions(destroyer.shipCoordinatesX, battleship.shipCoordinatesX, destroyer.shipCoordinatesY, battleship.shipCoordinatesY)){
                        clearShipFromGrid(battleship);
                        battleship = null;
                    }
                }
                if(cruiser != null){
                    if(compareShipPositions(destroyer.shipCoordinatesX, cruiser.shipCoordinatesX, destroyer.shipCoordinatesY, cruiser.shipCoordinatesY)){
                        clearShipFromGrid(cruiser);
                        cruiser = null;
                    }
                }
            }
            myGrid[destroyer.getIndexOfX(0)][destroyer.getIndexOfY(0)] = SHIP;
        }catch(ArrayIndexOutOfBoundsException ex){
            destroyer = null;
        }
    }

    public boolean compareShipPositions(int ship1X[], int ship2X[], int ship1Y[], int ship2Y[]){
        /*
            Tarkastellaan ensin löytyykö laivoista samoja X-koordinaattipisteitä. Jos pisteitä
            löytyy, tarkastellaan sen pisteen Y-koordinaattia mahdollisen törmäyksen aiheuttavan
            laivan Y-koordinaatteihin. Jos löytyy sama Y-koordinaatti, törmäys tapahtuu.
         */
        for(int i = 0; i < ship1X.length; i++){
            for(int comparedX : ship2X){
                if(ship1X[i] == comparedX){
                    for(int j = 0; j < ship1Y.length; j++){
                        for(int comparedY : ship2Y){
                            if(ship1Y[j] == comparedY){
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    //Asetetaan törmäyksen sattuessa ruudukosta poistetun laivan koordinaatit tyhjäksi.
    public void clearShipFromGrid(Ship ship){
        if(ship.getOrientation() == "HORIZONTAL"){
            for(int i = 0; i < ship.getSize(); i++){
                myGrid[ship.getIndexOfX(i)][ship.getIndexOfY(0)] = NOSHIP;
            }
        }else{
            for(int i = 0; i < ship.getSize(); i++){
                myGrid[ship.getIndexOfX(0)][ship.getIndexOfY(i)] = NOSHIP;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        /*
            Jos ollaan laivojenasettelutilassa, haetaan painallustapahtuman koordinaatit
            ja tunnettujen mittojen avulla lasketaan mihin ruutuun laivaa yritetään asettaa.
         */
        if(shipsBeingSet){
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                if(event.getX() < (marginWidth + gridSideLength)) {
                    int x = (int) ((event.getX() - marginWidth) / cellSideLength);
                    int y = (int) ((event.getY() - marginHeight) / cellSideLength) - 1;
                    //System.out.println(x + "," + y);
                    if((x >= 0 && x <= 9) && (y >= 0 && y <= 9)){
                            switch (selectedShipType) {
                                case 0:
                                    if (battleship != null) {
                                        clearShipFromGrid(battleship);
                                        battleship = null;
                                        createBattleship(x, y);
                                    }else{
                                        createBattleship(x, y);
                                    }
                                    break;
                                case 1:
                                    if (cruiser != null) {
                                        clearShipFromGrid(cruiser);
                                        cruiser = null;
                                        createCruiser(x, y);
                                    }else{
                                        createCruiser(x, y);
                                    }
                                    break;
                                case 2:
                                    if (destroyer != null) {
                                        clearShipFromGrid(destroyer);
                                        destroyer = null;
                                        createDestroyer(x, y);
                                    }else{
                                        createDestroyer(x, y);
                                    }
                                    break;
                            }
                            invalidate();
                        }
                    }
                }
            }
        /**
        TODO: Tähän ehtolause, jonka avulla rajoitetaan tähtäinasettelu vihollisruudukkoon vain,
        jos on pelaajan vuoro eikä olla laivojenasettelutilassa.
         **/
        // Tähtäimen asettaminen vihollisruudukkoon

        if(event.getAction() == MotionEvent.ACTION_DOWN){
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
