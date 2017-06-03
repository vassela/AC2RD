/**
 * @file DatabaseProvider.java
 * @brief Software's database provider
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

import java.util.HashMap;
import fr.vassela.acrrd.database.DatabaseDefinition.CONTACT_FILTER;
import fr.vassela.acrrd.database.DatabaseDefinition.TELEPHONE_CALL;
import fr.vassela.acrrd.database.DatabaseDefinition.TELEPHONE_LOG;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class DatabaseProvider extends ContentProvider
{
	private SQLiteDatabase database;
	private static final String DATABASE_NAME = "ACRRD.db";
	private static final int DATABASE_VERSION = 1;
	
	private static String AUTHORITIES = "fr.vassela.acrrd.provider";
	public static Uri TELEPHONE_CALL_CONTENT_URI = Uri.parse("content://" + AUTHORITIES + "/telephone.call");
	public static Uri TELEPHONE_LOG_CONTENT_URI = Uri.parse("content://" + AUTHORITIES + "/telephone.log");
	public static Uri CONTACT_FILTER_CONTENT_URI = Uri.parse("content://" + AUTHORITIES + "/contact.filter");
	
	private static final UriMatcher URI_MATCHER;
	private static final int ALL_CALL = 1;
	private static final int CALL_ID = 2;
	private static final int ALL_LOG = 3;
	private static final int LOG_ID = 4;
	private static final int ALL_FILTER = 5;
	private static final int FILTER_ID = 6;
	
	private static HashMap<String, String> TELEPHONE_CALL_MAP_PROJECTION;
	private static HashMap<String, String> TELEPHONE_LOG_MAP_PROJECTION;
	private static HashMap<String, String> CONTACT_FILTER_MAP_PROJECTION;
	
	static
	{
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

		URI_MATCHER.addURI(AUTHORITIES, "telephone.call", ALL_CALL);
		URI_MATCHER.addURI(AUTHORITIES, "telephone.call/#", CALL_ID);

		TELEPHONE_CALL_MAP_PROJECTION = new HashMap<String, String>();
		TELEPHONE_CALL_MAP_PROJECTION.put(TELEPHONE_CALL._ID, TELEPHONE_CALL._ID);
		TELEPHONE_CALL_MAP_PROJECTION.put(TELEPHONE_CALL.DIRECTION, TELEPHONE_CALL.DIRECTION);
		TELEPHONE_CALL_MAP_PROJECTION.put(TELEPHONE_CALL.NUMBER, TELEPHONE_CALL.NUMBER);
		TELEPHONE_CALL_MAP_PROJECTION.put(TELEPHONE_CALL.DOUBLE_CALL, TELEPHONE_CALL.DOUBLE_CALL);
		TELEPHONE_CALL_MAP_PROJECTION.put(TELEPHONE_CALL.DOUBLE_CALL_NUMBER, TELEPHONE_CALL.DOUBLE_CALL_NUMBER);
		TELEPHONE_CALL_MAP_PROJECTION.put(TELEPHONE_CALL.STARTING_DATE, TELEPHONE_CALL.STARTING_DATE);
		TELEPHONE_CALL_MAP_PROJECTION.put(TELEPHONE_CALL.ENDING_DATE, TELEPHONE_CALL.ENDING_DATE);
		TELEPHONE_CALL_MAP_PROJECTION.put(TELEPHONE_CALL.DURATION, TELEPHONE_CALL.DURATION);
		TELEPHONE_CALL_MAP_PROJECTION.put(TELEPHONE_CALL.RECORD_PATH, TELEPHONE_CALL.RECORD_PATH);
		TELEPHONE_CALL_MAP_PROJECTION.put(TELEPHONE_CALL.RECORD_FILENAME, TELEPHONE_CALL.RECORD_FILENAME);
		TELEPHONE_CALL_MAP_PROJECTION.put(TELEPHONE_CALL.RECORD_FORMAT, TELEPHONE_CALL.RECORD_FORMAT);
		TELEPHONE_CALL_MAP_PROJECTION.put(TELEPHONE_CALL.LOCKED, TELEPHONE_CALL.LOCKED);
		TELEPHONE_CALL_MAP_PROJECTION.put(TELEPHONE_CALL.DESCRIPTION, TELEPHONE_CALL.DESCRIPTION);
		
		URI_MATCHER.addURI(AUTHORITIES, "telephone.log", ALL_LOG);
		URI_MATCHER.addURI(AUTHORITIES, "telephone.log/#", LOG_ID);
		
		TELEPHONE_LOG_MAP_PROJECTION = new HashMap<String, String>();
		TELEPHONE_LOG_MAP_PROJECTION.put(TELEPHONE_LOG._ID, TELEPHONE_LOG._ID);
		TELEPHONE_LOG_MAP_PROJECTION.put(TELEPHONE_LOG.TEXT, TELEPHONE_LOG.TEXT);
		TELEPHONE_LOG_MAP_PROJECTION.put(TELEPHONE_LOG.DATE, TELEPHONE_LOG.DATE);
		TELEPHONE_LOG_MAP_PROJECTION.put(TELEPHONE_LOG.PRIORITY, TELEPHONE_LOG.PRIORITY);
		
		URI_MATCHER.addURI(AUTHORITIES, "contact.filter", ALL_FILTER);
		URI_MATCHER.addURI(AUTHORITIES, "contact.filter/#", FILTER_ID);
		
		CONTACT_FILTER_MAP_PROJECTION = new HashMap<String, String>();
		CONTACT_FILTER_MAP_PROJECTION.put(CONTACT_FILTER._ID, CONTACT_FILTER._ID);
		CONTACT_FILTER_MAP_PROJECTION.put(CONTACT_FILTER.NUMBER, CONTACT_FILTER.NUMBER);
		CONTACT_FILTER_MAP_PROJECTION.put(CONTACT_FILTER.RECORDABLE, CONTACT_FILTER.RECORDABLE);
	}
	
	@Override
	public boolean onCreate()
	{
		try
		{
			SQLiteOpenHelper databaseHelper = new Database(getContext(), DATABASE_NAME, null, DATABASE_VERSION);
			database = databaseHelper.getWritableDatabase();
			return (database != null) ? true : false;
		}
		catch (Exception e)
		{
			Log.e("DatabaseProvider", "onCreate : " + e);
			return false;
		}
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues initialValues)
	{
		try
		{
			long retInsert;
			ContentValues values;
	
			if (initialValues != null)
			{
				values = new ContentValues(initialValues);
			} else
			{
				values = new ContentValues();
			}
			
			switch (URI_MATCHER.match(uri))
			{
				case ALL_CALL:
					retInsert = database.insert(DatabaseDefinition.TELEPHONE_CALL_ENTITY, null, values);
					
					if (retInsert > 0)
					{
						Uri uriId = Uri.withAppendedPath(TELEPHONE_CALL_CONTENT_URI, Long.toString(retInsert));
						getContext().getContentResolver().notifyChange(uriId, null);
						return uriId;
					}
					throw new SQLException("Failed to insert row into : " + uri);
					
				case ALL_LOG:
					retInsert = database.insert(DatabaseDefinition.TELEPHONE_LOG_ENTITY, null, values);
					
					if (retInsert > 0)
					{
						Uri uriId = Uri.withAppendedPath(TELEPHONE_LOG_CONTENT_URI, Long.toString(retInsert));
						getContext().getContentResolver().notifyChange(uriId, null);
						return uriId;
					}
					throw new SQLException("Failed to insert row into : " + uri);
					
				case ALL_FILTER:
					retInsert = database.insert(DatabaseDefinition.CONTACT_FILTER_ENTITY, null, values);
					
					if (retInsert > 0)
					{
						Uri uriId = Uri.withAppendedPath(CONTACT_FILTER_CONTENT_URI, Long.toString(retInsert));
						getContext().getContentResolver().notifyChange(uriId, null);
						return uriId;
					}
					throw new SQLException("Failed to insert row into : " + uri);
					
				default:
					throw new IllegalArgumentException("Unknown URI " + uri);
			}
		}
		catch (Exception e)
		{
			Log.e("DatabaseProvider", "insert : " + e);
			return null;
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs)
	{
		int retDelete = 0;

		try
		{
			switch (URI_MATCHER.match(uri))
			{
				case ALL_CALL:
					retDelete = database.delete(DatabaseDefinition.TELEPHONE_CALL_ENTITY, selection, selectionArgs);
					break;
		
				case CALL_ID:
					String callSegment = uri.getPathSegments().get(1);
					String callWhereId = TELEPHONE_CALL._ID + "=" + callSegment + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
					retDelete = database.delete(DatabaseDefinition.TELEPHONE_CALL_ENTITY, callWhereId, selectionArgs);
					break;
					
				case ALL_LOG:
					retDelete = database.delete(DatabaseDefinition.TELEPHONE_LOG_ENTITY, selection, selectionArgs);
					break;
		
				case LOG_ID:
					String logSegment = uri.getPathSegments().get(1);
					String logWhereId = TELEPHONE_LOG._ID + "=" + logSegment + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
					retDelete = database.delete(DatabaseDefinition.TELEPHONE_LOG_ENTITY, logWhereId, selectionArgs);
					break;
		
				case ALL_FILTER:
					retDelete = database.delete(DatabaseDefinition.CONTACT_FILTER_ENTITY, selection, selectionArgs);
					break;
		
				case FILTER_ID:
					String filterSegment = uri.getPathSegments().get(1);
					String filterWhereId = CONTACT_FILTER._ID + "=" + filterSegment + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
					retDelete = database.delete(DatabaseDefinition.CONTACT_FILTER_ENTITY, filterWhereId, selectionArgs);
					break;
					
				default:
					throw new IllegalArgumentException("Unknown URI : " + uri);
			}
	
			getContext().getContentResolver().notifyChange(uri, null);
			return retDelete;
		}
		catch (Exception e)
		{
			Log.e("DatabaseProvider", "delete : " + e);
			return retDelete;
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
	{
		int retUpdate = 0;
		
		try
		{
			switch (URI_MATCHER.match(uri))
			{
				case ALL_CALL:
					retUpdate = database.update(DatabaseDefinition.TELEPHONE_CALL_ENTITY, values, selection,
							selectionArgs);
					break;
		
				case CALL_ID:
					String callSegment = uri.getPathSegments().get(1);
					String callWhereId = TELEPHONE_CALL._ID + "=" + callSegment + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
					retUpdate = database.update(DatabaseDefinition.TELEPHONE_CALL_ENTITY, values, callWhereId, selectionArgs);
					break;
					
				case ALL_LOG:
					retUpdate = database.update(DatabaseDefinition.TELEPHONE_LOG_ENTITY, values, selection,
							selectionArgs);
					break;
		
				case LOG_ID:
					String logSegment = uri.getPathSegments().get(1);
					String logWhereId = TELEPHONE_LOG._ID + "=" + logSegment + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
					retUpdate = database.update(DatabaseDefinition.TELEPHONE_LOG_ENTITY, values, logWhereId, selectionArgs);
					break;
					
				case ALL_FILTER:
					retUpdate = database.update(DatabaseDefinition.CONTACT_FILTER_ENTITY, values, selection,
							selectionArgs);
					break;
		
				case FILTER_ID:
					String filterSegment = uri.getPathSegments().get(1);
					String filterWhereId = CONTACT_FILTER._ID + "=" + filterSegment + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
					retUpdate = database.update(DatabaseDefinition.CONTACT_FILTER_ENTITY, values, filterWhereId, selectionArgs);
					break;
							
				default:
					throw new IllegalArgumentException("Unknown URI : " + uri);
			}
	
			getContext().getContentResolver().notifyChange(uri, null);
			return retUpdate;
		}
		catch (Exception e)
		{
			Log.e("DatabaseProvider", "update : " + e);
			return retUpdate;
		}
	}
	
	@Override
	public String getType(Uri uri)
	{
		try
		{
			switch (URI_MATCHER.match(uri))
			{
				case ALL_CALL:
					return "vnd.android.cursor.dir/fr.vassela.acrrd.provider.telephone.call";
		
				case CALL_ID:
					return "vnd.android.cursor.item/fr.vassela.acrrd.provider.telephone.call";
					
				case ALL_LOG:
					return "vnd.android.cursor.dir/fr.vassela.acrrd.provider.telephone.log";
		
				case LOG_ID:
					return "vnd.android.cursor.item/fr.vassela.acrrd.provider.telephone.log";
					
				case ALL_FILTER:
					return "vnd.android.cursor.dir/fr.vassela.acrrd.provider.contact.filter";
		
				case FILTER_ID:
					return "vnd.android.cursor.item/fr.vassela.acrrd.provider.contact.filter";
		
				default:
					throw new IllegalArgumentException("Unknown URI : " + uri);
			}
		}
		catch (Exception e)
		{
			Log.e("DatabaseProvider", "getType : " + e);
			return "";
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
	{
		try
		{
			SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
	
			switch (URI_MATCHER.match(uri))
			{
				case ALL_CALL:
					queryBuilder.setTables(DatabaseDefinition.TELEPHONE_CALL_ENTITY);
					queryBuilder.setProjectionMap(TELEPHONE_CALL_MAP_PROJECTION);
					break;
		
				case CALL_ID:
					queryBuilder.setTables(DatabaseDefinition.TELEPHONE_CALL_ENTITY);
					queryBuilder.appendWhere("_id=" + uri.getPathSegments().get(1));
					break;
					
				case ALL_LOG:
					queryBuilder.setTables(DatabaseDefinition.TELEPHONE_LOG_ENTITY);
					queryBuilder.setProjectionMap(TELEPHONE_LOG_MAP_PROJECTION);
					break;
		
				case LOG_ID:
					queryBuilder.setTables(DatabaseDefinition.TELEPHONE_LOG_ENTITY);
					queryBuilder.appendWhere("_id=" + uri.getPathSegments().get(1));
					break;
					
				case ALL_FILTER:
					queryBuilder.setTables(DatabaseDefinition.CONTACT_FILTER_ENTITY);
					queryBuilder.setProjectionMap(CONTACT_FILTER_MAP_PROJECTION);
					break;
		
				case FILTER_ID:
					queryBuilder.setTables(DatabaseDefinition.CONTACT_FILTER_ENTITY);
					queryBuilder.appendWhere("_id=" + uri.getPathSegments().get(1));
					break;
					
				default:
					throw new IllegalArgumentException("Unknown URI : " + uri);
			}
	
			String orderBy;
			if (TextUtils.isEmpty(sortOrder) && ((URI_MATCHER.match(uri) == ALL_CALL) || (URI_MATCHER.match(uri) == CALL_ID)))
			{
				orderBy = TELEPHONE_CALL.DEFAULT_SORT_ORDER;
			}
			else if (TextUtils.isEmpty(sortOrder) && ((URI_MATCHER.match(uri) == ALL_LOG) || (URI_MATCHER.match(uri) == LOG_ID)))
			{
				orderBy = TELEPHONE_LOG.DEFAULT_SORT_ORDER;
			}
			else if (TextUtils.isEmpty(sortOrder) && ((URI_MATCHER.match(uri) == ALL_FILTER) || (URI_MATCHER.match(uri) == FILTER_ID)))
			{
				orderBy = CONTACT_FILTER.DEFAULT_SORT_ORDER;
			}
			else
			{
				orderBy = sortOrder;
			}
	
			Cursor retQuery = queryBuilder.query(database, projection, selection, selectionArgs, null, null, orderBy);
			retQuery.setNotificationUri(getContext().getContentResolver(), uri);
			return retQuery;
		}
		catch (Exception e)
		{
			Log.e("DatabaseProvider", "query : " + e);
			return null;
		}
	}

	private static class Database extends SQLiteOpenHelper
	{
		public Database(Context context, String name, CursorFactory factory, int version)
		{
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase database)
		{
			try
			{
				database.execSQL("CREATE TABLE " + DatabaseDefinition.TELEPHONE_CALL_ENTITY + " ("
					+ TELEPHONE_CALL._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
					+ TELEPHONE_CALL.DIRECTION + " INTEGER,"
					+ TELEPHONE_CALL.NUMBER + " TEXT,"
					+ TELEPHONE_CALL.DOUBLE_CALL + " INTEGER,"
					+ TELEPHONE_CALL.DOUBLE_CALL_NUMBER + " TEXT,"
					+ TELEPHONE_CALL.STARTING_DATE + " LONG,"
					+ TELEPHONE_CALL.ENDING_DATE + " LONG,"
					+ TELEPHONE_CALL.DURATION + " LONG,"
					+ TELEPHONE_CALL.RECORD_PATH + " TEXT,"
					+ TELEPHONE_CALL.RECORD_FILENAME + " TEXT,"
					+ TELEPHONE_CALL.RECORD_FORMAT + " INTEGER,"
					+ TELEPHONE_CALL.LOCKED + " INTEGER,"
					+ TELEPHONE_CALL.DESCRIPTION + " TEXT);");
			
				database.execSQL("CREATE TABLE " + DatabaseDefinition.TELEPHONE_LOG_ENTITY + " ("
					+ TELEPHONE_LOG._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
					+ TELEPHONE_LOG.TEXT + " TEXT,"
					+ TELEPHONE_LOG.DATE + " LONG,"
					+ TELEPHONE_LOG.PRIORITY + " INTEGER);");
				
				database.execSQL("CREATE TABLE " + DatabaseDefinition.CONTACT_FILTER_ENTITY + " ("
					+ CONTACT_FILTER._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
					+ CONTACT_FILTER.NUMBER + " TEXT,"
					+ CONTACT_FILTER.RECORDABLE + " INTEGER);");
			}
			catch (Exception e)
			{
				Log.e("Database", "onCreate : " + e);
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion)
		{
			try
			{
				database.execSQL("DROP TABLE IF EXISTS " + DatabaseDefinition.TELEPHONE_CALL_ENTITY);
				database.execSQL("DROP TABLE IF EXISTS " + DatabaseDefinition.TELEPHONE_LOG_ENTITY);
				database.execSQL("DROP TABLE IF EXISTS " + DatabaseDefinition.CONTACT_FILTER_ENTITY);
				onCreate(database);
			}
			catch (Exception e)
			{
				Log.e("Database", "onUpgrade : " + e);
			}
		}
	}
}