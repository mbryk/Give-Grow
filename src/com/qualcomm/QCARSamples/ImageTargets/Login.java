package com.qualcomm.QCARSamples.ImageTargets;

import android.app.Activity;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Login extends Activity implements OnClickListener{
	private class AccountManagerObserver extends DataSetObserver {
		@Override
		public void onChanged() {
			if(accountManager.isLoggedin())
				finish();
		}
	}
	
	private AccountManager accountManager;
	
	private AccountManagerObserver accountManagerObserver2;
	
	protected void onCreate(Bundle savedInstanceState){
	super.onCreate(savedInstanceState);
	setContentView(R.layout.login);
	
	accountManager = AccountManager.getInstance(this);
	accountManagerObserver2 = new AccountManagerObserver();
	accountManager.addObserver(accountManagerObserver2);
	
	// associate views with their ids
			
	final EditText username = (EditText) findViewById(R.id.login_username);
	final EditText password = (EditText) findViewById(R.id.login_password);
	Button login = (Button) findViewById(R.id.login_button);
    TextView registerScreen = (TextView) findViewById(R.id.register_link);
 
    
    // Listening to register new account link
    registerScreen.setOnClickListener(new OnClickListener() {
    	public void onClick(View v) {
    	// Switching to Register screen
    		Intent i = new Intent(getApplicationContext(), Register.class);
    		startActivity(i);
    	}
    });

	
    // set the button's listener
	login.setOnClickListener(new OnClickListener() {
     	public void onClick(View v) {
     		
     		accountManager.login(username.getText().toString(), password.getText().toString());
     		
     		/*
     		 * Intent intent = new Intent(v.getContext(), Menu.class);
     		Menu.loggedin=true;
     		startActivity(intent);
     		*/
     		
     	}
     });
	
	
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

}
