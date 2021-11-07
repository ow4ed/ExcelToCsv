package parser;

import java.awt.Color;
import java.awt.Font;
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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.Border;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import records.BodyRecord;
import records.FooterRecord;
import records.HeaderRecord;
 
public class Parser implements Runnable{ 
	
	private static final SimpleDateFormat CSV_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	
	// GUI elements
	private JLabel information;
	private JButton convertFile;
	private JPanel convertSection;

	// file path obtained from GUI
	private String filePath;
	
	// map that contains all parameters from GUI
	private Map<String, String> paramaters;
	
	// generated CSV File name, set when reading excel sheet
	private String csvFileName;
	
	// *** setters used by GUI ***
	
	public void setInformation(JLabel information) {
		this.information = information;
	}
	
	public void setConvertfile(JButton convertFile) {
		this.convertFile = convertFile;
	}
	
	public void setConvertSection(JPanel convertSection) {
		this.convertSection = convertSection;
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

		JProgressBar bar = swapButtonForProgresBar();
		
		try {
			HeaderRecord headerRecord = generateHeaderRecord();
			List<BodyRecord> bodyRecords = generateBodyRecords(bar);
			FooterRecord footerRecord = generateFooterRecord(bodyRecords.size());
			
			// update file path for the new CSV File
			filePath = Paths.get(filePath).getParent().toString() + File.separator + csvFileName + ".csv";

			generateCsvFile(headerRecord, bodyRecords, footerRecord);
			
			informSuccess();			
		
		// if any unexpected exception happens stop the parse process
		} catch (IOException | ParseException e) {
			informError(bar);
			e.printStackTrace();
		} finally {
			swapProgressBarForButton();
			
			System.out.println("Parser - run - End");

		}
		
	}

	private HeaderRecord generateHeaderRecord() {
		
		HeaderRecord headerRecord = new HeaderRecord(Integer.parseInt(this.paramaters.get("headerCode")));
		headerRecord.setName(this.paramaters.get("headerName"));
		headerRecord.setColumnSeparator(this.paramaters.get("headerColumSeparator"));
		headerRecord.setEndOfLine(this.paramaters.get("headerEndOfLine"));
		
		return headerRecord;
	}

	private List<BodyRecord> generateBodyRecords(JProgressBar bar) throws IOException, ParseException {
		
		List<BodyRecord> bodyRecords = new ArrayList<BodyRecord>();
		
		FileInputStream file = new FileInputStream(new File(filePath));
		
		//Create Workbook instance holding reference to .xlsx file
		try (XSSFWorkbook workbook = new XSSFWorkbook(file)) {
			
			
			// get the right sheet to parse
			XSSFSheet sheet = getSheetToParse(workbook);
			
			// generate file name
			this.csvFileName = sheet.getSheetName();
			
			// generate the percentage to increment the progress bar
			int numberOfBodyRows = sheet.getPhysicalNumberOfRows() - 2;
			int percentagePerRow = (int) (1/((double) numberOfBodyRows) * 100);

			parseExcelData(bodyRecords, sheet, bar, percentagePerRow);
			
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


	private void parseExcelData(List<BodyRecord> bodyRecords, XSSFSheet sheet, JProgressBar bar, int percentagePerRow) throws ParseException {
		// this is how data will be always organized in the Excel File
		//  x = 0      x = 1     x = 2     x = 3      x = 4    x = 5       x = 6
		// [useless] [useless] [useless] ['endOfLine']  		                       y = 0
		// [useless] [useless]>[useful]< [useless]  [useful]  [useful]  ['endOfLine']  y = 1
		// [useless] [useless] [useful]  [useless]  [useful]  [useful]  ['endOfLine']  y = 2
		// [useless] [useless] [useful]  [useless]  [useful]  [useful]  ['endOfLine']  y = 3
		// [useless] [useless] [useless] [useless] ['endOfLine']                       y = 4
				
		// [useful] cell contains a number > Max({HeaderRecord.getCode(), BodyRecord.getCode(), FooterRecord.getCode()})
		int maxCode = Math.max(Integer.parseInt(this.paramaters.get("headerCode")), Integer.parseInt(this.paramaters.get("headerCode")));
		maxCode = Math.max(maxCode, Integer.parseInt(this.paramaters.get("footerCode")));
		
		// find (x,y) of the first [useful] cell
		int[] position = getStartOfDataPosition(sheet, maxCode);
		
		if(null != position) {
			
			int x = position[0];
			int y = position[1];
			
			// while we are reading body type cells, excel data -> body records
			//isStartOfData(Cell cell, int maxCode)
			while (isStartOfData(sheet.getRow(y).getCell(x), maxCode)) {
				
				Row row = sheet.getRow(y);
				
				// get Record Number of the Body Record
				long recordNumber = (long) Double.parseDouble(row.getCell(x).toString().trim());
				
				
				// *** get Record Date of the Body Record ***
				Date recordDate = null;
				
				String stringDate = row.getCell(x + 2).toString().trim();
				// TODO: delete sysout ?
				System.out.println(stringDate);
				
				// try to get date from 2 distinct possible formats
				DateFormat formatModel1 = new SimpleDateFormat("dd-MM-yyyy");
				DateFormat formatModel2 = new SimpleDateFormat("dd-MMM-yyyy", new Locale("en", "EN"));
				try {
					recordDate = formatModel1.parse(stringDate);
				} catch (ParseException eModel1) {
					try {
						recordDate = formatModel2.parse(stringDate);
						
					} catch (ParseException eModel2) {
						throw eModel2;
					}
				}
				
				// get Value of the Body Record
				double value = Double.parseDouble(row.getCell(x + 3).toString().trim());

				bodyRecords.add(generateBodyRecord(recordNumber,recordDate, value));
				updateProgressBar(bar, percentagePerRow);
				/*
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				*/
				y++;
			}
		}
	}

	private int[] getStartOfDataPosition(XSSFSheet sheet, int maxCode) {
		
		for (int y = 0; y < sheet.getLastRowNum(); y++) {
			for (int x = 0; x < sheet.getRow(y).getLastCellNum(); x++) {
				Cell cell = sheet.getRow(y).getCell(x);
				if (isStartOfData(cell, maxCode)) {
					int[] position = new int[2];
					position[0] = x;
					position[1] = y;
					// TODO: delete sysout ?
					System.out.println("Data starts at x: " + position[0]);
					System.out.println("Data starts at y: " + position[1]);
					return position;
				}
			}
		}
		
		return null;
	}

	private boolean isStartOfData(Cell cell, int maxCode) {
		
		try {
			// working around library "feature":
			// cell.toString() returns "1.0" instead of "1" for some reason, we are using Double instead of Long
 			if(Double.parseDouble(cell.toString().trim()) > maxCode) {
 				return true;
 			}
 			
		} catch (NullPointerException | NumberFormatException e) {
			return false;
		}
		
		return false;
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
		bodyRecord.setColumnSeparator(this.paramaters.get("bodyColumSeparator"));
		bodyRecord.setEndOfLine(this.paramaters.get("bodyEndOfLine"));
		
		return bodyRecord;
	}
	
	private FooterRecord generateFooterRecord(int numberOfBodyRecords) {
		
		FooterRecord footerRecord = new FooterRecord(Integer.parseInt(this.paramaters.get("footerCode")));
		footerRecord.setName(this.paramaters.get("footerName"));
		footerRecord.setColumnSeparator(this.paramaters.get("footerColumSeparator"));
		footerRecord.setEndOfLine(this.paramaters.get("footerEndOfLine"));
		footerRecord.setNumberOfBodyRecords(numberOfBodyRecords);
		
		return footerRecord;
	}

	private void generateCsvFile(HeaderRecord headerRecord, List<BodyRecord> bodyRecords, FooterRecord footerRecord) throws FileNotFoundException {

		try (PrintWriter writer = new PrintWriter(new File(filePath))) {
		
			StringBuilder sb = new StringBuilder();

			// generate header row
			sb.append(headerRecord.getName());
			sb.append(headerRecord.getColumnSeparator());
			sb.append(headerRecord.getCode());
			sb.append(headerRecord.getColumnSeparator());
			sb.append(CSV_DATE_FORMAT.format(headerRecord.getCurrentDate()));
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
				sb.append(CSV_DATE_FORMAT.format(bodyRecord.getRecordDate()));
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
			sb.append(CSV_DATE_FORMAT.format(footerRecord.getCurrentDate()));
			sb.append(footerRecord.getColumnSeparator());
			sb.append(footerRecord.getNumberOfBodyRecords());
			sb.append(footerRecord.getColumnSeparator());
			sb.append(footerRecord.getEndOfLine() + System.lineSeparator());

			writer.write(sb.toString());

		} 

	}
	
	//  *** methods for GUI  ***
	
	// *** Select Info Label methods ***

	public void informSelectFile() {
		
		this.information.setText("Drag an Excel File Here or Click 'Select File'");
		Border borderForSelectInfoLabel = BorderFactory.createLineBorder(Color.ORANGE, 15);
		this.information.setBorder(borderForSelectInfoLabel);
	}
	
	private void informSuccess() {
		
		this.information.setText("Success, drag another Excel File Here or Click 'Select File'");
		Border borderForSelectInfoLabel = BorderFactory.createLineBorder(Color.GREEN, 15);
		this.information.setBorder(borderForSelectInfoLabel);
	}
	
	private void informError(JProgressBar bar) {
		
		this.information.setText("ERROR occurred, drag another Excel File Here or Click 'Select File'");		
		Border borderForSelectInfoLabel = BorderFactory.createLineBorder(Color.RED, 15);
		this.information.setBorder(borderForSelectInfoLabel);
		
		bar.setForeground(Color.RED);

	}
	
	// *** Progress Bar methods ***
	
	private JProgressBar swapButtonForProgresBar() {
		
		JProgressBar bar = new JProgressBar();
		bar.setSize(this.convertFile.getWidth(), this.convertFile.getHeight());
		bar.setStringPainted(true);
		bar.setForeground(Color.getHSBColor(116, 194, 0));
		bar.setString("0 %");
		bar.setFont(new Font("Calibri", Font.BOLD, 45));
		bar.setValue(0);
			
		this.convertSection.removeAll();
		this.convertSection.add(bar);

		return bar;
	}
	
	
	private void swapProgressBarForButton() {
		this.convertSection.removeAll();
		this.convertSection.add(this.convertFile);
	}
	
	private void updateProgressBar(JProgressBar bar, int percentage) {
		System.out.println("Current Value:" + bar.getValue());
		System.out.println("Increment Ammount:" + percentage);
		bar.setValue(bar.getValue() + percentage);
		bar.setString(bar.getValue() + " %");
	}
	
}