/**
 * @file TelephoneCallReceiver.java
 * @brief Telephone call receiver class
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

package fr.vassela.acrrd.receiver;

import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import fr.vassela.acrrd.R;
import fr.vassela.acrrd.database.DatabaseManager;
import fr.vassela.acrrd.filters.FiltersManager;
import fr.vassela.acrrd.recorder.RecordServiceManager;
import fr.vassela.acrrd.sensor.ShakeDetectorServiceManager;

/**
 * Classe permettant de récupérer les événements sur changement d'état du téléphone
 * @author Arnaud Vassellier
 *
 */
public class TelephoneCallReceiver extends BroadcastReceiver
{
	private static int previousState = TelephonyManager.CALL_STATE_IDLE;
	private static int currentState = TelephonyManager.CALL_STATE_IDLE;
	
	private static boolean isIncomingCall = true;
	private static boolean isConnectedCall = false;
	private static boolean isDoubleCall = false;
	
	private static String telephoneNumber;
	private static String doubleCallNumber;
	
	private static int isRecordableContact = -1;
	
	private FiltersManager filtersManager = new FiltersManager();
	private DatabaseManager databaseManager = new DatabaseManager();

	@Override
	public void onReceive(Context context, Intent intent)
	{
		try
		{
			if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL"))
			{
				Log.d("TelephoneCallReceiver", "onReceive : " + "NEW_OUTGOING_CALL");
				telephoneNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
		    }
			else
		    {
				String callState = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
				String incomingNumber = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
				int state = 0;
				
				if(callState.equals(TelephonyManager.EXTRA_STATE_IDLE))
				{
					Log.d("TelephoneCallReceiver", "onReceive : " + "CALL_STATE_IDLE");
					state = TelephonyManager.CALL_STATE_IDLE;	
				}
				else if(callState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK))
				{
					Log.d("TelephoneCallReceiver", "onReceive : " + "CALL_STATE_OFFHOOK");
					state = TelephonyManager.CALL_STATE_OFFHOOK;	
				}
				else if(callState.equals(TelephonyManager.EXTRA_STATE_RINGING))
				{
					Log.d("TelephoneCallReceiver", "onReceive : " + "CALL_STATE_RINGING");
					state = TelephonyManager.CALL_STATE_RINGING;
				}
				
				onCallStateChanged(context, state, incomingNumber);
			}
		}
		catch (Exception e)
		{
			Log.w("TelephoneCallReceiver", "onReceive : " + context.getApplicationContext().getString(R.string.log_telephone_call_receiver_error_receive) + " : " + e);
			databaseManager.insertLog(context.getApplicationContext(), "" + context.getApplicationContext().getString(R.string.log_telephone_call_receiver_error_receive), new Date().getTime(), 2, false);
		}
	}

	public void onCallStateChanged(Context context, int state, String number)
	{
		try
		{
			currentState = state;
			
		    if(previousState == currentState)
		    {
		    	return;
		    }
		    
		    switch (currentState)
		    {
			    case TelephonyManager.CALL_STATE_IDLE:
			    	if(previousState == TelephonyManager.CALL_STATE_RINGING)
			    	{
			    		onMissedCall(context, telephoneNumber);
			    	}
			    	else if(previousState == TelephonyManager.CALL_STATE_OFFHOOK)
			    	{
			    		if(isIncomingCall)
			    		{
			    			onIncomingCallEnded(context, telephoneNumber);
			    		}
			    		else
			    		{
			    			onOutgoingCallEnded(context, telephoneNumber);
			    		}	    		
			    	}
			    	isIncomingCall = true;
			    	isConnectedCall = false;
			    	isDoubleCall = false;
			    	telephoneNumber = "";
			    	doubleCallNumber = "";
			    	isRecordableContact = -1;
			        break;
			        
			    case TelephonyManager.CALL_STATE_RINGING:
			    	if(previousState == TelephonyManager.CALL_STATE_IDLE)
			    	{
				    	isIncomingCall = true;
				    	telephoneNumber = number;
			    	}
			    	else if(previousState == TelephonyManager.CALL_STATE_OFFHOOK)
			    	{
			    		isDoubleCall = true;
			    		doubleCallNumber = number;
			    	}
			        break;
			        
			    case TelephonyManager.CALL_STATE_OFFHOOK:
			    	if(previousState == TelephonyManager.CALL_STATE_RINGING)
			    	{
			    		if(isDoubleCall == false)
			    		{
				    		isConnectedCall = true;
				    		isRecordableContact = filtersManager.isContactFilter(context.getApplicationContext(), telephoneNumber);
				    		onIncomingCallStarted(context, telephoneNumber);
			    		}
			    		else
			    		{
			    			onDoubleCallDetected(context, doubleCallNumber);
			    		}
			    	}
			    	else if(previousState == TelephonyManager.CALL_STATE_IDLE)
			    	{
			    		isIncomingCall = false;
			    		isConnectedCall = true; // DEBUG
			    		if(isConnectedCall)
			    		{
			    			isRecordableContact = filtersManager.isContactFilter(context.getApplicationContext(), telephoneNumber);
			    			onOutgoingCallStarted(context, telephoneNumber);
			    		}
			    	}
			        break;
		    }
		    
		    previousState = currentState;
		}
		catch (Exception e)
		{
			Log.w("TelephoneCallReceiver", "onCallStateChanged : " + context.getApplicationContext().getString(R.string.log_telephone_call_receiver_error_call_state_change) + " : " + e);
			databaseManager.insertLog(context.getApplicationContext(), "" + context.getApplicationContext().getString(R.string.log_telephone_call_receiver_error_call_state_change), new Date().getTime(), 2, false);
		}
	}
	
	protected void onIncomingCallStarted(Context context, String number)
	{
		try
		{
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			boolean isPreferencesFiltersActivated = sharedPreferences.getBoolean("preferences_filters_activate",false);
			
			if((isPreferencesFiltersActivated == false) || ((isPreferencesFiltersActivated == true) && (isRecordableContact == 1)))
			{
				RecordServiceManager recordServiceManager = new RecordServiceManager();
				recordServiceManager.startService(context.getApplicationContext(), 0, number);
			}
			else
			{
				if((isPreferencesFiltersActivated == true) && (isRecordableContact != 0))
				{
					ShakeDetectorServiceManager shakeDetectorServiceManager = new ShakeDetectorServiceManager();
					shakeDetectorServiceManager.startService(context.getApplicationContext(), 0, number);
				}
			}
		}
		catch (Exception e)
		{
			Log.e("TelephoneCallReceiver", "onIncomingCallStarted : " + context.getApplicationContext().getString(R.string.log_telephone_call_receiver_error_incoming_call_started) + " : " + number + " : " + e);
			databaseManager.insertLog(context.getApplicationContext(), "" + context.getApplicationContext().getString(R.string.log_telephone_call_receiver_error_incoming_call_started) + " : " + number, new Date().getTime(), 1, false);
		}
	}

	protected void onOutgoingCallStarted(Context context, String number)
	{
		try
		{
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			boolean isPreferencesFiltersActivated = sharedPreferences.getBoolean("preferences_filters_activate",false);
			
			if((isPreferencesFiltersActivated == false) || ((isPreferencesFiltersActivated == true) && (isRecordableContact == 1)))
			{
				RecordServiceManager recordServiceManager = new RecordServiceManager();
				recordServiceManager.startService(context.getApplicationContext(), 1, number);
			}
			else
			{
				if((isPreferencesFiltersActivated == true) && (isRecordableContact != 0))
				{
					ShakeDetectorServiceManager shakeDetectorServiceManager = new ShakeDetectorServiceManager();
					shakeDetectorServiceManager.startService(context.getApplicationContext(), 1, number);
				}
			}
		}
		catch (Exception e)
		{
			Log.e("TelephoneCallReceiver", "onOutgoingCallStarted : " + context.getApplicationContext().getString(R.string.log_telephone_call_receiver_error_outgoing_call_started) + " : " + number + " : " + e);
			databaseManager.insertLog(context.getApplicationContext(), "" + context.getApplicationContext().getString(R.string.log_telephone_call_receiver_error_outgoing_call_started) + " : " + number, new Date().getTime(), 1, false);
		};
	}
	
	protected void onDoubleCallDetected(Context context, String number)
	{
		try
		{
			RecordServiceManager recordServiceManager = new RecordServiceManager();
			recordServiceManager.setDoubleCall(context.getApplicationContext(), number);
		}
		catch (Exception e)
		{
			Log.e("TelephoneCallReceiver", "onDoubleCallDetected : " + context.getApplicationContext().getString(R.string.log_telephone_call_receiver_error_double_call_detected) + " : " + number + " : " + e);
			databaseManager.insertLog(context.getApplicationContext(), "" + context.getApplicationContext().getString(R.string.log_telephone_call_receiver_error_double_call_detected) + " : " + number, new Date().getTime(), 1, false);
		};
	}
	
	protected void onIncomingCallEnded(Context context, String number)
	{
		try
		{
			RecordServiceManager recordServiceManager = new RecordServiceManager();
			recordServiceManager.stopService(context.getApplicationContext());
			
			ShakeDetectorServiceManager shakeDetectorServiceManager = new ShakeDetectorServiceManager();
			shakeDetectorServiceManager.stopService(context.getApplicationContext());
		}
		catch (Exception e)
		{
			Log.e("TelephoneCallReceiver", "onIncomingCallEnded : " + context.getApplicationContext().getString(R.string.log_telephone_call_receiver_error_incoming_call_ended) + " : " + number + " : " + e);
			databaseManager.insertLog(context.getApplicationContext(), "" + context.getApplicationContext().getString(R.string.log_telephone_call_receiver_error_incoming_call_ended) + " : " + number, new Date().getTime(), 1, false);
		};
	}
	
	protected void onOutgoingCallEnded(Context context, String number)
	{
		try
		{
			RecordServiceManager recordServiceManager = new RecordServiceManager();
			recordServiceManager.stopService(context.getApplicationContext());
			
			ShakeDetectorServiceManager shakeDetectorServiceManager = new ShakeDetectorServiceManager();
			shakeDetectorServiceManager.stopService(context.getApplicationContext());
		}
		catch (Exception e)
		{
			Log.e("TelephoneCallReceiver", "onOutgoingCallEnded : " + context.getApplicationContext().getString(R.string.log_telephone_call_receiver_error_outgoing_call_ended) + " : " + number + " : " + e);
			databaseManager.insertLog(context.getApplicationContext(), "" + context.getApplicationContext().getString(R.string.log_telephone_call_receiver_error_outgoing_call_ended) + " : " + number, new Date().getTime(), 1, false);
		};
	}
	
	protected void onMissedCall(Context context, String number)
	{
	}
	
	public static int getPreviousState()
	{
		return previousState;
	}
	
	public static int getCurrentState()
	{
		return currentState;
	}
}