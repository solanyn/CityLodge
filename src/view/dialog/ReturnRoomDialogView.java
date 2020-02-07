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
import model.exception.ReturnException;
import util.DateTime;

public class ReturnRoomDialogView extends Dialog<DateTime>{
	DialogController controller;
	RoomModel model;
	
	GridPane grid;
	ButtonType addBT;
	
	TextField returnDate;
	
	public ReturnRoomDialogView(DialogController controller, RoomModel model) {
		this.controller = controller;
		this.model = model;
		
		createView();
		observeAndUpdateView();
		setResultConverter(dialogButton -> controller.convertResultToDateTime(dialogButton, addBT, returnDate));
		Optional<DateTime> result = this.showAndWait();
		parseResult(result);
	}
	
	private void createView() {
		this.setTitle("Return room");
		this.setHeaderText(null);
		this.setResizable(true);
		this.getDialogPane().setPrefSize(350, 150);

		this.grid = new GridPane();
		this.addBT = new ButtonType("Return", ButtonData.OK_DONE);
		this.getDialogPane().getButtonTypes().addAll(addBT, ButtonType.CANCEL);
		grid.setHgap(10);
		grid.setVgap(10);

		grid.add(new Label("Return date: \n(dd/mm/yyyy)"), 0, 0);
		this.returnDate = new TextField();

		grid.add(returnDate, 1, 0);

		this.getDialogPane().setContent(grid);
		
		returnDate.requestFocus();
	}
	
	private void observeAndUpdateView() {
		// Disable addbutton if customer id field is empty
		Node addButton = this.getDialogPane().lookupButton(addBT);
		addButton.setDisable(true);

		returnDate.textProperty().addListener((observable, oldValue, newValue) -> {
			addButton.setDisable(newValue.isEmpty());
		});

	}
		
	private void parseResult(Optional<DateTime> result) {
		result.ifPresent(date -> {
			if (date != null) {
				controller.returnRoom(date);
			}
		});
	}
	
}
