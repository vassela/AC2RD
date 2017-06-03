/**
 * @file MonitoringManager.java
 * @brief Monitoring manager class
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

package fr.vassela.acrrd.monitoring;

import java.util.Date;
import fr.vassela.acrrd.R;
import fr.vassela.acrrd.database.DatabaseManager;
import fr.vassela.acrrd.main.Preferences;
import fr.vassela.acrrd.purge.PurgeServiceManager;
import fr.vassela.acrrd.receiver.TelephoneCallReceiver;
import fr.vassela.acrrd.recorder.RecordServiceManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class MonitoringManager extends Application
{
	private static String storagePath;
	private StatFs statFS;
	
	private DatabaseManager databaseManager = new DatabaseManager();
	
	public String getTelephoneCallState(Context context)
	{
		try
		{
			int  currentState = TelephoneCallReceiver.getCurrentState();
			String telephoneCallState = "";
			
			switch (currentState)
		    {
		    	case TelephonyManager.CALL_STATE_IDLE:
		    		telephoneCallState = context.getString(R.string.monitoring_telephone_call_state_idle);
		    		break;
		    	
		    	case TelephonyManager.CALL_STATE_RINGING:
		    		telephoneCallState = context.getString(R.string.monitoring_telephone_call_state_ringing);
		    		break;
		    		
		    	case TelephonyManager.CALL_STATE_OFFHOOK:
		    		telephoneCallState = context.getString(R.string.monitoring_telephone_call_state_offhook);
		    		break;
		    }
			
			return telephoneCallState;
		}
		catch (Exception e)
        {
			return "";
        }
	}
	
	public boolean getRecordActivateState(Context context)
	{
		try
		{
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			boolean isPreferencesRecordActivated = sharedPreferences.getBoolean("preferences_record_activate",false);
			
			if(isPreferencesRecordActivated == true)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		catch (Exception e)
        {
			return false;
        }
	}
	
	public boolean getRecordServiceState(Context context)
	{
		try
		{
			RecordServiceManager recordServiceManager = new RecordServiceManager();
			boolean isRecordServiceRunning = recordServiceManager.isRunning(context);
			
			if(isRecordServiceRunning == true)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		catch (Exception e)
        {
			return false;
        }
	}
	
	public int getRecordType(Context context)
	{
		try
		{
			RecordServiceManager recordServiceManager = new RecordServiceManager();
			int callDirection = recordServiceManager.getCallDirection(context);
			
			return callDirection;

		}
		catch (Exception e)
        {
			return 2;
        }
	}
	
	public String getAudioSourceState(Context context)
	{
		try
		{
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			int audioSource = Integer.parseInt(sharedPreferences.getString("preferences_audio_source", "1"));
			String audioSourceState = "";
			
			switch (audioSource)
		    {
		    	case 1:
		    		audioSourceState = context.getString(R.string.preferences_audio_source_list_entries_1);
		    		break;
		    	case 2:
		    		audioSourceState = context.getString(R.string.preferences_audio_source_list_entries_2);
		    		break;
		    	case 3:
		    		audioSourceState = context.getString(R.string.preferences_audio_source_list_entries_3);
		    		break;
		    	case 4:
		    		audioSourceState = context.getString(R.string.preferences_audio_source_list_entries_4);
		    		break;
		    	case 7:
		    		audioSourceState = context.getString(R.string.preferences_audio_source_list_entries_7);
		    		break;
		    }
			
			return audioSourceState;
		}
		catch (Exception e)
        {
			return "";
        }
	}
	
	public String getStoragePath(Context context)
	{
		try
		{
			if ((Preferences.getSharedStoragePath(context) == "") || (Preferences.getSharedStoragePath(context) == null))
	    	{
	        	storagePath = String.valueOf(Environment.getExternalStorageDirectory());	
	    	}
	        else
	        {
	        	storagePath = Preferences.getSharedStoragePath(context);
	        }
			
			return storagePath;
		}
		catch (Exception e)
        {
			Log.w("MonitoringManager", "getStoragePath : " + context.getString(R.string.log_monitoring_manager_error_get_storage_path) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_monitoring_manager_error_get_storage_path), new Date().getTime(), 2, false);
			
			return "";
        }
	}
	
	private long getStorageTotalSize(Context context)
    {
		try
		{
			statFS = new StatFs(storagePath);
			
			long storageTotalBlock = (long)statFS.getBlockSize() * (long)statFS.getBlockCount();
			
			statFS = null;
			
			return storageTotalBlock;
		}
		catch (Exception e)
        {
			Log.w("MonitoringManager", "getStorageTotalSize : " + context.getString(R.string.log_monitoring_manager_error_get_storage_total_size) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_monitoring_manager_error_get_storage_total_size), new Date().getTime(), 2, false);
			
			return 0;
        }
    }
	
	private long getStorageAvailableSize(Context context)
    {
		try
		{
			statFS = new StatFs(storagePath);
			
			long storageAvailableBlocks = (long)statFS.getBlockSize() * (long)statFS.getAvailableBlocks();
			
			statFS = null;
			
			return storageAvailableBlocks;
		}
		catch (Exception e)
        {
			Log.w("MonitoringManager", "getStorageAvailableSize : " + context.getString(R.string.log_monitoring_manager_error_get_storage_available_size) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_monitoring_manager_error_get_storage_available_size), new Date().getTime(), 2, false);
			
			return 0;
        }
    }
	
	public float getStorageTotalSizeInMo(Context context)
    {
		try
		{
			long storageTotalBlocks = getStorageTotalSize(context);
			
			float storageTotalBlocksInMo = storageTotalBlocks / (1024.f * 1024.f);
			
			return storageTotalBlocksInMo;
		}
		catch (Exception e)
        {
			Log.w("MonitoringManager", "getStorageTotalSizeInMo : " + context.getString(R.string.log_monitoring_manager_error_get_storage_total_size) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_monitoring_manager_error_get_storage_total_size), new Date().getTime(), 2, false);
			
			return 0;
        }
    }
	
	public float getStorageAvailableSizeInMo(Context context)
    {
		try
		{
			long storageAvailableBlocks = getStorageAvailableSize(context);
			
			float storageAvailableBlocksInMo = storageAvailableBlocks / (1024.f * 1024.f);
			
			return storageAvailableBlocksInMo;
		}
		catch (Exception e)
        {
			Log.w("MonitoringManager", "getStorageAvailableSizeInMo : " + context.getString(R.string.log_monitoring_manager_error_get_storage_available_size) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_monitoring_manager_error_get_storage_available_size), new Date().getTime(), 2, false);
			
			return 0;
        }
    }
	
	public float getStorageTotalSizeInGo(Context context)
    {
		try
		{
			long storageTotalBlocks = getStorageTotalSize(context);
			
			float storageTotalBlocksInGo = storageTotalBlocks / (1024.f * 1024.f * 1024.f);
			
			return storageTotalBlocksInGo;
		}
		catch (Exception e)
        {
			Log.w("MonitoringManager", "getStorageTotalSizeInGo : " + context.getString(R.string.log_monitoring_manager_error_get_storage_total_size) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_monitoring_manager_error_get_storage_total_size), new Date().getTime(), 2, false);
			
			return 0;
        }
    }
	
	public float getStorageAvailableSizeInGo(Context context)
    {
		try
		{
			long storageAvailableBlocks = getStorageAvailableSize(context);
			
			float storageAvailableBlocksInGo = storageAvailableBlocks / (1024.f * 1024.f * 1024.f);
			
			return storageAvailableBlocksInGo;
		}
		catch (Exception e)
        {
			Log.w("MonitoringManager", "getStorageAvailableSizeInGo : " + context.getString(R.string.log_monitoring_manager_error_get_storage_available_size) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_monitoring_manager_error_get_storage_available_size), new Date().getTime(), 2, false);
			
			return 0;
        }
    }
	
	public float getStoragePathAvailableSpaceInPercent(Context context)
	{
		try
		{
			getStoragePath(context);
			
			float storageTotalSize = getStorageTotalSizeInMo(context);
    		float storageAvailableSize = getStorageAvailableSizeInMo(context);
    		float storageAvailableSpaceInPercent = (storageAvailableSize * 100) / storageTotalSize;
    		
    		return storageAvailableSpaceInPercent;
		}
		catch (Exception e)
        {
			return 0;
        }
	}
	
	public boolean getPurgeServiceState(Context context)
	{
		try
		{
			PurgeServiceManager purgeServiceManager = new PurgeServiceManager();
			boolean isPurgeServiceRunning = purgeServiceManager.isRunning(context);
			
			if(isPurgeServiceRunning == true)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		catch (Exception e)
        {
			return false;
        }
	}
	
	public String getMonitoringServiceState(Context context)
	{
		try
		{
			MonitoringServiceManager monitoringServiceManager = new MonitoringServiceManager();
			boolean isMonitoringServiceRunning = monitoringServiceManager.isRunning(context);
			boolean isMonitoringServiceMustPurge = monitoringServiceManager.mustPurge(context);
			boolean isMonitoringServiceCanRecord = monitoringServiceManager.canRecord(context);
			
			String monitoringServiceState = "";
			
			if(isMonitoringServiceRunning == true)
			{
				monitoringServiceState = monitoringServiceState + "isMonitoringServiceRunning = true";
			}
			else
			{
				monitoringServiceState = monitoringServiceState + "isMonitoringServiceRunning = false";
			}
			
			if(isMonitoringServiceMustPurge == true)
			{
				monitoringServiceState = monitoringServiceState + " isMonitoringServiceMustPurge = true";
			}
			else
			{
				monitoringServiceState = monitoringServiceState + " isMonitoringServiceMustPurge = false";
			}
			
			if(isMonitoringServiceCanRecord == true)
			{
				monitoringServiceState = monitoringServiceState + " isMonitoringServiceCanRecord = true";
			}
			else
			{
				monitoringServiceState = monitoringServiceState + " isMonitoringServiceCanRecord = false";
			}
			
			return monitoringServiceState;
		}
		catch (Exception e)
        {
			return "";
        }
	}
}