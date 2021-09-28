package records;

import java.util.Date;

public class BodyRecord extends BaseRecord {

	// data retrieved from file
	private long recordNumber;
	private Date recordDate;
	private double value;
	
	// defined in GUI
	private char recordType;
	
	public BodyRecord(int recordCode) {
		super(recordCode);
	}

	public long getRecordNumber() {
		return recordNumber;
	}

	public void setRecordNumber(long recordNumber) {
		this.recordNumber = recordNumber;
	}

	public Date getRecordDate() {
		return recordDate;
	}

	public void setRecordDate(Date recordDate) {
		this.recordDate = recordDate;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public char getRecordType() {
		return recordType;
	}

	public void setRecordType(char recordType) {
		this.recordType = recordType;
	}
	
}