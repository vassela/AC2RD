/**
 * @file ShakeDetectorServiceManager.java
 * @brief Shake detector service manager class
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

package fr.vassela.acrrd.sensor;

import java.util.Date;
import fr.vassela.acrrd.R;
import fr.vassela.acrrd.database.DatabaseManager;
import fr.vassela.acrrd.notifier.TelephoneCallNotifier;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
//import android.os.Bundle;
import android.util.Log;

public class ShakeDetectorServiceManager extends Application
{
	private DatabaseManager databaseManager = new DatabaseManager();
	private TelephoneCallNotifier telephoneCallNotifier = new TelephoneCallNotifier();

	public boolean isRunning(Context context)
	{
		return ShakeDetectorService.isRunning;
	}
	
	public int getCallDirection(Context context)
	{
		return ShakeDetectorService.callDirection;
	}

	public String getTelephoneNumber(Context context)
	{
		return ShakeDetectorService.telephoneNumber;
	}
	
	public void startService(Context context, int direction, String number)
	{
		try
		{
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			boolean isPreferencesShakeDetectorActivated = sharedPreferences.getBoolean("preferences_shake_detector_activate",false);
			boolean isPreferencesRecordActivated = sharedPreferences.getBoolean("preferences_record_activate",false);
			
			if((isPreferencesRecordActivated == true) || ((direction != 0) && (direction != 1)))
			{
				if(isPreferencesShakeDetectorActivated == true)
				{
					if(isRunning(context) == false)
					{
						telephoneCallNotifier.displayToast(context, context.getString(R.string.notification_telephone_call_receiver_start_shake_detector_toast), true);
						
						Intent intent = new Intent(context, ShakeDetectorService.class);
						intent.putExtra("callDirection", direction);
						intent.putExtra("telephoneNumber", number);
						context.startService(intent);
					}
				}
			}
		}
		catch (Exception e)
		{
			Log.e("ShakeDetectorServiceManager", "startService : " + context.getString(R.string.log_shake_detector_service_manager_error_start) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_shake_detector_service_manager_error_start), new Date().getTime(), 1, false);
		}
	}
	
	public void stopService(Context context)
	{
		try
		{
			if(isRunning(context) == true)
			{
				Boolean shakeDetectorServiceStopped = context.stopService(new Intent(context, ShakeDetectorService.class));
				
				if(shakeDetectorServiceStopped == false)
				{
					int stopRetry = 10;
					
					while((shakeDetectorServiceStopped == false) && (stopRetry >= 0))
					{
						shakeDetectorServiceStopped = context.stopService(new Intent(context, ShakeDetectorService.class));
						stopRetry = stopRetry - 1;
					}
				}
			}
		}
		catch (Exception e)
		{
			Log.e("ShakeDetectorServiceManager", "stopService : " + context.getString(R.string.log_shake_detector_service_manager_error_stop) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_shake_detector_service_manager_error_stop), new Date().getTime(), 1, false);
		}
	}
}