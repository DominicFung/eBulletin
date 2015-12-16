package fung.dominic.eBulletin.GCMconnection;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import fung.dominic.eBulletin.MainSocket;
import fung.dominic.eBulletin.PageScroll;
import fung.dominic.eBulletin.R;
import fung.dominic.eBulletin.SundayDatePicker;

public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";
    public static final String RegIDTag = "RegID";


    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        SharedPreferences settings = getSharedPreferences(PageScroll.PREFS_NAME, MODE_PRIVATE);

        if (QuickstartPreferences.isAndroid) {
            try {
                synchronized (TAG) {

                    InstanceID instanceID = InstanceID.getInstance(this);
                    String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                            GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

                    Log.i(TAG, "GCM Registration Token: " + token);

                    sendRegistrationToServer(token);

                    if (!settings.getBoolean(MainSocket.ServerStatusMode, false)) {
                        Intent i = new Intent(RegistrationIntentService.this, PageScroll.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                    }

                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(RegIDTag, token);
                    editor.commit();
                }
            } catch (Exception e) {
                Log.d(TAG, "Failed to complete token refresh", e);
                // If an exception happens while fetching the new token or updating our registration data
                // on a third-party server, this ensures that we'll attempt the update at a later time.
                sharedPreferences.edit().putString(RegIDTag, "").apply();
            }
            // Notify UI that registration has completed, so the progress indicator can be hidden.
            Intent registrationComplete = new Intent(QuickstartPreferences.REGISTRATION_COMPLETE);
            LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
        }
        else{
            SharedPreferences.Editor editor = settings.edit();

            if (PageScroll.mainSockFrag != null) {
                MainSocket.setPendingLook(PageScroll.mainSockFrag);
            }

            checkServer();

            editor.putString(RegIDTag,"BLACKBERRY_NULL_REGISTRATION");
            editor.apply();
        }
    }

    private void checkServer(){
        Socket sock;

        SharedPreferences settings = getSharedPreferences(PageScroll.PREFS_NAME, MODE_PRIVATE);

        try{

            if (PageScroll.mainSockFrag != null){
                MainSocket.setPendingLook(PageScroll.mainSockFrag);
            }

            sock = new Socket();
            sock.connect(new InetSocketAddress(MainSocket.IPaddress, MainSocket.PORT), 1000);

            PrintWriter pw = new PrintWriter(sock.getOutputStream(),true);
            BufferedReader br = new BufferedReader(new InputStreamReader(sock.getInputStream()));

            pw.write("CHECK");
            pw.flush();


            String inputLine;
            StringBuilder response = new StringBuilder();

            if((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }

            pw.close();
            br.close();
            sock.close();

            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(MainSocket.ServerStatusMode, true).apply();

            if (PageScroll.mainSockFrag != null) {
                MainSocket.setOnlineLook(PageScroll.mainSockFrag);
            }

            if (PageScroll.SundayPickFrag != null){
                SundayDatePicker.setOnlineLook(PageScroll.SundayPickFrag);
            }

        }catch(IOException e){

            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(MainSocket.ServerStatusMode, false).apply();

            if (PageScroll.mainSockFrag != null) {
                MainSocket.setOfflineLook(PageScroll.mainSockFrag);
            }

            if (PageScroll.SundayPickFrag != null){
                SundayDatePicker.setOfflineLook(PageScroll.SundayPickFrag);
            }
        }

    }


    private void sendRegistrationToServer(String token) {
        Socket sock;
        try{

            SharedPreferences settings = getSharedPreferences(PageScroll.PREFS_NAME, MODE_PRIVATE);
            String toSend = token.concat(" " + settings.getString(RegIDTag, ""));
            toSend.concat(settings.getString(RegIDTag,""));

            Log.i(TAG, "toSend: " + toSend);
            Log.i(TAG, "oldRegID: " + settings.getString(RegIDTag, ""));
            Log.i(TAG, "token: " + token);

            sock = new Socket();
            sock.connect(new InetSocketAddress(MainSocket.IPaddress, MainSocket.PORT), 6000);

            BufferedOutputStream bw = new BufferedOutputStream(sock.getOutputStream());
            bw.write(toSend.getBytes());
            bw.flush();
            bw.close();
            sock.close();

            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(MainSocket.ServerStatusMode, true).apply();

        }catch(IOException e){
            SharedPreferences settings = getSharedPreferences(PageScroll.PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(MainSocket.ServerStatusMode, false).commit();
        }
    }



}
