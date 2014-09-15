package com.herb.truths;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.AdRequest.Builder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

public class MainMenuFragment extends Fragment implements OnClickListener
{
	public interface Listener {
        public void onStartGameButtonClicked();
        public void onShowAchievementsButtonClicked();
        public void onShowLeaderboardsButtonClicked();
        public void onSignInButtonClicked();
        public void onSignOutButtonClicked();
    }
	
	final int[] CLICKABLES = new int[] {R.id.gameplay_button, R.id.sign_in_button, R.id.sign_out_button,
    		R.id.show_achievements_button, R.id.show_leaderboards_button
    };
	
	Listener mListener = null;
	boolean showSignIn = true;
	
	LinearLayout signInBar, signOutBar;
	
	public static final String NAME = "MAIN MENU";
		
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_mainmenu, container, false);
        
        for (int i : CLICKABLES) {
            v.findViewById(i).setOnClickListener(this);
        }
        
        signInBar = (LinearLayout) v.findViewById(R.id.sign_in_bar);
        signOutBar = (LinearLayout) v.findViewById(R.id.sign_out_bar);
        
        AdView adView = (AdView) v.findViewById(R.id.adView);
        Builder adBuilder = new AdRequest.Builder();
        //adBuilder.addTestDevice("472FF7827E743365E9E208B1C229D2B7");
        AdRequest adRequest = adBuilder.build();
        adView.loadAd(adRequest);
        
        return v;
    }
	
	public void setListener(Listener l)
	{
		mListener = l;
	}
	
	@Override
	public void onClick(View view)
	{
		switch(view.getId())
		{
			case R.id.gameplay_button:
				mListener.onStartGameButtonClicked();
				break;
			case R.id.sign_in_button:
				mListener.onSignInButtonClicked();
				break;
			case R.id.sign_out_button:
				mListener.onSignOutButtonClicked();
				break;
			case R.id.show_achievements_button:
				mListener.onShowAchievementsButtonClicked();
				break;
			case R.id.show_leaderboards_button:
				mListener.onShowLeaderboardsButtonClicked();
				break;
			default:
				break;
		}
	}

	@Override
    public void onStart() {
        super.onStart();
        updateUi();
    }
	
	public void updateUi()
	{
		if(getActivity() == null)
			return;
		
		if(showSignIn)
		{
			signInBar.setVisibility(View.VISIBLE);
			signOutBar.setVisibility(View.GONE);

		}
		else
		{
			signInBar.setVisibility(View.GONE);
			signOutBar.setVisibility(View.VISIBLE);
		}
	}
	
	public void setShowSignIn(boolean signIn)
	{
		showSignIn = signIn;
		updateUi();
	}
}
