package com.kibab.android.EncPassChanger;

public class ChangePassParams {
	private String oldPass;
	private String newPass;
	private EncPassChangerActivity resultcb;
	
	public ChangePassParams(String oldPass, String newPass, EncPassChangerActivity resultCb) {
		this.oldPass = oldPass;
		this.newPass = newPass;
		this.resultcb = resultCb;
	}
	
	public String getOldPassword() {
		return this.oldPass;
	}

	public String getNewPassword() {
		return this.newPass;
	}
	
	public EncPassChangerActivity getCallingActivity() {
		return this.resultcb;
	}
}
