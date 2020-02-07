package view.dialog;

import java.sql.SQLException;
import java.util.Optional;

import controller.DialogController;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.GridPane;
import model.Room;
import model.RoomModel;
import model.exception.DatabaseException;
import model.exception.InvalidIdException;

public class AddRoomDialogView extends Dialog<Room> {
	
	private GridPane grid = new GridPane();
	private DialogController controller;
	private RoomModel model;
	
	//ToggleGroups
	private ToggleGroup roomTypes = new ToggleGroup();
	private ToggleGroup numBr = new ToggleGroup();
	private Node addButton;
	
	//ToggledSelections
	private RadioButton sixBed = new RadioButton("6");
	private RadioButton oneBed = new RadioButton("1");
	private RadioButton twoBed = new RadioButton("2");
	private RadioButton fourBed = new RadioButton("4");
	
	private ButtonType addBT;
	
	//Labels and inputs
	private Label numBrLabel = new Label("Number of Bedrooms: ");
	private Label lmLabel = new Label("Last Maintenance: \n(dd/mm/yyyy)");
	private Label rIdLabel = new Label("Room ID: ");
	private TextField lmText = new TextField();
	private TextField rIdText = new TextField();
	
	
	public AddRoomDialogView(DialogController controller, RoomModel model) {
		this.controller = controller;
		this.model = model;
		
		createView();
		observeAndUpdateView();
		setResultConverter(dialogButton -> controller.convertResultToRoom(dialogButton, addBT, roomTypes, numBr, rIdText, lmText));
		Optional<Room> result = this.showAndWait();
		parseResult(result);
	}
	
	private void createView() {
		setTitle("Add room");
		setHeaderText("Enter room info: ");
		setResizable(true);
		getDialogPane().setPrefSize(400, 300);

		this.addBT = new ButtonType("Add", ButtonData.OK_DONE);
		getDialogPane().getButtonTypes().addAll(addBT, ButtonType.CANCEL);
		grid.setHgap(10);
		grid.setVgap(10);

		// Room type - constant
		Label rtLabel = new Label("Room Type: ");
		RadioButton suiteRb = new RadioButton("Suite");
		RadioButton srRb = new RadioButton("Standard Room");
		rtLabel.setAlignment(Pos.CENTER_LEFT);
		
		suiteRb.setUserData("Suite");
		srRb.setUserData("Standard");
		suiteRb.setToggleGroup(roomTypes);
		srRb.setToggleGroup(roomTypes);
		
		grid.add(rtLabel, 0, 0);
		grid.add(suiteRb, 1, 0);
		grid.add(srRb, 1, 1);

		getDialogPane().setContent(grid);

		sixBed.setToggleGroup(numBr);
		oneBed.setToggleGroup(numBr);
		twoBed.setToggleGroup(numBr);
		fourBed.setToggleGroup(numBr);
		
		sixBed.setUserData(6);
		oneBed.setUserData(1);
		twoBed.setUserData(2);
		fourBed.setUserData(4);

		//Disable add button initially
		addButton = this.getDialogPane().lookupButton(addBT);
		addButton.setDisable(true);

	}
	
	private void observeAndUpdateView() {
		roomTypes.selectedToggleProperty()
		.addListener((obs, oldValue, newValue) -> {
			if (roomTypes.getSelectedToggle() != null) {
				addButton.setDisable(false);
				if (((String) roomTypes.getSelectedToggle().getUserData()).compareTo("Standard") == 0) {
					// Clear old view if switching selection
					grid.getChildren().removeAll(rIdLabel, rIdText, numBrLabel, sixBed, lmLabel, lmText);
					
					oneBed.setSelected(true);
					grid.add(rIdLabel, 0, 2);
					grid.add(rIdText, 1, 2);
					rIdText.requestFocus();
					grid.add(numBrLabel, 0, 4);
					grid.add(oneBed, 1, 3);
					grid.add(twoBed, 1, 4);
					grid.add(fourBed, 1, 5);
				} else {
					// Clear old view if switching selection
					grid.getChildren().removeAll(rIdLabel, rIdText, numBrLabel, oneBed, twoBed, fourBed);

					sixBed.setSelected(true);
					grid.add(rIdLabel, 0, 2);
					grid.add(rIdText, 1, 2);
					rIdText.requestFocus();
					grid.add(numBrLabel, 0, 3);
					grid.add(sixBed, 1, 3);

					grid.add(lmLabel, 0, 4);
					grid.add(lmText, 1, 4);
				}
			}
		});
		
		roomTypes.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
		    addButton.setDisable(!newValue.isSelected());
		});
	}

	private void parseResult(Optional<Room> result) {
		result.ifPresent(room -> {
			try {
				controller.addRoom(room);
			} catch (InvalidIdException iie) {
				new ErrorAlertView(iie);
			}
		});
	}
	
}
