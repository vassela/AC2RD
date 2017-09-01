/**
 * @file BootReceiver.java
 * @brief Boot receiver class
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

package fr.vassela.acrrd.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import fr.vassela.acrrd.SplashScreen;
import fr.vassela.acrrd.database.DatabaseManager;
import fr.vassela.acrrd.main.Preferences;

/**
 * Classe permettant de détecter le boot du téléphone
 * @author Arnaud Vassellier
 *
 */
public class BootReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		try
		{
			if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
			{
				boolean sharedPreferencesRecordActivate = Preferences.getSharedPreferenceRecordActivate(context);
				
				if(sharedPreferencesRecordActivate == true)
	    		{
					Log.d("onReceive", "onReceive : " + "BOOT");
					Intent automaticBootIntent = new Intent(context, SplashScreen.class);
					automaticBootIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(automaticBootIntent);
	    		}
			}
		}
		catch (Exception e)
		{
			Log.w("BootReceiver", "onReceive : " + e);
		}
	}
}