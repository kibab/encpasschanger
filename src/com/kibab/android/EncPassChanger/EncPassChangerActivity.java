package com.kibab.android.EncPassChanger;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.EditText;

public class EncPassChangerActivity extends Activity {

	// ./base/core/java/android/os/storage/IMountService.java
	static final int ENCRYPTION_STATE_NONE = 1;
	static final int ENCRYPTION_STATE_OK = 0;

	// Path to VDC binary
	static final String VDC_EXEC = "/system/bin/vdc";
	private BufferedReader rootResReader;
	private DataOutputStream rootOutStream;
	private BufferedReader rootErrReader;
	private EditText output;

	public EncPassChangerActivity() {
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		output = (EditText) findViewById(R.id.output);
	}

	/* 'Go' button handler */
	public void beginWork(View view) {
		Process p = null;

		EditText newPassInp = (EditText) findViewById(R.id.testfield);
		try {
			p = Runtime.getRuntime().exec("su");
		} catch (IOException e) {
			// Getting root rights failed. Do not do anything else
			e.printStackTrace();
			output.setText(R.string.su_fail);
			return;
		}

		String newPass = newPassInp.getText().toString();
		newPass = newPass.trim();
		if (newPass.length() < 1) {
			output.setText(R.string.pass_invalid);
			return;
		}

		rootOutStream = new DataOutputStream(p.getOutputStream());
		rootResReader = new BufferedReader(new InputStreamReader(
				p.getInputStream()));
		rootErrReader = new BufferedReader(new InputStreamReader(
				p.getErrorStream()));

		String result;

		try {
			// Step 1: Test if encryption is enabled
			result = execCmdAndGetReply(VDC_EXEC + " cryptfs cryptocomplete", 50);
			if (!parseVDCReply(result)) {
				throw new EncPassChangeException(getString(R.string.enc_verify_error),
						getString(R.string.unk_error));
			}
			String toks[] = result.split(" ");
			int ec = Integer.parseInt(toks[1]);
			if (ec != ENCRYPTION_STATE_OK) {
				throw new EncPassChangeException(getString(R.string.enc_not_enabled),
						getString(R.string.unk_error));				
			}
			
			// Step 2: Test if supplied password is valid
			result = execCmdAndGetReply(VDC_EXEC + " cryptfs verifypw '" + newPass + "'", 1000);
			if (!parseVDCReply(result)) {
				throw new EncPassChangeException(getString(R.string.pass_invalid),
						getString(R.string.verify_error));
			}
			// Step 3: Change password
			result = execCmdAndGetReply(VDC_EXEC + " cryptfs changepw '" + newPass + "'", 1000);
			if (!parseVDCReply(result)) {
				throw new EncPassChangeException(getString(R.string.error_when_changing),
						getString(R.string.unk_error));
			}

			output.setText(R.string.password_changed);

		} catch (EncPassChangeException e) {
			// Display error message
			e.printStackTrace();
			output.setText(e.getErrorText());
		}
		
		try {
			execCmdAndGetReply("exit", 20);
			p.waitFor();
		} catch (Exception e) {};
	}
	
	private boolean parseVDCReply(String reply) {
		String toks[] = reply.split(" ", 2);
		int code = Integer.parseInt(toks[0]);
		return (code == 200) ? true : false;
	}

	private String execCmdAndGetReply(String cmd, int maxWait) throws EncPassChangeException {
		String str = null;

		System.out.println("Executing command " + cmd);
		try {
			rootOutStream.writeBytes(cmd + "\n");
			rootOutStream.flush();
			SystemClock.sleep(maxWait);
			System.out.println("Reading reply...");
			//if(rootResReader.ready())
				str = rootResReader.readLine();

			if (str == null) {
				if(rootErrReader.ready())
					str = rootErrReader.readLine();
				if (str == null) {
					str = "(will block if read())";//;getString(R.string.unk_error);
				}
				throw new EncPassChangeException(getString(R.string.exec_fail), str);
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new EncPassChangeException(getString(R.string.exec_fail), e.getMessage());
		}

		return str;
	}
}