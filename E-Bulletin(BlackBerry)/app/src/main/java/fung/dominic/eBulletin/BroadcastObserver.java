package fung.dominic.eBulletin;

import android.util.Log;

import java.util.Observable;

public class BroadcastObserver extends Observable{

    private void triggerObservers(){
        setChanged();
        Log.i("BroadcastObserver","notifyObservers " + hasChanged());
        Log.i("BroadcastObserver","Observer count: " + countObservers());
        notifyObservers();
    }

    public void change(){
        triggerObservers();
    }
}
