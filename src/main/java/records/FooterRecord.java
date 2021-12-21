package records;

import java.util.Date;

public class FooterRecord extends BaseRecord {
	
	private final Date currentDate;
	
	// calculate on each time a file is parsed
	private int numberOfBodyRecords;

	public FooterRecord(int recordCode) {
		super(recordCode);
		
		this.currentDate = new Date();
	}
	
	public Date getCurrentDate() {
		return this.currentDate;
	}

	public int getNumberOfBodyRecords() {
		return this.numberOfBodyRecords;
	}

	public void setNumberOfBodyRecords(int numberOfBodyRecords) {
		this.numberOfBodyRecords = numberOfBodyRecords;
	}
	
}
