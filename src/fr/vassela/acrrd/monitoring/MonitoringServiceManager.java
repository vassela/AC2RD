/**
 * @file MonitoringServiceManager.java
 * @brief Monitoring service manager class
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
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MonitoringServiceManager extends Application
{
	private DatabaseManager databaseManager = new DatabaseManager();
	
	public boolean isRunning(Context context)
	{
		return MonitoringService.isRunning;
	}
	
	public boolean mustPurge(Context context)
	{
		return MonitoringService.mustPurge;
	}
	
	public boolean canRecord(Context context)
	{
		return MonitoringService.canRecord;
	}
	
	public void startService(Context context)
	{
		try
		{
			if(isRunning(context) == false)
			{
				Intent intent = new Intent(context, MonitoringService.class);
				context.startService(intent);
			}
		}
		catch (Exception e)
		{
			Log.e("MonitoringServiceManager", "startService : " + context.getString(R.string.log_monitoring_service_manager_error_start) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_monitoring_service_manager_error_start), new Date().getTime(), 1, false);
		}
	}
	
	public void stopService(Context context)
	{
		try
		{
			if(isRunning(context) == true)
			{
				Boolean monitoringServiceStopped = context.stopService(new Intent(context, MonitoringService.class));
				
				if(monitoringServiceStopped == false)
				{
					int stopRetry = 10;
					
					while((monitoringServiceStopped == false) && (stopRetry >= 0))
					{
						monitoringServiceStopped = context.stopService(new Intent(context, MonitoringService.class));
						stopRetry = stopRetry - 1;
					}
				}
			}
		}
		catch (Exception e)
		{
			Log.e("MonitoringServiceManager", "stopService : " + context.getString(R.string.log_monitoring_service_manager_error_stop) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_monitoring_service_manager_error_stop), new Date().getTime(), 1, false);
		}
	}
}