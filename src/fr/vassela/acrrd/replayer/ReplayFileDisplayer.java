/**
 * @file ReplayFileDisplayer.java
 * @brief Replay file displayer class
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

package fr.vassela.acrrd.replayer;

import java.util.Date;
import fr.vassela.acrrd.R;
import fr.vassela.acrrd.database.DatabaseManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

public class ReplayFileDisplayer
{
	protected float[] replayFileDisplayerPoints;
	private Paint replayFileDisplayerPaint;
	
	private int replayFileDisplayerMode = 0;
	
	private DatabaseManager databaseManager = new DatabaseManager();
	
	public ReplayFileDisplayer(Context context, Paint paint, int mode)
	{
		super();
		
		try
		{
			replayFileDisplayerPaint = paint;
			replayFileDisplayerMode = mode;
		}
		catch (Exception e)
		{
			Log.e("ReplayFileDisplayer", "ReplayFileDisplayer : " + context.getString(R.string.log_replay_file_displayer_error_create) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_replay_file_displayer_error_create), new Date().getTime(), 1, false);
		}
	}
	
	public void audioDisplayer(Context context, Canvas canvas, byte[] bytes, Rect rect)
	{
		try
		{
			if (replayFileDisplayerPoints == null || replayFileDisplayerPoints.length < bytes.length * 4)
			{
				replayFileDisplayerPoints = new float[bytes.length * 4];
			}
			
			switch(replayFileDisplayerMode)
			{
				case 0:
					break;
					
				case 1:
			
					for (int i = 0; i < bytes.length - 1; i++)
					{
						float wavePositionStart = rect.width() * i / (bytes.length - 1);
						float wavePositionEnd = rect.width() * (i + 1) / (bytes.length - 1);
						byte waveFormStart = (byte) (bytes[i] + 128);
						byte waveFormEnd = (byte) (bytes[i + 1] + 128);
						float rectCenter = rect.height() / 2;
						float adjustment = (rect.height() / 3) / 128;
						float x1 = wavePositionStart;
						float y1 = rectCenter + waveFormStart * adjustment;
						float x2 = wavePositionEnd;
						float y2 = rectCenter + waveFormEnd * adjustment;
						
						replayFileDisplayerPoints[i * 4] = x1;
						replayFileDisplayerPoints[i * 4 + 1] = y1;
						replayFileDisplayerPoints[i * 4 + 2] = x2;
						replayFileDisplayerPoints[i * 4 + 3] = y2;
					}
			
					canvas.drawLines(replayFileDisplayerPoints, replayFileDisplayerPaint);
					break;
					
				case 2:
					for (int i = 0; i < bytes.length / 2; i++)
					{
						byte realPart = bytes[2 * i];
						byte imaginaryPart = bytes[2 * i + 1];
						float magnitude = (realPart * realPart + imaginaryPart * imaginaryPart);
						int decibels = (int) (10 * Math.log10(magnitude));
						int amplitude = decibels * 2 - 10;
						float rectCenter = rect.height() - (rect.height() / 2);
						int adjustment = 3;
						float x1 =  2 * i * 4;
						float y1 = rectCenter - adjustment;
						float x2 = 2 * i * 4;
						float y2 = rectCenter - amplitude - adjustment;
						
						replayFileDisplayerPoints[i * 4] = x1;
						replayFileDisplayerPoints[i * 4 + 1] = y1;
						replayFileDisplayerPoints[i * 4 + 2] = x2;
						replayFileDisplayerPoints[i * 4 + 3] = y2;
					}
					
					canvas.drawLines(replayFileDisplayerPoints, replayFileDisplayerPaint);
					
					for (int i = 0; i < bytes.length / 2; i++)
					{
						byte realPart = bytes[2 * i];
						byte imaginaryPart = bytes[2 * i + 1];
						float magnitude = (realPart * realPart + imaginaryPart * imaginaryPart);
						int decibels = (int) (10 * Math.log10(magnitude));
						int amplitude = decibels * 2 - 10;
						float rectCenter = rect.height() / 2;
						int adjustment = 3;
						float x1 = 2 * i * 4;
						float y1 = rectCenter + adjustment;
						float x2 = 2 * i * 4;
						float y2 = rectCenter + amplitude + adjustment;
						
						replayFileDisplayerPoints[i * 4] = x1;
						replayFileDisplayerPoints[i * 4 + 1] = y1;
						replayFileDisplayerPoints[i * 4 + 2] = x2;
						replayFileDisplayerPoints[i * 4 + 3] = y2;
					}
					
					canvas.drawLines(replayFileDisplayerPoints, replayFileDisplayerPaint);
					
					break;
					
				default:
					break;
			}
		}
		catch (Exception e)
		{
			Log.e("ReplayFileDisplayer", "audioDisplayer : " + context.getString(R.string.log_replay_file_displayer_error_audio_displayer) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_replay_file_displayer_error_audio_displayer), new Date().getTime(), 1, false);
		}
	}
}