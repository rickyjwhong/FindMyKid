package com.rickster.findmykid.model;

import java.util.ArrayList;
import java.util.Date;

import com.rickster.findmykid.R;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class OfflineData extends SQLiteOpenHelper {
	
	private static final String TAG = "OfflineData";
	private Context mContext;
	private SharedPreferences mPrefs;
	
	private static final String DB_NAME 					= "offline.sqlite";
	private static final int DB_VERSION 					= 1;		
	
	private static final String TABLE_USER					= "user";
	private static final String USER_ID						= "user_id";
	private static final String USER_NAME					= "user_name";
	private static final String USER_CODE					= "user_code";
	private static final String USER_GCM					= "user_gcm";
	private static final String USER_PHONENUMBER			= "user_phonenumber";
	private static final String USER_UPDATED				= "user_updated";
	
	private static final String TABLE_CONNECTION 			= "connection";
	private static final String CONNECTION_ID				= "connection_id";
	private static final String CONNECTION_TRACKERID		= "connection_trackerid";
	private static final String CONNECTION_TRACKINGID		= "connection_trackingid";
	private static final String CONNECTION_UPDATED			= "connection_updated";
	
	private static final String TABLE_REQUEST				= "request";
	private static final String REQUEST_ID					= "request_id";
	private static final String REQUEST_TRACKERID			= "request_trackerid";
	private static final String REQUEST_TRACKINGID			= "request_trackingid";
	private static final String REQUEST_SOLVED				= "request_solved";
	private static final String REQUEST_TIME				= "request_time";
	private static final String REQUEST_UPDATED				= "request_updated";
	
	private static final String TABLE_LOCATION 				= "location";
	private static final String LOCATION_ID					= "location_id";
	private static final String LOCATION_LAT				= "location_lat";
	private static final String LOCATION_LON				= "location_lon";
	private static final String LOCATION_ALT				= "location_alt";
	private static final String LOCATION_TIME				= "location_time";
	
	private static final String SMS_SENT 					= "sms_sent";
	private static final String SMS_DELIVERED				= "sms_delivered";
	
	public OfflineData(Context c){
		super(c, DB_NAME, null, DB_VERSION);
		mContext = c;
		mPrefs = c.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		String query = 
					" CREATE TABLE IF NOT EXISTS " + TABLE_USER
					+ " ( " 
					+ USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " 
					+ USER_NAME	+ " VARCHAR(100), "
					+ USER_CODE	+ " VARCHAR(100), "
					+ USER_GCM + " VARCHAR(200), " 
					+ USER_PHONENUMBER + " VARCHAR(100), "
					+ USER_UPDATED + " INTEGER DEFAULT 0"
					+ " ) ";
		db.execSQL(query);
		
		query = 
					" CREATE TABLE IF NOT EXISTS " + TABLE_CONNECTION
					+ " ( " 
					+ CONNECTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ CONNECTION_TRACKERID	+ " INTEGER, "
					+ CONNECTION_TRACKINGID	+ " INTEGER, "
					+ CONNECTION_UPDATED + " INTEGER DEFAULT 0 "
					+ " ) " ;
		db.execSQL(query);
		
		query = 
					" CREATE TABLE IF NOT EXISTS " + TABLE_REQUEST
					+ " ( " 
					+ REQUEST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ REQUEST_TRACKERID + " INTEGER, "
					+ REQUEST_TRACKINGID + " INTEGER, "
					+ REQUEST_SOLVED + " INTEGER DEFAULT 0, " 
					+ REQUEST_TIME + " INTEGER, "
					+ REQUEST_UPDATED + " INTEGER DEFAULT 0"
					+ " ) ";
		db.execSQL(query);
		
		query = 
					" CREATE TABLE IF NOT EXISTS " + TABLE_LOCATION
					+ " ( "
					+ LOCATION_ID 	+ "INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ LOCATION_LAT 	+ " REAL, "
					+ LOCATION_LON	+ " REAL, "
					+ LOCATION_ALT 	+ " REAL, "
					+ LOCATION_TIME + " INTEGER "
					+ " ) ";
		db.execSQL(query);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		Log.i(TAG, "Upgrade has been called from " + oldVersion + " to " + newVersion);
	}
	
	public boolean updateData(){
		
		//grab everything and see if it is inconsistnetn with server...
		//SQLite will always triumph the priority elvel
		//have a offline version number and have a online version number... which ever is greater, use it to update the other
		
		return true;
	}
	
	public void respondLocation(Location loc){
		//send sms back to the original phone number
		//<location requestid="233" lat="123.1759" lon="192.2299" alt="192.9999" time="99999999999" address="4063 Sansom St, Philadelphia, PA 19104 United States" />
		
		
	}
	
	public User getCurrentUser(){
		Log.i(TAG, "Retrieving current User");
		long id = mPrefs.getLong(Constants.PREF_USER_ID, 0);
		String name = mPrefs.getString(Constants.PREF_USER_NAME, "");
		String code = mPrefs.getString(Constants.PREF_USER_CODE, "");
		String gcm = mPrefs.getString(Constants.PREF_USER_GCM, "");
		String phone_number = mPrefs.getString(Constants.PREF_USER_PHONE_NUMBER, "");		 
		return new User(id, name, gcm, phone_number, code);
	}
	
	public ArrayList<Location> requestLocation(User targetUser){
		if(hasPermission(targetUser)){
			//has permission ,send text
			//<request id="5" from="xxjsndsl">
			long requestId = insertRequest(targetUser);
			String message = "<request id=\"" + requestId + "\" from=\"" + getCurrentUser().getCode() + "\">";
			sendSMS(targetUser.getPhoneNumber(), message);
		}else{
			
		}	
		ArrayList<Location> locations = new ArrayList<Location>();
		return locations;
	}
	
	private long insertRequest(User targetUser){
		ContentValues cv = new ContentValues();
		cv.put(REQUEST_TRACKERID, getCurrentUser().getId());
		cv.put(REQUEST_TRACKINGID, targetUser.getId());
		cv.put(REQUEST_TIME, System.currentTimeMillis());
		return getWritableDatabase().insert(TABLE_REQUEST, null, cv);
	}
	
	private void showToast(int msg){
		String message = mContext.getResources().getString(msg);
		Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
	}
	
	private User getUser(String code){
		String query = "SELECT * FROM " + TABLE_USER + " WHERE " + USER_CODE + " =? ";
		Cursor wrapped = getReadableDatabase().rawQuery(query, new String[]{String.valueOf(code)});
		UserCursor cursor = new UserCursor(wrapped);
		User user = cursor.getUser();
		cursor.close();
		return user;
	}
	
	private void sendSMS(String phoneNumber, String message){
		
		PendingIntent sentPI = PendingIntent.getBroadcast(mContext, 0, new Intent(SMS_SENT), 0);
		PendingIntent deliveredPI = PendingIntent.getBroadcast(mContext, 0, new Intent(SMS_DELIVERED), 0);
		
		mContext.registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				switch(getResultCode()){
					case Activity.RESULT_OK:
						showToast(R.string.sms_sent);
						break;
					case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
						showToast(R.string.sms_failure);
						break;
					case SmsManager.RESULT_ERROR_NO_SERVICE:
						showToast(R.string.sms_no_service);
						break;
					case SmsManager.RESULT_ERROR_NULL_PDU:
						showToast(R.string.sms_null_pdu);
						break;
					case SmsManager.RESULT_ERROR_RADIO_OFF:
						showToast(R.string.sms_radio_off);
						break;
					}
				}
			}, new IntentFilter(SMS_SENT));
					//---when the SMS has been delivered---
		mContext.registerReceiver(new BroadcastReceiver(){
			@Override
			public void onReceive(Context arg0, Intent arg1) {
			switch (getResultCode()){
				case Activity.RESULT_OK:
					showToast(R.string.sms_delivered);
					break;
				case Activity.RESULT_CANCELED:
					showToast(R.string.sms_not_delivered);
					break;
					}
				}
			}, new IntentFilter(SMS_DELIVERED));
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
	}
	
	public ArrayList<User> getTrackers(User user){
		ArrayList<User> users = new ArrayList<User>();
		String query = "SELECT * FROM " + TABLE_CONNECTION + " WHERE " + CONNECTION_TRACKINGID + " =? ";
		Cursor wrapped = getReadableDatabase().rawQuery(query, new String[]{ String.valueOf(user.getId())});
		ConnectionCursor cursor = new ConnectionCursor(wrapped);
		if(cursor.moveToFirst()){
			do{
				users.add(cursor.getUser());				
			}while(cursor.moveToNext());
		}
		cursor.close();
		return users;
	}
	
	public ArrayList<User> getTrackings(User user){
		ArrayList<User> users = new ArrayList<User>();
		String query = "SELECT * FROM " + TABLE_CONNECTION + " WHERE " + CONNECTION_TRACKERID + " =? ";
		Cursor wrapped = getReadableDatabase().rawQuery(query, new String[]{ String.valueOf(user.getId())});
		ConnectionCursor cursor = new ConnectionCursor(wrapped);
		if(cursor.moveToFirst()){
			do{
				users.add(cursor.getUser());				
			}while(cursor.moveToNext());
		}
		cursor.close();
		return users;
	}
	
	public Connection getConnection(long trackerId, long trackingId){
		String query = " SELECT * FROM " + TABLE_CONNECTION + " WHERE " + CONNECTION_TRACKERID + " =? AND " + CONNECTION_TRACKINGID + " =? ";
		Cursor wrapped = getReadableDatabase().rawQuery(query, new String[]{String.valueOf(trackerId),String.valueOf(trackingId)});
		ConnectionCursor cursor = new ConnectionCursor(wrapped);
		Connection connection = cursor.getConnection();
		cursor.close();
		return connection;
	}
	
	public boolean hasPermission(User targetUser){
		Connection connection = getConnection(getCurrentUser().getId(), targetUser.getId());
		return (connection != null);
	}
	
	//NOT POSSIBLE IN OFFLINE DATA FOR NOW	
	public boolean deleteConnection(long trackerId, long trackingId){	
		return false;		
	}
	public ArrayList<Connection> connectUser(String currentUserCode, String targetingUserCode){
		return null;
	}
	public ArrayList<User> registerUser(User user){
		return null;
	}
	public ArrayList<Location> getLocation(long id){
		return null;
	}
	public boolean checkUserExist(String userCode){
		return true;
	}	
	public void sendFeedback(String message){
		return;
	}	
	
	/////////////////////////////////////////////////
	
	public class ConnectionCursor extends CursorWrapper{

		public ConnectionCursor(Cursor cursor) {
			super(cursor);
		}
		public Connection getConnection(){
			if(isBeforeFirst() || isAfterLast()) return null;
			long id = getLong(getColumnIndex(CONNECTION_ID));
			long trackerid = getLong(getColumnIndex(CONNECTION_TRACKERID));
			long trackingid = getLong(getColumnIndex(CONNECTION_TRACKINGID));
			Connection connection = new Connection(id, trackerid, trackingid);
			Log.i(TAG, "Returning Connection: " + connection.getId() + " | " + connection.getTracker());
			return connection;
		}
		public User getUser(){
			Connection connection = getConnection();
			String query = " SELECT * FROM " + TABLE_USER + " WHERE " + USER_ID + " =? " ;
			Cursor wrapped = getReadableDatabase().rawQuery(query, new String[] {String.valueOf(connection.getTracker())});
			UserCursor cursor = new UserCursor(wrapped);
			User user = cursor.getUser();
			cursor.close();			
			return user;
		}		
	}
	
	public class LocationCursor extends CursorWrapper{

		public LocationCursor(Cursor cursor) {
			super(cursor);
		}
		public Location getLocation(){
			if(isBeforeFirst() || isAfterLast()) return null;
			long id = getLong(getColumnIndex(LOCATION_ID));
			double lat = getDouble(getColumnIndex(LOCATION_LAT));
			double lon = getDouble(getColumnIndex(LOCATION_LON));
			double alt = getDouble(getColumnIndex(LOCATION_ALT));
			long time = getLong(getColumnIndex(LOCATION_TIME));
			Location location = new Location();
			location.setId(id);
			location.setLatitude(lat);
			location.setLongitude(lon);
			location.setAltitude(alt);
			location.setTime(time);
			return location;
		}		
	}
	
	public class UserCursor extends CursorWrapper{
		public UserCursor(Cursor cursor){
			super(cursor);
		}
		public User getUser(){
			if(isBeforeFirst() || isAfterLast()) return null;
			User user = new User();
			user.setId(getLong(getColumnIndex(USER_ID)));
			user.setCode(getString(getColumnIndex(USER_CODE)));
			user.setGCM(getString(getColumnIndex(USER_GCM)));
			user.setName(getString(getColumnIndex(USER_NAME)));
			user.setPhoneNumber(getString(getColumnIndex(USER_PHONENUMBER)));
			return user;			
		}		
	}
	
}
