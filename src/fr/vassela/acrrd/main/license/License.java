/**
 * @file License.java
 * @brief License class
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

package fr.vassela.acrrd.main.license;

import fr.vassela.acrrd.R;
import fr.vassela.acrrd.localizer.LocalizerManager;
import fr.vassela.acrrd.theme.ThemeManager;
import android.app.Activity;
import android.app.TabActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.view.ActionMode.Callback;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class License  extends Activity implements AppCompatCallback
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
           
            delegate.setContentView(R.layout.license);
            
            Toolbar toolbar = (Toolbar)findViewById(R.id.license_toolbar);
	        delegate.setSupportActionBar(toolbar);
	        delegate.getSupportActionBar().setLogo(R.drawable.ic_menu_info_details);
	        delegate.setTitle(getApplicationContext().getString(R.string.license));
	        delegate.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	        delegate.getSupportActionBar().setDisplayShowHomeEnabled(true);
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