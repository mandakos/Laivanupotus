package ryhma3.laivanupotus;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); //set orientation to landscape

        Button buttonNew = (Button)findViewById(R.id.button_new); // New game
        /*buttonNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ActivitynNimi.class);
                view.getContext().startActivity(intent);}
        });*/

        Button buttonJoin = (Button)findViewById(R.id.button_join); // Join game
        /*buttonJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ActivitynNimi.class);
                view.getContext().startActivity(intent);}
        });*/
    }


}
