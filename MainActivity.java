package com.svkrdj.myapplication;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.TextView;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import android.view.View;
import java.util.Set;
import java.util.UUID;

import static android.R.id.list;

public class MainActivity extends AppCompatActivity {


    String strRecieved;
    private BluetoothAdapter myBluetooth = null;
    private Set<BluetoothDevice> pairedDevices;
    String address;
    TextView tv;
    private ProgressDialog progress;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        if(myBluetooth == null)
        {
            //Show a mensag. that the device has no bluetooth adapter
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();

            //finish apk
            finish();
        }
        else if(!myBluetooth.isEnabled())
        {
            //Ask to the user turn the bluetooth on
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon,1);
        }
        pairedDevices = myBluetooth.getBondedDevices();

        if (pairedDevices.size()>0)
        {
            for(BluetoothDevice bt : pairedDevices)
            {
                address=bt.getAddress();
                String c="98:D3:32:30:84:62";
                if(c.equals(address)==true)
                {
                    new ConnectBT().execute();
                    break;//Get the device's name and the address
                }
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }
    }

    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
        new Recieve().start();
    }

    private class Recieve extends Thread {
        private final InputStream mmInStream;

        public Recieve() {
            InputStream tmpIn = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = btSocket.getInputStream();
                Toast.makeText(getApplicationContext(), "channel created", Toast.LENGTH_LONG).show();

            } catch (IOException e) {

            }

            mmInStream = tmpIn;
        }

        public void run() {

            byte[] buffer = new byte[1024];
            int bytes;
           int c=1;
           while(true) {

               try {


                   // Read from the InputStream
                   bytes = mmInStream.read(buffer);
                   strRecieved = new String(buffer, 0, bytes);
                   if(strRecieved.equals("1"))
                           {

                               Intent i=new Intent(MainActivity.this, ui.class);
                               startActivity(i);
                                c=0;

                           }

                   // Send the obtained bytes to the UI Activity
                   // h.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();
               } catch (IOException e) {

               }
               if(c==0)
               {

                   break;
               }

           }
           try{
               btSocket.close();
           }
           catch (IOException e)
           {

           }


        }
    }
    public void Send(View v) {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write("1".toString().getBytes());
            } catch (IOException e) {

                msg("Error");
            }
        }
    }



    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(MainActivity.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Connection Failed.");
                finish();
            }
            else
            {
                msg("Connected.");
                MainActivity.this.moveTaskToBack(true);
             }
            progress.dismiss();
        }
    }
}
