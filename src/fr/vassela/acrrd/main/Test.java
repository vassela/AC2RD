/**
 * @file Test.java
 * @brief Software's test class
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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import fr.vassela.acrrd.R;
import fr.vassela.acrrd.database.DatabaseManager;
import fr.vassela.acrrd.localizer.LocalizerManager;
import fr.vassela.acrrd.notifier.TelephoneCallNotifier;
import fr.vassela.acrrd.purge.PurgeManager;
import fr.vassela.acrrd.recorder.RecordFileMaker;
import fr.vassela.acrrd.recorder.RecordServiceManager;
import fr.vassela.acrrd.theme.ThemeManager;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.view.ActionMode.Callback;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Test  extends Activity implements AppCompatCallback, OnClickListener
{
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
            
            delegate.setContentView(R.layout.test);
            
            Toolbar toolbar = (Toolbar)findViewById(R.id.test_toolbar);
	        delegate.setSupportActionBar(toolbar);
	        delegate.getSupportActionBar().setLogo(R.drawable.ic_menu_manage);
	        delegate.setTitle(getApplicationContext().getString(R.string.main_toolbar_test));
	        delegate.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	        delegate.getSupportActionBar().setDisplayShowHomeEnabled(true);

    		Button test_record_on_microphone = (Button) findViewById (R.id.test_record_on_microphone);
    		Button test_stop_record = (Button) findViewById (R.id.test_stop_record);
    		Button test_insert_call = (Button) findViewById (R.id.test_insert_call);
    		Button test_add_event = (Button) findViewById (R.id.test_add_event);
    		Button test_delete_all_events = (Button) findViewById (R.id.test_delete_all_events);
    		Button test_notification = (Button) findViewById (R.id.test_notification);
    		Button test_toast_message = (Button) findViewById (R.id.test_toast_message);
    		Button test_purge_all_records = (Button) findViewById (R.id.test_purge_all_records);
    		
    		test_record_on_microphone.setOnClickListener(this);
    		test_stop_record.setOnClickListener(this);
    		test_insert_call.setOnClickListener(this);
    		test_add_event.setOnClickListener(this);
    		test_delete_all_events.setOnClickListener(this);
    		test_notification.setOnClickListener(this);
    		test_toast_message.setOnClickListener(this);
    		test_purge_all_records.setOnClickListener(this);
        }
        
    	@Override
    	public void onClick(View v)
    	{
			DatabaseManager databaseManager = new DatabaseManager();
			RecordServiceManager recordServiceManager = new RecordServiceManager();
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			PurgeManager purgeManager = new PurgeManager();
			TelephoneCallNotifier telephoneCallNotifier = new TelephoneCallNotifier();
			
			SimpleDateFormat simpleDurationFormat = new SimpleDateFormat("HH:mm:ss");
			
    		switch (v.getId()) {
    		
    		
    			case R.id.test_record_on_microphone:
    				recordServiceManager.startService(getApplicationContext(), 2, "MICROPHONE");
    				break;
    		
    			case R.id.test_stop_record:
    				recordServiceManager.stopService(getApplicationContext());
    				break;
    		
    			case R.id.test_insert_call:
    				
    				onInsertCall(0, "+1-202-555-0129", 0, null, 20, 8, 20, 35, 0, databaseManager, sharedPreferences);
    				onInsertCall(1, "+1-202-555-0167", 0, null, 18, 41, 19, 10, 0, databaseManager, sharedPreferences);
    				onInsertCall(0, "+1-202-555-0160", 0, null, 15, 2, 15, 5, 0, databaseManager, sharedPreferences);
    				onInsertCall(0, "+1-202-555-0175", 0, null, 13, 33, 13, 47, 0, databaseManager, sharedPreferences);
    				onInsertCall(1, "+1-202-555-0142", 0, null, 12, 4, 12, 24, 0, databaseManager, sharedPreferences);
    				onInsertCall(1, "+1-202-555-0108", 0, null, 11, 29, 11, 53, 0, databaseManager, sharedPreferences);
    				onInsertCall(0, "+1-202-555-0184", 0, null, 10, 55, 11, 7, 0, databaseManager, sharedPreferences);
    				onInsertCall(0, "+1-202-555-0190", 0, null, 9, 36, 10, 12, 0, databaseManager, sharedPreferences);
    				onInsertCall(0, "+1-202-555-0162", 0, null, 9, 7, 9, 11, 0, databaseManager, sharedPreferences);
    				onInsertCall(0, "+1-202-555-0135", 0, null, 8, 39, 8, 58, 0, databaseManager, sharedPreferences);
    				break;		
    				
    			case R.id.test_add_event:
    				databaseManager.insertLog(getApplicationContext(), "test", new Date().getTime(), 0, false);
    				break;		
    				
    			case R.id.test_delete_all_events:
    				databaseManager.deleteAllLog(getApplicationContext());
    				break;	
    				
    			case R.id.test_notification:
    				telephoneCallNotifier.displayNotification(getApplicationContext(), getApplicationContext().getString(R.string.notification_record_service_new_record_file_ticker) + " : " + "0123456789" + " (" + simpleDurationFormat.format(3636360) + ")", getApplicationContext().getString(R.string.notification_record_service_new_record_file_content_title), getApplicationContext().getString(R.string.notification_record_service_new_record_file_content_text) + " : " + "0123456789" + " (" + simpleDurationFormat.format(3636360) + ")", true, false, false);
    				break;	
    				
    			case R.id.test_toast_message:
    				telephoneCallNotifier.displayToast(getApplicationContext(), "test", false);
    				break;	
    				
    			case R.id.test_purge_all_records:
    				purgeManager.purgeAllRecords(getApplicationContext());
    				break;
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
		
		private void onInsertCall(int direction, String number, int doubleCall, String doubleCallNumber, int startingHour, int startingMinute, int endingHour, int endingMinute, int locked, DatabaseManager databaseManager, SharedPreferences sharedPreferences)
		{
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, startingHour);
			calendar.set(Calendar.MINUTE, startingMinute);
			long startingDate = calendar.getTime().getTime();
			calendar.set(Calendar.HOUR_OF_DAY, endingHour);
			calendar.set(Calendar.MINUTE, endingMinute);
			long endingDate = calendar.getTime().getTime();
			long duration = (endingDate - startingDate);
			
			String recordPath = Preferences.getSharedStoragePath(getApplicationContext());
			String recordFilename = null;
			int recordFormat = Integer.parseInt(sharedPreferences.getString("preferences_audio_format", "1"));
			String recordFolderPath = recordPath + "/" + getApplicationContext().getString(R.string.app_acronym);
			RecordFileMaker recordFileMaker = new RecordFileMaker();
			File recordFile = recordFileMaker.createRecordFile(getApplicationContext(), new Date(startingDate), recordFormat, recordFolderPath);
			recordFilename = recordFile.getName();
			databaseManager.insertCall(getBaseContext(), direction, number, doubleCall, doubleCallNumber, startingDate, endingDate, duration, recordFolderPath, recordFilename, recordFormat, locked, getApplicationContext().getString(R.string.record_service_call_description) + " (" + number + ")");
		}
}
