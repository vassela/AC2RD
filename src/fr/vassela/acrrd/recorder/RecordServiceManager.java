/**
 * @file RecordServiceManager.java
 * @brief Record service manager class
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

package fr.vassela.acrrd.recorder;

import java.util.Date;

import fr.vassela.acrrd.R;
import fr.vassela.acrrd.database.DatabaseManager;
import fr.vassela.acrrd.monitoring.MonitoringServiceManager;
import fr.vassela.acrrd.notifier.TelephoneCallNotifier;
import fr.vassela.acrrd.purge.PurgeServiceManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
//import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public class RecordServiceManager extends Application
{
	private DatabaseManager databaseManager = new DatabaseManager();
	private TelephoneCallNotifier telephoneCallNotifier = new TelephoneCallNotifier();
	
	/*private IRecordService binder = null;
	
	private ServiceConnection connection = new ServiceConnection()
	{
		@Override
		public void onServiceDisconnected(ComponentName name)
		{
			binder = null;
		}
		@Override
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			binder = IRecordService.Stub.asInterface(service);
		}
	};*/
	
	public boolean isRunning(Context context)
	{
		return RecordService.isRunning;
		
		/*context.bindService(new Intent("fr.vassela.acrrd.recorder.act.RECORDSERVICE"), connection, context.BIND_AUTO_CREATE);
		
		boolean isRunning = false;
		
		try
		{
			isRunning = binder.isRunning();
		}
		catch (RemoteException e)
		{
			e.printStackTrace();
		}
		
		return isRunning;*/
	}
	
	public void startService(Context context, int direction, String number)
	{
		try
		{
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			boolean isPreferencesRecordActivated = sharedPreferences.getBoolean("preferences_record_activate",false);
			boolean isPreferencesPurgeActivated = sharedPreferences.getBoolean("preferences_purge_activate", true);
			
			if((isPreferencesRecordActivated == true) || ((direction != 0) && (direction != 1)))
			{
				if(isRunning(context) == false)
				{
					MonitoringServiceManager monitoringServiceManager = new MonitoringServiceManager();
					boolean isMonitoringServiceRunning = monitoringServiceManager.isRunning(context);
					
					if(isMonitoringServiceRunning == false)
					{
						monitoringServiceManager.startService(context);
					}
					
					PurgeServiceManager purgeServiceManager = new PurgeServiceManager();
					boolean isPurgeServiceRunning = purgeServiceManager.isRunning(context);
					
					if((isPreferencesPurgeActivated == true) && (isPurgeServiceRunning == false))
					{
						purgeServiceManager.startService(context);
					}
					
					boolean canRecord = monitoringServiceManager.canRecord(context);

					if(canRecord)
					{
						Intent intent = new Intent(context, RecordService.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.putExtra("callDirection", direction);
						intent.putExtra("telephoneNumber", number);
						context.startService(intent);
					}
					else
					{
						telephoneCallNotifier.displayToast(context, context.getString(R.string.notification_record_service_manager_error_start_toast), true);
					}
				}
			}
		}
		catch (Exception e)
		{
			Log.e("RecordServiceManager", "startService : " + context.getString(R.string.log_record_service_manager_error_start) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_record_service_manager_error_start), new Date().getTime(), 1, false);
		}
	}
	
	public void stopService(Context context)
	{
		try
		{
			if(isRunning(context) == true)
			{
				Boolean recordServiceStopped = context.stopService(new Intent(context, RecordService.class));
				
				if(recordServiceStopped == false)
				{
					int stopRetry = 10;
					
					while((recordServiceStopped == false) && (stopRetry >= 0))
					{
						recordServiceStopped = context.stopService(new Intent(context, RecordService.class));
						stopRetry = stopRetry - 1;
					}
				}
			}
		}
		catch (Exception e)
		{
			Log.e("RecordServiceManager", "stopService : " + context.getString(R.string.log_record_service_manager_error_stop) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_record_service_manager_error_stop), new Date().getTime(), 1, false);
		}
	}
	
	public void setRecord(Context context, int direction, String number)
	{
		try
		{
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			boolean isPreferencesRecordActivated = sharedPreferences.getBoolean("preferences_record_activate",false);
			
			if(isPreferencesRecordActivated == true)
			{	
				if(isRunning(context) == true)
				{
					//Intent intent = new Intent("fr.vassela.acrrd.recorder.act.RECORDSERVICE");
					Intent intent = new Intent(context, RecordService.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra("callDirection", direction);
					intent.putExtra("telephoneNumber", number);
					context.startService(intent);
				}
				
				/*context.bindService(new Intent("fr.vassela.acrrd.recorder.act.RECORDSERVICE"), connection, context.BIND_AUTO_CREATE);
				
				try
				{
					binder.setRecord(direction, number);
				}
				catch (RemoteException e)
				{
					e.printStackTrace();
				}*/
			}
		}
		catch (Exception e)
		{
			Log.w("RecordServiceManager", "setRecord : " + context.getString(R.string.log_record_service_manager_error_set_call) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_record_service_manager_error_set_call), new Date().getTime(), 2, false);
		}
	}
	
	public int getCallDirection(Context context)
	{
		return RecordService.callDirection;
		
		/*context.bindService(new Intent("fr.vassela.acrrd.recorder.act.RECORDSERVICE"), connection, context.BIND_AUTO_CREATE);
		
		int callDirection = 0;
		
		try
		{
			callDirection = binder.getCallDirection();
		}
		catch (RemoteException e)
		{
			e.printStackTrace();
		}
		
		return callDirection;*/
	}

	public String getTelephoneNumber(Context context)
	{
		return RecordService.telephoneNumber;
		
		/*context.bindService(new Intent("fr.vassela.acrrd.recorder.act.RECORDSERVICE"), connection, context.BIND_AUTO_CREATE);
		
		String telephoneNumber = "";
		
		try
		{
			telephoneNumber = binder.getTelephoneNumber();
		}
		catch (RemoteException e)
		{
			e.printStackTrace();
		}
		
		return telephoneNumber;*/
	}
	
	public void setDoubleCall(Context context, String number)
	{
		try
		{
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			boolean isPreferencesRecordActivated = sharedPreferences.getBoolean("preferences_record_activate",false);
			
			if(isPreferencesRecordActivated == true)
			{
				if(isRunning(context) == true)
				{
					//Intent intent = new Intent("fr.vassela.acrrd.recorder.act.RECORDSERVICE");
					Intent intent = new Intent(context, RecordService.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra("doubleCall", true);
					intent.putExtra("doubleCallNumber", number);
					context.startService(intent);
				}
				
				/*context.bindService(new Intent("fr.vassela.acrrd.recorder.act.RECORDSERVICE"), connection, context.BIND_AUTO_CREATE);
				
				try
				{
					binder.setDoubleCall(number);
				}
				catch (RemoteException e)
				{
					e.printStackTrace();
				}*/
			}
		}
		catch (Exception e)
		{
			Log.w("RecordServiceManager", "setDoubleCall : " + context.getString(R.string.log_record_service_manager_error_set_double_call) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_record_service_manager_error_set_double_call), new Date().getTime(), 2, false);
		}
	}
	
	public boolean getDoubleCall(Context context)
	{
		return RecordService.doubleCall;
		
		/*context.bindService(new Intent("fr.vassela.acrrd.recorder.act.RECORDSERVICE"), connection, context.BIND_AUTO_CREATE);
		
		boolean doubleCall = false;
		
		try
		{
			doubleCall = binder.getDoubleCall();
		}
		catch (RemoteException e)
		{
			e.printStackTrace();
		}
		
		return doubleCall;*/
		
	}
	
	public String getDoubleCallNumber(Context context)
	{
		return RecordService.doubleCallNumber;
		
		/*context.bindService(new Intent("fr.vassela.acrrd.recorder.act.RECORDSERVICE"), connection, context.BIND_AUTO_CREATE);
		
		String doubleCallNumber = "";
		
		try
		{
			doubleCallNumber = binder.getDoubleCallNumber();
		}
		catch (RemoteException e)
		{
			e.printStackTrace();
		}
		
		return doubleCallNumber;*/
	}
}
