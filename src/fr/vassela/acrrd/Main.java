/**
 * @file Main.java
 * @brief Software's main class
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

package fr.vassela.acrrd;

import java.util.ArrayList;
import java.util.Date;

import fr.vassela.acrrd.R;
import fr.vassela.acrrd.database.DatabaseManager;
import fr.vassela.acrrd.localizer.LocalizerManager;
import fr.vassela.acrrd.main.About;
import fr.vassela.acrrd.main.Home;
import fr.vassela.acrrd.main.Preferences;
import fr.vassela.acrrd.main.Records;
import fr.vassela.acrrd.main.Test;
import fr.vassela.acrrd.main.logger.TelephoneCallLogger;
import fr.vassela.acrrd.monitoring.MonitoringServiceManager;
import fr.vassela.acrrd.purge.PurgeServiceManager;
import fr.vassela.acrrd.recorder.RecordServiceManager;
import fr.vassela.acrrd.theme.ThemeManager;
import android.app.Activity;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.view.ActionMode.Callback;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.TextView;

public class Main extends TabActivity implements AppCompatCallback 
{
	public static String SHOW_RECORDS = "fr.vassela.acrrd.act.SHOWRECORDS";
	
	private LocalizerManager localizerManager = new LocalizerManager();
	private ThemeManager themeManager = new ThemeManager();
	private DatabaseManager databaseManager = new DatabaseManager();
	
	private AppCompatDelegate delegate;
	
	Toolbar toolbar;

	private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private ListView drawerList;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		try
		{
			localizerManager.setPreferencesLocale(getApplicationContext());
			themeManager.setPreferencesTheme(getApplicationContext(), this);
			
			delegate = AppCompatDelegate.create(this, this);
			delegate.installViewFactory();
			
			super.onCreate(savedInstanceState);
			
			delegate.onCreate(savedInstanceState);
	
			delegate.setContentView(R.layout.main);
			
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			int currentLocaleIndex = Integer.parseInt(sharedPreferences.getString("preferences_locale_set", "0"));
			String currentLocale = localizerManager.getLocale(getApplicationContext()).toString();

			ArrayList<String> slideMenuTitles = new ArrayList<String>();
			slideMenuTitles.add(getApplicationContext().getString(R.string.preferences_locale).toString());
			//slideMenuTitles.add(getApplicationContext().getString(R.string.main_toolbar_test).toString());
			slideMenuTitles.add(getApplicationContext().getString(R.string.main_toolbar_preferences).toString());
			slideMenuTitles.add(getApplicationContext().getString(R.string.main_toolbar_log).toString());
			slideMenuTitles.add(getApplicationContext().getString(R.string.main_toolbar_about).toString());
			slideMenuTitles.add(getApplicationContext().getString(R.string.main_toolbar_exit).toString());
			
			ArrayList<String> slideMenuSubtitles = new ArrayList<String>();
			slideMenuSubtitles.add(currentLocale);
			//slideMenuSubtitles.add(getApplicationContext().getString(R.string.main_toolbar_test_description).toString());
			slideMenuSubtitles.add(getApplicationContext().getString(R.string.main_toolbar_preferences_description).toString());
			slideMenuSubtitles.add(getApplicationContext().getString(R.string.main_toolbar_log_description).toString());
			slideMenuSubtitles.add(getApplicationContext().getString(R.string.main_toolbar_about_description).toString());
			slideMenuSubtitles.add(getApplicationContext().getString(R.string.main_toolbar_exit_description).toString());

			ArrayList<Integer> slideMenuIcons = new ArrayList<Integer>();
			slideMenuIcons.add(themeManager.getLocaleFlag(getApplicationContext(), currentLocaleIndex));
			slideMenuIcons.add(R.drawable.ic_menu_preferences);
			//slideMenuIcons.add(R.drawable.ic_menu_preferences);
			slideMenuIcons.add(R.drawable.ic_menu_archive);
			slideMenuIcons.add(R.drawable.ic_menu_info_details);
			slideMenuIcons.add(R.drawable.ic_menu_revert);
			
	        drawerList = (ListView) findViewById(R.id.drawer_list);
	        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);

	        SlidingMenuAdapter slidingMenuAdapter = new SlidingMenuAdapter(this, slideMenuTitles, slideMenuSubtitles, slideMenuIcons);
	        
	        drawerList.setAdapter(slidingMenuAdapter);

	        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	            @Override
	            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	                
	                switch(position)
	                {
	                	case 0 :
	                		break;
	                		
						/*case 0 :
							drawerLayout.closeDrawers();
							startActivity(new Intent(getApplicationContext(), Test.class));
							break;*/
	                
		                case 1 :
		                	drawerLayout.closeDrawers();
		            		startActivity(new Intent(getApplicationContext(), Preferences.class));
		            		break;
		            		
		            	case 2 :
		            		drawerLayout.closeDrawers();
		            		startActivity(new Intent(getApplicationContext(), TelephoneCallLogger.class));
		            		break;

		            	case 3 :
		            		drawerLayout.closeDrawers();
		            		startActivity(new Intent(getApplicationContext(), About.class));
		            		break;
		            		
		            	case 4 :
		            		drawerLayout.closeDrawers();
		            		RecordServiceManager recordServiceManager = new RecordServiceManager();
		            		boolean isRecordServiceRunning = recordServiceManager.isRunning(getApplicationContext());
		            		
		            		if(isRecordServiceRunning == false)
		            		{
		                		MonitoringServiceManager monitoringServiceManager = new MonitoringServiceManager();
		                		monitoringServiceManager.stopService(getApplicationContext());
		                		
		                		PurgeServiceManager purgeServiceManager = new PurgeServiceManager();
		                		purgeServiceManager.stopService(getApplicationContext());	
		            		}

		            		moveTaskToBack(true);
		            		onBackPressed();
		            		break;

	                }
	                
	            }
	        });

			toolbar = (Toolbar)findViewById(R.id.main_toolbar);
			delegate.setSupportActionBar(toolbar);
			delegate.setTitle(getString(R.string.main_toolbar));
			delegate.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			delegate.getSupportActionBar().setDisplayShowHomeEnabled(true);
			delegate.getSupportActionBar().setHomeButtonEnabled(true);
			
			TabHost tabhost = (TabHost) findViewById (android.R.id.tabhost);
			TabWidget tabwidget = tabhost.getTabWidget();
			
			TabSpec tab_home = tabhost.newTabSpec(getString(R.string.main_tab_home));
	        TabSpec tab_records = tabhost.newTabSpec(getString(R.string.main_tab_records));
	        TabSpec tab_preferences = tabhost.newTabSpec(getString(R.string.main_tab_preferences));
	        TabSpec tab_about = tabhost.newTabSpec(getString(R.string.main_tab_about));
	        TabSpec tab_test = tabhost.newTabSpec("test");
	        
	        tab_home.setIndicator(getString(R.string.main_tab_home), this.getResources().getDrawable(R.drawable.ic_menu_home));
	        tab_home.setContent(new Intent(this,Home.class));
	       
	        tab_records.setIndicator(getString(R.string.main_tab_records), this.getResources().getDrawable(R.drawable.ic_voice_search));
	        tab_records.setContent(new Intent(this,Records.class));
	
	        tab_preferences.setIndicator(getString(R.string.main_tab_preferences), this.getResources().getDrawable(R.drawable.ic_menu_preferences));
	        tab_preferences.setContent(new Intent(this,Preferences.class));
	        
	        tab_about.setIndicator(getString(R.string.main_tab_about), this.getResources().getDrawable(R.drawable.ic_menu_info_details));
	        tab_about.setContent(new Intent(this,About.class));
	        
	        tab_test.setIndicator("test", this.getResources().getDrawable(R.drawable.ic_menu_preferences));
	        tab_test.setContent(new Intent(this,Test.class));
	        
	        tabhost.addTab(tab_home);
	        tabhost.addTab(tab_records);

	        Intent intent = getIntent();

	        /*String setCurrentTab;
	        
	        if (savedInstanceState == null)
	        {
	        	Bundle extras = getIntent().getExtras();
	        	
	        	if(extras == null)
	        	{
	        		setCurrentTab = null;
	        	}
	        	else
	        	{
	        		setCurrentTab = extras.getString("setCurrentTab");
	        	}
	        }
	        else
	        {
	        	setCurrentTab = (String) savedInstanceState.getSerializable("setCurrentTab");
	        }
	        
	        getIntent().removeExtra("setCurrentTab"); 
	        
        	if(setCurrentTab.equals("home"))
        	{
        		tabhost.setCurrentTab(0);
        	}
        	else if(setCurrentTab.equals("records"))
        	{
        		tabhost.setCurrentTab(1);
        	}
        	else
        	{
        		tabhost.setCurrentTab(0);
        	}*/
	        
	        if (SHOW_RECORDS.equals(intent.getAction()))
			{
	        	tabhost.setCurrentTab(1);
			}
	        else
	        {
	        	tabhost.setCurrentTab(0);
	        }

	        initDrawer();

	        themeManager.setTabWidget(getApplicationContext(), tabwidget);
	        
		}
		catch (Exception e)
		{
			Log.e("Main", "onCreate : " + getApplicationContext().getString(R.string.log_main_error_create) + " : " + e);
			databaseManager.insertLog(getApplicationContext(), "" + getApplicationContext().getString(R.string.log_main_error_create), new Date().getTime(), 1, false);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.layout.main_toolbar, menu);
        return true;
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item)
	{
        int id = item.getItemId();
  
        if (drawerToggle.onOptionsItemSelected(item)) {
        	
            return true;
        }
        
        switch(id)
        {
        	case R.id.main_toolbar_preferences :
        		startActivity(new Intent(getApplicationContext(), Preferences.class));
        		break;
        		
        	case R.id.main_toolbar_test :
        		startActivity(new Intent(getApplicationContext(), Test.class));
        		break;
        		
        	case R.id.main_toolbar_logs :
        		startActivity(new Intent(getApplicationContext(), TelephoneCallLogger.class));
        		break;
        		
        	case R.id.main_toolbar_about :
        		startActivity(new Intent(getApplicationContext(), About.class));
        		break;
        		
        	case R.id.main_toolbar_exit :
        		RecordServiceManager recordServiceManager = new RecordServiceManager();
        		boolean isRecordServiceRunning = recordServiceManager.isRunning(getApplicationContext());
        		
        		if(isRecordServiceRunning == false)
        		{
            		MonitoringServiceManager monitoringServiceManager = new MonitoringServiceManager();
            		monitoringServiceManager.stopService(getApplicationContext());
            		
            		PurgeServiceManager purgeServiceManager = new PurgeServiceManager();
            		purgeServiceManager.stopService(getApplicationContext());	
        		}

        		moveTaskToBack(true);
        		onBackPressed();
        		break;
        }
        
        return super.onOptionsItemSelected(item);
    }

	private void initDrawer()
	{
		 
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.main_toolbar_slide_menu_open, R.string.main_toolbar_slide_menu_close)
        {
 
            @Override
            public void onDrawerClosed(View drawerView)
            {
                super.onDrawerClosed(drawerView);
 
            }
 
            @Override
            public void onDrawerOpened(View drawerView)
            {
                super.onDrawerOpened(drawerView);
 
            }
        };
        
        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.setDrawerListener(drawerToggle);
    }
	
	
	@Override
    protected void onPostCreate(Bundle savedInstanceState)
	{
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }
 
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }
    
    public class SlidingMenuAdapter extends BaseAdapter
    {
    	
    	private Context context;
        private ArrayList<String> title;
        private ArrayList<String> subtitle;
        private ArrayList<Integer> icon;

    	public SlidingMenuAdapter(Context context, ArrayList<String> title, ArrayList<String> subtitle, ArrayList<Integer> icon)
    	{
    		this.context = context;
    		this.title = title;
    		this.subtitle = subtitle;
    		this.icon = icon;
    	}
    	
		@Override
    	public int getCount()
    	{
    		return title.size();
    	}

    	@Override
    	public Object getItem(int index)
    	{
    		return title.get(index);
    	}

    	@Override
    	public long getItemId(int index)
    	{
    		return index;
    	}

    	@Override
    	public View getView(int index, View view, ViewGroup arg2)
    	{	
    		if (view == null)
    		{
                LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                view = mInflater.inflate(R.layout.sliding_menu_item, null);
            }

            TextView titleTextview = (TextView) view.findViewById(R.id.sliding_menu_item_title);
            TextView subtitleTextview = (TextView) view.findViewById(R.id.sliding_menu_item_subtitle);
            ImageView IconImageview = (ImageView) view.findViewById(R.id.sliding_menu_item_icon);

            String itemTitle = title.get(index);
            String itemSubtitle = subtitle.get(index);
            int itemIcon = icon.get(index);
      
            titleTextview.setText(itemTitle);
            subtitleTextview.setText(itemSubtitle);
            IconImageview.setImageResource(itemIcon);
             
            return view;
    	}
    } 
}