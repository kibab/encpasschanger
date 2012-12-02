package com.kibab.android.EncPassChanger;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;

public class EncPassChangerActivity extends Activity {

	private EditText output;
	private ProgressBar progress;
	private CheckBox isVerbose;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		output = (EditText) findViewById(R.id.output);
		progress = (ProgressBar) findViewById(R.id.progress);
		isVerbose = (CheckBox) findViewById(R.id.isDebug);
	}

	/* 'Go' button handler */
	public void beginWork(View view) {
		
		hideSoftKeyboard();
		output.setText("");

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
	
	/**
	 * Used to deliver progress from background thread.
	 * Called in UI thread.
	 * @param RID If not 0, this is considered a final update,
	 * the progress bar will be shut down and localized message displayed.
	 * @param desc This is a verbose-mode message. Displayed only if "Verbose"
	 * flag is set in UI.
	 */
	public void updateResultDisplay(int RID, String desc) {
		String msg = new String();

		if (RID > 0)
			msg = getString(RID);
		if (desc.length() > 0 && isVerbose.isChecked())
			msg += (msg.length() > 0 ? ": " : "") + desc;
		
		if (msg.length() > 0)
			output.append(msg + "\n");
		if(RID > 0)
			progress.setVisibility(View.INVISIBLE);
	}
	
	/**
	 * Force soft keyboard to hide away
	 */
	private void hideSoftKeyboard() {
	    InputMethodManager inputMethodManager =
	    		(InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
	    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
	}
	
}