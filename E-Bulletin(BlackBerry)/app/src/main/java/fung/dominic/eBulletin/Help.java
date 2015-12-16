package fung.dominic.eBulletin;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class Help extends Fragment {

    Button buttonBack, buttonVersion;
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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        v = getView();

        buttonBack = (Button)v.findViewById(R.id.Back);
        buttonVersion = (Button)v.findViewById(R.id.version);

        buttonBack.setOnClickListener((buttonBackOnClickListener));
        buttonVersion.setOnClickListener((buttonVersionOnClickListener));

        String s = "If problems persist, the server maybe down, please contact Dominic Fung: <b><u>fung_dominic@hotmail.com</u></b>";
        TextView set = (TextView)v.findViewById(R.id.contactInfo);
        set.setText(Html.fromHtml(s));
    }

    View.OnClickListener buttonVersionOnClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {

            ViewPager vp = (ViewPager)getActivity().findViewById(R.id.pager);
            vp.setCurrentItem(3);

        }
    };


    View.OnClickListener buttonBackOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            ViewPager vp = (ViewPager)getActivity().findViewById(R.id.pager);
            vp.setCurrentItem(0);

        }
    };
}
