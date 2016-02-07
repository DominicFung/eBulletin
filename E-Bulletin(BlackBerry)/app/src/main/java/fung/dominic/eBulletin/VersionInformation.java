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

public class VersionInformation extends Fragment {

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

        View v = getView();
        String feedBackMsg = getResources().getString(R.string.commentsFeedbackMsg);
        feedBackMsg = feedBackMsg.replace("{EMAIL}","<b><u>fung_dominic@hotmail.com</u></b>");
        TextView feedbackEle = (TextView)v.findViewById(R.id.commentsFeedbackMsg);
        feedbackEle.setText(Html.fromHtml(feedBackMsg));
    }
}
