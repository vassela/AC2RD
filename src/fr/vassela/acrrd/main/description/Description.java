/**
 * @file Description.java
 * @brief Records description class
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

package fr.vassela.acrrd.main.description;

import fr.vassela.acrrd.R;
import fr.vassela.acrrd.database.DatabaseManager;
import fr.vassela.acrrd.localizer.LocalizerManager;
import fr.vassela.acrrd.theme.ThemeManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.view.ActionMode.Callback;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

public class Description extends Activity implements AppCompatCallback, OnClickListener
{
	private String recordFilename;
	private String description;
	
	private EditText descriptionEditText;
    private Button description_accept;
	private Button description_cancel;
	
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
            
            delegate.setContentView(R.layout.description);

            Toolbar toolbar = (Toolbar)findViewById(R.id.description_toolbar);
	        delegate.setSupportActionBar(toolbar);
	        delegate.getSupportActionBar().setLogo(R.drawable.ic_menu_edit);
	        delegate.setTitle(getApplicationContext().getString(R.string.description_toolbar));
	        delegate.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	        delegate.getSupportActionBar().setDisplayShowHomeEnabled(true);
	        
	        descriptionEditText = (EditText) findViewById(R.id.description_to_add);
	        description_accept = (Button) findViewById (R.id.description_accept);
			description_cancel = (Button) findViewById (R.id.description_cancel);
			
	        Intent intent = getIntent();
	        
	        if(intent.getExtras() != null)
			{
	        	recordFilename = intent.getStringExtra("recordFilename");
	        	description = intent.getStringExtra("description");
	        	
	        	if(!description.equals(""))
	        	{
	        		descriptionEditText.setText(description);
	        	}
			}
	        
	        descriptionEditText.setFocusableInTouchMode(true);
	        descriptionEditText.setFocusable(true);
	        descriptionEditText.requestFocus();
			
			descriptionEditText.setOnTouchListener(new View.OnTouchListener()
			{
				@Override
				public boolean onTouch(View v, MotionEvent event)
				{
					v.requestFocus();
					InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
					inputMethodManager.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
					return false;
				}	
			});

			description_accept.setOnClickListener(this);
			description_cancel.setOnClickListener(this);
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
		public void onClick(View v)
		{
			try
			{
				switch (v.getId())
				{
					case R.id.description_accept:
						String description = descriptionEditText.getText().toString();
						databaseManager.insertDescription(getApplicationContext(), recordFilename, description);
						super.onBackPressed();
						break;
						
					case R.id.description_cancel:
						super.onBackPressed();
						break;
				}
			}
			catch (Exception e)
			{
			};
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