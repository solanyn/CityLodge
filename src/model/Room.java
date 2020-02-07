package model;

import java.util.LinkedList;

import model.exception.InvalidIdException;
import model.exception.MaintenanceException;
import model.exception.RentException;
import model.exception.ReturnException;
import util.DateTime;

public abstract class Room {
	
	private final int MAX_RECORD_SIZE = 10;
	
	private String id;
	private int numBedrooms;
	private String feature;
	private String type;
	private Character status;
	private String imagePath;

	private LinkedList<HiringRecord> records = new LinkedList<HiringRecord>();

	public Room(String id, String type, int numBedrooms) {
		this.id = id;
		this.type = type;
		this.numBedrooms = numBedrooms;
		status = 'A';
	}

	public void rent(String customerId, DateTime rentDate, int numOfRentDay) throws RentException, InvalidIdException {
		if (status == 'A') {
			if (!customerId.startsWith("CUS")) {
				throw new RentException("Customer ID must start with CUS");
			}
			records.add(new HiringRecord(this.id, customerId, rentDate, new DateTime(rentDate, numOfRentDay)));
			setStatus('R');
		} else {
			throw new RentException("Room is not available!");
		}
	}

	public abstract void returnRoom(DateTime returnDate) throws ReturnException;

	public void performMaintenance() throws MaintenanceException {
		if (status == 'A') {
			setStatus('M');
		} else {
			throw new MaintenanceException("Room is not available!");
		}
	}

	public void completeMaintenance(DateTime completionDate) throws MaintenanceException {
		if (status == 'M') {
			if (records.isEmpty()) {
				setStatus('A');
			} else {
				if (DateTime.diffDays(completionDate, records.get(records.size() - 1).getActualReturnDate()) < 0) {
					throw new MaintenanceException("Maintenance completion date is before last return date!");
				} else {
					setStatus('A');
				}
			}
		} else {
			throw new MaintenanceException("Room is not under maintenance!");
		}
	}

	public abstract String toString();

	public abstract String getDetails();
	
	public void setFeature(String feature) {
		this.feature = feature;
	}

	public Character getStatus() {
		return status;
	}

	public String getId() {
		return id;
	}

	public int getNumBedrooms() {
		return numBedrooms;
	}

	public String getFeature() {
		return feature;
	}

	public String getType() {
		return type;
	}

	public void setStatus(Character s) {
		this.status = s;
	}

	public LinkedList<HiringRecord> getRecords() {
		return records;
	}
	
	public void addRecord(HiringRecord record) {
		if (records.size() > getMaxRecordSize()) {
			records.pop();
		} 
		if (!records.contains(record)) {
			records.add(record);
		}
	}

	public int getMaxRecordSize() {
		return MAX_RECORD_SIZE;
	}
	
	public String getImagePath() {
		return imagePath;
	}
	
	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}
}
