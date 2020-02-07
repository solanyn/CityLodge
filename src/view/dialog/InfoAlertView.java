package view.dialog;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class InfoAlertView {
	public InfoAlertView(String title, String message) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);

		alert.showAndWait();
	}
}
