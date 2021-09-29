package parser;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.apache.commons.compress.compressors.FileNameUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import records.BodyRecord;
import records.FooterRecord;
import records.HeaderRecord;

///private SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd"); TODO
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
	//filePath =  Paths.get(filePath).getParent().toString() + sheet.getSheetName() + "csv";
	
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
			System.out.println("nome da sheet que foi lida: " + sheet.getSheetName());
			// generate file path for CSV file
			this.csvFileName = sheet.getSheetName();
			
			//Iterate through each rows one by one & generate csv file content
			//TODO initialize csv file ocntent
			
			// generate the percentage to increment the progress bar
			int numberOfRows = sheet.getPhysicalNumberOfRows() - 2;
			int percentagePerRow = (int) (1/((double) numberOfRows) * 100);
			System.out.println("Progress Bar incrementation "+percentagePerRow);
			
			
			
   
			Iterator<Row> rowIterator = sheet.iterator();
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				// For each row, iterate through all the columns
				Iterator<Cell> cellIterator = row.cellIterator();

				while (cellIterator.hasNext()) {

				}
				System.out.println("");
				//updateProgressBar(percentagePerRow);
			}
		}
        
        file.close();
		return null;
	}
	
	private BodyRecord generateBodyRecord() {
		BodyRecord bodyRecord = new BodyRecord(Integer.parseInt(this.paramaters.get("bodyCode")));
		bodyRecord.setName(this.paramaters.get("bodyName"));
		bodyRecord.setRecordType(this.paramaters.get("bodyRecordType").charAt(0));
		bodyRecord.setColumnSeparator(this.paramaters.get("bodyColumSeparator").charAt(0));
		bodyRecord.setEndOfLine(this.paramaters.get("bodyEndOfLine").charAt(0));
		return bodyRecord;
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