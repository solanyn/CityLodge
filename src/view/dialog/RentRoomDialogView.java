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

public class RentRoomDialogView extends Dialog<String[]> {
	private DialogController controller;
	private RoomModel model;
	
	GridPane grid;
	ButtonType addBT;
	
	TextField cId;
	TextField rd;
	TextField numDays;
	
	String[] items;
	
	public RentRoomDialogView(DialogController controller, RoomModel model) {
		this.controller = controller;
		this.model = model;
		
		createView();
		observeAndUpdateView();
		this.setResultConverter(dialogButton -> controller.convertResultToStringArray(dialogButton, addBT, cId, rd, numDays));
		Optional<String[]> result = this.showAndWait();
		parseResult(result);
	}
	
	private void createView() {
		this.setTitle("Rent room");
		this.setHeaderText(null);
		this.setResizable(true);
		this.getDialogPane().setPrefSize(350, 175);

		this.grid = new GridPane();
		this.addBT = new ButtonType("Add", ButtonData.OK_DONE);
		this.getDialogPane().getButtonTypes().addAll(addBT, ButtonType.CANCEL);
		grid.setHgap(10);
		grid.setVgap(10);

		grid.add(new Label("Customer ID: "), 0, 0);
		grid.add(new Label("Rent date: \n(dd/mm/yyyy)"), 0, 1);
		grid.add(new Label("Number of days to rent: "), 0, 2);

		this.cId = new TextField();
		this.rd = new TextField();
		this.numDays = new TextField();

		grid.add(cId, 1, 0);
		grid.add(rd, 1, 1);
		grid.add(numDays, 1, 2);

		this.getDialogPane().setContent(grid);
		cId.requestFocus();
	}
	
	private void observeAndUpdateView() {
		// Disable addbutton if customer id field is empty
		Node addButton = this.getDialogPane().lookupButton(addBT);
		addButton.setDisable(true);

		cId.textProperty().addListener((observable, oldValue, newValue) -> {
			addButton.setDisable(newValue.isEmpty());
		});
	}
		
	private void parseResult(Optional<String[]> result) {
		result.ifPresent(items -> {
			if (items.length == 3 && items[0] != null) {
				controller.rentRoom(items);
			}
		});
	}
}
