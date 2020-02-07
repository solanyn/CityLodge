package model.exception;

@SuppressWarnings("serial")
public class DatabaseException extends Exception {
	
	public DatabaseException(String errorMessage) {
		super(errorMessage);
	}
	
}
