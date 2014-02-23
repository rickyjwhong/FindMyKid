package com.rickster.findmykid.model;

import java.util.ArrayList;
import java.util.Date;

import com.rickster.findmykid.R;
import com.rickster.findmykid.controller.OnlineLab;

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
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class OfflineData extends SQLiteOpenHelper {
	
	private static final String TAG = "OfflineData";
	private Context mContext;
	private SharedPreferences mPrefs;
	
	private static final String DB_NAME 					= "offline.db";
	private static final int DB_VERSION 					= 3;		
	
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
	private static final String LOCATION_REQUEST_ID			= "location_request_id";
	private static final String LOCATION_ADDRESS			= "location_address";
	
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
					+ USER_ID + " INTEGER PRIMARY KEY, " 
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
					+ CONNECTION_ID + " INTEGER PRIMARY KEY, "
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
					+ LOCATION_ID 	+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ LOCATION_LAT 	+ " REAL, "
					+ LOCATION_LON	+ " REAL, "
					+ LOCATION_ALT 	+ " REAL, "
					+ LOCATION_TIME + " INTEGER, "
					+ LOCATION_REQUEST_ID	+ " INTEGER, "
					+ LOCATION_ADDRESS	+ " VARCHAR(250) "
					+ " ) ";
		db.execSQL(query);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		Log.i(TAG, "Upgrade has been called from " + oldVersion + " to " + newVersion);
		String query = "ALTER TABLE " + TABLE_LOCATION + " ADD COLUMN " + LOCATION_ADDRESS + " VARCHAR(250);";
		db.execSQL(query);
	}
	
	///////////////////////////////////////////////////////////////////////////
	//update
	///////////////////////////////////////////////////////////////////////////	
	
	public void updateLocalUsers(ArrayList<User> newUsers){
		Log.i(TAG, "Updating Data: " + newUsers.size() + " users to be udpated");
		deleteUsers(); // delete all users
		addUsers(newUsers);
	}
	
	public void deleteUsers(){
		Log.i(TAG, "Deleting all users on local server");
		getWritableDatabase().delete(TABLE_USER, null, null);
	}
	
	public void addUsers(ArrayList<User> newUsers){
		for(User user : newUsers){
			Log.i(TAG, "Adding user to local server: " + user.getName());
			long id = insertUser(user);
			Log.i(TAG, "User added with id: " + id);
		}
	}
	
	private long insertUser(User user){
		ContentValues cv = new ContentValues();
		cv.put(USER_ID, user.getId());
		cv.put(USER_NAME, user.getName());
		cv.put(USER_CODE, user.getCode());
		cv.put(USER_GCM, user.getGCM());
		cv.put(USER_PHONENUMBER, user.getPhoneNumber());
		cv.put(USER_UPDATED, 1);
		Log.i(TAG, "ID: " + user.getId() + " Name: " + user.getName() + " code: " + user.getCode());
		return getWritableDatabase().insert(TABLE_USER, null, cv);
	}
	
	public void updateLocalConnections(ArrayList<Connection> newConnections){
		Log.i(TAG, "Updating Data: " + newConnections.size() + " connections to be updated");
		deleteConnections(); 		//delete all connections
		addConnections(newConnections);		
	}
	
	public void deleteConnections(){
		Log.i(TAG, "Deleting all connections on local server");
		getWritableDatabase().delete(TABLE_CONNECTION, null, null);
	}
	
	public void addConnections(ArrayList<Connection> newConnections){
		for(Connection connection : newConnections){
			Log.i(TAG, "Adding connection to Local Server: " + connection.getId());
			
			long id = insertConnection(connection);
			Log.i(TAG, "Connction ID " + id);
		}
	}
	
	public long insertLocation(Location location){
		ContentValues cv = new ContentValues();
		cv.put(LOCATION_LAT, String.valueOf(location.getLatitude()));
		cv.put(LOCATION_LON, String.valueOf(location.getLongitude()));
		cv.put(LOCATION_ALT, String.valueOf(location.getAltitude()));
		cv.put(LOCATION_TIME, String.valueOf(location.getTime()));
		cv.put(LOCATION_REQUEST_ID, String.valueOf(location.getRequestId()));
		cv.put(LOCATION_ADDRESS, String.valueOf(location.getAddress()));
		return getWritableDatabase().insert(TABLE_LOCATION, null, cv);
	}
	
	public Location getLocation(long id){
		Location location = new Location();
		String query = "SELECT * FROM " + TABLE_LOCATION + " WHERE " + LOCATION_ID	+ " =? LIMIT 1";
		Cursor wrapped = getReadableDatabase().rawQuery(query, new String[] { String.valueOf(id) });
		LocationCursor cursor = new LocationCursor(wrapped);
		if(cursor.moveToFirst()){
			do{
				location = cursor.getLocation();
			}while(cursor.moveToNext());
		}
		cursor.close();
		Log.i(TAG, "Got Location: " + location.getAddress());
		return location;
	}
	
	public long insertConnection(Connection connection){
		Log.i(TAG, "Adding connection: " + connection.getTracker() + " and " + connection.getTracking());
		ContentValues cv = new ContentValues();
		cv.put(CONNECTION_ID, connection.getId());
		cv.put(CONNECTION_TRACKERID, String.valueOf(connection.getTracker()));
		cv.put(CONNECTION_TRACKINGID, String.valueOf(connection.getTracking()));
		cv.put(CONNECTION_UPDATED, String.valueOf(1));
		return getWritableDatabase().insert(TABLE_CONNECTION, null, cv);
	}
	
	public ArrayList<Connection> getConnections(){
		ArrayList<Connection> connections = new ArrayList<Connection>();
		String query = "SELECT * FROM " + TABLE_CONNECTION;
		Cursor wrapped = getReadableDatabase().rawQuery(query, null);
		ConnectionCursor cursor = new ConnectionCursor(wrapped);
		if(cursor.moveToFirst()){
			do{
				connections.add(cursor.getConnection());
			}while(cursor.moveToNext());
		}
		cursor.close();
		Log.i(TAG, "Got " + connections.size() + " From the Local Server");
		return connections;
	}	
	
	///////////////////////////////////////////////////////////////////////////
	
	public User getCurrentUser(){
		Log.i(TAG, "Retrieving current User");
		long id = mPrefs.getLong(Constants.PREF_USER_ID, 0);
		String name = mPrefs.getString(Constants.PREF_USER_NAME, "");
		String code = mPrefs.getString(Constants.PREF_USER_CODE, "");
		String gcm = mPrefs.getString(Constants.PREF_USER_GCM, "");
		String phone_number = mPrefs.getString(Constants.PREF_USER_PHONE_NUMBER, "");		 
		return new User(id, name, gcm, phone_number, code);
	}
	
	public void respondLocation(User currentUser, User targetUser, Location location){		
		String message = infoWrap(Constants.RESPONSE_TEXT) + respondWrap(location);
		Log.i(TAG, "Sending Response via SMS: " + message);
		sendSMS(currentUser, targetUser, message);
	}
	
	public void requestLocation(User currentUser, User targetUser){
		long requestId = insertRequest(targetUser);
		//<info key="app-specific code" type="request" /><data id="33" sender="xxxx" />
		String message = infoWrap(Constants.REQUEST_TEXT) + requestWrap(requestId, currentUser.getCode());		
		Log.i(TAG, "Sending Request via SMS: " + message);
		sendSMS(currentUser, targetUser, message);
	}
	
	private String infoWrap(String type){
		String str = "<" + Constants.INFO_TEXT
					+ " " + Constants.INFO_KEY 	+ "=" + quoteWrap(Constants.TEXT_CODE)
					+ " " + Constants.INFO_TYPE + "=" + quoteWrap(type)
					+ " /> ";
		return str;
	}
	
	private String requestWrap(long id, String senderCode){
		String str = "<" + Constants.DATA_TEXT
					+ " " + Constants.DATA_ID + "=" + quoteWrap(String.valueOf(id))
					+ " " + Constants.DATA_SENDER_CODE + "=" + quoteWrap(senderCode)
					+ " />";
		return str;
	}
	
	private String respondWrap(Location location){
		String str = "<" + Constants.DATA_TEXT
					+ " " + Constants.DATA_LAT + "=" + quoteWrap(String.valueOf(location.getLatitude()))
					+ " " + Constants.DATA_LON + "=" + quoteWrap(String.valueOf(location.getLongitude()))
					+ " " + Constants.DATA_ALT + "=" + quoteWrap(String.valueOf(location.getAltitude()))
					+ " " + Constants.DATA_TIME + "=" + quoteWrap(String.valueOf(location.getTime()))
					+ " " + Constants.DATA_REQUEST_ID + "=" + quoteWrap(String.valueOf(location.getRequestId()))
					+ " />";
		return str;
	}
	
	private String quoteWrap(String value){
		return "\"" + value + "\"";
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
	
	public User getUser(String code){
		Log.i(TAG, "Retrieving User with code: " + code);
		User user = new User();
		String query = "SELECT * FROM " + TABLE_USER + " WHERE " + USER_CODE + " =? ";
		Cursor wrapped = getReadableDatabase().rawQuery(query, new String[]{String.valueOf(code)});
		UserCursor cursor = new UserCursor(wrapped);
		if(cursor.moveToFirst()){
			do{
				user = cursor.getUser();
			}while(cursor.moveToNext());
		}
		cursor.close();
		return user;
	}
	
	public void sendSMS(User sender, User receiver, String message){		
		PendingIntent sentPI = PendingIntent.getBroadcast(mContext, 0, new Intent(Constants.TEXT_SMS_SENT), 0);
		PendingIntent deliveredPI = PendingIntent.getBroadcast(mContext, 0, new Intent(Constants.TEXT_SMS_DELIVERED), 0);
		
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
			}, new IntentFilter(Constants.TEXT_SMS_SENT));
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
			}, new IntentFilter(Constants.TEXT_SMS_DELIVERED));		
		Log.i(TAG, "Sending Message to " + receiver.getName() + " at " + receiver.getPhoneNumber() + " | " + message);
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(receiver.getPhoneNumber(), null, message, sentPI, deliveredPI);
	}
	
	public ArrayList<User> getTrackers(User user){		
		ArrayList<User> users = new ArrayList<User>();
		String query = "SELECT * FROM " + TABLE_CONNECTION + " WHERE " + CONNECTION_TRACKINGID + " =? ";
		Cursor wrapped = getReadableDatabase().rawQuery(query, new String[]{ String.valueOf(user.getId())});
		ConnectionCursor cursor = new ConnectionCursor(wrapped);
		Log.i(TAG, "Tracker CursorSize: " + cursor.getCount());
		if(cursor.moveToFirst()){
			do{
				User u = getTrackerFromConnection(cursor.getConnection());
				Log.i(TAG, "Tracker retrieved: " + u.getName());
				users.add(u);	
			}while(cursor.moveToNext());
		}
		Log.i(TAG, "Retrieving Trackers: " + users.size());
		cursor.close();
		return users;
	}
	
	
	public ArrayList<User> getTrackings(User user){
		ArrayList<User> users = new ArrayList<User>();
		String query = "SELECT * FROM " + TABLE_CONNECTION + " WHERE " + CONNECTION_TRACKERID + " =? ";
		Log.i(TAG, query);
		Cursor wrapped = getReadableDatabase().rawQuery(query, new String[]{ String.valueOf(user.getId())});
		ConnectionCursor cursor = new ConnectionCursor(wrapped);
		if(cursor.moveToFirst()){
			do{
				User u = getTrackingFromConnection(cursor.getConnection());
				Log.i(TAG, "Tracking retrieved: " + u.getName());
				users.add(u);								
			}while(cursor.moveToNext());
		}
		Log.i(TAG, "Retrieving Trackings: " + users.size());
		cursor.close();
		return users;
	}
	
	public User getTrackingFromConnection(Connection connection){
		User user = new User();
		Log.i(TAG, "Got connection: " + connection.getTracker() + " | " + connection.getTracking());
		String query = " SELECT * FROM " + TABLE_USER + " WHERE " + USER_ID + " =? " ;
		Cursor wrapped = getReadableDatabase().rawQuery(query, new String[] {String.valueOf(connection.getTracking())});
		UserCursor cursor = new UserCursor(wrapped);
		if(cursor.moveToFirst()){
			do{
				user = cursor.getUser();
			}while(cursor.moveToNext());
		}		
		cursor.close();			
		return user;
	}
	
	public User getTrackerFromConnection(Connection connection){
		User user = new User();
		Log.i(TAG, "Got connection: " + connection.getTracker() + " | " + connection.getTracking());
		String query = " SELECT * FROM " + TABLE_USER + " WHERE " + USER_ID + " =? " ;
		Cursor wrapped = getReadableDatabase().rawQuery(query, new String[] {String.valueOf(connection.getTracker())});
		UserCursor cursor = new UserCursor(wrapped);
		if(cursor.moveToFirst()){
			do{
				user = cursor.getUser();
			}while(cursor.moveToNext());
		}
		cursor.close();			
		return user;
	}
	
	public Connection getConnection(long trackerId, long trackingId){
		Connection connection = new Connection();
		Log.i(TAG, "Checking Permission from Local Server between: " + trackerId + " | " + trackingId);
		String query = " SELECT * FROM " + TABLE_CONNECTION + " WHERE " + CONNECTION_TRACKERID + " =? AND " + CONNECTION_TRACKINGID + " =? ";
		Cursor wrapped = getReadableDatabase().rawQuery(query, new String[]{String.valueOf(trackerId),String.valueOf(trackingId)});
		ConnectionCursor cursor = new ConnectionCursor(wrapped);
		if(cursor.moveToFirst()){
			do{
				Log.i(TAG, "Got Connection for Permission: " + connection.getId() + " | " + connection.getTracker() + " | " + connection.getTracking());
				connection = cursor.getConnection();
			}while(cursor.moveToNext());
		}
		cursor.close();
		return connection;
	}
	
	public boolean hasPermission(User currentUser, User targetUser){
		Connection connection = getConnection(currentUser.getId(), targetUser.getId());
		return (connection.getId() > 0);
	}
	
	public Location getCurrentLocation(String requestId){		
		LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
		android.location.Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		
		Location location = new Location();
		location.setLatitude(loc.getLatitude());
		location.setLongitude(loc.getLongitude());
		location.setAltitude(loc.getAltitude());
		location.setTime(loc.getTime());
		location.setRequestId(Long.valueOf(requestId));
		return location;
		//get rid of the location updates after response has been sent back
	}
	
	private final android.location.LocationListener mLocationListener = new android.location.LocationListener() {
		@Override
		public void onLocationChanged(android.location.Location arg0) {			
		}
		@Override
		public void onProviderDisabled(String provider) {			
		}
		@Override
		public void onProviderEnabled(String provider) {			
		}
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {			
		}
	};
	
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
			Log.i(TAG, "Returning Connection: " + connection.getTracker() + " | " + connection.getTracking());
			return connection;
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
			String address = getString(getColumnIndex(LOCATION_ADDRESS));
			Location location = new Location();
			location.setId(id);
			location.setLatitude(lat);
			location.setLongitude(lon);
			location.setAltitude(alt);
			location.setTime(time);
			location.setAddress(address);
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
			Log.i(TAG, "Got user: " + user.getName());
			user.setId(getLong(getColumnIndex(USER_ID)));
			user.setCode(getString(getColumnIndex(USER_CODE)));
			user.setGCM(getString(getColumnIndex(USER_GCM)));
			user.setName(getString(getColumnIndex(USER_NAME)));
			user.setPhoneNumber(getString(getColumnIndex(USER_PHONENUMBER)));
			return user;			
		}		
	}
	
}
