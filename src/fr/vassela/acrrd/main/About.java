/**
 * @file About.java
 * @brief Software's about class
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

package fr.vassela.acrrd.main;

import java.util.Date;
import fr.vassela.acrrd.R;
import fr.vassela.acrrd.database.DatabaseManager;
import fr.vassela.acrrd.localizer.LocalizerManager;
import fr.vassela.acrrd.main.license.License;
import fr.vassela.acrrd.main.terms.Terms;
import fr.vassela.acrrd.theme.ThemeManager;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.view.ActionMode.Callback;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

public class About extends PreferenceActivity implements AppCompatCallback, SharedPreferences.OnSharedPreferenceChangeListener
{
	private DatabaseManager databaseManager = new DatabaseManager();
	private LocalizerManager localizerManager = new LocalizerManager();
	private ThemeManager themeManager = new ThemeManager();
	
	private AppCompatDelegate delegate;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        localizerManager.setPreferencesLocale(getApplicationContext());
		themeManager.setPreferencesTheme(getApplicationContext(), this);
		
    	delegate = AppCompatDelegate.create(this, this);
		delegate.installViewFactory();
		
        super.onCreate(savedInstanceState);
        
        delegate.onCreate(savedInstanceState);
       
        delegate.setContentView(R.layout.about);
        
        Toolbar toolbar = (Toolbar)findViewById(R.id.about_toolbar);
        delegate.setSupportActionBar(toolbar);
        delegate.getSupportActionBar().setLogo(R.drawable.ic_menu_info_details);
        delegate.setTitle(getApplicationContext().getString(R.string.main_toolbar_about));
        delegate.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        delegate.getSupportActionBar().setDisplayShowHomeEnabled(true);
        
        addPreferencesFromResource(R.layout.about_preferences);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        
        findPreference("about_app_acquitment_detail").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
		{
	        @Override
	        public boolean onPreferenceClick(Preference preference)
	        {
	            startActivity(new Intent(getApplicationContext(), Terms.class));
	            return false;
	        }
	    });
        
        findPreference("about_app_license_detail").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
		{
	        @Override
	        public boolean onPreferenceClick(Preference preference)
	        {
	            startActivity(new Intent(getApplicationContext(), License.class));
	            return false;
	        }
	    });
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
    	try
		{
    		if(key.equals("about_app_acquitment"))
	    	{
    			onSharedPreferenceAboutAppAcquitmentChanged(sharedPreferences);
	    	}

		}
		catch (Exception e)
		{
			Log.w("About", "onSharedPreferenceChanged : " + getApplicationContext().getString(R.string.log_preferences_error_changed) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_preferences_error_changed), new Date().getTime(), 2, false);
		}
    }
    
    private void onSharedPreferenceAboutAppAcquitmentChanged(SharedPreferences sharedPreferences)
	{
		try
		{
			boolean isPreferencesAboutAppAcquitment = sharedPreferences.getBoolean("about_app_acquitment", true);
			
			if(isPreferencesAboutAppAcquitment == true)
			{
				Log.d("About", "onSharedPreferenceAboutAppAcquitmentChanged : " + getApplicationContext().getString(R.string.log_about_app_acquitment_changed_checked));
				databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_about_app_acquitment_changed_checked), new Date().getTime(), 3, false);	
			}
			else
			{
				Log.d("About", "onSharedPreferenceAboutAppAcquitmentChanged : " + getApplicationContext().getString(R.string.log_about_app_acquitment_changed_not_checked));
				databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_about_app_acquitment_changed_not_checked), new Date().getTime(), 3, false);
				
				Intent restartApplicationIntent = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
				restartApplicationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				restartApplicationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				finish();
				startActivity(restartApplicationIntent);
			}
		}
		catch (Exception e)
		{
			Log.w("Preferences", "onSharedPreferenceAboutAppAcquitmentChanged : " + getApplicationContext().getString(R.string.log_about_error_app_acquitment_changed) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_about_error_app_acquitment_changed), new Date().getTime(), 2, false);
		}
	}
    
    public static boolean getSharedAppAcquitment(Context context)
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean sharedPreferencesAppAcquitmentValue = sharedPreferences.getBoolean("about_app_acquitment", false);
		
		return sharedPreferencesAppAcquitmentValue;
	}

	@Override
	public void onSupportActionModeFinished(ActionMode arg0)
	{
	}

	@Override
	public void onSupportActionModeStarted(ActionMode arg0)
	{
	}

	@Override
	public ActionMode onWindowStartingSupportActionMode(Callback arg0)
	{
		return null;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item)
	{
        int id = item.getItemId();
        
        switch(id)
        {
        	case android.R.id.home :
        		super.onBackPressed();
        		break;
        }
        
        return super.onOptionsItemSelected(item);
    }
}