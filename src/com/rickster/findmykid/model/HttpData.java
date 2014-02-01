package com.rickster.findmykid.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;


import android.util.Log;
import android.widget.Toast;

public class HttpData {
	
	private static final String TAG 						= "HttpData";
	
	public static final String ENDPOINT 					= "http://acebuff.com/encuentra.php";	
	public static final String PERMISSION 					= "hi";
	
	public static final String PARAM_PERMISSION 			= "permission";
	public static final String PARAM_METHOD 				= "method";	
	public static final String PARAM_USERID 				= "user_id";
	public static final String PARAM_ID						= "id";
	public static final String PARAM_NAME 					= "name";
	public static final String PARAM_GCM 					= "gcm";
	public static final String PARAM_PHONE_NUMBER 			= "phonenumber";
	public static final String PARAM_CODE					= "code";
	public static final String PARAM_TRACKER_CODE 			= "tracker_code";
	public static final String PARAM_TRACKING_CODE 			= "tracking_code";
	public static final String PARAM_TRACKER_ID				= "tracker_id";
	public static final String PARAM_TRACKING_ID			= "tracking_id";
	public static final String PARAM_CONNECTION_ID 			= "connection_id";	
	public static final String PARAM_LATITUDE				= "latitude";
	public static final String PARAM_LONGITUDE				= "longitude";
	public static final String PARAM_ALTITUDE				= "altitude";
	public static final String PARAM_TIME					= "time";
	public static final String PARAM_REQUEST_ID				= "request_id";
	public static final String PARAM_ADDRESS				= "address";
	public static final String PARAM_LOCATION_ID			= "location_id";
	public static final String PARAM_MESSAGE				= "message";
	
	
	public static final String METHOD_REGISTER 				= "register";
	public static final String METHOD_REQUEST_TRACKING 		= "request_tracking";
	public static final String METHOD_LOCATION_RESPONSE		= "location_response";
	public static final String METHOD_TRACKERS 				= "get_trackers";
	public static final String METHOD_TRACKING 				= "get_tracking";
	public static final String METHOD_ADD_CONNECTION 		= "add_connection";
	public static final String METHOD_DELETE_CONNECTION 	= "delete_connection";
	public static final String METHOD_USER_EXISTS			= "user_exists";
	public static final String METHOD_GET_LOCATION			= "get_location";
	public static final String METHOD_IS_CONNECTED			= "is_connected";
	public static final String METHOD_FEEDBACK				= "feedback";
	
	public static final String XML_USER 					= "user";
	public static final String XML_ID 						= "id";
	public static final String XML_NAME 					= "name";
	public static final String XML_CODE 					= "code";
	public static final String XML_GCM 						= "gcm";
	public static final String XML_PHONE_NUMBER				= "phonenumber";
	public static final String XML_CONNECTION 				= "connection";
	public static final String XML_TRACKER 					= "tracker";
	public static final String XML_TRACKING 				= "tracking";
	public static final String XML_LOCATION					= "location";
	public static final String XML_LATITUDE					= "latitude";
	public static final String XML_LONGITUDE				= "longitude";
	public static final String XML_ALTITUDE					= "altitude";
	public static final String XML_TIME						= "time";
	public static final String XML_REQUEST_ID 				= "request_id";
	public static final String XML_SOLVED					= "solved";
	public static final String XML_ADDRESS					= "address";
	
	
	public static final String RESPONSE_SUCCESS 			= "success";
	
	public static byte[] postData(ArrayList<BasicNameValuePair> inputs) {
	    // Create a new HttpClient and Post Header
		Log.i(TAG, "Received Post Request");
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost(ENDPOINT);
	    try {
	        // Add your data
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	        nameValuePairs.add(new BasicNameValuePair(PARAM_PERMISSION, PERMISSION));	 
	        for(BasicNameValuePair vp : inputs)
	        	nameValuePairs.add(vp);
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	        // Execute HTTP Post Request
	        HttpResponse response = httpclient.execute(httppost);
	        HttpEntity resEntity = response.getEntity();
	        return EntityUtils.toByteArray(resEntity);
	    } catch (ClientProtocolException e) {
	        // TODO Auto-generated catch block
	    	e.printStackTrace();
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	    	e.printStackTrace();
	    }
	    return null;
	} 	
	
	public static String postDataString(ArrayList<BasicNameValuePair> inputs){
		return new String(postData(inputs));
	}
	
	public static byte[] getURLBytes(String urlString) throws IOException{
		URL url = new URL(urlString);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		try	{
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			InputStream in = connection.getInputStream();			
			if(connection.getResponseCode() != HttpURLConnection.HTTP_OK) return null;			
			int bytesRead = 0;
			byte[] buffer = new byte[1024];
			while((bytesRead = in.read(buffer)) > 0)
				os.write(buffer, 0, bytesRead);
			os.close();
			return os.toByteArray();			
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getURL(String url) throws IOException{
		return new String(getURLBytes(url));
	}
	
	public static void respondLocation(Location loc){
		ArrayList<BasicNameValuePair> vp = new ArrayList<BasicNameValuePair>();
		vp.add(new BasicNameValuePair(PARAM_METHOD, METHOD_LOCATION_RESPONSE));
		vp.add(new BasicNameValuePair(PARAM_LATITUDE, String.valueOf(loc.getLatitude())));
		vp.add(new BasicNameValuePair(PARAM_LONGITUDE, String.valueOf(loc.getLongitude())));
		vp.add(new BasicNameValuePair(PARAM_ALTITUDE, String.valueOf(loc.getAltitude())));
		vp.add(new BasicNameValuePair(PARAM_TIME, String.valueOf(loc.getTime())));
		vp.add(new BasicNameValuePair(PARAM_REQUEST_ID, String.valueOf(loc.getRequestId())));
		vp.add(new BasicNameValuePair(PARAM_ADDRESS, loc.getAddress()));
		String response = postDataString(vp);
		Log.i(TAG, "Response: " + response);
	}
	
	public static ArrayList<User> registerUser(User user){
		ArrayList<BasicNameValuePair> vp = new ArrayList<BasicNameValuePair>();
		vp.add(new BasicNameValuePair(PARAM_METHOD, METHOD_REGISTER));
		vp.add(new BasicNameValuePair(PARAM_NAME, user.getName()));
		vp.add(new BasicNameValuePair(PARAM_GCM, user.getGCM()));
		vp.add(new BasicNameValuePair(PARAM_PHONE_NUMBER, user.getPhoneNumber()));
		return new HttpParse<User>(XML_USER).download(postDataString(vp));
	}
	
	public static ArrayList<Location> requestLocation(User currentUser , User targetUser){
		ArrayList<BasicNameValuePair> vp = new ArrayList<BasicNameValuePair>();
		vp.add(new BasicNameValuePair(PARAM_METHOD, METHOD_REQUEST_TRACKING));
		vp.add(new BasicNameValuePair(PARAM_TRACKER_ID, String.valueOf(currentUser.getId())));
		vp.add(new BasicNameValuePair(PARAM_TRACKING_ID, String.valueOf(targetUser.getId())));
		return new HttpParse<Location>(XML_LOCATION).download(postDataString(vp));
	}
	
	public static ArrayList<Location> getLocation(long id){
		ArrayList<BasicNameValuePair> vp = new ArrayList<BasicNameValuePair>();
		vp.add(new BasicNameValuePair(PARAM_METHOD, METHOD_GET_LOCATION));
		vp.add(new BasicNameValuePair(PARAM_LOCATION_ID , String.valueOf(id)));
		return new HttpParse<Location>(XML_LOCATION).download(postDataString(vp));
	}
	
	public static ArrayList<User> getTrackers(User user){
		ArrayList<BasicNameValuePair> vp = new ArrayList<BasicNameValuePair>();
		vp.add(new BasicNameValuePair(PARAM_METHOD, METHOD_TRACKERS));
		vp.add(new BasicNameValuePair(PARAM_USERID, String.valueOf(user.getId())));			
		return new HttpParse<User>(XML_USER).download(postDataString(vp));
	}
	
	public static ArrayList<User> getTrackings(User user){
		ArrayList<BasicNameValuePair> vp = new ArrayList<BasicNameValuePair>();
		vp.add(new BasicNameValuePair(PARAM_METHOD, METHOD_TRACKING));
		vp.add(new BasicNameValuePair(PARAM_USERID, String.valueOf(user.getId())));			
		return new HttpParse<User>(XML_USER).download(postDataString(vp));
	}
	
	public static ArrayList<Connection> connectUser(String currentUserCode, String targetingUserCode){
		ArrayList<BasicNameValuePair> vp = new ArrayList<BasicNameValuePair>();
		vp.add(new BasicNameValuePair(PARAM_METHOD, METHOD_ADD_CONNECTION));
		vp.add(new BasicNameValuePair(PARAM_TRACKER_CODE, targetingUserCode));
		vp.add(new BasicNameValuePair(PARAM_TRACKING_CODE, currentUserCode));
		return new HttpParse<Connection>(XML_CONNECTION).download(postDataString(vp));
	}
	
	public static ArrayList<User> checkUserExist(String userCode){
		ArrayList<BasicNameValuePair> vp = new ArrayList<BasicNameValuePair>();
		vp.add(new BasicNameValuePair(PARAM_METHOD, METHOD_USER_EXISTS));
		vp.add(new BasicNameValuePair(PARAM_CODE, userCode));
		return new HttpParse<User>(XML_USER).download(postDataString(vp));
	}
	
	public static boolean isConnected(long trackerId, long trackingId){
		ArrayList<BasicNameValuePair> vp = new ArrayList<BasicNameValuePair>();
		vp.add(new BasicNameValuePair(PARAM_METHOD, METHOD_IS_CONNECTED));
		vp.add(new BasicNameValuePair(PARAM_TRACKER_ID, String.valueOf(trackerId)));
		vp.add(new BasicNameValuePair(PARAM_TRACKING_ID, String.valueOf(trackingId)));
		String s = postDataString(vp);
		Log.i(TAG, "Result from connection check " + s);
		if(s.equalsIgnoreCase("true")) {
			Log.i(TAG, "Permission Granted");
			return true;
		}
		else return false;
	}
	
	public static boolean deleteConnection(long trackerId, long trackingId){
		ArrayList<BasicNameValuePair> vp = new ArrayList<BasicNameValuePair>();
		vp.add(new BasicNameValuePair(PARAM_METHOD, METHOD_DELETE_CONNECTION));
		vp.add(new BasicNameValuePair(PARAM_TRACKER_ID, String.valueOf(trackerId)));
		vp.add(new BasicNameValuePair(PARAM_TRACKING_ID, String.valueOf(trackingId)));
		String s = postDataString(vp);
		Log.i(TAG, "Delted: " + s);
		if(s.equalsIgnoreCase(RESPONSE_SUCCESS)){
			return true;
		}
		return false;		
	}
	
	public static void sendFeedback(String message){
		ArrayList<BasicNameValuePair> vp = new ArrayList<BasicNameValuePair>();
		vp.add(new BasicNameValuePair(PARAM_METHOD, METHOD_FEEDBACK));
		vp.add(new BasicNameValuePair(PARAM_MESSAGE, message));
		postDataString(vp);
	}
	
	
		
}
