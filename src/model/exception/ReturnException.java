package model.exception;

@SuppressWarnings("serial")
public class ReturnException extends Exception {
	
	public ReturnException(String errorMessage) {
		super(errorMessage);
	}
	
}
