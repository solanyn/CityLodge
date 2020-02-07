package init;

import java.sql.SQLException;
import controller.*;
import javafx.application.Application;
import javafx.stage.Stage;
import model.*;
import model.database.DatabaseModel;
import model.exception.DatabaseException;
import view.*;
import view.dialog.ErrorAlertView;
import javafx.scene.Scene;

public class App extends Application {

	public final static double WIDTH = 750;
	public final static double HEIGHT = 500;

	@Override
	public void start(Stage primaryStage) throws ClassNotFoundException, SQLException {
		DatabaseModel dbModel = new DatabaseModel();
		try {
			dbModel.init();
		} catch (DatabaseException de) {
			new ErrorAlertView(de);
		}
		RoomModel  model = new RoomModel();
		RoomController rController = new RoomController(primaryStage, model, dbModel);
		RoomListView rView = new RoomListView(rController, model);
		MenuController mController = new MenuController(primaryStage, rController, model, dbModel);
		
		rView.getChildren().add(mController.getView());
		
		Scene scene = new Scene(rView, WIDTH, HEIGHT);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static void main(String[] args) {
		Application.launch(args);
	}
	
	
}
