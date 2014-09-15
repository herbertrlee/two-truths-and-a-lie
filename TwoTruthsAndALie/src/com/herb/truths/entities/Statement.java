/*
 *	Java Bean representing a Statement. 
 */

package com.herb.truths.entities;

import org.json.JSONException;
import org.json.JSONObject;

public class Statement
{
	private static final String ID_TAG = "ID";
	private static final String TEXT_TAG = "TEXT";
	private static final String EXPLANATION_TAG = "EXPLANATION";
	private static final String TRUTH_TAG = "TRUTH";
	private static final String REVEALED_TAG = "REVEALED";
	
	private int id = -1;
	private String text = "";
	private String explanation = "";
	private boolean truth = true;
	private boolean revealed = false;
	
	public Statement()
	{
	}

	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	public String getExplanation()
	{
		return explanation;
	}

	public void setExplanation(String explanation)
	{
		this.explanation = explanation;
	}

	public boolean isTruth()
	{
		return truth;
	}

	public void setTruth(boolean truth)
	{
		this.truth = truth;
	}

	public boolean isRevealed()
	{
		return revealed;
	}

	public void setRevealed(boolean revealed)
	{
		this.revealed = revealed;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}
	
	public JSONObject toJSON() throws JSONException
	{
		JSONObject json = new JSONObject();
		
		json.put(ID_TAG, id);
		json.put(TEXT_TAG, text);
		json.put(EXPLANATION_TAG, explanation);
		json.put(TRUTH_TAG, truth);
		json.put(REVEALED_TAG, revealed);
		
		return json;
	}
	
	public static Statement fromJSON(JSONObject json) throws JSONException
	{
		Statement s = new Statement();
		
		s.setId(json.getInt(ID_TAG));
		s.setText(json.getString(TEXT_TAG));
		s.setExplanation(json.getString(EXPLANATION_TAG));
		s.setTruth(json.getBoolean(TRUTH_TAG));
		s.setRevealed(json.getBoolean(REVEALED_TAG));
		
		return s;
	}
}
