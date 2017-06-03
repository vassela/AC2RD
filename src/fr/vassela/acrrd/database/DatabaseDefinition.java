/**
 * @file DatabaseDefinition.java
 * @brief Software's database definition
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

package fr.vassela.acrrd.database;

import android.provider.BaseColumns;

public final class DatabaseDefinition
{
	public final static String TELEPHONE_CALL_ENTITY = "telephone_call_entity";
	public final static String TELEPHONE_LOG_ENTITY = "telephone_log_entity";
	public final static String CONTACT_FILTER_ENTITY = "contact_filter_entity";
	
	public final static class TELEPHONE_CALL implements BaseColumns
	{
		public final static String DEFAULT_SORT_ORDER = "STARTING_DATE DESC";
		
		public final static String DIRECTION = "direction";
		
		public final static String NUMBER = "number";
	
		public final static String DOUBLE_CALL = "double_call";
		
		public final static String DOUBLE_CALL_NUMBER = "double_call_number";
		
		public final static String STARTING_DATE = "starting_date";
		
		public final static String ENDING_DATE = "ending_date";
		
		public final static String DURATION = "duration";
		
		public final static String RECORD_PATH = "record_path";
		
		public final static String RECORD_FILENAME = "record_filename";
		
		public final static String RECORD_FORMAT = "record_format";
		
		public final static String LOCKED = "locked";
		
		public final static String DESCRIPTION = "description";
	}
	
	public final static class TELEPHONE_LOG implements BaseColumns
	{
		public final static String DEFAULT_SORT_ORDER = "DATE DESC";

		public final static String TEXT = "text";
		
		public final static String DATE = "date";
		
		public final static String PRIORITY = "priority";
	}
	
	public final static class CONTACT_FILTER implements BaseColumns
	{
		public final static String DEFAULT_SORT_ORDER = "RECORDABLE DESC";
		
		public final static String NUMBER = "number";
		
		public final static String RECORDABLE = "recordable";
	}
}