package ryhma3.laivanupotus;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ToggleButton;

public class ShipSettingActivity extends AppCompatActivity {

    /*
    TODO: Kaikki ohjelmalogiikka ":D"
    Note to self:
    toteuta tässä käyttöliittymäkomponenttien logiikka ja mahdollinen BT-toiminnallisuus,
    mutta kokeile piirtää ruudukko Viewin avulla
     */

    ToggleButton toggleBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ship_setting);

        toggleBtn = (ToggleButton) findViewById(R.id.toggleButton);
        toggleBtn.setText(getString(R.string.horizontal));
        toggleBtn.setTextOn(getString(R.string.horizontal));
        toggleBtn.setTextOff(getString(R.string.vertical));
    }
}
