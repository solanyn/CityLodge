package view;

import java.util.LinkedList;

import controller.RoomController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import model.*;

public class RoomDetailView extends BorderPane {
	
	RoomController controller;
	RoomModel model;

	public RoomDetailView(RoomController controller, RoomModel model) {
		this.controller = controller;
		this.model = model;
		
		setPadding(new Insets(11.5, 12.5, 13.5, 14.5));
		setPaneSections();	
	}
	
	private void setPaneSections() {
		setButtons(model.getCurrentRoom());
		
		// Back button
		Button back = new Button("<< Back");
		BorderPane.setAlignment(back, Pos.BOTTOM_RIGHT);
		back.setOnMouseClicked(controller);
		Pane backPane = new Pane();
		backPane.getChildren().add(back);
		BorderPane.setAlignment(backPane, Pos.TOP_LEFT);
		setLeft(backPane);

		// Image + details
		VBox centerPane = new VBox(10);
		centerPane.setAlignment(Pos.TOP_CENTER);
		ImageView image = new ImageView(getImage(model.getCurrentRoom(), 400, 400));
		ScrollPane scroll = new ScrollPane();
		scroll.setVbarPolicy(ScrollBarPolicy.ALWAYS);
		LinkedList<HiringRecord> records = model.getCurrentRoom().getRecords();
		scroll.setContent(getMoreDetailText(model.getCurrentRoom(), records));
		centerPane.getChildren().addAll(image, scroll);
		setCenter(centerPane);

		// Title
		setTop(getRoomTitle(model.getCurrentRoom()));		
	}
	
	private void setButtons(Room room) {
		VBox buttons = new VBox(4);
		Button rent = new Button("Rent");
		Button ret = new Button("Return");
		Button perfMain = new Button("Perform Maintenance");
		Button compMain = new Button("Complete Maintenance");

		rent.setOnMouseClicked(controller);
		ret.setOnMouseClicked(controller);
		perfMain.setOnMouseClicked(controller);
		compMain.setOnMouseClicked(controller);

		if (room.getStatus() == 'A') {
			ret.setDisable(true);
			perfMain.setDisable(false);
			compMain.setDisable(true);
			rent.setDisable(false);
		} else if (room.getStatus() == 'R') {
			ret.setDisable(false);
			perfMain.setDisable(true);
			compMain.setDisable(true);
			rent.setDisable(true);
		} else if (room.getStatus() == 'M') {
			ret.setDisable(true);
			perfMain.setDisable(true);
			compMain.setDisable(false);
			rent.setDisable(true);
		}

		BorderPane.setAlignment(buttons, Pos.TOP_RIGHT);
		buttons.getChildren().addAll(rent, ret, perfMain, compMain);

		setRight(buttons);
	}

	private GridPane getMoreDetailText(Room room, LinkedList<HiringRecord> records) {
		GridPane texts = new GridPane();
		texts.setAlignment(Pos.TOP_CENTER);

		Font defaultFont = Font.getDefault();
		Text type1 = new Text("Room Type: ");
		Text type2 = new Text(room.getType());
		type2.setFont(Font.font(defaultFont.getName(), FontWeight.BOLD, defaultFont.getSize()));

		Text status1 = new Text("Status: ");
		Text status2 = new Text(room.getStatus().toString());
		if (room.getStatus() == 'A') {
			status2.setFill(Color.GREEN);
		} else if (room.getStatus() == 'R') {
			status2.setFill(Color.RED);
		} else {
			status2.setFill(Color.ORANGE);
		}

		Text features1 = new Text("Features: ");
		Text features2 = new Text(room.getFeature());

		Text numBedrooms1 = new Text("Number of Beds: ");
		Text numBedrooms2 = new Text(Integer.toString(room.getNumBedrooms()));

		texts.add(type1, 0, 0);
		texts.add(type2, 1, 0);
		texts.add(status1, 0, 1);
		texts.add(status2, 1, 1);
		texts.add(features1, 0, 2);
		texts.add(features2, 1, 2);
		texts.add(numBedrooms1, 0, 3);
		texts.add(numBedrooms2, 1, 3);

		if (room instanceof Suite) {
			Text lastMain1 = new Text("Last Maintenance: ");
			Text lastMain2 = new Text(((Suite) room).getLastMaintenance().toString());

			texts.add(lastMain1, 0, 4);
			texts.add(lastMain2, 1, 4);
		}
		if (!room.getRecords().isEmpty()) {
			texts.add(new Text("HIRING RECORD"), 0, 5);
		}

		addRecordTexts(texts, room.getRecords(), 6);
		return texts;
	}

	private void addRecordTexts(GridPane texts, LinkedList<HiringRecord> records, int i) {
		for (HiringRecord record : records) {
			Text[][] recordtexts = new Text[12][2];
			recordtexts[0][0] = new Text("ID: ");
			recordtexts[0][1] = new Text(record.getId());
			recordtexts[1][0] = new Text("Rent Date: ");
			recordtexts[1][1] = new Text(record.getRentDate().toString());
			recordtexts[2][0] = new Text("Estimated Return Date:     ");
			recordtexts[2][1] = new Text(record.getEstimatedReturnDate().toString());
			recordtexts[3][0] = new Text("Actual Return Date: ");
			if (record.getActualReturnDate() == null) {
				recordtexts[3][1] = new Text("No record");
			} else {
				recordtexts[3][1] = new Text(record.getActualReturnDate().toString());
			}
			recordtexts[4][0] = new Text("Rental Fee: ");
			recordtexts[5][0] = new Text("Late Fee: ");
			if (record.getRentalFee() == 0) {
				recordtexts[4][1] = new Text("No record");
			} else {
				recordtexts[4][1] = new Text("$" + String.format("%.2f", record.getRentalFee()));
			}
			if (record.getLateFee() == 0 && record.getActualReturnDate() == null) {
				recordtexts[5][1] = new Text("No record");
			} else {
				recordtexts[5][1] = new Text("$" + String.format("%.2f", record.getLateFee()));
			}

			for (int x = 0; x < 6; x++) {
				for (int y = 0; y < 2; y++) {
					texts.add(recordtexts[x][y], y, x + i);
				}
			}
			i++;
		}
		
	}

	private Text getRoomTitle(Room room) {
		Text title;
		if (room.getType().compareTo("Standard") == 0) {
			title = new Text("Standard Room no. " + room.getId().substring(2));
			Font defaultFont = Font.getDefault();
			title.setFont(Font.font(defaultFont.getName(), FontWeight.BOLD, defaultFont.getSize() + 2));
			BorderPane.setAlignment(title, Pos.CENTER);
		} else {
			title = new Text("Suite no. " + room.getId().substring(2));
			Font defaultFont = Font.getDefault();
			title.setFont(Font.font(defaultFont.getName(), FontWeight.BOLD, defaultFont.getSize() + 2));
			BorderPane.setAlignment(title, Pos.CENTER);
		}
		return title;
	}

	private Image getImage(Room room, double height, double width) {
		final boolean hasNoImage = room.getImagePath().compareTo("images/No_image_available.png") == 0;
		if (hasNoImage) {
			return new Image("file:" + room.getImagePath(), height/2, width/2, true, true);
		}
		return new Image("file:" + room.getImagePath(), height, width, true, true);
	}
	
}
