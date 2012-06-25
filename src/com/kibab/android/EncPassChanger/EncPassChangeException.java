package com.kibab.android.EncPassChanger;

public class EncPassChangeException extends Exception {

	private String txt;
	private String exc_text;
	
	public EncPassChangeException(String txt, String exc_text) {
		this.txt = txt;
		this.exc_text = exc_text;
	}
	
	public String getErrorText() {
		return new String(txt + ": " + exc_text);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 8744404800489563498L;

}
