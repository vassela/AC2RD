/**
 * @file ShakeDetectorService.java
 * @brief Shake detector service class
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
import fr.vassela.acrrd.sensor.IShakeDetectorService;
import fr.vassela.acrrd.database.DatabaseManager;
import android.os.IBinder;
import android.util.Log;
import android.app.Service;
import android.content.Intent;

public class ShakeDetectorService extends Service
{
    public static boolean isRunning;
    
    public static int callDirection;
    public static String telephoneNumber;
    
    private ShakeDetector shakeDetector;
    
    private DatabaseManager databaseManager = new DatabaseManager();

    private IBinder binder = new IShakeDetectorService.Stub()
	{
    	public boolean isRunning()
    	{
    		return isRunning;
    	}
    	
    	public int getCallDirection()
		{
			return callDirection;
		}

		public String getTelephoneNumber()
		{
			return telephoneNumber;
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
			callDirection = 0;
	        telephoneNumber = "";
			
			shakeDetector = new ShakeDetector(getApplicationContext());
		}
		catch (Exception e)
		{
			Log.e("ShakeDetectorService", "onCreate : " + getApplicationContext().getString(R.string.log_shake_detector_service_error_create) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_shake_detector_service_error_create), new Date().getTime(), 1, false);
		}      
    }
    
    private void onStartCall(Intent intent)
    {
    	try
		{
	    	if(intent.getExtras() != null)
			{
		    	callDirection = intent.getIntExtra("callDirection", 0);
		    	telephoneNumber = intent.getStringExtra("telephoneNumber");
			}
		}
		catch (Exception e)
		{
			Log.w("ShakeDetectorService", "onStartCall : " + getApplicationContext().getString(R.string.log_shake_detector_service_error_get_extras_call) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_shake_detector_service_error_get_extras_call), new Date().getTime(), 2, false);
		}
    }
    
    @Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
    	try
		{
    		onStartCall(intent);
    		
    		shakeDetector.startShakeDetector();
    		
	    	isRunning = true;

	    	return 0;
		}
		catch (Exception e)
		{
			Log.e("ShakeDetectorService", "onStartCommand : " + getApplicationContext().getString(R.string.log_shake_detector_service_error_start) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_shake_detector_service_error_start), new Date().getTime(), 1, false);
			
			isRunning = false;
			
			return 0;
		} 
	}

    public void onDestroy()
    {
    	try
		{
	    	super.onDestroy();
	    	
	    	isRunning = false;
	    	
	    	shakeDetector.stopShakeDetector();
		}
		catch (Exception e)
		{
			Log.e("ShakeDetectorService", "onDestroy : " + getApplicationContext().getString(R.string.log_shake_detector_service_error_destroy) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_shake_detector_service_error_destroy), new Date().getTime(), 1, false);
		}  
    }
}