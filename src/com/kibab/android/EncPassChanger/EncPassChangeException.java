package com.kibab.android.EncPassChanger;

public class EncPassChangeException extends Exception {

	private int txt_id;
	private String exc_text;
	
	public EncPassChangeException(int txt_id, String exc_text) {
		this.txt_id = txt_id;
		this.exc_text = exc_text;
	}
	
	//XXX: Should fix this
	public String getErrorText() {
		return new String(": " + exc_text);
	}
	
	public int getMessageCode() {
		return txt_id;
	}
	
	public String getMessage() {
		return exc_text;
	}
 
	/**
	 * 
	 */
	private static final long serialVersionUID = 8744404800489563498L;

}
