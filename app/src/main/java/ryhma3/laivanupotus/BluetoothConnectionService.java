package ryhma3.laivanupotus;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

public class BluetoothConnectionService {
    private static final String TAG = "BluetoothConnServ";
    private static final String appName = "Laivanupotus";
    private static final UUID UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66"); //vielä vähän epäselvää täytyykö tähän vaihtaa jotakin?
    //private static final UUID UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothAdapter mBluetoothAdapter;
    Context mContext;

    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    private BluetoothDevice mDevice;
    private UUID deviceUUID;
    ProgressDialog mProgressDialog;

    public BluetoothConnectionService(Context c){
        mContext = c;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        start();
    }

    /*
    Tämä säie pyörii kun kuunnellaan tulevia yhteyksiä ja se toimii ikäänkuin serverinä.
    Säie pyörii kunnes yhteys hyväksytään tai perutaan.
     */

    private class AcceptThread extends Thread{
        //paikallinen server socket
        private final BluetoothServerSocket mServerSocket;

        public AcceptThread(){
            BluetoothServerSocket tmp = null;

            //tehdään uusi kuunteleva server socket
            try {
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, UUID_INSECURE);
                Log.d(TAG, "AcceptThread : Setting up Server using " + UUID_INSECURE);
            } catch (IOException e) {
                Log.e(TAG, "AcceptThread : IOException: " + e.getMessage());
            }

            mServerSocket = tmp;
        }

        public void run(){
            Log.d(TAG, "run: AcceptThread running.");
            BluetoothSocket socket = null;

            try {
                //Säie jää tänne hengailemaan niin kauan, että yhteys luodaan tai tulee poikkeus
                Log.d(TAG, "run : RFCOM server socket start...");
                socket = mServerSocket.accept();
                Log.d(TAG, "run : RFCOM server socket accepted connection");
            } catch (IOException e) {
                Log.e(TAG, "Run thread : IOException" + e.getMessage());
            }

            if(socket != null){
                Log.i(TAG, "Server connect");

                connected(socket, socket.getRemoteDevice());

                Log.i(TAG, "Server connected");

            }

            Log.i(TAG, "END mAcceptThread");
        }

        public void cancel(){
            Log.d(TAG, "cancel : Canceling AcceptThread");
            try{
                mServerSocket.close();
            }catch(IOException e){
                Log.e(TAG, "cancel: Close of AcceptThread ServerSocket failed. " + e.getMessage());
            }
        }
    }

    //Tämä säie pyörähtää kun yritetään yhdistää laitteeseen. Säie ajetaan alusta loppuun ja yhteys joko onnistuu tai ei

    private class ConnectThread extends Thread{
        private BluetoothSocket mSocket;
        boolean success = false;

        public ConnectThread(BluetoothDevice device, UUID uuid){
            Log.d(TAG, "ConnectThread: started");
            mDevice = device;
            deviceUUID = uuid;


            BluetoothSocket tmp = null;
            Log.d(TAG, "ConnectThread : Trying to create InsecureRFcommSocket using UUID: " + UUID_INSECURE + " DEVICE: " + mDevice);

            //Haetaan BT Socket yhteydelle laitteen kanssa
            try {
                Log.d(TAG, "ConnectThread : Trying to create InsecureRFcommSocket using UUID: " + UUID_INSECURE);
                tmp = mDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                Log.e(TAG, "ConnectThraed: Could not create InsecureRFcommSocket " + e.getMessage());
            }

            if (tmp != null){
                mSocket = tmp;
            }
            else {
                Log.d(TAG, "ConnectThread : Failed to set mSocket with UUID: " + UUID_INSECURE);
            }

        }

        public void run(){
            Log.i(TAG, "RUN mConnectThread " + mDevice.getName() + UUID_INSECURE);

            //Laitteiden etsintä perutaan aina kun yhteys luodaan muistinkäyttösyistä
            mBluetoothAdapter.cancelDiscovery();

            try {
                mSocket = mDevice.createRfcommSocketToServiceRecord(UUID_INSECURE);
            } catch (Exception e) {Log.e("","Error creating socket");}

            try {
                mSocket.connect();
                Log.e("","Connected");
            } catch (IOException e) {
                Log.e("",e.getMessage());
                try {
                    Log.e("","trying fallback...");

                    mSocket =(BluetoothSocket) mDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(mDevice,1);
                    mSocket.connect();

                    Log.e("","Connected");
                }
                catch (Exception e2) {
                    Log.e("", "Couldn't establish Bluetooth connection!");
                }
            }

            // Start the connected thread
            if(mSocket != null) {
                connected(mSocket, mDevice);
            }

        }

        public void cancel(){
            Log.d(TAG, "cancel : Closing Client Socket.");
            try{
                mSocket.close();
            }catch(IOException e){
                Log.e(TAG, "cancel: Close() of mSocket in ConnectThread failed. " + e.getMessage());
            }
        }
    }

    /*
        Suorittaa AcceptThreadin, jotta voidaan aloittaa sessio serveritilassa. Tulee kutsua aktiviteetin onResume()-metodissa
     */

    public synchronized void start(){
        Log.d(TAG, "start");

        // Perutaan säiekeet, jotka yrittävät luoda yhteyksiä parhaillaan
        if(mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if(mInsecureAcceptThread == null){
            mInsecureAcceptThread = new AcceptThread();
            mInsecureAcceptThread.start();
        }
    }

    public void startClient(BluetoothDevice device, UUID uuid){

        //Aloitetaan edistymisdialogi
        mProgressDialog = ProgressDialog.show(mContext, "Connecting Bluetooth", "Please wait...", true);

        mConnectThread = new ConnectThread(device, uuid);
        mConnectThread.start();
    }

    /*
        Säie, joka on vastuussa Bluetooth-yhteyden ylläpidosta sekä datan lähetyksestä ja vastaanotosta input- ja outputstreamien välityksellä
     */

    private class ConnectedThread extends Thread{
        private final BluetoothSocket mSocket;
        private final InputStream inStream;
        private final OutputStream outStream;

        public ConnectedThread(BluetoothSocket socket){
            Log.d(TAG, "ConnectedThread: Starting! ");
            mSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            //Edistysdialogi suljetaan kun yhteys on valmis
            try{
                if(mProgressDialog != null){
                    mProgressDialog.dismiss();
                }

                // Kun bluetooth yhteys on tehty, käsketään BluetoothActivityssä avaamaan seuraava activity
                //BluetoothActivity btActivity = BluetoothActivity.getInstance();
                //btActivity.startNextActivity();
                //Log.i(TAG, "Start ShipSettingActivity");


            }catch(NullPointerException e){
                Log.e(TAG, "Null Pointer exception in ConnectedThread" + e.getMessage());
            }

            Log.d(TAG, "ConnectedThread OK");


           try{
                tmpIn = mSocket.getInputStream();
                tmpOut = mSocket.getOutputStream();
            }catch(IOException e){
                Log.e(TAG, "ConnectedThread : Could not create input/output streams");
            }

            inStream = tmpIn;
            outStream = tmpOut;
        }

        public void run(){
            byte[] buffer = new byte[1024]; //puskuri streameille
            int bytes; //read()-metodin palauttamat tavut

            //Kuunnellaan input streamia niin kauan, että poikkeus tapahtuu
            while(true){
                try {
                    bytes = inStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "InputStream: " + incomingMessage);

                    //Tällä lähetetään kamaa
                    Intent incomingMessageIntent = new Intent("incomingMessage");
                    incomingMessageIntent.putExtra("MESSAGE", incomingMessage);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(incomingMessageIntent);

                } catch (IOException e) {
                    Log.e(TAG, "read: Error reading from inputstream. " + e.getMessage());
                    break;
                }
            }
        }

        //Kutsu aktiviteetista datan lähettämiseksi
        public void write(byte[] bytes){
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to outputstream: " + text);
            try{
                outStream.write(bytes);
            }catch(IOException e){
                Log.e(TAG, "write: Error writing to outputstream. " + e.getMessage());
            }
        }

        //Kutsu aktiviteetista yhteyden katkaisemiseksi
        public void cancel(){
            try{
                mSocket.close();
            }catch(IOException e){
                e.printStackTrace();
            }
        }

    }

    private void connected(BluetoothSocket mSocket, BluetoothDevice mDevice){
        Log.d(TAG, "connected : Starting");

        //Käynnistetään säie yhteyksien hallinnointia ja viestien välitystä varten
        mConnectedThread = new ConnectedThread(mSocket);
        mConnectedThread.start();


    }

    /*
        Kirjoittaa ConnectedThreadiin ei-synkronoidusti.
        out = tavut, joita kirjoitetaan.
        Katso ConnectedThreadin write()-metodi. Käytä siis tätä metodia, kun lähetetään dataa.
     */

    public void write(byte[] out){
        Log.d(TAG, "write: Write called");
        mConnectedThread.write(out);
    }


}


