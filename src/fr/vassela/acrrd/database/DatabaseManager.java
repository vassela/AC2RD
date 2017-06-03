/**
 * @file DatabaseManager.java
 * @brief Software's database manager
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

package fr.vassela.acrrd.database;

import java.util.Date;

import fr.vassela.acrrd.R;
import fr.vassela.acrrd.database.DatabaseDefinition.CONTACT_FILTER;
import fr.vassela.acrrd.database.DatabaseDefinition.TELEPHONE_CALL;
import fr.vassela.acrrd.database.DatabaseDefinition.TELEPHONE_LOG;
import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;

public class DatabaseManager extends Application
{
	private ContentResolver contentResolver = null;
	private Uri contentUri = null;
	
	public void insertCall(Context context, int direction, String number, int doubleCall, String doubleCallNumber, long startingDate, long endingDate, long duration, String recordPath, String recordFilename, int recordFormat, int locked, String description)
	{
		try
		{
			contentResolver = context.getContentResolver();
			contentUri = DatabaseProvider.TELEPHONE_CALL_CONTENT_URI;
			ContentValues contentValues = new ContentValues();
			
			contentValues.put(TELEPHONE_CALL.DIRECTION, direction);
			contentValues.put(TELEPHONE_CALL.NUMBER, number);
			contentValues.put(TELEPHONE_CALL.DOUBLE_CALL, doubleCall);
			contentValues.put(TELEPHONE_CALL.DOUBLE_CALL_NUMBER, doubleCallNumber);
			contentValues.put(TELEPHONE_CALL.STARTING_DATE, startingDate);
			contentValues.put(TELEPHONE_CALL.ENDING_DATE, endingDate);
			contentValues.put(TELEPHONE_CALL.DURATION, duration);
			contentValues.put(TELEPHONE_CALL.RECORD_PATH, recordPath);
			contentValues.put(TELEPHONE_CALL.RECORD_FILENAME, recordFilename);
			contentValues.put(TELEPHONE_CALL.RECORD_FORMAT, recordFormat);
			contentValues.put(TELEPHONE_CALL.LOCKED, locked);
			contentValues.put(TELEPHONE_CALL.DESCRIPTION, description);
			
			contentResolver.insert(contentUri, contentValues);
		}
		catch (Exception e)
		{
			Log.w("DatabaseManager", "insertCall : " + context.getString(R.string.log_database_manager_error_request) + " : " + e);
		}
	}
	
    public void insertDescription(Context context, String fileName, String description)
	{
		try
		{
			contentResolver = context.getContentResolver();
			contentUri = DatabaseProvider.TELEPHONE_CALL_CONTENT_URI;
			
        	Cursor cursor = contentResolver.query(contentUri, null, TELEPHONE_CALL.RECORD_FILENAME + " = \"" + fileName + "\"" ,null, TELEPHONE_CALL.DEFAULT_SORT_ORDER);		        	
			
        	if (cursor.moveToFirst())
        	{
        		String recordFileName = cursor.getString(cursor.getColumnIndex(TELEPHONE_CALL.RECORD_FILENAME));
        		
        		ContentValues contentValues = new ContentValues();
        		contentValues.put(TELEPHONE_CALL.DESCRIPTION, description);
				
				contentResolver.update(contentUri, contentValues, TELEPHONE_CALL.RECORD_FILENAME + " = \"" + recordFileName + "\"", null);
        	}
		}
		catch (Exception e)
		{
			Log.w("DatabaseManager", "insertDescription : " + context.getString(R.string.log_database_manager_error_request) + " : " + e);
		}
	}
	
	public Cursor queryCallOfDay(Context context, Cursor cursor, String orderBy)
	{
		try
		{
			Date currentDate = new Date(System.currentTimeMillis());
			currentDate.setHours(0);
			currentDate.setMinutes(0);
			currentDate.setSeconds(0);
			
			contentResolver = context.getContentResolver();
			contentUri = DatabaseProvider.TELEPHONE_CALL_CONTENT_URI;
			
			long startCurrentDate = currentDate.getTime();
			long endCurrentDate = startCurrentDate + 86400000;
			
			if((orderBy == null) || (orderBy == ""))
			{
				cursor = contentResolver.query(contentUri, null, "STARTING_DATE > " + startCurrentDate + " AND STARTING_DATE < " + endCurrentDate, null, TELEPHONE_CALL.DEFAULT_SORT_ORDER);
			}
			else
			{
				cursor = contentResolver.query(contentUri, null, "STARTING_DATE > " + startCurrentDate + " AND STARTING_DATE < " + endCurrentDate, null, orderBy);
	
			}
			
			return cursor;
		}
		catch (Exception e)
		{
			Log.w("DatabaseManager", "queryCallOfDay : " + context.getString(R.string.log_database_manager_error_request) + " : " + e);
			
			return cursor;
		}
	}
	
	public Cursor queryAllCall(Context context, Cursor cursor, String orderBy)
	{
		try
		{
			contentResolver = context.getContentResolver();
			contentUri = DatabaseProvider.TELEPHONE_CALL_CONTENT_URI;
			
			if((orderBy == null) || (orderBy == ""))
			{
				cursor = contentResolver.query(contentUri, null, null, null, TELEPHONE_CALL.DEFAULT_SORT_ORDER);
			}
			else
			{
				cursor = contentResolver.query(contentUri, null, null, null, orderBy);
			}
			
			return cursor;
		}
		catch (Exception e)
		{
			Log.w("DatabaseManager", "queryAllCall : " + context.getString(R.string.log_database_manager_error_request) + " : " + e);
			
			return cursor;
		}
	}
	
	public String getRecordContactName(Context context, String number)
    {
		try
		{
	    	String contactName = "";
	    	
	    	Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
	        Cursor cursor = contentResolver.query(uri, new String[]{PhoneLookup._ID,PhoneLookup.DISPLAY_NAME}, null, null, null);
	
	        if(cursor.moveToFirst())
	        {
	            contactName = cursor.getString(cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME));
	        }
	        
	        if(cursor != null && !cursor.isClosed())
	        {
	            cursor.close();
	        }
	        
	        return contactName;
		}
		catch (Exception e)
		{
			Log.w("DatabaseManager", "getRecordContactName : " + context.getString(R.string.log_database_manager_error_request) + " : " + e);
			
			return null;
		}
    }
	
	public void deleteAllCall(Context context)
	{
		try
		{
			contentResolver = context.getContentResolver();
			contentUri = DatabaseProvider.TELEPHONE_CALL_CONTENT_URI;
			contentResolver.delete(contentUri,null,null);
		}
		catch (Exception e)
		{
			Log.w("DatabaseManager", "deleteAllCall : " + context.getString(R.string.log_database_manager_error_request) + " : " + e);;
		}
	}
	
	public void insertLog(Context context, String text, long date, int priority, boolean force)
	{
		try
		{
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			boolean isPreferencesLogsActivated = sharedPreferences.getBoolean("preferences_logs_activate",false);
			
			if((isPreferencesLogsActivated == true) || (force == true))
			{
				contentResolver = context.getContentResolver();
				contentUri = DatabaseProvider.TELEPHONE_LOG_CONTENT_URI;
				ContentValues contentValues = new ContentValues();
				
				contentValues.put(TELEPHONE_LOG.TEXT, text);
				contentValues.put(TELEPHONE_LOG.DATE, date);
				contentValues.put(TELEPHONE_LOG.PRIORITY, priority);
				
				contentResolver.insert(contentUri, contentValues);
			}
		}
		catch (Exception e)
		{
			Log.w("DatabaseManager", "insertLog : " + context.getString(R.string.log_database_manager_error_request) + " : " + e);;
		}
	}
	
	public Cursor queryLogByPriority(Context context, Cursor cursor, String orderBy, int priority)
	{
		try
		{
			contentResolver = context.getContentResolver();
			contentUri = DatabaseProvider.TELEPHONE_LOG_CONTENT_URI;
			
			if((orderBy == null) || (orderBy == ""))
			{
				cursor = contentResolver.query(contentUri, null, "PRIORITY <= " + priority, null, TELEPHONE_LOG.DEFAULT_SORT_ORDER);
			}
			else
			{
				cursor = contentResolver.query(contentUri, null, "PRIORITY <= " + priority, null, orderBy);
	
			}
			
			return cursor;
		}
		catch (Exception e)
		{
			Log.w("DatabaseManager", "queryLogByPriority : " + context.getString(R.string.log_database_manager_error_request) + " : " + e);
			
			return cursor;
		}
	}
	
	public Cursor queryAllLog(Context context, Cursor cursor, String orderBy)
	{
		try
		{
			contentResolver = context.getContentResolver();
			contentUri = DatabaseProvider.TELEPHONE_LOG_CONTENT_URI;
			
			if((orderBy == null) || (orderBy == ""))
			{
				cursor = contentResolver.query(contentUri, null, null, null, TELEPHONE_LOG.DEFAULT_SORT_ORDER);
			}
			else
			{
				cursor = contentResolver.query(contentUri, null, null, null, orderBy);
			}
			
			return cursor;
		}
		catch (Exception e)
		{
			Log.w("DatabaseManager", "queryAllLog : " + context.getString(R.string.log_database_manager_error_request) + " : " + e);
			
			return cursor;
		}
	}
	
	public void deleteAllLog(Context context)
	{
		try
		{
			contentResolver = context.getContentResolver();
			contentUri = DatabaseProvider.TELEPHONE_LOG_CONTENT_URI;
			contentResolver.delete(contentUri,null,null);
		}
		catch (Exception e)
		{
			Log.w("DatabaseManager", "deleteAllLog : " + context.getString(R.string.log_database_manager_error_request) + " : " + e);;
		}
	}
	
	public Cursor queryAllFilter(Context context, Cursor cursor, String orderBy)
	{
		try
		{
			contentResolver = context.getContentResolver();
			contentUri = DatabaseProvider.CONTACT_FILTER_CONTENT_URI;
			
			if((orderBy == null) || (orderBy == ""))
			{
				cursor = contentResolver.query(contentUri, null, null, null, CONTACT_FILTER.DEFAULT_SORT_ORDER);
			}
			else
			{
				cursor = contentResolver.query(contentUri, null, null, null, orderBy);
			}
			
			return cursor;
		}
		catch (Exception e)
		{
			Log.w("DatabaseManager", "queryAllFilter : " + context.getString(R.string.log_database_manager_error_request) + " : " + e);
			
			return cursor;
		}
	}
	
	public Cursor queryFilterByRecordable(Context context, Cursor cursor, String orderBy, int recordable)
	{
		try
		{
			contentResolver = context.getContentResolver();
			contentUri = DatabaseProvider.CONTACT_FILTER_CONTENT_URI;
			
			if((orderBy == null) || (orderBy == ""))
			{
				cursor = contentResolver.query(contentUri, null, null, null, CONTACT_FILTER.DEFAULT_SORT_ORDER);
			}
			else
			{
				cursor = contentResolver.query(contentUri, null, "RECORDABLE = " + recordable, null, orderBy);
			}
			
			return cursor;
		}
		catch (Exception e)
		{
			Log.w("DatabaseManager", "queryAllFilter : " + context.getString(R.string.log_database_manager_error_request) + " : " + e);
			
			return cursor;
		}
	}
	
	public void insertFilter(Context context, String number, int recordable)
	{
		try
		{
			contentResolver = context.getContentResolver();
			contentUri = DatabaseProvider.CONTACT_FILTER_CONTENT_URI;
			ContentValues contentValues = new ContentValues();
			
			contentValues.put(CONTACT_FILTER.NUMBER, number);
			contentValues.put(CONTACT_FILTER.RECORDABLE, recordable);
			
			contentResolver.insert(contentUri, contentValues);
		}
		catch (Exception e)
		{
			Log.w("DatabaseManager", "insertFilter : " + context.getString(R.string.log_database_manager_error_request) + " : " + e);;
		}
	}
	
	public void updateFilter(Context context, String number, int recordable)
	{
		try
		{
			contentResolver = context.getContentResolver();
			contentUri = DatabaseProvider.CONTACT_FILTER_CONTENT_URI;
	
			Cursor cursor = contentResolver.query(contentUri, null, CONTACT_FILTER.NUMBER + " = \"" + number + "\"", null, CONTACT_FILTER.DEFAULT_SORT_ORDER);
			
        	if (cursor.moveToFirst())
        	{
        		String contactNumber = cursor.getString(cursor.getColumnIndex(CONTACT_FILTER.NUMBER));
        		
        		ContentValues contentValues = new ContentValues();
        		contentValues.put(CONTACT_FILTER.RECORDABLE, recordable);
				
				contentResolver.update(contentUri, contentValues, CONTACT_FILTER.NUMBER + " = \"" + contactNumber + "\"", null);
        	}
		}
		catch (Exception e)
		{
			Log.w("DatabaseManager", "updateFilter : " + context.getString(R.string.log_database_manager_error_request) + " : " + e);;
		}
	}
	
	public void deleteFilter(Context context, String number)
	{
		try
		{
			contentResolver = context.getContentResolver();
			contentUri = DatabaseProvider.CONTACT_FILTER_CONTENT_URI;
			contentResolver.delete(contentUri,CONTACT_FILTER.NUMBER + " = \"" + number + "\"",null);
		}
		catch (Exception e)
		{
			Log.w("DatabaseManager", "deleteFilter : " + context.getString(R.string.log_database_manager_error_request) + " : " + e);
		}
	}
	
	public void deleteAllFilter(Context context)
	{
		try
		{
			contentResolver = context.getContentResolver();
			contentUri = DatabaseProvider.CONTACT_FILTER_CONTENT_URI;
			contentResolver.delete(contentUri,null,null);
		}
		catch (Exception e)
		{
			Log.w("DatabaseManager", "deleteAllFilter : " + context.getString(R.string.log_database_manager_error_request) + " : " + e);
		}
	}
}