package com.rickster.findmykid.model;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;


import android.util.Log;

public class HttpParse<E> {	
	private static final String TAG = "HttpParse";	
	private String mType;
	public HttpParse(String type){		
		mType = type;
		Log.i(TAG, "Type: " + mType);
	}	
	
	public ArrayList<E> download(String xmlString){
		ArrayList<E> lists = new ArrayList<E>();
		try	{			
			Log.i(TAG, "Got post data: " + xmlString);			
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(xmlString));
			if(mType.equals(HttpData.XML_USER)){
				Log.i(TAG, "Parsing Users");
				parseUsers(lists, parser);
			}else if(mType.equals(HttpData.XML_CONNECTION)){
				Log.i(TAG, "Parsing Connections");
				parseConnections(lists, parser);
			}else if(mType.equals(HttpData.XML_LOCATION)){
				Log.i(TAG, "Parsing Locations");
				parseLocations(lists, parser);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return lists;
	}
	
	public void parseUsers(ArrayList<E> lists, XmlPullParser parser) throws IOException{
		try	{
			int eventType = parser.next();
			while(eventType != XmlPullParser.END_DOCUMENT){
				if(eventType == XmlPullParser.START_TAG && parser.getName().equals(HttpData.XML_USER)){
					String id = parser.getAttributeValue(null, HttpData.XML_ID);
					String name = parser.getAttributeValue(null, HttpData.XML_NAME);	
					String gcm = parser.getAttributeValue(null, HttpData.XML_GCM);
					String code = parser.getAttributeValue(null, HttpData.XML_CODE);
					String phonenumber = parser.getAttributeValue(null, HttpData.XML_PHONE_NUMBER);						
					User user = new User(Long.valueOf(id), name, gcm, phonenumber, code);
					lists.add((E) user);
					Log.i(TAG, "User added: " + user.getName());
				}
				eventType = parser.next();
			}
		}catch(XmlPullParserException e){
			Log.i(TAG, "XMLPullParserExeption: " + e.toString());
		}		
	}
	
	public void parseConnections(ArrayList<E> lists, XmlPullParser parser) throws IOException{
		try	{
			int eventType = parser.next();
			while(eventType != XmlPullParser.END_DOCUMENT){
				if(eventType == XmlPullParser.START_TAG && parser.getName().equals(HttpData.XML_CONNECTION)){
					String id = parser.getAttributeValue(null, HttpData.XML_ID);
					String trackerid = parser.getAttributeValue(null, HttpData.XML_TRACKER);	
					String trackingid = parser.getAttributeValue(null, HttpData.XML_TRACKING);	
					Connection connection = new Connection(Long.valueOf(id), Long.valueOf(trackerid), Long.valueOf(trackingid));
					lists.add((E) connection);
					Log.i(TAG, "Connection added: " + connection.getId());
				}
				eventType = parser.next();
			}
		}catch(XmlPullParserException e){
			Log.i(TAG, "XMLPullParserExeption: " + e.toString());
		}		
	}
	
	public void parseLocations(ArrayList<E> lists, XmlPullParser parser) throws IOException{
		try	{
			int eventType = parser.next();
			while(eventType != XmlPullParser.END_DOCUMENT){
				if(eventType == XmlPullParser.START_TAG && parser.getName().equals(HttpData.XML_LOCATION)){
					String id = parser.getAttributeValue(null, HttpData.XML_ID);
					String latitude = parser.getAttributeValue(null, HttpData.XML_LATITUDE);	
					String longitude = parser.getAttributeValue(null, HttpData.XML_LONGITUDE);	
					String altitude = parser.getAttributeValue(null, HttpData.XML_ALTITUDE);	
					String time = parser.getAttributeValue(null, HttpData.XML_TIME);
					String requestId = parser.getAttributeValue(null, HttpData.XML_REQUEST_ID);
					String address = parser.getAttributeValue(null, HttpData.XML_ADDRESS);
					Location location = new Location(Long.valueOf(id), Float.valueOf(latitude), Float.valueOf(longitude), Float.valueOf(altitude), Long.valueOf(time), Long.valueOf(requestId), address);
					lists.add((E) location);
					Log.i(TAG, "Location added: " + location.getAddress());
				}
				eventType = parser.next();
			}
		}catch(XmlPullParserException e){
			Log.i(TAG, "XMLPullParserExeption: " + e.toString());
		}		
	}
}