package com.qualcomm.QCARSamples.ImageTargets;

import com.qualcomm.QCARSamples.ImageTargets.AccountManager;
import com.qualcomm.QCARSamples.ImageTargets.R;

import android.app.Activity;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Register extends Activity implements OnClickListener{
	private class AccountManagerObserver extends DataSetObserver {
		@Override
		public void onChanged() {
			if(accountManager.isLoggedin())
				finish();
		}
	}
	
	private AccountManager accountManager;
	
	private AccountManagerObserver accountManagerObserver2;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set View to register.xml
        setContentView(R.layout.register);
        
		accountManager = AccountManager.getInstance(this);
		accountManagerObserver2 = new AccountManagerObserver();
		accountManager.addObserver(accountManagerObserver2);
        
        final EditText username = (EditText) findViewById(R.id.reg_username);
    	final EditText password1 = (EditText) findViewById(R.id.reg_password);
    	final EditText password2 = (EditText) findViewById(R.id.reg_password2);
    	Button loginButton = (Button) findViewById(R.id.btnRegister);
        TextView loginScreen = (TextView) findViewById(R.id.link_to_login);
 
        // Listening to Login Screen link
        loginScreen.setOnClickListener(new OnClickListener() {
 
            public void onClick(View arg0) {
                                // Closing registration screen
                // Switching to Login Screen/closing register screen
                finish();
            }
        });
        
        loginButton.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		
        	
    			// make sure passwords match
    			if (password1.getText().toString().equals(password2.getText().toString())) {
    				// send signup information to the server
    				accountManager.signup(username.getText().toString(), password1.getText().toString(),password2.getText().toString());
    			} 
    			else { // passwords do not match
    				Toast.makeText(Register.this, R.string.error_mismatched_passwords, Toast.LENGTH_SHORT);
    			}        		
         	}
         });
              
    }

	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		
	}

}
