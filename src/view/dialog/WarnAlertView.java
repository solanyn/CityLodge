package view.dialog;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class WarnAlertView {
	public WarnAlertView(String title, String message) {
		Alert alert = new Alert(AlertType.WARNING);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);

		alert.showAndWait();
	}

}
