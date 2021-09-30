package parser;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.apache.poi.ss.usermodel.Cell;
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
		swapButtonForProgresBar();
		
		try {
		
			HeaderRecord headerRecord = generateHeaderRecord();
			List<BodyRecord> bodyRecords = generateBodyRecords();
			FooterRecord footerRecord = generateFooterRecord(bodyRecords.size());

			// update file path for the new CSV File
			filePath =  Paths.get(filePath).getParent().toString() + csvFileName + "csv";
			
			generateCsvFile(headerRecord,bodyRecords,footerRecord);
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		//swapProgressBarForButton();
		System.out.println("Parser - run - End");

	}
	
	private HeaderRecord generateHeaderRecord() {
		HeaderRecord headerRecord = new HeaderRecord(Integer.parseInt(this.paramaters.get("headerCode")));
		headerRecord.setName(this.paramaters.get("headerCode"));
		headerRecord.setColumnSeparator(this.paramaters.get("headerColumSeparator").charAt(0));
		headerRecord.setEndOfLine(this.paramaters.get("headerEndOfLine").charAt(0));
		return headerRecord;
	}
	
	private FooterRecord generateFooterRecord(int numberOfBodyRecords) {
		FooterRecord footerRecord = new FooterRecord(Integer.parseInt(this.paramaters.get("footerCode")));
		footerRecord.setName(this.paramaters.get("footerName"));
		footerRecord.setColumnSeparator(this.paramaters.get("footerColumSeparator").charAt(0));
		footerRecord.setEndOfLine(this.paramaters.get("footerEndOfLineLabel").charAt(0));
		footerRecord.setNumberOfBodyRecords(numberOfBodyRecords);
		return footerRecord;
	}

	private List<BodyRecord> generateBodyRecords() throws IOException {
		
		List<BodyRecord> bodyRecords = new ArrayList<BodyRecord>();
		
		FileInputStream file = new FileInputStream(new File(filePath));
		
		//Create Workbook instance holding reference to .xlsx file
		try (XSSFWorkbook workbook = new XSSFWorkbook(file)) {
			
			//Get most recent sheet from the workbook
			int index = 0;
			XSSFSheet sheet = workbook.getSheetAt(index);
			// get the right sheet for the file type C 
			if(paramaters.get("bodyRecordType").equals("C")) {
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
	
	private void parseExcelData(List<BodyRecord> bodyRecords, XSSFSheet sheet, int percentagePerRow) {
		int x = 0;
		int y = 0;
		// find the cell (x,y) where we should start to extract our data
		for (int i = 0; i < sheet.getLastRowNum(); i++) {
			for (int j = 0; j < sheet.getRow(i).getLastCellNum(); j++) {
				Cell cell = sheet.getRow(i).getCell(j);
				if (containsData(cell)) {
					x = cell.getRowIndex();
					y = cell.getColumnIndex();
					break;// TODO check if this exitrs the both loops
				}
			}
		}

		// excel data -> body records
		// TODO: check if what we are reading from cell is empty
		while (x < sheet.getLastRowNum()) {
			while (y < sheet.getRow(x).getLastCellNum()) {
				long recordNumber = 0;
				Date recordDate = null;
				double value = 0;

				bodyRecords.add(generateBodyRecord(recordNumber, recordDate, value));
				updateProgressBar(percentagePerRow);
				y++;
			}
			x++;
		}

	}


	private boolean containsData(Cell cell) {
		// TODO Auto-generated method stub
		return false;
	}


	private BodyRecord generateBodyRecord(long recordNumber, Date recordDate, double value) {
		BodyRecord bodyRecord = new BodyRecord(Integer.parseInt(this.paramaters.get("bodyCode")));
		bodyRecord.setName(this.paramaters.get("bodyName"));
		bodyRecord.setRecordId(recordNumber);
		bodyRecord.setRecordType(this.paramaters.get("bodyRecordType").charAt(0));
		bodyRecord.setRecordDate(recordDate);
		bodyRecord.setValue(value);
		bodyRecord.setColumnSeparator(this.paramaters.get("bodyColumSeparator").charAt(0));
		bodyRecord.setEndOfLine(this.paramaters.get("bodyEndOfLine").charAt(0));
		return bodyRecord;
	}

	private void generateCsvFile(HeaderRecord headerRecord, List<BodyRecord> bodyRecords, FooterRecord footerRecord) {

		try (PrintWriter writer = new PrintWriter(new File(filePath))) {

			StringBuilder sb = new StringBuilder();
			
			SimpleDateFormat csvDateformat = new SimpleDateFormat("yyyy-MM-dd");

			// generate header row
			sb.append(headerRecord.getName() + headerRecord.getColumnSeparator());
			sb.append(headerRecord.getCode() + headerRecord.getColumnSeparator());
			sb.append(csvDateformat.format(headerRecord.getCurrentDate()) + headerRecord.getColumnSeparator());
			sb.append(headerRecord.getEndOfLine() + '\n');

			// generate body rows
			for (BodyRecord bodyRecord : bodyRecords) {
				sb.append(bodyRecord.getName() + bodyRecord.getColumnSeparator());
				sb.append(bodyRecord.getCode() + bodyRecord.getColumnSeparator());
				sb.append(bodyRecord.getRecordId() + bodyRecord.getColumnSeparator());
				sb.append(bodyRecord.getRecordType() + bodyRecord.getColumnSeparator());
				sb.append(csvDateformat.format(bodyRecord.getRecordDate()) + bodyRecord.getColumnSeparator());
				sb.append(bodyRecord.getValue() + bodyRecord.getColumnSeparator());
				sb.append(bodyRecord.getEndOfLine() + '\n');
			}

			// generate footer row
			sb.append(footerRecord.getName() + footerRecord.getColumnSeparator());
			sb.append(footerRecord.getCode() + footerRecord.getColumnSeparator());
			sb.append(csvDateformat.format(footerRecord.getCurrentDate()) + footerRecord.getColumnSeparator());
			sb.append(footerRecord.getNumberOfBodyRecords() + footerRecord.getColumnSeparator());
			sb.append(footerRecord.getEndOfLine() + '\n');

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