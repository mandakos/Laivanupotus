package ryhma3.laivanupotus;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.bluetooth.BluetoothAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    BluetoothAdapter mBluetoothAdapter;
    //int REQUEST_ENABLE_BT = 1;
    public static final int REQUEST_ENABLE_BT=999;


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); //set orientation to landscape
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Button buttonNew = (Button)findViewById(R.id.button_new);
        buttonNew.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
              if(!mBluetoothAdapter.isEnabled())
              {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }}
        });

        Button buttonJoin = (Button)findViewById(R.id.button_join); // Join game
        /*buttonJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ActivitynNimi.class);
                view.getContext().startActivity(intent);}
        });*/



}}
