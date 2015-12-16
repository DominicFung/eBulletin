package fung.dominic.eBulletin;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import fung.dominic.eBulletin.GCMconnection.QuickstartPreferences;

public class BootupPage extends Activity{

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    static final String BOOTUP_DELAYER_ID = "Bootup Delayer";

    private boolean isDone = false;
    ProgressBar progress;
    TextView Percentage;
    public static boolean fromBootup = true;
    NotificationManager nm;
    int ShownPercentage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bootup_page);

        progress = (ProgressBar)findViewById(R.id.progressBar);
        Percentage = (TextView)findViewById(R.id.textView30);

        progress.setVisibility(View.GONE);
        Percentage.setVisibility(View.GONE);

        MainSocket.isOpenable = true;

        nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences settings = getSharedPreferences(PageScroll.PREFS_NAME, MODE_PRIVATE);

        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(QuickstartPreferences.TRY_REREG, true);
        editor.apply();

        if (true) { // use checkPlayServices() on android devices
            // registration happens in resume of PageScroll

            Log.i("BootupPage", "in onStart");

            boolean AutoConnect = settings.getBoolean(PageScroll.AutoConnectMode, false);
            if(AutoConnect) {

                ConnectServer OnlineServer = new ConnectServer(MainSocket.IPaddress, MainSocket.PORT);
                OnlineServer.execute();

            } else {

                waitTime ToMainPage = new waitTime();
                ToMainPage.execute();

            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i("BootupPage", "OnResume Called");

        SharedPreferences settings = getSharedPreferences(PageScroll.PREFS_NAME, MODE_PRIVATE);

        if(settings.getBoolean(QuickstartPreferences.WAS_DOWNLOADING, false) && settings.getBoolean(BOOTUP_DELAYER_ID,false)) {

            SharedPreferences.Editor editor = settings.edit();

            editor.putBoolean(BOOTUP_DELAYER_ID, false);
            editor.putBoolean(QuickstartPreferences.WAS_DOWNLOADING, false);

            editor.putBoolean(QuickstartPreferences.TRY_REREG, !QuickstartPreferences.isAndroid);
            editor.apply();

            try {
                Intent i = new Intent(BootupPage.this, PageScroll.class);

                File as = new File(getApplicationContext()
                        .getDir("bulletin.pdf", Context.MODE_PRIVATE).getParent(), "bulletin.pdf");

                Log.i("BootupPage", as.getAbsolutePath());

                if (isDone) {
                    Log.i("BootupPage", "SKIP_SOCKET is put");
                    i.putExtra("SKIP_SOCKET", as.getAbsolutePath());
                }

                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.i("BootupPage", "OnPaused Called");

        SharedPreferences settings = getSharedPreferences(PageScroll.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(BOOTUP_DELAYER_ID, true).apply();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("GCMmessage", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    public class waitTime extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... params) {

            try{
                Thread.sleep(3000);
            }catch(InterruptedException ex){
                Thread.currentThread().interrupt();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Intent i = new Intent(BootupPage.this, PageScroll.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }
    }

    public class ConnectServer extends AsyncTask<Void, Integer, Void> {

        String dstAddress;
        int dstPort;

        ConnectServer(String addr, int port) {
            dstAddress = addr;
            dstPort = port;
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            try{
                Thread.sleep(1000);
            }catch(InterruptedException ex){
                Thread.currentThread().interrupt();
            }

            RunAnimation();

            Log.i("Connection", "There is connection");

            Socket socket = null;
            SharedPreferences settings = BootupPage.this.getSharedPreferences(PageScroll.PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();


            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(dstAddress, dstPort), 6000);

                editor.putBoolean(QuickstartPreferences.WAS_DOWNLOADING, true).apply();

                InputStream inputStream = socket.getInputStream();
                PrintWriter pw = new PrintWriter(socket.getOutputStream(),true);

                pw.write("EM");
                pw.flush();

                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                StringBuffer response = new StringBuffer();

                int c;
                while ((c = br.read()) != -1){ // not very efficient (but the only way I can do it for now)
                    response.append((char) c);
                    if (response.toString().endsWith("bytes")) break;
                }

                String[] Input = (response.toString()).split(" ");
                String SaveDate = Input[0];
                int SaveTotalBytes = Integer.valueOf(Input[1].replace("bytes", ""));

                Log.i("BootupPage", "Date of file: " + SaveDate);

                editor.putString(QuickstartPreferences.CURRENT_PDF_DATE,SaveDate);
                editor.apply();


                ByteArrayOutputStream byteArrayOutputStream =
                        new ByteArrayOutputStream(1024);
                byte[] buffer = new byte[1024];

                int bytesRead;
                int bytesDownloaded = 0;

                while ((bytesRead = inputStream.read(buffer)) > 0){
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                    bytesDownloaded= bytesDownloaded + bytesRead;
                    this.publishProgress(((bytesDownloaded * 100) / SaveTotalBytes));
                }

                byte[] bytes = byteArrayOutputStream.toByteArray();

                File as = new File(getApplicationContext()
                        .getDir("bulletin.pdf", Context.MODE_PRIVATE).getParent(), "bulletin.pdf");
                if (!as.exists()){
                    as.createNewFile();
                }

                FileOutputStream outStream = new FileOutputStream(as);
                outStream.write(bytes);


                inputStream.close();
                outStream.flush();
                outStream.close();

                Log.i("BootupPage", "file size: " + as.length());

                MainSocket.isOpenable = (SaveTotalBytes == bytesDownloaded);
                isDone = true;

                editor.putBoolean(MainSocket.ServerStatusMode, true);
                editor.apply();

            } catch (IllegalArgumentException | IOException e) {

                editor.putBoolean(MainSocket.ServerStatusMode, false);
                editor.apply();

            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {

            Percentage.setTextSize(20);

            if((values[0]%50)==0){
                Percentage.setTextColor(Color.parseColor("#ff0000"));
                Percentage.setTypeface(null, Typeface.BOLD);
            } else if ((values[0]%10)==0){
                Percentage.setTextColor(Color.parseColor("#0000FF"));
            }else{
                Percentage.setTextColor(Color.parseColor("#000078"));
                Percentage.setTypeface(null, Typeface.NORMAL);
            }
            Percentage.setText(values[0] + "%");
            progress.setProgress(values[0]);
            ShownPercentage = values[0];
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            SharedPreferences settings = getSharedPreferences(PageScroll.PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();

            if(!settings.getBoolean(BOOTUP_DELAYER_ID, false)) {

                editor.putBoolean(QuickstartPreferences.WAS_DOWNLOADING, false);

                editor.putBoolean(QuickstartPreferences.TRY_REREG, !QuickstartPreferences.isAndroid);
                editor.apply();

                try {
                    Intent i = new Intent(BootupPage.this, PageScroll.class);

                    File as = new File(getApplicationContext()
                            .getDir("bulletin.pdf", Context.MODE_PRIVATE).getParent(), "bulletin.pdf");

                    Log.i("BootupPage", as.getAbsolutePath());

                    if (isDone) {
                        Log.i("BootupPage", "SKIP_SOCKET is put");
                        i.putExtra("SKIP_SOCKET", as.getAbsolutePath());
                    }

                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{

                if(settings.getBoolean(PageScroll.NotifyMode,true)) {

                    Intent intent = new Intent(BootupPage.this, BootupPage.class);
                    intent.setAction(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    PendingIntent pi = PendingIntent.getActivity(BootupPage.this, 0, intent, 0);
                    String body = "Download Complete!";
                    String Title = "eBulletin: Download";
                    NotificationCompat.Builder n = new NotificationCompat.Builder(getApplicationContext())
                            .setSmallIcon(R.drawable.ic_stat_ic_notification)
                            .setTicker("eBulletin: " + body)
                            .setContentTitle(Title)
                            .setWhen(System.currentTimeMillis())
                            .setContentText(body)
                            .setAutoCancel(true)
                            .setContentIntent(pi);

                    nm.notify(QuickstartPreferences.uniqueID, n.build());
                }
            }
        }
    }


    private void RunAnimation(){
        BootupPage.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageView Logo = (ImageView)findViewById(R.id.imageView2);

                RelativeLayout.LayoutParams positionRules = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);

                positionRules.setMargins(0, 0, 0, -57);
                positionRules.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.anchor);
                positionRules.addRule(RelativeLayout.CENTER_HORIZONTAL);
                Logo.setLayoutParams(positionRules);

                progress.setProgress(0);
                Percentage.setText("0%");

                progress.setVisibility(View.VISIBLE);
                Percentage.setVisibility(View.VISIBLE);

            }
        });
    }

}