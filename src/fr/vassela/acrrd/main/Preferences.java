/**
 * @file Preferences.java
 * @brief Software's preferences class
 * @author Arnaud Vassellier
 * @version 1.0
 * @date 2016
 * 
 * This file is part of ACRRD (Android Call Recorder Replayer Dictaphone).

    ACRRD is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    ACRRD is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ACRRD.  If not, see <http://www.gnu.org/licenses/>
 * 
 */

package fr.vassela.acrrd.main;

import java.io.File;
import java.util.Date;
import fr.vassela.acrrd.R;
import fr.vassela.acrrd.database.DatabaseManager;
import fr.vassela.acrrd.filters.Filters;
import fr.vassela.acrrd.localizer.LocalizerManager;
import fr.vassela.acrrd.notifier.TelephoneCallNotifier;
import fr.vassela.acrrd.purge.PurgeServiceManager;
import fr.vassela.acrrd.theme.ThemeManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.view.ActionMode.Callback;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Preferences extends PreferenceActivity implements AppCompatCallback, SharedPreferences.OnSharedPreferenceChangeListener
{
	private static String preferencesAudioSourceValue = "";
	private static String preferencesAudioFormatValue = "";
	private static String preferencesReplayerVisualizerValue = "";
	private static String preferencesStoragePathValue = "";
	private static String preferencesPurgeFrequencyValue = "";
	private static String preferencesPurgeRetentionValue = "";
	private static String preferencesLocaleSetValue = "";
	private static String preferencesThemeSetValue = "";
	private static String preferencesColorThemeSetValue = "";
	
	private boolean isInit;
	
	private DatabaseManager databaseManager = new DatabaseManager();
	private TelephoneCallNotifier telephoneCallNotifier = new TelephoneCallNotifier();
	private LocalizerManager localizerManager = new LocalizerManager();
	private ThemeManager themeManager = new ThemeManager();
	
	private AppCompatDelegate delegate;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		try
		{
			localizerManager.setPreferencesLocale(getApplicationContext());
			themeManager.setPreferencesTheme(getApplicationContext(), this);

			delegate = AppCompatDelegate.create(this, this);
			delegate.installViewFactory();
			
			super.onCreate(savedInstanceState);
			
			delegate.onCreate(savedInstanceState);
			
			delegate.setContentView(R.layout.preferences);
			
			Toolbar toolbar = (Toolbar)findViewById(R.id.preferences_toolbar);
	        delegate.setSupportActionBar(toolbar);
	        delegate.getSupportActionBar().setLogo(R.drawable.ic_menu_preferences);
	        delegate.setTitle(getApplicationContext().getString(R.string.main_toolbar_preferences));
	        delegate.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	        delegate.getSupportActionBar().setDisplayShowHomeEnabled(true);
			
			addPreferencesFromResource(R.layout.preferences_preferences);
			PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
			onInitSharedPreferences(PreferenceManager.getDefaultSharedPreferences(this));
			
			findPreference("preferences_filters_define").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
			{
		        @Override
		        public boolean onPreferenceClick(Preference preference)
		        {
		            startActivity(new Intent(getApplicationContext(), Filters.class));
		            return false;
		        }
		    });
		}
		catch (Exception e)
		{
			Log.w("Preferences", "onCreate : " + getApplicationContext().getString(R.string.log_preferences_error_create) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_error_create), new Date().getTime(), 2, false);
		}
	}
	
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
    	try
		{
    		if(key.equals("preferences_record_activate"))
	    	{
    			onSharedPreferenceRecordActivateChanged(sharedPreferences);
	    	}
    		
    		if(key.equals("preferences_filters_activate"))
	    	{
    			onSharedPreferenceFiltersActivateChanged(sharedPreferences);
	    	}
    		
    		if(key.equals("preferences_shake_detector_activate"))
	    	{
    			onSharedPreferenceShakeDetectorActivateChanged(sharedPreferences);
	    	}
    		
	    	if(key.equals("preferences_audio_source"))
	    	{
	    		onSharedPreferenceAudioSourceChanged();
	    	}
	    	
	    	if(key.equals("preferences_audio_max_volume_activate"))
	    	{
	    		onSharedPreferenceAudioMaxVolumeActivateChanged(sharedPreferences);
	    	}
	    	
	    	if(key.equals("preferences_audio_format"))
	    	{
	    		onSharedPreferenceAudioFormatChanged();
	    	}
	    	
	    	if(key.equals("preferences_replayer_visualizer"))
	    	{
	    		onSharedPreferenceReplayerVisualizerChanged();
	    	}
	    	
	    	if(key.equals("preferences_storage_path"))
	    	{
	    		onSharedStoragePathChanged();
	    	}
	    	
	    	if(key.equals("preferences_purge_activate"))
	    	{
	    		onSharedPreferencePurgeActivateChanged(sharedPreferences);
	    	}
	    	
	    	if(key.equals("preferences_purge_frequency"))
	    	{
	    		onSharedPreferencePurgeFrequencyChanged();
	    	}
	    	
	    	if(key.equals("preferences_purge_retention"))
	    	{
	    		onSharedPreferencePurgeRetentionChanged();
	    	}
	    	
	    	if(key.equals("preferences_logs_activate"))
	    	{
	    		onSharedPreferenceLogsActivateChanged(sharedPreferences);
	    	}
	    	
	    	if(key.equals("preferences_notifications_activate"))
	    	{
	    		onSharedPreferenceNotificationsActivateChanged(sharedPreferences);
	    	}
	    	
	    	if(key.equals("preferences_notifications_sound_activate"))
	    	{
	    		onSharedPreferenceNotificationsSoundActivateChanged(sharedPreferences);
	    	}
	    	
	    	if(key.equals("preferences_notifications_vibrate_activate"))
	    	{
	    		onSharedPreferenceNotificationsVibrateActivateChanged(sharedPreferences);
	    	}
	    	
	    	if(key.equals("preferences_notifications_led_activate"))
	    	{
	    		onSharedPreferenceNotificationsLedActivateChanged(sharedPreferences);
	    	}
	    	
	    	if(key.equals("preferences_toast_messages_activate"))
	    	{
	    		onSharedPreferenceToastMessagesActivateChanged(sharedPreferences);
	    	}
	    	
	    	if(key.equals("preferences_locale_set"))
	    	{
	    		onSharedPreferenceLocaleSetChanged();
	    	}
	    	
	    	if(key.equals("preferences_theme_set"))
	    	{
	    		onSharedPreferenceThemeSetChanged();
	    	}
	    	
	    	if(key.equals("preferences_color_theme_set"))
	    	{
	    		onSharedPreferenceColorThemeSetChanged();
	    	}

	    	if(key.equals("preferences_splashscreen_activate"))
	    	{
	    		onSharedPreferenceSplashscreenActivateChanged(sharedPreferences);
	    	}	
		}
		catch (Exception e)
		{
			Log.w("Preferences", "onSharedPreferenceChanged : " + getApplicationContext().getString(R.string.log_preferences_error_changed) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_error_changed), new Date().getTime(), 2, false);
		}
    }
    
	private void onInitSharedPreferences(SharedPreferences sharedPreferences)
	{
		try
		{
			isInit = false;
			
			onSharedPreferenceRecordActivateChanged(sharedPreferences);
			onSharedPreferenceFiltersActivateChanged(sharedPreferences);
			onSharedPreferenceShakeDetectorActivateChanged(sharedPreferences);
			onSharedPreferenceAudioSourceChanged();
			onSharedPreferenceAudioFormatChanged();
			onSharedPreferenceReplayerVisualizerChanged();
			onInitSharedPreferenceStoragePath();
			onSharedStoragePathChanged();
			onSharedPreferencePurgeActivateChanged(sharedPreferences);
			onSharedPreferencePurgeFrequencyChanged();
			onSharedPreferencePurgeRetentionChanged();
			onSharedPreferenceLogsActivateChanged(sharedPreferences);
    		onSharedPreferenceNotificationsActivateChanged(sharedPreferences);
    		onSharedPreferenceNotificationsSoundActivateChanged(sharedPreferences);
    		onSharedPreferenceNotificationsVibrateActivateChanged(sharedPreferences);
    		onSharedPreferenceNotificationsLedActivateChanged(sharedPreferences);
    		onSharedPreferenceToastMessagesActivateChanged(sharedPreferences);
    		onSharedPreferenceLocaleSetChanged();
    		onSharedPreferenceThemeSetChanged();
    		onSharedPreferenceColorThemeSetChanged();
    		onSharedPreferenceSplashscreenActivateChanged(sharedPreferences);
    		
    		isInit = true;
		}
		catch (Exception e)
		{
			Log.w("Preferences", "onInitSharedPreferences : " + getApplicationContext().getString(R.string.log_preferences_error_init) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_error_init), new Date().getTime(), 2, false);
		}
	}
	
	private void onSharedPreferenceRecordActivateChanged(SharedPreferences sharedPreferences)
	{
		try
		{
			boolean isPreferencesRecordActivate = sharedPreferences.getBoolean("preferences_record_activate", true);
			
			if(isPreferencesRecordActivate == true)
			{
				Log.d("Preferences", "onSharedPreferenceRecordActivateChanged : " + getApplicationContext().getString(R.string.log_preferences_record_activate_changed_start));
				
				if(isInit == true)
				{
					databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_record_activate_changed_start), new Date().getTime(), 3, false);	
				}
				
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
		        {
					telephoneCallNotifier.displayNotification(getApplicationContext(), getApplicationContext().getString(R.string.notification_preferences_activate_record_ticker), getApplicationContext().getString(R.string.notification_preferences_activate_record_content_title), getApplicationContext().getString(R.string.notification_preferences_activate_record_content_text), false, true, true);
		        }
				else
				{
					telephoneCallNotifier.displayCompatNotification(getApplicationContext(), getApplicationContext().getString(R.string.notification_preferences_activate_record_ticker), getApplicationContext().getString(R.string.notification_preferences_activate_record_content_title), getApplicationContext().getString(R.string.notification_preferences_activate_record_content_text), false, true, true);
				}
			}
			else
			{
				Log.d("Preferences", "onSharedPreferenceRecordActivateChanged : " + getApplicationContext().getString(R.string.log_preferences_record_activate_changed_stop));
				
				if(isInit == true)
				{
					databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_record_activate_changed_stop), new Date().getTime(), 3, false);
				}
				
				telephoneCallNotifier.cancelOngoingNotification(getApplicationContext());
			}
		}
		catch (Exception e)
		{
			Log.w("Preferences", "onSharedPreferenceRecordActivateChanged : " + getApplicationContext().getString(R.string.log_preferences_error_record_activate_changed) + " : " + e);
			
			if(isInit == true)
			{
				databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_error_record_activate_changed), new Date().getTime(), 2, false);
			}
		}
	}
	
	public static void onSharedPreferenceRecordActivate(Context context)
	{
		try
		{
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			boolean isPreferencesRecordActivate = sharedPreferences.getBoolean("preferences_record_activate", true);
			TelephoneCallNotifier telephoneCallNotifier = new TelephoneCallNotifier();
			
			if(isPreferencesRecordActivate == true)
			{
				Log.d("Preferences", "onSharedPreferenceRecordActivate : " + context.getString(R.string.log_preferences_record_activate_changed_start));
				
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
		        {
					telephoneCallNotifier.displayNotification(context, context.getString(R.string.notification_preferences_activate_record_ticker), context.getString(R.string.notification_preferences_activate_record_content_title), context.getString(R.string.notification_preferences_activate_record_content_text), false, true, true);
		        }
				else
				{
					telephoneCallNotifier.displayCompatNotification(context, context.getString(R.string.notification_preferences_activate_record_ticker), context.getString(R.string.notification_preferences_activate_record_content_title), context.getString(R.string.notification_preferences_activate_record_content_text), false, true, true);
				}
			}
			else
			{
				Log.d("Preferences", "onSharedPreferenceRecordActivate : " + context.getString(R.string.log_preferences_record_activate_changed_stop));
				
				telephoneCallNotifier.cancelOngoingNotification(context);
			}
		}
		catch (Exception e)
		{
			Log.w("Preferences", "onSharedPreferenceRecordActivate : " + context.getString(R.string.log_preferences_error_record_activate_changed) + " : " + e);
		}
	}
	
	private void onSharedPreferenceFiltersActivateChanged(SharedPreferences sharedPreferences)
	{
		try
		{
			boolean isPreferencesFiltersActivate = sharedPreferences.getBoolean("preferences_filters_activate", false);
			
			if(isPreferencesFiltersActivate == true)
			{
				Log.d("Preferences", "onSharedPreferenceFiltersActivateChanged : " + getApplicationContext().getString(R.string.log_preferences_filters_activate_changed_start));
				
				if(isInit == true)
				{
					databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_filters_activate_changed_start), new Date().getTime(), 3, false);	
				}
			}
			else
			{
				Log.d("Preferences", "onSharedPreferenceFiltersActivateChanged : " + getApplicationContext().getString(R.string.log_preferences_filters_activate_changed_stop));
				
				if(isInit == true)
				{
					databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_filters_activate_changed_stop), new Date().getTime(), 3, false);
				}
			}
			
			getPreferenceScreen().findPreference("preferences_filters_define").setEnabled(isPreferencesFiltersActivate);
			getPreferenceScreen().findPreference("preferences_shake_detector_activate").setEnabled(isPreferencesFiltersActivate);
		}
		catch (Exception e)
		{
			Log.w("Preferences", "onSharedPreferenceFiltersActivateChanged : " + getApplicationContext().getString(R.string.log_preferences_error_filters_activate_changed) + " : " + e);
			
			if(isInit == true)
			{
				databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_error_filters_activate_changed), new Date().getTime(), 2, false);
			}
		}
	}
	
	private void onSharedPreferenceShakeDetectorActivateChanged(SharedPreferences sharedPreferences)
	{
		try
		{
			boolean isPreferencesShakeDetectorActivate = sharedPreferences.getBoolean("preferences_shake_detector_activate", true);
			
			if(isPreferencesShakeDetectorActivate == true)
			{
				Log.d("Preferences", "onSharedPreferenceShakeDetectorActivateChanged : " + getApplicationContext().getString(R.string.log_preferences_shake_detector_activate_changed_start));
				
				if(isInit == true)
				{
					databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_shake_detector_activate_changed_start), new Date().getTime(), 3, false);	
				}
			}
			else
			{
				Log.d("Preferences", "onSharedPreferenceShakeDetectorActivateChanged : " + getApplicationContext().getString(R.string.log_preferences_shake_detector_activate_changed_stop));
				
				if(isInit == true)
				{
					databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_shake_detector_activate_changed_stop), new Date().getTime(), 3, false);
				}
			}
		}
		catch (Exception e)
		{
			Log.w("Preferences", "onSharedPreferenceShakeDetectorActivateChanged : " + getApplicationContext().getString(R.string.log_preferences_error_shake_detector_activate_changed) + " : " + e);
			
			if(isInit == true)
			{
				databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_error_shake_detector_activate_changed), new Date().getTime(), 2, false);
			}
		}
	}
	
	private void onSharedPreferenceAudioSourceChanged()
	{
		try
		{
			Preference preferencesAudioSource = findPreference("preferences_audio_source");
			if (preferencesAudioSource instanceof ListPreference)
			{
				ListPreference listPreferencesAudioSource = (ListPreference) preferencesAudioSource;
				preferencesAudioSourceValue = (String) listPreferencesAudioSource.getEntry();
				preferencesAudioSource.setSummary(getString(R.string.preferences_audio_source_summary) + " " + listPreferencesAudioSource.getEntry());
				
				Log.d("Preferences", "onSharedPreferenceAudioSourceChanged : " + getApplicationContext().getString(R.string.log_preferences_audio_source_changed) + " : " + listPreferencesAudioSource.getEntry());
				
				if(isInit == true)
				{	
					databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_audio_source_changed) + " : " + listPreferencesAudioSource.getEntry(), new Date().getTime(), 3, false);
				}
			}
		}
		catch (Exception e)
		{
			Log.w("Preferences", "onSharedPreferenceAudioSourceChanged : " + getApplicationContext().getString(R.string.log_preferences_error_audio_source_changed) + " : " + e);
			
			if(isInit == true)
			{
				databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_error_audio_source_changed), new Date().getTime(), 2, false);
			}
		}
	}
	
	private void onSharedPreferenceAudioMaxVolumeActivateChanged(SharedPreferences sharedPreferences)
	{
		try
		{
			boolean isPreferencesAudioMaxVolumeActivate = sharedPreferences.getBoolean("preferences_audio_max_volume_activate", true);
			
			if(isPreferencesAudioMaxVolumeActivate == true)
			{
				Log.d("Preferences", "onSharedPreferenceAudioMaxVolumeActivateChanged : " + getApplicationContext().getString(R.string.log_preferences_audio_max_volume_activate_changed_start));
				
				if(isInit == true)
				{
					databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_audio_max_volume_activate_changed_start), new Date().getTime(), 3, false);	
				}
			}
			else
			{
				Log.d("Preferences", "onSharedPreferenceAudioMaxVolumeActivateChanged : " + getApplicationContext().getString(R.string.log_preferences_audio_max_volume_activate_changed_stop));
				
				if(isInit == true)
				{
					databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_audio_max_volume_activate_changed_stop), new Date().getTime(), 3, false);
				}
			}
		}
		catch (Exception e)
		{
			Log.w("Preferences", "onSharedPreferenceAudioMaxVolumeActivateChanged : " + getApplicationContext().getString(R.string.log_preferences_error_audio_max_volume_activate_changed) + " : " + e);
			
			if(isInit == true)
			{
				databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_error_audio_max_volume_activate_changed), new Date().getTime(), 2, false);
			}
		}
	}
	
	public static String getSharedPreferenceAudioSource()
	{
		return preferencesAudioSourceValue;
	}
	
	private void onSharedPreferenceAudioFormatChanged()
	{
		try
		{
			Preference preferencesAudioFormat = findPreference("preferences_audio_format");
			if (preferencesAudioFormat instanceof ListPreference)
			{
				ListPreference listPreferencesAudioFormat = (ListPreference) preferencesAudioFormat;
				preferencesAudioFormatValue = (String) listPreferencesAudioFormat.getEntry();
				preferencesAudioFormat.setSummary(getString(R.string.preferences_audio_format_summary) + " " + listPreferencesAudioFormat.getEntry());
				
				Log.d("Preferences", "onSharedPreferenceAudioFormatChanged : " + getApplicationContext().getString(R.string.log_preferences_audio_format_changed) + " : " + listPreferencesAudioFormat.getEntry());
				
				if(isInit == true)
				{
					databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_audio_format_changed) + " : " + listPreferencesAudioFormat.getEntry(), new Date().getTime(), 3, false);
				}
			}
		}
		catch (Exception e)
		{
			Log.w("Preferences", "onSharedPreferenceAudioFormatChanged : " + getApplicationContext().getString(R.string.log_preferences_error_audio_format_changed) + " : " + e);
			
			if(isInit == true)
			{
				databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_error_audio_format_changed), new Date().getTime(), 2, false);
			}
		}
	}
	
	public static String getSharedPreferenceAudioFormat()
	{
		return preferencesAudioFormatValue;
	}
	
	private void onSharedPreferenceReplayerVisualizerChanged()
	{
		try
		{
			Preference preferencesReplayerVisualizer = findPreference("preferences_replayer_visualizer");
			if (preferencesReplayerVisualizer instanceof ListPreference)
			{
				ListPreference listPreferencesReplayerVisualizer = (ListPreference) preferencesReplayerVisualizer;
				preferencesReplayerVisualizerValue = (String) listPreferencesReplayerVisualizer.getEntry();
				preferencesReplayerVisualizer.setSummary(getString(R.string.preferences_replayer_visualizer_summary) + " " + listPreferencesReplayerVisualizer.getEntry());
				
				Log.d("Preferences", "onSharedPreferenceReplayerVisualizerChanged : " + getApplicationContext().getString(R.string.log_preferences_replayer_visualizer_changed) + " : " + listPreferencesReplayerVisualizer.getEntry());
				
				if(isInit == true)
				{
					databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_replayer_visualizer_changed) + " : " + listPreferencesReplayerVisualizer.getEntry(), new Date().getTime(), 3, false);
				}
			}
		}
		catch (Exception e)
		{
			Log.w("Preferences", "onSharedPreferenceReplayerVisualizerChanged : " + getApplicationContext().getString(R.string.log_preferences_error_replayer_visualizer_changed) + " : " + e);
			
			if(isInit == true)
			{
				databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_error_replayer_visualizer_changed), new Date().getTime(), 2, false);
			}
		}
	}
	
	public static String getSharedPreferenceReplayerVisualizer()
	{
		return preferencesReplayerVisualizerValue;
	}
	
	private void onInitSharedPreferenceStoragePath()
	{
		try
		{
			final ListPreference listPreferenceStoragePath = (ListPreference) findPreference("preferences_storage_path");
			CharSequence[] listPreferenceStoragePathEntries;
			CharSequence[] listPreferenceStoragePathValues;
			CharSequence[] listPreferenceStoragePathEntriesEnabled;
			CharSequence[] listPreferenceStoragePathValuesEnabled;
			int[] listPreferenceStoragePathEnable;
			String[] storagePathArray;
			int nbPreferenceStoragePathEntries = 0;
			int nbExternalStorage = 0;
			int nbSecondaryStorage = 0;
			int nbSecondaryStorageEnabled = 0;
			
			if (Environment.getExternalStorageState() != null)
			{
				nbExternalStorage = nbExternalStorage + 1;
			}
			
			String storagePath = System.getenv("SECONDARY_STORAGE");
			if (storagePath != null)
			{
				storagePathArray = storagePath.split(":");
				nbSecondaryStorage = storagePathArray.length;
				
				nbPreferenceStoragePathEntries = nbExternalStorage + nbSecondaryStorage;
				
				listPreferenceStoragePathEntries = new String[nbPreferenceStoragePathEntries];
				listPreferenceStoragePathValues = new String[nbPreferenceStoragePathEntries];
				listPreferenceStoragePathEnable = new int[nbPreferenceStoragePathEntries];
				
				int listPreferenceStoragePathIndex = 0;
				int secondaryStoragePathIndex = 0;
				
				if (nbExternalStorage != 0)
				{
					listPreferenceStoragePathEntries[listPreferenceStoragePathIndex] = String.valueOf(Environment.getExternalStorageDirectory());
					listPreferenceStoragePathValues[listPreferenceStoragePathIndex] = String.valueOf(listPreferenceStoragePathIndex + 1);
					listPreferenceStoragePathEnable[listPreferenceStoragePathIndex] = 1;
					nbSecondaryStorageEnabled = nbSecondaryStorageEnabled + 1;
					listPreferenceStoragePathIndex = listPreferenceStoragePathIndex + 1;
				}
				
				if (nbSecondaryStorage != 0)
				{
					for (; secondaryStoragePathIndex < nbSecondaryStorage ; listPreferenceStoragePathIndex ++, secondaryStoragePathIndex++ )
					{					
						File folder = new File(storagePathArray[secondaryStoragePathIndex]);
						
						if(!folder.exists() || !folder.canWrite())
						{
							listPreferenceStoragePathEnable[listPreferenceStoragePathIndex] = 0;
						}
						else
						{
							listPreferenceStoragePathEnable[listPreferenceStoragePathIndex] = 1;
							nbSecondaryStorageEnabled = nbSecondaryStorageEnabled + 1;
						}
						
						listPreferenceStoragePathEntries[listPreferenceStoragePathIndex] = storagePathArray[secondaryStoragePathIndex];
						listPreferenceStoragePathValues[listPreferenceStoragePathIndex] = String.valueOf(listPreferenceStoragePathIndex + 1);
					}
				}
				
				if(nbSecondaryStorageEnabled > 0)
				{
					listPreferenceStoragePathEntriesEnabled = new String[nbSecondaryStorageEnabled];
					listPreferenceStoragePathValuesEnabled = new String[nbSecondaryStorageEnabled];
					
					int i = 0;
					listPreferenceStoragePathIndex = 0;
					
					listPreferenceStoragePathEntriesEnabled[i] = listPreferenceStoragePathEntries[listPreferenceStoragePathIndex];
					listPreferenceStoragePathValuesEnabled[i] = "1";
					listPreferenceStoragePathIndex = listPreferenceStoragePathIndex + 1;
					i = i + 1;
					
					for (; listPreferenceStoragePathIndex < nbSecondaryStorage ; listPreferenceStoragePathIndex ++ )
					{	
						if(listPreferenceStoragePathEnable[listPreferenceStoragePathIndex] == 1)
						{
							listPreferenceStoragePathEntriesEnabled[i] = listPreferenceStoragePathEntries[listPreferenceStoragePathIndex];
							listPreferenceStoragePathValuesEnabled[i] = String.valueOf(i + 1);
							i++;
						}
					}
					
					listPreferenceStoragePath.setEntries(listPreferenceStoragePathEntriesEnabled);
					listPreferenceStoragePath.setDefaultValue("1");
					listPreferenceStoragePath.setEntryValues(listPreferenceStoragePathEntriesEnabled);
				}
				else
				{
					listPreferenceStoragePath.setEntries(listPreferenceStoragePathEntries);
					listPreferenceStoragePath.setDefaultValue("1");
					listPreferenceStoragePath.setEntryValues(listPreferenceStoragePathValues);
				}
				
			}
			else
			{
				nbPreferenceStoragePathEntries = nbExternalStorage;
				
				listPreferenceStoragePathEntries = new String[nbPreferenceStoragePathEntries];
				listPreferenceStoragePathValues = new String[nbPreferenceStoragePathEntries];
				
				int listPreferenceStoragePathIndex = 0;
				
				if (nbExternalStorage != 0)
				{
					listPreferenceStoragePathEntries[listPreferenceStoragePathIndex] = String.valueOf(Environment.getExternalStorageDirectory());
					listPreferenceStoragePathValues[listPreferenceStoragePathIndex] = String.valueOf(listPreferenceStoragePathIndex + 1);
				}
				
				listPreferenceStoragePath.setEntries(listPreferenceStoragePathEntries);
				listPreferenceStoragePath.setDefaultValue("1");
				listPreferenceStoragePath.setEntryValues(listPreferenceStoragePathValues);
			}
		}
		catch (Exception e)
		{
			Log.w("Preferences", "onInitSharedPreferenceStoragePath : " + getApplicationContext().getString(R.string.log_preferences_error_init_storage_path) + " : " + e);
			
			if(isInit == true)
			{
				databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_error_init_storage_path), new Date().getTime(), 2, false);
			}
		}
	}
	
	private void onSharedStoragePathChanged()
	{
		try
		{
			Preference preferencesStoragePath = findPreference("preferences_storage_path");
			if (preferencesStoragePath instanceof ListPreference)
			{
				ListPreference listPreferencesStoragePath = (ListPreference) preferencesStoragePath;
				if(listPreferencesStoragePath.getEntry() == null)
				{
					if (Environment.getExternalStorageState() != null)
					{
						preferencesStoragePathValue = String.valueOf(Environment.getExternalStorageDirectory());
					}
					else
					{
						preferencesStoragePathValue = "";
					}
					
					preferencesStoragePath.setSummary(getString(R.string.preferences_storage_path_summary) + " " + preferencesStoragePathValue);
				}
				else
				{
					preferencesStoragePathValue = (String) listPreferencesStoragePath.getEntry();
					preferencesStoragePath.setSummary(getString(R.string.preferences_storage_path_summary) + " " + listPreferencesStoragePath.getEntry());
				}
				
				SharedPreferences sharedPreferencesStoragePathValue = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				SharedPreferences.Editor sharedPreferencesEditor = sharedPreferencesStoragePathValue.edit();
    			sharedPreferencesEditor.putString("preferences_storage_path_value", preferencesStoragePathValue);
    			sharedPreferencesEditor.commit();
				
				Log.d("Preferences", "onSharedStoragePathChanged : " + getApplicationContext().getString(R.string.log_preferences_storage_path_changed) + " : " + preferencesStoragePathValue);
				
				if(isInit == true)
				{
					databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_storage_path_changed) + " : " + preferencesStoragePathValue, new Date().getTime(), 3, false);
				}
			}
		}
		catch (Exception e)
		{
			Log.w("Preferences", "onSharedStoragePathChanged : " + getApplicationContext().getString(R.string.log_preferences_error_storage_path_changed) + " : " + e);
			
			if(isInit == true)
			{
				databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_error_storage_path_changed), new Date().getTime(), 2, false);
			}
		}
	}
	
	public static String getSharedStoragePath(Context context)
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		String sharedPreferencesStoragePathValue = sharedPreferences.getString("preferences_storage_path_value", "");
		
		return sharedPreferencesStoragePathValue;
	}
	
	private void onSharedPreferencePurgeActivateChanged(SharedPreferences sharedPreferences)
	{
		try
		{
			boolean isPreferencesPurgeActivate = sharedPreferences.getBoolean("preferences_purge_activate", true);
			PurgeServiceManager purgeServiceManager = new PurgeServiceManager();
			
			if(isPreferencesPurgeActivate == true)
			{
				purgeServiceManager.startService(getApplicationContext());
				
				Log.d("Preferences", "onSharedPreferencePurgeActivateChanged : " + getApplicationContext().getString(R.string.log_purge_service_start_command));
				
				if(isInit == true)
				{
					databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_purge_service_start_command), new Date().getTime(), 3, false);
				}
			}
			else
			{
				Boolean purgeServiceStopped = purgeServiceManager.stopService(getApplicationContext());
				
				if(purgeServiceStopped == true)
				{
					Log.d("Preferences", "onSharedPreferencePurgeActivateChanged : " + getApplicationContext().getString(R.string.log_purge_service_destroy));
					
					if(isInit == true)
					{
						databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_purge_service_destroy), new Date().getTime(), 3, false);
					}
				}
				else
				{
					Log.w("Preferences", "onSharedPreferencePurgeActivateChanged : " + getApplicationContext().getString(R.string.log_purge_service_error_destroy));
					
					if(isInit == true)
					{
						databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_purge_service_error_destroy), new Date().getTime(), 2, false);
					}
				}
			}
			
			getPreferenceScreen().findPreference("preferences_purge_frequency").setEnabled(!isPreferencesPurgeActivate);
			getPreferenceScreen().findPreference("preferences_purge_retention").setEnabled(!isPreferencesPurgeActivate);
		}
		catch (Exception e)
		{
			Log.w("Preferences", "onSharedPreferencePurgeActivateChanged : " + getApplicationContext().getString(R.string.log_preferences_error_purge_activate_changed) + " : " + e);
			
			if(isInit == true)
			{
				databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_error_purge_activate_changed), new Date().getTime(), 2, false);
			}
		}
	}
	
	public static void onSharedPreferencePurgeActivate(Context context)
	{
		try
		{
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			boolean isPreferencesPurgeActivate = sharedPreferences.getBoolean("preferences_purge_activate", true);
			PurgeServiceManager purgeServiceManager = new PurgeServiceManager();
			
			if(isPreferencesPurgeActivate == true)
			{
				purgeServiceManager.startService(context);
				
				Log.d("Preferences", "onSharedPreferencePurgeActivate : " + context.getString(R.string.log_purge_service_start_command));
			}
			else
			{
				Boolean purgeServiceStopped = purgeServiceManager.stopService(context);
				
				if(purgeServiceStopped == true)
				{
					Log.d("Preferences", "onSharedPreferencePurgeActivate : " + context.getString(R.string.log_purge_service_destroy));
				}
				else
				{
					Log.w("Preferences", "onSharedPreferencePurgeActivate : " + context.getString(R.string.log_purge_service_error_destroy));
				}
			}
		}
		catch (Exception e)
		{
			Log.w("Preferences", "onSharedPreferencePurgeActivate : " + context.getString(R.string.log_preferences_error_purge_activate_changed) + " : " + e);
		}
	}
	
	private void onSharedPreferencePurgeFrequencyChanged()
	{
		try
		{
			Preference preferencesPurgeFrequency = findPreference("preferences_purge_frequency");
			if (preferencesPurgeFrequency instanceof ListPreference)
			{
				ListPreference listPreferencesPurgeFrequency = (ListPreference) preferencesPurgeFrequency;
				preferencesPurgeFrequencyValue = (String) listPreferencesPurgeFrequency.getEntry();
				preferencesPurgeFrequency.setSummary(getString(R.string.preferences_purge_frequency_summary) + " " + listPreferencesPurgeFrequency.getEntry());
				
				Log.d("Preferences", "onSharedPreferencePurgeFrequencyChanged : " + getApplicationContext().getString(R.string.log_preferences_purge_frequency_changed) + " : " + listPreferencesPurgeFrequency.getEntry());
				
				if(isInit == true)
				{
					databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_purge_frequency_changed) + " : " + listPreferencesPurgeFrequency.getEntry(), new Date().getTime(), 3, false);
				}
			}
		}
		catch (Exception e)
		{
			Log.w("Preferences", "onSharedPreferencePurgeFrequencyChanged : " + getApplicationContext().getString(R.string.log_preferences_error_purge_frequency_changed) + " : " + e);
			
			if(isInit == true)
			{
				databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_error_purge_frequency_changed), new Date().getTime(), 2, false);
			}
		}
	}
	
	public static String getSharedPreferencePurgeFrequency()
	{
		return preferencesPurgeFrequencyValue;
	}
	
	private void onSharedPreferencePurgeRetentionChanged()
	{
		try
		{
			Preference preferencesPurgeRetention = findPreference("preferences_purge_retention");
			if (preferencesPurgeRetention instanceof ListPreference)
			{
				ListPreference listPreferencesPurgeRetention = (ListPreference) preferencesPurgeRetention;
				preferencesPurgeRetentionValue = (String) listPreferencesPurgeRetention.getEntry();
				preferencesPurgeRetention.setSummary(getString(R.string.preferences_purge_retention_summary) + " " + listPreferencesPurgeRetention.getEntry());
				
				Log.d("Preferences", "onSharedPreferencePurgeRetentionChanged : " + getApplicationContext().getString(R.string.log_preferences_purge_retention_changed) + " : " + listPreferencesPurgeRetention.getEntry());
				
				if(isInit == true)
				{
					databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_purge_retention_changed) + " : " + listPreferencesPurgeRetention.getEntry(), new Date().getTime(), 3, false);
				}
			}
		}
		catch (Exception e)
		{
			Log.w("Preferences", "onSharedPreferencePurgeRetentionChanged : " + getApplicationContext().getString(R.string.log_preferences_error_purge_retention_changed) + " : " + e);
			
			if(isInit == true)
			{
				databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_error_purge_retention_changed), new Date().getTime(), 2, false);
			}
		}
	}
	
	public static String getSharedPreferencePurgeRetention()
	{
		return preferencesPurgeRetentionValue;
	}
	
	private void onSharedPreferenceLogsActivateChanged(SharedPreferences sharedPreferences)
	{
		try
		{
			boolean isPreferencesLogsActivate = sharedPreferences.getBoolean("preferences_logs_activate", true);
			
			if(isPreferencesLogsActivate == true)
			{
				Log.d("Preferences", "onSharedPreferenceLogsActivateChanged : " + getApplicationContext().getString(R.string.log_preferences_logs_activate_changed_start));
				
				if(isInit == true)
				{
					databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_logs_activate_changed_start), new Date().getTime(), 3, false);
				}
			}
			else
			{
				Log.d("Preferences", "onSharedPreferenceLogsActivateChanged : " + getApplicationContext().getString(R.string.log_preferences_logs_activate_changed_stop));
				
				if(isInit == true)
				{
					databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_logs_activate_changed_stop), new Date().getTime(), 3, true);
				}
			}
		}
		catch (Exception e)
		{
			Log.w("Preferences", "onSharedPreferenceLogsActivateChanged : " + getApplicationContext().getString(R.string.log_preferences_error_logs_activate_changed) + " : " + e);
			
			if(isInit == true)
			{
				databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_error_logs_activate_changed), new Date().getTime(), 2, false);
			}
		}
	}
	
	private void onSharedPreferenceNotificationsActivateChanged(SharedPreferences sharedPreferences)
	{
		try
		{
			boolean isPreferencesNotificationsActivate = sharedPreferences.getBoolean("preferences_notifications_activate", true);
			
			if(isPreferencesNotificationsActivate == true)
			{
				Log.d("Preferences", "onSharedPreferenceNotificationsActivateChanged : " + getApplicationContext().getString(R.string.log_preferences_notifications_activate_changed_start));
				
				if(isInit == true)
				{
					databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_notifications_activate_changed_start), new Date().getTime(), 3, false);
				}
			}
			else
			{
				Log.d("Preferences", "onSharedPreferenceNotificationsActivateChanged : " + getApplicationContext().getString(R.string.log_preferences_notifications_activate_changed_stop));
				
				if(isInit == true)
				{
					databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_notifications_activate_changed_stop), new Date().getTime(), 3, false);
				}
			}
			
			getPreferenceScreen().findPreference("preferences_notifications_sound_activate").setEnabled(isPreferencesNotificationsActivate);
			getPreferenceScreen().findPreference("preferences_notifications_vibrate_activate").setEnabled(isPreferencesNotificationsActivate);
			getPreferenceScreen().findPreference("preferences_notifications_led_activate").setEnabled(isPreferencesNotificationsActivate);
		}
		catch (Exception e)
		{
			Log.w("Preferences", "onSharedPreferenceNotificationsActivateChanged : " + getApplicationContext().getString(R.string.log_preferences_error_notifications_activate_changed) + " : " + e);
			
			if(isInit == true)
			{
				databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_error_notifications_activate_changed), new Date().getTime(), 2, false);
			}
		}
	}
	
	private void onSharedPreferenceNotificationsSoundActivateChanged(SharedPreferences sharedPreferences)
	{
		try
		{
			boolean isPreferencesNotificationsSoundActivate = sharedPreferences.getBoolean("preferences_notifications_sound_activate", true);
			
			if(isPreferencesNotificationsSoundActivate == true)
			{
				Log.d("Preferences", "onSharedPreferenceNotificationsSoundActivateChanged : " + getApplicationContext().getString(R.string.log_preferences_notifications_sound_activate_changed_start));
				
				if(isInit == true)
				{
					databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_notifications_sound_activate_changed_start), new Date().getTime(), 3, false);	
				}
			}
			else
			{
				Log.d("Preferences", "onSharedPreferenceNotificationsSoundActivateChanged : " + getApplicationContext().getString(R.string.log_preferences_notifications_sound_activate_changed_stop));
				
				if(isInit == true)
				{
					databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_notifications_sound_activate_changed_stop), new Date().getTime(), 3, false);
				}
			}
		}
		catch (Exception e)
		{
			Log.w("Preferences", "onSharedPreferenceNotificationsSoundActivateChanged : " + getApplicationContext().getString(R.string.log_preferences_error_notifications_sound_activate_changed) + " : " + e);
			
			if(isInit == true)
			{
				databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_error_notifications_sound_activate_changed), new Date().getTime(), 2, false);
			}
		}
	}
	
	private void onSharedPreferenceNotificationsVibrateActivateChanged(SharedPreferences sharedPreferences)
	{
		try
		{
			boolean isPreferencesNotificationsVibrateActivate = sharedPreferences.getBoolean("preferences_notifications_vibrate_activate", true);
			
			if(isPreferencesNotificationsVibrateActivate == true)
			{
				Log.d("Preferences", "onSharedPreferenceNotificationsVibrateActivateChanged : " + getApplicationContext().getString(R.string.log_preferences_notifications_vibrate_activate_changed_start));
				
				if(isInit == true)
				{
					databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_notifications_vibrate_activate_changed_start), new Date().getTime(), 3, false);	
				}
			}
			else
			{
				Log.d("Preferences", "onSharedPreferenceNotificationsVibrateActivateChanged : " + getApplicationContext().getString(R.string.log_preferences_notifications_vibrate_activate_changed_stop));
				
				if(isInit == true)
				{
					databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_notifications_vibrate_activate_changed_stop), new Date().getTime(), 3, false);
				}
			}
		}
		catch (Exception e)
		{
			Log.w("Preferences", "onSharedPreferenceNotificationsVibrateActivateChanged : " + getApplicationContext().getString(R.string.log_preferences_error_notifications_vibrate_activate_changed) + " : " + e);
			
			if(isInit == true)
			{
				databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_error_notifications_vibrate_activate_changed), new Date().getTime(), 2, false);
			}
		}
	}
	
	private void onSharedPreferenceNotificationsLedActivateChanged(SharedPreferences sharedPreferences)
	{
		try
		{
			boolean isPreferencesNotificationsLedActivate = sharedPreferences.getBoolean("preferences_notifications_led_activate", true);
			
			if(isPreferencesNotificationsLedActivate == true)
			{
				Log.d("Preferences", "onSharedPreferenceNotificationsLedActivateChanged : " + getApplicationContext().getString(R.string.log_preferences_notifications_led_activate_changed_start));
				
				if(isInit == true)
				{
					databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_notifications_led_activate_changed_start), new Date().getTime(), 3, false);	
				}
			}
			else
			{
				Log.d("Preferences", "onSharedPreferenceNotificationsLedActivateChanged : " + getApplicationContext().getString(R.string.log_preferences_notifications_led_activate_changed_stop));
				
				if(isInit == true)
				{
					databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_notifications_led_activate_changed_stop), new Date().getTime(), 3, false);
				}
			}
		}
		catch (Exception e)
		{
			Log.w("Preferences", "onSharedPreferenceNotificationsLedActivateChanged : " + getApplicationContext().getString(R.string.log_preferences_error_notifications_led_activate_changed) + " : " + e);
			
			if(isInit == true)
			{
				databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_error_notifications_led_activate_changed), new Date().getTime(), 2, false);
			}
		}
	}

	private void onSharedPreferenceToastMessagesActivateChanged(SharedPreferences sharedPreferences)
	{
		try
		{
			boolean isPreferencesToastMessagesActivate = sharedPreferences.getBoolean("preferences_toast_messages_activate", true);
			
			if(isPreferencesToastMessagesActivate == true)
			{
				Log.d("Preferences", "onSharedPreferenceToastMessagesActivateChanged : " + getApplicationContext().getString(R.string.log_preferences_toast_messages_activate_changed_start));
				
				if(isInit == true)
				{
					databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_toast_messages_activate_changed_start), new Date().getTime(), 3, false);	
				}
			}
			else
			{
				Log.d("Preferences", "onSharedPreferenceToastMessagesActivateChanged : " + getApplicationContext().getString(R.string.log_preferences_toast_messages_activate_changed_stop));
				
				if(isInit == true)
				{
					databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_toast_messages_activate_changed_stop), new Date().getTime(), 3, false);
				}
			}
		}
		catch (Exception e)
		{
			Log.w("Preferences", "onSharedPreferenceToastMessagesActivateChanged : " + getApplicationContext().getString(R.string.log_preferences_error_toast_messages_activate_changed) + " : " + e);
			
			if(isInit == true)
			{
				databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_error_toast_messages_activate_changed), new Date().getTime(), 2, false);
			}
		}
	}
	
	private void onSharedPreferenceLocaleSetChanged()
	{
		try
		{
			Preference preferencesLocaleSet = findPreference("preferences_locale_set");
			if (preferencesLocaleSet instanceof ListPreference)
			{
				ListPreference listPreferencesLocaleSet = (ListPreference) preferencesLocaleSet;
				preferencesLocaleSetValue = (String) listPreferencesLocaleSet.getEntry();
				preferencesLocaleSet.setSummary(getString(R.string.preferences_locale_set_summary) + " " + listPreferencesLocaleSet.getEntry());
				
				Log.d("Preferences", "onSharedPreferenceLocaleSetChanged : " + getApplicationContext().getString(R.string.log_preferences_theme_set_changed) + " : " + listPreferencesLocaleSet.getEntry());
								
				if(isInit == true)
				{
					databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_locale_set_changed) + " : " + listPreferencesLocaleSet.getEntry(), new Date().getTime(), 3, false);
					
					localizerManager.setPreferencesLocale(getApplicationContext());
					
					Intent restartApplicationIntent = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
					restartApplicationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					restartApplicationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					finish();
					startActivity(restartApplicationIntent);
				}
			}
		}
		catch (Exception e)
		{
			Log.w("Preferences", "onSharedPreferenceLocaleSetChanged : " + getApplicationContext().getString(R.string.log_preferences_error_locale_set_changed) + " : " + e);
			
			if(isInit == true)
			{
				databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_error_locale_set_changed), new Date().getTime(), 2, false);
			}
		}
	}
	
	public static String getSharedPreferenceLocaleSet()
	{
		return preferencesLocaleSetValue;
	}
	
	private void onSharedPreferenceThemeSetChanged()
	{
		try
		{
			Preference preferencesThemeSet = findPreference("preferences_theme_set");
			if (preferencesThemeSet instanceof ListPreference)
			{
				ListPreference listPreferencesThemeSet = (ListPreference) preferencesThemeSet;
				preferencesThemeSetValue = (String) listPreferencesThemeSet.getEntry();
				preferencesThemeSet.setSummary(getString(R.string.preferences_theme_set_summary) + " " + listPreferencesThemeSet.getEntry());
				
				Log.d("Preferences", "onSharedPreferenceThemeSetChanged : " + getApplicationContext().getString(R.string.log_preferences_theme_set_changed) + " : " + listPreferencesThemeSet.getEntry());
								
				if(isInit == true)
				{
					databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_theme_set_changed) + " : " + listPreferencesThemeSet.getEntry(), new Date().getTime(), 3, false);
				
					Intent restartApplicationIntent = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
					restartApplicationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					restartApplicationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					finish();
					startActivity(restartApplicationIntent);
				}
			}
		}
		catch (Exception e)
		{
			Log.w("Preferences", "onSharedPreferenceThemeSetChanged : " + getApplicationContext().getString(R.string.log_preferences_error_theme_set_changed) + " : " + e);
			
			if(isInit == true)
			{
				databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_error_theme_set_changed), new Date().getTime(), 2, false);
			}
		}
	}
	
	public static String getSharedPreferenceThemeSet()
	{
		return preferencesThemeSetValue;
	}
	
	private void onSharedPreferenceColorThemeSetChanged()
	{
		try
		{
			Preference preferencesColorThemeSet = findPreference("preferences_color_theme_set");
			if (preferencesColorThemeSet instanceof ListPreference)
			{
				ListPreference listPreferencesColorThemeSet = (ListPreference) preferencesColorThemeSet;
				preferencesColorThemeSetValue = (String) listPreferencesColorThemeSet.getEntry();
				preferencesColorThemeSet.setSummary(getString(R.string.preferences_color_theme_set_summary) + " " + listPreferencesColorThemeSet.getEntry());
				
				Log.d("Preferences", "onSharedPreferenceColorThemeSetChanged : " + getApplicationContext().getString(R.string.log_preferences_color_theme_set_changed) + " : " + listPreferencesColorThemeSet.getEntry());
								
				if(isInit == true)
				{
					databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_color_theme_set_changed) + " : " + listPreferencesColorThemeSet.getEntry(), new Date().getTime(), 3, false);
				
					Intent restartApplicationIntent = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
					restartApplicationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					restartApplicationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					finish();
					startActivity(restartApplicationIntent);
				}
			}
		}
		catch (Exception e)
		{
			Log.w("Preferences", "onSharedPreferenceColorThemeSetChanged : " + getApplicationContext().getString(R.string.log_preferences_error_color_theme_set_changed) + " : " + e);
			
			if(isInit == true)
			{
				databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_error_color_theme_set_changed), new Date().getTime(), 2, false);
			}
		}
	}
	
	public static String getSharedPreferenceColorThemeSet()
	{
		return preferencesColorThemeSetValue;
	}
	
	private void onSharedPreferenceSplashscreenActivateChanged(SharedPreferences sharedPreferences)
	{
		try
		{
			boolean isPreferencesSplashscreenActivate = sharedPreferences.getBoolean("preferences_splashscreen_activate", true);
			
			if(isPreferencesSplashscreenActivate == true)
			{
				Log.d("Preferences", "onSharedPreferenceSplashscreenActivateChanged : " + getApplicationContext().getString(R.string.log_preferences_splashscreen_activate_changed_checked));
				
				if(isInit == true)
				{
					databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_splashscreen_activate_changed_checked), new Date().getTime(), 3, false);	
				}
			}
			else
			{
				Log.d("Preferences", "onSharedPreferenceSplashscreenActivateChanged : " + getApplicationContext().getString(R.string.log_preferences_splashscreen_activate_changed_not_checked));
				
				if(isInit == true)
				{
					databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_splashscreen_activate_changed_not_checked), new Date().getTime(), 3, false);
				}
			}
		}
		catch (Exception e)
		{
			Log.w("Preferences", "onSharedPreferenceSplashscreenActivateChanged : " + getApplicationContext().getString(R.string.log_preferences_error_splashscreen_activate_changed) + " : " + e);
			
			if(isInit == true)
			{
				databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_error_splashscreen_activate_changed), new Date().getTime(), 2, false);
			}
		}
	}
	
	public static boolean getSharedPreferenceSplashscreen(Context context)
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean sharedPreferencesSplashscreenValue = sharedPreferences.getBoolean("preferences_splashscreen_activate", true);
		
		return sharedPreferencesSplashscreenValue;
	}

	@Override
	public void onSupportActionModeFinished(ActionMode arg0)
	{
	}

	@Override
	public void onSupportActionModeStarted(ActionMode arg0)
	{
	}

	@Override
	public ActionMode onWindowStartingSupportActionMode(Callback arg0)
	{
		return null;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item)
	{
        int id = item.getItemId();
        
        switch(id)
        {
        	case android.R.id.home :
        		super.onBackPressed();
        		break;
        }
        
        return super.onOptionsItemSelected(item);
    }
}