package model;

import model.exception.InvalidIdException;
import model.exception.MaintenanceException;
import model.exception.RentException;
import model.exception.ReturnException;
import util.DateTime;

public class Suite extends Room {
	private final double SUITE_RATE = 999;
	private final double SUITE_LATE_RATE = 1099;
	private final int MAINTENANCE_INTERVAL_DAYS = 10;

	private DateTime lastMaintenance;

	public Suite(String roomId, String roomType, int numBedrooms, DateTime lastMaintenance) throws InvalidIdException {
		super(roomId, roomType, numBedrooms);
		this.lastMaintenance = lastMaintenance;
		setFeature("large seating area, outdoor balconies");
		setImagePath("images/suite.jpg");
		if (!roomId.startsWith("S_")) {
			throw new InvalidIdException("Room ID must start with S_");
		}
	}
	
	public Suite(String roomId, String roomType, int numBedrooms, DateTime lastMaintenance, String imagePath) throws InvalidIdException {
		this(roomId, roomType, numBedrooms, lastMaintenance);
		setImagePath(imagePath);
		if (!roomId.startsWith("S_")) {
			throw new InvalidIdException("Room ID must start with S_");
		}
	}
	
	public Suite(String roomId, int numBedrooms, String roomType, char status, DateTime lastMaintenance, String features, String imagePath) throws InvalidIdException {
		this(roomId, roomType, numBedrooms, lastMaintenance, imagePath);
		setStatus(status);
		if (!roomId.startsWith("S_")) {
			throw new InvalidIdException("Room ID must start with S_");
		}
	}

	@Override
	public String toString() {
		// roomId:numberOfBeds:roomtype:status:lastMaintenanceDate:featureSummary
		return String.format("%s:%d:%s:%c:%s:%s:%s", getId(), getNumBedrooms(), getType(), getStatus(),
				lastMaintenance.toString(), getFeature(), getImagePath());
	}

	@Override
	public void rent(String customerId, DateTime rentDate, int numOfRentDay) throws RentException, InvalidIdException {
		DateTime estimatedReturnDate = new DateTime(rentDate, numOfRentDay);
		DateTime nextMaintenanceDate = new DateTime(lastMaintenance, MAINTENANCE_INTERVAL_DAYS);
		if (DateTime.diffDays(nextMaintenanceDate, estimatedReturnDate) < 0) {
			throw new RentException("Can not rent suite during scheduled maintenance interval!");
		}

		// If room has been rented before, check if rental date is after last return
		// date
		int length = getRecords().size();
		if (super.getRecords().size() > 0) {
			if (DateTime.diffDays(rentDate, getRecords().get(length - 1).getActualReturnDate()) < 0) {
				throw new RentException("Rent date can not be before last return date!");
			}
		}

		super.rent(customerId, rentDate, numOfRentDay);
	}

	@Override
	public void returnRoom(DateTime returnDate) throws ReturnException {
		// Check return date validity (not before rent date and not before estimated
		// return date)
		if (DateTime.diffDays(returnDate, getRecords().getLast().getRentDate()) < 0) {
			throw new ReturnException("Return date is before rent date!");
		} else if (DateTime.diffDays(returnDate, getRecords().getLast().getEstimatedReturnDate()) < 0) {
			throw new ReturnException("Return date is before estimated return date!");
		} else if (getStatus() == 'R') {
			setStatus('A');

			// Calculate fees
			double rentalFee, lateFee;

			double[] fees = calcFees(getRecords().getLast().getRentDate(),
					getRecords().getLast().getEstimatedReturnDate(), returnDate);
			rentalFee = fees[0];
			lateFee = fees[1];

			// late fee can't be negative
			if (fees[1] < 0)
				lateFee = 0;

			getRecords().getLast().returnRoom(returnDate, rentalFee, lateFee);

			// Move iterator to next record
			if (getRecords().size() > super.getMaxRecordSize()) {
				getRecords().pop();
			}
		} else {
			throw new ReturnException("Can not return room!");
		}
	}

	@Override
	public String getDetails() {
		// Convert status to something more readable
		String status;
		if (getStatus() == 'A') {
			status = "Available";
		} else if (getStatus() == 'R') {
			status = "Rented";
		} else {
			status = "Maintenance";
		}

		String details = String.format(
				"Room ID:\t%s\nNumber of beds:\t%d\nType:\t%s\nStatus:\t%s\nLast maintenance date:\t%s\nFeature summary:\t%s\n",
				getId(), getNumBedrooms(), getType(), status, lastMaintenance.getFormattedDate(), getFeature());
		// Append hiring record to details
		details += "RENTAL RECORD\n";

		for (int i = getRecords().size(); i >= 0; i--) {
			// When iterator moves to next empty record (after room returned), continue loop
			if (getRecords().get(i) == null) {
				continue;
			}
			details += getRecords().get(i).getDetails();
			if (getRecords().size() > 0 && i != 0) {
				details += "--------------------------------------\n";
			}
		}

		return details;
	}

	@Override
	public void completeMaintenance(DateTime completionDate) throws MaintenanceException {
		// Check valid completion date (not before last maintenance)
		if (DateTime.diffDays(completionDate, lastMaintenance) < 0) {
			throw new MaintenanceException("Maintenance completion date is before last maintenance date!");
		} else if (getStatus() == 'M') {
			if (getRecords().isEmpty()) {
				setStatus('A');
				this.lastMaintenance = completionDate;
			} else {
				if (DateTime.diffDays(completionDate,
						getRecords().get(getRecords().size() - 1).getActualReturnDate()) < 0) {
					throw new MaintenanceException("Maintenance completion date is before last return date!");
				} else {
					setStatus('A');
					this.lastMaintenance = completionDate;
				}
			}
		} else
			throw new MaintenanceException("Maintenance completion date is before last maintenance date!");
	}

	private double[] calcFees(DateTime rentDate, DateTime estimatedReturnDate, DateTime actualReturnDate) {
		double lateFee = SUITE_LATE_RATE * DateTime.diffDays(actualReturnDate, estimatedReturnDate);
		double rentalFee = SUITE_RATE * DateTime.diffDays(estimatedReturnDate, rentDate);

		return new double[] { rentalFee, lateFee };
	}

	public DateTime getLastMaintenance() {
		return lastMaintenance;
	}

}
