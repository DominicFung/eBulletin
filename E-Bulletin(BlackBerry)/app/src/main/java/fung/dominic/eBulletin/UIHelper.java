package fung.dominic.eBulletin;


import android.widget.LinearLayout;

public class UIHelper {
    public static void setButtonState(LinearLayout ll, Boolean canConnect){
        if(canConnect){
            ll.setBackgroundResource(R.drawable.lightbuttonshape);
            ll.setClickable(true);
        }else {
            ll.setBackgroundResource(R.drawable.lightbuttonshape_disable);
            ll.setClickable(false);
        }
    }
}
