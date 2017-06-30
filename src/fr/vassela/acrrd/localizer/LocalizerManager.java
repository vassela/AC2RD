/**
 * @file LocalizerManager.java
 * @brief Language manager
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

package fr.vassela.acrrd.localizer;

import java.util.Date;
import java.util.Locale; 
import fr.vassela.acrrd.R;
import fr.vassela.acrrd.database.DatabaseManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.Log;

public class LocalizerManager extends Application
{
	private String[] locales = { "default", "fr_FR", "en_US" };
    private DatabaseManager databaseManager = new DatabaseManager();
    
    public void setLocale(Context context, String language)
    {
    	try
		{
    		Locale locale = new Locale(language);
    		Locale.setDefault(locale);
    		Configuration config = new Configuration();
    		config.locale = locale;
    		context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
		}
		catch (Exception e)
		{
			Log.w("LocalizerManager", "setLocale : " + context.getString(R.string.log_localizer_manager_error_set_locale) + " : " + language + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_localizer_manager_error_set_locale) + " : " + language, new Date().getTime(), 2, false);
		}
    }
    
    public void setPreferencesLocale(Context context)
    {
    	try
		{
    		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    		int localeSet = Integer.parseInt(sharedPreferences.getString("preferences_locale_set", "0"));
    		
    		if(localeSet == 0)
    		{
    			String locale = getDefaultLocale(context).toString();
    			Log.w("LocalizerManager", "locale : " + locale);
    			setLocale(context, locale);
    		}
    		else
    		{
    			setLocale(context, locales[localeSet]);
    		}
		}
		catch (Exception e)
		{
			Log.w("LocalizerManager", "setPreferencesLocale : " + context.getString(R.string.log_localizer_manager_error_set_preferences_locale) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_localizer_manager_error_set_preferences_locale), new Date().getTime(), 2, false);
		}
    }
    
    public Locale getLocale(Context context)
    {
    	try
		{
    		Locale locale = context.getResources().getConfiguration().locale;
    		
    		return locale;
		}
		catch (Exception e)
		{
			Log.w("LocalizerManager", "getLocale : " + context.getString(R.string.log_localizer_manager_error_get_locale) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_localizer_manager_error_get_locale), new Date().getTime(), 2, false);
			return null;
		}
    }
    
    public Locale getDefaultLocale(Context context)
    {
    	try
		{
    		Locale locale = Resources.getSystem().getConfiguration().locale;
    		
    		return locale;
		}
		catch (Exception e)
		{
			Log.w("LocalizerManager", "getDefaultLocale : " + context.getString(R.string.log_localizer_manager_error_get_default_locale) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_localizer_manager_error_get_default_locale), new Date().getTime(), 2, false);
			return null;
		}
    }
}