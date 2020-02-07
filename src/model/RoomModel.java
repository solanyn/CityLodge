package model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import view.dialog.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import model.database.DatabaseModel;
import model.exception.InvalidIdException;
import util.DateTime;

public class RoomModel {

	private ObservableMap<String, Room> roomMap = FXCollections.observableHashMap();
	private ObjectProperty<Room> currentRoom = new SimpleObjectProperty<>(null);
	private ObservableList<Room> roomList = FXCollections.observableArrayList();
	DatabaseModel db;

	public RoomModel() {

		try {
			this.db = new DatabaseModel();
			this.roomMap = db.getRooms();
			refreshList();
			setCurrentRoom(roomList.get(0));
		} catch (ClassNotFoundException cnfe) {
			new ErrorAlertView(cnfe);
		} catch (SQLException sqle) {
			new ErrorAlertView(sqle);
		} catch (InvalidIdException iie) {
			new ErrorAlertView(iie);
		} catch (ParseException pe) {
			new ErrorAlertView(pe);
		} catch (Exception e) {
			new ErrorAlertView(e);
		}

	}

	public ObjectProperty<Room> currentRoomProperty() {
		return currentRoom;
	}

	public Room getCurrentRoom() {
		return currentRoom.get();
	}

	public void setCurrentRoom(Room room) {
		currentRoom.set(room);
	}

	public ObservableMap<String, Room> getRoomMap() {
		return roomMap;
	}

	public void addRoom(Room room) {
		roomMap.put(room.getId(), room);
		refreshList();
	}

	public void refreshList() {
		roomList.clear();
		roomList.addAll(roomMap.values());
	}

	public ObservableList<Room> getAsObservableList() {
		return roomList;
	}

	public void importText(File file)
			throws FileNotFoundException, NumberFormatException, InvalidIdException, ParseException, SQLException {

		HashMap<String, Room> newRooms = new HashMap<>();
		InputStream is = new FileInputStream(file.getAbsolutePath());
		Scanner sc = new Scanner(is);

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		while (sc.hasNextLine()) {
			String[] splitLine = sc.nextLine().split(":");
			ArrayList<String> items = new ArrayList<>();
			for (String item : splitLine) {
				items.add(item);
			}

			final boolean isRoom = items.get(0).split("_").length == 2;
			if (isRoom) {
				String roomId = items.get(0);
				int numBedrooms = Integer.parseInt(items.get(1));
				String roomType = items.get(2);
				char status = items.get(3).charAt(0);
				String features = items.get(4);

				final boolean isStandard = roomId.startsWith("R_") && (roomType.compareTo("Standard") == 0);
				if (isStandard) {
					final boolean hasImage = items.size() == 6;
					if (!hasImage) {
						items.add("images/No_image_available.png");
					}

					String imagePath = items.get(5);
					StandardRoom newRoom = new StandardRoom(roomId, numBedrooms, roomType, status, features, imagePath);
					addRoom(newRoom);
					newRooms.put(newRoom.getId(), newRoom);
				} else {
					final boolean hasImage = items.size() == 7;
					if (!hasImage) {
						items.add("images/No_image_available.png");
					}
					
					String imagePath = items.get(6);
					DateTime lastMaintenance = new DateTime(sdf.parse(items.get(4)));
					Suite suite = new Suite(roomId, numBedrooms, roomType, status, lastMaintenance, features,
							imagePath);
					addRoom(suite);
					newRooms.put(suite.getId(), suite);
				}
			} else {
				String[] recordIdSplit = items.get(0).split("_");
				String roomId = recordIdSplit[0] + "_" + recordIdSplit[1];
				Room currentRoom = roomMap.get(roomId);
				String id = items.get(0);
				DateTime rentDate = new DateTime(sdf.parse(items.get(1)));
				DateTime estimatedReturnDate = new DateTime(sdf.parse(items.get(2)));

				final boolean incompleteRecord = items.get(3).compareTo("none") == 0;
				if (incompleteRecord) {
					HiringRecord record = new HiringRecord(id, rentDate, estimatedReturnDate);

					currentRoom.addRecord(record);
					newRooms.get(currentRoom.getId()).addRecord(record);
				} else {
					DateTime actualReturnDate = new DateTime(sdf.parse(items.get(3)));
					double rentalFee = Double.parseDouble(items.get(4));
					double lateFee = Double.parseDouble(items.get(5));
					HiringRecord record = new HiringRecord(id, rentDate, estimatedReturnDate, actualReturnDate,
							rentalFee, lateFee);

					currentRoom.addRecord(record);
					newRooms.get(currentRoom.getId()).addRecord(record);
				}
			}
		}
		
		sc.close();
		db.addRooms(roomMap, newRooms);
		refreshList();
	}

	public void exportText(File file) throws IOException {
		String path = file.getAbsolutePath() + "/export_data.txt";
		FileWriter fw = new FileWriter(path);
		for (Room room : roomMap.values()) {
			fw.write(room.toString() + "\n");
			for (HiringRecord record : room.getRecords()) {
				fw.write(record.toString() + "\n");
			}
		}
		fw.close();
	}

}
