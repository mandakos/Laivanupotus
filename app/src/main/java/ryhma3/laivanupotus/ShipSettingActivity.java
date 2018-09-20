package ryhma3.laivanupotus;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ToggleButton;

public class ShipSettingActivity extends AppCompatActivity {

    final String TAG = "ShipSettingActivity";

    /*
    Note to self:
    toteuta tässä käyttöliittymäkomponenttien logiikka ja mahdollinen BT-toiminnallisuus,
    mutta kokeile piirtää ruudukko Viewin avulla
     */

    ShipView shipView;
    ToggleButton toggleBtn;
    RadioGroup ships;
    Button readyButton;
    Button fireButton;
    Ship battleship, cruiser, destroyer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ship_setting);
        //TODO: Viewi XML:n
        //shipView = findViewById(R.id.)

        toggleBtn = findViewById(R.id.toggleButton);
        toggleBtn.setText(getString(R.string.horizontal));
        toggleBtn.setTextOn(getString(R.string.horizontal));
        toggleBtn.setTextOff(getString(R.string.vertical));

        ships = findViewById(R.id.radioShips);

        readyButton = findViewById(R.id.readyButton);
        readyButton.setOnClickListener(new Button.OnClickListener(){
            //Tämä toimii tässä vaiheessa laivojen asettelun debuggerina
            //TODO: Varsinainen pelin alottaminen
            @Override
            public void onClick(View v) {
                //Siirrä tämä logiikka Viewiin
                int x = 0, y = 0;
                String orientation = toggleBtn.getText().toString();
                int radioButtonID = ships.getCheckedRadioButtonId();
                RadioButton radioButton = findViewById(radioButtonID);
                int radioIndex = ships.indexOfChild(radioButton);
                Log.d(TAG, "You tried to create a " + radioButton.getText() + " with " + orientation + " orientation.");
                Log.d(TAG, "radioButtonIndex is " + radioIndex);
                switch(radioIndex){
                    case 0:
                        if(battleship == null){
                            battleship = new Ship(orientation, radioIndex, x, y);
                        }else{
                            Log.d(TAG, "Ship already created");
                        }
                        break;
                    case 1:
                        if(cruiser == null){
                            cruiser = new Ship(orientation, radioIndex, x, y);
                        }else{
                            Log.d(TAG, "Ship already created");
                        }
                        break;
                    case 2:
                        if(destroyer == null){
                            destroyer = new Ship(orientation, radioIndex, x, y);
                        }else{
                            Log.d(TAG, "Ship already created");
                        }
                        break;
                }
            }
        });
        fireButton = findViewById(R.id.fireButton);
        //fireButton.setEnabled(false);
        fireButton.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {
                Log.d(TAG, "Fire clicked!");
                //TODO: Ampumisen ohjelmalogiikka
            }
        });
    }
}
