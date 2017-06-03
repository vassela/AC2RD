/**
 * @file RecordService.java
 * @brief Record service class
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

package fr.vassela.acrrd.recorder;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.media.AudioManager;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import fr.vassela.acrrd.R;
import fr.vassela.acrrd.recorder.IRecordService;
import fr.vassela.acrrd.database.DatabaseManager;
import fr.vassela.acrrd.main.Preferences;
import fr.vassela.acrrd.notifier.TelephoneCallNotifier;

public class RecordService  extends Service
{
    public static boolean isRunning;
    
    public static int callDirection;
    public static String telephoneNumber;
    public static boolean doubleCall;
    public static String doubleCallNumber;
    
    private static boolean isMicrophoneMute;
    
    private static int audioSource;
    private static int audioFormat;
    private static String storagePath;
    private static String recordFolderPath;
    private static String recordFileName;
    
    private static Date callStartTime;
    private long CallDuration;
    
    private RecordFileMaker recordFileMaker;
    private RecordFileWriter recordFileWriter;
    private DatabaseManager databaseManager;
    private TelephoneCallNotifier telephoneCallNotifier;
    
    private static SimpleDateFormat simpleDurationFormat;

    private IBinder binder = new IRecordService.Stub()
	{
    	public boolean isRunning()
    	{
    		return isRunning;
    	}
    	
		public void setRecord(int direction, String number)
		{
			callDirection = direction;
			telephoneNumber = number;
		}
		
		public int getCallDirection()
		{
			return callDirection;
		}

		public String getTelephoneNumber()
		{
			return telephoneNumber;
		}
		
		public void setDoubleCall(String number)
		{
			doubleCall = true;
			if (doubleCallNumber == "")
			{
				doubleCallNumber = number;
			}
			else
			{
				doubleCallNumber = doubleCallNumber + ", " + number;
			}
		}
		
		public boolean getDoubleCall()
    	{
    		return doubleCall;
    	}
		
		public String getDoubleCallNumber()
		{
			return doubleCallNumber;
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
    	databaseManager = new DatabaseManager();
    	telephoneCallNotifier = new TelephoneCallNotifier();
    	
    	try
		{
	        super.onCreate();
	
	        isRunning = false;
	        callDirection = 0;
	        telephoneNumber = "";
	        doubleCall = false;
	        doubleCallNumber = "";
	        
	        isMicrophoneMute = false;
	        
	        audioSource = 4;
	        audioFormat = 1;
	        storagePath = String.valueOf(Environment.getExternalStorageDirectory());
	        recordFolderPath = "";
	        recordFileName = "";
	        
	        callStartTime = null;
	        CallDuration = 0;
	        
	        recordFileMaker = null;
	        recordFileWriter = null;
	        
	        simpleDurationFormat = new SimpleDateFormat("HH:mm:ss");
		}
		catch (Exception e)
		{
			Log.w("RecordService", "onCreate : " + getApplicationContext().getString(R.string.log_record_service_error_create) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_record_service_error_create), new Date().getTime(), 2, false);
		}
    }
    
    private boolean onStartDoubleCall(Intent intent)
    {
    	try
		{
	    	if (isRunning)
	    	{
	    		if(intent.getExtras() != null)
	    		{
	    			doubleCall = intent.getBooleanExtra("doubleCall", false);
	    			
	    			if (doubleCallNumber == "")
	    			{
	    				doubleCallNumber = intent.getStringExtra("doubleCallNumber");
	    			}
	    			else
	    			{
	    				doubleCallNumber = doubleCallNumber + ", " + intent.getStringExtra("doubleCallNumber");
	    			}
	    		}
	    		
	    		return true;
	    	}
	    	else
	    	{
	    		return false;
	    	}
		}
		catch (Exception e)
		{
			Log.w("RecordService", "onStartDoubleCall : " + getApplicationContext().getString(R.string.log_record_service_error_get_extras_double_call) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_record_service_error_get_extras_double_call), new Date().getTime(), 2, false);
			return false;
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
			Log.w("RecordService", "onStartCall : " + getApplicationContext().getString(R.string.log_record_service_error_get_extras_call) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_record_service_error_get_extras_call), new Date().getTime(), 2, false);
		}
    }
    
    private void onSetCallPreferences()
    {
    	try
		{
	    	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	    	
	    	if((callDirection == 0) || (callDirection == 1))
	    	{
	    		if(callDirection == 1) // TODO temporary fix
	    		{
	    			audioSource = 1;
	    		}
	    		else
	    		{
	    			audioSource = Integer.parseInt(sharedPreferences.getString("preferences_audio_source", "4"));
	    		}
	    	}
	    	else
	    	{
	    		audioSource = 1;
	    	}
	    	
	    	audioFormat = Integer.parseInt(sharedPreferences.getString("preferences_audio_format", "1"));
	    	
	    	if ((Preferences.getSharedStoragePath(getApplicationContext()) != "") && (Preferences.getSharedStoragePath(getApplicationContext()) != null))
	    	{
	    		storagePath = Preferences.getSharedStoragePath(getApplicationContext());
	    	}
		}
		catch (Exception e)
		{
			Log.w("RecordService", "onSetCallPreferences : " + getApplicationContext().getString(R.string.log_record_service_error_get_shared_preferences) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_record_service_error_get_shared_preferences), new Date().getTime(), 2, false);
		}
    }
    
    private boolean onCreateRecordFolder()
    {
    	try
		{
    		boolean retCreateRecordFolder = false;
    		
	    	recordFileMaker = new RecordFileMaker();
	    	recordFolderPath = storagePath + "/" + getApplicationContext().getString(R.string.app_acronym);;
	    	retCreateRecordFolder = recordFileMaker.createRecordFolder(getApplicationContext(), recordFolderPath);
	    	
	    	if(retCreateRecordFolder == false)
	    	{
				Log.e("RecordService", "onCreateRecordFolder : " + getApplicationContext().getString(R.string.log_record_service_error_create_record_folder));
				databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_record_service_error_create_record_folder), new Date().getTime(), 1, false);
				
	    		return false;
	    	}
	    	else
	    	{
	    		return true;
	    	}
		}
		catch (Exception e)
		{
			Log.e("RecordService", "onCreateRecordFolder : " + getApplicationContext().getString(R.string.log_record_service_error_create_record_folder) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_record_service_error_create_record_folder), new Date().getTime(), 1, false);
			return false;
		}
    }
    
    private boolean onUnMuteMicrophone()
    {
    	try
		{
	    	AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
	    	
	    	isMicrophoneMute = audioManager.isMicrophoneMute();
	    	
	    	if(isMicrophoneMute == true)
	    	{
	    		audioManager.setMicrophoneMute(false);
	    	}
	    	
	    	return true;
		}
		catch (Exception e)
		{
			Log.e("RecordService", "onUnMuteMicrophone : " + getApplicationContext().getString(R.string.log_record_service_error_unmute_microphone) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_record_service_error_unmute_microphone), new Date().getTime(), 1, false);
			return false;
		}
    }
    
    private File onCreateRecordFile()
    {
    	try
		{
    		boolean isRecordFileWriter = false;
        	File recordFile = null;
        	
	    	callStartTime = new Date();
	    	recordFile = recordFileMaker.createRecordFile(getApplicationContext(), callStartTime, audioFormat, recordFolderPath);
	    	
	    	if(recordFile == null)
	    	{
	    		return null;
	    	}
	    	
	    	recordFileName = recordFile.getName();

	    	recordFileWriter = new RecordFileWriter();
	    	isRecordFileWriter = recordFileWriter.start(getApplicationContext(), recordFile, audioSource, audioFormat);
	    	
	    	if(isRecordFileWriter == true)
	    	{
	    		return recordFile;
	    	}
	    	else
	    	{
	    		onDeleteRecordFile(recordFile);
	    		return null;
	    	}
		}
		catch (Exception e)
		{
			Log.e("RecordService", "onCreateRecordFile : " + getApplicationContext().getString(R.string.log_record_service_error_create_record_file) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_record_service_error_create_record_file), new Date().getTime(), 1, false);
			return null;
		}
    }
    
    private void onDeleteRecordFile(File recordFile)
    {
    	isRunning = false;
		callDirection = 0;
        telephoneNumber = "";
        doubleCall = false;
        doubleCallNumber = "";
        
        isMicrophoneMute = false;
        
        audioSource = 4;
        audioFormat = 1;
        
        recordFolderPath = "";
        recordFileName = "";
        
        callStartTime = null;
        CallDuration = 0;
        
        recordFileWriter = null;
		recordFileMaker = null;
        
		boolean retDeleteRecordFile = false;
		
		try
		{
			retDeleteRecordFile = recordFile.delete();
			
			if(retDeleteRecordFile == false)
			{
    			Log.w("RecordService", "onDeleteRecordFile : " + getApplicationContext().getString(R.string.log_record_service_error_delete_record_file));
    			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_record_service_error_delete_record_file), new Date().getTime(), 2, false);
			}
			
	        storagePath = String.valueOf(Environment.getExternalStorageDirectory());
		}
		catch (Exception e)
		{
			Log.w("RecordService", "onDeleteRecordFile : " + getApplicationContext().getString(R.string.log_record_service_error_delete_record_file) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_record_service_error_delete_record_file), new Date().getTime(), 2, false);
		}
    }
    
    private void onStartRecordFileNotify()
    {
    	try
		{
	    	isRunning = true;
			
			Log.i("RecordService", "onStartRecordFileNotify : " + getApplicationContext().getString(R.string.log_record_service_create_record_file_start_time) + " : " + telephoneNumber);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_record_service_create_record_file_start_time) + " : " + telephoneNumber, new Date().getTime(), 0, false);
			
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
	        {
				if((callDirection == 0) || (callDirection == 1))
				{
					telephoneCallNotifier.displayNotification(getApplicationContext(), getApplicationContext().getString(R.string.notification_record_service_on_start_ticker), getApplicationContext().getString(R.string.notification_record_service_on_start_content_title), getApplicationContext().getString(R.string.notification_record_service_on_start_content_text) + " : " + telephoneNumber, false, true, false);
				}
				else
				{
					telephoneCallNotifier.displayNotification(getApplicationContext(), getApplicationContext().getString(R.string.notification_record_service_on_start_microphone_ticker), getApplicationContext().getString(R.string.notification_record_service_on_start_microphone_content_title), getApplicationContext().getString(R.string.notification_record_service_on_start_microphone_content_text), false, true, false);
				}
	        }
			else
			{
				if((callDirection == 0) || (callDirection == 1))
				{
					telephoneCallNotifier.displayCompatNotification(getApplicationContext(), getApplicationContext().getString(R.string.notification_record_service_on_start_ticker), getApplicationContext().getString(R.string.notification_record_service_on_start_content_title), getApplicationContext().getString(R.string.notification_record_service_on_start_content_text) + " : " + telephoneNumber, false, true, false);
				}
				else
				{
					telephoneCallNotifier.displayCompatNotification(getApplicationContext(), getApplicationContext().getString(R.string.notification_record_service_on_start_microphone_ticker), getApplicationContext().getString(R.string.notification_record_service_on_start_microphone_content_title), getApplicationContext().getString(R.string.notification_record_service_on_start_microphone_content_text), false, true, false);
				}
			}
		}
		catch (Exception e)
		{
			Log.w("RecordService", "onStartRecordFileNotify : " + getApplicationContext().getString(R.string.log_record_service_create_record_file_start_notify) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_record_service_create_record_file_start_notify), new Date().getTime(), 2, false);
		}
    }
    
    @Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
    	boolean isStartDoubleCall = onStartDoubleCall(intent);
    	
    	if(isStartDoubleCall == true)
    	{
    		return 1;
    	}
    	
    	onStartCall(intent);
    	
    	onSetCallPreferences();
    	
    	boolean isCreateCallRecordFolder = onCreateRecordFolder();
    	
    	if(isCreateCallRecordFolder == false)
    	{
    		return -1;
    	}
    	
    	boolean isMicrophoneUnMute = onUnMuteMicrophone();
    	
    	if(isMicrophoneUnMute == false)
    	{
    		return -1;
    	}

    	File recordFile = onCreateRecordFile();
    	
    	if(recordFile == null)
    	{
    		return -1;
    	}
    	
    	onStartRecordFileNotify();

    	return 0;
	}
    
    private void onDestroyRecordFile()
    {
    	try
		{
	    	if(isRunning == true)
	    	{
	    		boolean retRecordFileWriter = false;
	    		
	    		retRecordFileWriter = recordFileWriter.stop(getApplicationContext());
	    		
	    		if(retRecordFileWriter == true)
	    		{
			    	Date callEndTime = new Date();
			    	
			    	CallDuration = callEndTime.getTime() - callStartTime.getTime();
			    	
			    	int isDoubleCall = 0;
			    	
			    	if(doubleCall == true)
			    	{
			    		isDoubleCall = 1;
			    	}
			    	
			    	if((callDirection == 0) || (callDirection == 1))
			    	{
			    		databaseManager.insertCall(getApplicationContext(), callDirection, telephoneNumber, isDoubleCall, doubleCallNumber, callStartTime.getTime(), callEndTime.getTime(), CallDuration, recordFolderPath, recordFileName, audioFormat, 0, getApplicationContext().getString(R.string.record_service_call_description) + " (" + telephoneNumber + ")");
			    	}
			    	else
			    	{
			    		databaseManager.insertCall(getApplicationContext(), callDirection, telephoneNumber, isDoubleCall, doubleCallNumber, callStartTime.getTime(), callEndTime.getTime(), CallDuration, recordFolderPath, recordFileName, audioFormat, 0, getApplicationContext().getString(R.string.record_service_dictaphone_description) + " (" + telephoneNumber + ")");
			    	}
	    		}
	    	}
		}
		catch (Exception e)
		{
			Log.e("RecordService", "onDestroyRecordFile : " + getApplicationContext().getString(R.string.log_record_service_error_create_record_file_end_time) + " : " + telephoneNumber + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_record_service_error_create_record_file_end_time) + " : " + telephoneNumber, new Date().getTime(), 1, false);
		}
    	finally
    	{
    		onDestroyRecordFileNotify();
    	}
    }
    
    private void onDestroyRecordFileNotify()
    {
    	try
		{
	    	Log.i("RecordService", "onDestroyRecordFileNotify : " + getApplicationContext().getString(R.string.log_record_service_create_record_file_end_time) + " : " + telephoneNumber);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_record_service_create_record_file_end_time) + " : " + telephoneNumber, new Date().getTime(), 0, false);
			
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			boolean isPreferencesRecordActivated = sharedPreferences.getBoolean("preferences_record_activate",false);
			
			if(isPreferencesRecordActivated == false)
			{
				telephoneCallNotifier.cancelOngoingNotification(getApplicationContext());
			}
			
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
	        {
				if((callDirection == 0) || (callDirection == 1))
				{
					telephoneCallNotifier.displayNotification(getApplicationContext(), getApplicationContext().getString(R.string.notification_record_service_new_record_file_ticker) + " : " + telephoneNumber + " (" + simpleDurationFormat.format(CallDuration - 3600000) + ")", getApplicationContext().getString(R.string.notification_record_service_new_record_file_content_title), getApplicationContext().getString(R.string.notification_record_service_new_record_file_content_text) + " : " + telephoneNumber + " (" + simpleDurationFormat.format(CallDuration - 3600000) + ")", true, false, false);
				}
				else
				{
					telephoneCallNotifier.displayNotification(getApplicationContext(), getApplicationContext().getString(R.string.notification_record_service_new_microphone_record_file_ticker), getApplicationContext().getString(R.string.notification_record_service_new_microphone_record_file_content_title), getApplicationContext().getString(R.string.notification_record_service_new_microphone_record_file_content_text), true, false, false);
				}
				
				if(isPreferencesRecordActivated == true)
				{
					telephoneCallNotifier.displayNotification(getApplicationContext(), getApplicationContext().getString(R.string.notification_preferences_activate_record_ticker), getApplicationContext().getString(R.string.notification_preferences_activate_record_content_title), getApplicationContext().getString(R.string.notification_preferences_activate_record_content_text), false, true, true);
				}
	        }
			else
			{
				if((callDirection == 0) || (callDirection == 1))
				{
					telephoneCallNotifier.displayCompatNotification(getApplicationContext(), getApplicationContext().getString(R.string.notification_record_service_new_record_file_ticker) + " : " + telephoneNumber + " (" + simpleDurationFormat.format(CallDuration - 3600000) + ")", getApplicationContext().getString(R.string.notification_record_service_new_record_file_content_title), getApplicationContext().getString(R.string.notification_record_service_new_record_file_content_text) + " : " + telephoneNumber + " (" + simpleDurationFormat.format(CallDuration - 3600000) + ")", true, false, false);
				}
				else
				{
					telephoneCallNotifier.displayCompatNotification(getApplicationContext(), getApplicationContext().getString(R.string.notification_record_service_new_microphone_record_file_ticker), getApplicationContext().getString(R.string.notification_record_service_new_microphone_record_file_content_title), getApplicationContext().getString(R.string.notification_record_service_new_microphone_record_file_content_text), true, false, false);
				}
				
				if(isPreferencesRecordActivated == true)
				{
					telephoneCallNotifier.displayCompatNotification(getApplicationContext(), getApplicationContext().getString(R.string.notification_preferences_activate_record_ticker), getApplicationContext().getString(R.string.notification_preferences_activate_record_content_title), getApplicationContext().getString(R.string.notification_preferences_activate_record_content_text), false, true, true);
				}
			}
		}
		catch (Exception e)
		{
			Log.e("RecordService", "onDestroyRecordFileNotify : " + getApplicationContext().getString(R.string.log_record_service_error_create_record_file_notify) + " : " + telephoneNumber + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_record_service_error_create_record_file_notify) + " : " + telephoneNumber, new Date().getTime(), 1, false);
		}
    }
    
    private void onResetMicrophoneMute()
    {
    	try
		{
    		if(isMicrophoneMute == true)
	    	{
    			AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

	    		audioManager.setMicrophoneMute(true);
	    	}
		}
		catch (Exception e)
		{
			Log.e("RecordService", "onResetMicrophoneMute : " + getApplicationContext().getString(R.string.log_record_service_reset_mute_microphone) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_record_service_reset_mute_microphone), new Date().getTime(), 1, false);
		}
    }
    
    private void onResetRecordParameters()
    {
    	try
		{
	    	isRunning = false;
	        callDirection = 0;
	        telephoneNumber = "";
	        doubleCall = false;
	        doubleCallNumber = "";
	        
	        isMicrophoneMute = false;
	        
	        audioSource = 4;
	        audioFormat = 1;
	        storagePath = String.valueOf(Environment.getExternalStorageDirectory());
	        recordFolderPath = "";
	        recordFileName = "";
	        
	        callStartTime = null;
	        CallDuration = 0;
	        
	        recordFileWriter = null;
			recordFileMaker = null;
		}
		catch (Exception e)
		{
			Log.w("RecordService", "onResetRecordParameters : " + getApplicationContext().getString(R.string.log_record_service_error_destroy) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_record_service_error_destroy), new Date().getTime(), 2, false);
		}
    }

    public void onDestroy()
    {
    	super.onDestroy();
    	
    	onDestroyRecordFile();
    	
    	onResetMicrophoneMute();
    	
    	onResetRecordParameters();
    }
}