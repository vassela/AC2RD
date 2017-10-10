/**
 * @file ThemeManager.java
 * @brief Theme manager class
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

package fr.vassela.acrrd.theme;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import fr.vassela.acrrd.R;
import fr.vassela.acrrd.database.DatabaseManager;
import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.preference.PreferenceManager;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TabWidget;
import android.widget.TableRow;
import android.widget.TextView;

public class ThemeManager extends Application
{
	private int[][] theme = {
			{R.style.Default, R.style.Default,
				R.style.Default, R.style.Default,
				R.style.Default, R.style.Default,
				R.style.Default, R.style.Default,
				R.style.Default, R.style.Default},
			{R.style.LightDarkActionBar_LightBlue, R.style.LightDarkActionBar_Blue,
				R.style.LightDarkActionBar_LightPurple, R.style.LightDarkActionBar_Purple,
				R.style.LightDarkActionBar_LightGreen, R.style.LightDarkActionBar_Green,
				R.style.LightDarkActionBar_LightOrange, R.style.LightDarkActionBar_Orange,
				R.style.LightDarkActionBar_LightRed, R.style.LightDarkActionBar_Red},
			{R.style.Dark_LightBlue, R.style.Dark_Blue,
				R.style.Dark_LightPurple, R.style.Dark_Purple,
				R.style.Dark_LightGreen, R.style.Dark_Green,
				R.style.Dark_LightOrange, R.style.Dark_Orange,
				R.style.Dark_LightRed, R.style.Dark_Red},
			{R.style.Light_LightBlue, R.style.Light_Blue,
				R.style.Light_LightPurple, R.style.Light_Purple,
				R.style.Light_LightGreen, R.style.Light_Green,
				R.style.Light_LightOrange, R.style.Light_Orange,
				R.style.Light_LightRed, R.style.Light_Red}
	};
	
	private int[][] color = {
			{R.color.orange, R.color.orange,
				R.color.orange, R.color.orange,
				R.color.orange, R.color.orange,
				R.color.orange, R.color.orange,
				R.color.orange, R.color.orange},
			{R.color.lightblue, R.color.blue,
				R.color.lightpurple, R.color.purple,
				R.color.lightgreen, R.color.green,
				R.color.orange, R.color.orange,
				R.color.lightred, R.color.red},
			{R.color.lightblue, R.color.blue,
				R.color.lightpurple, R.color.purple,
				R.color.lightgreen, R.color.green,
				R.color.lightorange, R.color.orange,
				R.color.lightred, R.color.red},
			{R.color.lightblue, R.color.blue,
				R.color.lightpurple, R.color.purple,
				R.color.lightgreen, R.color.green,
				R.color.orange, R.color.orange,
				R.color.lightred, R.color.red}
	};
	
	private int[][] tabwidget_drawable = {
			{R.drawable.tabwidget_orange, R.drawable.tabwidget_orange,
				R.drawable.tabwidget_orange, R.drawable.tabwidget_orange,
				R.drawable.tabwidget_orange, R.drawable.tabwidget_orange,
				R.drawable.tabwidget_orange, R.drawable.tabwidget_orange,
				R.drawable.tabwidget_orange, R.drawable.tabwidget_orange},
			{R.drawable.tabwidget_lightblue, R.drawable.tabwidget_blue,
				R.drawable.tabwidget_lightpurple, R.drawable.tabwidget_purple,
				R.drawable.tabwidget_lightgreen, R.drawable.tabwidget_green,
				R.drawable.tabwidget_lightorange, R.drawable.tabwidget_orange,
				R.drawable.tabwidget_lightred, R.drawable.tabwidget_red},
			{R.drawable.tabwidget_lightblue, R.drawable.tabwidget_blue,
				R.drawable.tabwidget_lightpurple, R.drawable.tabwidget_purple,
				R.drawable.tabwidget_lightgreen, R.drawable.tabwidget_green,
				R.drawable.tabwidget_lightorange, R.drawable.tabwidget_orange,
				R.drawable.tabwidget_lightred, R.drawable.tabwidget_red},
			{R.drawable.tabwidget_lightblue, R.drawable.tabwidget_blue,
				R.drawable.tabwidget_lightpurple, R.drawable.tabwidget_purple,
				R.drawable.tabwidget_lightgreen, R.drawable.tabwidget_green,
				R.drawable.tabwidget_lightorange, R.drawable.tabwidget_orange,
				R.drawable.tabwidget_lightred, R.drawable.tabwidget_red}
	};
	
	private int[][] tablerow_drawable = {
			{R.drawable.tablerow_orange, R.drawable.tablerow_orange,
				R.drawable.tablerow_orange, R.drawable.tablerow_orange,
				R.drawable.tablerow_orange, R.drawable.tablerow_orange,
				R.drawable.tablerow_orange, R.drawable.tablerow_orange,
				R.drawable.tablerow_orange, R.drawable.tablerow_orange},
			{R.drawable.tablerow_lightblue_, R.drawable.tablerow_blue,
				R.drawable.tablerow_lightpurple, R.drawable.tablerow_purple,
				R.drawable.tablerow_lightgreen, R.drawable.tablerow_green,
				R.drawable.tablerow_lightorange, R.drawable.tablerow_orange,
				R.drawable.tablerow_lightred, R.drawable.tablerow_red},
			{R.drawable.tablerow_lightblue_, R.drawable.tablerow_blue,
				R.drawable.tablerow_lightpurple, R.drawable.tablerow_purple,
				R.drawable.tablerow_lightgreen, R.drawable.tablerow_green,
				R.drawable.tablerow_lightorange, R.drawable.tablerow_orange,
				R.drawable.tablerow_lightred, R.drawable.tablerow_red},
			{R.drawable.tablerow_lightblue_, R.drawable.tablerow_blue,
				R.drawable.tablerow_lightpurple, R.drawable.tablerow_purple,
				R.drawable.tablerow_lightgreen, R.drawable.tablerow_green,
				R.drawable.tablerow_lightorange, R.drawable.tablerow_orange,
				R.drawable.tablerow_lightred, R.drawable.tablerow_red}
	};
	
	private int[][] color_replayer = {
			{Color.argb(88, 255, 136, 0), Color.argb(88, 255, 136, 0),
				Color.argb(88, 255, 136, 0), Color.argb(88, 255, 136, 0),
				Color.argb(88, 255, 136, 0), Color.argb(88, 255, 136, 0),
				Color.argb(88, 255, 136, 0), Color.argb(88, 255, 136, 0),
				Color.argb(88, 255, 136, 0), Color.argb(88, 255, 136, 0)},
			{Color.argb(88, 51, 181, 229), Color.argb(88, 0, 153, 204),
				Color.argb(88, 170, 102, 204), Color.argb(88, 153, 51, 204),
				Color.argb(88, 153, 204, 0), Color.argb(88, 102, 153, 0),
				Color.argb(88, 255, 187, 51), Color.argb(88, 255, 136, 0),
				Color.argb(88, 255, 68, 68), Color.argb(88, 204, 0, 0)},
			{Color.argb(88, 51, 181, 229), Color.argb(88, 0, 153, 204),
				Color.argb(88, 170, 102, 204), Color.argb(88, 153, 51, 204),
				Color.argb(88, 153, 204, 0), Color.argb(88, 102, 153, 0),
				Color.argb(88, 255, 187, 51), Color.argb(88, 255, 136, 0),
				Color.argb(88, 255, 68, 68), Color.argb(88, 204, 0, 0)},
			{Color.argb(88, 51, 181, 229), Color.argb(88, 0, 153, 204),
				Color.argb(88, 170, 102, 204), Color.argb(88, 153, 51, 204),
				Color.argb(88, 153, 204, 0), Color.argb(88, 102, 153, 0),
				Color.argb(88, 255, 187, 51), Color.argb(88, 255, 136, 0),
				Color.argb(88, 255, 68, 68), Color.argb(88, 204, 0, 0)}
	};
	
	private int[] locales_flags = {
			R.drawable.en_us,
			R.drawable.ar_sa,
			R.drawable.de_de,
			R.drawable.en_us,
			R.drawable.es_es,
			R.drawable.fr_fr,
			R.drawable.hi_in,
			R.drawable.jp_jp,
			R.drawable.pt_pt,
			R.drawable.ru_ru,
			R.drawable.zh_cn
	};
	
	private DatabaseManager databaseManager = new DatabaseManager();
    
    private int hilightColor = Color.argb(255, 51, 181, 229);
    private int replayFileDisplayerColor = Color.argb(88, 51, 181, 229);
    
    public void setTheme(Context context, Activity activity, int theme)
    {
    	try
		{
    		activity.setTheme(theme);
		}
		catch (Exception e)
		{
			Log.w("ThemeManager", "setTheme : " + context.getString(R.string.log_theme_manager_error_set_theme) + " : " + theme + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_theme_manager_error_set_theme) + " : " + theme, new Date().getTime(), 2, false);
		}
    }
    
    public void setPreferencesTheme(Context context, Activity activity)
    {
    	try
		{
    		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    		int themeSet = Integer.parseInt(sharedPreferences.getString("preferences_theme_set", "0"));
    		int colorThemeSet = Integer.parseInt(sharedPreferences.getString("preferences_color_theme_set", "7"));

    		Log.w("ThemeManager", "setPreferencesTheme : " + themeSet + " | " + colorThemeSet + " : " + theme[themeSet][colorThemeSet]);

    		setTheme(context, activity, theme[themeSet][colorThemeSet]);
    		
		}
		catch (Exception e)
		{
			Log.w("ThemeManager", "setPreferencesTheme : " + context.getString(R.string.log_theme_manager_error_set_preferences_theme) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_theme_manager_error_set_preferences_theme), new Date().getTime(), 2, false);
		}
    }
    
	public void setTabWidget(Context context, TabWidget tabwidget)
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		int themeSet = Integer.parseInt(sharedPreferences.getString("preferences_theme_set", "0"));
		int colorThemeSet = Integer.parseInt(sharedPreferences.getString("preferences_color_theme_set", "7"));
		
		for(int i = 0; i < tabwidget.getChildCount(); i++)
		{
			View v = tabwidget.getChildAt(i);

			TextView textview = (TextView)v.findViewById(android.R.id.title);
			if(textview == null)
			{
					continue;
			}

			v.setBackgroundResource(tabwidget_drawable[themeSet][colorThemeSet]);
		}
    }
	
	public void setTableBorder(Context context, TableRow tableRow)
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		int themeSet = Integer.parseInt(sharedPreferences.getString("preferences_theme_set", "0"));
		int colorThemeSet = Integer.parseInt(sharedPreferences.getString("preferences_color_theme_set", "7"));
		
		tableRow.setBackgroundResource(tablerow_drawable[themeSet][colorThemeSet]);
    }

	public void setAlertDialog(Context context, Dialog dialog)
	{
		try
		{
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			int themeSet = Integer.parseInt(sharedPreferences.getString("preferences_theme_set", "0"));
			int colorThemeSet = Integer.parseInt(sharedPreferences.getString("preferences_color_theme_set", "7"));
			
			ViewGroup decorView = (ViewGroup) dialog.getWindow().getDecorView();
			if (decorView == null)
			{
				return;
			}
			
			FrameLayout windowContentView = (FrameLayout) decorView.getChildAt(0);
			if (windowContentView == null)
			{
				return;
			}
			
			FrameLayout contentView = (FrameLayout) windowContentView.getChildAt(0);
			if (contentView == null)
			{
				return;
			}
			
			LinearLayout parentPanel = (LinearLayout) contentView.getChildAt(0);
			if (parentPanel == null)
			{
				return;
			}
			
			LinearLayout topPanel = (LinearLayout) parentPanel.getChildAt(0);
			if (topPanel == null)
			{
				return;
			}
			
			View titleDivider = topPanel.getChildAt(2);
			if (titleDivider == null)
			{
				return;
			}
			else
			{
				titleDivider.setBackgroundColor(context.getResources().getColor(color[themeSet][colorThemeSet]));
			}	
			
			LinearLayout titleTemplate = (LinearLayout) topPanel.getChildAt(1);
			if (titleTemplate == null)
			{
				return;
			}
			
			TextView alertTitle = (TextView) titleTemplate.getChildAt(1);
			if (alertTitle == null)
			{
				return;
			}
			else
			{
				alertTitle.setTextColor(context.getResources().getColor(color[themeSet][colorThemeSet]));
			}
		}
		catch (Exception e)
		{

		}
	}

	public void setMediaController(Context context, View view)
	{
		if (view instanceof MediaController) {
			MediaController v = (MediaController) view;
			for (int i = 0; i < v.getChildCount(); i++)
			{
					this.setMediaController(context, v.getChildAt(i));
			}
		}
		else if (view instanceof LinearLayout)
		{
			LinearLayout ll = (LinearLayout) view;
			for (int i = 0; i < ll.getChildCount(); i++)
			{
					this.setMediaController(context, ll.getChildAt(i));
			}
		}
		else if (view instanceof SeekBar)
		{
			((SeekBar) view).getProgressDrawable().mutate().setColorFilter(context.getResources().getColor(getColorTheme(context)),PorterDuff.Mode.SRC_IN);
		}
	}
	
	public void setSeekBar(Context context, SeekBar seekBar)
	{
		seekBar.getProgressDrawable().mutate().setColorFilter(context.getResources().getColor(getColorTheme(context)),PorterDuff.Mode.SRC_IN);
	}
    
    public int getColorTheme(Context context)
    {
    	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		int themeSet = Integer.parseInt(sharedPreferences.getString("preferences_theme_set", "0"));
		int colorThemeSet = Integer.parseInt(sharedPreferences.getString("preferences_color_theme_set", "7"));
		
		return color[themeSet][colorThemeSet];
    }
    
    public int getColorReplayer(Context context)
    {
    	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		int themeSet = Integer.parseInt(sharedPreferences.getString("preferences_theme_set", "0"));
		int colorThemeSet = Integer.parseInt(sharedPreferences.getString("preferences_color_theme_set", "7"));
		
		return color_replayer[themeSet][colorThemeSet];
    }
    
    public int getHilightColor()
    {
    	return hilightColor;
    }
    
    public int getReplayFileDisplayerColor()
    {
    	return replayFileDisplayerColor;
    }
    
    public int getColorAccent(Context context)
    {
        TypedValue typedValue = new TypedValue();

        TypedArray typedArray = context.obtainStyledAttributes(typedValue.data, new int[] { R.attr.colorAccent });
        int color = typedArray.getColor(0, 0);

        typedArray.recycle();

        return color;
    }
    
    @ColorInt
    public int getThemeColor
    (
            @NonNull final Context context,
            @AttrRes final int attributeColor
    )
    {
        final TypedValue value = new TypedValue();
        context.getTheme ().resolveAttribute (attributeColor, value, true);
        return value.data;
    }
    
    public int getLocaleFlag(Context context, int locale)
    {
    	if(locale == 0)
		{
    		Locale defaultLocale = Resources.getSystem().getConfiguration().locale;
    		if(defaultLocale.toString().contains("ar")){ return R.drawable.ar_sa; }
    		else if(defaultLocale.toString().contains("de")){ return R.drawable.de_de; }
    		else if(defaultLocale.toString().contains("en")){ return R.drawable.en_us; }
    		else if(defaultLocale.toString().contains("es")){ return R.drawable.es_es; }
    		else if(defaultLocale.toString().contains("fr")){ return R.drawable.fr_fr; }
    		else if(defaultLocale.toString().contains("hi")){ return R.drawable.hi_in; }
    		else if(defaultLocale.toString().contains("jp")){ return R.drawable.jp_jp; }
    		else if(defaultLocale.toString().contains("pt")){ return R.drawable.pt_pt; }
    		else if(defaultLocale.toString().contains("ru")){ return R.drawable.ru_ru; }
    		else if(defaultLocale.toString().contains("zh")){ return R.drawable.zh_cn; }
    		else { return R.drawable.en_us; }
		}
    	else
    	{
	    	return locales_flags[locale];
    	}
    }

}