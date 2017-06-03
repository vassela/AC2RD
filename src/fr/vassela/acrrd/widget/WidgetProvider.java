/**
 * @file WidgetProvider.java
 * @brief Widget provider class
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

package fr.vassela.acrrd.widget;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import fr.vassela.acrrd.R;
import fr.vassela.acrrd.database.DatabaseManager;
import fr.vassela.acrrd.monitoring.MonitoringManager;
import fr.vassela.acrrd.recorder.RecordServiceManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

public class WidgetProvider extends AppWidgetProvider
{
	public static String UPDATE_WIDGET = "fr.vassela.acrrd.widget.act.UPDATEWIDGET";
	public static String START_RECORD = "fr.vassela.acrrd.widget.act.STARTRECORD";
	public static String STOP_RECORD = "fr.vassela.acrrd.widget.act.STOPRECORD";
	public static String SHOW_RECORDS = "fr.vassela.acrrd.act.SHOWRECORDS";
	
	private static DatabaseManager databaseManager = new DatabaseManager();
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		super.onReceive(context, intent);
		
		if (UPDATE_WIDGET.equals(intent.getAction()))
		{
			try
			{
				ComponentName componentName = new ComponentName(context.getPackageName(), getClass().getName());
			    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			    int appWidgetIds[] = appWidgetManager.getAppWidgetIds(componentName);
			    
			    final int appWidgetIdsLength = appWidgetIds.length;
				
				for (int i = 0; i < appWidgetIdsLength; i++)
			    {
					int appWidgetId = appWidgetIds[i];
					
			    	updateAppWidget(context, appWidgetManager, appWidgetId);
			    }
			}
			catch (Exception e)
			{
				Log.w("WidgetProvider", "onReceive : " + context.getString(R.string.log_widget_provider_error_on_receive_update_widget) + " : " + e);
				databaseManager.insertLog(context, "" + context.getString(R.string.log_widget_provider_error_on_receive_update_widget), new Date().getTime(), 2, false);
			}
		}
		
		if (START_RECORD.equals(intent.getAction()))
		{
			try
			{
				RecordServiceManager recordServiceManager = new RecordServiceManager();
				recordServiceManager.startService(context, 2, "MICROPHONE");
			}
			catch (Exception e)
			{
				Log.e("WidgetProvider", "onReceive : " + context.getString(R.string.log_widget_provider_error_on_receive_start_record) + " : " + e);
				databaseManager.insertLog(context, "" + context.getString(R.string.log_widget_provider_error_on_receive_start_record), new Date().getTime(), 1, false);
			}
		}
		else if (STOP_RECORD.equals(intent.getAction()))
		{
			try
			{
				RecordServiceManager recordServiceManager = new RecordServiceManager();
				recordServiceManager.stopService(context);
			}
			catch (Exception e)
			{
				Log.e("WidgetProvider", "onReceive : " + context.getString(R.string.log_widget_provider_error_on_receive_stop_record) + " : " + e);
				databaseManager.insertLog(context, "" + context.getString(R.string.log_widget_provider_error_on_receive_stop_record), new Date().getTime(), 1, false);
			}
		}
	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		try
		{
			final int appWidgetIdsLength = appWidgetIds.length;
			
			for (int i = 0; i < appWidgetIdsLength; i++)
			{
				int appWidgetId = appWidgetIds[i];
	
				updateAppWidget(context, appWidgetManager, appWidgetId);
			}
		}
		catch (Exception e)
		{
			Log.w("WidgetProvider", "onUpdate : " + context.getString(R.string.log_widget_provider_error_on_update) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_widget_provider_error_on_update), new Date().getTime(), 2, false);
		}
	}
	
	@Override 
	public void onEnabled(Context context)
	{
		try
		{
			super.onEnabled(context);
			
			AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	        Calendar calendar = Calendar.getInstance();
	        calendar.setTimeInMillis(System.currentTimeMillis());
	        calendar.add(Calendar.SECOND, 1);
	        alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), 1000, createUpdateWidgetPendingIntent(context));
		}
		catch (Exception e)
		{
			Log.e("WidgetProvider", "onEnabled : " + context.getString(R.string.log_widget_provider_error_on_enabled) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_widget_provider_error_on_enabled), new Date().getTime(), 1, false);
		}
	}
	
	@Override
	public void onDisabled(Context context)
	{
		try
		{
			super.onDisabled(context);
			
			AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	        alarmManager.cancel(createUpdateWidgetPendingIntent(context));
		}
		catch (Exception e)
		{
			Log.e("WidgetProvider", "onDisabled : " + context.getString(R.string.log_widget_provider_error_on_disabled) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_widget_provider_error_on_disabled), new Date().getTime(), 1, false);
		}
	}
	
	protected static PendingIntent getPendingIntent(Context context, Intent intent)
	{
		try
		{
		    return PendingIntent.getActivity(context, 0, intent, 0);
		}
		catch (Exception e)
		{
			return null;
		}
	}

	protected static PendingIntent getPendingSelfIntent(Context context, Intent intent)
	{
		try
		{
		    return PendingIntent.getBroadcast(context, 0, intent, 0);
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	protected static PendingIntent getPendingSelfIntent(Context context, String action)
	{
		try
		{
		    Intent intent = new Intent(context, WidgetProvider.class);
		    intent.setAction(action);
		    return PendingIntent.getBroadcast(context, 0, intent, 0);
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	private PendingIntent createUpdateWidgetPendingIntent(Context context)
	{
		try
		{
			Intent intent = new Intent(UPDATE_WIDGET);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			return pendingIntent;
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
	{
		try
		{
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(),	R.layout.widget);
			
			MonitoringManager monitoringManager = new MonitoringManager();
			boolean recordServiceState = monitoringManager.getRecordServiceState(context);
			
			String recorderState = context.getString(R.string.widget_telephone_call_recorder_state_off);
			
			float storageTotalSize = monitoringManager.getStorageTotalSizeInMo(context);
    		float storageAvailableSize = monitoringManager.getStorageAvailableSizeInMo(context);
    		float storageAvailableSpaceInPercent = (storageAvailableSize * 100) / storageTotalSize;
    		
    		String storageState = null;
    		
    		if(storageAvailableSize > 2048)
    		{
    			storageAvailableSize = storageAvailableSize / 1024;
    			DecimalFormat decimalFormat = new DecimalFormat("#####.##");
    			storageState = ""+context.getString(R.string.widget_storage_state)+" : "+(int) storageAvailableSpaceInPercent+"% ("+ decimalFormat.format(storageAvailableSize) +"GB)";
    		}
    		else
    		{
    			DecimalFormat decimalFormat = new DecimalFormat("#####.##");
    			storageState = ""+context.getString(R.string.widget_storage_state)+" : "+(int) storageAvailableSpaceInPercent+"% ("+ decimalFormat.format(storageAvailableSize) +"MB)";
    		}
    		
    		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    		boolean isPreferencesRecordActivated = sharedPreferences.getBoolean("preferences_record_activate",false);
    		
			if(recordServiceState == true)
	    	{
				int recordType = monitoringManager.getRecordType(context);
				
				switch(recordType)
				{
					case 0:
						recorderState = context.getString(R.string.widget_telephone_call_recorder_state_on_incoming_call);
						break;
						
					case 1:
						recorderState = context.getString(R.string.widget_telephone_call_recorder_state_on_outgoing_call);
						break;

					case 2:
						recorderState = context.getString(R.string.widget_telephone_call_recorder_state_on_microphone);
						break;
				}
				
				remoteViews.setOnClickPendingIntent(R.id.widget_button_start_record, null);
				remoteViews.setOnClickPendingIntent(R.id.widget_button_stop_record, getPendingSelfIntent(context, STOP_RECORD));
				Intent intent = new Intent(SHOW_RECORDS);
				remoteViews.setOnClickPendingIntent(R.id.widget_button_playlist, getPendingIntent(context, intent));
				
				remoteViews.setTextViewText(R.id.widget_telephone_call_recorder_state_on, recorderState);
				remoteViews.setViewVisibility(R.id.widget_telephone_call_recorder_state_off, View.GONE);
				remoteViews.setViewVisibility(R.id.widget_telephone_call_recorder_state_on, View.VISIBLE);
				remoteViews.setViewVisibility(R.id.widget_button_start_record, View.GONE);
				remoteViews.setViewVisibility(R.id.widget_button_start_record_disabled, View.VISIBLE);
				
				if(recordType == 2)
	    		{
					remoteViews.setViewVisibility(R.id.widget_button_stop_record, View.VISIBLE);
					remoteViews.setViewVisibility(R.id.widget_button_stop_record_disabled, View.GONE);
	    		}
				else
				{
					remoteViews.setViewVisibility(R.id.widget_button_stop_record, View.GONE);
					remoteViews.setViewVisibility(R.id.widget_button_stop_record_disabled, View.VISIBLE);
				}
	    	}
			else
			{
				remoteViews.setOnClickPendingIntent(R.id.widget_button_start_record, getPendingSelfIntent(context, START_RECORD));
				remoteViews.setOnClickPendingIntent(R.id.widget_button_stop_record, null);
				Intent intent = new Intent(SHOW_RECORDS);
				remoteViews.setOnClickPendingIntent(R.id.widget_button_playlist, getPendingIntent(context, intent));
				
				remoteViews.setTextViewText(R.id.widget_telephone_call_recorder_state_off, recorderState);
				remoteViews.setViewVisibility(R.id.widget_telephone_call_recorder_state_off, View.VISIBLE);
				remoteViews.setViewVisibility(R.id.widget_telephone_call_recorder_state_on, View.GONE);
				remoteViews.setViewVisibility(R.id.widget_button_start_record, View.VISIBLE);
				remoteViews.setViewVisibility(R.id.widget_button_start_record_disabled, View.GONE);
				remoteViews.setViewVisibility(R.id.widget_button_stop_record, View.GONE);
				remoteViews.setViewVisibility(R.id.widget_button_stop_record_disabled, View.VISIBLE);
			}
			
			remoteViews.setTextViewText(R.id.widget_storage_state, storageState);
			remoteViews.setTextViewText(R.id.widget_storage_state_low, storageState);
			remoteViews.setTextViewText(R.id.widget_storage_state_very_low, storageState);
			
			// storage state
			if(storageAvailableSpaceInPercent < 20 && storageAvailableSpaceInPercent > 10)
			{
				remoteViews.setViewVisibility(R.id.widget_storage_state, View.GONE);
				remoteViews.setViewVisibility(R.id.widget_storage_state_low, View.VISIBLE);
				remoteViews.setViewVisibility(R.id.widget_storage_state_very_low, View.GONE);
			}
			else if(storageAvailableSpaceInPercent <= 10)
			{
				remoteViews.setViewVisibility(R.id.widget_storage_state, View.GONE);
				remoteViews.setViewVisibility(R.id.widget_storage_state_low, View.GONE);
				remoteViews.setViewVisibility(R.id.widget_storage_state_very_low, View.VISIBLE);
			}
			else
			{
				remoteViews.setViewVisibility(R.id.widget_storage_state, View.VISIBLE);
				remoteViews.setViewVisibility(R.id.widget_storage_state_low, View.GONE);
				remoteViews.setViewVisibility(R.id.widget_storage_state_very_low, View.GONE);
			}
			
			// record service state
			if(isPreferencesRecordActivated == true)
			{
				remoteViews.setTextViewText(R.id.widget_recorder_service_state_on, context.getString(R.string.widget_recorder_service_state_on));
				remoteViews.setViewVisibility(R.id.widget_recorder_service_state_on, View.VISIBLE);
				remoteViews.setViewVisibility(R.id.widget_recorder_service_state_off, View.GONE);
			}
			else
			{
				remoteViews.setTextViewText(R.id.widget_recorder_service_state_off, context.getString(R.string.widget_recorder_service_state_off));
				remoteViews.setViewVisibility(R.id.widget_recorder_service_state_on, View.GONE);
				remoteViews.setViewVisibility(R.id.widget_recorder_service_state_off, View.VISIBLE);
			}
	
			appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
		}
		catch (Exception e)
		{
			Log.w("WidgetProvider", "updateAppWidget : " + context.getString(R.string.log_widget_provider_error_update_app_widget) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_widget_provider_error_update_app_widget), new Date().getTime(), 2, false);
		}
	}
}