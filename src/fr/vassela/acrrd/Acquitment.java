/**
 * @file Acquitment.java
 * @brief Acquitment of the terms of use of the software
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

import fr.vassela.acrrd.R;
import fr.vassela.acrrd.localizer.LocalizerManager;
import fr.vassela.acrrd.theme.ThemeManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class Acquitment extends Activity implements OnClickListener
{
	private LocalizerManager localizerManager = new LocalizerManager();
	private ThemeManager themeManager = new ThemeManager();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		try
		{
			localizerManager.setPreferencesLocale(getApplicationContext());
			themeManager.setPreferencesTheme(getApplicationContext(), this);
			
			super.onCreate(savedInstanceState);
	
			setContentView(R.layout.acquitment);
			
			Button acquitment_accept = (Button) findViewById (R.id.acquitment_accept);
			Button acquitment_cancel = (Button) findViewById (R.id.acquitment_cancel);
			acquitment_accept.setEnabled(false);
			acquitment_accept.setClickable(false); 
			
			acquitment_accept.setOnClickListener(this);
			acquitment_cancel.setOnClickListener(this);
			
			AppCompatCheckBox acquitment_checkbox = (AppCompatCheckBox) findViewById (R.id.acquitment_checkbox);
			acquitment_checkbox.setChecked(false);
			
			acquitment_checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener()
			{
	
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
				{
					Button acquitment_accept = (Button) findViewById (R.id.acquitment_accept);
	
					if (isChecked == true){
						acquitment_accept.setEnabled(true);
						acquitment_accept.setClickable(true);
						acquitment_accept.setCompoundDrawablesWithIntrinsicBounds(R.drawable.btn_check_buttonless_on, 0, 0, 0);
					}
					else {
						acquitment_accept.setEnabled(false);
						acquitment_accept.setClickable(false); 
						acquitment_accept.setCompoundDrawablesWithIntrinsicBounds(R.drawable.btn_check_buttonless_off, 0, 0, 0);
					}
				}
			});
		}
		catch (Exception e)
		{
			Log.e("Acquitment", "onCreate : " + getApplicationContext().getString(R.string.log_acquitment_error_create) + " : " + e);
		};
	}
	
	@Override
	public void onClick(View v)
	{
		try
		{
			switch (v.getId())
			{
				case R.id.acquitment_accept:
					SharedPreferences sharedPreferencesStoragePathValue = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
					SharedPreferences.Editor sharedPreferencesEditor = sharedPreferencesStoragePathValue.edit();
	    			sharedPreferencesEditor.putBoolean("about_app_acquitment", true);
	    			sharedPreferencesEditor.commit();
					
					finish();
					Intent intent = new Intent(getApplicationContext(), Main.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra("setCurrentTab", "home");
		            startActivity(intent);
					break;
					
				case R.id.acquitment_cancel:
					super.onBackPressed();
					break;
			}
		}
		catch (Exception e)
		{
			Log.e("Acquitment", "onClick : " + getApplicationContext().getString(R.string.log_acquitment_error_click) + " : " + e);
		};
	}
}