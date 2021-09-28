package records;

public class BaseRecord {
	
	// defined in GUI
	private String name;
	private char columnSeparator;
	private char endOfLine;
	// based on each type of record
	private int code;

	public BaseRecord(int recordCode) {
		this.code = recordCode;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public char getColumnSeparator() {
		return columnSeparator;
	}

	public void setColumnSeparator(char columnSeparator) {
		this.columnSeparator = columnSeparator;
	}
	
	public char getEndOfLine() {
		return endOfLine;
	}

	public void setEndOfLine(char endOfLine) {
		this.endOfLine = endOfLine;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}
	
}
