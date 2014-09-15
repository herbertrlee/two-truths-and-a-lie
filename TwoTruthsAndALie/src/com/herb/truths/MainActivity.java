package com.herb.truths;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameActivity;
import com.herb.truths.entities.Question;
import com.herb.truths.lib.DatabaseHelper;
import com.herb.truths.lib.SyncHelper;

public class MainActivity extends BaseGameActivity implements MainMenuFragment.Listener, GamePlayFragment.Listener, SubmitFragment.Listener
{
	
	// request codes we use when invoking an external activity
    final int RC_RESOLVE = 5000, RC_UNUSED = 5001;
    
    final static String TAG = "TRUTHS";
    final static String FRAG_TAG = "TRUTHS FRAG TAG";
    final static String QUESTION_TAG = "TRUTHS QUESTION TAG";
    final static String USED_IDS_TAG = "TRUTHS USED IDS TAG";
    final static String CORRECT_TAG = "CORRECT TAG";
    
    final int MAX_USED_ID_SIZE = 99;
    
	GamePlayFragment myGamePlayFragment;
	MainMenuFragment myMainMenuFragment;
	SubmitFragment mySubmitFragment;
	ActionBar actionBar;
	
	Fragment currentFragment;
	String currentFragTag;
	
	DatabaseHelper myDbHelper;
	SyncHelper mySyncHelper;
	
	Question question = null;
	private boolean correct = false;
	
	ArrayList<Integer> usedIds = new ArrayList<Integer>();
	AccomplishmentsOutbox outbox = new AccomplishmentsOutbox();
	
	Map<Fragment, String> fragmentToNameMap;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
				
		myDbHelper = new DatabaseHelper(this);
		mySyncHelper = new SyncHelper();
		
		fragmentToNameMap = new HashMap<Fragment, String>();
		
		if(isNetworkConnected())
			sync();
				
		actionBar = getActionBar();
		actionBar.setHomeButtonEnabled(true);
		
		if(savedInstanceState != null)
		{
			currentFragTag = savedInstanceState.getString(FRAG_TAG);
			correct = savedInstanceState.getBoolean(CORRECT_TAG);
			
			FragmentManager myFm = getSupportFragmentManager();
			
			myMainMenuFragment = (MainMenuFragment) myFm.findFragmentByTag(MainMenuFragment.NAME);
			myGamePlayFragment =  (GamePlayFragment) myFm.findFragmentByTag(GamePlayFragment.NAME);
			mySubmitFragment = (SubmitFragment) myFm.findFragmentByTag(SubmitFragment.NAME);
			
			myMainMenuFragment.setListener(this);
			myGamePlayFragment.setListener(this);
			mySubmitFragment.setListener(this);
			
			fragmentToNameMap.put(myMainMenuFragment, MainMenuFragment.NAME);
			fragmentToNameMap.put(myGamePlayFragment, GamePlayFragment.NAME);
			fragmentToNameMap.put(mySubmitFragment, SubmitFragment.NAME);
			
			if(currentFragTag.equals(SubmitFragment.NAME))
			{
				currentFragment = mySubmitFragment;
				getSupportFragmentManager().beginTransaction()
				.hide(myMainMenuFragment)
				.hide(myGamePlayFragment)
				.commit();
			}
			else if(currentFragTag.equals(GamePlayFragment.NAME))
			{
				currentFragment = myGamePlayFragment;
				getSupportFragmentManager().beginTransaction()
				.hide(mySubmitFragment)
				.hide(myMainMenuFragment)
				.commit();
			}
			else
			{
				currentFragment = myMainMenuFragment;
				getSupportFragmentManager().beginTransaction()
				.hide(mySubmitFragment)
				.hide(myGamePlayFragment)
				.commit();
			}
			
			try
			{
				question = Question.fromJSON(new JSONObject(savedInstanceState.getString(QUESTION_TAG)));
				getUsedIdsFromJSON(new JSONArray(savedInstanceState.getString(USED_IDS_TAG)));
			} catch (JSONException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			getNextQuestion();
			
			myMainMenuFragment = new MainMenuFragment();
			myGamePlayFragment = new GamePlayFragment();
			mySubmitFragment = new SubmitFragment();
			
			myMainMenuFragment.setListener(this);
			myGamePlayFragment.setListener(this);
			mySubmitFragment.setListener(this);
			
			fragmentToNameMap.put(myMainMenuFragment, MainMenuFragment.NAME);
			fragmentToNameMap.put(myGamePlayFragment, GamePlayFragment.NAME);
			fragmentToNameMap.put(mySubmitFragment, SubmitFragment.NAME);
			
			getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, mySubmitFragment, SubmitFragment.NAME)
		    .hide(mySubmitFragment)
		    .add(R.id.fragment_container, myGamePlayFragment, GamePlayFragment.NAME)
		    .hide(myGamePlayFragment)
		    .add(R.id.fragment_container, myMainMenuFragment, MainMenuFragment.NAME)
		    .commit();
			
			currentFragment = myMainMenuFragment;
			currentFragTag = MainMenuFragment.NAME;
		}
	}
			
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState)
	{
		savedInstanceState.putString(FRAG_TAG, currentFragTag);
		savedInstanceState.putBoolean(CORRECT_TAG, correct);
		try
		{
			savedInstanceState.putString(QUESTION_TAG, question.toJSON().toString());
			savedInstanceState.putString(USED_IDS_TAG, usedIdsToJSON().toString());
		} catch (JSONException e)
		{
			e.printStackTrace();
		}
		
		super.onSaveInstanceState(savedInstanceState);
	}
	
	private void sync()
	{
		new CheckForUpdatesProcess().execute();
	}


	private void getNextQuestion()
	{
		if(question != null)
		{
			int[] ids = question.getStatementIds();
			
			for(int id : ids)
			{
				usedIds.add(Integer.valueOf(id));
			}
			
			if(usedIds.size() > MAX_USED_ID_SIZE)
			{
				usedIds.remove(0);
				usedIds.remove(0);
				usedIds.remove(0);
			}
		}
				
		QuestionProcess qp = new QuestionProcess();
		try
		{
			question = qp.execute(usedIds.toArray(new Integer[usedIds.size()])).get();
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		} catch (ExecutionException e)
		{
			e.printStackTrace();
		}
		question.shuffleStatements();
	}
	
	// Switch UI to the given fragment
    void switchToFragment(Fragment newFrag) 
    {
        if(newFrag.isVisible())
        	return;
        
        FragmentTransaction t =  getSupportFragmentManager().beginTransaction();
        
        newFrag.getView().bringToFront();
        currentFragment.getView().bringToFront();
        
        t.hide(currentFragment).show(newFrag);
        
        currentFragment = newFrag;
        currentFragTag = fragmentToNameMap.get(newFrag);
        t.commit();
        			
    }
    
	@Override
	public void onSignInFailed()
	{
		myMainMenuFragment.setShowSignIn(true);
		mySubmitFragment.setShowSignIn(true);
	}

	@Override
	public void onSignInSucceeded()
	{
		myMainMenuFragment.setShowSignIn(false);
		mySubmitFragment.setShowSignIn(false);
	}

	@Override
	public void onStartGameButtonClicked()
	{
		myGamePlayFragment.updateUi();
		switchToFragment(myGamePlayFragment);
	}

	@Override
    public void onShowAchievementsButtonClicked() 
	{
        if (isSignedIn()) 
        {
            startActivityForResult(Games.Achievements.getAchievementsIntent(getApiClient()),
                    RC_UNUSED);
        } 
        else 
        {
            showAlert(getString(R.string.achievements_not_available));
        }
    }

    @Override
    public void onShowLeaderboardsButtonClicked() 
    {
        if (isSignedIn()) 
        {
            startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(getApiClient()),
                    RC_UNUSED);
        } 
        else 
        {
            showAlert(getString(R.string.leaderboards_not_available));
        }
    }

	@Override
	public void onSignInButtonClicked()
	{
		beginUserInitiatedSignIn();
	}

	@Override
	public void onSignOutButtonClicked()
	{
		signOut();
		myMainMenuFragment.setShowSignIn(true);
		mySubmitFragment.setShowSignIn(true);
	}

	@Override
	public void onNextButtonClicked()
	{
		resetCorrect();
		getNextQuestion();
		myGamePlayFragment.resetSelection();
		switchToFragment(myGamePlayFragment);
		myGamePlayFragment.updateUi();
	}

	@Override
	public void onHintButtonClicked()
	{
		question.useHint();
	}

	@Override
	public void onSubmitButtonClicked(int i)
	{
		correct = question.validate(i);
			
		checkForAchievements();
		
		pushAccomplishments();
		
		mySubmitFragment.updateUi();
		switchToFragment(mySubmitFragment);
	}


	@Override
	public Question getQuestion()
	{
		return question;
	}


	public boolean isCorrect()
	{
		return correct;
	}

	public void resetCorrect()
	{
		correct = false;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

	    int itemId = item.getItemId();
	    switch (itemId) {
	    case android.R.id.home:
	    	if(!currentFragTag.equals(myMainMenuFragment.getTag()))
	    	{
	    		if(currentFragTag.equals(mySubmitFragment.getTag()))
	    		{
	    			resetCorrect();
	    			getNextQuestion();
	    			myGamePlayFragment.resetSelection();
	    		}
		        switchToFragment(myMainMenuFragment);
	    	}
	        break;
	    }

	    return true;
	}
	
	 private class QuestionProcess extends AsyncTask<Integer, Void, Question>
	 {

		@Override
		protected Question doInBackground(Integer... params)
		{
			int[] ids = new int[params.length];
			
			for(int i=0;i<params.length;i++)
			{
				ids[i] = params[i].intValue();
			}
						
			return myDbHelper.generateQuestion(ids);
		}
		 
	 }
	 
	 private class CheckForUpdatesProcess extends AsyncTask<Void, Void, Void>
	 {		 
		@Override
		protected Void doInBackground(Void... params)
		{			
			int lastClientId = myDbHelper.getLastStatementId();
			int lastServerId = 0;
			try
			{
				lastServerId = mySyncHelper.getMostRecentId();
			} catch (JSONException e)
			{
				e.printStackTrace();
			}
			
			if(lastClientId < lastServerId)
			{
				Log.i("TRUTHS SYNC", "updates available.  attempting sync.");
				try
				{
					JSONArray statementsJSONArray = mySyncHelper.fetchStatements(lastClientId);
					myDbHelper.insertStatements(statementsJSONArray);
					
				} catch (JSONException e)
				{
					e.printStackTrace();
				}
			}
			else
				Log.i("TRUTHS SYNC", String.format("%d rows on server, %d rows on client.  database up to date.", lastClientId, lastServerId));
			
			return null;
		}
	 }
	 
	 private void checkForAchievements()
	 {
		if(correct)
		{
			outbox.currentStreak++;
			outbox.count++;
			
			if(!question.isHintUsed())
			{
				outbox.noHintCount++;
			}
		}
		else
		{
			outbox.currentStreak = 0;
			outbox.losses++;
		}
		
		if(outbox.count > 0)
		{
			outbox.firstLieAchievement = true;
		}
		if(outbox.currentStreak == 10)
		{
			outbox.godlikeAchievement = true;
		}
		if(outbox.losses > 0)
		{
			outbox.cantWinAchievement = true;
		}
	 }
	 
	 private void pushAccomplishments()
	 {
		if(!isSignedIn())
		{
			return;
		}
		
		if(outbox.firstLieAchievement)
		{
			Games.Achievements.unlock(getApiClient(), getString(R.string.achievement_lie_to_me));
			outbox.firstLieAchievement = false;
		}
		if(outbox.polygraphAchievement)
		{
			Games.Achievements.unlock(getApiClient(), getString(R.string.achievement_polygraph_expert));
			outbox.polygraphAchievement = false;
		}
		if(outbox.cantWinAchievement)
		{
			Games.Achievements.unlock(getApiClient(), getString(R.string.achievement_cant_win_em_all));
			outbox.cantWinAchievement = false;
		}
		if(outbox.shamelessAchievement)
		{
			Games.Achievements.unlock(getApiClient(), getString(R.string.achievement_shameless_begging_from_the_developer));
			outbox.shamelessAchievement = false;
		}
		if(outbox.godlikeAchievement)
		{
			Games.Achievements.unlock(getApiClient(), getString(R.string.achievement_godlike));
			outbox.godlikeAchievement = false;
		}
		if(outbox.noHintsAchievement)
		{
			Games.Achievements.unlock(getApiClient(), getString(R.string.achievement_dont_need_no_stupid_hints));
			outbox.noHintsAchievement = false;
		}
		if(outbox.lieMasterAchievement)
		{
			Games.Achievements.unlock(getApiClient(), getString(R.string.achievement_the_lie_master));
			outbox.lieMasterAchievement = false;
		}
		
		if(outbox.count > 0)
		{
			Games.Achievements.increment(getApiClient(), getString(R.string.achievement_the_lie_master), outbox.count);
			Games.Achievements.increment(getApiClient(), getString(R.string.achievement_polygraph_expert), outbox.count);
			outbox.count=0;
		}
		
		if(outbox.noHintCount > 0)
		{
			Games.Achievements.increment(getApiClient(), getString(R.string.achievement_dont_need_no_stupid_hints), outbox.noHintCount);
			outbox.noHintCount = 0;
		}
		
		if(outbox.losses > 0)
		{
			Games.Achievements.increment(getApiClient(), getString(R.string.achievement_cant_win_em_all), outbox.losses);
			outbox.losses = 0;
		}
		
		if(outbox.currentStreak > 0)
		{
			Games.Leaderboards.submitScore(getApiClient(), getString(R.string.leaderboard_longest_streaks), outbox.currentStreak);
		}
	 }
	 
	 class AccomplishmentsOutbox
	 {
		 boolean firstLieAchievement = false;
		 boolean polygraphAchievement = false;
		 boolean cantWinAchievement = false;
		 boolean shamelessAchievement = false;
		 boolean godlikeAchievement = false;
		 boolean noHintsAchievement = false;
		 boolean lieMasterAchievement = false;
		 
		 int count = 0;
		 int currentStreak = 0;
		 int noHintCount = 0;
		 int losses = 0;
		 
		 boolean isEmpty()
		 {
			 return !firstLieAchievement && !polygraphAchievement && !cantWinAchievement &&
					 !shamelessAchievement && !godlikeAchievement && !noHintsAchievement &&
					 !lieMasterAchievement && count==0 && currentStreak==0 && noHintCount==0 && losses==0;
		 }
	 }
	 
	 private boolean isNetworkConnected() {
		  ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		  return (cm.getActiveNetworkInfo() != null);
	 }
	 
	 private JSONArray usedIdsToJSON()
	 {
		 JSONArray usedIdJSONArray = new JSONArray();
		 
		 for(int i=0;i<usedIds.size();i++)
		 {
			 usedIdJSONArray.put(usedIds.get(i));
		 }
		 
		 return usedIdJSONArray;
	 }
	 
	 private void getUsedIdsFromJSON(JSONArray usedIdJSONArray) throws JSONException
	 {
		 for(int i=0;i<usedIdJSONArray.length();i++)
		 {
			 usedIds.add(usedIdJSONArray.getInt(i));
		 }
	 }
}
