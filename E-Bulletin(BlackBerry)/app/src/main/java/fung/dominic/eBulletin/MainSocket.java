package fung.dominic.eBulletin;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import net.sf.andpdf.pdfviewer.PdfViewerActivity;

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
import java.net.UnknownHostException;

import fung.dominic.eBulletin.GCMconnection.QuickstartPreferences;

public class MainSocket extends Fragment {

    TextView textResponse, percentage;
    LinearLayout z_buttonPref, z_buttonOpenWith, z_buttonConnect, statusContainer;
    AlertDialog ShowAlert;
    public static final String IPaddress = "192.0.215.122";//192.168.2.107
    public static final int PORT = 8203;
    public static final String ServerStatusMode = "ServerStatus";
    ProgressBar loadCircle;
    AlertDialog.Builder UnknownHost, timeOut;
    int hasError = 0;
    private static final String hasErrorID = "hasError";
    View v;
    NotificationManager nm;
    private int bytesDownloaded, SaveTotalBytes;

    public static boolean isOpenable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        SharedPreferences settings = this.getActivity().getSharedPreferences(PageScroll.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(QuickstartPreferences.WAS_DOWNLOADING, false).apply();

        LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(mMessageReceiver, new IntentFilter("UPDATE"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_main_socket, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        v = getView();

        nm = (NotificationManager)getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

        assert v != null;
        z_buttonConnect = (LinearLayout)v.findViewById(R.id.connect);
        z_buttonPref = (LinearLayout)v.findViewById(R.id.pref);
        textResponse = (TextView)v.findViewById(R.id.response);
        loadCircle = (ProgressBar)v.findViewById(R.id.Loading);
        z_buttonOpenWith = (LinearLayout)v.findViewById(R.id.openWith);
        percentage = (TextView)v.findViewById(R.id.ShowPercent);

        percentage.setVisibility(View.GONE);
        loadCircle.setVisibility(View.GONE);

        File as = new File(getActivity().getApplicationContext()
                .getDir("bulletin.pdf", Context.MODE_PRIVATE).getParent(), "bulletin.pdf");

        if (!as.exists()){
            isOpenable = false;
        }

        setButtonState(z_buttonOpenWith, isOpenable);

        SharedPreferences settings = this.getActivity().getSharedPreferences(PageScroll.PREFS_NAME, Context.MODE_PRIVATE);

        if(settings.getBoolean(QuickstartPreferences.WAS_DOWNLOADING, false)){
            percentage.setVisibility(View.VISIBLE);
            loadCircle.setVisibility(View.VISIBLE);
            loadCircle.setProgress(100);
            percentage.setText("100%");
            percentage.setTextSize(40);
            percentage.setTextColor(Color.parseColor("#0000FF"));

        }

        z_buttonConnect.setOnClickListener(buttonConnectOnClickListener);
        z_buttonPref.setOnClickListener((buttonPrefOnClickListener));
        z_buttonOpenWith.setOnClickListener((buttonReopenOnClickListener));

        boolean ServerOnline = settings.getBoolean(ServerStatusMode,true);
        if (ServerOnline){ //This code can be simplified greatly setButtonState(button, ServerOnline) for example
            setButtonState(z_buttonConnect, true);
            setServerState(true);
            Log.i("MainSocket","Online Look");
        }else {
            setButtonState(z_buttonConnect, false);
            setServerState(false);
            Log.i("MainSocket","Offline Look");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.i("MainSocket","onResume");

        SharedPreferences settings = getActivity().getSharedPreferences(PageScroll.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        if(QuickstartPreferences.ProgressShow){
            percentage.setVisibility(View.VISIBLE);
            loadCircle.setVisibility(View.VISIBLE);
            setButtonState(z_buttonConnect, true);
        }

        if(settings.getBoolean(QuickstartPreferences.WAS_DOWNLOADING, false)){

            UnknownHost = new AlertDialog.Builder(MainSocket.this.getActivity());
            UnknownHost.setTitle("CONNECTION UNFOUND");
            UnknownHost.setMessage("Ensure that you are connected to the " +
                    "correct wireless fidelity. Click \"help\" for more information");
            UnknownHost.setCancelable(false);
            UnknownHost.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

            timeOut = new AlertDialog.Builder(MainSocket.this.getActivity());
            timeOut.setTitle("CONNECTION TIME-OUT");
            timeOut.setMessage("The server may be down: Ensure that you are connected to the " +
                    "correct wireless fidelity. Click \"help\" for more information");
            timeOut.setCancelable(false);
            timeOut.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

            hasError = settings.getInt(hasErrorID, 0);

            if (hasError == 1) {
                ShowAlert = UnknownHost.create();
                ShowAlert.show();
            } else if (hasError == 2) {
                ShowAlert = timeOut.create();
                ShowAlert.show();
            } else if (hasError == 3) {
                Toast.makeText(MainSocket.this.getActivity(), "No Application available to view pdf", Toast.LENGTH_LONG).show();
            }else if (hasError == 4){
                QuickstartPreferences.postingMsg(this, "file was corrupted, please try again");
            }else {

                File as = new File(getActivity().getApplicationContext()
                        .getDir("bulletin.pdf", Context.MODE_PRIVATE).getParent(), "bulletin.pdf");

                if (settings.getBoolean(PageScroll.ReaderMode, true)) {
                    Uri uri = ToExternalStorage.ToExternalStorage(as, ToExternalStorage.DEFAULT_NAME);

                    if (uri == null) {
                        QuickstartPreferences.postingMsg(MainSocket.this, "Unable to update external");
                    }

                    if (ToExternalStorage.isExternalStorageReadable()) {
                        try {
                            Intent i = new Intent(Intent.ACTION_VIEW);

                            i.setDataAndType(uri, "application/pdf");
                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                            startActivity(i);
                        } catch (Exception e) {
                            e.printStackTrace();
                            QuickstartPreferences.postingMsg(MainSocket.this, "No Application available to view pdf");
                        }
                    } else {
                        QuickstartPreferences.postingMsg(MainSocket.this, "Unable to read from external");
                    }
                } else {
                    try {
                        Intent i = new Intent(MainSocket.this.getActivity(), ShowBulletin.class);
                        String str = as.getPath();

                        i.putExtra(PdfViewerActivity.EXTRA_PDFFILENAME, str);
                        startActivity(i);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                editor.putBoolean(QuickstartPreferences.WAS_DOWNLOADING, false).apply();

                customViewPager vp = (customViewPager) getActivity().findViewById(R.id.pager);
                vp.setPSEnabled(true);

                setButtonState(z_buttonOpenWith, isOpenable);
                percentage.setVisibility(View.GONE);
                loadCircle.setVisibility(View.GONE);
                QuickstartPreferences.ProgressShow = false;
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i("MainSocket", "onPause");
    }

    public void setButtonState(LinearLayout ll, Boolean canConnect){
        if(canConnect){
            ll.setBackgroundResource(R.drawable.lightbuttonshape);
            ll.setClickable(true);
        }else {
            ll.setBackgroundResource(R.drawable.lightbuttonshape_disable);
            ll.setClickable(false);
        }
    }

    public void setPendingLook(){
        MainSocket.this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                //StatusText.setTextColor(Color.MAGENTA);
                //StatusText.setTypeface(null, Typeface.ITALIC);
                //StatusText.setText("Pending...");
            }
        });
    }

    public void setOfflineLook(){
        MainSocket.this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                setButtonState(z_buttonConnect, false);
                setServerState(false);
            }
        });
    }

    public void setServerState(Boolean isOn){
        ImageView iv = (ImageView)this.getView().getRootView().findViewById(R.id.serverStateImg);
        TextView tv = (TextView)this.getView().getRootView().findViewById(R.id.serverStateMsg);
        if(isOn){
            iv.setImageResource(R.drawable.on_circle);
            tv.setText("Online");
            tv.setTextColor(getResources().getColor(R.color.primGrey));
        }else{
            iv.setImageResource(R.drawable.off_circle);
            tv.setText("Offline");
            tv.setTextColor(Color.RED);
        }
    }
    OnClickListener buttonPrefOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent i = new Intent(getActivity().getApplicationContext(), SettingsActivity.class);
            startActivity(i);
        }
    };

    OnClickListener buttonReopenOnClickListener = new OnClickListener(){

        @Override
        public void onClick(View arg0) {

            SharedPreferences settings = getActivity().getSharedPreferences(PageScroll.PREFS_NAME, Context.MODE_PRIVATE);

            File as = new File(getActivity().getApplicationContext()
                    .getDir("bulletin.pdf", Context.MODE_PRIVATE).getParent(), "bulletin.pdf");
            if (!as.exists()){
                AlertDialog.Builder nofile = new AlertDialog.Builder(MainSocket.this.getActivity());
                nofile.setTitle("ERROR: Invalid File");
                nofile.setMessage("The bulletin PDF corrupted/non-existent, please connect to the " +
                        "MCAC Server");
                nofile.setCancelable(false);
                nofile.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

                ShowAlert = nofile.create();
                ShowAlert.show();
            }
            else if (!settings.getBoolean(PageScroll.ReaderMode, true)){
                try {
                    Intent i = new Intent(MainSocket.this.getActivity(), ShowBulletin.class);
                    String str = as.getPath();

                    i.putExtra(PdfViewerActivity.EXTRA_PDFFILENAME, str);
                    startActivity(i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else{
                Uri uri = ToExternalStorage.ToExternalStorage(as,ToExternalStorage.DEFAULT_NAME);

                if (uri==null){
                    Toast.makeText(getActivity(), "Unable to update external", Toast.LENGTH_LONG).show();
                }

                if(ToExternalStorage.isExternalStorageReadable()){
                    try {
                        Intent i = new Intent(Intent.ACTION_VIEW);

                        i.setDataAndType(uri, "application/pdf");
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        startActivity(i);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "No Application available to view pdf", Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(getActivity(), "Unable to read from external", Toast.LENGTH_LONG).show();
                }
            }
        }

    };

    OnClickListener buttonConnectOnClickListener = new OnClickListener(){

        @Override
        public void onClick(View arg0) {

            customViewPager vp = (customViewPager)getActivity().findViewById(R.id.pager);

            if(vp.isPSEnabled()) {

                hasError = 0;

                loadCircle.setProgress(0);
                loadCircle.setVisibility(View.VISIBLE);

                percentage.setTextSize(40);
                percentage.setTextColor(Color.parseColor("#0000FF"));
                percentage.setText("0%");
                percentage.setVisibility(View.VISIBLE);
                QuickstartPreferences.ProgressShow = true;


                UnknownHost = new AlertDialog.Builder(MainSocket.this.getActivity());
                UnknownHost.setTitle("CONNECTION UNFOUND");
                UnknownHost.setMessage("Ensure that you are connected to the " +
                        "correct wireless fidelity. Click \"help\" for more information");
                UnknownHost.setCancelable(false);
                UnknownHost.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

                timeOut = new AlertDialog.Builder(MainSocket.this.getActivity());
                timeOut.setTitle("CONNECTION TIME-OUT");
                timeOut.setMessage("The server may be down: Ensure that you are connected to the " +
                        "correct wireless fidelity. Click \"help\" for more information");
                timeOut.setCancelable(false);
                timeOut.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

                vp.setPSEnabled(false);

                ConnectServer OfflineServer = new ConnectServer(IPaddress, PORT);
                OfflineServer.execute();
            }

        }
    };


    public class ConnectServer extends AsyncTask<Void, Integer, Void> {

        String dstAddress;
        int dstPort;
        String response = "";

        ConnectServer(String addr, int port){
            dstAddress = addr;
            dstPort = port;
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            Socket socket = null;
            SharedPreferences settings = MainSocket.this.getActivity().getSharedPreferences(PageScroll.PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();

            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(dstAddress, dstPort), 6000);

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
                SaveTotalBytes = Integer.valueOf(Input[1].replace("bytes", ""));

                Log.i("MainSocket", "Date of file: " + SaveDate);

                editor.putString(QuickstartPreferences.CURRENT_PDF_DATE, SaveDate);
                editor.apply();

                ByteArrayOutputStream byteArrayOutputStream =
                        new ByteArrayOutputStream(1024);
                byte[] buffer = new byte[1024];

                int bytesRead;
                bytesDownloaded = 0;

                while ((bytesRead = inputStream.read(buffer)) > 0){
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                    bytesDownloaded= bytesDownloaded + bytesRead;
                    this.publishProgress(((bytesDownloaded*100)/SaveTotalBytes));
                }

                byte[] bytes = byteArrayOutputStream.toByteArray();

                File as = new File(getActivity().getApplicationContext()
                        .getDir("bulletin.pdf", Context.MODE_PRIVATE).getParent(), "bulletin.pdf");
                if (!as.exists()){
                    as.createNewFile();
                }

                FileOutputStream outStream = new FileOutputStream(as);
                outStream.write(bytes);


                inputStream.close();
                outStream.flush();
                outStream.close();

                if(bytesDownloaded == SaveTotalBytes){

                    isOpenable = true;

                    if (settings.getBoolean(PageScroll.ReaderMode, true)) {
                        Uri uri = ToExternalStorage.ToExternalStorage(as, ToExternalStorage.DEFAULT_NAME);

                        if (uri == null) {
                            QuickstartPreferences.postingMsg(MainSocket.this, "Unable to update external");
                        }

                        if (ToExternalStorage.isExternalStorageReadable()) {
                            if (MainSocket.this.getActivity().hasWindowFocus()) { // seperated out because we dont want the toast to pop up
                                try {
                                    Intent i = new Intent(Intent.ACTION_VIEW);

                                    i.setDataAndType(uri, "application/pdf");
                                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                                    startActivity(i);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    QuickstartPreferences.postingMsg(MainSocket.this, "No Application available to view PDF");
                                }
                            }
                        } else {
                            QuickstartPreferences.postingMsg(MainSocket.this, "Unable to read from external");
                        }
                    } else {
                        try {
                            Intent i = new Intent(MainSocket.this.getActivity(), ShowBulletin.class);
                            String str = as.getPath();

                            i.putExtra(PdfViewerActivity.EXTRA_PDFFILENAME, str);
                            startActivity(i);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                }
                else{
                    hasError = 4;
                    QuickstartPreferences.postingMsg(MainSocket.this, "File was corrupted, please try again");
                    isOpenable = false;
                }
            } catch (IllegalArgumentException | UnknownHostException e) {
                hasError = 1;

                editor.putBoolean(MainSocket.ServerStatusMode, false).apply();

                Intent intentUPDATE = new Intent("UPDATE");
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intentUPDATE);

                setOfflineLook();

                response = "UnknownHostException: " + e.toString();

            } catch (IOException e) {
                hasError = 2;

                editor.putBoolean(MainSocket.ServerStatusMode, false).apply();

                Intent intentUPDATE = new Intent("UPDATE");
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intentUPDATE);

                setOfflineLook();

                response = "IOException: " + e.toString();

            }finally{
                if(socket != null){
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

            if ((values[0]%10)==0){
                percentage.setTextSize(40);
                percentage.setTextColor(Color.parseColor("#0000FF"));
            }else{
                percentage.setTextSize(35);
                percentage.setTextColor(Color.parseColor("#000078"));
            }
            percentage.setText(values[0] + "%");
            loadCircle.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            SharedPreferences settings = MainSocket.this.getActivity().getSharedPreferences(PageScroll.PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();

            if (MainSocket.this.getActivity().hasWindowFocus()){
                editor.putBoolean(QuickstartPreferences.WAS_DOWNLOADING, false).apply();
                customViewPager vp = (customViewPager) getActivity().findViewById(R.id.pager);
                vp.setPSEnabled(true);

                setButtonState(z_buttonOpenWith,isOpenable);
                percentage.setVisibility(View.GONE);
                loadCircle.setVisibility(View.GONE);
                QuickstartPreferences.ProgressShow = false;

                if (hasError == 1) {
                    ShowAlert = UnknownHost.create();
                    ShowAlert.show();
                } else if (hasError == 2) {
                    ShowAlert = timeOut.create();
                    ShowAlert.show();
                } else if (hasError == 3) {
                    Toast.makeText(MainSocket.this.getActivity(), "No Application available to view pdf", Toast.LENGTH_LONG).show();
                }
            }else{

                editor.putInt(hasErrorID, hasError);
                editor.putBoolean(QuickstartPreferences.WAS_DOWNLOADING, true);
                editor.apply();

                Intent intent = new Intent(MainSocket.this.getActivity(), BootupPage.class);
                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                PendingIntent pi = PendingIntent.getActivity(MainSocket.this.getActivity(), 0, intent, 0);
                String body = "Download Complete!";
                String Title = "eBulletin: Download";
                NotificationCompat.Builder n = new NotificationCompat.Builder(getActivity().getApplicationContext())
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

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            SharedPreferences settings = MainSocket.this.getActivity().getSharedPreferences(PageScroll.PREFS_NAME, Context.MODE_PRIVATE);
            boolean status = settings.getBoolean(MainSocket.ServerStatusMode, true);

            if (status) {
                setButtonState(z_buttonConnect,true);
                setServerState(true);
            }
            else {
                setButtonState(z_buttonConnect,false);
                setServerState(false);
            }

            if (intent.getBooleanExtra("PENDING",false)){
                //StatusText.setTextColor(Color.MAGENTA);
                //StatusText.setTypeface(null, Typeface.ITALIC);
                //StatusText.setText("Pending...");
            }
        }
    };

    @Override
    public void onDestroy() {

        LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }
}