/**
 * @file TelephoneCallNotifier.java
 * @brief Telephone call notifier class
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

package fr.vassela.acrrd.notifier;

import java.util.Date;
import fr.vassela.acrrd.Main;
import fr.vassela.acrrd.R;
import fr.vassela.acrrd.database.DatabaseManager;
import android.annotation.TargetApi;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import android.support.v4.app.NotificationCompat;

public class TelephoneCallNotifier extends Application
{
	public static String SHOW_RECORDS = "fr.vassela.acrrd.act.SHOWRECORDS";
	
	private DatabaseManager databaseManager = new DatabaseManager();
	
	private int getNotificationId()
	{
		try
		{
			long currentTime = new Date().getTime();
			String currentTimeInString = String.valueOf(currentTime);
			String notificationIdInString = currentTimeInString.substring(currentTimeInString.length() - 5);
			int notificationId = Integer.valueOf(notificationIdInString);
			
			return notificationId;
		}
		catch (Exception e)
		{
			return 1;
		}
	}
	
	private int getOngoingNotificationId()
	{
			return 0;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void displayNotification(Context context, String ticker, String contentTitle, String contentText, boolean autoCancel, boolean ongoingEvent, boolean activateEvent)
	{
		try
		{
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			boolean isPreferencesNotificationsActivated = sharedPreferences.getBoolean("preferences_notifications_activate",false);
			boolean isPreferencesNotificationsSoundActivated = sharedPreferences.getBoolean("preferences_notifications_sound_activate",false);
			boolean isPreferencesNotificationsVibrateActivated = sharedPreferences.getBoolean("preferences_notifications_vibrate_activate",false);
			boolean isPreferencesNotificationsLedActivated = sharedPreferences.getBoolean("preferences_notifications_led_activate",false);
			
			if(isPreferencesNotificationsActivated == true)
			{
				long notificationWhen = System.currentTimeMillis();
				int notificationDefaults = 0;
				        
				Intent intent;
				
				if(ongoingEvent == true)
				{
					intent = new Intent(context, Main.class);
					intent.putExtra("setCurrentTab", "home");
				}
				else
				{
					intent = new Intent(SHOW_RECORDS);
				}
				
				PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
				
				NotificationManager notificationManager  = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
				Notification.Builder notificationBuilder = new Notification.Builder(context);
				
				if(ongoingEvent == false)
				{
					notificationBuilder.setWhen(notificationWhen);
				}
				
				if(isPreferencesNotificationsSoundActivated == true)
				{
					notificationDefaults = notificationDefaults | Notification.DEFAULT_SOUND;
				}
				
				if(isPreferencesNotificationsVibrateActivated == true)
				{
					notificationDefaults = notificationDefaults | Notification.DEFAULT_VIBRATE;
				}
				
				if(isPreferencesNotificationsLedActivated == true)
				{
					notificationDefaults = notificationDefaults | Notification.DEFAULT_LIGHTS;
				}
				
				if(ongoingEvent == false)
				{
					notificationBuilder.setDefaults(notificationDefaults);
				}
					
				if(activateEvent == true)
				{
					notificationBuilder.setSmallIcon(R.drawable.presence_audio_online);	
				}
				else
				{
					notificationBuilder.setSmallIcon(R.drawable.presence_audio_busy);
				}

				if(ongoingEvent == false)
				{
					notificationBuilder.setTicker(ticker);
				}
				
				notificationBuilder.setContentTitle(contentTitle);
				notificationBuilder.setContentText(contentText);
				notificationBuilder.setContentIntent(pendingIntent);
				notificationBuilder.setAutoCancel(autoCancel);
				notificationBuilder.setOngoing(ongoingEvent);
				
				Notification notification = notificationBuilder.build();
		
				if(ongoingEvent == true)
				{
					notificationManager.notify(getOngoingNotificationId(), notification);
				}
				else
				{
					notificationManager.notify(getNotificationId(), notification);
				}
			}
		}
		catch (Exception e)
		{
			Log.w("TelephoneCallNotifier", "displayNotification : " + context.getApplicationContext().getString(R.string.log_telephone_call_notifier_error_display_notification) + " : " + e);
			databaseManager.insertLog(context.getApplicationContext(), "" + context.getApplicationContext().getString(R.string.log_telephone_call_notifier_error_display_notification), new Date().getTime(), 2, false);
		}
	}
	
	public void displayCompatNotification(Context context, String ticker, String contentTitle, String contentText, boolean autoCancel, boolean ongoingEvent, boolean activateEvent)
	{
			try
			{
				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
				boolean isPreferencesNotificationsActivated = sharedPreferences.getBoolean("preferences_notifications_activate",false);
				boolean isPreferencesNotificationsSoundActivated = sharedPreferences.getBoolean("preferences_notifications_sound_activate",false);
				boolean isPreferencesNotificationsVibrateActivated = sharedPreferences.getBoolean("preferences_notifications_vibrate_activate",false);
				boolean isPreferencesNotificationsLedActivated = sharedPreferences.getBoolean("preferences_notifications_led_activate",false);
				
				if(isPreferencesNotificationsActivated == true)
				{
					long notificationWhen = System.currentTimeMillis();
					int notificationDefaults = 0;
					        
					Intent intent;
					
					if(ongoingEvent == true)
					{
						intent = new Intent(context, Main.class);
						intent.putExtra("setCurrentTab", "home");
					}
					else
					{
						intent = new Intent(SHOW_RECORDS);
					}
					
					PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
					
					NotificationManager notificationManager  = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
					NotificationCompat.Builder notificationCompatBuilder = new NotificationCompat.Builder(context);
					
					if(ongoingEvent == false)
					{
						notificationCompatBuilder.setWhen(notificationWhen);
					}
					
					if(isPreferencesNotificationsSoundActivated == true)
					{
						notificationDefaults = notificationDefaults | Notification.DEFAULT_SOUND;
					}
					
					if(isPreferencesNotificationsVibrateActivated == true)
					{
						notificationDefaults = notificationDefaults | Notification.DEFAULT_VIBRATE;
					}
					
					if(isPreferencesNotificationsLedActivated == true)
					{
						notificationDefaults = notificationDefaults | Notification.DEFAULT_LIGHTS;
					}
					
					if(ongoingEvent == false)
					{
						notificationCompatBuilder.setDefaults(notificationDefaults);
					}
					
					if(activateEvent == true)
					{
						notificationCompatBuilder.setSmallIcon(R.drawable.presence_audio_online);	
					}
					else
					{
						notificationCompatBuilder.setSmallIcon(R.drawable.presence_audio_busy);
					}
					
					if(ongoingEvent == false)
					{
						notificationCompatBuilder.setTicker(ticker);
					}
					
					notificationCompatBuilder.setContentTitle(contentTitle);
					notificationCompatBuilder.setContentText(contentText);
					notificationCompatBuilder.setContentIntent(pendingIntent);
					notificationCompatBuilder.setAutoCancel(autoCancel);
					notificationCompatBuilder.setOngoing(ongoingEvent);
					
					Notification notification = notificationCompatBuilder.build();
			        
					if(ongoingEvent == true)
					{
						notificationManager.notify(getOngoingNotificationId(), notification);
					}
					else
					{
						notificationManager.notify(getNotificationId(), notification);
					}
				}
			}
			catch (Exception e)
			{
				Log.w("TelephoneCallNotifier", "displayCompatNotification : " + context.getApplicationContext().getString(R.string.log_telephone_call_notifier_error_display_notification) + " : " + e);
				databaseManager.insertLog(context.getApplicationContext(), "" + context.getApplicationContext().getString(R.string.log_telephone_call_notifier_error_display_notification), new Date().getTime(), 2, false);
			}
	}
	
	public void cancelOngoingNotification(Context context)
	{
		try
		{
			NotificationManager notificationManager  = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancel(getOngoingNotificationId());
		}
		catch (Exception e)
		{
			
		}
	}
	
	public void displayToast(Context context, String text, boolean length_long)
	{
		try
		{
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			boolean isPreferencesToastMessagesActivated = sharedPreferences.getBoolean("preferences_toast_messages_activate",false);
			
			if(isPreferencesToastMessagesActivated == true)
			{
				Toast toast;
				
				if(length_long == true)
				{
					toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
				}
				else
				{
					toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
				}
				
				toast.show();
			}
		}
		catch (Exception e)
		{
			Log.w("TelephoneCallNotifier", "displayToast : " + context.getApplicationContext().getString(R.string.log_telephone_call_notifier_error_display_toast) + " : " + e);
			databaseManager.insertLog(context.getApplicationContext(), "" + context.getApplicationContext().getString(R.string.log_telephone_call_notifier_error_display_toast), new Date().getTime(), 2, false);
		}
	}
}