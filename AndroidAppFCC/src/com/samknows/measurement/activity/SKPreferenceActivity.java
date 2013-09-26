package com.samknows.measurement.activity;

import com.samknows.libcore.SKLogger;
import com.samknows.libcore.SKConstants;
import com.samknows.measurement.FCCAppSettings;
import com.samknows.measurement.MainService;
import com.samknows.measurement.R;
import com.samknows.measurement.util.OtherUtils;
import com.samknows.measurement.util.TimeUtils;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;

import android.util.Log;
import android.widget.Toast;

public class SKPreferenceActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener{

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		//Hide preference user_self_id for beta version of the app
		if(!FCCAppSettings.getFCCAppSettingsInstance().user_self_id){
			Preference user_tag = findPreference("user_self_id");
			PreferenceCategory pc = (PreferenceCategory) findPreference("first_category");
			pc.removePreference(user_tag);
		} else {
			Log.e(this.getClass().toString(), "user_self_id set to true");
		}
		
		
		String versionName="";
		try {
			versionName = this.getPackageManager().getPackageInfo(this.getPackageName(), 0 ).versionName;
		} catch (NameNotFoundException e) {
			SKLogger.e(this, e.getMessage());
		}
		PreferenceCategory mCategory = (PreferenceCategory) findPreference("first_category");
		mCategory.setTitle(mCategory.getTitle()+" "+"("+versionName+")");

		long testsScheduled = FCCAppSettings.getInstance().getLong("number_of_tests_schedueld", -1);
		CheckBoxPreference mCheckBoxPref = (CheckBoxPreference) findPreference(SKConstants.PREF_SERVICE_ENABLED);
		if(testsScheduled <= 0){
			mCheckBoxPref.setChecked(false);
			mCheckBoxPref.setEnabled(false);
		}else{
			mCheckBoxPref.setChecked(FCCAppSettings.getInstance().isServiceEnabled());
		}
		
		updateLabels();
	
	}
	
	@SuppressWarnings("deprecation")
	protected void updateLabels(){
		FCCAppSettings app = FCCAppSettings.getFCCAppSettingsInstance();
		long configDataCap = app.getLong(SKConstants.PREF_DATA_CAP, -1l );
		String s_configDataCap = configDataCap == -1l ? "": configDataCap +"";
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SKPreferenceActivity.this);
		Preference p;
		
		String data_cap = preferences.getString(SKConstants.PREF_DATA_CAP, s_configDataCap);
		p = (Preference) findPreference(SKConstants.PREF_DATA_CAP);
		p.setTitle(getString(R.string.data_cap_title)+ " "+data_cap+getString(R.string.mb));	
		
		
		int data_cap_day = preferences.getInt(SKConstants.PREF_DATA_CAP_RESET_DAY, 1);
		p = (Preference) findPreference(SKConstants.PREF_DATA_CAP_RESET_DAY);
		p.setTitle(getString(R.string.data_cap_day_title)+ TimeUtils.getDayOfMonthSuffix(data_cap_day));
		
		
	}
	
	
	@Override
	protected void onResume(){
		super.onResume();
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).unregisterOnSharedPreferenceChangeListener(this);
	}
	
	
	@SuppressWarnings("deprecation")
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(SKConstants.PREF_SERVICE_ENABLED)) {
			if (FCCAppSettings.getInstance().isServiceEnabled()) {
				MainService.poke(SKPreferenceActivity.this);
			}else{
				OtherUtils.cancelAlarm(this);
			}
		}
		if (key.equals(SKConstants.PREF_DATA_CAP)) {

		    String dataCapValueString = sharedPreferences.getString(key, "");
		    Long dataCapValue;
		    try{
		    	dataCapValue = Long.parseLong(dataCapValueString);
		    }catch(NumberFormatException nfe){
		    	dataCapValue = 0L;
		    }
		    
		    if(dataCapValue > SKConstants.DATA_CAP_MAX_VALUE){
		  
		    	EditTextPreference p = (EditTextPreference) findPreference(key);
		    	p.setText(""+SKConstants.DATA_CAP_MAX_VALUE);
		    	Toast t = Toast.makeText(SKPreferenceActivity.this,getString(R.string.max_data_cap_message)+" "+SKConstants.DATA_CAP_MAX_VALUE, Toast.LENGTH_SHORT);
		    	t.show();
		    }else if(dataCapValue < SKConstants.DATA_CAP_MIN_VALUE){
		    	EditTextPreference p = (EditTextPreference) findPreference(key);
		    	p.setText(""+SKConstants.DATA_CAP_MIN_VALUE);
		    	Toast t = Toast.makeText(SKPreferenceActivity.this,getString(R.string.min_data_cap_message)+" "+SKConstants.DATA_CAP_MIN_VALUE, Toast.LENGTH_SHORT);
		    	t.show();
		    }
		}
		updateLabels();
	}
	
}
