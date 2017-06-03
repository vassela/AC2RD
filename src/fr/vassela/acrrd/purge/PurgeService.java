/**
 * @file PurgeService.java
 * @brief Purge service class
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

package fr.vassela.acrrd.purge;

import java.util.Timer;
import java.util.TimerTask;

import fr.vassela.acrrd.R;
import fr.vassela.acrrd.purge.IPurgeService;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Classe permettant la purge des enregistrements ayant dépassés leur durée de rétention
 * @author Arnaud Vassellier
 *
 */
public class PurgeService extends Service
{
	private Timer timer;
	
	public static boolean isRunning = false;
	
	private PurgeManager purgeManager;
	
	private IBinder binder = new IPurgeService.Stub()
	{
    	public boolean isRunning()
    	{
    		return isRunning;
    	}
	};
	
	@Override
	public IBinder onBind(Intent arg0)
	{
		return binder;
	}
	
	public boolean onUnbind(Intent intent)
    {
        return false;
    }
	
	public void onCreate()
	{
		try
		{
			super.onCreate();
			
			purgeManager = new PurgeManager();
		}
		catch (Exception e)
		{
			Log.w("PurgeService", "onCreate : " + getApplicationContext().getString(R.string.log_purge_service_error_create) + " : " + e);
		}
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		try
		{
			SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			int purgeFrequency = Integer.parseInt(SP.getString("preferences_purge_frequency", "1"));
			
			timer = new Timer(true);
			
			timer.scheduleAtFixedRate(new TimerTask()
			{ 
		        public void run()
		        {
		        	isRunning = true;

		        	purgeManager.purgeStaleRecords(getApplicationContext());
		        } 
		    }, 0, (3600000 * purgeFrequency)); 
			
			return START_NOT_STICKY;
		}
		catch (Exception e)
		{
			Log.w("PurgeService", "onStartCommand : " + getApplicationContext().getString(R.string.log_purge_service_error_start_command) + " : " + e);
			return START_NOT_STICKY;
		}
		finally
		{
			Log.d("PurgeService", "onStartCommand : " + getApplicationContext().getString(R.string.log_purge_service_start_command));
		}
	}
	
	public void onDestroy()
	{
		try
		{
			isRunning = false;
			timer.cancel(); 
		}
		catch (Exception e)
		{
			Log.w("PurgeService", "onDestroy : " + getApplicationContext().getString(R.string.log_purge_service_error_destroy) + " : " + e);
		}
		finally
		{
			Log.d("PurgeService", "onDestroy : " + getApplicationContext().getString(R.string.log_purge_service_destroy));
		}
	}
}