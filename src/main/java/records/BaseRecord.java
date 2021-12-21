package records;

public class BaseRecord {
	
	// defined in GUI
	private String name;
	private String columnSeparator;
	private String endOfLine;
	// based on each type of record
	private final int code;

	public BaseRecord(int recordCode) {
		this.code = recordCode;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getColumnSeparator() {
		return columnSeparator;
	}

	public void setColumnSeparator(String columnSeparator) {
		this.columnSeparator = columnSeparator;
	}
	
	public String getEndOfLine() {
		return endOfLine;
	}

	public void setEndOfLine(String endOfLine) {
		this.endOfLine = endOfLine;
	}

	public int getCode() {
		return code;
	}
	
}
