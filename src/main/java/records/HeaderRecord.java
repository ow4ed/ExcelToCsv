package records;

import java.util.Date;

public class HeaderRecord extends BaseRecord {

	private final Date currentDate;
	
	public HeaderRecord(int recordCode) {
		super(recordCode);
		
		this.currentDate = new Date();
	}
	
	public Date getCurrentDate() {
		return this.currentDate;
	}
	
}
