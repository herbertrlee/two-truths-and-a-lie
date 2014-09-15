package com.herb.truths;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdRequest.Builder;
import com.google.android.gms.ads.AdView;
import com.herb.truths.entities.Question;
import com.herb.truths.entities.Statement;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class SubmitFragment extends Fragment implements OnClickListener
{
	public interface Listener
	{
		public void onNextButtonClicked();
		public void onSignInButtonClicked();
		public void onSignOutButtonClicked();
		public Question getQuestion();
		public boolean isCorrect();
	}
	
	public static final int[] CLICKABLES = {R.id.sign_in_button, R.id.next_question_button};
	public static final int[] EXPLANATION_TEXTVIEWS = {R.id.explanation_1_textview, R.id.explanation_2_textview, R.id.explanation_3_textview};
	public static final int[] LINEAR_LAYOUTS = {R.id.box1, R.id.box2, R.id.box3};
	public static int[] STATEMENT_TEXTVIEWS = {R.id.statement_1_textview, R.id.statement_2_textview, R.id.statement_3_textview};

	private Listener myListener;
	private Question question;
	private boolean showSignIn = true;
	
	LinearLayout[] boxes = new LinearLayout[3];
	TextView[] statementTextViews = new TextView[3];
	TextView[] explanationTextViews = new TextView[3];
	
	public static final String NAME = "SUBMIT";
		
	@Override
	public void onStart()
	{
		super.onStart();
		updateUi();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_submit, container, false);
        
        for(int i : CLICKABLES)
        {
        	v.findViewById(i).setOnClickListener(this);
        }
        
        for(int i=0;i<3;i++)
        {
        	boxes[i] = (LinearLayout) v.findViewById(LINEAR_LAYOUTS[i]);
        	statementTextViews[i] = (TextView) v.findViewById(STATEMENT_TEXTVIEWS[i]);
        	explanationTextViews[i] = (TextView) v.findViewById(EXPLANATION_TEXTVIEWS[i]);
        }
        
        AdView adView = (AdView) v.findViewById(R.id.adView);
        Builder adBuilder = new AdRequest.Builder();
        //adBuilder.addTestDevice("472FF7827E743365E9E208B1C229D2B7");
        AdRequest adRequest = adBuilder.build();
        adView.loadAd(adRequest);
        
        return v;
    }
	
	@Override
	public void onClick(View v)
	{
		switch(v.getId())
		{
			case R.id.sign_in_button:
				myListener.onSignInButtonClicked();
				break;
			case R.id.sign_out_button:
				myListener.onSignOutButtonClicked();
				break;
			case R.id.next_question_button:
				myListener.onNextButtonClicked();
				break;
			default:
				break;
		}
	}

	public Listener getListener()
	{
		return myListener;
	}

	public void setListener(Listener myListener)
	{
		this.myListener = myListener;
	}
	
	public void updateUi()
	{
		if(getActivity()==null)
			return;
		
		if(myListener==null)
			return;
		
		if(showSignIn)
		{
			getActivity().findViewById(R.id.sign_in_bar).setVisibility(View.VISIBLE);
		}
		else
		{
			getActivity().findViewById(R.id.sign_in_bar).setVisibility(View.GONE);

		}
		
		question = myListener.getQuestion();
		
		TextView resultTextView = (TextView) getActivity().findViewById(R.id.result_textview);
		if(myListener.isCorrect())
		{
			resultTextView.setText(getString(R.string.congratulations));
		}
		else
		{
			resultTextView.setText(getString(R.string.oops));
		}
		
		ScrollView scrollView = (ScrollView) getActivity().findViewById(R.id.submit_scroll);
		scrollView.fullScroll(ScrollView.FOCUS_UP);
		
		Statement[] statements = question.getStatements();
		
		for(int i=0;i<statements.length;i++)
		{
			explanationTextViews[i].setText(statements[i].getExplanation());
			
			statementTextViews[i].setText(statements[i].getText());
			
			if(statements[i].isTruth())
			{
				boxes[i].setBackgroundResource(R.color.lightBlue);
			}
			else
			{
				boxes[i].setBackgroundColor(Color.YELLOW);
			}
		}
	}

	public void setShowSignIn(boolean showSignIn)
	{
		this.showSignIn = showSignIn;
		updateUi();
	}
}
