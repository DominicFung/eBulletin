package fung.dominic.eBulletin;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import fung.dominic.eBulletin.GCMconnection.QuickstartPreferences;

public class SundayDatePicker extends Fragment {

    View v;
    Socket sock;
    ProgressBar progress;
    TextView percentage;
    List<Integer> DayList;
    int LastSavedDay, LastSavedMonth, LastSavedYear;
    static final String SUNPICK_DELAYER_ID = "SundayPick Delayer";
    private static final String FILENAME_ID = "SunPicFileName";
    final static String DAY_ID = "DAY";
    final static String MONTH_ID = "MONTH";
    final static String YEAR_ID = "YEAR";
    NumberPicker pickYear, pickMonth, pickDay;
    Button Retrieve, back;
    Configuration Config;
    NotificationManager nm;
    private final String[] Months = {"January","February","March","April","May","June","July",
            "August","September","October","November","December"};

    private final String[] Months2015 = {"May","June","July",
            "August","September","October","November","December"};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rView;

        Config = getResources().getConfiguration();

        if(Config.orientation == Configuration.ORIENTATION_LANDSCAPE){
            rView = inflater.inflate(R.layout.fragment_pick_date_lan, container, false);
        }else {
            rView = inflater.inflate(R.layout.fragment_pick_date_por, container, false);
        }

        return rView;
    }

    @Override
    public void onResume() {
        super.onResume();

        PageScroll.SundayPickFrag = this;

        Log.i("SundayDatePicker", "onResume");

        SharedPreferences settings = this.getActivity().getSharedPreferences(PageScroll.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        if (settings.getBoolean(MainSocket.ServerStatusMode, true)){
            Retrieve.setEnabled(true);
        }else{
            Retrieve.setEnabled(false);
        }

        if (settings.getBoolean(QuickstartPreferences.WAS_DOWNLOADING, false)
                && settings.getBoolean(SUNPICK_DELAYER_ID, false)){

            File as = new File(getActivity().getApplicationContext()
                    .getDir("archived_bulletin.pdf", Context.MODE_PRIVATE).getParent(), "archived_bulletin.pdf");

            String retrieveFileName = settings.getString(FILENAME_ID, "");
            int bytesDownloaded = settings.getInt(QuickstartPreferences.BYTES_DOWNLOADED_ID, 0);
            int SaveTotalBytes = settings.getInt(QuickstartPreferences.BYTES_TOTAL_ID, 0);

            Uri uri = ToExternalStorage.ToExternalStorage(as, retrieveFileName);

            if (bytesDownloaded != SaveTotalBytes) {
                QuickstartPreferences.postingMsg(SundayDatePicker.this, "file was corrupted, please try again");
            } else if (uri == null)
                QuickstartPreferences.postingMsg(SundayDatePicker.this, "Unable to update external");

            else if (ToExternalStorage.isExternalStorageReadable()) {
                try {
                    Intent i = new Intent(Intent.ACTION_VIEW);

                    i.setDataAndType(uri, "application/pdf");
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    startActivity(i);
                } catch (Exception e) {
                    e.printStackTrace();
                    QuickstartPreferences.postingMsg(SundayDatePicker.this, "No Application available to view pdf");
                }
            } else {
                QuickstartPreferences.postingMsg(SundayDatePicker.this, "Unable to read from external");
            }

            editor.putBoolean(QuickstartPreferences.WAS_DOWNLOADING, false).apply();
            customViewPager vp = (customViewPager)getActivity().findViewById(R.id.pager);
            vp.setPSEnabled(true);

            progress.setVisibility(View.GONE);
            percentage.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        PageScroll.SundayPickFrag = null;

        SharedPreferences settings = SundayDatePicker.this.getActivity().getSharedPreferences(PageScroll.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(SUNPICK_DELAYER_ID, true).apply();

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        v = getView();
        SharedPreferences settings = this.getActivity().getSharedPreferences(PageScroll.PREFS_NAME, Context.MODE_PRIVATE);

        progress = (ProgressBar)v.findViewById(R.id.progressBar2);
        percentage = (TextView)v.findViewById(R.id.textView31);
        progress.setVisibility(View.GONE);
        percentage.setVisibility(View.GONE);

        if (settings.getBoolean(QuickstartPreferences.WAS_DOWNLOADING, false)){
            progress.setVisibility(View.VISIBLE);
            percentage.setVisibility(View.VISIBLE);
            if (settings.getBoolean(SUNPICK_DELAYER_ID, false)){
                percentage.setTextColor(Color.parseColor("#ff0000"));
                percentage.setTypeface(null, Typeface.BOLD);
                percentage.setText("100%");
                progress.setProgress(100);
            }
        }

        GregorianCalendar cal = new GregorianCalendar();

        pickYear = (NumberPicker)v.findViewById(R.id.YearPicker);
        pickMonth = (NumberPicker)v.findViewById(R.id.MonthPicker);
        pickDay = (NumberPicker)v.findViewById(R.id.DayPicker);
        Retrieve = (Button)v.findViewById(R.id.buttonRetrieve);
        back = (Button)v.findViewById(R.id.backButton2);

        pickYear.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        pickMonth.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        pickDay.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        LastSavedDay = settings.getInt(DAY_ID, 3);
        LastSavedMonth = settings.getInt(MONTH_ID, 4);
        LastSavedYear = settings.getInt(YEAR_ID, 2015);

        pickMonth.setMinValue(0);
        pickMonth.setMaxValue(11);
        pickMonth.setDisplayedValues(Months);
        pickMonth.setValue(LastSavedMonth);

        pickYear.setMinValue(2015);
        pickYear.setMaxValue(cal.get(GregorianCalendar.YEAR));
        pickYear.setValue(LastSavedYear);
        findSundays(pickYear.getValue(), pickMonth.getValue());

        pickYear.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                findSundays(newVal, pickMonth.getValue());
            }
        });

        pickMonth.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                findSundays(pickYear.getValue(), newVal);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customViewPager vp = (customViewPager)getActivity().findViewById(R.id.pager);

                if(vp.isPSEnabled())
                    vp.setCurrentItem(0);
            }
        });

        Retrieve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                customViewPager vp = (customViewPager)getActivity().findViewById(R.id.pager);
                if(vp.isPSEnabled()) {

                    int[] ChooseDay = new int[DayList.size()];
                    for (int i = 0; i < ChooseDay.length; i++)
                        ChooseDay[i] = DayList.get(i);

                    String retrieveFileName;
                    String year = "" + pickYear.getValue();
                    String month = String.format("%02d", (pickMonth.getValue() + 1));
                    String day = String.format("%02d", ChooseDay[pickDay.getValue()]);

                    Log.i("SundayDatePicker", "YYYY/MM/DD: " + year + "/" + month + "/" + day);
                    retrieveFileName = year + "/" + month + "/" + day;

                    progress.setProgress(0);
                    progress.setVisibility(View.VISIBLE);

                    percentage.setTextColor(Color.parseColor("#ff0000"));
                    percentage.setText("0%");
                    percentage.setVisibility(View.VISIBLE);

                    vp.setPSEnabled(false);

                    ConnectServer getOldFile = new ConnectServer(retrieveFileName);
                    getOldFile.execute();
                }
            }
        });
    }

    public class ConnectServer extends AsyncTask<Void, Integer, Void> {

        String retrieveFileName;

        ConnectServer(String FileName){
            retrieveFileName = FileName;
        }

        @Override
        protected Void doInBackground(Void... params) {

            SharedPreferences settings = SundayDatePicker.this.getActivity().getSharedPreferences(PageScroll.PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();

            try{
                sock = new Socket();
                sock.connect(new InetSocketAddress(MainSocket.IPaddress, MainSocket.PORT), 6000);

                editor.putBoolean(QuickstartPreferences.WAS_DOWNLOADING, true);

                InputStream inputStream = sock.getInputStream();
                PrintWriter pw = new PrintWriter(sock.getOutputStream(),true);

                pw.write(retrieveFileName+"EM");
                pw.flush();

                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                StringBuffer response = new StringBuffer();

                int c;
                while ((c = br.read()) != -1){ // not very efficient (but the only way I can do it for now)
                    response.append((char) c);
                    if (response.toString().endsWith("yes")) break;
                    if (response.toString().equals("no")) break;
                }

                Log.i("SundayDatePicker", "Is there a file? " + response.toString());

                if(response.toString().endsWith("yes")){

                    String[] Input = (response.toString()).split(" ");
                    int SaveTotalBytes = Integer.valueOf(Input[0]);

                    ByteArrayOutputStream byteArrayOutputStream =
                            new ByteArrayOutputStream(1024);
                    byte[] buffer = new byte[1024];

                    int bytesRead;
                    int bytesDownloaded = 0;

                    while ((bytesRead = inputStream.read(buffer)) != -1){
                        byteArrayOutputStream.write(buffer, 0, bytesRead);
                        bytesDownloaded= bytesDownloaded + bytesRead;
                        this.publishProgress(((bytesDownloaded*100)/SaveTotalBytes));
                    }

                    byte[] bytes = byteArrayOutputStream.toByteArray();

                    File as = new File(getActivity().getApplicationContext()
                            .getDir("archived_bulletin.pdf", Context.MODE_PRIVATE).getParent(), "archived_bulletin.pdf");
                    if (!as.exists()){
                        as.createNewFile();
                    }

                    FileOutputStream outStream = new FileOutputStream(as);
                    outStream.write(bytes);


                    outStream.flush();
                    outStream.close();

                    if(!settings.getBoolean(SUNPICK_DELAYER_ID, false)) {

                        Uri uri = ToExternalStorage.ToExternalStorage(as, retrieveFileName);

                        if (bytesDownloaded != SaveTotalBytes) {
                            QuickstartPreferences.postingMsg(SundayDatePicker.this, "file was corrupted, please try again");
                        } else if (uri == null)
                            QuickstartPreferences.postingMsg(SundayDatePicker.this, "Unable to update external");

                        else if (ToExternalStorage.isExternalStorageReadable()) {
                            try {
                                Intent i = new Intent(Intent.ACTION_VIEW);

                                i.setDataAndType(uri, "application/pdf");
                                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                                startActivity(i);
                            } catch (Exception e) {
                                e.printStackTrace();
                                QuickstartPreferences.postingMsg(SundayDatePicker.this, "No Application available to view pdf");
                            }
                        } else {
                            QuickstartPreferences.postingMsg(SundayDatePicker.this, "Unable to read from external");
                        }
                    }else{
                        editor.putInt(QuickstartPreferences.BYTES_DOWNLOADED_ID, bytesDownloaded);
                        editor.putInt(QuickstartPreferences.BYTES_TOTAL_ID, SaveTotalBytes);
                        editor.apply();
                    }

                }else{
                    QuickstartPreferences.postingMsg(SundayDatePicker.this, retrieveFileName + " is not in Archives");
                }


                pw.close();
                inputStream.close();


            }catch (IOException e){

                retrieveFileName = "";

                editor.putBoolean(MainSocket.ServerStatusMode, false).apply();

                setOfflineLook(SundayDatePicker.this);
                if(PageScroll.mainSockFrag != null)
                    MainSocket.setOfflineLook(PageScroll.mainSockFrag);

                QuickstartPreferences.postingMsg(SundayDatePicker.this, "Server is Offline");
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if((values[0]%50)==0){
                percentage.setTextColor(Color.parseColor("#ff0000"));
                percentage.setTypeface(null, Typeface.BOLD);
            } else if ((values[0]%10)==0){
                percentage.setTextColor(Color.parseColor("#0000FF"));
            }else{
                percentage.setTextColor(Color.parseColor("#000078"));
                percentage.setTypeface(null, Typeface.NORMAL);
            }
            percentage.setText(values[0] + "%");
            progress.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            SharedPreferences settings = SundayDatePicker.this.getActivity().getSharedPreferences(PageScroll.PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();

            if(!settings.getBoolean(SUNPICK_DELAYER_ID, false)) {
                editor.putBoolean(QuickstartPreferences.WAS_DOWNLOADING, false).apply();
                customViewPager vp = (customViewPager) getActivity().findViewById(R.id.pager);
                vp.setPSEnabled(true);

                progress.setVisibility(View.GONE);
                percentage.setVisibility(View.GONE);
            }else{

                nm = (NotificationManager)getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

                editor.putString(FILENAME_ID, retrieveFileName).apply();

                Intent intent = new Intent(SundayDatePicker.this.getActivity(), BootupPage.class);
                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                PendingIntent pi = PendingIntent.getActivity(SundayDatePicker.this.getActivity(), 0, intent, 0);
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


    private void findSundays(int Year, int Month){

        Log.i("Calculation", "" + pickDay.getValue());
        DayList = new ArrayList<>();

        GregorianCalendar refDate = new GregorianCalendar(Year, Month, 1);
        ArrayList<String> Days = new ArrayList<>();
        String forLog = "";

        Log.i("Calculation", "Max days for year: " + Year + " month: " + Month + " = " + refDate.getActualMaximum(Calendar.DAY_OF_MONTH));

        for (int i = 1; i <= refDate.getActualMaximum(Calendar.DAY_OF_MONTH); i++){
            GregorianCalendar dayCheck = new GregorianCalendar(Year,Month,i);
            Log.i("Calculation","day " + i + " is a " + dayCheck.get(GregorianCalendar.DAY_OF_WEEK));
            if (dayCheck.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.SUNDAY ){
                Days.add(i+"");
                DayList.add(i);
                forLog = forLog.concat(i+" ");
            }
        }

        Log.i("Calculation", "Days: " + forLog + " Total days: " + Days.size());

        if (Days.size() == 0){ //safety
            Days.add("1");
        }

        String[] output = new String[Days.size()];
        output = Days.toArray(output);

        pickDay.setDisplayedValues(null);

        pickDay.setMinValue(0);
        pickDay.setMaxValue(Days.size() - 1);
        pickDay.setWrapSelectorWheel(true);

        pickDay.setDisplayedValues(output);

        if(LastSavedDay != 0){
            int Index = Arrays.asList(output).indexOf(LastSavedDay+"");
            LastSavedDay = 0;
            if (Index != -1){
                pickDay.setValue(Index);
            }else{
                Log.i("DatePicker", "Date does not exist");
                pickDay.setValue(0);
            }
        }else
            pickDay.setValue(0);
    }

    @Override
    public void onStop() {
        super.onStop();

        SharedPreferences settings = this.getActivity().getSharedPreferences(PageScroll.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putInt(DAY_ID, Integer.parseInt(pickDay.getDisplayedValues()[pickDay.getValue()]));
        editor.putInt(MONTH_ID, pickMonth.getValue());
        editor.putInt(YEAR_ID, pickYear.getValue());
        editor.apply();

    }

    public static void setOnlineLook(final Fragment f){
        f.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Button Retrieve = (Button)f.getActivity().findViewById(R.id.buttonRetrieve);

                Retrieve.setEnabled(true);
            }
        });
    }

    public static void setOfflineLook(final Fragment f){
        f.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Button Retrieve = (Button)f.getActivity().findViewById(R.id.buttonRetrieve);

                Retrieve.setEnabled(false);
            }
        });
    }
}
