package fung.dominic.eBulletin.GCMconnection;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import fung.dominic.eBulletin.BroadcastObserver;
import fung.dominic.eBulletin.MainSocket;
import fung.dominic.eBulletin.PageScroll;
import fung.dominic.eBulletin.R;

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";

    @Override
    public void onMessageReceived(String from, Bundle data) {

        String Status = data.getString("status");
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "status: " + Status);

        SharedPreferences settings = getSharedPreferences(PageScroll.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        if(Status.equals("online")){
            editor.putBoolean(MainSocket.ServerStatusMode, true);
            String message = data.getString("message");
            String title = data.getString("title");
            sendNotification(title, message);
            Log.i(TAG, "Status \""+ Status + "\" set");
        }else if(Status.equals("offline")){
            editor.putBoolean(MainSocket.ServerStatusMode, false);
            String message = data.getString("message");
            String title = data.getString("title");
            sendNotification(title, message);
            Log.i(TAG, "Status \"" + Status + "\" sent");
        }else{
            Log.i(TAG, "This should never happen");
        }

        editor.commit();

        if(settings.getBoolean(QuickstartPreferences.IS_APP_RUNNING, false)) {
            BroadcastObserver bco = PageScroll.bco;
            bco.change();
        }
    }

    private void sendNotification(String title, String message) {

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Intent intent = new Intent();
        intent.setClass(this.getApplicationContext(),PageScroll.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pi = PendingIntent.getActivity(this.getApplicationContext(),0,intent,0);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setTicker(message)
                .setContentTitle(title)
                .setWhen(System.currentTimeMillis())
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pi);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(QuickstartPreferences.uniqueID, notificationBuilder.build());
    }
}
