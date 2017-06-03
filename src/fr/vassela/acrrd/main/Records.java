/**
 * @file About.java
 * @brief Software's Records class
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

import java.text.SimpleDateFormat;
import java.util.Date;

import fr.vassela.acrrd.R;
import fr.vassela.acrrd.database.DatabaseManager;
import fr.vassela.acrrd.database.DatabaseDefinition.TELEPHONE_CALL;
import fr.vassela.acrrd.localizer.LocalizerManager;
import fr.vassela.acrrd.main.description.Description;
import fr.vassela.acrrd.notifier.TelephoneCallNotifier;
import fr.vassela.acrrd.purge.PurgeManager;
import fr.vassela.acrrd.recorder.RecordFileMaker;
import fr.vassela.acrrd.replayer.Replayer;
import fr.vassela.acrrd.theme.ThemeManager;
import android.app.Activity;
import android.app.AlertDialog;
//import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
//import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

public class Records extends Activity implements OnItemClickListener, OnItemLongClickListener
{
	private Cursor cursor;
	private ListView listView;
	private SimpleCursorAdapter simpleCursorAdapter;
	
	private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	private static SimpleDateFormat simpleDurationFormat = new SimpleDateFormat("HH:mm:ss");
	
	private String[] from = { TELEPHONE_CALL.NUMBER, TELEPHONE_CALL.DIRECTION, TELEPHONE_CALL.NUMBER, TELEPHONE_CALL.STARTING_DATE, TELEPHONE_CALL.DURATION };
	private int[] to = { R.id.record_number, R.id.record_direction, R.id.record_contact, R.id.record_starting_date, R.id.record_duration};
	
	private String orderBy;
	private boolean callOfDay;
	private boolean isLongClick = false;
	
	private DatabaseManager databaseManager = new DatabaseManager();
	private TelephoneCallNotifier telephoneCallNotifier = new TelephoneCallNotifier();
	private LocalizerManager localizerManager = new LocalizerManager();
	private ThemeManager themeManager = new ThemeManager();
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	try
		{
	        localizerManager.setPreferencesLocale(getApplicationContext());
			themeManager.setPreferencesTheme(getApplicationContext(), this);
			
	        super.onCreate(savedInstanceState); 
	        
			setContentView(R.layout.records);
			
			orderBy = TELEPHONE_CALL.DEFAULT_SORT_ORDER;
			cursor = databaseManager.queryAllCall(getApplicationContext(), cursor, orderBy);
			
			callOfDay = false;			
			
			setCursorAdapter();
			
			listView.setOnItemClickListener(this);
			listView.setOnItemLongClickListener(this);
			listView.setEmptyView(findViewById(R.id.records_empty));
		}
		catch (Exception e)
		{
			Log.w("Records", "onCreate : " + getApplicationContext().getString(R.string.log_records_error_create) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_records_error_create), new Date().getTime(), 2, false);
		}
    }
    
    public boolean setRecordViewValue(View aView, Cursor aCursor, int aColumnIndex)
    {
    	try
		{
	    	if (aColumnIndex == aCursor.getColumnIndex(TELEPHONE_CALL.DIRECTION))
	    	{
	            int direction = aCursor.getInt(aColumnIndex);
	            ImageView imageView = (ImageView) aView;
	            
	            if(direction == 0)
	            {
	            	imageView.setImageResource(R.drawable.sym_call_incoming);
	            }
	            else if(direction == 1)
	            {
	            	imageView.setImageResource(R.drawable.sym_call_outgoing);
	            }
	            else
	            {
	            	imageView.setImageResource(R.drawable.ic_btn_speak_now);
	            }
	
	            return true;
	    	}
	    	
	    	if (aColumnIndex == aCursor.getColumnIndex(TELEPHONE_CALL.NUMBER))
	    	{
	            String number = aCursor.getString(aColumnIndex);
	            TextView textView = (TextView) aView;
	            
	            if(number.contains("MICROPHONE"))
	            {
	            	String textViewId = aView.getResources().getResourceName(aView.getId());

	            	if(textViewId.contains("record_number"))
	            	{
	            		textView.setText(getApplicationContext().getString(R.string.records_micropĥone_vocal_record));
	            	}
	            	else if(textViewId.contains("record_contact"))
	            	{
	            		textView.setText(" " + getApplicationContext().getString(R.string.records_microphone));
	            	}
	            }
	            else
	            {
	            	String contactName = "";
		            contactName = databaseManager.getRecordContactName(getApplicationContext(), number);
		            
		            String textViewId = aView.getResources().getResourceName(aView.getId());
		            
		            if(contactName != "")
		            {
		            	if(textViewId.contains("record_number"))
		            	{
		            		textView.setText(contactName);
		            	}
		            	else if(textViewId.contains("record_contact"))
		            	{
		            		textView.setText(" " + number);
		            	}
		            }
		            else
		            {
		            	if(textViewId.contains("record_number"))
		            	{
		            		textView.setText(number);
		            	}
		            	else if(textViewId.contains("record_contact"))
		            	{
		            		textView.setText(" " + getApplicationContext().getString(R.string.records_unknown_contact));
		            	}
		            }
	            }
	            
	            return true;
	    	}
	    	
	    	if (aColumnIndex == aCursor.getColumnIndex(TELEPHONE_CALL.STARTING_DATE))
	        {
	                long startingDate = aCursor.getLong(aColumnIndex);
	                TextView textView = (TextView) aView;
	                textView.setText("Date : " + simpleDateFormat.format(new Date(startingDate)) + " ");
	                return true;
	        }
	    	
	    	if (aColumnIndex == aCursor.getColumnIndex(TELEPHONE_CALL.DURATION))
	        {
	                long duration = aCursor.getLong(aColumnIndex);
	                duration = duration - 3600000;
	                TextView textView = (TextView) aView;
	                textView.setText("Durée : " + simpleDurationFormat.format(new Date(duration)));
	                return true;
	        }
	    	
	    	if (aColumnIndex == aCursor.getColumnIndex(TELEPHONE_CALL.LOCKED))
	    	{
	            int locked = aCursor.getInt(aColumnIndex);
	            TextView textView = (TextView) aView;
	            textView.setText("Locked : " + locked);
	            return true;
	    	}
		}
		catch (Exception e)
		{
			Log.w("Records", "setRecordViewValue : " + getApplicationContext().getString(R.string.log_records_error_set_view_value) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_records_error_set_view_value), new Date().getTime(), 2, false);
			return false;
		}
    	return false;
    }
    
    public boolean setCursorAdapter()
    {
    	try
		{
	    	startManagingCursor(cursor);
			
			listView = (ListView) findViewById(android.R.id.list);
			
			simpleCursorAdapter = new SimpleCursorAdapter(this, R.layout.record, cursor, from, to);
	       
			simpleCursorAdapter.setViewBinder(new ViewBinder()
			{
			    public boolean setViewValue(View aView, Cursor aCursor, int aColumnIndex)
			    {
			    	return setRecordViewValue(aView, aCursor, aColumnIndex);
			    }
			});
	
			listView.setAdapter(simpleCursorAdapter);
			
	    	return true;
		}
		catch (Exception e)
		{
			Log.w("Records", "setCursorAdapter : " + getApplicationContext().getString(R.string.log_records_error_set_cursor) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_records_error_set_cursor), new Date().getTime(), 2, false);
			return false;
		}
    }

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
	{
		try
		{
			if(isLongClick == false)
			{
				SimpleCursorAdapter simpleCursorAdapter = (SimpleCursorAdapter) arg0.getAdapter();
				
				Cursor cursor = simpleCursorAdapter.getCursor();
				cursor.moveToPosition(arg2);
				
				int cursorIndex1 = cursor.getColumnIndex(TELEPHONE_CALL.DIRECTION);
				int cursorIndex2 = cursor.getColumnIndex(TELEPHONE_CALL.NUMBER);
				int cursorIndex3 = cursor.getColumnIndex(TELEPHONE_CALL.DOUBLE_CALL);
				int cursorIndex4 = cursor.getColumnIndex(TELEPHONE_CALL.DOUBLE_CALL_NUMBER);
				int cursorIndex5 = cursor.getColumnIndex(TELEPHONE_CALL.STARTING_DATE);
				int cursorIndex6 = cursor.getColumnIndex(TELEPHONE_CALL.ENDING_DATE);
				int cursorIndex7 = cursor.getColumnIndex(TELEPHONE_CALL.DURATION);
				int cursorIndex8 = cursor.getColumnIndex(TELEPHONE_CALL.RECORD_PATH);
				int cursorIndex9 = cursor.getColumnIndex(TELEPHONE_CALL.RECORD_FILENAME);
				int cursorIndex10 = cursor.getColumnIndex(TELEPHONE_CALL.RECORD_FORMAT);
				int cursorIndex11 = cursor.getColumnIndex(TELEPHONE_CALL.LOCKED);
				int cursorIndex12 = cursor.getColumnIndex(TELEPHONE_CALL.DESCRIPTION);
				
				final int direction = cursor.getInt(cursorIndex1);
				final String number = cursor.getString(cursorIndex2);
				final int doubleCall = cursor.getInt(cursorIndex3);
				final String doubleCallNumber = cursor.getString(cursorIndex4);
				final long startingDate = cursor.getLong(cursorIndex5);
				final long endingDate = cursor.getLong(cursorIndex6);
				final long duration = cursor.getLong(cursorIndex7) - 3600000;
				final String recordPath = cursor.getString(cursorIndex8);
				final String recordFilename = cursor.getString(cursorIndex9);
				final int recordFormat = cursor.getInt(cursorIndex10);
				final int locked = cursor.getInt(cursorIndex11);
				final String description = cursor.getString(cursorIndex12);
				
				LayoutInflater layoutInflater = LayoutInflater.from(Records.this);
				View dialogview = layoutInflater.inflate(R.layout.record_dialog, null);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(Records.this);
				
				if(number.contains("MICROPHONE"))
	            {
					builder.setTitle(getApplicationContext().getString(R.string.records_micropĥone_vocal_record));
	            }
				else
				{
					builder.setTitle(number);
				}
				
				builder.setView(dialogview);
				
				Button replayButton = (Button) dialogview.findViewById(R.id.record_dialog_replay);
				Button addDescriptionButton = (Button) dialogview.findViewById(R.id.record_dialog_add_description);
				Button lockButton = (Button) dialogview.findViewById(R.id.record_dialog_lock);
				Button deleteButton = (Button) dialogview.findViewById(R.id.record_dialog_delete);
				Button cancelButton = (Button) dialogview.findViewById(R.id.record_dialog_cancel);
				
				final Dialog alertDialog;
				alertDialog = builder.create();
				alertDialog.show();
				themeManager.setAlertDialog(getApplicationContext(), alertDialog);
				
				if(locked == 0)
				{
					lockButton.setText(getApplicationContext().getString(R.string.record_dialog_lock));
					
					lockButton.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							PurgeManager purgeManager = new PurgeManager();
							purgeManager.lockRecord(getApplicationContext(), recordFilename);
							alertDialog.dismiss();
						}
						
					});
				}
				else
				{
					lockButton.setText(getApplicationContext().getString(R.string.record_dialog_unlock));
					
					lockButton.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							PurgeManager purgeManager = new PurgeManager();
							purgeManager.unlockRecord(getApplicationContext(), recordFilename);
							alertDialog.dismiss();
						}
						
					});
				}
				
				addDescriptionButton.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						alertDialog.dismiss();
						
						Intent intent = new Intent(getApplicationContext(), Description.class);
						intent.putExtra("recordFilename", recordFilename);
						intent.putExtra("description", description);
						startActivity(intent);
					}
					
				});

				replayButton.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						alertDialog.dismiss();
						
						Intent intent = new Intent(getApplicationContext(), Replayer.class);
						intent.putExtra("direction", direction);
						intent.putExtra("number", number);
						intent.putExtra("doubleCall", doubleCall);
						intent.putExtra("doubleCallNumber", doubleCallNumber);
						intent.putExtra("startingDate", startingDate);
						intent.putExtra("endingDate", endingDate);
						intent.putExtra("duration", duration);
						intent.putExtra("recordPath", recordPath);
						intent.putExtra("recordFilename", recordFilename);
						intent.putExtra("recordFormat", recordFormat);
						intent.putExtra("locked", locked);
						intent.putExtra("description", description);
						startActivity(intent);
					}
				});
				
				deleteButton.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						alertDialog.dismiss();
						
						AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(Records.this);
						deleteBuilder.setTitle(getApplicationContext().getString(R.string.record_dialog_delete_confirm));
						deleteBuilder.setMessage("")
						.setCancelable(false)
						.setPositiveButton(getApplicationContext().getString(R.string.record_dialog_delete_yes), new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								if(locked == 0)
								{
									PurgeManager purgeManager = new PurgeManager();
									purgeManager.purgeRecord(getApplicationContext(), recordFilename);
								}
								else
								{
									telephoneCallNotifier.displayToast(getApplicationContext(), getApplicationContext().getString(R.string.notification_record_dialog_delete_error_toast), true);
								}
							}
						})
						.setNegativeButton(getApplicationContext().getString(R.string.record_dialog_delete_no), new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								dialog.cancel();
							}
						});
						
						final Dialog deleteAlertDialog;
						deleteAlertDialog = deleteBuilder.create();
						deleteAlertDialog.show();
						themeManager.setAlertDialog(getApplicationContext(), deleteAlertDialog);
					}
					
				});
				
				cancelButton.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						alertDialog.dismiss();
					}
					
				});
			}
		}
		catch (Exception e)
		{
			Log.w("Records", "onItemClick : " + getApplicationContext().getString(R.string.log_records_error_item_click) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_records_error_item_click), new Date().getTime(), 2, false);
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
	{
		try
		{
			isLongClick = true;
			
			SimpleCursorAdapter simpleCursorAdapter = (SimpleCursorAdapter) arg0.getAdapter();
			
			Cursor cursor = simpleCursorAdapter.getCursor();
			cursor.moveToPosition(arg2);
			
			int cursorIndex1 = cursor.getColumnIndex(TELEPHONE_CALL.DIRECTION);
			int cursorIndex2 = cursor.getColumnIndex(TELEPHONE_CALL.NUMBER);
			int cursorIndex3 = cursor.getColumnIndex(TELEPHONE_CALL.DOUBLE_CALL);
			int cursorIndex4 = cursor.getColumnIndex(TELEPHONE_CALL.DOUBLE_CALL_NUMBER);
			int cursorIndex5 = cursor.getColumnIndex(TELEPHONE_CALL.STARTING_DATE);
			int cursorIndex6 = cursor.getColumnIndex(TELEPHONE_CALL.ENDING_DATE);
			int cursorIndex7 = cursor.getColumnIndex(TELEPHONE_CALL.DURATION);
			int cursorIndex8 = cursor.getColumnIndex(TELEPHONE_CALL.RECORD_PATH);
			int cursorIndex9 = cursor.getColumnIndex(TELEPHONE_CALL.RECORD_FILENAME);
			int cursorIndex10 = cursor.getColumnIndex(TELEPHONE_CALL.RECORD_FORMAT);
			int cursorIndex11 = cursor.getColumnIndex(TELEPHONE_CALL.LOCKED);
			int cursorIndex12 = cursor.getColumnIndex(TELEPHONE_CALL.DESCRIPTION);
			
			final int direction = cursor.getInt(cursorIndex1);
			final String number = cursor.getString(cursorIndex2);
			final int doubleCall = cursor.getInt(cursorIndex3);
			final String doubleCallNumber = cursor.getString(cursorIndex4);
			final long startingDate = cursor.getLong(cursorIndex5);
			final long endingDate = cursor.getLong(cursorIndex6);
			final long duration = cursor.getLong(cursorIndex7) - 3600000;
			final String recordPath = cursor.getString(cursorIndex8);
			final String recordFilename = cursor.getString(cursorIndex9);
			final int recordFormat = cursor.getInt(cursorIndex10);
			final int locked = cursor.getInt(cursorIndex11);
			final String description = cursor.getString(cursorIndex12);
			
			LayoutInflater layoutInflater = LayoutInflater.from(Records.this);
			View dialogview = layoutInflater.inflate(R.layout.record_dialog_detail, null);
			
			AlertDialog.Builder builder = new AlertDialog.Builder(Records.this);
			
			if(number.contains("MICROPHONE"))
            {
				builder.setTitle(getApplicationContext().getString(R.string.records_micropĥone_vocal_record));
            }
			else
			{
				builder.setTitle(number);
			}
			
			builder.setView(dialogview);
			
			TextView directionTextview = (TextView) dialogview.findViewById(R.id.record_dialog_detail_direction);
			TextView numberTextview = (TextView) dialogview.findViewById(R.id.record_dialog_detail_number);
			TextView doubleCallTextview = (TextView) dialogview.findViewById(R.id.record_dialog_detail_doublecall);
			TextView doubleCallNumberTextview = (TextView) dialogview.findViewById(R.id.record_dialog_detail_doublecallnumber);
			TextView startingDateTextView = (TextView) dialogview.findViewById(R.id.record_dialog_detail_startingdate);
			TextView endingDateTextView = (TextView) dialogview.findViewById(R.id.record_dialog_detail_endingdate);
			TextView durationTextView = (TextView) dialogview.findViewById(R.id.record_dialog_detail_duration);
			TextView recordPathTextView = (TextView) dialogview.findViewById(R.id.record_dialog_detail_recordpath);
			TextView recordFilenameTextView = (TextView) dialogview.findViewById(R.id.record_dialog_detail_recordfilename);
			TextView recordFormatTextView = (TextView) dialogview.findViewById(R.id.record_dialog_detail_recordformat);
			TextView lockedTextView = (TextView) dialogview.findViewById(R.id.record_dialog_detail_locked);
			TextView descriptionTextView = (TextView) dialogview.findViewById(R.id.record_dialog_detail_description);
			Button acquitButton = (Button) dialogview.findViewById(R.id.record_dialog_detail_acquit);
			
			switch(direction)
			{
				case 0:
					directionTextview.setText(getApplicationContext().getString(R.string.record_dialog_detail_direction) + " : " + getApplicationContext().getString(R.string.record_dialog_detail_direction_0));
					break;
				case 1:
					directionTextview.setText(getApplicationContext().getString(R.string.record_dialog_detail_direction) + " : " + getApplicationContext().getString(R.string.record_dialog_detail_direction_1));
					break;
				case 2:
					directionTextview.setText(getApplicationContext().getString(R.string.record_dialog_detail_direction) + " : " + getApplicationContext().getString(R.string.record_dialog_detail_direction_2));
					break;
				default:
					directionTextview.setText(getApplicationContext().getString(R.string.record_dialog_detail_direction) + " : " + direction);
					break;
			}
			
			if(number.contains("MICROPHONE"))
            {
				numberTextview.setText(getApplicationContext().getString(R.string.record_dialog_detail_number) + " : " + getApplicationContext().getString(R.string.records_microphone));
            }
			else
			{
				numberTextview.setText(getApplicationContext().getString(R.string.record_dialog_detail_number) + " : " + number);
			}
			
			if(doubleCall == 0)
			{
				doubleCallTextview.setText(getApplicationContext().getString(R.string.record_dialog_detail_doublecall) + " : " + getApplicationContext().getString(R.string.record_dialog_detail_doublecall_no));
			}
			else
			{
				doubleCallTextview.setText(getApplicationContext().getString(R.string.record_dialog_detail_doublecall) + " : " + getApplicationContext().getString(R.string.record_dialog_detail_doublecall_yes));
			}
			
			doubleCallNumberTextview.setText(getApplicationContext().getString(R.string.record_dialog_detail_doublecallnumber) + " : " + doubleCallNumber);
			startingDateTextView.setText(getApplicationContext().getString(R.string.record_dialog_detail_startingdate) + " : " + simpleDateFormat.format(new Date(startingDate)));
			endingDateTextView.setText(getApplicationContext().getString(R.string.record_dialog_detail_endingdate) + " : " + simpleDateFormat.format(new Date(endingDate)));
			durationTextView.setText(getApplicationContext().getString(R.string.record_dialog_detail_duration) + " : " + simpleDurationFormat.format(new Date(duration)));
			recordPathTextView.setText(getApplicationContext().getString(R.string.record_dialog_detail_recordpath) + " : " + recordPath);
			recordFilenameTextView.setText(getApplicationContext().getString(R.string.record_dialog_detail_recordfilename) + " : " + recordFilename);
			recordFormatTextView.setText(getApplicationContext().getString(R.string.record_dialog_detail_recordformat) + " : " + new RecordFileMaker().getRecordFormat(getApplicationContext(), recordFormat));
			
			if(locked == 0)
			{
				lockedTextView.setText(getApplicationContext().getString(R.string.record_dialog_detail_locked) + " : " + getApplicationContext().getString(R.string.record_dialog_detail_locked_no));
			}
			else
			{
				lockedTextView.setText(getApplicationContext().getString(R.string.record_dialog_detail_locked) + " : " + getApplicationContext().getString(R.string.record_dialog_detail_locked_yes));
			}
			
			descriptionTextView.setText(getApplicationContext().getString(R.string.record_dialog_detail_description) + " : " + description);
			
			final Dialog alertDialog;
			alertDialog = builder.create();
			alertDialog.show();
			themeManager.setAlertDialog(getApplicationContext(), alertDialog);
			
			acquitButton.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					alertDialog.dismiss();
				}
				
			});
			
			isLongClick = false;
	
			return true;
		}
		catch (Exception e)
		{
			Log.w("Records", "onItemLongClick : " + getApplicationContext().getString(R.string.log_records_error_item_long_click) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_records_error_item_long_click), new Date().getTime(), 2, false);
			return false;
		}
	}
	
	public boolean onCreateOptionsMenu(Menu menu)
	{
		try
		{
			MenuInflater inflater = getMenuInflater();
			
			inflater.inflate(R.layout.records_menu, menu);
			 
			return true;
		}
		catch (Exception e)
		{
			Log.w("Records", "onCreateOptionsMenu : " + getApplicationContext().getString(R.string.log_records_error_create_options_menu) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_records_error_create_options_menu), new Date().getTime(), 2, false);
			return false;
		}
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		try
		{
			switch (item.getItemId())
			{
				case R.id.records_submenu_order_by_asc:
					orderBy = "STARTING_DATE ASC";
					if(callOfDay == true)
					{
						cursor = databaseManager.queryCallOfDay(getApplicationContext(), cursor, orderBy);
					}
					else
					{
						cursor = databaseManager.queryAllCall(getApplicationContext(), cursor, orderBy);
					}
					setCursorAdapter();
					break;
					
				case R.id.records_submenu_order_by_desc:
					orderBy = "STARTING_DATE DESC";
					if(callOfDay == true)
					{
						cursor = databaseManager.queryCallOfDay(getApplicationContext(), cursor, orderBy);
					}
					else
					{
						cursor = databaseManager.queryAllCall(getApplicationContext(), cursor, orderBy);
					}
					setCursorAdapter();
					break;
					
				case R.id.records_submenu_display_call_of_day:
					callOfDay = true;
					cursor = databaseManager.queryCallOfDay(getApplicationContext(), cursor, orderBy);
					setCursorAdapter();
					break;
					
				case R.id.records_submenu_display_all_call:
					callOfDay = false;
					cursor = databaseManager.queryAllCall(getApplicationContext(), cursor, orderBy);
					setCursorAdapter();
					break;
			}
			return true;
		}
		catch (Exception e)
		{
			Log.w("Records", "onOptionsItemSelected : " + getApplicationContext().getString(R.string.log_records_error_options_menu_item_selected) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_records_error_options_menu_item_selected), new Date().getTime(), 2, false);
			return false;
		}
	}
}