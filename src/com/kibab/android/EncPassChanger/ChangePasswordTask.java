package com.kibab.android.EncPassChanger;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.os.AsyncTask;
import android.os.SystemClock;

public class ChangePasswordTask extends AsyncTask<ChangePassParams, Void, ChangePassResult> {

	// ./base/core/java/android/os/storage/IMountService.java
	static final int ENCRYPTION_STATE_NONE = 1;
	static final int ENCRYPTION_STATE_OK = 0;

	// Path to VDC binary
	static final String VDC_EXEC = "/system/bin/vdc";
	private BufferedReader rootResReader;
	private DataOutputStream rootOutStream;
	private BufferedReader rootErrReader;
	
	private EncPassChangerActivity resultCb;

	@Override
	protected ChangePassResult doInBackground(ChangePassParams... changeParams) {
		resultCb = changeParams[0].getCallingActivity();
		Process p = null;
		
		try {
			p = Runtime.getRuntime().exec("su");
		} catch (IOException e) {
			// Getting root rights failed. Do not do anything else
			e.printStackTrace();
			return new ChangePassResult(false, R.string.su_fail, e.getMessage());
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
				throw new EncPassChangeException(R.string.enc_verify_error, "");
			}
			String[] toks = result.split(" ");
			int ec = Integer.parseInt(toks[1]);
			if (ec != ENCRYPTION_STATE_OK) {
				System.out.println("!= ENCRYPTION_STATE_OK");
				throw new EncPassChangeException(R.string.enc_not_enabled, "");				
			}
			
//			verifyPassword(changeParams[0].getOldPassword(), R.string.pass_incorrect);

			// Step 3: Change password
			result = execCmdAndGetReply(VDC_EXEC + " cryptfs changepw '" + changeParams[0].getNewPassword() + "'", 1000);
			if (!parseVDCReply(result)) {
				throw new EncPassChangeException(R.string.error_when_changing, "");
			}

			// Step 4: Verify if this password is correct
			verifyPassword(changeParams[0].getNewPassword(), R.string.new_pass_incorrect);

		} catch (EncPassChangeException e) {
			// Display error message
			e.printStackTrace();
			return new ChangePassResult(false, e.getMessageCode(), e.getMessage());
		}
		
		try {
			execCmdAndGetReply("exit", 20);
			p.waitFor();
		} catch (Exception e) {};

		// TODO Auto-generated method stub
		return new ChangePassResult(true, R.string.password_changed, "OK");
	}

	/**
	 * @param changeParams
	 * @throws EncPassChangeException
	 */
	private void verifyPassword(String str, int whatToThrow)
			throws EncPassChangeException {
		String result;
		String[] toks;
		int ec;
		// Step 2: Test if supplied password is correct
		result = execCmdAndGetReply(VDC_EXEC + " cryptfs verifypw '" +
				str + "'", 1000);
		if (!parseVDCReply(result)) {
			System.out.println("VDC cannot veryfypw");
			throw new EncPassChangeException(R.string.verify_error, "");
		}
		toks = result.split(" ");
		ec = Integer.parseInt(toks[1]);
		if (ec != 0) {
			System.out.println("verifypw doesnt like this password");
			throw new EncPassChangeException(whatToThrow, "");				
		}
	}
	
	protected void onPostExecute (ChangePassResult result) {
		this.resultCb.updateResultDisplay(result.getMessageId(), result.getMessage());
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
			str = rootResReader.readLine();

			if (str == null) {
				if(rootErrReader.ready())
					str = rootErrReader.readLine();
				if (str == null) {
					str = "[unknown]";
				}
				throw new EncPassChangeException(R.string.exec_fail, str);
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new EncPassChangeException(R.string.exec_fail, e.getMessage());
		}

		return str;
	}

}
