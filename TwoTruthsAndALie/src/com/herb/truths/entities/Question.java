package com.herb.truths.entities;

import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Question
{
	private static final String STATEMENTS_TAG = "STATEMENTS";
	private static final String HINT_USED_TAG = "HINT USED";
	
	Statement[] statements = new Statement[3];
	boolean hintUsed = false;
	
	public Question()
	{
		
	}
	
	public void setStatements(Statement[] statements)
	{
		this.statements = statements;
	}
	
	public Statement[] getStatements()
	{
		return statements;
	}
	
	public void shuffleStatements()
	{
		Random rand = new Random();
		int index;
		Statement temp;
		
		for(int i=2;i>0;i--)
		{
			index = rand.nextInt(i+1);
			temp = statements[index];
			statements[index] = statements[i];
			statements[i] = temp;
		}
	}
	
	public Statement getStatement(int i)
	{
		return statements[i];
	}
	
	public Statement[] getTrueStatements()
	{
		Statement[] trueStatements = new Statement[2];
		
		int i=0;
		for(Statement statement : statements)
		{
			if(statement.isTruth())
			{
				trueStatements[i] = statement;
				i++;
			}
		}
		
		return trueStatements;
	}
	
	public void useHint()
	{
		if(!hintUsed)
		{
			hintUsed = true;
			Random rand = new Random();
			Statement[] trueStatements = getTrueStatements();
			int i= rand.nextInt(2);
			trueStatements[i].setRevealed(true);
		}
	}
	
	public boolean validate(int i)
	{
		return !statements[i].isTruth();
	}
	
	public boolean isHintUsed()
	{
		return hintUsed;
	}
	
	public int[] getStatementIds()
	{
		int[] ids = new int[statements.length];
		
		for(int i=0;i<ids.length;i++)
		{
			ids[i] = statements[i].getId();
		}
		
		return ids;
	}
	
	public JSONObject toJSON() throws JSONException
	{
		JSONObject questionJSON = new JSONObject();
		
		JSONArray statementJSONArray = new JSONArray();
		
		for(Statement s : statements)
		{
			statementJSONArray.put(s.toJSON());
		}
		
		questionJSON.put(STATEMENTS_TAG, statementJSONArray);
		questionJSON.put(HINT_USED_TAG, hintUsed);
		
		return questionJSON;
	}
	
	public static Question fromJSON(JSONObject questionJSON) throws JSONException
	{
		Question q = new Question();
		
		JSONArray statementJSONArray = questionJSON.getJSONArray(STATEMENTS_TAG);
		Statement[] statements = new Statement[statementJSONArray.length()];
		
		for(int i=0;i<statementJSONArray.length();i++)
		{
			statements[i] = Statement.fromJSON(statementJSONArray.getJSONObject(i));
		}
		
		q.setStatements(statements);
		q.setHintUsed(questionJSON.getBoolean(HINT_USED_TAG));
		
		return q;
	}

	public void setHintUsed(boolean hintUsed)
	{
		this.hintUsed = hintUsed;
	}
}
