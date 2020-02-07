package view.dialog;

import controller.DialogController;
import model.RoomModel;

public class PerformMaintenanceDialogView {
	DialogController controller;
	RoomModel model;

	public PerformMaintenanceDialogView(DialogController controller, RoomModel model) {
		this.controller = controller;
		this.model = model;

		controller.performMaintenance();
	}

}
