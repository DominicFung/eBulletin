package fung.dominic.eBulletin;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;

import fung.dominic.eBulletin.GCMconnection.QuickstartPreferences;
import fung.dominic.eBulletin.GCMconnection.RegistrationIntentService;


public class PageScroll extends FragmentActivity implements Observer{

    public static customViewPager viewPager = null;
    Fragment fragment = null;
    SlidingUpPanelLayout mLayout = null;
    CheckBox OpenOnStart, ExternalReader;
    Switch Notifications;
    Button CheckUpdates;
    AlertDialog ShowAlert;
    AlertDialog.Builder timeOut;
    ProgressBar updateCircle;

    public static final String PREFS_NAME = "MyPrefsFile";
    public static final String AutoConnectMode = "AutoConnectMode";
    public static final String ReaderMode = "ReaderMode";
    public static final String NotifyMode = "NotifyMode";
    public static BroadcastObserver bco;

    public static String RegID = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_scroll);

        SharedPreferences settings = this.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        final SharedPreferences.Editor editor = settings.edit();

        bco = new BroadcastObserver();
        bco.addObserver(this);
        editor.putBoolean(QuickstartPreferences.IS_APP_RUNNING, true).apply();

        RegID = settings.getString(RegistrationIntentService.RegIDTag, null);

        boolean AutoConnect = settings.getBoolean(AutoConnectMode, false);

        if (AutoConnect && BootupPage.fromBootup) {

            if(!settings.getBoolean(MainSocket.ServerStatusMode,true)) {
                Toast.makeText(this, "Server is Offline, Try again later", Toast.LENGTH_LONG).show();
            } else if (!MainSocket.isOpenable){
                Toast.makeText(this, "file was corrupted, please try again", Toast.LENGTH_LONG).show();
            }else {

                if (savedInstanceState == null) {
                    Bundle extras = getIntent().getExtras();
                    if (extras != null) {

                        if (!settings.getBoolean(ReaderMode, true)) {
                            Intent j = new Intent(PageScroll.this, ShowBulletin.class);
                            String str = extras.getString("SKIP_SOCKET");

                            j.putExtra(net.sf.andpdf.pdfviewer.PdfViewerActivity.EXTRA_PDFFILENAME, str);

                            try {
                                startActivity(j);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {

                            String str = extras.getString("SKIP_SOCKET");

                            File as = new File(str);
                            if (!as.exists()) {
                                Log.i("PageScroll", "file didnt exist before");
                            }

                            Log.i("PageScroll", "file size: " + as.length());

                            Uri uri = ToExternalStorage.ToExternalStorage(as, ToExternalStorage.DEFAULT_NAME);

                            if (uri == null) {
                                Toast.makeText(this, "Unable to update external", Toast.LENGTH_LONG).show();
                            }

                            if (ToExternalStorage.isExternalStorageReadable()) {
                                try {
                                    Intent i = new Intent(Intent.ACTION_VIEW);

                                    i.setDataAndType(uri, "application/pdf");
                                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                                    startActivity(i);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(this, "No Application available to view pdf", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(this, "Unable to read from external", Toast.LENGTH_LONG).show();
                            }
                        }

                    } else {
                        Log.i("PageScroll", "SKIP_SOCKET is null");
                        timeOut = new AlertDialog.Builder(PageScroll.this);
                        timeOut.setTitle("CONNECTION TIME-OUT");
                        timeOut.setMessage("The server may be down: Ensure that you are connected to the " +
                                "correct wireless fidelity. Click \"help\" for more information");
                        timeOut.setCancelable(false);
                        timeOut.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                        ShowAlert = timeOut.create();
                        ShowAlert.show();
                    }
                }
            }
        }

        viewPager = ((customViewPager) findViewById(R.id.pager));
        FragmentManager fragmentManager = getSupportFragmentManager();
        MyAdapter myAdapter = new MyAdapter(fragmentManager);
        viewPager.setAdapter(myAdapter);

        mLayout = (SlidingUpPanelLayout)findViewById(R.id.sliding_layout);
        mLayout.setCoveredFadeColor(Color.TRANSPARENT);

        OpenOnStart = (CheckBox)findViewById(R.id.checkBox2);
        ExternalReader = (CheckBox)findViewById(R.id.checkBox3);
        Notifications = (Switch)findViewById(R.id.switch1);
        CheckUpdates = (Button)findViewById(R.id.button);

        OpenOnStart.setChecked(AutoConnect);
        ExternalReader.setChecked(settings.getBoolean(ReaderMode, true));
        Notifications.setChecked(settings.getBoolean(NotifyMode, true));

        ExternalReader.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean(ReaderMode, ExternalReader.isChecked()).apply();
            }
        });

        Notifications.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String NotifyMessage;

                if (isChecked)
                    NotifyMessage = "Notifications: On";
                else
                    NotifyMessage = "Notifications: Off";

                Toast.makeText(getApplicationContext(), NotifyMessage, Toast.LENGTH_LONG).show();
                editor.putBoolean(NotifyMode, Notifications.isChecked()).apply();
            }
        });

        CheckUpdates.setOnClickListener(buttonCheckUpdatesOnClickListener);

        updateCircle = (ProgressBar)findViewById(R.id.progressBar3);
        updateCircle.setVisibility(View.GONE);

        BootupPage.fromBootup = false;

    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Log.i("PageScroll","OnResume called");

        if(!QuickstartPreferences.ProgressShow) {

            if (("".equals(settings.getString(RegistrationIntentService.RegIDTag, "")) || settings.getBoolean(QuickstartPreferences.TRY_REREG, false))) {

                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(QuickstartPreferences.TRY_REREG, !QuickstartPreferences.isAndroid);
                editor.apply();

                Intent intent = new Intent(this, RegistrationIntentService.class);
                startService(intent);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("PageScroll", "OnPause called");
    }

    OnClickListener buttonCheckUpdatesOnClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View arg0) {

            updateCircle.setVisibility(View.VISIBLE);

            ConnectServer ServerCheck = new ConnectServer();
            ServerCheck.execute();
        }
    };

    @Override
    public void update(Observable observable, Object data) {
        Log.i("PageScroll", "Update Triggered");

        Intent intent = new Intent("UPDATE");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public class ConnectServer extends AsyncTask<Void, Void, Void> {

        private String OutputMsg;

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            updateCircle.setVisibility(View.GONE);

            Toast.makeText(getApplicationContext(),OutputMsg,Toast.LENGTH_LONG).show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            Socket sock;

            SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            String currentDate = settings.getString(QuickstartPreferences.CURRENT_PDF_DATE, "NO DATE FOUND");

            try{
                sock = new Socket();
                sock.connect(new InetSocketAddress(MainSocket.IPaddress, MainSocket.PORT), 6000);

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

                String inputDate = response.toString();
                Log.i("pdfDateCheck", "Current: " + currentDate + ", New: " + inputDate);

                assert currentDate != null;
                if (currentDate.equals(inputDate)){
                    OutputMsg = "E-Bulletin is up to date";
                }else{
                    OutputMsg = "E-Bulletin is NOT up to date";
                }
                sock.close();

                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(MainSocket.ServerStatusMode, true).apply();

                Intent intentUPDATE = new Intent("UPDATE");
                LocalBroadcastManager.getInstance(PageScroll.this).sendBroadcast(intentUPDATE);

                if ("".equals(settings.getString(RegistrationIntentService.RegIDTag, ""))){
                    Intent intent = new Intent(PageScroll.this, RegistrationIntentService.class);
                    startService(intent);
                }

            }catch(IOException e){

                OutputMsg = "Server is Offline";
                if(settings.getBoolean(MainSocket.ServerStatusMode,true)){
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean(MainSocket.ServerStatusMode, false).apply();
                }

                Intent intentUPDATE = new Intent("UPDATE");
                LocalBroadcastManager.getInstance(PageScroll.this).sendBroadcast(intentUPDATE);
            }

            return null;
        }
    }

    class MyAdapter extends FragmentStatePagerAdapter {
        SparseArray<Fragment> registeredFragments = new SparseArray<>();

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public Fragment getItem(int position) {

            if (position == 0){
                fragment = new MainSocket();
            }else if (position == 1){
                fragment = new SundayDatePicker();
            }else if (position == 2){
                fragment = new Help();
            }else if (position == 3){
                fragment = new VersionInformation();
            }
            return fragment;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment)super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
            if (mLayout != null &&
                    (mLayout.getPanelState() == PanelState.EXPANDED || mLayout.getPanelState() == PanelState.ANCHORED)) {
                mLayout.setPanelState(PanelState.COLLAPSED);
                return true;
            } else {
                moveTaskToBack(true);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStop() {
        super.onStop();

        OpenOnStart = (CheckBox)findViewById(R.id.checkBox2);
        ExternalReader = (CheckBox)findViewById(R.id.checkBox3);
        Notifications = (Switch)findViewById(R.id.switch1);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(AutoConnectMode, OpenOnStart.isChecked());
        editor.putBoolean(ReaderMode, ExternalReader.isChecked());
        editor.putBoolean(NotifyMode, Notifications.isChecked());
        editor.putBoolean(QuickstartPreferences.IS_APP_RUNNING, false);

        editor.commit();
    }


}
