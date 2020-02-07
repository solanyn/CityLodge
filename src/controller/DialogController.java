package controller;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import model.Room;
import model.RoomModel;
import model.StandardRoom;
import model.Suite;
import model.database.DatabaseModel;
import model.exception.DatabaseException;
import model.exception.InvalidIdException;
import model.exception.MaintenanceException;
import model.exception.RentException;
import model.exception.ReturnException;
import util.DateTime;
import view.dialog.ErrorAlertView;
import view.dialog.InfoAlertView;

public class DialogController {

	RoomModel model;
	DatabaseModel dbModel;

	public DialogController(RoomModel model, DatabaseModel dbModel) {
		this.model = model;
		this.dbModel = dbModel;
	}

	public Room convertResultToRoom(ButtonType dialogBt, ButtonType addBt, ToggleGroup roomTypes, ToggleGroup numBr,
			TextField rIdText, TextField lmText) {
		if (dialogBt == addBt) {
			if (roomTypes.getSelectedToggle() != null) {
				int beds = (int) numBr.getSelectedToggle().getUserData();
				String rType = (String) roomTypes.getSelectedToggle().getUserData();
				String rId = rIdText.getText();

				if (((String) roomTypes.getSelectedToggle().getUserData()).compareTo("Standard") == 0) {

					try {
						if (rId.startsWith("R_")) {
							return new StandardRoom(rId, rType, beds);
						} else {
							throw new InvalidIdException("Room ID must start with R_");
						}
					} catch (InvalidIdException e) {
						new ErrorAlertView(e);
					}

				} else if (((String) roomTypes.getSelectedToggle().getUserData()).compareTo("Suite") == 0) {
					SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

					try {
						DateTime lm = new DateTime(sdf.parse(lmText.getText()));

						if (rId.startsWith("S_")) {
							return new Suite(rId, rType, beds, lm);
						}
						throw new InvalidIdException("Room ID must start with S_");
					} catch (InvalidIdException e) {
						new ErrorAlertView(e);
					} catch (ParseException e) {
						new ErrorAlertView(e);
					}

				}

			}
		}
		return null;
	}

	public DateTime convertResultToDateTime(ButtonType dialogBt, ButtonType addBt, TextField dateText) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

		if (dialogBt == addBt) {
			try {
				DateTime date = new DateTime(sdf.parse(dateText.getText()));
				return date;
			} catch (ParseException pe) {
				new ErrorAlertView(pe);
			} catch (NumberFormatException nfe) {
				new ErrorAlertView(nfe);
			} catch (Exception e) {
				new ErrorAlertView(e);
			}
		}
		return null;
	}

	public String[] convertResultToStringArray(ButtonType dialogBt, ButtonType addBt, TextField cId, TextField rd,
			TextField numDays) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

		String[] items = new String[3];
		if (dialogBt == addBt) {
			try {
				sdf.parse(rd.getText());
				Integer.parseInt(numDays.getText());

				items[0] = cId.getText();
				items[1] = rd.getText();
				items[2] = numDays.getText();
				return items;
			} catch (ParseException pe) {
				new ErrorAlertView(pe);
			} catch (NumberFormatException nfe) {
				new ErrorAlertView(nfe);
			}
		}
		return items;
	}

	public void addRoom(Room room) throws InvalidIdException {
		if (room != null) {
			if (model.getRoomMap().containsKey(room.getId())) {
				throw new InvalidIdException("Room already exists!");
			} else {
				model.addRoom(room);
			}
		}
	}

	public void rentRoom(String[] items) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		try {
			model.getCurrentRoom().rent(items[0], new DateTime(sdf.parse(items[1])), Integer.parseInt(items[2]));
			dbModel.updateStatus(model.getCurrentRoom());
			dbModel.addHiringRecord(model.getCurrentRoom().getRecords().getLast());
			new InfoAlertView("Rent", "Room rented!");
		} catch (SQLException sqle) {
			new ErrorAlertView(sqle);
		} catch (NumberFormatException nfe) {
			new ErrorAlertView(nfe);
		} catch (RentException re) {
			new ErrorAlertView(re);
		} catch (InvalidIdException iie) {
			new ErrorAlertView(iie);
		} catch (ParseException pe) {
			new ErrorAlertView(pe);
		} catch (DatabaseException de) {
			new ErrorAlertView(de);
		}
	}

	public void returnRoom(DateTime date) {
		try {
			model.getCurrentRoom().returnRoom(date);
			dbModel.updateStatus(model.getCurrentRoom());
			dbModel.updateRecord(model.getCurrentRoom().getRecords().getLast());
			new InfoAlertView("Return", "Room returned!");
		} catch (SQLException sqle) {
			new ErrorAlertView(sqle);
		} catch (ReturnException re) {
			new ErrorAlertView(re);
		}
	}

	public void completeMaintenance(DateTime date) {
		try {
			model.getCurrentRoom().completeMaintenance(date);
			dbModel.updateStatus(model.getCurrentRoom());
			new InfoAlertView("Complete maintenance", "Maintenance completed!");
		} catch (SQLException sqle) {
			new ErrorAlertView(sqle);
		} catch (MaintenanceException me) {
			new ErrorAlertView(me);
		}
	}

	public void performMaintenance() {
		try {
			model.getCurrentRoom().performMaintenance();
			dbModel.updateStatus(model.getCurrentRoom());
		} catch (SQLException sqle) {
			new ErrorAlertView(sqle);
		} catch (MaintenanceException me) {
			new ErrorAlertView(me);
		}
	}
}
