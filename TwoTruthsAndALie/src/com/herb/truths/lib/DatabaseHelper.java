package com.herb.truths.lib;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.herb.truths.R;
import com.herb.truths.entities.Question;
import com.herb.truths.entities.Statement;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class DatabaseHelper extends SQLiteAssetHelper
{
	private static final String DB_NAME = "truths.2.db";
	private static final int DB_VERSION = R.integer.db_version;
	
	private static final String STATEMENT_TABLE_NAME = "statements";
	
	private static final String ID_KEY = "rowid";
	private static final String TEXT_KEY = "statement_text";
	private static final String EXPLANATION_KEY = "statement_explanation";
	private static final String VALUE_KEY = "value";
	private static final String SET_ID_KEY = "set_id";
	
	private static final String TRUE_VALUE = "TRUE";
	private static final String FALSE_VALUE = "FALSE";
	
	private static final String RANDOM_ORDER = "RANDOM()";
	
	public DatabaseHelper(Context context)
	{
		super(context, DB_NAME, null, DB_VERSION);
	}

	public void printAllRows()
	{
		SQLiteDatabase db = getReadableDatabase();
		
		String sql = "SELECT * FROM statements where rowid > 180";
		
		Cursor c = db.rawQuery(sql, null);
		
		while(c.moveToNext())
		{
			String s = "row: ";
			for(int i=0;i<c.getColumnCount();i++)
			{
				s += String.format("(%s, %s)", c.getColumnName(i), c.getString(i));
			}
			
			Log.e("statement", s);
		}
		c.close();
		db.close();
	}
	
	public Question generateQuestion(int[] usedIds)
	{
		Question question = new Question();
		SQLiteDatabase db = getReadableDatabase();
		
		String whereNotInClause = "(";
		
		for(int i=0;i<usedIds.length;i++)
		{
			if(i!=0)
			{
				whereNotInClause += ", ";
			}
			
			whereNotInClause += Integer.toString(usedIds[i]);
		}
		
		whereNotInClause += ")";
		
		String sql = String.format("SELECT %s, %s, %s FROM %s WHERE %s = '%s' AND %s NOT IN %s ORDER BY %s LIMIT 2", ID_KEY, TEXT_KEY, EXPLANATION_KEY, STATEMENT_TABLE_NAME, VALUE_KEY, TRUE_VALUE, ID_KEY, whereNotInClause, RANDOM_ORDER);
		
		Log.e("sql", sql);
				
		Cursor c = db.rawQuery(sql, null);
		
		c.moveToNext();
		int id = c.getInt(0);
		String text = c.getString(1);
		String explanation = c.getString(2);
		
		Statement trueStatement1 = new Statement();
		trueStatement1.setId(id);
		trueStatement1.setText(text);
		trueStatement1.setExplanation(explanation);
		trueStatement1.setTruth(true);
		
		c.moveToNext();
		id = c.getInt(0);
		text = c.getString(1);
		explanation = c.getString(2);
		
		Statement trueStatement2 = new Statement();
		trueStatement2.setId(id);
		trueStatement2.setText(text);
		trueStatement2.setExplanation(explanation);
		trueStatement2.setTruth(true);
		
		sql = String.format("SELECT %s, %s, %s FROM %s WHERE %s = '%s' AND %s NOT IN %s ORDER BY %s LIMIT 1", ID_KEY, TEXT_KEY, EXPLANATION_KEY, STATEMENT_TABLE_NAME, VALUE_KEY, FALSE_VALUE, ID_KEY, whereNotInClause, RANDOM_ORDER);

		c = db.rawQuery(sql, null);
		
		c.moveToNext();
		id = c.getInt(0);
		text = c.getString(1);
		explanation = c.getString(2);
		
		Statement falseStatement = new Statement();
		falseStatement.setId(id);
		falseStatement.setText(text);
		falseStatement.setExplanation(explanation);
		falseStatement.setTruth(false);
		
		Statement[] statements = {trueStatement1, trueStatement2, falseStatement};
		question.setStatements(statements);
		
		c.close();
		db.close();
		return question;
	}
	
	public int getLastStatementId()
	{
		int lastId = 0;
		SQLiteDatabase db = getReadableDatabase();
		
		String sql = String.format("SELECT MAX(%s) FROM %s", ID_KEY, STATEMENT_TABLE_NAME);
		Cursor c = db.rawQuery(sql, null);
		
		if(c.moveToFirst())
		{
			lastId = c.getInt(0);
		}
		
		c.close();
		db.close();
		
		
		return lastId;
	}
	
	public void insertStatements(JSONArray statementsJSONArray)
	{
		SQLiteDatabase db = getWritableDatabase();
		for(int i=0;i<statementsJSONArray.length();i++)
		{
			try
			{
				JSONObject statementJSON = statementsJSONArray.getJSONObject(i);
				
				int rowId = statementJSON.getInt("rowId");
				String statementText = statementJSON.getString("statementText");
				String statementExplanation = statementJSON.getString("statementExplanation");
				boolean value = statementJSON.getBoolean(VALUE_KEY);
				int setId = statementJSON.getInt("setId");
				
				String valueString = FALSE_VALUE;
				if(value)
					valueString = TRUE_VALUE;
				
				ContentValues statementValues = new ContentValues();
				statementValues.put(ID_KEY, rowId);
				statementValues.put(TEXT_KEY, statementText);
				statementValues.put(EXPLANATION_KEY, statementExplanation);
				statementValues.put(VALUE_KEY, valueString);
				statementValues.put(SET_ID_KEY, setId);
				
				db.insert(STATEMENT_TABLE_NAME, null, statementValues);
				
			} catch (JSONException e)
			{
				e.printStackTrace();
			}
		}
		
		db.close();
	}
}
