/**
 * @file Home.java
 * @brief Software's home class
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

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;
import fr.vassela.acrrd.R;
import fr.vassela.acrrd.localizer.LocalizerManager;
import fr.vassela.acrrd.monitoring.MonitoringManager;
import fr.vassela.acrrd.recorder.RecordServiceManager;
import fr.vassela.acrrd.theme.ThemeManager;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.SwitchCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TableRow;
import android.widget.TextView;

public class Home  extends Activity implements OnClickListener, CompoundButton.OnCheckedChangeListener
{
	private LocalizerManager localizerManager = new LocalizerManager();
	private ThemeManager themeManager = new ThemeManager();
	private MonitoringManager monitoringManager;
	private Timer timer;
	private boolean isPreferencesRecordActivated;
	
	SeekBar seekBar;
	
	SwitchCompat homeSwitchRecordActivate;
	Button microphoneStart;
	Button microphoneStop;
	
	int previousAudioManagerState = -1;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	try
		{
	        localizerManager.setPreferencesLocale(getApplicationContext());
			themeManager.setPreferencesTheme(getApplicationContext(), this);
			
	        super.onCreate(savedInstanceState);
	        
	        setContentView(R.layout.home);
	        
	        monitoringManager = new MonitoringManager();
	        
	        isPreferencesRecordActivated = false;
	        
	        TableRow tableHomeGeneralState = (TableRow) findViewById (R.id.table_home_general_state);
	        TableRow tableHomeTelephoneCallRecorder = (TableRow) findViewById (R.id.table_home_telephone_call_recorder);
	        TableRow tableHomeDictaphone = (TableRow) findViewById (R.id.table_home_dictaphone);
	        
	        themeManager.setTableBorder(getApplicationContext(), tableHomeGeneralState);
	        themeManager.setTableBorder(getApplicationContext(), tableHomeTelephoneCallRecorder);
	        themeManager.setTableBorder(getApplicationContext(), tableHomeDictaphone);
	        
	        seekBar = (SeekBar) findViewById (R.id.home_storage_available_space_seekbar);
	        themeManager.setSeekBar(getApplicationContext(), seekBar);
	        
	        seekBar.setOnTouchListener(new View.OnTouchListener()
	        {
	            @Override
	            public boolean onTouch(View view, MotionEvent motionEvent)
	            {
	                return true;
	            }
	        });
	        
	        homeSwitchRecordActivate = (SwitchCompat) findViewById(R.id.home_switch_record_activate);
	        homeSwitchRecordActivate.setSwitchPadding(40);
			homeSwitchRecordActivate.setOnCheckedChangeListener(this);
	        
	        microphoneStart = (Button) findViewById (R.id.home_microphone_start);
	        microphoneStop = (Button) findViewById (R.id.home_microphone_stop);
	        microphoneStart.setOnClickListener(this);
	        microphoneStop.setOnClickListener(this);
		}
		catch (Exception e)
		{

		}
    }
    
	@Override
	public void onResume()
	{
		try
		{
			super.onResume();
			
			timer = new Timer();
			
			timer.schedule(new TimerTask()
			{
				@Override
				public void run()
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							onRefreshAllState();
						}
					});
				}
			}, 0, 1000);
		}
		catch (Exception e)
		{

		}
	}
	
	@Override
	public void onPause()
	{
		try
		{
			super.onPause();
			
			timer.cancel();
		}
		catch (Exception e)
		{

		}
	}
	
    @Override
	public boolean onTouchEvent(MotionEvent event) 
	{
		try
		{
			onRefreshAllState();
			
			return true;
		}
		catch (Exception e)
		{

			return false;
		}
	}
    
    private void onRefreshAllState()
    {
    	try
		{
    		onRefreshTelephoneCallState();
    		onRefreshRecordActivateState();
    		onRefreshRecordServiceState();
    		onRefreshStorageState();
		}
    	catch (Exception e)
		{
		}
    }
        
    private void onRefreshTelephoneCallState()
    {
    	String telephoneCallState = monitoringManager.getTelephoneCallState(getApplicationContext());
    	
    	TextView homeTelephoneCallStateTextView = (TextView) findViewById(R.id.home_telephone_call_state_value);
    	homeTelephoneCallStateTextView.setText(" " + telephoneCallState);
    }
    
    private void onRefreshRecordActivateState()
    {
    	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		boolean isPreferencesRecordActivated = sharedPreferences.getBoolean("preferences_record_activate",false);

		if(this.isPreferencesRecordActivated != isPreferencesRecordActivated)
		{
			this.isPreferencesRecordActivated = isPreferencesRecordActivated;
			homeSwitchRecordActivate.setChecked(isPreferencesRecordActivated);
			Preferences.onSharedPreferenceRecordActivate(getApplicationContext());
		}
    }
    
    private void onRefreshRecordServiceState()
    {
    	boolean recordServiceState = monitoringManager.getRecordServiceState(getApplicationContext());
    	
    	TextView homeTelephoneCallRecorderStateTextView = (TextView) findViewById(R.id.home_telephone_call_recorder_state);
    	AppCompatRadioButton homeTelephoneCallRecorderStateRadioButton = (AppCompatRadioButton) findViewById(R.id.home_telephone_call_recorder_state_value);
    	TextView homeTelephoneCallRecorderActionValue = (TextView) findViewById(R.id.home_telephone_call_recorder_action_value);

    	String stateValueDetail = getApplicationContext().getString(R.string.home_telephone_call_recorder_action_unknown);
    	
    	if(recordServiceState == true)
    	{
    		int recordType = monitoringManager.getRecordType(getApplicationContext());

    		switch(recordType)
			{
				case 0:
					stateValueDetail = getApplicationContext().getString(R.string.home_telephone_call_recorder_action_incoming_call);
					break;
					
				case 1:
					stateValueDetail = getApplicationContext().getString(R.string.home_telephone_call_recorder_action_outgoing_call);
					break;

				case 2:
					stateValueDetail = getApplicationContext().getString(R.string.home_telephone_call_recorder_action_microphone);
					break;
			}

    		microphoneStart.setEnabled(false);
    		
    		if(recordType == 2)
    		{
    			microphoneStop.setEnabled(true);
    		}
    		else
    		{
    			microphoneStop.setEnabled(false);
    		}
    		
    		homeTelephoneCallRecorderStateTextView.setText(getApplicationContext().getString(R.string.home_telephone_call_recorder_state));
    		homeTelephoneCallRecorderStateRadioButton.setChecked(true);
    		homeTelephoneCallRecorderStateRadioButton.setText(getApplicationContext().getString(R.string.home_telephone_call_recorder_state_on));
    		homeTelephoneCallRecorderActionValue.setText(stateValueDetail);
    	}
    	else
    	{
    		microphoneStart.setEnabled(true);
    		microphoneStop.setEnabled(false);
    		
    		homeTelephoneCallRecorderStateTextView.setText(getApplicationContext().getString(R.string.home_telephone_call_recorder_state));
    		homeTelephoneCallRecorderStateRadioButton.setChecked(false);
    		homeTelephoneCallRecorderStateRadioButton.setText(getApplicationContext().getString(R.string.home_telephone_call_recorder_state_off));
    		homeTelephoneCallRecorderActionValue.setText(stateValueDetail);
    	}
    }
    
    private void onRefreshStorageState()
    {
    	try
		{
        	String storagePath = monitoringManager.getStoragePath(getApplicationContext());
        	
        	if((storagePath == "") || (storagePath == null))
        	{
        		storagePath = String.valueOf(Environment.getExternalStorageDirectory());
        	}
        	
        	if((storagePath != "") && (storagePath != null))
        	{
        		float storageTotalSize = monitoringManager.getStorageTotalSizeInMo(getApplicationContext());
        		float storageAvailableSize = monitoringManager.getStorageAvailableSizeInMo(getApplicationContext());
        		float storageAvailableSpaceInPercent = (storageAvailableSize * 100) / storageTotalSize;
        		
        		TextView homeStoragePathValueTextView = (TextView) findViewById(R.id.home_storage_path_value);
        		homeStoragePathValueTextView.setText(" " + storagePath);
        		
        		TextView homeStorageAvailableSpaceValueTextView = (TextView) findViewById(R.id.home_storage_available_space_value);
        		
        		if(storageAvailableSize > 2048)
        		{
        			storageAvailableSize = storageAvailableSize / 1024;
        			DecimalFormat decimalFormat = new DecimalFormat("#####.##");
        			homeStorageAvailableSpaceValueTextView.setText(" " + (int) storageAvailableSpaceInPercent + "% (" + decimalFormat.format(storageAvailableSize) + "GB)");
        		}
        		else
        		{
        			DecimalFormat decimalFormat = new DecimalFormat("#####.##");
        			homeStorageAvailableSpaceValueTextView.setText(" " + (int) storageAvailableSpaceInPercent + "% (" + decimalFormat.format(storageAvailableSize) + "MB)");
        		}
        		
        		seekBar.setProgress((int) storageAvailableSpaceInPercent);
        	}
		}
		catch (Exception e)
		{

		}
    }
    
	@Override
	public void onClick(View v)
	{
		try
		{
			RecordServiceManager recordServiceManager = new RecordServiceManager();
			
    		switch (v.getId())
    		{
	    		case R.id.home_microphone_start:
	    			recordServiceManager.startService(getApplicationContext(), 2, "MICROPHONE");
	    			break;
	    		case R.id.home_microphone_stop:
	    			recordServiceManager.stopService(getApplicationContext());
	    			break;
    		}
		}
		catch (Exception e)
		{

		}
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		switch (buttonView.getId())
		{
        	case R.id.home_switch_record_activate:
    			SharedPreferences sharedPreferencesRecordActivate = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    			SharedPreferences.Editor sharedPreferencesEditor = sharedPreferencesRecordActivate.edit();
    			sharedPreferencesEditor.putBoolean("preferences_record_activate", isChecked);
    			sharedPreferencesEditor.commit();
        		break;
        }
	}
}