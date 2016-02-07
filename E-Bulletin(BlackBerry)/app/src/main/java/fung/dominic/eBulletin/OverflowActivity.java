package fung.dominic.eBulletin;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class OverflowActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_overflow_page);

        Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle params = getIntent().getExtras();
        String fragmentToOpen = params.getString("fragment"); //fragment key stores fragment class name
        if(fragmentToOpen.equals(Help.class.getSimpleName())){
            setTitle("Help");
            getSupportFragmentManager().beginTransaction().replace(R.id.settingsframe, new Help()).commit();
        }else if(fragmentToOpen.equals(VersionInformation.class.getSimpleName())){
            setTitle("About");
            getSupportFragmentManager().beginTransaction().replace(R.id.settingsframe, new VersionInformation()).commit();
        }else{
            finish(); //ideally should not reach this situation
        }
    }
}
