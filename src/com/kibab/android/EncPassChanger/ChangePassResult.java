package com.kibab.android.EncPassChanger;

public class ChangePassResult {
	
	private boolean is_success;
	private int message_id;
	private String descr;

	public ChangePassResult(boolean is_success, int message_id, String descr) {
		this.is_success = is_success;
		this.message_id = message_id;
		this.descr = descr;
	}
	
	public boolean isSuccessful() {
		return is_success;
	}
	
	public int getMessageId() {
		return message_id;
	}
	
	public String getMessage() {
		return descr;
	}

}
