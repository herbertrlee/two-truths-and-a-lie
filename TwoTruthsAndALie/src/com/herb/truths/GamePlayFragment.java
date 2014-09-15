package com.herb.truths;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.AdRequest.Builder;
import com.herb.truths.entities.Question;
import com.herb.truths.entities.Statement;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

public class GamePlayFragment extends Fragment implements OnClickListener
{
	public interface Listener
	{
		public void onHintButtonClicked();
		public void onSubmitButtonClicked(int i);
		public Question getQuestion();
	}
		
	public static int[] MY_BUTTONS = {R.id.hint_button, R.id.submit_button};
	public static int[] STATEMENT_TEXTVIEWS = {R.id.statement_1_textview, R.id.statement_2_textview, R.id.statement_3_textview};
	
	private Listener myListener;
	private Question question = new Question();
	private TextView[] statementTextViews = new TextView[3];
	private int selected = -1;
	
	public static final String NAME = "GAME PLAY";
		
	@Override
	public void onStart()
	{
		super.onStart();
		updateUi();
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_gameplay, container, false);
        for (int i : MY_BUTTONS) {
            v.findViewById(i).setOnClickListener(this);
        }
        
        for(int i=0;i<STATEMENT_TEXTVIEWS.length;i++)
        {
        	int id = STATEMENT_TEXTVIEWS[i];
        	statementTextViews[i] = (TextView) v.findViewById(id);
        	statementTextViews[i].setOnClickListener(this);
        }
        
        AdView adView = (AdView) v.findViewById(R.id.adView);
        Builder adBuilder = new AdRequest.Builder();
        //adBuilder.addTestDevice("472FF7827E743365E9E208B1C229D2B7");
        AdRequest adRequest = adBuilder.build();
        adView.loadAd(adRequest);
        
        return v;
    }
	
	public void setListener(Listener l)
	{
		this.myListener = l;
	}
	
	@Override
	public void onClick(View view)
	{
		switch(view.getId())
		{
			case R.id.hint_button:
				myListener.onHintButtonClicked();
				updateUi();
				break;
			case R.id.submit_button:
				if(selected != -1)
					myListener.onSubmitButtonClicked(selected);
				break;
			case R.id.statement_1_textview:
				selected = 0;
				updateUi();
				break;
			case R.id.statement_2_textview:
				selected = 1;
				updateUi();
				break;
			case R.id.statement_3_textview:
				selected = 2;
				updateUi();
				break;
			default:
				break;
		}
	}

	public void updateUi()
	{
		if(getActivity() == null)
		{
			return;
		}
		
		if(myListener == null)
		{
			return;
		}
		
		this.question = myListener.getQuestion();
				
		if(question.isHintUsed())
		{
			Button hintButton = (Button) getActivity().findViewById(R.id.hint_button);
			hintButton.setTextColor(Color.GRAY);
			hintButton.setEnabled(false);
		}
		else
		{
			Button hintButton = (Button) getActivity().findViewById(R.id.hint_button);
			hintButton.setTextColor(Color.BLACK);
			hintButton.setEnabled(true);
		}
		
		ScrollView scrollView = (ScrollView) getActivity().findViewById(R.id.gameplay_scroll);
		scrollView.fullScroll(ScrollView.FOCUS_UP);
		
		Statement[] statements = question.getStatements();
		
		for(int i=0;i<STATEMENT_TEXTVIEWS.length;i++)
        {
        	statementTextViews[i].setText(statements[i].getText());
        	
        	if(statements[i].isRevealed())
        	{
        		statementTextViews[i].setTextColor(Color.GRAY);
        		statementTextViews[i].setClickable(false);
        		
        		if(selected==i)
        		{
        			selected = -1;
        			i=0;
        		}
        	}
        	else
        	{
        		statementTextViews[i].setTextColor(Color.BLACK);
        		statementTextViews[i].setClickable(true);
        	}
        	
        	if(selected != -1)
        	{
	        	if(i==selected)
	        	{
	        		statementTextViews[i].setBackgroundColor(Color.YELLOW);
	        	}
	        	else
	        	{
	        		statementTextViews[i].setBackgroundResource(R.color.lightBlue);
	        	}
        	}
        	else
        	{
        		statementTextViews[i].setBackgroundColor(Color.WHITE);
        	}
        }
	}
	
	public void resetSelection()
	{
		selected = -1;
	}
}
