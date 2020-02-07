package controller;

import init.App;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.*;
import model.database.DatabaseModel;
import model.exception.*;
import view.*;
import view.dialog.*;

public class RoomController implements EventHandler<MouseEvent> {

	private Stage primaryStage;
	private RoomModel model;
	private RoomListView roomListView;
	private RoomDetailView roomDetailView;
	private DatabaseModel dbModel;
	private DialogController dialogController;

	public RoomController(Stage primaryStage, RoomModel model, DatabaseModel dbModel) {
		this.model = model;
		this.roomListView = new RoomListView(this, model);
		this.primaryStage = primaryStage;
		this.dbModel = dbModel;
		this.dialogController = new DialogController(model, dbModel);
		//roomDetailView not initialized until room is selected
	}

	@Override
	public void handle(MouseEvent event) {
		Object source = event.getSource();

		if (((Button) source).getText().startsWith("<< Back")) {
			
			this.roomListView = new RoomListView(this, model);
			setView(roomListView);
			
		} else if (((Button) source).getText().startsWith("Details >>")) {
			
			model.setCurrentRoom((Room) ((Button) source).getUserData());
			this.roomDetailView = new RoomDetailView(this, model);
			setView(roomDetailView);
			
		} else if (((Button) source).getText().startsWith("Rent")) {
			
			rentRoom(model.getCurrentRoom());
			
			this.roomDetailView = new RoomDetailView(this, model);
			setView(roomDetailView);			
			
		} else if (((Button) source).getText().startsWith("Return")) {
			
			returnRoom(model.getCurrentRoom());
			
			this.roomDetailView = new RoomDetailView(this, model);
			setView(roomDetailView);
			
		} else if (((Button) source).getText().startsWith("Perform")) {
			
			try {
				performMaintenance(model.getCurrentRoom());
				
				this.roomDetailView = new RoomDetailView(this, model);
				setView(roomDetailView);
				
			} catch (MaintenanceException me) {
				new ErrorAlertView(me);
			}
			
		} else if (((Button) source).getText().startsWith("Complete")) {
			
			completeMaintenance(model.getCurrentRoom());
			
			this.roomDetailView = new RoomDetailView(this, model);
			setView(roomDetailView);
			
		}
	}

	public RoomListView getRoomListView() {
		return roomListView;
	}

	public RoomDetailView getRoomDetailView() {
		return roomDetailView;
	}

	public Stage getPrimaryStage() {
		return primaryStage;
	}

	public ListView<Room> getRoomListPane() {
		ListView<Room> roomList = new ListView<Room>(model.getAsObservableList());
		roomList.setCellFactory(param -> new ListCell<Room>() {
			private ImageView imageView = new ImageView();
			private BorderPane pane = new BorderPane();

			@Override
			protected void updateItem(Room item, boolean empty) {
				super.updateItem(item, empty);

				if (empty || item == null || item.getId() == null) {
					setText(null);
					setGraphic(null);
				} else {
					pane.setTop(getRoomTitle(item));

					imageView.setImage(getImage(item, 200, 200));
					BorderPane.setAlignment(imageView, Pos.CENTER_LEFT);
					pane.setLeft(imageView);

					pane.setCenter(getRoomDetail(item));

					Button details = new Button("Details >>");
					BorderPane.setAlignment(details, Pos.BOTTOM_RIGHT);
					details.setUserData(item);
					pane.setRight(details);
					details.setOnMouseClicked(RoomController.this);
					setGraphic(pane);
				}
			}

			private Text getRoomTitle(Room room) {
				Text title;
				Font defaultFont = Font.getDefault();
				if (room.getType().compareTo("Standard") == 0) {
					title = new Text("Standard Room no. " + room.getId().substring(2));
					title.setFont(Font.font(defaultFont.getName(), FontWeight.BOLD, defaultFont.getSize() + 2));
					BorderPane.setAlignment(title, Pos.CENTER);
				} else {
					title = new Text("Suite no. " + room.getId().substring(2));
					title.setFont(Font.font(defaultFont.getName(), FontWeight.BOLD, defaultFont.getSize() + 2));
					BorderPane.setAlignment(title, Pos.CENTER);
				}
				return title;
			}

			private GridPane getRoomDetail(Room room) {
				GridPane pane = new GridPane();
				Font defaultFont = Font.getDefault();

				Text[][] texts = new Text[3][2];
				texts[0][0] = new Text("Room Type: ");
				texts[0][1] = new Text(room.getType());
				texts[1][0] = new Text("Status: ");
				texts[1][1] = new Text(room.getStatus().toString());
				texts[2][0] = new Text("Features: ");
				texts[2][1] = new Text(room.getFeature());

				texts[0][1].setFont(Font.font(defaultFont.getName(), FontWeight.BOLD, defaultFont.getSize()));

				// Different color for each status
				if (room.getStatus() == 'A') {
					texts[1][1].setFill(Color.GREEN);
				} else if (room.getStatus() == 'R') {
					texts[1][1].setFill(Color.RED);
				} else {
					texts[1][1].setFill(Color.ORANGE);
				}

				for (int i = 0; i < 3; i++) {
					for (int j = 0; j < 2; j++) {
						pane.add(texts[i][j], j, i);
					}
				}
				return pane;
			}

			private Image getImage(Room room, double height, double width) {
				final boolean hasNoImage = room.getImagePath().compareTo("images/No_image_available.png") == 0;
				if (hasNoImage) {
					return new Image("file:" + room.getImagePath(), height/2, width, true, true);
				}
				return new Image("file:" + room.getImagePath(), height, width, true, true);
			}

		});

		return roomList;
	}

	private void setView(Pane view) {
		Scene scene = new Scene(view, App.WIDTH, App.HEIGHT);
		primaryStage.setScene(scene);
	}

	private void rentRoom(Room room) {
		new RentRoomDialogView(dialogController, model);
	}
	
	private void returnRoom(Room room) {
		new ReturnRoomDialogView(dialogController, model);
	}

	private void performMaintenance(Room room) throws MaintenanceException {
		new PerformMaintenanceDialogView(dialogController, model);
	}

	private void completeMaintenance(Room room) {
		new CompleteMaintenanceDialogView(dialogController, model);
	}

}
