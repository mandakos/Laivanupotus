package ryhma3.laivanupotus;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.ToggleButton;

public class ShipSettingActivity extends AppCompatActivity {

    final String TAG = "ShipSettingActivity";

    ShipView shipView;
    ToggleButton toggleBtn;
    RadioGroup shipsRadioGroup;
    Button readyButton;
    Button fireButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ship_setting);
        shipView = findViewById(R.id.ship_view);
        shipView.setSelectedOrientation("Horizontal"); // Myös nappi on alussa horisontaalisessa tilassa
        toggleBtn = findViewById(R.id.toggleButton);
        toggleBtn.setText(getString(R.string.horizontal));
        toggleBtn.setTextOn(getString(R.string.horizontal));
        toggleBtn.setTextOff(getString(R.string.vertical));

        toggleBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //Koodi ajetaan napin tilanmuutoksen tapahtuessa
                if(isChecked){
                    shipView.setSelectedOrientation("Horizontal");
                }else{
                    shipView.setSelectedOrientation("Vertical");
                }
            }
        });


        shipsRadioGroup = findViewById(R.id.radioShips);
        shipsRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                //Nakataan shipviewille valittu laivan tyyppi
                RadioButton radioButton = findViewById(checkedId);
                int radioIndex = group.indexOfChild(radioButton);
                shipView.setSelectedShipType(radioIndex);
            }
        });


        readyButton = findViewById(R.id.readyButton);
        readyButton.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {
                //TODO: Varsinainen pelin alottaminen, kommentoi toasti pois kun peli on valmis.
                // shipView.startGame();
                // fireButton.setEnabled(true);
                Toast.makeText(getApplicationContext(), "WAITING IMPLEMENTATION", Toast.LENGTH_SHORT).show();

            }
        });

        fireButton = findViewById(R.id.fireButton);
        //Kommentoi tämä pois, alussa ei saa olla mahdollista ampua
        //fireButton.setEnabled(false);
        fireButton.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {
                shipView.fire();
            }
        });
    }
}
