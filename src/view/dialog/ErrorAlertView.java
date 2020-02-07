package view.dialog;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class ErrorAlertView {
	public ErrorAlertView(Exception e) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle(e.getClass().getSimpleName());
		alert.setHeaderText(null);
		alert.setContentText(e.getMessage());
		
		alert.showAndWait();
	}

}
