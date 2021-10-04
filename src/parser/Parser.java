package parser;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import records.BodyRecord;
import records.FooterRecord;
import records.HeaderRecord;
 
public class Parser implements Runnable{ 
	
	// gui elements 
	private JPanel panel;
	private JFrame frame;
	//TODO: remove from here
	private JProgressBar bar;
	
	// file path obtained from gui
	private String filePath;
	
	// map that contains all parameters from gui ***
	private Map<String, String> paramaters;
	
	// generated csv File name, set when reading excel sheet
	private String csvFileName;
	
	public Parser() {
		this.bar = new JProgressBar();
	}
	
	// *** setters from gui ***
	
	public void setSection(JPanel panel) {
		this.panel = panel;
	}
	
	public void setFrame(JFrame frame) {
		this.frame = frame;
	}
	
	public void setFilePath(String filePath) {
		this.filePath = filePath;	
	}
	
	public void setParameters(Map<String, String> paramaters) {
		this.paramaters = paramaters;
	}

	@Override
	public void run() {
		System.out.println("Parser - run - Start");
		
		//TODO progressBar
		//swapButtonForProgresBar();
		
		try {
		
			HeaderRecord headerRecord = generateHeaderRecord();
			List<BodyRecord> bodyRecords = generateBodyRecords();
			FooterRecord footerRecord = generateFooterRecord(bodyRecords.size());

			// update file path for the new CSV File
			filePath =  Paths.get(filePath).getParent().toString() +  File.separator + csvFileName + ".csv"; 
			
			generateCsvFile(headerRecord,bodyRecords,footerRecord);
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		//swapProgressBarForButton();
		System.out.println("Parser - run - End");

	}
	

	private HeaderRecord generateHeaderRecord() {
		HeaderRecord headerRecord = new HeaderRecord(Integer.parseInt(this.paramaters.get("headerCode")));
		headerRecord.setName(this.paramaters.get("headerName"));
		//TODO
		headerRecord.setColumnSeparator(this.paramaters.get("headerColumSeparator").charAt(0));
		headerRecord.setEndOfLine(this.paramaters.get("headerEndOfLine").charAt(0));
		return headerRecord;
	}

	private List<BodyRecord> generateBodyRecords() throws IOException {
		
		List<BodyRecord> bodyRecords = new ArrayList<BodyRecord>();
		
		FileInputStream file = new FileInputStream(new File(filePath));
		
		//Create Workbook instance holding reference to .xlsx file
		try (XSSFWorkbook workbook = new XSSFWorkbook(file)) {
			
			
			// get the right sheet to parse
			XSSFSheet sheet = getSheetToParse(workbook);
			
			// generate file path for CSV file
			this.csvFileName = sheet.getSheetName();

			
			// generate the percentage to increment the progress bar
			int numberOfBodyRows = sheet.getPhysicalNumberOfRows() - 2;
			int percentagePerRow = (int) (1/((double) numberOfBodyRows) * 100);

			parseExcelData(bodyRecords, sheet, percentagePerRow);
		}
        
        file.close();
		return bodyRecords;
	}
	
	private XSSFSheet getSheetToParse(XSSFWorkbook workbook) {
		//Get most recent sheet from the workbook
		int index = 0;
		XSSFSheet sheet = workbook.getSheetAt(index);
		// get the right sheet for the file type C 
		if(paramaters.get("bodyRecordType").equals("C")) {
			// 
			while(!sheet.getSheetName().contains("obros")) {
				index++;
				sheet = workbook.getSheetAt(index);
			}
		// get the right sheet for the file type D	
		} else {
			while(!sheet.getSheetName().contains("ciones")) {
				index++;
				sheet = workbook.getSheetAt(index);
			}
		}
		return sheet;
	}


	private void parseExcelData(List<BodyRecord> bodyRecords, XSSFSheet sheet, int percentagePerRow) {
		int[] position = getStartOfDataPosition(sheet);
		
		if(null != position) {
			int x = position[0];
			int y = position[1];
			
			// excel data -> body records
			// while we are reading body type cells
			//TODO: improve getCell(1) and && condition
			while ((int)(sheet.getRow(y).getCell(1).getNumericCellValue()) != Integer.parseInt(this.paramaters.get("footerCode")) && y<100) {
				// while we don't hit an end of line char
				//TODO
				
				Row row = sheet.getRow(y);
				long recordNumber = (long)row.getCell(x).getNumericCellValue();
				//TODO: fiz thise 2
				
				Date recordDate = null;
				String stringDate = row.getCell(x + 2).toString().trim();
				System.out.println(stringDate);
				
				//try dateformat model1
				DateFormat formatModel1 = new SimpleDateFormat("dd-MM-yyyy");
				DateFormat formatModel2 = new SimpleDateFormat("dd-MMM-yyyy", new Locale("en", "EN"));
				try {
					recordDate = formatModel1.parse(stringDate);
				} catch (ParseException e) {
					try {
						recordDate = formatModel2.parse(stringDate);
						
					} catch (ParseException e1) {
						e1.printStackTrace();
					}
				}
				

				double value = row.getCell(x + 3).getNumericCellValue();

				bodyRecords.add(generateBodyRecord(recordNumber,recordDate, value));
				updateProgressBar(percentagePerRow);
				y++;
			}
		}
	}

	private int[] getStartOfDataPosition(XSSFSheet sheet) {
		// this is how data will be always organized in the Excel File
		//  x = 0      x = 1     x = 2     x = 3      x = 4
		// [useless] [useless] [useless] [useless] ['endOfLine']  y = 0
		// [useless] [useful]  [useful]  [useful]  ['endOfLine']  y = 1
		// [useless] [useful]  [useful]  [useful]  ['endOfLine']  y = 2
		// [useless] [useful]  [useful]  [useful]  ['endOfLine']  y = 3
		// [useless] [useless] [useless] [useless] ['endOfLine']  y = 4
				
		
		// find the cell (x,y) where we should start to extract our data
		for (int y = 0; y < sheet.getLastRowNum(); y++) {
			for (int x = 0; x < sheet.getRow(y).getLastCellNum(); x++) {
				Cell cell = sheet.getRow(y).getCell(x);
				if (containsData(cell)) {
					int[] position = new int[2];
					position[0] = x;
					position[1] = y;
					System.out.println("here is my y: " + position[1]);
					System.out.println("here is my x: " + position[0]);
					return position;
				}
			}
		}
		
		return null;
	}


	private boolean containsData(Cell cell) {
		long recordNumber = 0;
		
		// if cell dosen't store an number
		try {
			recordNumber = (long) cell.getNumericCellValue();
		} catch (Exception e) {
			return false;
		}
		
		// if cell number is not big enought 
		// TODO: improve this
		if(recordNumber<1000000) {
			return false;
		}
		
		return true;
	}

	private BodyRecord generateBodyRecord(long recordNumber, Date recordDate, double value) {
		BodyRecord bodyRecord = new BodyRecord(Integer.parseInt(this.paramaters.get("bodyCode")));
		bodyRecord.setName(this.paramaters.get("bodyName"));
		bodyRecord.setRecordId(recordNumber);
		bodyRecord.setRecordType('C');
		bodyRecord.setRecordDate(recordDate);
		if(this.paramaters.get("bodyRecordType").charAt(0) == 'C') {
			bodyRecord.setValue(value);
		} else {
			bodyRecord.setValue(-value);
		}
		bodyRecord.setColumnSeparator(this.paramaters.get("bodyColumSeparator").charAt(0));
		bodyRecord.setEndOfLine(this.paramaters.get("bodyEndOfLine").charAt(0));
		return bodyRecord;
	}
	
	private FooterRecord generateFooterRecord(int numberOfBodyRecords) {
		FooterRecord footerRecord = new FooterRecord(Integer.parseInt(this.paramaters.get("footerCode")));
		footerRecord.setName(this.paramaters.get("footerName"));
		footerRecord.setColumnSeparator(this.paramaters.get("footerColumSeparator").charAt(0));
		footerRecord.setEndOfLine(this.paramaters.get("footerEndOfLine").charAt(0));
		footerRecord.setNumberOfBodyRecords(numberOfBodyRecords);
		return footerRecord;
	}

	private void generateCsvFile(HeaderRecord headerRecord, List<BodyRecord> bodyRecords, FooterRecord footerRecord) {

		try (PrintWriter writer = new PrintWriter(new File(filePath))) {

			SimpleDateFormat csvDateformat = new SimpleDateFormat("yyyy-MM-dd");
			
			StringBuilder sb = new StringBuilder();

			// generate header row
			sb.append(headerRecord.getName());
			sb.append(headerRecord.getColumnSeparator());
			sb.append(headerRecord.getCode());
			sb.append(headerRecord.getColumnSeparator());
			sb.append(csvDateformat.format(headerRecord.getCurrentDate()));
			sb.append(headerRecord.getColumnSeparator());
			sb.append(headerRecord.getEndOfLine() + System.lineSeparator());

			// generate body rows
			for (BodyRecord bodyRecord : bodyRecords) {
				sb.append(bodyRecord.getName());
				sb.append(bodyRecord.getColumnSeparator());
				sb.append(bodyRecord.getCode());
				sb.append(bodyRecord.getColumnSeparator());
				sb.append(bodyRecord.getRecordId());
				sb.append(bodyRecord.getColumnSeparator());
				sb.append(bodyRecord.getRecordType());
				sb.append(bodyRecord.getColumnSeparator());
				sb.append(csvDateformat.format(bodyRecord.getRecordDate()));
				sb.append(bodyRecord.getColumnSeparator());
				sb.append(bodyRecord.getValue());
				sb.append(bodyRecord.getColumnSeparator());
				sb.append(bodyRecord.getEndOfLine() + System.lineSeparator());
			}

			// generate footer row
			sb.append(footerRecord.getName());
			sb.append(footerRecord.getColumnSeparator());
			sb.append(footerRecord.getCode());
			sb.append(footerRecord.getColumnSeparator());
			sb.append(csvDateformat.format(footerRecord.getCurrentDate()));
			sb.append(footerRecord.getColumnSeparator());
			sb.append(footerRecord.getNumberOfBodyRecords());
			sb.append(footerRecord.getColumnSeparator());
			sb.append(footerRecord.getEndOfLine() + System.lineSeparator());

			writer.write(sb.toString());

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}
	
	//  *** methods for gui  ***

	public void swapButtonForProgresBar() {
		this.bar.setValue(0);
		
		this.frame.remove(this.panel);
		
		JPanel convertSection = new JPanel(new BorderLayout());
		convertSection.add(this.bar);
		
		this.frame.add(convertSection);
	}
	
	public void swapProgressBarForButton() {
		//this.panel.add(this.button);
		this.panel.remove(this.bar);
	}
	
	public void updateProgressBar(int percentage) {
		this.bar.setValue(this.bar.getValue() + percentage);
	}

}