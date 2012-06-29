package com.kibab.android.EncPassChanger;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

public class EncPassChangerActivity extends Activity {

	private EditText output;
	private ProgressBar progress;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		output = (EditText) findViewById(R.id.output);
		progress = (ProgressBar) findViewById(R.id.progress);
	}

	/* 'Go' button handler */
	public void beginWork(View view) {

		EditText oldPassInp = (EditText) findViewById(R.id.oldpass);
		EditText newPassInp = (EditText) findViewById(R.id.newpass);
		EditText newPassInp2 = (EditText) findViewById(R.id.newpass_again);
		
		String oldPass = oldPassInp.getText().toString().trim();
		String newPass = newPassInp.getText().toString().trim();

		if (! newPass.equals(newPassInp2.getText().toString().trim())) {
			output.setText(R.string.newpass_not_the_same);
			return;			
		}

		if (newPass.length() < 1) {
			output.setText(R.string.pass_empty);
			return;
		}
		
		ChangePasswordTask tsk = new ChangePasswordTask();
		ChangePassParams params = new ChangePassParams(oldPass, newPass, this);
		progress.setVisibility(View.VISIBLE);
		tsk.execute(params);
	}
	
	public void updateResultDisplay(int RID, String desc) {
		String msg = getString(RID);// + ":" + desc;
		output.setText(msg);
		progress.setVisibility(View.INVISIBLE);
	}
	
}