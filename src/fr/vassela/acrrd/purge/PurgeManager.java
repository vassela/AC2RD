/**
 * @file PurgeManager.java
 * @brief Purge manager class
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

package fr.vassela.acrrd.purge;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import fr.vassela.acrrd.R;
import fr.vassela.acrrd.database.DatabaseManager;
import fr.vassela.acrrd.database.DatabaseProvider;
import fr.vassela.acrrd.database.DatabaseDefinition.TELEPHONE_CALL;
import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

public class PurgeManager extends Application
{
	private ContentResolver contentResolver = null;
	private Uri contentUri = null;
	
    private DatabaseManager databaseManager = new DatabaseManager();
    
    public void purgeRecordFile(Context context, String recordFolderPath, String recordFileName)
    {
    	try
		{
	    	File recordFile = new File(recordFolderPath + "/" + recordFileName);
			
			if (!recordFile.exists())
			{
				Log.w("PurgeManager", "purgeRecordFile : " + context.getString(R.string.log_purge_manager_error_record_file_not_exists) + " : " + recordFolderPath + " / " + recordFileName);
				databaseManager.insertLog(context, "" + context.getString(R.string.log_purge_manager_error_record_file_not_exists) + " : " + recordFolderPath + " / " + recordFileName, new Date().getTime(), 2, false);
			}
			else
			{
				if (!recordFile.canWrite())
				{
					Log.w("PurgeManager", "purgeRecordFile : " + context.getString(R.string.log_purge_manager_error_record_file_cant_delete) + " : " + recordFolderPath + " / " + recordFileName);
					databaseManager.insertLog(context, "" + context.getString(R.string.log_purge_manager_error_record_file_cant_delete) + " : " + recordFolderPath + " / " + recordFileName, new Date().getTime(), 2, false);
	
				}
				else
				{
					recordFile.delete();
					
					Log.d("PurgeManager", "purgeRecordFile : " + context.getString(R.string.log_purge_manager_delete_record_file) + " : " + recordFolderPath + " / " + recordFileName);
					databaseManager.insertLog(context, "" + context.getString(R.string.log_purge_manager_delete_record_file) + " : " + recordFolderPath + " / " + recordFileName, new Date().getTime(), 3, false);
				}
			}
		}
		catch (Exception e)
		{
			Log.w("PurgeManager", "purgeRecordFile : " + context.getString(R.string.log_purge_manager_error_purge_record_file) + " : " + recordFolderPath + " / " + recordFileName + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_purge_manager_error_purge_record_file) + " : " + recordFolderPath + " / " + recordFileName, new Date().getTime(), 2, false);
		}
    }
    
    public void purgeRecord(Context context, String fileName)
	{
		try
		{
			contentResolver = context.getContentResolver();
			contentUri = DatabaseProvider.TELEPHONE_CALL_CONTENT_URI;
			
        	Cursor cursor = contentResolver.query(contentUri, null, TELEPHONE_CALL.RECORD_FILENAME + " = \"" + fileName + "\" AND LOCKED = 0" ,null, TELEPHONE_CALL.DEFAULT_SORT_ORDER);		        	
			
        	if (cursor.moveToFirst())
        	{
				String recordFolderPath = cursor.getString(cursor.getColumnIndex(TELEPHONE_CALL.RECORD_PATH));
        		String recordFileName = cursor.getString(cursor.getColumnIndex(TELEPHONE_CALL.RECORD_FILENAME));
        		
				purgeRecordFile(context, recordFolderPath, recordFileName);
				
				contentResolver.delete(contentUri,TELEPHONE_CALL.RECORD_FILENAME + " = \"" + recordFileName + "\"",null);
        	}
		}
		catch (Exception e)
		{
			Log.w("PurgeManager", "purgeRecord : " + context.getString(R.string.log_purge_manager_error_purge_record) + " : " + fileName + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_purge_manager_error_purge_record) + " : " + fileName, new Date().getTime(), 2, false);
		}
	}
    
    public void lockRecord(Context context, String fileName)
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
        		contentValues.put(TELEPHONE_CALL.LOCKED, 1);
				
				contentResolver.update(contentUri, contentValues, TELEPHONE_CALL.RECORD_FILENAME + " = \"" + recordFileName + "\"", null);
        	}
		}
		catch (Exception e)
		{
			Log.w("PurgeManager", "lockRecord : " + context.getString(R.string.log_purge_manager_error_lock_record_file) + " : " + fileName + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_purge_manager_error_lock_record_file) + " : " + fileName, new Date().getTime(), 2, false);
		}
	}
    
    public void unlockRecord(Context context, String fileName)
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
        		contentValues.put(TELEPHONE_CALL.LOCKED, 0);
				
				contentResolver.update(contentUri, contentValues, TELEPHONE_CALL.RECORD_FILENAME + " = \"" + recordFileName + "\"", null);
        	}
		}
		catch (Exception e)
		{
			Log.w("PurgeManager", "unlockRecord : " + context.getString(R.string.log_purge_manager_error_unlock_record_file) + " : " + fileName + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_purge_manager_error_unlock_record_file) + " : " + fileName, new Date().getTime(), 2, false);
		}
	}
	
	public void purgeStaleRecords(Context context)
	{
		try
		{
			SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context);
			int purgeRetention = Integer.parseInt(SP.getString("preferences_purge_retention", "1"));
			
			contentResolver = context.getContentResolver();
			contentUri = DatabaseProvider.TELEPHONE_CALL_CONTENT_URI;
			
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DATE, -purgeRetention);
			
        	Cursor cursor = contentResolver.query(contentUri, null, "STARTING_DATE < " + calendar.getTime().getTime() + " AND LOCKED = 0" ,null, TELEPHONE_CALL.DEFAULT_SORT_ORDER);		        	
			
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
        	{
				String recordFolderPath = cursor.getString(cursor.getColumnIndex(TELEPHONE_CALL.RECORD_PATH));
        		String recordFileName = cursor.getString(cursor.getColumnIndex(TELEPHONE_CALL.RECORD_FILENAME));
        		
				purgeRecordFile(context, recordFolderPath, recordFileName);
				
				contentResolver.delete(contentUri,TELEPHONE_CALL.RECORD_FILENAME + " = \"" + recordFileName + "\"",null);
        	}
		}
		catch (Exception e)
		{
			Log.w("PurgeManager", "purgeStaleRecords : " + context.getString(R.string.log_purge_manager_error_purge_stale_records) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_purge_manager_error_purge_stale_records), new Date().getTime(), 2, false);
		}
	}
	
	public void purgeAllRecords(Context context)
	{
		try
		{
			SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context);
			int purgeRetention = Integer.parseInt(SP.getString("preferences_purge_retention", "1"));
			
			contentResolver = context.getContentResolver();
			contentUri = DatabaseProvider.TELEPHONE_CALL_CONTENT_URI;
			
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DATE, -purgeRetention);
			
        	Cursor cursor = contentResolver.query(contentUri, null, null, null, TELEPHONE_CALL.DEFAULT_SORT_ORDER);		        	
			
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
        	{
				String recordFolderPath = cursor.getString(cursor.getColumnIndex(TELEPHONE_CALL.RECORD_PATH));
        		String recordFileName = cursor.getString(cursor.getColumnIndex(TELEPHONE_CALL.RECORD_FILENAME));
        		
				purgeRecordFile(context, recordFolderPath, recordFileName);
				
				contentResolver.delete(contentUri,TELEPHONE_CALL.RECORD_FILENAME + " = \"" + recordFileName + "\"",null);
        	}
		}
		catch (Exception e)
		{
			Log.w("PurgeManager", "purgeAllRecords : " + context.getString(R.string.log_purge_manager_error_purge_all_records) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_purge_manager_error_purge_all_records), new Date().getTime(), 2, false);
		}
	}
	
	public void purgeOldestRecord(Context context)
	{
		try
		{
			Log.d("purgeLastRecord::start", "run");
			
			contentResolver = context.getContentResolver();
			contentUri = DatabaseProvider.TELEPHONE_CALL_CONTENT_URI;
			
        	Cursor cursor = contentResolver.query(contentUri, null, null, null, "STARTING_DATE ASC");		        	
			
        	if (cursor.moveToFirst())
        	{
        		String recordFolderPath = cursor.getString(cursor.getColumnIndex(TELEPHONE_CALL.RECORD_PATH));
        		String recordFileName = cursor.getString(cursor.getColumnIndex(TELEPHONE_CALL.RECORD_FILENAME));
        		
        		purgeRecordFile(context, recordFolderPath, recordFileName);
        		
    			contentResolver.delete(contentUri,TELEPHONE_CALL.RECORD_FILENAME + " = \"" + recordFileName + "\"",null);
        	}
		}
		catch (Exception e)
		{
			Log.w("PurgeManager", "purgeOldestRecord : " + context.getString(R.string.log_purge_manager_error_purge_oldest_record) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_purge_manager_error_purge_oldest_record), new Date().getTime(), 2, false);
		}
	}
}