package com.kibab.android.EncPassChanger;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import android.os.AsyncTask;
import android.os.Build;
import android.os.SystemClock;

public class ChangePasswordTask extends
		AsyncTask<ChangePassParams, ChangePassProgress, ChangePassResult> {

	// ./base/core/java/android/os/storage/IMountService.java
	static final int ENCRYPTION_STATE_NONE = 1;
	static final int ENCRYPTION_STATE_OK = 0;
	
	// Path to VDC binary
	static final String VDC_EXEC = "/system/bin/vdc";
	private static final String VDC_REPLY_OK = "200";
	private BufferedReader rootResReader;
	private DataOutputStream rootOutStream;
	private BufferedReader rootErrReader;

	private EncPassChangerActivity resultCb;
	private int resultPos;
	private List<String> logstrings;

	@Override
	protected ChangePassResult doInBackground(ChangePassParams... changeParams) {
		resultCb = changeParams[0].getCallingActivity();
		
		// Starting from Jelly Bean the format of vdc output has been changed,
		// 3 numbers instead of 2, the result is the last one.
		resultPos = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH) ? 1 : 2;
		Process p = null;

		addToLog("Getting root rights...");
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
			String[] toks;
			
			// Step 1: Test if encryption is enabled
			result = execCmdAndGetReply(VDC_EXEC + " cryptfs cryptocomplete", 50);

			toks = result.split(" ");
			if (!toks[0].equals(VDC_REPLY_OK))
				throw new EncPassChangeException(R.string.enc_verify_error, toks[0]);

			int ec = Integer.parseInt(toks[resultPos]);
			if (ec != ENCRYPTION_STATE_OK) {
				addToLog("Encryption status is not ENCRYPTION_STATE_OK: " + ec );
				throw new EncPassChangeException(R.string.enc_not_enabled, "");
			}

			// Step 2: Verify if old password is correct
			verifyPassword(changeParams[0].getOldPassword(),
					R.string.pass_incorrect);

			// Step 3: Change password
			result = execCmdAndGetReply(VDC_EXEC + " cryptfs changepw '"
					+ ESC(changeParams[0].getNewPassword()) + "'", 1000);
			toks = result.split(" ");
			if (!toks[0].equals(VDC_REPLY_OK))
				throw new EncPassChangeException(R.string.error_when_changing, "");
			
			// Step 4: Verify if this password is correct
			verifyPassword(changeParams[0].getNewPassword(),
					R.string.new_pass_incorrect);

		} catch (EncPassChangeException e) {
			// Display error message
			e.printStackTrace();
			return new ChangePassResult(false, e.getMessageCode(), e.getMessage());
		}

		try {
			execCmdAndGetReply("exit", 20);
			p.waitFor();
		} catch (Exception e) {
		}

		return new ChangePassResult(true, R.string.password_changed, "");
	}

	@Override
	protected void onPostExecute(ChangePassResult result) {
		this.resultCb.updateResultDisplay(result.getMessageId(),
				result.getMessage());
	}
	
	@Override
	protected void onProgressUpdate(ChangePassProgress... prog) {
		this.resultCb.updateResultDisplay(0, prog[0].getLogMsg());
	}
	

	/**
	 * Escape single quotes and backslashes
	 * @param str
	 * @return Escaped string
	 */
	private String ESC(String str) {
		String res; 
		res = str.replaceAll("\\\\", "\\\\\\\\");
		res = res.replaceAll("'", "'\"'\"'");
		return res;
	}

	/**
	 * @param changeParams
	 * @throws EncPassChangeException
	 */
	private void verifyPassword(String password, int whatToThrow)
			throws EncPassChangeException {
		String result;
		String[] toks;
		int ec;
		// Step 2: Test if supplied password is correct
		result = execCmdAndGetReply(VDC_EXEC + " cryptfs verifypw '" + ESC(password)
				+ "'", 1000);
		
		toks = result.split(" ");
		if (!toks[0].equals(VDC_REPLY_OK)) {
			addToLog("VDC cannot veryfypw");
			throw new EncPassChangeException(R.string.verify_error, "");
		}
			
		ec = Integer.parseInt(toks[resultPos]);
		if (ec != 0) {
			addToLog("verifypw doesnt like this password");
			throw new EncPassChangeException(whatToThrow, "");
		}
	}
 
	/**
	 * Executes the command and gets reply, handling errors
	 * 
	 * @param cmd The command to execute
	 * @param maxWait How fast should this command complete
	 * @return Output of the command
	 * @throws EncPassChangeException
	 */
	private String execCmdAndGetReply(String cmd, int maxWait)
			throws EncPassChangeException {

		String str = null;
		
		addToLog("Executing command " + cmd);
		try {
			rootOutStream.writeBytes(cmd + "\n");
			rootOutStream.flush();
			SystemClock.sleep(maxWait);
			addToLog("Reading reply...");
			str = rootResReader.readLine();
			addToLog("Reply: " + str);
			if (str == null) {
				if (rootErrReader.ready())
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
	
	/**
	 * Outputs specified string to LogCat and also updates UI thread
	 * @param str
	 */
	private void addToLog(String str) {
		System.out.println(str);
		publishProgress(new ChangePassProgress(str));
	}

}
