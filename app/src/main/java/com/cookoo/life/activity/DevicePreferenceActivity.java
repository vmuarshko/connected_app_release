package com.cookoo.life.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.cookoo.life.R;
import com.cookoo.life.domain.AlertCategories;
import com.cookoo.life.service.BluetoothService;

import java.util.HashMap;
import java.util.Map;

public class DevicePreferenceActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static String TAG = DevicePreferenceActivity.class.getSimpleName();
    public static final Map<String,Byte[]> preferenceToCategory;
    static {
        preferenceToCategory = new HashMap<String,Byte[]>();
        preferenceToCategory.put("set_on_alarm_social", new Byte[]{AlertCategories.SOCIAL});
        preferenceToCategory.put("set_on_alarm_private", new Byte[]{ AlertCategories.PRIVATE});
        preferenceToCategory.put("set_on_alarm_email",  new Byte[]{AlertCategories.EMAIL});
        preferenceToCategory.put("set_on_alarm_calendar", new Byte[]{ AlertCategories.SCHEDULE});
        preferenceToCategory.put("set_on_alarm_battery",  new Byte[]{AlertCategories.BATTERY});
        preferenceToCategory.put("set_on_alarm_call",  new Byte[]{AlertCategories.CALL,AlertCategories.MISSED_CALL,AlertCategories.VOICE_MAIL});
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.PreferencesTheme);
        super.onCreate(savedInstanceState);
        PreferenceManager.getDefaultSharedPreferences(MainActivity.activity).registerOnSharedPreferenceChangeListener(this);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new DevicePref1Fragment())
                .commit();
    }

    @Override
    public void onBackPressed(){
        MainActivity.showSplash = false;
        super.onBackPressed();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, String.format("shared preference %s changed", key));
        if (preferenceToCategory.containsKey(key)) {
            boolean isOn = sharedPreferences.getBoolean(key, true);
            Byte[] categories = preferenceToCategory.get(key);
            BluetoothService.getInstance().updateAlertSourceMask(categories,isOn);
        }
    }

    /**
     * This fragment shows the preferences for the first header.
     */
    public static class DevicePref1Fragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            PreferenceManager.setDefaultValues(getActivity(),
                    R.xml.preference, false);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preference);

        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.device_pref_fragment, container, false);
            return v;
        }

    }

}
