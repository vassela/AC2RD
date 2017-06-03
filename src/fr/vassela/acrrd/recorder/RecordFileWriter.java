/**
 * @file RecordFileWritter.java
 * @brief Record file writter class
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
import java.io.RandomAccessFile;
import java.util.Date;

import fr.vassela.acrrd.R;
import fr.vassela.acrrd.database.DatabaseManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.preference.PreferenceManager;
import android.util.Log;

public class RecordFileWriter extends Application
{
	private static MediaRecorder mediaRecorder;
	private static AudioRecord audioRecord;
	private boolean isAudioRecordRunning;
	private int audioRecordSampleRate;
	private int audioRecordPeriodInFrames;
	private byte[] audioRecordBuffer;
	private int audioRecordPayloadSize;
	private RandomAccessFile audioRecordRandomAccessFile;
	
	private static int witchRecorder;
	
	private DatabaseManager databaseManager = new DatabaseManager();
	
	private Context mContext;
	
	private boolean initMediaRecorder(Context context, int audioSource)
	{
		witchRecorder = 0;
		
		try
		{
			mediaRecorder = new MediaRecorder();
	    	mediaRecorder.setAudioSource(audioSource);
		}
		catch (Exception e)
		{
			Log.w("RecordFileWriter", "initMediaRecorder : " + context.getString(R.string.log_record_file_writer_error_mediarecorder_init) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_record_file_writer_error_mediarecorder_init), new Date().getTime(), 2, false);
			return false;
		}
		
		return true;
	}
	
	private AudioRecord.OnRecordPositionUpdateListener onAudioRecordPositionUpdateListener = new AudioRecord.OnRecordPositionUpdateListener()
	{
		public void onPeriodicNotification(AudioRecord recorder)
		{	
			try
			{
				if ((audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED) || (isAudioRecordRunning == false))
				{
					return;
				}
				
				try
				{
					audioRecord.read(audioRecordBuffer, 0, audioRecordBuffer.length);
				}
				catch (Exception e)
				{
					if(mContext != null)
					{
						Log.w("RecordFileWriter", "onAudioRecordPositionUpdateListener : " + mContext.getString(R.string.log_record_file_writer_error_read_buffer) + " : " + e);
						databaseManager.insertLog(mContext, "" + mContext.getString(R.string.log_record_file_writer_error_read_buffer), new Date().getTime(), 2, false);
					}
				}
				
				try
				{
					audioRecordRandomAccessFile.write(audioRecordBuffer);
					audioRecordPayloadSize += audioRecordBuffer.length;
				}
				catch (Exception e)
				{
					if(mContext != null)
					{
						Log.w("RecordFileWriter", "onAudioRecordPositionUpdateListener : " + mContext.getString(R.string.log_record_file_writer_error_write_buffer) + " : " + e);
						databaseManager.insertLog(mContext, "" + mContext.getString(R.string.log_record_file_writer_error_write_buffer), new Date().getTime(), 2, false);
					}
				}
			}
			catch (Exception e)
			{
				if(mContext != null)
				{
					Log.w("RecordFileWriter", "onAudioRecordPositionUpdateListener : " + mContext.getString(R.string.log_record_file_writer_error_read_write_buffer) + " : " + e);
					databaseManager.insertLog(mContext, "" + mContext.getString(R.string.log_record_file_writer_error_read_write_buffer), new Date().getTime(), 2, false);
				}
			}
		}
		
		public void onMarkerReached(AudioRecord recorder)
		{
			
		}
	};
	
	private boolean initAudioRecord(Context context, int audioSource, int sampleRate, int channelConfig, int chanelNumber, int audioFormat, int nbBitsPerSample, int bufferSize)
	{
		witchRecorder = 1;
		isAudioRecordRunning = false;
		
		try
		{
			audioRecordSampleRate = sampleRate;
			int timerInterval = 120;
			audioRecordPeriodInFrames = sampleRate * timerInterval / 1000;
			bufferSize = audioRecordPeriodInFrames * 2 * chanelNumber * nbBitsPerSample / 8;
			
			if(bufferSize < AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat))
			{
				bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
				audioRecordPeriodInFrames = bufferSize / ( 2 * nbBitsPerSample * chanelNumber / 8 );
			}
			
			audioRecord = new AudioRecord(audioSource, sampleRate, channelConfig, audioFormat, bufferSize);
			
			if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED)
			{
				Log.w("RecordFileWriter", "initAudioRecord : " + context.getString(R.string.log_record_file_writer_error_audiorecord_init));
				databaseManager.insertLog(context, "" + context.getString(R.string.log_record_file_writer_error_audiorecord_init), new Date().getTime(), 2, false);
				return false;
			}
			
			audioRecord.setRecordPositionUpdateListener(onAudioRecordPositionUpdateListener);
			audioRecord.setPositionNotificationPeriod(audioRecordPeriodInFrames);
		}
		catch (Exception e)
		{
			Log.w("RecordFileWriter", "initAudioRecord : " + context.getString(R.string.log_record_file_writer_error_audiorecord_init) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_record_file_writer_error_audiorecord_init), new Date().getTime(), 2, false);
			return false;
		}
		
		return true;
	}
	
	private boolean configureMediaRecorderDataSource(Context context, File recordFile, int audioFormat)
	{
		try
		{
			mediaRecorder.setOutputFormat(audioFormat);
			mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
			mediaRecorder.setOutputFile(recordFile.getAbsolutePath());
		}
		catch (Exception e)
		{
			Log.w("RecordFileWriter", "configureMediaRecorderDataSource : " + context.getString(R.string.log_record_file_writer_error_data_source) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_record_file_writer_error_data_source), new Date().getTime(), 2, false);
			return false;
		}

		return true;
	}
	
	private boolean prepareMediaRecorder(Context context)
	{
		try
		{
			mediaRecorder.prepare();
		}
		catch (Exception e)
		{
			Log.w("RecordFileWriter", "prepareMediaRecorder : " + context.getString(R.string.log_record_file_writer_error_mediarecorder_prepare) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_record_file_writer_error_mediarecorder_prepare), new Date().getTime(), 2, false);
			return false;
		}
		
		return true;
	}
	
	private boolean prepareAudioRecord(Context context, File recordFile, int audioRecordChannelConfig, int chanelNumber, int audioRecordAudioFormat, int nbBitsPerSample)
	{
		try
		{
			if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED)
			{
				audioRecordRandomAccessFile = new RandomAccessFile(recordFile.getAbsolutePath(), "rw");
				audioRecordRandomAccessFile.setLength(0);
				audioRecordRandomAccessFile.writeBytes("RIFF"); // 00 - Marks the file as a riff file
				audioRecordRandomAccessFile.writeInt(0); // 04 - Size of the overall file
				audioRecordRandomAccessFile.writeBytes("WAVE"); // 08 - File Type Header
				audioRecordRandomAccessFile.writeBytes("fmt "); // 12 - Format chunk marker
				audioRecordRandomAccessFile.writeInt(Integer.reverseBytes(16)); // 16 - Length of format data as listed above
				audioRecordRandomAccessFile.writeShort(Short.reverseBytes((short) 1)); // 20 - Type of format
				audioRecordRandomAccessFile.writeShort(Short.reverseBytes((short) chanelNumber)); // 22 - Number of Channels
				audioRecordRandomAccessFile.writeInt(Integer.reverseBytes(audioRecordSampleRate)); // 24 - Sample Rate
				audioRecordRandomAccessFile.writeInt(Integer.reverseBytes(audioRecordSampleRate * chanelNumber * (short)nbBitsPerSample / 8)); // 28 - ByteRate
				audioRecordRandomAccessFile.writeShort(Short.reverseBytes((short)(chanelNumber * nbBitsPerSample / 8))); // 32 - Alignment
				audioRecordRandomAccessFile.writeShort(Short.reverseBytes((short) nbBitsPerSample)); // 34 - Bits per sample
				audioRecordRandomAccessFile.writeBytes("data"); // 36 - "data" chunk header
				audioRecordRandomAccessFile.writeInt(0); // 40 - Size of the data section
				audioRecordBuffer = new byte[audioRecordPeriodInFrames * (short)nbBitsPerSample / 8 * chanelNumber];
			}
			else
			{
				Log.w("RecordFileWriter", "prepareAudioRecord : " + context.getString(R.string.log_record_file_writer_error_audiorecord_prepare));
				databaseManager.insertLog(context, "" + context.getString(R.string.log_record_file_writer_error_audiorecord_prepare), new Date().getTime(), 2, false);
				return false;
			}
		}
		catch (Exception e)
		{
			Log.w("RecordFileWriter", "prepareAudioRecord : " + context.getString(R.string.log_record_file_writer_error_audiorecord_prepare) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_record_file_writer_error_audiorecord_prepare), new Date().getTime(), 2, false);
			return false;
		}
		
		return true;
	}
	
	private boolean setAudioMaxVolume(Context context)
	{
		try
		{
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			boolean isPreferencesAudioMaxVolumeActivated = sharedPreferences.getBoolean("preferences_audio_max_volume_activate",false);
			
			if(isPreferencesAudioMaxVolumeActivated == true)
			{
				AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
				audioManager.setStreamVolume(0, audioManager.getStreamMaxVolume(0), 0);
			}
		}
		catch (Exception e)
		{
			Log.w("RecordFileWriter", "setAudioMaxVolume : " + context.getString(R.string.log_record_file_writer_error_set_audio_max_volume) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_record_file_writer_error_set_audio_max_volume), new Date().getTime(), 2, false);
			return false;
		}
		
		return true;
	}
	
	private boolean startMediaRecorder(Context context)
	{
		try
		{
			setAudioMaxVolume(context);
			
			mediaRecorder.start();
		}
		catch (Exception e)
		{
			Log.w("RecordFileWriter", "startMediaRecorder : " + context.getString(R.string.log_record_file_writer_error_start_mediarecorder) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_record_file_writer_error_start_mediarecorder), new Date().getTime(), 2, false);
			return false;
		}
		
		return true;
	}
	
	private boolean startAudioRecord(Context context)
	{
		try
		{
			setAudioMaxVolume(context);
			
			audioRecordPayloadSize = 0;
			audioRecord.startRecording();
			audioRecord.read(audioRecordBuffer, 0, audioRecordBuffer.length);
			isAudioRecordRunning = true;
		}
		catch (Exception e)
		{
			Log.w("RecordFileWriter", "startAudioRecord : " + context.getString(R.string.log_record_file_writer_error_start_audiorecord) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_record_file_writer_error_start_audiorecord), new Date().getTime(), 2, false);
			return false;
		}
		
		return true;
	}
	
	private boolean resetMediaRecorder(Context context)
	{
		try
		{
			mediaRecorder.reset();
			mediaRecorder.release();
		}
		catch (Exception e)
		{
			Log.w("RecordFileWriter", "resetMediaRecorder : " + context.getString(R.string.log_record_file_writer_error_reset_mediarecorder) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_record_file_writer_error_reset_mediarecorder), new Date().getTime(), 2, false);
			return false;
		}
		
		return true;
	}
	
	private boolean releaseAudioRecord(Context context)
	{
		try
		{
			if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING)
			{
				stopAudioRecord(context);
			}
			else if(audioRecord.getState() == AudioRecord.STATE_INITIALIZED)
			{
				audioRecordRandomAccessFile.close();
			}
			
			if (audioRecord != null)
			{
				audioRecord.release();
			}
		}
		catch (Exception e)
		{
			Log.w("RecordFileWriter", "releaseAudioRecord : " + context.getString(R.string.log_record_file_writer_error_release_audiorecord) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_record_file_writer_error_release_audiorecord), new Date().getTime(), 2, false);
			return false;
		}
		
		return true;
	}
	
	private boolean startMediaRecorderHandler(Context context, File recordFile, int audioSource, int audioFormat)
	{
		boolean retInitMediaRecorder = false;
		boolean retConfigureMediaRecorderDataSource = false;
		boolean retPrepareMediaRecorder = false;
		boolean retStartMediaRecorder = false;
		
		retInitMediaRecorder  = initMediaRecorder(context, audioSource);
		
		if(retInitMediaRecorder == true)
		{
			retConfigureMediaRecorderDataSource = configureMediaRecorderDataSource(context, recordFile, audioFormat);
			
			if(retConfigureMediaRecorderDataSource == true)
			{
				retPrepareMediaRecorder = prepareMediaRecorder(context);
				
				if(retPrepareMediaRecorder == true)
				{
					retStartMediaRecorder =  startMediaRecorder(context);
					
					if(retStartMediaRecorder == false)
					{
						return false;
					}
				}
				else
				{
					return false;
				}
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
		return true;
	}
	
	private boolean startAudioRecordHandler(Context context, File recordFile, int audioSource, int audioFormat)
	{
		boolean retInitAudioRecord = false;
		boolean retPrepareAudioRecord = false;
		boolean retStartAudioRecord = false;
		int[] audioRecordSampleRates = {8000};
		int audioRecordSampleRatesIndex = 0;
		int audioRecordChannelConfig = AudioFormat.CHANNEL_IN_MONO;
		int audioRecordChanelNumber = 1;
		int audioRecordAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
		int audioRecordNbBitsPerSample = 16;
		int audioRecordBufferSize = 0;
		
		do
		{
			retInitAudioRecord = initAudioRecord(context, audioSource, audioRecordSampleRates[audioRecordSampleRatesIndex], audioRecordChannelConfig, audioRecordChanelNumber, audioRecordAudioFormat, audioRecordNbBitsPerSample, audioRecordBufferSize);
		}
		while((++audioRecordSampleRatesIndex < audioRecordSampleRates.length) && (retInitAudioRecord == false));

		if(retInitAudioRecord == true)
		{	
			retPrepareAudioRecord = prepareAudioRecord(context, recordFile, audioRecordChannelConfig, audioRecordChanelNumber, audioRecordAudioFormat, audioRecordNbBitsPerSample);
			
			if(retPrepareAudioRecord == true)
			{
				retStartAudioRecord = startAudioRecord(context);
				
				if(retStartAudioRecord == false)
				{
					return false;
				}
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
		
		return true;
	}
	
	public boolean start(Context context, File recordFile, int audioSource, int audioFormat)
	{
		mContext = context;
		
		boolean retResetMediaRecorder = false;
		boolean retReleaseAudioRecord = false;
		
		if(audioFormat < 10) // 3GPP, AMR, MP3
		{
			boolean retStartMediaRecorderHandler = startMediaRecorderHandler(context, recordFile, audioSource, audioFormat);
			
			if(retStartMediaRecorderHandler == false)
			{
				retResetMediaRecorder = resetMediaRecorder(context);
				
				if(retResetMediaRecorder == true)
				{
					/*retStartMediaRecorderHandler = startMediaRecorderHandler(context, recordFile, MediaRecorder.AudioSource.DEFAULT, audioFormat);
					
					if(retStartMediaRecorderHandler == false)
					{
						return false;
					}*/
					
					return false;
				}
				else
				{
					return false;
				}
			}
		}
		else // WAV
		{
			boolean retStartAudioRecordHandler = startAudioRecordHandler(context, recordFile, audioSource, audioFormat);
		
			if(retStartAudioRecordHandler == false)
			{
				retReleaseAudioRecord = releaseAudioRecord(context);
				
				if(retReleaseAudioRecord == true)
				{
					/*retStartAudioRecordHandler = startAudioRecordHandler(context, recordFile, MediaRecorder.AudioSource.DEFAULT, audioFormat);
				
					if(retStartAudioRecordHandler == false)
					{
						return false;
					}*/
					
					return false;
				}
				else
				{
					return false;
				}
			}
		}
		
		return true;
	}
	
	private boolean stopMediaRecorder(Context context)
	{
		try
		{
			mediaRecorder.stop();
			mediaRecorder.release();
		}
		catch (Exception e)
		{
			Log.w("RecordFileWriter", "stopMediaRecorder : " + context.getString(R.string.log_record_file_writer_error_stop_mediarecorder) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_record_file_writer_error_stop_mediarecorder), new Date().getTime(), 2, false);
			return false;
		}
		
		return true;
	}
	
	private boolean stopAudioRecord(Context context)
	{
		try
		{
			if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING)
			{
				isAudioRecordRunning = false;
				audioRecord.setRecordPositionUpdateListener(null);
				audioRecord.stop();
				audioRecordRandomAccessFile.seek(4);
				audioRecordRandomAccessFile.writeInt(Integer.reverseBytes(36 + audioRecordPayloadSize)); // 04 - Size of the overall file
				audioRecordRandomAccessFile.seek(40);
				audioRecordRandomAccessFile.writeInt(Integer.reverseBytes(audioRecordPayloadSize)); // 40 - Size of the data section
				audioRecordRandomAccessFile.close();
			}
		}
		catch (Exception e)
		{
			Log.w("RecordFileWriter", "stopAudioRecord : " + context.getString(R.string.log_record_file_writer_error_stop_audiorecord) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_record_file_writer_error_stop_audiorecord), new Date().getTime(), 2, false);
			return false;
		}
		
		return true;
	}
	
	public boolean stop(Context context)
	{
		mContext = context;
		
		boolean retStopMediaRecorder = false;
		boolean retStopAudioRecord = false;
		
		if(witchRecorder == 0)
		{
			retStopMediaRecorder = stopMediaRecorder(context);
			mediaRecorder = null;
			
			return retStopMediaRecorder;
		}
		else
		{
			retStopAudioRecord = stopAudioRecord(context);
			audioRecord = null;
			
			return retStopAudioRecord;
		}
	}
}