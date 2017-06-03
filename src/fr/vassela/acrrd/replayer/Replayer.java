/**
 * @file Replayer.java
 * @brief Replayer class
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

package fr.vassela.acrrd.replayer;

import java.text.SimpleDateFormat;
import java.util.Date;
import fr.vassela.acrrd.R;
import fr.vassela.acrrd.database.DatabaseManager;
import fr.vassela.acrrd.localizer.LocalizerManager;
import fr.vassela.acrrd.notifier.TelephoneCallNotifier;
import fr.vassela.acrrd.recorder.RecordFileMaker;
import fr.vassela.acrrd.theme.ThemeManager;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.view.ActionMode.Callback;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.MediaController;
import android.widget.TextView;

public class Replayer extends Activity implements MediaController.MediaPlayerControl, OnPreparedListener, AppCompatCallback
{
	private MediaPlayer mediaPlayer;
	private ReplayFileVisualizer replayFileVisualizer;
	private MediaController mediaController;
	private Handler handler = new Handler();
	
	private int direction;
	private String number;
	private int doubleCall;
	private String doubleCallNumber;
	private long startingDate;
	private long endingDate;
	//private long duration;
	private String recordPath;
	private String recordFilename;
	private int recordFormat;
	//private int locked;
	private String description;
	
	private String recordFileAbsolutePath;
	
	private boolean isPrepared;
	
	private DatabaseManager databaseManager = new DatabaseManager();
	private TelephoneCallNotifier telephoneCallNotifier = new TelephoneCallNotifier();
	private LocalizerManager localizerManager = new LocalizerManager();
	private ThemeManager themeManager = new ThemeManager();

	private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	private static SimpleDateFormat simpleDurationFormat = new SimpleDateFormat("HH:mm:ss");
	
	private AppCompatDelegate delegate;

	@Override
    public void onCreate(Bundle savedInstanceState)
    {
		try
		{
	        localizerManager.setPreferencesLocale(getApplicationContext());
			themeManager.setPreferencesTheme(getApplicationContext(), this);
			
			delegate = AppCompatDelegate.create(this, this);
			delegate.installViewFactory();
			
	        super.onCreate(savedInstanceState);
	        
	        delegate.onCreate(savedInstanceState);
	        
	        delegate.setContentView(R.layout.replayer);
	        
	        Toolbar toolbar = (Toolbar)findViewById(R.id.replayer_toolbar);
	        delegate.setSupportActionBar(toolbar);
	        delegate.getSupportActionBar().setLogo(R.drawable.ic_menu_play_clip);
	        delegate.setTitle("Replayer");
	        delegate.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	        delegate.getSupportActionBar().setDisplayShowHomeEnabled(true);
	        
	        Intent intent = getIntent();
	        
	        if(intent.getExtras() != null)
			{
				direction = intent.getIntExtra("direction",0);
				number = intent.getStringExtra("number");
				doubleCall = intent.getIntExtra("doubleCall",0);
				doubleCallNumber = intent.getStringExtra("doubleCallNumber");
				startingDate = intent.getLongExtra("startingDate", 0);
				endingDate = intent.getLongExtra("endingDate", 0);
				//duration = intent.getLongExtra("duration", 0);
				recordPath = intent.getStringExtra("recordPath");
				recordFilename = intent.getStringExtra("recordFilename");
				recordFormat = intent.getIntExtra("recordFormat",0);
				//locked = intent.getIntExtra("locked",0);
				description = intent.getStringExtra("description");
	
	        	TextView directionTextView = (TextView) findViewById(R.id.replayer_direction);
	        	TextView numberTextView = (TextView) findViewById(R.id.replayer_number);
	        	TextView doubleCallTextView = (TextView) findViewById(R.id.replayer_double_call);
	        	TextView doubleCallNumberTextView = (TextView) findViewById(R.id.replayer_double_call_number);
	        	TextView startingDateTextView = (TextView) findViewById(R.id.replayer_starting_date);
	        	TextView endingDateTextView = (TextView) findViewById(R.id.replayer_ending_date);
	        	//TextView durationTextView = (TextView) findViewById(R.id.replayer_duration);
	        	//TextView recordPathTextView = (TextView) findViewById(R.id.replayer_record_path);
	        	TextView recordFilenameTextView = (TextView) findViewById(R.id.replayer_record_filename);
	        	TextView recordFormatTextView = (TextView) findViewById(R.id.replayer_record_format);
	        	//TextView lockedTextView = (TextView) findViewById(R.id.replayer_locked);
	        	TextView descriptionTextView = (TextView) findViewById(R.id.replayer_description);
	        	
	        	directionTextView.setText(getApplicationContext().getString(R.string.record_dialog_detail_direction) + " : " + direction);
	        	
	        	if(number.contains("MICROPHONE"))
	            {
	        		numberTextView.setText(getApplicationContext().getString(R.string.record_dialog_detail_number) + " : " + getApplicationContext().getString(R.string.records_microphone));
	            }
	        	else
	        	{
	        		numberTextView.setText(getApplicationContext().getString(R.string.record_dialog_detail_number) + " : " + number);
	        	}
	        	
	        	if(doubleCall == 0)
				{
	        		doubleCallTextView.setText(getApplicationContext().getString(R.string.record_dialog_detail_doublecall) + " : " + getApplicationContext().getString(R.string.record_dialog_detail_doublecall_no));
				}
	        	else
	        	{
	        		doubleCallTextView.setText(getApplicationContext().getString(R.string.record_dialog_detail_doublecall) + " : " + getApplicationContext().getString(R.string.record_dialog_detail_doublecall_yes));
	        	}
	        	
	        	doubleCallNumberTextView.setText(getApplicationContext().getString(R.string.record_dialog_detail_doublecallnumber) + " : " + doubleCallNumber);
	        	startingDateTextView.setText(getApplicationContext().getString(R.string.record_dialog_detail_startingdate) + " : " + simpleDateFormat.format(new Date(startingDate)));
	        	endingDateTextView.setText(getApplicationContext().getString(R.string.record_dialog_detail_endingdate) + " : " + simpleDateFormat.format(new Date(endingDate)));
	        	//durationTextView.setText(getApplicationContext().getString(R.string.record_dialog_detail_duration) + " : " + simpleDurationFormat.format(new Date(duration)));
	        	//recordPathTextView.setText(getApplicationContext().getString(R.string.record_dialog_detail_recordpath) + " : " + recordPath);
	        	recordFilenameTextView.setText(getApplicationContext().getString(R.string.record_dialog_detail_recordfilename) + " : " + recordFilename);
	        	recordFormatTextView.setText(getApplicationContext().getString(R.string.record_dialog_detail_recordformat) + " : " + new RecordFileMaker().getRecordFormat(getApplicationContext(), recordFormat));
	        	
	        	/*if(locked == 0)
				{
	        		lockedTextView.setText(getApplicationContext().getString(R.string.record_dialog_detail_locked) + " : " + getApplicationContext().getString(R.string.record_dialog_detail_locked_no));
				}
	        	else
	        	{
	        		lockedTextView.setText(getApplicationContext().getString(R.string.record_dialog_detail_locked) + " : " + getApplicationContext().getString(R.string.record_dialog_detail_locked_yes));
	        	}*/
	        	
	        	descriptionTextView.setText(getApplicationContext().getString(R.string.record_dialog_detail_description) + " : " + description);
				
	        	recordFileAbsolutePath = recordPath + "/" + recordFilename;
	        	
	        	ReplayFileReader replayFileReader = new ReplayFileReader();
				
				boolean isReplayFileExists = replayFileReader.onCreate(getApplicationContext(), recordFileAbsolutePath);
				
				isPrepared = false;
				
				if(isReplayFileExists == true)
				{
					mediaController = new MediaController(this);
					
					if(recordFormat < 10) // 3GPP, AMR, MP3
					{
						initMediaplayer();
					}
					else // WAV
					{
						initWavMediaplayer(replayFileReader);
					}
				}
				else
				{
					Log.e("Replayer", "initMediaController : " + getApplicationContext().getString(R.string.log_replayer_error_player_init));
					databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_replayer_error_player_init), new Date().getTime(), 1, false);
					
					telephoneCallNotifier.displayToast(getApplicationContext(), getApplicationContext().getString(R.string.notification_replayer_error_launch_toast), true);
				}
				
				replayFileReader.onClose(getApplicationContext(), recordFileAbsolutePath);
			}
		}
		catch (Exception e)
		{
			Log.e("Replayer", "onCreate : " + getApplicationContext().getString(R.string.log_replayer_error_create) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_replayer_error_create), new Date().getTime(), 1, false);
		}
    }
	
    public void initMediaplayer()
    {
		try
		{
			mediaPlayer = new MediaPlayer();
			
			mediaPlayer.setDataSource(recordFileAbsolutePath);
			mediaPlayer.setOnPreparedListener(this);
			mediaPlayer.prepare();
		}
		catch (Exception e)
		{		
			try
			{
				mediaPlayer.reset();
				mediaPlayer.release();
				mediaPlayer = null;
			}
			catch (Exception e2)
			{
				Log.w("Replayer", "initMediaplayer : " + getApplicationContext().getString(R.string.log_replayer_echec_player_close) + " : " + e);
				databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_replayer_echec_player_close), new Date().getTime(), 2, false);
				Log.e("Replayer", "initMediaplayer : " + getApplicationContext().getString(R.string.log_replayer_error_player_init) + " : " + e);
				databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_replayer_error_player_init), new Date().getTime(), 1, false);
				
				telephoneCallNotifier.displayToast(getApplicationContext(), getApplicationContext().getString(R.string.notification_replayer_error_launch_toast), true);
			}
			finally
			{
				Log.e("Replayer", "initMediaplayer : " + getApplicationContext().getString(R.string.log_replayer_error_player_init) + " : " + e);
				databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_replayer_error_player_init), new Date().getTime(), 1, false);
				
				telephoneCallNotifier.displayToast(getApplicationContext(), getApplicationContext().getString(R.string.notification_replayer_error_launch_toast), true);
			}
		}
    }
	
    public void initWavMediaplayer(ReplayFileReader replayFileReader)
    {
		boolean isValidAudioReplayWavFile = replayFileReader.isValidAudioReplayWavFile(getApplicationContext(), recordFileAbsolutePath);
		
		if(isValidAudioReplayWavFile == true)
		{
			try
			{
				mediaPlayer = new MediaPlayer();
				
				mediaPlayer.setDataSource(recordFileAbsolutePath);
				mediaPlayer.setOnPreparedListener(this);
				mediaPlayer.prepare();
			}
			catch (Exception e)
			{
				try
				{
					mediaPlayer.reset();
					mediaPlayer.release();
					mediaPlayer = null;
				}
				catch (Exception e2)
				{
					Log.w("Replayer", "initWavMediaplayer : " + getApplicationContext().getString(R.string.log_replayer_echec_player_close) + " : " + e);
					databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_replayer_echec_player_close), new Date().getTime(), 2, false);
					Log.e("Replayer", "initWavMediaplayer : " + getApplicationContext().getString(R.string.log_replayer_error_player_init) + " : " + e);
					databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_replayer_error_player_init), new Date().getTime(), 1, false);
					
					telephoneCallNotifier.displayToast(getApplicationContext(), getApplicationContext().getString(R.string.notification_replayer_error_launch_toast), true);
				}
				finally
				{
					Log.e("Replayer", "initWavMediaplayer : " + getApplicationContext().getString(R.string.log_replayer_error_player_init) + " : " + e);
					databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_replayer_error_player_init), new Date().getTime(), 1, false);
					
					telephoneCallNotifier.displayToast(getApplicationContext(), getApplicationContext().getString(R.string.notification_replayer_error_launch_toast), true);
				}
			}
		}
    }
	
    public void initMediaController()
    {
		try
		{
	    	Log.d("Replayer", "initMediaController");
	    	
	    	mediaController.setMediaPlayer(this);
		    mediaController.setAnchorView(findViewById(R.id.replayer_media_controller));
		    
		    themeManager.setMediaController(getApplicationContext(), mediaController);
		    
			handler.post(new Runnable()
			{
				public void run()
				{
					mediaController.setEnabled(true);
					mediaController.show();
				}
			});
		}
		catch (Exception e)
		{
			Log.e("Replayer", "initMediaController : " + getApplicationContext().getString(R.string.log_replayer_error_controller_init) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_replayer_error_controller_init), new Date().getTime(), 1, false);
			
			telephoneCallNotifier.displayToast(getApplicationContext(), getApplicationContext().getString(R.string.notification_replayer_error_launch_toast), true);
		}
		finally
		{
			Log.i("Replayer", "initMediaController : " + getApplicationContext().getString(R.string.log_replayer_replay_file) + " : " + recordFileAbsolutePath);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_replayer_replay_file) + " : " + recordFileAbsolutePath, new Date().getTime(), 3, false);
		}
    }

	@Override
	public void onPrepared(MediaPlayer mp)
	{
		isPrepared = true;
		
		initMediaController();
	}
	
	@Override
	protected void onStop()
	{
		try
		{
			super.onStop();

			if ((mediaPlayer != null) && (mediaController != null) && (isPrepared == true))
			{
				replayFileVisualizer.releaseReplayFileVisualizer(getApplicationContext());
				mediaController.hide();
				mediaPlayer.stop();
				mediaPlayer.release();
			}
		}
		catch (Exception e)
		{
			Log.w("Replayer", "onStop : " + getApplicationContext().getString(R.string.log_replayer_echec_stop) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_replayer_echec_stop), new Date().getTime(), 2, false);
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) 
	{
		try
		{
			if ((mediaPlayer != null) && (mediaController != null) && (isPrepared == true))
			{
				mediaController.show();
				return true;
			}
			else
			{
				return false;
			}
		}
		catch (Exception e)
		{
			Log.w("Replayer", "onTouchEvent : " + getApplicationContext().getString(R.string.log_replayer_echec_touch_event) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_replayer_echec_touch_event), new Date().getTime(), 2, false);
			return false;
		}
	}

	@Override
	public boolean canPause()
	{
		return true;
	}

	@Override
	public boolean canSeekBackward()
	{
		return true;
	}

	@Override
	public boolean canSeekForward()
	{
		return true;
	}

	@Override
	public int getBufferPercentage()
	{
		return 0;
	}

	@Override
	public int getCurrentPosition()
	{
		try
		{
			if ((mediaPlayer != null) && (mediaController != null) && (isPrepared == true))
			{
				return mediaPlayer.getCurrentPosition();
			}
			else
			{
				return 0;
			}
		}
		catch (Exception e)
		{
			Log.w("Replayer", "getCurrentPosition : " + getApplicationContext().getString(R.string.log_replayer_echec_get_current_position) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_replayer_echec_get_current_position), new Date().getTime(), 2, false);
			return 0;
		}
	}

	@Override
	public int getDuration()
	{
		try
		{
			if ((mediaPlayer != null) && (mediaController != null) && (isPrepared == true))
			{
				Log.d("Replayer", "getDuration : mediaPlayer.getDuration" + mediaPlayer.getDuration());
				return mediaPlayer.getDuration();
			}
			else
			{
				return 0;
			}
		}
		catch (Exception e)
		{
			Log.w("Replayer", "getDuration : " + getApplicationContext().getString(R.string.log_replayer_echec_get_duration) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_replayer_echec_get_duration), new Date().getTime(), 2, false);
			return 0;
		}
	}

	@Override
	public boolean isPlaying()
	{
		try
		{
			if ((mediaPlayer != null) && (mediaController != null) && (isPrepared == true))
			{
				return mediaPlayer.isPlaying();
			}
			else
			{
				return false;
			}
		}
		catch (Exception e)
		{
			Log.w("Replayer", "isPlaying : " + getApplicationContext().getString(R.string.log_replayer_echec_is_playing) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_replayer_echec_is_playing), new Date().getTime(), 2, false);
			return false;
		}
	}

	@Override
	public void pause()
	{
		try
		{
			if ((mediaPlayer != null) && (mediaController != null) && (isPrepared == true))
			{
				replayFileVisualizer.releaseReplayFileVisualizer(getApplicationContext());
				
				mediaPlayer.pause();
			}
		}
		catch (Exception e)
		{
			Log.w("Replayer", "pause : " + getApplicationContext().getString(R.string.log_replayer_echec_pause) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_replayer_echec_pause), new Date().getTime(), 2, false);
			
			telephoneCallNotifier.displayToast(getApplicationContext(), getApplicationContext().getString(R.string.notification_replayer_error_pause_toast), false);
		}
	}

	@Override
	public void seekTo(int pos)
	{
		try
		{
			if ((mediaPlayer != null) && (mediaController != null) && (isPrepared == true))
			{
				mediaPlayer.seekTo(pos);
			}
		}
		catch (Exception e)
		{
			Log.w("Replayer", "seekTo : " + getApplicationContext().getString(R.string.log_replayer_echec_seek_to) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_replayer_echec_seek_to), new Date().getTime(), 2, false);
			
			telephoneCallNotifier.displayToast(getApplicationContext(), getApplicationContext().getString(R.string.notification_replayer_error_seek_to_toast), false);
		}
	}

	@Override
	public void start()
	{
		try
		{
			if ((mediaPlayer != null) && (mediaController != null) && (isPrepared == true))
			{
				mediaPlayer.start();
				
				try
				{
					replayFileVisualizer = (ReplayFileVisualizer) findViewById(R.id.replayer_visualizer);
					replayFileVisualizer.linkToMediaPlayer(getApplicationContext(), mediaPlayer);
					replayFileVisualizer.showAudioDisplayer(getApplicationContext());
				}
				catch (Exception e)
				{
					Log.w("Replayer", "start : " + getApplicationContext().getString(R.string.log_replayer_echec_start_visualizer) + " : " + e);
					databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_replayer_echec_start_visualizer), new Date().getTime(), 2, false);
					
					telephoneCallNotifier.displayToast(getApplicationContext(), getApplicationContext().getString(R.string.notification_replayer_error_play_visualizer_toast), false);
				}
			}
		}
		catch (Exception e)
		{
			Log.w("Replayer", "start : " + getApplicationContext().getString(R.string.log_replayer_echec_start) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_replayer_echec_start), new Date().getTime(), 2, false);
			
			telephoneCallNotifier.displayToast(getApplicationContext(), getApplicationContext().getString(R.string.notification_replayer_error_play_toast), false);
		}
	}

	@Override
	public int getAudioSessionId()
	{
		try
		{
			if ((mediaPlayer != null) && (mediaController != null) && (isPrepared == true))
			{
				return mediaPlayer.getAudioSessionId();
			}
			else
			{
				return 0;
			}
		}
		catch (Exception e)
		{
			Log.w("Replayer", "getAudioSessionId : " + getApplicationContext().getString(R.string.log_replayer_echec_get_audio_session_id) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_replayer_echec_get_audio_session_id), new Date().getTime(), 2, false);
			
			return 0;
		}
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
