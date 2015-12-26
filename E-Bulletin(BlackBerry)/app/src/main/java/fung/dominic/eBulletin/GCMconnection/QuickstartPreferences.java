package fung.dominic.eBulletin.GCMconnection;

import android.support.v4.app.Fragment;
import android.widget.Toast;

public class QuickstartPreferences {

    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String IS_APP_RUNNING = "isAppRunning";
    public static final String TRY_REREG = "tryReregistration";
    public static final String CURRENT_PDF_DATE = "pdfDate";
    public static final String WAS_DOWNLOADING = "wasDownloading";
    public static final String BYTES_DOWNLOADED_ID = "BytesDownloadedID";
    public static final String BYTES_TOTAL_ID = "BytesTotalID";
    public static final int uniqueID = 45523;


    public static final boolean isAndroid = true;


    public static void postingMsg(final Fragment f, final String message){
        f.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(f.getActivity(), message, Toast.LENGTH_LONG).show();
            }
        });
    }
}