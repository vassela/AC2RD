/**
 * @file EditTextPreferenceThemeManager.java
 * @brief Edit text preference theme manager class
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

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class EditTextPreferenceThemeManager extends EditTextPreference
{
	private ThemeManager themeManager = new ThemeManager();
	
	public EditTextPreferenceThemeManager(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
    }

	public EditTextPreferenceThemeManager(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}
	
	@Override
	protected void showDialog(Bundle state)
	{
		super.showDialog(state);
		final Resources res = getContext().getResources();
		final Window window = getDialog().getWindow();
		final int color = res.getColor(themeManager.getColorTheme(getContext()));

		// Title
		final int titleId = res.getIdentifier("alertTitle", "id", "android");
		final View title = window.findViewById(titleId);
		if (title != null)
		{
			((TextView) title).setTextColor(color);
		}
		
		// Title divider
		final int titleDividerId = res.getIdentifier("titleDivider", "id", "android");
		final View titleDivider = window.findViewById(titleDividerId);
		if (titleDivider != null)
		{
			titleDivider.setBackgroundColor(color);
		}
	}
}