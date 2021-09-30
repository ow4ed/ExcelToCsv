package records;

import java.util.Date;

public class BodyRecord extends BaseRecord {

	// data retrieved from file
	private long recordId;
	private Date recordDate;
	private double value;
	
	// defined in GUI
	private char recordType;
	
	public BodyRecord(int recordCode) {
		super(recordCode);
	}

	public long getRecordId() {
		return recordId;
	}

	public void setRecordId(long recordId) {
		this.recordId = recordId;
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