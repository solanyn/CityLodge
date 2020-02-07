package model.exception;

@SuppressWarnings("serial")
public class MaintenanceException extends Exception {
	
	public MaintenanceException(String errorMessage) {
		super(errorMessage);
	}

}
