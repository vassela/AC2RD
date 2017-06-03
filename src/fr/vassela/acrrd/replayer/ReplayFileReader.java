/**
 * @file ReplayFileReader.java
 * @brief Replay file reader class
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

import java.io.RandomAccessFile;
import java.util.Date;

import fr.vassela.acrrd.R;
import fr.vassela.acrrd.database.DatabaseManager;
import android.app.Application;
import android.content.Context;
import android.util.Log;

public class ReplayFileReader extends Application
{
	private static RandomAccessFile audioReplayRandomAccessFile;
	
	private int audioReplayPayloadSize;
	private int audioReplayHeaderSize;
	private short audioReplayFormatAudio;
	private short audioReplayChanelConfig;
	private int audioReplaySampleRate;
	private int audioReplayByteRate;
	private short audioReplayBlockAlign;
	private short audioReplayNbBitsPerSample;
	private int audioReplayDataSize;
	
	private DatabaseManager databaseManager = new DatabaseManager();

	public boolean onCreate(Context context, String audioReplayFileAbsolutePath)
	{
		try
		{
			if (audioReplayRandomAccessFile != null)
			{
				audioReplayRandomAccessFile.close();
			}
			
			audioReplayRandomAccessFile = new RandomAccessFile(audioReplayFileAbsolutePath, "r");
			
		}
		catch (Exception e)
		{
			Log.w("ReplayFileReader", "onCreate : " + context.getString(R.string.log_replay_file_reader_file_not_found) + " : " + audioReplayFileAbsolutePath + " : " + e);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_replay_file_reader_file_not_found) + " : " + audioReplayFileAbsolutePath, new Date().getTime(), 2, false);
			
			return false;
		}
		
		return true;
	}
	
	private static boolean onReadString(String string)
	{
		try
	    {
			byte[] byteBuffer = new byte[string.length()];
			
			for (int i = 0; i < string.length(); i++)
			{
			    try
			    {
			    	byteBuffer[i] = audioReplayRandomAccessFile.readByte();
				}
			    catch (Exception e)
			    {
					return false;
				}
			}
			
			String stringBuffer = new String(byteBuffer);
			
			if(string.equals(stringBuffer))
			{
				return true;
			}
	    }
		catch (Exception e)
	    {
			return false;
		}
		
		return false;
	}
	
	private static int onReadInt()
	{
		try
	    {
	    	return Integer.reverseBytes(audioReplayRandomAccessFile.readInt());
		}
	    catch (Exception e)
	    {
			return -1;
		}
	}
	
	private static short onReadShort()
	{
		try
	    {
	    	return  Short.reverseBytes(audioReplayRandomAccessFile.readShort());
		}
	    catch (Exception e)
	    {
			return -1;
		}
	}
	
	public boolean isValidAudioReplayWavFile(Context context, String audioReplayFileAbsolutePath)
	{
		boolean isValid = false;
		
		isValid = onReadString("RIFF"); // 00 - Marks the file as a riff file
		
		if(isValid == true)
		{
			audioReplayPayloadSize = onReadInt(); // 04 - Size of the overall file
			
			if(audioReplayFormatAudio != -1)
			{
				audioReplayPayloadSize = audioReplayPayloadSize - 36;
			
				isValid = onReadString("WAVE"); // 08 - File Type Header
				
				if(isValid == true)
				{
					isValid = onReadString("fmt "); // 12 - Format chunk marker
					
					if(isValid == true)
					{
						audioReplayHeaderSize = onReadInt(); // 16 - Length of format data as listed above
						
						if(audioReplayHeaderSize != -1) 
						{
							audioReplayFormatAudio = onReadShort(); // 20 - Type of format
							
							if(audioReplayFormatAudio != -1)
							{
								audioReplayChanelConfig = onReadShort(); // 22 - Number of Channels
								
								if(audioReplayChanelConfig != -1)
								{
									audioReplaySampleRate = onReadInt(); // 24 - Sample Rate
									
									if(audioReplaySampleRate != -1)
									{
										audioReplayByteRate = onReadInt(); // 28 - ByteRate
										
										if(audioReplayByteRate != -1)
										{
											audioReplayBlockAlign = onReadShort(); // 32 - Alignment
											
											if(audioReplayBlockAlign != -1)
											{
												audioReplayNbBitsPerSample = onReadShort(); // 34 - Bits per sample
												
												if(audioReplayNbBitsPerSample != -1)
												{
													isValid = onReadString("data"); // 36 - "data" chunk header
													
													if(isValid == true)
													{
														audioReplayDataSize = onReadInt(); // 40 - Size of the data section
														
														if(audioReplayDataSize == -1)
														{
															isValid = false;
														}
													}	
												}
												else
												{
													isValid = false;
												}
											}
											else
											{
												isValid = false;
											}
										}
										else
										{
											isValid = false;
										}
									}
									else
									{
										isValid = false;
									}	
								}
								else
								{
									isValid = false;
								}	
							}
							else
							{
								isValid = false;
							}
							
						}
						else
						{
							isValid = false;
						}	
					}
				}
			}
			else
			{
				isValid = false;
			}
		}
		
		if(isValid == false)
		{
			Log.w("ReplayFileReader", "isValidAudioReplayWavFile : " + context.getString(R.string.log_replay_file_reader_file_bad_format) + " : " + audioReplayFileAbsolutePath);
			databaseManager.insertLog(context, "" + context.getString(R.string.log_replay_file_reader_file_bad_format) + " : " + audioReplayFileAbsolutePath, new Date().getTime(), 2, false);
		}
		
		return isValid;
	}
	
	public boolean onClose(Context context, String audioReplayFileAbsolutePath)
	{
		try
		{
			if (audioReplayRandomAccessFile != null)
			{
				audioReplayRandomAccessFile.close();
			}
		}
		catch (Exception e)
		{
			Log.w("ReplayFileReader", "onClose : " + context.getString(R.string.log_replay_file_reader_file_not_close) + " : " + audioReplayFileAbsolutePath + " : " + e);
			
			return false;
		}
		
		return true;	
	}
}