package model;

import model.exception.InvalidIdException;
import model.exception.RentException;
import model.exception.ReturnException;
import util.DateTime;

public class StandardRoom extends Room {
	private final double ONE_BED_RATE = 59;
	private final double TWO_BED_RATE = 99;
	private final double FOUR_BED_RATE = 199;
	private final double LATE_COEF = 1.35;

	private final double WEEKDAY_MIN_DAYS = 2;
	private final double WEEKEND_MIN_DAYS = 3;
	private final double MAX_DAYS = 10;

	public StandardRoom(String roomId, String roomType, int numBedrooms) throws InvalidIdException {
		super(roomId, roomType, numBedrooms);
		setImagePath("images/standardroom.jpg");
		setFeature("air conditioning, cable TV, Wifi, fridge");
		if (!roomId.startsWith("R_")) {
			throw new InvalidIdException("Room ID must start with R_");
		}
	}

	public StandardRoom(String roomId, String roomType, int numBedrooms, String imagePath) throws InvalidIdException {
		this(roomId, roomType, numBedrooms);
		setImagePath(imagePath);
		if (!roomId.startsWith("R_")) {
			throw new InvalidIdException("Room ID must start with R_");
		}
	}

	public StandardRoom(String roomId, int numBedrooms, String roomType, char status, String features, String imagePath)
			throws InvalidIdException {
		this(roomId, roomType, numBedrooms, imagePath);
		setStatus(status);
		if (!roomId.startsWith("R_")) {
			throw new InvalidIdException("Room ID must start with R_");
		}
	}

	@Override
	public void rent(String customerId, DateTime rentDate, int numOfRentDay) throws RentException, InvalidIdException {

		if (numOfRentDay > MAX_DAYS) {
			throw new RentException("Suite can not be rented for more than " + MAX_DAYS + " days!");
		} else if (rentDate.getNameOfDay().equals("Saturday") || rentDate.getNameOfDay().equals("Sunday")) {
			if (numOfRentDay < WEEKEND_MIN_DAYS) {
				throw new RentException("Room must be rented for at least " + WEEKEND_MIN_DAYS + " days on weekends!");
			}
		} else {
			if (numOfRentDay < WEEKDAY_MIN_DAYS) {
				throw new RentException("Room must be rented for at least " + WEEKDAY_MIN_DAYS + " days on weekdays!");
			}
		}

		// If room has been rented before, check if rental date is after last return
		// date
		if (super.getRecords().size() > 0) {
			if (DateTime.diffDays(rentDate,
					super.getRecords().get(super.getRecords().size() - 1).getActualReturnDate()) < 0) {
				throw new RentException("Rental date is before last return date!");
			}
		}
		super.rent(customerId, rentDate, numOfRentDay);
	}

	@Override
	public String toString() {
		return String.format("%s:%d:%s:%c:%s:%s", getId(), getNumBedrooms(), getType(), getStatus(), getFeature(),
				getImagePath());
	}

	@Override
	public String getDetails() {
		String status;
		if (getStatus() == 'A') {
			status = "Available";
		} else if (getStatus() == 'R') {
			status = "Rented";
		} else {
			status = "Maintenance";
		}
		String details = String.format(
				"Room ID:\t%s\nNumber of beds:\t%d\nType:\t%s\nStatus:\t%s\nFeature summary:\t%s\n", getId(),
				getNumBedrooms(), getType(), status, getFeature());
		details += "RENTAL RECORD\n";

		for (int i = super.getRecords().size(); i >= 0; i--) {
			// Skip iteration if hiring record is empty (new record)
			if (super.getRecords().get(i) == null) {
				continue;
			}
			details += super.getRecords().get(i).getDetails();
			if (super.getRecords().size() > 0 && i != 0) {
				details += "--------------------------------------\n";
			}
		}

		return details;
	}

	@Override
	public void returnRoom(DateTime returnDate) throws ReturnException {
		// Check for valid return dates
		if (DateTime.diffDays(returnDate, super.getRecords().getLast().getRentDate()) < 0) {
			throw new ReturnException("Return date is before rent date!");
		} else if (DateTime.diffDays(returnDate, super.getRecords().getLast().getEstimatedReturnDate()) < 0) {
			throw new ReturnException("Return date is before estimated return date!");
		} else if (getStatus() == 'R') {
			setStatus('A');

			// Calculate fees
			double rentalFee, lateFee;

			double[] fees = calcFees(getNumBedrooms(), super.getRecords().getLast().getRentDate(),
					super.getRecords().getLast().getEstimatedReturnDate(), returnDate);
			rentalFee = fees[0];
			lateFee = fees[1];

			// Late fee can't be negative
			if (fees[1] < 0)
				lateFee = 0;

			super.getRecords().getLast().returnRoom(returnDate, rentalFee, lateFee);
			// Move iterator to next record
			if (super.getRecords().size() > super.getMaxRecordSize()) {
				super.getRecords().pop();
			}

		} else {
			throw new ReturnException("Can not return room!");
		}
	}

	public double[] calcFees(int numBedrooms, DateTime rentDate, DateTime estimatedReturnDate,
			DateTime actualReturnDate) {
		double lateFee, rentalFee;

		if (numBedrooms == 1) {
			rentalFee = ONE_BED_RATE * DateTime.diffDays(estimatedReturnDate, rentDate);
			lateFee = ONE_BED_RATE * LATE_COEF * DateTime.diffDays(actualReturnDate, estimatedReturnDate);
		} else if (numBedrooms == 2) {
			rentalFee = TWO_BED_RATE * DateTime.diffDays(estimatedReturnDate, rentDate);
			lateFee = TWO_BED_RATE * LATE_COEF * DateTime.diffDays(actualReturnDate, estimatedReturnDate);
		} else {
			rentalFee = FOUR_BED_RATE * DateTime.diffDays(estimatedReturnDate, rentDate);
			lateFee = FOUR_BED_RATE * LATE_COEF * DateTime.diffDays(actualReturnDate, estimatedReturnDate);
		}

		return new double[] { rentalFee, lateFee };
	}

}
