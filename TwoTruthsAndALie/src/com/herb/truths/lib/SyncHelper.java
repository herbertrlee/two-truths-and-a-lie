package com.herb.truths.lib;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SyncHelper
{
	private static final String appUrl = "http://herbtruths.appspot.com";
	
	private static final String TAG = "tag";
	
	private static final String GET_MOST_RECENT_ID = "getMostRecentId";
	private static final String FETCH_STATEMENTS = "fetchStatements";
	private static final String REGISTER_USER_DEVICE = "registerUserDevice";
	
	private static final String MOST_RECENT_ID_TAG = "mostRecentId";
	private static final String LAST_ID_TAG = "lastId";
	private static final String STATEMENTS_TAG = "statements";
	private static final String REG_ID_TAG = "regId";
	
	private JSONParser jsonParser;
	
	public SyncHelper()
	{
		jsonParser = new JSONParser();
	}
	
	public int getMostRecentId() throws JSONException
	{
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair(TAG, GET_MOST_RECENT_ID));
		JSONObject json = jsonParser.getJSONFromUrl(appUrl, params);
		return json.getInt(MOST_RECENT_ID_TAG);
	}
	
	public JSONArray fetchStatements(int lastId) throws JSONException
	{
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair(TAG, FETCH_STATEMENTS));
		params.add(new BasicNameValuePair(LAST_ID_TAG, Integer.toString(lastId)));
		JSONObject json = jsonParser.getJSONFromUrl(appUrl, params);
		
		return json.getJSONArray(STATEMENTS_TAG);
	}

	public JSONObject registerUserDevice(String regid)
	{
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair(TAG, REGISTER_USER_DEVICE));
		params.add(new BasicNameValuePair(REG_ID_TAG, regid));
		JSONObject json = jsonParser.getJSONFromUrl(appUrl, params);
		return json;
	}
}
