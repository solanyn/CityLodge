package view.dialog;

import java.util.Optional;

import controller.DialogController;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.GridPane;
import model.RoomModel;
import util.DateTime;

public class CompleteMaintenanceDialogView extends Dialog<DateTime> {
	
	DialogController controller;
	RoomModel model;
	
	GridPane grid;
	ButtonType addBT;
	
	TextField completionDate;
	
	public CompleteMaintenanceDialogView(DialogController controller, RoomModel model) {
		this.controller = controller;
		this.model = model;
		
		createView();
		observeAndUpdateView();
		this.setResultConverter(dialogButton -> controller.convertResultToDateTime(dialogButton, addBT, completionDate));
		Optional<DateTime> result = this.showAndWait();
		parseResult(result);
	}
	
	private void createView() {
		this.setTitle("Complete maintenance");
		this.setHeaderText(null);
		this.setResizable(true);
		this.getDialogPane().setPrefSize(400, 125);

		this.grid = new GridPane();
		this.addBT = new ButtonType("Complete", ButtonData.OK_DONE);
		this.getDialogPane().getButtonTypes().addAll(addBT, ButtonType.CANCEL);
		grid.setHgap(10);
		grid.setVgap(10);

		grid.add(new Label("Maintenance completion date:\n(dd/mm/yyyy)"), 0, 0);
		this.completionDate = new TextField();

		grid.add(completionDate, 1, 0);

		this.getDialogPane().setContent(grid);
		completionDate.requestFocus();
	}
	
	private void observeAndUpdateView() {
		// Disable add button if customer id field is empty
		Node addButton = this.getDialogPane().lookupButton(addBT);
		addButton.setDisable(true);

		completionDate.textProperty().addListener((observable, oldValue, newValue) -> {
			addButton.setDisable(newValue.isEmpty());
		});

	}
	
	private void parseResult(Optional<DateTime> result) {
		result.ifPresent(item -> {
			if (item != null) {
				controller.completeMaintenance(item);
			}
		});

	}
}
