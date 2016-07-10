package com.cookoo.life.fragment;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cookoo.life.R;

public class DeviceSettingsFragment extends PreferenceFragment {


	public DeviceSettingsFragment() {
		Bundle args = new Bundle(1);
		setArguments(args);
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    addPreferencesFromResource(R.xml.preference);



	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View mRootView = super.onCreateView(inflater, container, savedInstanceState);
		return mRootView;
	}


}
