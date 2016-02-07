package fung.dominic.eBulletin;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ConnectToChurch extends Fragment {

    Configuration Config;
    View v;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rView;
//        Config = getResources().getConfiguration();

//        if(Config.orientation == Configuration.ORIENTATION_LANDSCAPE){
//            rView = inflater.inflate(R.layout.fragment_connect_lan, container, false);
//        }else {
//            rView = inflater.inflate(R.layout.fragment_connect_por, container, false);
//        }

        rView = inflater.inflate(R.layout.fragment_connect_por, container, false);

        return rView;
    }
}
