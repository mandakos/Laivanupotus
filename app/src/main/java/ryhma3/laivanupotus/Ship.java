package ryhma3.laivanupotus;


public class Ship {

    public enum shipType{
        BATTLESHIP,
        CRUISER,
        DESTROYER
    }
    public enum shipOrientation{
        HORIZONTAL,
        VERTICAL
    }

    public shipType sType;
    public shipOrientation sOrientation;

    public int hitPoints; //Tämä on todellisuudessa sama kuin laivan koko ruudukolla
    public int size; //Tämä auttaa koordinaattitaulukoiden täyttämisessä
    public int centerX;
    public int centerY;
    public int[] shipCoordinatesX;
    public int[] shipCoordinatesY;

    public Ship(String orientation, int st, int x, int y){
        switch(orientation){
            case "Horizontal":
                sOrientation = shipOrientation.HORIZONTAL;
                break;
            case "Vertical":
                sOrientation = shipOrientation.VERTICAL;
                break;
        }
        centerX = x;
        centerY = y;
        switch(st){
            case 0:
                sType = shipType.BATTLESHIP;
                size = 2;
                hitPoints = 5;
                break;
            case 1:
                sType = shipType.CRUISER;
                size = 1;
                hitPoints = 3;
                break;
            case 2:
                //Tämän tyyppinen laiva on yhden ruudun kokoinen eli sen koordinaatit voidaan täyttää jo konstruktorissa
                sType = shipType.DESTROYER;
                size = 0;
                hitPoints = 1;
                shipCoordinatesX = new int[1];
                shipCoordinatesX[0] = x;
                shipCoordinatesY = new int[1];
                shipCoordinatesY[0] = y;
                break;
        }

        populateCoordinates();
    }

    private void populateCoordinates(){
        if(sType == shipType.BATTLESHIP || sType == shipType.CRUISER){
            int tmp = -size; //koon käänteisarvo
            if(sOrientation == shipOrientation.HORIZONTAL){
                shipCoordinatesY = new int[1];
                shipCoordinatesY[0] = centerY;
                shipCoordinatesX = new int[hitPoints];
                for(int i = 0; i < hitPoints; i++){
                    shipCoordinatesX[i] = centerX + tmp;
                    tmp++;
                }
            }else if(sOrientation == shipOrientation.VERTICAL){
                shipCoordinatesX = new int[1];
                shipCoordinatesX[0] = centerX;
                shipCoordinatesY = new int[hitPoints];
                for(int i = 0; i < hitPoints; i++){
                    shipCoordinatesY[i] = centerY + tmp;
                    tmp++;
                }
            }
        }
    }

    public String getOrientation(){
        return sOrientation.toString();
    }

    public int getSize(){
        return hitPoints;
    }

    public int getIndexOfX(int i){
        return shipCoordinatesX[i];
    }

    public int getIndexOfY(int i){
        return shipCoordinatesY[i];
    }
}
