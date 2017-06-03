/**
 * @file ReplayFileVisualizer.java
 * @brief Replay file visualizer class
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
import java.util.HashSet;
import java.util.Set;

import fr.vassela.acrrd.R;
import fr.vassela.acrrd.database.DatabaseManager;
import fr.vassela.acrrd.theme.ThemeManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class ReplayFileVisualizer extends View
{
	private Visualizer replayFileVisualizer;
	
	private int replayFileVisualizerMode = 0;
	
	private byte[] replayFileVisualizerBytes;
	
	private Rect replayFileVisualizerRect = new Rect();
	private Bitmap replayFileVisualizerBitmap;
	private Canvas replayFileVisualizerCanvas;
	private Paint replayFileVisualizerPaint = new Paint();
	Matrix replayFileVisualizerMatrix = new Matrix();
	
	private Set<ReplayFileDisplayer> replayFileVisualizerDisplayerHashSet;
	
	private DatabaseManager databaseManager = new DatabaseManager();
	private Context replayFileVisualizerContext = null;
	private ThemeManager themeManager = new ThemeManager();

	public ReplayFileVisualizer(Context context)
	{
		super(context, null, 0);
		
		try
		{
			replayFileVisualizerContext = context;
			initReplayFileVisualizer(context);
		}
		catch (Exception e)
		{
			Log.e("ReplayFileVisualizer", "ReplayFileVisualizer : " + context.getString(R.string.log_replay_file_visualizer_error_create) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_replay_file_visualizer_error_create), new Date().getTime(), 1, false);
		}
	}
	
	public ReplayFileVisualizer(Context context, AttributeSet attrs)
	{
		super(context, attrs, 0);
		
		try
		{
			replayFileVisualizerContext = context;
			initReplayFileVisualizer(context);
		}
		catch (Exception e)
		{
			Log.e("ReplayFileVisualizer", "ReplayFileVisualizer : " + context.getString(R.string.log_replay_file_visualizer_error_create) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_replay_file_visualizer_error_create), new Date().getTime(), 1, false);
		}
	}
	
	public ReplayFileVisualizer(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs);
		
		try
		{
			replayFileVisualizerContext = context;
			initReplayFileVisualizer(context);
		}
		catch (Exception e)
		{
			Log.e("ReplayFileVisualizer", "ReplayFileVisualizer : " + context.getString(R.string.log_replay_file_visualizer_error_create) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_replay_file_visualizer_error_create), new Date().getTime(), 1, false);
		}
	}

	private void initReplayFileVisualizer(Context context)
	{
		try
		{
			replayFileVisualizerContext = context;
			replayFileVisualizerBytes = null;
			replayFileVisualizerPaint.setColor(Color.argb(238, 255, 255, 255));
			replayFileVisualizerPaint.setXfermode(new PorterDuffXfermode(Mode.MULTIPLY));
			replayFileVisualizerDisplayerHashSet = new HashSet<ReplayFileDisplayer>();
		}
		catch (Exception e)
		{
			Log.e("ReplayFileVisualizer", "initReplayFileVisualizer : " + context.getString(R.string.log_replay_file_visualizer_error_init) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_replay_file_visualizer_error_init), new Date().getTime(), 1, false);
		}
	}

	public void linkToMediaPlayer(Context context, MediaPlayer mediaPlayer)
	{
		try
		{
			replayFileVisualizerContext = context;
			
			if(mediaPlayer == null)
			{
				return;
			}
			
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			replayFileVisualizerMode = Integer.parseInt(sharedPreferences.getString("preferences_replayer_visualizer", "1"));
			
			if(replayFileVisualizerMode == 0)
			{
				return;
			}

			replayFileVisualizer = new Visualizer(mediaPlayer.getAudioSessionId());
			replayFileVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
	
			Visualizer.OnDataCaptureListener captureListener = new Visualizer.OnDataCaptureListener()
			{
				@Override
				public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate)
				{
					if (replayFileVisualizerMode == 1)
					{
						updateReplayFileVisualizer(replayFileVisualizerContext, bytes);
					}
				}
	
				@Override
				public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate)
				{
					if (replayFileVisualizerMode == 2)
					{
						updateReplayFileVisualizer(replayFileVisualizerContext, bytes);
					}
				}
			};
	
			replayFileVisualizer.setDataCaptureListener(captureListener, Visualizer.getMaxCaptureRate() / 2, true, true);
	
			replayFileVisualizer.setEnabled(true);
			
			mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
			{
				@Override
				public void onCompletion(MediaPlayer mediaPlayer)
				{
					replayFileVisualizer.setEnabled(false);
				}
			});
		}
		catch (Exception e)
		{
			Log.e("ReplayFileVisualizer", "linkToMediaPlayer : " + context.getString(R.string.log_replay_file_visualizer_error_link) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_replay_file_visualizer_error_link), new Date().getTime(), 1, false);
		}
	}

	public void addReplayFileDisplayer(Context context, ReplayFileDisplayer replayFileDisplayer)
	{
		try
		{
			replayFileVisualizerContext = context;
			
			if(replayFileDisplayer != null)
			{
				replayFileVisualizerDisplayerHashSet.add(replayFileDisplayer);
			}
		}
		catch (Exception e)
		{
			Log.e("ReplayFileVisualizer", "addReplayFileDisplayer : " + context.getString(R.string.log_replay_file_visualizer_error_add_displayer) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_replay_file_visualizer_error_add_displayer), new Date().getTime(), 1, false);
		}
	}

	public void clearReplayFileDisplayerHashSet(Context context)
	{
		try
		{
			replayFileVisualizerContext = context;
			replayFileVisualizerDisplayerHashSet.clear();
		}
		catch (Exception e)
		{
			Log.e("ReplayFileVisualizer", "clearReplayFileDisplayerHashSet : " + context.getString(R.string.log_replay_file_visualizer_error_clear_displayer) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_replay_file_visualizer_error_clear_displayer), new Date().getTime(), 1, false);
		}
	}

	public void releaseReplayFileVisualizer(Context context)
	{
		try
		{
			replayFileVisualizerContext = context;
			replayFileVisualizer.release();
		}
		catch (Exception e)
		{
			Log.e("ReplayFileVisualizer", "releaseReplayFileVisualizer : " + context.getString(R.string.log_replay_file_visualizer_error_release_displayer) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_replay_file_visualizer_error_release_displayer), new Date().getTime(), 1, false);
		}
	}

	public void updateReplayFileVisualizer(Context context, byte[] bytes)
	{
		try
		{
			replayFileVisualizerContext = context;
			replayFileVisualizerBytes = bytes;
			invalidate();
		}
		catch (Exception e)
		{
			Log.e("ReplayFileVisualizer", "updateReplayFileVisualizer : " + context.getString(R.string.log_replay_file_visualizer_error_update_displayer) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_replay_file_visualizer_error_update_displayer), new Date().getTime(), 1, false);
		}
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);

		try
		{
			replayFileVisualizerRect.set(0, 0, getWidth(), getHeight());
	
			if(replayFileVisualizerBitmap == null)
			{
				replayFileVisualizerBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Config.ARGB_8888);
			}
	
			if(replayFileVisualizerCanvas == null)
			{
				replayFileVisualizerCanvas = new Canvas(replayFileVisualizerBitmap);
			}
	
			if (replayFileVisualizerBytes != null)
			{
				byte[] replayFileDisplayerBytes = replayFileVisualizerBytes;
				
				for(ReplayFileDisplayer replayFileDisplayer : replayFileVisualizerDisplayerHashSet)
				{
					replayFileDisplayer.audioDisplayer(replayFileVisualizerContext, replayFileVisualizerCanvas, replayFileDisplayerBytes, replayFileVisualizerRect);
				}
			}
	
			replayFileVisualizerCanvas.drawPaint(replayFileVisualizerPaint);

			canvas.drawBitmap(replayFileVisualizerBitmap, replayFileVisualizerMatrix, null);
		}
		catch (Exception e)
		{
			Log.e("ReplayFileVisualizer", "updateReplayFileVisualizer : " + replayFileVisualizerContext.getString(R.string.log_replay_file_visualizer_error_update_displayer) + " : " + e);
			databaseManager.insertLog(replayFileVisualizerContext, "" + replayFileVisualizerContext.getString(R.string.log_replay_file_visualizer_error_update_displayer), new Date().getTime(), 1, false);
		}
	}
	
	public void showAudioDisplayer(Context context)
	{
		try
		{
			replayFileVisualizerContext = context;
			
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setColor(themeManager.getColorReplayer(context));
			
			ReplayFileDisplayer replayFileDisplayer;

			switch(replayFileVisualizerMode)
			{
				case 0:
					break;
					
				case 1:
					paint.setStrokeWidth(3f);
					replayFileDisplayer = new ReplayFileDisplayer(context, paint, replayFileVisualizerMode);
					addReplayFileDisplayer(context, replayFileDisplayer);
					break;
					
				case 2:
					paint.setStrokeWidth(6f);
				    replayFileDisplayer = new ReplayFileDisplayer(context, paint, replayFileVisualizerMode);
					addReplayFileDisplayer(context, replayFileDisplayer);
					break;
				
				default:
					break;
			}
		}
		catch (Exception e)
		{
			Log.e("ReplayFileVisualizer", "showAudioDisplayer : " + context.getString(R.string.log_replay_file_visualizer_error_show_displayer) + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_replay_file_visualizer_error_show_displayer), new Date().getTime(), 1, false);
		}
	}
}