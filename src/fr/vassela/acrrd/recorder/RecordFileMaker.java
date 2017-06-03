/**
 * @file RecordFileMaker.java
 * @brief Record file maker class
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

import fr.vassela.acrrd.R;
import fr.vassela.acrrd.database.DatabaseManager;
import android.app.Application;
import android.content.Context;
import android.media.MediaRecorder;
import android.util.Log;

public class RecordFileMaker extends Application
{
	private DatabaseManager databaseManager = new DatabaseManager();
	
	public boolean createRecordFolder(Context context, String recordFolderPath)
	{
		try
		{
			File recordFolder = new File(recordFolderPath);

			if (!recordFolder.exists())
			{
	            try
	            {
	            	recordFolder.mkdirs();
	            }
	            catch (Exception e)
	            {
	    			Log.w("RecordFileMaker", "createRecordFolder : " + context.getString(R.string.log_record_file_maker_error_create_folder) + " : " + recordFolderPath + " : " + e);
	    			databaseManager.insertLog(context, "" + context.getString(R.string.log_record_file_maker_error_create_folder) + " : " + recordFolderPath, new Date().getTime(), 2, false);
	            	return false;
	            }
	            finally
	            {
	    			Log.i("RecordFileMaker", "createRecordFolder : " + context.getString(R.string.log_record_file_maker_create_folder) + " : " + recordFolderPath);
	    			databaseManager.insertLog(context, "" + context.getString(R.string.log_record_file_maker_create_folder) + " : " + recordFolderPath, new Date().getTime(), 3, false);
	            }
	        }
			else
			{
				if (!recordFolder.canWrite())
				{
	    			Log.w("RecordFileMaker", "createRecordFolder : " + context.getString(R.string.log_record_file_maker_folder_read_only) + " : " + recordFolderPath);
	    			databaseManager.insertLog(context, "" + context.getString(R.string.log_record_file_maker_folder_read_only) + " : " + recordFolderPath, new Date().getTime(), 2, false);
					return false;
	            }
	        }
		}
		catch (Exception e)
        {
			Log.w("RecordFileMaker", "createRecordFolder : " + context.getString(R.string.log_record_file_maker_error_create_folder) + " : " + recordFolderPath + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_record_file_maker_error_create_folder) + " : " + recordFolderPath, new Date().getTime(), 2, false);
			return false;
        }
		return true;
	}
	
	@SuppressWarnings("deprecation")
	public File createRecordFile(Context context, Date callStartTime, int audioFormat, String recordFolderPath)
	{
		File recordFile = null;
		
		try
		{
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmssSS");
			String recordFileName = simpleDateFormat.format(callStartTime);
			String recordFileExtension = "";
			File recordFolder = new File(recordFolderPath);
			
			if(audioFormat >= 10)
			{
				recordFileExtension = ".wav";
			}
			else
			{
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
		        {
					switch (audioFormat)
					{
				        case MediaRecorder.OutputFormat.THREE_GPP:
				        	recordFileExtension = ".3gpp";
				            break;
				        case MediaRecorder.OutputFormat.MPEG_4:
				        	recordFileExtension = ".mp3";
				            break;
				        case MediaRecorder.OutputFormat.AMR_NB:
				        	recordFileExtension = ".amr";
				            break;
			        }
		        }
				else
				{
					switch (audioFormat)
					{
				        case MediaRecorder.OutputFormat.THREE_GPP:
				        	recordFileExtension = ".3gpp";
				            break;
				        case MediaRecorder.OutputFormat.MPEG_4:
				        	recordFileExtension = ".mp3";
				            break;
				        case MediaRecorder.OutputFormat.RAW_AMR:
				        	recordFileExtension = ".amr";
				            break;
			        }
				}
			}
			
			try
			{
	            recordFile = File.createTempFile(recordFileName, recordFileExtension, recordFolder);
	        }
			catch (Exception e)
			{
				Log.w("RecordFileMaker", "createRecordFile : " + context.getString(R.string.log_record_file_maker_error_create_file) + " : " + recordFolderPath + "/" + recordFile.getName() + " : " + e);
				databaseManager.insertLog(context, "" + context.getString(R.string.log_record_file_maker_error_create_file) + " : " + recordFolderPath + "/" + recordFile.getName(), new Date().getTime(), 2, false);
	            return null;
	        }
			finally
			{
				Log.i("RecordFileMaker", "createRecordFile : " + context.getString(R.string.log_record_file_maker_create_file) + " : " + recordFolderPath + "/" + recordFile.getName());
				databaseManager.insertLog(context, "" + context.getString(R.string.log_record_file_maker_create_file) + " : " + recordFolderPath + "/" + recordFile.getName(), new Date().getTime(), 3, false);
			}
		}
		catch (Exception e)
		{
			Log.w("RecordFileMaker", "createRecordFile : " + context.getString(R.string.log_record_file_maker_error_create_file) + " : " + recordFolderPath + "/" + recordFile.getName() + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_record_file_maker_error_create_file) + " : " + recordFolderPath + "/" + recordFile.getName(), new Date().getTime(), 2, false);
            return null;
        }
		
		return recordFile;
	}
	
	@SuppressWarnings("deprecation")
	public String getRecordFormat(Context context, int audio)
	{
		String audioFormat = "";
		
		try
		{
			if(audio >= 10)
			{
				audioFormat = "WAV";
			}
			else
			{
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
		        {
					switch (audio)
					{
				        case MediaRecorder.OutputFormat.THREE_GPP:
				        	audioFormat = "3GPP";
				            break;
				        case MediaRecorder.OutputFormat.MPEG_4:
				        	audioFormat = "MP3";
				            break;
				        case MediaRecorder.OutputFormat.AMR_NB:
				        	audioFormat = "AMR";
				            break;
			        }
		        }
				else
				{
					switch (audio)
					{
				        case MediaRecorder.OutputFormat.THREE_GPP:
				        	audioFormat = "3GPP";
				            break;
				        case MediaRecorder.OutputFormat.MPEG_4:
				        	audioFormat = "MP3";
				            break;
				        case MediaRecorder.OutputFormat.RAW_AMR:
				        	audioFormat = "AMR";
				            break;
			        }
				}
			}
		}
		catch (Exception e)
		{
			Log.w("RecordFileMaker", "getRecordFormat : " + context.getString(R.string.log_record_file_maker_get_format) + " : " + e);
            return "";
        }
		
		return audioFormat;
	}
}