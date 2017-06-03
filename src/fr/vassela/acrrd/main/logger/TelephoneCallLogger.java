/**
 * @file TelephoneCallLogger.java
 * @brief Logger class
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

package fr.vassela.acrrd.main.logger;

import java.text.SimpleDateFormat;
import java.util.Date;

import fr.vassela.acrrd.R;
import fr.vassela.acrrd.database.DatabaseManager;
import fr.vassela.acrrd.database.DatabaseDefinition.TELEPHONE_LOG;
import fr.vassela.acrrd.localizer.LocalizerManager;
import fr.vassela.acrrd.theme.ThemeManager;
import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.view.ActionMode.Callback;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.SimpleCursorAdapter.ViewBinder;

public class TelephoneCallLogger extends Activity implements AppCompatCallback
{
	private Cursor cursor;
	private ListView listView;
	private SimpleCursorAdapter simpleCursorAdapter;
	
	private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	
	private String[] from = { TELEPHONE_LOG.DATE, TELEPHONE_LOG.TEXT, TELEPHONE_LOG.PRIORITY };
	private int[] to = { R.id.log_date, R.id.log_text, R.id.log_text };
	
	private String orderBy;
	private int logByPriority;
	
	private DatabaseManager databaseManager = new DatabaseManager();
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
        
        delegate.setContentView(R.layout.logs);
        
        Toolbar toolbar = (Toolbar)findViewById(R.id.log_toolbar);
        delegate.setSupportActionBar(toolbar);
        delegate.getSupportActionBar().setLogo(R.drawable.ic_menu_archive);
        delegate.setTitle(getApplicationContext().getString(R.string.main_toolbar_log));
        delegate.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        delegate.getSupportActionBar().setDisplayShowHomeEnabled(true);
		
		orderBy = TELEPHONE_LOG.DEFAULT_SORT_ORDER;
		logByPriority = 1;
		cursor = databaseManager.queryLogByPriority(getApplicationContext(), cursor, orderBy, logByPriority);
		
		setCursorAdapter();
        
		listView.setEmptyView(findViewById(R.id.logs_empty));
    }
    
    public boolean setLogViewValue(View aView, Cursor aCursor, int aColumnIndex)
    {
    	try
		{
	    	if (aColumnIndex == aCursor.getColumnIndex(TELEPHONE_LOG.DATE))
	        {
	                long date = aCursor.getLong(aColumnIndex);
	                TextView textView = (TextView) aView;
	                textView.setText("" + simpleDateFormat.format(new Date(date)) + " ");
	                return true;
	        }
	    	
	    	if (aColumnIndex == aCursor.getColumnIndex(TELEPHONE_LOG.TEXT))
	    	{
	            String text = aCursor.getString(aColumnIndex);
	            TextView textView = (TextView) aView;
	            textView.setText("" + text);
	            return true;
	    	}
	    	
	    	if (aColumnIndex == aCursor.getColumnIndex(TELEPHONE_LOG.PRIORITY))
	    	{
	            int priority = aCursor.getInt(aColumnIndex);
	            TextView textView = (TextView) aView;
	            
	            switch(priority)
	            {
	            	case 0:
	            		textView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
	            		break;
	            	case 1:
	            		textView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
	            		break;
	            	case 2:
	            		textView.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
	            		break;
	            	case 3:
	            		textView.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
	            		break;
	            }
	            
	            return true;
	    	}
		}
		catch (Exception e)
		{
			Log.w("TelephoneCallLogger", "setLogViewValue : " + getApplicationContext().getString(R.string.log_telephone_call_logger_error_set_view_value) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_telephone_call_logger_error_set_view_value), new Date().getTime(), 2, false);
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
			
			simpleCursorAdapter = new SimpleCursorAdapter(this, R.layout.log, cursor, from, to);
	       
			simpleCursorAdapter.setViewBinder(new ViewBinder()
			{
			    public boolean setViewValue(View aView, Cursor aCursor, int aColumnIndex)
			    {
			    	return setLogViewValue(aView, aCursor, aColumnIndex);
			    }
			});
	
			listView.setAdapter(simpleCursorAdapter);
			
	    	return true;
		}
		catch (Exception e)
		{
			Log.w("TelephoneCallLogger", "setCursorAdapter : " + getApplicationContext().getString(R.string.log_telephone_call_logger_error_set_cursor) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_telephone_call_logger_error_set_cursor), new Date().getTime(), 2, false);
			return false;
		}
    }
    
	public boolean onCreateOptionsMenu(Menu menu)
	{
		try
		{
			MenuInflater inflater = getMenuInflater();
			
			inflater.inflate(R.layout.logs_menu, menu);
			 
			return true;
		}
		catch (Exception e)
		{
			Log.w("TelephoneCallLogger", "onCreateOptionsMenu : " + getApplicationContext().getString(R.string.log_telephone_call_logger_error_create_options_menu) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_telephone_call_logger_error_create_options_menu), new Date().getTime(), 2, false);
			return false;
		}
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		try
		{
			switch (item.getItemId())
			{
				case android.R.id.home :
					super.onBackPressed();
					break;
        		
				case R.id.logs_submenu_order_by_asc:
					orderBy = "DATE ASC";
					cursor = databaseManager.queryLogByPriority(getApplicationContext(), cursor, orderBy, logByPriority);
					setCursorAdapter();
					break;
					
				case R.id.logs_submenu_order_by_desc:
					orderBy = "DATE DESC";
					cursor = databaseManager.queryLogByPriority(getApplicationContext(), cursor, orderBy, logByPriority);
					setCursorAdapter();
					break;
					
				case R.id.logs_submenu_display_logs:
					logByPriority = 1;
					cursor = databaseManager.queryLogByPriority(getApplicationContext(), cursor, orderBy, logByPriority);
					setCursorAdapter();
					break;
					
				case R.id.logs_submenu_display_all_logs:
					logByPriority = 3;
					cursor = databaseManager.queryAllLog(getApplicationContext(), cursor, orderBy);
					setCursorAdapter();
					break;
			}
			return super.onOptionsItemSelected(item);
		}
		catch (Exception e)
		{
			Log.w("TelephoneCallLogger", "onOptionsItemSelected : " + getApplicationContext().getString(R.string.log_telephone_call_logger_error_options_menu_item_selected) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_telephone_call_logger_error_options_menu_item_selected), new Date().getTime(), 2, false);
			return false;
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
}