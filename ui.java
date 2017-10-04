package com.svkrdj.myapplication;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import android.os.CountDownTimer;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.util.concurrent.TimeUnit;

import static android.R.id.text1;
import static com.svkrdj.myapplication.R.id.time;
import static java.lang.System.in;

public class ui extends AppCompatActivity {

    private static final int REQUEST_LOCATION = 1;
    Button button;
    TextView textTimer;
    private ProgressDialog progress;
    LocationManager locationManager;
    String lattitude,longitude;
    BufferedReader reader=null;
    StringBuilder sb=null;
    String httppath="https://shivamkumarrajput56789.000webhostapp.com/p.php?";

    String serverResponse=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ui);
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            getLocation();
        }
        textTimer = (TextView) findViewById(R.id.textView2);
        long maxTimeInMilliseconds = 30000;// in your case

        startTimer(maxTimeInMilliseconds, 1000);

    }
    public void  Cancel(View v)
    {
        Intent i=new Intent(ui.this, reject.class);
        startActivity(i);
    }


public void startTimer(final long finish, long tick) {
        CountDownTimer t;
        t = new CountDownTimer(finish, tick) {

       public void onTick(long millisUntilFinished) {
        long remainedSecs = millisUntilFinished / 1000;
        textTimer.setText("" + (remainedSecs / 60) + ":" + (remainedSecs % 60));// manage it accordign to you
        }

       public void onFinish() {
           textTimer.setText("00:00:00");
           TextView tv=(TextView) findViewById (R.id.textView);
           tv.setText("Ambulance Arriving");
           Toast.makeText(ui.this, "Finish", Toast.LENGTH_SHORT).show();
           cancel();
           new ConnectBT().execute();
        }
        }.start();
        }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(ui.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (ui.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(ui.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        } else {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (location != null) {
                double latti = location.getLatitude();
                double longi = location.getLongitude();
                lattitude = String.valueOf(latti);
                longitude = String.valueOf(longi);
                String s="Your current location is"+ "\n" + "Lattitude = " + lattitude + "\n" + "Longitude = " + longitude;
                Toast.makeText(this,s,Toast.LENGTH_SHORT).show();

            }else{
                Toast.makeText(this,"Unble to Trace your location",Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void buildAlertMessageNoGps() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please Turn ON your GPS Connection")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(ui.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            httppath+="name=gaurav&lat="+lattitude+"&lng="+longitude;
            InputStream inputStream = null;
            String result = "";
            try {
                // create HttpClient
                HttpClient httpclient = new DefaultHttpClient();

                // make GET request to the given URL
                HttpResponse httpResponse = httpclient.execute(new HttpGet(httppath));

                // receive response as inputStream
                inputStream = httpResponse.getEntity().getContent();

                // convert InputStream to string
                if(inputStream != null)
                    result = convertInputStreamToString(inputStream);
                else
                    result = "InputStream did not work";

            } catch (Exception e) {
                Log.d("InputStream", e.getLocalizedMessage());
            }
            return  null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
           progress.dismiss();
        }
    }
    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;
    }

}
