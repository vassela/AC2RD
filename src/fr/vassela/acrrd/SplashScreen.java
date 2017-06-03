/**
 * @file SplashScreen.java
 * @brief Software's splash screen
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

package fr.vassela.acrrd;

import fr.vassela.acrrd.R;
import fr.vassela.acrrd.main.About;
import fr.vassela.acrrd.main.Preferences;
import fr.vassela.acrrd.monitoring.MonitoringServiceManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

public class SplashScreen extends Activity implements OnSharedPreferenceChangeListener
{
	private boolean active = true;
	private final int SPLASH_TIME = 5000; 
	private boolean normalLaunch = true;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
		
		Preferences.onSharedPreferenceRecordActivate(getApplicationContext());
		
		Preferences.onSharedPreferencePurgeActivate(getApplicationContext());
		
		MonitoringServiceManager monitoringServiceManager = new MonitoringServiceManager();
		monitoringServiceManager.startService(getApplicationContext());
		
		boolean sharedPreferencesSplashscreenValue = Preferences.getSharedPreferenceSplashscreen(getApplicationContext());
		
		if(sharedPreferencesSplashscreenValue == false)
		{
			boolean sharedPreferencesAppAcquitmentValue = About.getSharedAppAcquitment(getApplicationContext());
			
			if(sharedPreferencesAppAcquitmentValue == true)
    		{
				normalLaunch = false;
    			finish();
    			Intent intent = new Intent(getApplicationContext(), Main.class);
    			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra("setCurrentTab", "home");
	            startActivity(intent);
    		}
    		else
    		{
    			normalLaunch = false;
    			finish();
                startActivity(new Intent(getApplicationContext(), Acquitment.class));
    		}
		}
		
		setContentView(R.layout.splashscreen);
		
		 Thread splashThread = new Thread()
		 {
		        @Override
		        public void run()
		        {
		            try
		            {
		            	final View viewsplashScreenAppAcronym;
		            	viewsplashScreenAppAcronym = findViewById(R.id.splashscreen_app_acronym);
		            	
		            	final View viewSplashScreenAppName;
		            	viewSplashScreenAppName = findViewById(R.id.splashscreen_app_name);
		            	
		            	final View viewSplashScreenAppCopyright;
		            	viewSplashScreenAppCopyright = findViewById(R.id.splashscreen_app_copyright);
		            	
		            	final Animation splashScreenAppAcronymFadeIn = new AlphaAnimation(0.0f,1.0f);
		            	splashScreenAppAcronymFadeIn.setDuration(2000);
		            	
		            	final Animation splashScreenAppNameFadeIn = new AlphaAnimation(0.0f,1.0f);
		            	splashScreenAppNameFadeIn.setDuration(2000);
		            	
		            	final Animation splashScreenCopyrightFadeIn = new AlphaAnimation(0.0f,1.0f);
		            	splashScreenCopyrightFadeIn.setDuration(2000);
		            	splashScreenCopyrightFadeIn.setStartOffset(2000);
		            	
		            	viewsplashScreenAppAcronym.startAnimation(splashScreenAppAcronymFadeIn);
		            	viewSplashScreenAppName.startAnimation(splashScreenAppNameFadeIn);
		            	viewSplashScreenAppCopyright.startAnimation(splashScreenCopyrightFadeIn);

		                int waited = 0;
		                while(active && (waited < SPLASH_TIME))
		                {
		                    sleep(100);
		                    if(active)
		                    {
		                        waited += 100;
		                    }
		                }

		            }
		            catch(Exception e)
		            {
		            	Log.e("SplashScreen", "onCreate : " + getApplicationContext().getString(R.string.log_splashscreen_error_create) + " : " + e);
		            }
		            finally
		            {
		            	boolean sharedPreferencesAppAcquitmentValue = About.getSharedAppAcquitment(getApplicationContext());
		        		
		        		if(sharedPreferencesAppAcquitmentValue == true)
		        		{
		        			finish();
		        			Intent intent = new Intent(getApplicationContext(), Main.class);
							intent.putExtra("setCurrentTab", "home");
				            startActivity(intent);
		        		}
		        		else
		        		{
		        			finish();
			                startActivity(new Intent(getApplicationContext(), Acquitment.class));
		        		}
		            }
		        }
		    };
		    
		    if(normalLaunch == true)
		    {
		    	splashThread.start();
		    }
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key)
	{
	}
}