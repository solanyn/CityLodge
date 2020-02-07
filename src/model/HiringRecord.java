package model;

import model.exception.InvalidIdException;
import model.exception.ReturnException;
import util.DateTime;

public class HiringRecord {
	private String id;
	private DateTime rentDate;
	private DateTime estimatedReturnDate;
	private DateTime actualReturnDate;
	private double rentalFee;
	private double lateFee;

	public HiringRecord(String roomId, String customerId, DateTime rentDate, DateTime estimatedReturnDate) throws InvalidIdException {
		if (!customerId.startsWith("CUS")) {
			throw new InvalidIdException("Customer ID must start with CUS");
		} 
		this.id = roomId + "_" + customerId + "_" + rentDate.getEightDigitDate();
		this.rentDate = rentDate;
		this.estimatedReturnDate = estimatedReturnDate;
	}

	public HiringRecord(String id, DateTime rentDate, DateTime estimatedReturnDate) {
		this.id = id;
		this.rentDate = rentDate;
		this.estimatedReturnDate = estimatedReturnDate;
	}

	public HiringRecord(String id, DateTime rentDate, DateTime estimatedReturnDate, DateTime actualReturnDate,
			double rentalFee, double lateFee) {
		this.id = id;
		this.rentDate = rentDate;
		this.estimatedReturnDate = estimatedReturnDate;
		this.actualReturnDate = actualReturnDate;
		this.rentalFee = rentalFee;
		this.lateFee = lateFee;
	}

	public String toString() {
		// recordId:rentDate:estimatedReturnDate:actualReturnDate:rentalFee:lateFee
		if (actualReturnDate == null) {
			return String.format("%s:%s:%s:none:none:none", id, rentDate.toString(), estimatedReturnDate.toString());
		} else
			return String.format("%s:%s:%s:%s:%.2f:%.2f", id, rentDate.toString(), estimatedReturnDate.toString(),
					actualReturnDate.toString(), rentalFee, lateFee);
	}

	public String getDetails() {
		// Currently rented, incomplete hiring record
		if (actualReturnDate == null) {
			return String.format("Record ID:\t%s\nRentDate:\t%s\nEstimated Return Date:\t%s\n", id, rentDate.toString(),
					estimatedReturnDate.toString());
		} else
			return String.format(
					"Record ID:\t%s\nRentDate:\t%s\nEstimated Return Date:\t%s\nActual Return Date:\t%s\nRentalFee:\t%.2f\nLate Fee:\t%.2f\n",
					id, rentDate.toString(), estimatedReturnDate.toString(), actualReturnDate.toString(), rentalFee,
					lateFee);
	}

	public DateTime getRentDate() {
		return rentDate;
	}

	public void returnRoom(DateTime actualReturnDate, double rentalFee, double lateFee) throws ReturnException {
		// Complete hiring record when called
		this.actualReturnDate = actualReturnDate;
		this.rentalFee = rentalFee;
		this.lateFee = lateFee;
	}

	public DateTime getEstimatedReturnDate() {
		return estimatedReturnDate;
	}

	public DateTime getActualReturnDate() {
		return actualReturnDate;
	}

	public String getId() {
		return id;
	}

	public double getRentalFee() {
		return rentalFee;
	}

	public double getLateFee() {
		return lateFee;
	}

}
