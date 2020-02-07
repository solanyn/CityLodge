package model.exception;

@SuppressWarnings("serial")
public class InvalidIdException extends Exception {
	
	public InvalidIdException(String errorMessage) {
		super(errorMessage);
	}

}
