package view;

import controller.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import model.*;

public class RoomListView extends BorderPane {
	RoomModel model;
	RoomController controller;
	
	Text viewTitle;
	
	public RoomListView(RoomController controller, RoomModel model) {
		this.model = model;
		this.controller = controller;
		this.viewTitle = new Text("Room List");
		
		setPadding(new Insets(11.5, 12.5, 13.5, 14.5));
		setTitle();
		setRoomList();
	}
			
	private void setTitle() {
		Font defaultFont = Font.getDefault();
		viewTitle.setFont(Font.font(defaultFont.getName(), FontWeight.BOLD, defaultFont.getSize() + 4));
		BorderPane.setAlignment(viewTitle, Pos.CENTER);
		
		setTop(viewTitle);
	}
	
	private void setRoomList() {
		ListView<Room> list = controller.getRoomListPane();
		setCenter(list);
	}
	
	public void updateRoomList(ListView<Room> list) {
		setCenter(list);
	}
	
}
