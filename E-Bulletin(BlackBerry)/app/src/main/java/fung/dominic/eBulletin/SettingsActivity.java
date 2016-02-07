package fung.dominic.eBulletin;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.util.Map;


public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "MyPrefsFile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_overflow_page);

        Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getFragmentManager().beginTransaction().replace(R.id.settingsframe, new MyPreferenceFragment()).commit();

    }

    public static class MyPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            refreshPreferences();
        }

        @Override
        public void onResume() {
            super.onResume();

            getPreferenceScreen()
                    .getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            refreshPreferences();
            getPreferenceScreen()
                    .getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            updatePreference(findPreference(key), key);
        }

        private void refreshPreferences(){
            SharedPreferences sharedPreferences = this.getActivity().getSharedPreferences(PageScroll.PREFS_NAME, MODE_PRIVATE);
            SharedPreferences screenPreferences = getPreferenceScreen().getSharedPreferences();
            setInitialPreferences(sharedPreferences, screenPreferences);
        }

        private void setInitialPreferences(SharedPreferences sharedPreferences, SharedPreferences screenSettings){
            Map<String,?> settings = sharedPreferences.getAll();

            //Set settings screen to reflect actual shared preferences
            for(Map.Entry<String,?> entry : settings.entrySet()){
                Log.i("Settings read",entry.getKey() + ": " +
                        entry.getValue().toString());
                if(screenSettings.contains(entry.getKey())){
                    Preference p = findPreference(entry.getKey());
                    if(p instanceof CheckBoxPreference){
                        ((CheckBoxPreference) p).setChecked((Boolean)entry.getValue());
                    }
                    else if(p instanceof SwitchPreference){
                        ((SwitchPreference) p).setChecked((Boolean)entry.getValue());
                    }
                    else
                    {
                        continue;
                    }
                }
            }
        }
        private void updatePreference(Preference preference, String key) {
            if (preference == null) return;

            SharedPreferences sharedPreferences = this.getActivity().getSharedPreferences(PageScroll.PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (preference instanceof CheckBoxPreference) {
                CheckBoxPreference cbPreference = (CheckBoxPreference) preference;
                editor.putBoolean(cbPreference.getKey(), cbPreference.isChecked());
            }else if(preference instanceof SwitchPreference){
                SwitchPreference sPreference = (SwitchPreference) preference;
                editor.putBoolean(sPreference.getKey(), sPreference.isChecked());
            }
            else{
                return;
            }
            editor.apply();
        }


    }

}