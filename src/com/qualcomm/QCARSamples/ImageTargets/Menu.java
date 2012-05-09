package com.qualcomm.QCARSamples.ImageTargets;

import android.app.Activity;
import android.content.Intent;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Menu extends Activity{
	
	private AccountManager accountManager;
	private AccountManagerObserver accountManagerObserver;
	private Button view,profile,login,donate,logout;
	
	public static final String DOMAIN = "warm-journey-4949.herokuapp.com";
	public static final String SLASH_DOMAIN = "//" + DOMAIN;
	public int given= 0;
	
/*	public void onRestart() {
		if (given==true)
		{
			Looper.prepare();
			Toast.makeText(context, context.getString(R.string.donation), Toast.LENGTH_SHORT);
		}
	}
*/	private class AccountManagerObserver extends DataSetObserver {
		public void onChanged() {
			
			profile.setText(accountManager.getUsername());
			
			if (accountManager.isLoggedin() ) {
				// login related buttons are now visible
				view.setVisibility(View.VISIBLE);
				profile.setVisibility(View.VISIBLE);
				logout.setVisibility(View.VISIBLE);
				login.setVisibility(View.GONE);
				//	register.setVisibility(View.GONE);
			} else {
				// 	login related buttons are now invisible
				view.setVisibility(View.GONE);
				profile.setVisibility(View.GONE);
				logout.setVisibility(View.GONE);
				login.setVisibility(View.VISIBLE);
				//	register.setVisibility(View.VISIBLE);
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);
        
     // get the current instance of account manager
     	accountManager = AccountManager.getInstance(this);
     	// watch the account manager for when the user logs in
     	accountManagerObserver = new AccountManagerObserver();
     	accountManager.addObserver(accountManagerObserver);
        
        view = (Button) findViewById(R.id.view);
        profile = (Button) findViewById(R.id.menu_username);
        login = (Button) findViewById(R.id.login);
        donate = (Button) findViewById(R.id.donate);
        logout = (Button) findViewById(R.id.logout);
        
        view.setVisibility(View.GONE);
		profile.setVisibility(View.GONE);
		logout.setVisibility(View.GONE);
		
        view.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		if (given>0){
        			Intent intent1 = new Intent(v.getContext(), ImageTargets.class);
        			startActivityForResult(intent1,0);
        		}
        	}
        });
             		
        donate.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://students.cooper.edu/bryk/cs102e/web-2/givingGarden/index.html"));
        		given=1;
        		startActivity(browserIntent);
        	}
        });
        
        login.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		Intent intent2 = new Intent(v.getContext(), Login.class);
        		startActivityForResult(intent2,0);
        	}
        });
        
        logout.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		startActivity(getIntent()); 
        		finish();
        	}
        });
	
	}

}
