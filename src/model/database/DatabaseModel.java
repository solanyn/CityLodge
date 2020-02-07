package model.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import model.HiringRecord;
import model.Room;
import model.StandardRoom;
import model.Suite;
import model.exception.DatabaseException;
import model.exception.InvalidIdException;
import util.DateTime;
import view.dialog.ErrorAlertView;

public class DatabaseModel {
	private Connection connection;

	// Inner singleton class for single database connection
	private static class Database {
		private static Database instance = null;
		private String database;

		private Connection connection;

		private Database() throws SQLException, ClassNotFoundException {
			Class.forName("org.hsqldb.jdbc.JDBCDriver");
			database = "citylodge";
			connection = DriverManager.getConnection("jdbc:hsqldb:file:database/" + database, "SA", "");
		}

		private static Database getInstance() throws ClassNotFoundException, SQLException {
			if (instance == null)
				instance = new Database();
			return instance;
		}

		private Connection getConnection() {
			return connection;
		}
	}

	public DatabaseModel() throws SQLException, ClassNotFoundException {
		this.connection = Database.getInstance().getConnection();
	}

	public void init() throws ClassNotFoundException, SQLException, DatabaseException {
		DatabaseMetaData meta = connection.getMetaData();
		boolean roomTableExists = false;
		boolean hiringrecordTableExists = false;
		
		ResultSet tables = meta.getTables(null, null, "%", null);
		while(tables.next()) {
			String table = tables.getString("Table_NAME");
			if (table.compareTo("ROOMS") == 0) {
				roomTableExists = true;
			} else if (table.compareTo("HIRINGRECORD") == 0) {
				hiringrecordTableExists = true;
			}
		}
		
		if (!(roomTableExists && hiringrecordTableExists)) {
			createHiringRecordTable();
			createRoomsTable();
			insertInitialRecords();
		} else if (!roomTableExists) {
			createRoomsTable();
		} else if (!hiringrecordTableExists) {
			createHiringRecordTable();
		}

	}

	public void createRoomsTable() throws SQLException, ClassNotFoundException, DatabaseException {
		try (Statement state = connection.createStatement()) {
			String query = "CREATE TABLE rooms (" + "roomId VARCHAR(5) NOT NULL," + "roomType VARCHAR(20) NOT NULL,"
					+ "numBedroom INT NOT NULL," + "status VARCHAR(1) NOT NULL," + "lastMaintenance VARCHAR(10),"
					+ "imagePath VARCHAR(50) NOT NULL," + "PRIMARY KEY (roomId))";

			int result = state.executeUpdate(query);

			if (result == -1) {
				throw new DatabaseException("Could not create ROOMS table!");
			}
		}
	} 

	public void createHiringRecordTable() throws SQLException, ClassNotFoundException, DatabaseException {
		try (Statement state = connection.createStatement()) {
			String query = "CREATE TABLE hiringrecord (" + "id VARCHAR(30) NOT NULL," + "rentDate VARCHAR(10) NOT NULL,"
					+ "estimatedReturnDate VARCHAR(10) NOT NULL," + "actualReturnDate VARCHAR(10)," + "rentalFee INT,"
					+ "lateFee INT," + "PRIMARY KEY (id))";

			state.executeUpdate(query);
		}
	}

	public void dropHiringRecordTable() throws SQLException, ClassNotFoundException, DatabaseException {
		try (Statement state = connection.createStatement()) {
			String query = "DROP TABLE hiringrecord";
			state.executeUpdate(query);
		}
	}

	public void dropRoomsTable() throws SQLException, ClassNotFoundException, DatabaseException {
		try (Statement state = connection.createStatement()) {
			String query = "DROP TABLE rooms";
			state.executeUpdate(query);
		}
	}

	public void insertInitialRecords() throws SQLException, ClassNotFoundException {
		try (Statement state = connection.createStatement()) {

			String insertRooms = "INSERT INTO rooms VALUES "
					+ "('R_111', 'Standard', 1, 'A', null, 'images/standardroom.jpg'),"
					+ "('R_222', 'Standard', 2, 'A', null, 'images/standardroom.jpg'),"
					+ "('R_333', 'Standard', 4, 'A', null, 'images/standardroom.jpg'),"
					+ "('S_111', 'Suite', 6, 'A', '10/10/2019', 'images/suite.jpg'),"
					+ "('S_222', 'Suite', 6, 'A', '10/10/2019', 'images/suite.jpg'),"
					+ "('S_333', 'Suite', 6, 'A', '10/10/2019', 'images/suite.jpg')";
			String insertRecords = "INSERT INTO hiringrecord VALUES "
					+ "('S_333_CUS111_01102019', '01/10/2019', '03/10/2019', '03/10/2019', 999, 0),"
					+ "('R_111_CUS222_02102019', '02/10/2019', '06/10/2019', '07/10/2019', 199, 269)";

			state.executeUpdate(insertRooms);
			state.executeUpdate(insertRecords);
			connection.commit();
		}
	}

	public void updateStatus(Room room) throws SQLException {
		String query = String.format("UPDATE rooms SET status = '%s' WHERE roomId LIKE '%s'", 
				room.getStatus(), room.getId());
		try (Statement state = connection.createStatement()) {
			state.executeUpdate(query);
			connection.commit();
		}
	}

	public void updateRecord(HiringRecord record) throws SQLException {
		String query = String.format("UPDATE hiringrecord SET actualReturnDate = '%s', rentalFee = %f, lateFee =  %f WHERE id LIKE '%s'", 
				record.getActualReturnDate().toString(), record.getRentalFee(), record.getLateFee(), record.getId());
		try (Statement state = connection.createStatement()) {
			state.executeUpdate(query);
			connection.commit();
		}
	}

	public void addRoom(Room room) throws SQLException, DatabaseException {
		try (Statement state = connection.createStatement()) {
			String query = String.format("INSERT INTO rooms VALUES ('%s', '%s', %d, '%c', ", 
					room.getId(), room.getType(), room.getNumBedrooms(), room.getStatus());
			
			if (room.getType().compareTo("Suite") == 0) {
				query += String.format("'%s', ", ((Suite)room).getLastMaintenance().toString()); 
			} else {
				query += "null, ";
			}
			query += String.format("'%s')", room.getImagePath());

			int result = state.executeUpdate(query);

			connection.commit();
			if (result == -1) {
				throw new DatabaseException("Room could not be added to database!");
			}
		}
	}

	public LinkedList<HiringRecord> getRecords() throws ParseException {
		LinkedList<HiringRecord> list = new LinkedList<>();

		try (Statement state = connection.createStatement()) {
			String query = "SELECT * FROM hiringrecord";
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			try (ResultSet r = state.executeQuery(query)) {
				while (r.next()) {
					String id = r.getString("id");
					DateTime rentDate = new DateTime(sdf.parse(r.getString("rentDate")));
					DateTime estimatedReturnDate = new DateTime(sdf.parse(r.getString("estimatedReturnDate")));
					if (r.getString("actualReturnDate") != null) {
						DateTime actualReturnDate = new DateTime(sdf.parse(r.getString("actualReturnDate")));
						double rentalFee = r.getDouble("rentalFee");
						double lateFee = r.getDouble("lateFee");
						list.add(new HiringRecord(id, rentDate, estimatedReturnDate, actualReturnDate, rentalFee,
								lateFee));
					} else {
						list.add(new HiringRecord(id, rentDate, estimatedReturnDate));
					}
				}
			} catch (SQLException e) {
				new ErrorAlertView(e);
			}
		} catch (SQLException e) {
			new ErrorAlertView(e);
		}
		return list;
	}

	public ObservableMap<String, Room> getRooms() throws InvalidIdException, ParseException {
		ObservableMap<String, Room> map = FXCollections.observableHashMap();
		LinkedList<HiringRecord> allRecords = getRecords();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

		try (Statement state = connection.createStatement()) {
			String query = "SELECT * FROM rooms";

			try (ResultSet r = state.executeQuery(query)) {
				while (r.next()) {
					String roomId = r.getString("roomId");
					String roomType = r.getString("roomType");
					char status = r.getString("status").charAt(0);
					int numBedrooms = r.getInt("numBedroom");
					String imagePath = r.getString("imagePath");
					
					if (roomType.compareTo("Standard") == 0) {
						map.put(roomId, new StandardRoom(roomId, roomType, numBedrooms, imagePath));
						map.get(roomId).setStatus(status);
					} else {
						DateTime lastMaintenance = new DateTime(sdf.parse(r.getString("lastMaintenance")));
						map.put(roomId, new Suite(roomId, roomType, numBedrooms, lastMaintenance, imagePath));
						map.get(roomId).setStatus(status);
					}
				}

				for (Room room : map.values()) {
					for (HiringRecord record : allRecords) {
						if (record.getId().startsWith(room.getId())) {
							room.addRecord(record);
						}
					}
				}

			} catch (SQLException e) {
				new ErrorAlertView(e);
			}
		} catch (SQLException e) {
			new ErrorAlertView(e);
		}
		return map;
	}

	public void addHiringRecord(HiringRecord record) throws DatabaseException {
		try (Statement state = connection.createStatement()) {
			StringBuilder query = new StringBuilder(String.format("INSERT INTO hiringrecord VALUES ('%s', '%s', '%s', ", 
					record.getId(), record.getRentDate().toString(), record.getEstimatedReturnDate().toString()));
			if (record.getActualReturnDate() == null) {
				query.append("null, null, null)");
			} else {
				query.append(String.format("'%s', %d, %d)", 
						record.getActualReturnDate().toString(), record.getRentalFee(), record.getLateFee()));
			}

			int result = state.executeUpdate(query.toString());
			
			if (result == -1) {
				throw new DatabaseException("Could not add hiring record to database!");
			}
			
			connection.commit();

		} catch (SQLException e) {
			new ErrorAlertView(e);
		}

	}

	public void addRooms(ObservableMap<String, Room> allRooms, HashMap<String, Room> newRooms)
			throws SQLException, InvalidIdException, ParseException {
		StringBuilder insertRooms = new StringBuilder("INSERT INTO rooms VALUES ");
		StringBuilder insertRecords = new StringBuilder("INSERT INTO hiringrecord VALUES ");
		ObservableMap<String, Room> dbRooms = getRooms();

		for (Room room : newRooms.values()) {
			final boolean isSuite = room.getType().compareTo("Suite") == 0;
			if (isSuite) {
				if (dbRooms.containsKey(room.getId())) {
					StringBuilder updateRoom = new StringBuilder("UPDATE rooms SET ");
					updateRoom.append(String.format(
							"roomType = '%s', numBedroom = %d, status = '%c', lastMaintenance = '%s', imagePath = '%s' ",
							room.getType(), room.getNumBedrooms(), room.getStatus(),
							((Suite) room).getLastMaintenance().getFormattedDate(), room.getImagePath()));
					updateRoom.append(String.format("WHERE roomId LIKE '%s'", room.getId()));
					try (Statement state = connection.createStatement()) {
						state.executeUpdate(updateRoom.toString());
					}
				} else {
					insertRooms.append(String.format("('%s', '%s', %d, '%c', '%s', '%s'),", room.getId(),
							room.getType(), room.getNumBedrooms(), room.getStatus(),
							((Suite) room).getLastMaintenance().getFormattedDate(), room.getImagePath()));
				}
			} else {
				if (dbRooms.containsKey(room.getId())) {
					StringBuilder updateRoom = new StringBuilder("UPDATE rooms SET ");
					updateRoom.append(String.format(
							"roomType = '%s', numBedroom = %d, status = '%c', lastMaintenance = null, imagePath = '%s' ",
							room.getType(), room.getNumBedrooms(), room.getStatus(), room.getImagePath()));
					updateRoom.append(String.format("WHERE roomId LIKE '%s'", room.getId()));
					try (Statement state = connection.createStatement()) {
						state.executeUpdate(updateRoom.toString());
					}
				} else {
					insertRooms.append(String.format("('%s', '%s', %d, '%c', null, '%s'),", room.getId(),
							room.getType(), room.getNumBedrooms(), room.getStatus(), room.getImagePath()));
				}
			}

			for (HiringRecord record : room.getRecords()) {
				final boolean isComplete = record.getActualReturnDate() != null;
				final boolean recExists = allRooms.get(room.getId()).getRecords().contains(record);
				if (!isComplete) {
					if (!recExists) {
						insertRecords.append(String.format("('%s', '%s', '%s', null, null, null),", record.getId(),
								record.getRentDate(), record.getEstimatedReturnDate()));
					}
				} else {
					if (!recExists) {
						insertRecords.append(String.format("('%s', '%s', '%s', '%s', %d, %d),", record.getId(),
								record.getRentDate(), record.getEstimatedReturnDate(), record.getActualReturnDate(),
								record.getRentalFee(), record.getLateFee()));
					}
				}
			}

		}

		// Remove final comma
		insertRooms.deleteCharAt(insertRooms.length() - 1);
		insertRecords.deleteCharAt(insertRecords.length() - 1);

		if (insertRecords.toString().endsWith(")")) {
			try (Statement state = connection.createStatement()) {
				state.executeUpdate(insertRecords.toString());
			}
		}
		
		if (insertRooms.toString().endsWith(")")) {
			try (Statement state = connection.createStatement()) {
				state.executeUpdate(insertRooms.toString());
			}
		}
		connection.commit();
	}
}
