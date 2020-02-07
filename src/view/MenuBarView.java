package view;

import controller.MenuController;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import model.RoomModel;

public class MenuBarView extends MenuBar {
	
	MenuController controller;
	RoomModel model;
	Menu fileMenu;
	MenuItem addRoom;
	MenuItem importData;
	MenuItem exportData;
	MenuItem quit;

	public MenuBarView(MenuController controller, RoomModel model) {
		this.controller = controller;
		this.model = model;
		
		this.fileMenu = new Menu("File");
		this.addRoom = new MenuItem("Add room");
		this.importData = new MenuItem("Import data...");
		this.exportData = new MenuItem("Export data...");
		this.quit = new MenuItem("Quit");
		
		addRoom.setOnAction(controller);
		importData.setOnAction(controller);
		exportData.setOnAction(controller);
		quit.setOnAction(controller);

		fileMenu.getItems().addAll(addRoom, importData, exportData, quit);
		getMenus().add(fileMenu);
		setUseSystemMenuBar(true);
	}
}
