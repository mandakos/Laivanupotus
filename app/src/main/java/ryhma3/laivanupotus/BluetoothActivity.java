package ryhma3.laivanupotus;
import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;
import java.lang.String;

import static ryhma3.laivanupotus.MainActivity.REQUEST_ENABLE_BT;

public class BluetoothActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{
    private static final String TAG = "BluetoothActivity";
    private static final UUID UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    //static BluetoothActivity instance;

    BluetoothAdapter mBluetoothAdapter;
    BluetoothConnectionService mBluetoothConnection;
    BluetoothDevice mBTDevice;

    Button btnEnableDisable_Discoverable;
    Button btnSend;
    Button btnStartConnection;
    EditText etSend;
    ListView lvNewDevices;
    TextView incomingMessages;
    static TextView textList;

    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public DeviceListAdapter mDeviceListAdapter;
    StringBuilder messages;
    private static final int Finished_Activity = 1;


    /*
        BroadcastReceiver Bluetoothin pÃ¤Ã¤lle/pois laittamiselle
     */

    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)){
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);
                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "OnReceive: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "OnReceive: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "OnReceive: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "OnReceive: STATE TURNING ON");
                        break;
                }
            }
        }
    };

    //BroadcastReceiver bluetooth-tilojen vaihdoksille

    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action.equals(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED)){
                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, mBluetoothAdapter.ERROR);
                switch(mode){
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discorability Enabled");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Able to receive connections.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "mBroadcastReceiver2: Connecting...");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "mBroadcastReceiver2: Connected.");
                        break;
                }
            }
        }
    };

    /*
        TÃ¤mÃ¤ BroadcastReceiver etsii laitteet, joita ei ole vielÃ¤ paritettu
     */

    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND");

            if(action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);
                Log.d(TAG, "onReceive: " + device.getName() + " : " + device.getAddress());
                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                lvNewDevices.setAdapter(mDeviceListAdapter);
            }
        }
    };

    /*
        BroadcastReceiver, joka havaitsee Bluetooth-parituksen tilanvaihdot
     */

    private BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                //Laite on jo paritettu
                if(mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    Log.d(TAG, "BroadcastReceiver4 : BOND_BONDED");
                    mBTDevice = mDevice;
                    //textList.setText("Connected to " + mBTDevice.getName());
                }
                //Paritus kÃ¤ynnissÃ¤
                if(mDevice.getBondState() == BluetoothDevice.BOND_BONDING){
                    Log.d(TAG, "BroadcastReceiver4 : BOND_BONDING");
                    //textList.setText("Connecting to " + mBTDevice.getName());
                }
                //Paritus katkennut
                if(mDevice.getBondState() == BluetoothDevice.BOND_NONE){
                    Log.d(TAG, "BroadcastReceiver4 : BOND_NONE");
                    //textList.setText("Connection failed");
                }
            }
        }
    };

    // TÃ¤Ã¤llÃ¤ kÃ¤sitellÃ¤Ã¤n saapuvia viestejÃ¤
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("MESSAGE");
            /*messages.append(text + "\n");
            incomingMessages.setText(messages);*/
            incomingMessages.setText(text);
        }
    };


    private void checkBluetooth() {
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            Log.d(TAG, "Bluetooth on");
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); //set orientation to landscape
        //instance = this;
        //Button btnONOFF = findViewById(R.id.btnONOFF);
        btnEnableDisable_Discoverable = findViewById(R.id.btnDiscoverable_on_off);
        btnStartConnection = findViewById(R.id.btnStartConnection);
        btnStartConnection.setEnabled(false);

        textList = findViewById(R.id.textList);
        lvNewDevices = findViewById(R.id.lvNewDevices);

        btnSend = findViewById(R.id.btnSend);
        etSend = findViewById(R.id.editText);
        incomingMessages = findViewById(R.id.incomingMessage);

        mBTDevices = new ArrayList<>();
        messages = new StringBuilder();

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("incomingMessage"));
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("Status"));

        //Kutsutaan kun paritus tapahtuu
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4, filter);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        lvNewDevices.setOnItemClickListener(BluetoothActivity.this);


        /*btnONOFF.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: enabling/disabling bluetooth");
                enableDisableBT();
            }
        });*/

        // START CONNECTION button
        btnStartConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "btnStartConnection Clicked");
                if (mBluetoothConnection != null) {
                    startNextActivity();
                }
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBluetoothConnection != null){
                    byte[] bytes = etSend.getText().toString().getBytes(Charset.defaultCharset());
                    mBluetoothConnection.write(bytes);
                    etSend.setText("");
                }
            }
        });
    }

    /*
    public static BluetoothActivity getInstance() {
        return instance;
    }*/


    //Metodit yhteyden luomiselle.
    //Sovellus kaatuu jos paritusta ei ole tehty ensin

    public void startConnection(){
        startBTConnection(mBTDevice, UUID_INSECURE);
    }

    public void startBTConnection(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startBTConnection : Initializing RFCOM Bluetooth Connection");

        mBluetoothConnection.startClient(device, uuid);
    }

    @Override
    protected void onDestroy(){
        Log.d(TAG, "onDestroy called.");
        super.onDestroy();
        //unregisterReceiver(mBroadcastReceiver1);
        unregisterReceiver(mBroadcastReceiver2);
        unregisterReceiver(mBroadcastReceiver3);
        unregisterReceiver(mBroadcastReceiver4);
    }

    /*public void enableDisableBT() {
        if(mBluetoothAdapter == null){
            Log.d(TAG, "enableDisableBT: Does not have BT capabilities");
        }
        if(!mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "enabling BT");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);

        }
        if(mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "disabling BT");
            mBluetoothAdapter.disable();
            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
    }*/

    public void btnEnableDisable_Discoverable(View view) {
        Log.d(TAG, "btnEnableDisable_Discoverable: Making device discoverable for 300 seconds");

        textList.setText("Bluetooth visible");
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        IntentFilter intentFilter = new IntentFilter(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcastReceiver2, intentFilter);

        //textList.setText("Waiting for opponent...");
    }

    public void btnDiscover(View view) {
        Log.d(TAG, "btnDiscover: Looking for unpaired devices.");
        textList.setText("Looking for bluetooth devices...");

        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "btnDiscover: canceling discovery");

            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
        if(!mBluetoothAdapter.isDiscovering()){
            checkBTPermissions();

            // Remove all previous devices from list
            if (mBTDevices.size() > 0) {
                mDeviceListAdapter.clear();
                mDeviceListAdapter.notifyDataSetChanged();
            }

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
    }

    /*
        Metodia tarvitaan, jos kohdelaitteen SDK > 23.
        NÃ¤ille laitteille ei riitÃ¤, jos laittaa luvat pelkÃ¤stÃ¤Ã¤n manifestiin. Metodi ajetaan ainoastaan versioilla > LOLLIPOP
     */

    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            if(Build.VERSION.SDK_INT > 22) {
                int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
                permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");

                if (permissionCheck != 0) {
                    this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
                } else {
                    Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
                }
            }

        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //Jos Bluetooth-laitteiden listalla klikataan elementtiÃ¤, perutaan laitteiden etsintÃ¤ ensin, sillÃ¤ se syÃ¶ paljon muistia
        mBluetoothAdapter.cancelDiscovery();

        Log.d(TAG, "onItemClick: You clicked a device.");
        String deviceName = mBTDevices.get(position).getName();
        String deviceAddress = mBTDevices.get(position).getAddress();

        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);
        textList.setText("Chosen device:" + deviceName);

        //paritus
        //createBond()-metodi vaatii suuremman Android SDK version entÃ¤ JELLY_BEAN_MR2
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
            //Tallennetaan listalta valittu BT-laite laitteeksi, jonka kanssa kommunikoidaan ja avataan kommunikointiyhteys
            Log.d(TAG, "Trying to pair with " + deviceName);
            mBTDevices.get(position).createBond();
            mBTDevice = mBTDevices.get(position);
            mBluetoothConnection = new BluetoothConnectionService(BluetoothActivity.this);
            startConnection();
        }
    }
    // Kutsutaan BluetoothConnectionServicen ConnectedThreadissa
    public void startNextActivity(){

            Log.d(TAG, "startNextActivity ");
            // Käynnistetään uusi activity
            Log.d(TAG, "Start game ");
            Intent intent = new Intent(BluetoothActivity.this, ShipSettingActivity.class);
            startActivityForResult(intent, Finished_Activity);
    }


    @Override
    public void onResume() {

        // onResumessa käynnistetään AcceptThread eli serveri
        super.onResume();

        // Tarkista Bluetooth
        if (!mBluetoothAdapter.isEnabled()) {
            checkBluetooth();
            if (mBluetoothConnection == null && mBluetoothAdapter.isEnabled()) {
                Log.d(TAG, "Start Acceptthread onResume");
                BluetoothConnectionService mBluetoothConnection = new BluetoothConnectionService(this);
                mBluetoothConnection.start();
            } else {
                Log.d(TAG, "onResume .start() failed");
            }
        }
    }


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("Status");
            Bundle b = intent.getBundleExtra("Connect");
            Boolean state = b.getBoolean("Connect");

            if(state == true){
                btnStartConnection.setEnabled(true);
            }
            textList.setText(message);

        }
    };

}