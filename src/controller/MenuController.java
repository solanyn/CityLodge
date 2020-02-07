package controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;
import model.RoomModel;
import model.database.DatabaseModel;
import model.exception.InvalidIdException;
import view.*;
import view.dialog.*;

public class MenuController implements EventHandler<ActionEvent> {
	private Stage primaryStage;
	private RoomModel model;
	private DatabaseModel dbModel;
	private MenuBarView view = new MenuBarView(this, model);
	private RoomController rController;
		
	public MenuController(Stage primaryStage, RoomController rController, RoomModel model, DatabaseModel dbModel) {
		this.model = model;
		this.dbModel = dbModel;
		this.primaryStage = primaryStage;
		this.rController = rController;
	}
	
	@Override
	public void handle(ActionEvent event) {
		Object source = event.getSource();
		if (((MenuItem)source).getText().startsWith("Add")) {
			addRoom();
		} else if (((MenuItem)source).getText().startsWith("Import")) {
			
			try {
				importData();
			} catch (NumberFormatException nfe) {
				new ErrorAlertView(nfe);
			} catch (FileNotFoundException fnfe) {
				new ErrorAlertView(fnfe);
			} catch (InvalidIdException iie) {
				new ErrorAlertView(iie);
			} catch (ParseException pe) {
				new ErrorAlertView(pe);
			} catch (SQLException sqle) {
				new ErrorAlertView(sqle);
			}
			
		} else if (((MenuItem)source).getText().startsWith("Export")) {
			
			try {
				exportData();
			} catch (IOException ioe) {
				new ErrorAlertView(ioe);
			}
			
		} else {
			quit();
		}
		
	}
	
	private void addRoom() {
		new AddRoomDialogView(new DialogController(model, dbModel), model);
	}
	
	private void importData() throws NumberFormatException, FileNotFoundException, InvalidIdException, ParseException, SQLException {
		FileChooser fc = new FileChooser();
		fc.setTitle("Open file");
		fc.getExtensionFilters().addAll(new ExtensionFilter("Text Files", "*.txt"));
		File file = fc.showOpenDialog(primaryStage);
		
		if (file != null) {
			model.importText(file);
			RoomListView view = rController.getRoomListView();
			view.updateRoomList(rController.getRoomListPane());
			new InfoAlertView("Import", "Import successful!");
		}
	}

	private void exportData() throws IOException {
		DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setInitialDirectory(new File("src"));
        File file = dirChooser.showDialog(primaryStage);
        
        if (file != null) {
            model.exportText(file);
			new InfoAlertView("Export", "Export successful!");
        }
	}
		
	private void quit() {
		System.exit(0);
	}
	
	public MenuBarView getView() {
		return view;
	}
}
