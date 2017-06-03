/**
 * @file MonitoringService.java
 * @brief Monitoring service class
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
import java.util.Timer;
import java.util.TimerTask;

import android.os.IBinder;
import android.util.Log;
import android.app.Service;
import android.content.Intent;
import fr.vassela.acrrd.R;
import fr.vassela.acrrd.monitoring.IMonitoringService;
import fr.vassela.acrrd.database.DatabaseManager;
import fr.vassela.acrrd.purge.PurgeManager;
import fr.vassela.acrrd.purge.PurgeServiceManager;
import fr.vassela.acrrd.recorder.RecordServiceManager;

public class MonitoringService extends Service
{
    public static boolean isRunning;
    public static boolean mustPurge;
    public static boolean canRecord;
    
    private float mustPurgeThreshold = 5;
    private float canRecordThreshold = 1;
    
    private MonitoringManager monitoringManager;
    private PurgeManager purgeManager;
    private PurgeServiceManager purgeServiceManager;
    private RecordServiceManager recordServiceManager;
    
    private DatabaseManager databaseManager = new DatabaseManager();
    
    private Timer timer;

    private IBinder binder = new IMonitoringService.Stub()
	{
    	public boolean isRunning()
    	{
    		return isRunning;
    	}
    	
    	public boolean mustPurge()
    	{
    		return mustPurge;
    	}
    	
    	public boolean canRecord()
    	{
    		return canRecord;
    	}
	};

    public IBinder onBind(Intent intent)
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
			
			isRunning = false;
	        mustPurge = false;
	        canRecord = false;
	        
	        monitoringManager = new MonitoringManager();
	        purgeManager = new PurgeManager();
	        purgeServiceManager = new PurgeServiceManager();
	        recordServiceManager = new RecordServiceManager();
		}
		catch (Exception e)
		{
			Log.e("MonitoringService", "onCreate : " + getApplicationContext().getString(R.string.log_monitoring_service_error_create) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_monitoring_service_error_create), new Date().getTime(), 1, false);
		}      
    }
    
    @Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
    	try
		{
	    	isRunning = true;
	    	mustPurge = false;
	    	canRecord = false;
	    	
	    	timer = new Timer(true); 
	    	
	    	timer.scheduleAtFixedRate(new TimerTask()
			{
				@Override
				public void run()
				{
					float storageAvailableSpaceInPercent = monitoringManager.getStoragePathAvailableSpaceInPercent(getApplicationContext());
					
					if (storageAvailableSpaceInPercent <= canRecordThreshold)
					{
						canRecord = false;
						mustPurge = true;
						
						boolean isRecordServiceRunning = recordServiceManager.isRunning(getApplicationContext());
						
						if (isRecordServiceRunning == true)
						{
							recordServiceManager.stopService(getApplicationContext());
						}
						
						boolean isPurgeServiceRunning = purgeServiceManager.isRunning(getApplicationContext());
						
						if(isPurgeServiceRunning == true)
						{
							purgeManager.purgeOldestRecord(getApplicationContext());
						}
					}
					else if ((storageAvailableSpaceInPercent > canRecordThreshold) && (storageAvailableSpaceInPercent <= mustPurgeThreshold))
					{
						canRecord = true;
						mustPurge = true;
						
						boolean isPurgeServiceRunning = purgeServiceManager.isRunning(getApplicationContext());
						
						if(isPurgeServiceRunning == true)
						{
							purgeManager.purgeOldestRecord(getApplicationContext());
						}
					}
					else
					{
						canRecord = true;
						mustPurge = false;
					}
				}
			}, 0, 10000);
	    	
	    	return 0;
		}
		catch (Exception e)
		{
			Log.e("MonitoringService", "onStartCommand : " + getApplicationContext().getString(R.string.log_monitoring_service_error_start) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_monitoring_service_error_start), new Date().getTime(), 1, false);
		
			return 0;
		} 
	}

    public void onDestroy()
    {
    	try
		{
	    	super.onDestroy();
	    	
	    	isRunning = false;
	    	mustPurge = false;
	    	canRecord = false;
	    	
	    	timer.cancel();
		}
		catch (Exception e)
		{
			Log.e("MonitoringService", "onDestroy : " + getApplicationContext().getString(R.string.log_monitoring_service_error_destroy) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_monitoring_service_error_destroy), new Date().getTime(), 1, false);
		}  
    }
}