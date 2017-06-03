/**
 * @file FiltersManager.java
 * @brief Call filters Manager
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

package fr.vassela.acrrd.filters;

import java.util.Date;

import fr.vassela.acrrd.R;
import fr.vassela.acrrd.database.DatabaseManager;
import fr.vassela.acrrd.database.DatabaseProvider;
import fr.vassela.acrrd.database.DatabaseDefinition.CONTACT_FILTER;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

public class FiltersManager extends Application
{
	private ContentResolver contentResolver = null;
	private Uri contentUri = null;
	
    private DatabaseManager databaseManager = new DatabaseManager();
    
    public int isContactFilter(Context context, String number)
    {
    	try
		{
    		contentResolver = context.getContentResolver();
			contentUri = DatabaseProvider.CONTACT_FILTER_CONTENT_URI;

    		Cursor cursor = contentResolver.query(contentUri, null, null, null, CONTACT_FILTER.DEFAULT_SORT_ORDER);
    		
    		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
        	{
    			String contactFilterNumber = cursor.getString(cursor.getColumnIndex(CONTACT_FILTER.NUMBER));
    			int contactFilterRecordable = cursor.getInt(cursor.getColumnIndex(CONTACT_FILTER.RECORDABLE));
    			
    			boolean isIdenticalNumber = PhoneNumberUtils.compare(number, contactFilterNumber);
    			
    			if (isIdenticalNumber == true)
    			{
    				return contactFilterRecordable;
    			}
        	}
    		
    		return -1;
		}
		catch (Exception e)
		{
			Log.w("FiltersManager", "isContactFilter : " + context.getString(R.string.log_preferences_filters_manager_error_is_contact_filter) + " : " + number + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_preferences_filters_manager_error_is_contact_filter) + " : " + number, new Date().getTime(), 2, false);
			return -1;
		}
    }
    
    public boolean isContactFilterAlreadyExists(Context context, String number)
    {
    	try
		{
    		contentResolver = context.getContentResolver();
			contentUri = DatabaseProvider.CONTACT_FILTER_CONTENT_URI;

    		Cursor cursor = contentResolver.query(contentUri, null, CONTACT_FILTER.NUMBER + " = \"" + number + "\"", null, CONTACT_FILTER.DEFAULT_SORT_ORDER);
    		
    		if (cursor.moveToFirst())
        	{
    			return true;
        	}
    		else
    		{
    			return false;
    		}
		}
		catch (Exception e)
		{
			Log.w("FiltersManager", "isContactFilterAlreadyExists : " + context.getString(R.string.log_preferences_filters_manager_error_is_contact_filter_already_exists) + " : " + number + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_preferences_filters_manager_error_is_contact_filter_already_exists) + " : " + number, new Date().getTime(), 2, false);
			return true;
		}
    }
}