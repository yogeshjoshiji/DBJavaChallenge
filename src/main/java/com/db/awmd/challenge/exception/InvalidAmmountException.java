package com.db.awmd.challenge.exception;


public class InvalidAmmountException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7947189583145486822L;

	public InvalidAmmountException(){
		
	}
	
	public InvalidAmmountException(String message){
		super(message);
	}

}
