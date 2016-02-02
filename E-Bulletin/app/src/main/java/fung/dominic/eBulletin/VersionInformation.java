package fung.dominic.eBulletin;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class VersionInformation extends Fragment {

    Button buttonBack;
    Configuration Config;
    View v;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rView;
        Config = getResources().getConfiguration();

        if(Config.orientation == Configuration.ORIENTATION_LANDSCAPE){
            rView = inflater.inflate(R.layout.fragment_version_lan, container, false);
        }else {
            rView = inflater.inflate(R.layout.fragment_version_por, container, false);
        }

        return rView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        v = getView();

        buttonBack = (Button) v.findViewById(R.id.Back);

        buttonBack.setOnClickListener((buttonBackOnClickListener));
    }

    View.OnClickListener buttonBackOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            ViewPager vp = (ViewPager)getActivity().findViewById(R.id.pager);
            vp.setCurrentItem(2);

        }
    };
}
