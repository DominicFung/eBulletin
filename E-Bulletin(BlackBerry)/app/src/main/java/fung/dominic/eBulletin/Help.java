package fung.dominic.eBulletin;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class Help extends Fragment {

    Configuration Config;
    View v;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rView;
        Config = getResources().getConfiguration();

        if(Config.orientation == Configuration.ORIENTATION_LANDSCAPE){
            rView = inflater.inflate(R.layout.fragment_help_lan, container, false);
        }else {
            rView = inflater.inflate(R.layout.fragment_help_por, container, false);
        }

        return rView;
    }

//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//    }
}
