package ryhma3.laivanupotus;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class BluetoothActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = "Bluetooth Activity";

    BluetoothAdapter mBluetoothAdapter;
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public DeviceListAdapter mDeviceListAdapter;
    ListView lvNewDevices;

    /*
        BroadcastReceiver Bluetoothin päälle/pois laittamiselle
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
        Tämä BroadcastReceiver etsii laitteet, joita ei ole vielä paritettu
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
                }
                //Paritus käynnissä
                if(mDevice.getBondState() == BluetoothDevice.BOND_BONDING){
                    Log.d(TAG, "BroadcastReceiver4 : BOND_BONDING");
                }
                //Paritus katkennut
                if(mDevice.getBondState() == BluetoothDevice.BOND_NONE){
                    Log.d(TAG, "BroadcastReceiver4 : BOND_NONE");
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt);
        Button btnONOFF = (Button) findViewById(R.id.btnONOFF);
        Button btnEnableDisable_Discoverable = (Button) findViewById(R.id.btnDiscoverable_on_off);
        lvNewDevices = (ListView) findViewById(R.id.lvNewDevices);
        mBTDevices = new ArrayList<>();

        //Kutsutaan kun paritus tapahtuu
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4, filter);


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        lvNewDevices.setOnItemClickListener(BluetoothActivity.this);
        btnONOFF.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: enabling/disabling bluetooth");
                enableDisableBT();
            }
        });
    }

    @Override
    protected void onDestroy(){
        Log.d(TAG, "onDestroy called.");
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver1);
        unregisterReceiver(mBroadcastReceiver2);
        unregisterReceiver(mBroadcastReceiver3);
        unregisterReceiver(mBroadcastReceiver4);
    }

    public void enableDisableBT() {
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
    }

    public void btnEnableDisable_Discoverable(View view) {
        Log.d(TAG, "btnEnableDisable_Discoverable: Making device discoverable for 300 seconds");

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        IntentFilter intentFilter = new IntentFilter(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcastReceiver2, intentFilter);
    }

    public void btnDiscover(View view) {
        Log.d(TAG, "btnDiscover: Looking for unpaired devices.");

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

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
    }

    /*
        Metodia tarvitaan, jos kohdelaitteen SDK > 23.
        Näille laitteille ei riitä, jos laittaa luvat pelkästään manifestiin. Metodi ajetaan ainoastaan versioilla > LOLLIPOP
     */

    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");

            if(permissionCheck != 0){
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
            }else{
                Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //Jos Bluetooth-laitteiden listalla klikataan elementtiä, perutaan laitteiden etsintä ensin, sillä se syö paljon muistia
        mBluetoothAdapter.cancelDiscovery();

        Log.d(TAG, "onItemClick: You clicked a device.");
        String deviceName = mBTDevices.get(position).getName();
        String deviceAddress = mBTDevices.get(position).getAddress();

        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

        //paritus
        //createBond()-metodi vaatii suuremman Android SDK version entä JELLY_BEAN_MR2
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
            Log.d(TAG, "Trying to pair with " + deviceName);
            mBTDevices.get(position).createBond();
        }
    }
}
