/**
 * @file Filters.java
 * @brief Call filters
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

package fr.vassela.acrrd.filters;

import fr.vassela.acrrd.R;
import fr.vassela.acrrd.database.DatabaseManager;
import fr.vassela.acrrd.database.DatabaseDefinition.CONTACT_FILTER;
import fr.vassela.acrrd.localizer.LocalizerManager;
import fr.vassela.acrrd.notifier.TelephoneCallNotifier;
import fr.vassela.acrrd.theme.ThemeManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.view.ActionMode.Callback;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.SimpleCursorAdapter.ViewBinder;

public class Filters extends Activity implements AppCompatCallback, OnClickListener, OnItemClickListener, OnItemLongClickListener
{
	private Cursor cursor;
	private ListView listView;
	private SimpleCursorAdapter simpleCursorAdapter;
	
	private String[] from = { CONTACT_FILTER.NUMBER, CONTACT_FILTER.NUMBER, CONTACT_FILTER.RECORDABLE};
	private int[] to = { R.id.filter_number, R.id.filter_contact, R.id.filter_recordable};
	
	private int recordableContact = 1;
	
	private String orderBy;
	private boolean isLongClick = false;
	
	private int displayContacts = 0;
	
	private FiltersManager filtersManager = new FiltersManager();
	private DatabaseManager databaseManager = new DatabaseManager();
	private TelephoneCallNotifier telephoneCallNotifier = new TelephoneCallNotifier();
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
 
	        delegate.setContentView(R.layout.filters);
	        
	        Toolbar toolbar = (Toolbar)findViewById(R.id.filters_toolbar);
	        delegate.setSupportActionBar(toolbar);
	        delegate.getSupportActionBar().setLogo(R.drawable.ic_menu_friendslist);
	        delegate.setTitle(getApplicationContext().getString(R.string.filters_toolbar));
	        delegate.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	        delegate.getSupportActionBar().setDisplayShowHomeEnabled(true);
	        
	        orderBy = CONTACT_FILTER.DEFAULT_SORT_ORDER;
	        cursor = databaseManager.queryAllFilter(getApplicationContext(), cursor, orderBy);
	        
	        setCursorAdapter();
	        
	        listView.setOnItemClickListener(this);
			listView.setOnItemLongClickListener(this);
	        listView.setEmptyView(findViewById(R.id.filters_empty));
	        
	        Button addRecordableContact = (Button) findViewById (R.id.filters_add_recordable_contact);
	        Button addNotRecordableContact = (Button) findViewById (R.id.filters_add_not_recordable_contact);
	        
	        addRecordableContact.setOnClickListener(this);
	        addNotRecordableContact.setOnClickListener(this);
	        
	        recordableContact = 0;
	        displayContacts = 2;
        }
        
        public boolean setFilterViewValue(View aView, Cursor aCursor, int aColumnIndex)
        {
        	try
    		{
    	    	if (aColumnIndex == aCursor.getColumnIndex(CONTACT_FILTER.RECORDABLE))
    	    	{
    	            int recordable = aCursor.getInt(aColumnIndex);
    	            ImageView imageView = (ImageView) aView;
    	            
    	            if(recordable == 0)
    	            {
    	            	imageView.setImageResource(R.drawable.presence_busy);
    	            }
    	            else
    	            {
    	            	imageView.setImageResource(R.drawable.presence_online);
    	            }
    	
    	            return true;
    	    	}
    	    	
    	    	if (aColumnIndex == aCursor.getColumnIndex(CONTACT_FILTER.NUMBER))
    	    	{
    	            String number = aCursor.getString(aColumnIndex);
    	            TextView textView = (TextView) aView;

    	            String contactName = "";
		            contactName = databaseManager.getRecordContactName(getApplicationContext(), number);
		            
		            String textViewId = aView.getResources().getResourceName(aView.getId());
		            
		            if(contactName != "")
		            {
		            	if(textViewId.contains("filter_number"))
		            	{
		            		textView.setText(contactName);
		            	}
		            	else if(textViewId.contains("filter_contact"))
		            	{
		            		textView.setText(" " + number);
		            	}
		            }
		            else
		            {
		            	if(textViewId.contains("filter_number"))
		            	{
		            		textView.setText(number);
		            	}
		            	else if(textViewId.contains("filter_contact"))
		            	{
		            		textView.setText(" " + getApplicationContext().getString(R.string.filters_unknown_contact));
		            	}
		            }
    	            
    	            return true;
    	    	}
    		}
    		catch (Exception e)
    		{
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
    			
    			simpleCursorAdapter = new SimpleCursorAdapter(this, R.layout.filter, cursor, from, to);
    	       
    			simpleCursorAdapter.setViewBinder(new ViewBinder()
    			{
    			    public boolean setViewValue(View aView, Cursor aCursor, int aColumnIndex)
    			    {
    			    	return setFilterViewValue(aView, aCursor, aColumnIndex);
    			    }
    			});
    	
    			listView.setAdapter(simpleCursorAdapter);
    			
    	    	return true;
    		}
    		catch (Exception e)
    		{
    			return false;
    		}
        }
        
        @Override
    	public void onClick(View v)
    	{
    		try
    		{
        		switch (v.getId())
        		{
    	    		case R.id.filters_add_recordable_contact:
    	    			recordableContact = 1;
    	    			addContact();
    	    			break;	
    	    		case R.id.filters_add_not_recordable_contact:
    	    			recordableContact = 0;
    	    			addContact();
    	    			break;
        		}
    		}
    		catch (Exception e)
    		{

    		}
    	}
        
		public void addContact()
		{
			try
			{
				Intent intent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
				intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
				startActivityForResult(intent, 1);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		@Override
		public void onActivityResult(int reqCode, int resultCode, Intent data)
		{
			super.onActivityResult(reqCode, resultCode, data);
		
			switch (reqCode)
			{
				case 1:
					if (resultCode == Activity.RESULT_OK)
					{
						Uri contactUri = data.getData();
						
						String[] projection = {Phone.NUMBER};
						
						Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null);
			            
			            if (cursor.moveToFirst())
						{
			            	int column = cursor.getColumnIndex(Phone.NUMBER);
				            String number = cursor.getString(column);
				            
				            boolean isContactFilterAlreadyExists = filtersManager.isContactFilterAlreadyExists(getApplicationContext(), number);
				            
				            if(isContactFilterAlreadyExists == false)
				            {
				            	databaseManager.insertFilter(getApplicationContext(), number, recordableContact);
				            }
				            else
				            {
				            	telephoneCallNotifier.displayToast(getApplicationContext(), getApplicationContext().getString(R.string.notification_filters_error_add_contact), true);
				            }
						}  
					}
					break;
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
					
					int cursorIndex1 = cursor.getColumnIndex(CONTACT_FILTER.NUMBER);
					int cursorIndex2 = cursor.getColumnIndex(CONTACT_FILTER.RECORDABLE);
					
					final String number = cursor.getString(cursorIndex1);
					final int recordable = cursor.getInt(cursorIndex2);
					
					LayoutInflater layoutInflater = LayoutInflater.from(Filters.this);
					View dialogview = layoutInflater.inflate(R.layout.filter_dialog, null);
					
					AlertDialog.Builder builder = new AlertDialog.Builder(Filters.this);
					
					builder.setTitle(number);
					
					builder.setView(dialogview);
					
					Button switchButton = (Button) dialogview.findViewById(R.id.filter_dialog_switch);
					Button deleteButton = (Button) dialogview.findViewById(R.id.filter_dialog_delete);
					Button cancelButton = (Button) dialogview.findViewById(R.id.filter_dialog_cancel);
					
					final Dialog alertDialog;
					alertDialog = builder.create();
					alertDialog.show();
					themeManager.setAlertDialog(getApplicationContext(), alertDialog);
					
					if(recordable == 0)
					{
						switchButton.setText(getApplicationContext().getString(R.string.filter_dialog_switch_1));
						switchButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_invite, 0, 0, 0);
						
						switchButton.setOnClickListener(new View.OnClickListener()
						{
							@Override
							public void onClick(View v)
							{
								databaseManager.updateFilter(getApplicationContext(), number, 1);
								alertDialog.dismiss();
							}
							
						});
					}
					else
					{
						switchButton.setText(getApplicationContext().getString(R.string.filter_dialog_switch_0));
						switchButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_blocked_user, 0, 0, 0);
						
						switchButton.setOnClickListener(new View.OnClickListener()
						{
							@Override
							public void onClick(View v)
							{
								databaseManager.updateFilter(getApplicationContext(), number, 0);
								alertDialog.dismiss();
							}
							
						});
					}
					
					deleteButton.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							alertDialog.dismiss();
							
							AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(Filters.this);
							deleteBuilder.setTitle(getApplicationContext().getString(R.string.filter_dialog_delete_confirm));
							deleteBuilder.setMessage("")
							.setCancelable(false)
							.setPositiveButton(getApplicationContext().getString(R.string.filter_dialog_delete_yes), new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(DialogInterface dialog, int which)
								{
									databaseManager.deleteFilter(getApplicationContext(), number);
								}
							})
							.setNegativeButton(getApplicationContext().getString(R.string.filter_dialog_delete_no), new DialogInterface.OnClickListener()
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
				
				int cursorIndex1 = cursor.getColumnIndex(CONTACT_FILTER.NUMBER);
				int cursorIndex2 = cursor.getColumnIndex(CONTACT_FILTER.RECORDABLE);
				
				final String number = cursor.getString(cursorIndex1);
				final int recordable = cursor.getInt(cursorIndex2);
				
				LayoutInflater layoutInflater = LayoutInflater.from(Filters.this);
				View dialogview = layoutInflater.inflate(R.layout.filter_dialog_detail, null);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(Filters.this);
				
				builder.setTitle(number);
				
				builder.setView(dialogview);
				
				TextView numberTextview = (TextView) dialogview.findViewById(R.id.filter_dialog_detail_number);
				Button acquitButton = (Button) dialogview.findViewById(R.id.filter_dialog_detail_acquit);
				
				if(recordable == 0)
				{
					numberTextview.setText(getApplicationContext().getString(R.string.filter_dialog_detail_number_0));
				}
				else
				{
					numberTextview.setText(getApplicationContext().getString(R.string.filter_dialog_detail_number_1));
				}
				
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

				return false;
			}
		}
		
		public boolean onCreateOptionsMenu(Menu menu)
		{
			try
			{
				MenuInflater inflater = getMenuInflater();
				
				inflater.inflate(R.layout.filters_menu, menu);
				 
				return true;
			}
			catch (Exception e)
			{

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
	        		
					case R.id.filters_submenu_order_by_asc:
						orderBy = "RECORDABLE ASC";
						switch(displayContacts)
						{
							case 0:
								cursor = databaseManager.queryFilterByRecordable(getApplicationContext(), cursor, orderBy, displayContacts);
								break;
							case 1:
								cursor = databaseManager.queryFilterByRecordable(getApplicationContext(), cursor, orderBy, displayContacts);
								break;
							case 2:
								cursor = databaseManager.queryAllFilter(getApplicationContext(), cursor, orderBy);
								break;
							default:
								cursor = databaseManager.queryAllFilter(getApplicationContext(), cursor, orderBy);
								break;
						}
						setCursorAdapter();
						break;
						
					case R.id.filters_submenu_order_by_desc:
						orderBy = "RECORDABLE DESC";
						switch(displayContacts)
						{
							case 0:
								cursor = databaseManager.queryFilterByRecordable(getApplicationContext(), cursor, orderBy, displayContacts);
								break;
							case 1:
								cursor = databaseManager.queryFilterByRecordable(getApplicationContext(), cursor, orderBy, displayContacts);
								break;
							case 2:
								cursor = databaseManager.queryAllFilter(getApplicationContext(), cursor, orderBy);
								break;
							default:
								cursor = databaseManager.queryAllFilter(getApplicationContext(), cursor, orderBy);
								break;
						}
						setCursorAdapter();
						break;
						
					case R.id.filters_submenu_display_not_recordable_contacts:
						displayContacts = 0;
						cursor = databaseManager.queryFilterByRecordable(getApplicationContext(), cursor, orderBy, displayContacts);
						setCursorAdapter();
						break;
						
					case R.id.filters_submenu_display_recordable_contacts:
						displayContacts = 1;
						cursor = databaseManager.queryFilterByRecordable(getApplicationContext(), cursor, orderBy, displayContacts);
						setCursorAdapter();
						break;
						
					case R.id.filters_submenu_display_all_contacts:
						displayContacts = 2;
						cursor = databaseManager.queryAllFilter(getApplicationContext(), cursor, orderBy);
						setCursorAdapter();
						break;
				}
				return true;
			}
			catch (Exception e)
			{

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