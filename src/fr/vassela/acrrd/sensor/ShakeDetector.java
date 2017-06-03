/**
 * @file ShakeDetector.java
 * @brief Shake detector class
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
import fr.vassela.acrrd.recorder.RecordServiceManager;
import android.app.Application;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class ShakeDetector extends Application
{
	private SensorManager sensorManager;
	
	private float previousAcceleration;
	private float currentAcceleration;
	private float acceleration;
	
	private int shakeDetectorThreshold = 12;
	private Context shakeDetectorContext;
	
	private ShakeDetectorServiceManager shakeDetectorServiceManager = new ShakeDetectorServiceManager();
	private DatabaseManager databaseManager = new DatabaseManager();
		  
	public ShakeDetector(Context context)
	{
		try
		{
			shakeDetectorContext = context;
			
			sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
			
			previousAcceleration = SensorManager.GRAVITY_EARTH;
			currentAcceleration = SensorManager.GRAVITY_EARTH;
			acceleration = 0.00f;
		}
		catch (Exception e)
		{
			Log.e("ShakeDetector", "ShakeDetector : " + context.getString(R.string.log_shake_detector_error_create) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_shake_detector_error_create), new Date().getTime(), 1, false);
		}
	}
	  
	public void startShakeDetector()
	{
		try
		{
			sensorManager.registerListener(mSensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
		}
		catch (Exception e)
		{
			Log.e("ShakeDetector", "startShakeDetector : " + shakeDetectorContext.getString(R.string.log_shake_detector_error_start) + " : " + e);
			databaseManager.insertLog(shakeDetectorContext, "" + shakeDetectorContext.getString(R.string.log_shake_detector_error_start), new Date().getTime(), 1, false);
		}
	}
	  
	public void stopShakeDetector()
	{
		try
		{
			sensorManager.unregisterListener(mSensorListener);
		}
		catch (Exception e)
		{
			Log.e("ShakeDetector", "stopShakeDetector : " + shakeDetectorContext.getString(R.string.log_shake_detector_error_stop) + " : " + e);
			databaseManager.insertLog(shakeDetectorContext, "" + shakeDetectorContext.getString(R.string.log_shake_detector_error_stop), new Date().getTime(), 1, false);
		}
	}
	
	private final SensorEventListener mSensorListener = new SensorEventListener()
	{
		public void onSensorChanged(SensorEvent sensorEvent)
		{	
			try
			{
				if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
				{
					float xAxis = sensorEvent.values[0];
					float yAxis = sensorEvent.values[1];
					float zAxis = sensorEvent.values[2];
					
					previousAcceleration = currentAcceleration;
					currentAcceleration = (float) Math.sqrt((double) ((xAxis * xAxis) + (yAxis * yAxis) + (zAxis * zAxis)));
					
					float accelerationDelta = currentAcceleration - previousAcceleration;
					acceleration = acceleration * 0.9f + accelerationDelta;
	
					if (acceleration > shakeDetectorThreshold)
					{
						int callDirection = shakeDetectorServiceManager.getCallDirection(shakeDetectorContext);
						String telephoneNumber = shakeDetectorServiceManager.getTelephoneNumber(shakeDetectorContext);

						RecordServiceManager recordServiceManager = new RecordServiceManager();
						recordServiceManager.startService(shakeDetectorContext.getApplicationContext(), callDirection, telephoneNumber);

						shakeDetectorServiceManager.stopService(shakeDetectorContext);
			    	}
				}
			}
			catch (Exception e)
			{
				Log.e("ShakeDetector", "SensorEventListener : " + shakeDetectorContext.getString(R.string.log_shake_detector_error_sensor_event_listener) + " : " + e);
				databaseManager.insertLog(shakeDetectorContext, "" + shakeDetectorContext.getString(R.string.log_shake_detector_error_sensor_event_listener), new Date().getTime(), 1, false);
			}
		}

		public void onAccuracyChanged(Sensor sensor, int accuracy)
		{
			
		}
	};
}