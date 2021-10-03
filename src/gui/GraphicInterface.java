package gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import parser.Parser;

public class GraphicInterface {
   	
	private Parser parser;
	private JFrame frame;

    public GraphicInterface() {
    	
    	this.parser = new Parser();
    	
    	this.frame = new JFrame("Excel File -> CSV File");
		this.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.frame.setLayout(new BorderLayout());
		this.frame.setSize(1000, 500);
				
		addContentToFrame();
		open();
    }
    
    @SuppressWarnings("serial")
	private void addContentToFrame() {
		
    	// *** search section ***
    	
    	JTextField filePath = new JTextField();
		filePath.setFont(new Font("Calibri", Font.BOLD, 25));
		filePath.setDropTarget(new DropTarget() {
	        public synchronized void drop(DropTargetDropEvent DropEvent) {
	            try {
	            	DropEvent.acceptDrop(DnDConstants.ACTION_COPY);
	                @SuppressWarnings("unchecked")
					List<File> droppedFiles = (List<File>) DropEvent.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
	                for (File file : droppedFiles) {
	                   
	                	filePath.setText(file.getAbsolutePath());
	                }
	            } catch (Exception ex) {
	                ex.printStackTrace();
	            }
	        }
	    });
		
		JButton selectFile = new JButton("Select File");
		selectFile.setFont(new Font("Calibri", Font.BOLD, 25));
		selectFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				FileFilter filter = new FileNameExtensionFilter("Excel file", "xls", "xlsx");
				chooser.addChoosableFileFilter(filter);
				
				int result= chooser.showOpenDialog(frame);
				if(result==JFileChooser.APPROVE_OPTION) {
					 
					filePath.setText(chooser.getSelectedFile().getAbsolutePath());
				 }			
			}
		});
		
		JLabel selectInfo = new JLabel("Drag an Excel File Here or Click 'Select File'");
		selectInfo.setFont(new Font("Calibri", Font.BOLD, 30));
		selectInfo.setHorizontalAlignment(JLabel.CENTER);
		selectInfo.setVerticalAlignment(JLabel.CENTER);
		Border borderForSelectInfoLabel = BorderFactory.createLineBorder(Color.ORANGE, 15);
		selectInfo.setBorder(borderForSelectInfoLabel);
		selectInfo.setDropTarget(new DropTarget() {
	        public synchronized void drop(DropTargetDropEvent DropEvent) {
	            try {
	            	DropEvent.acceptDrop(DnDConstants.ACTION_COPY);
	                @SuppressWarnings("unchecked")
					List<File> droppedFiles = (List<File>) DropEvent.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
	                for (File file : droppedFiles) {
	                   
	                	filePath.setText(file.getAbsolutePath());
	                }
	            } catch (Exception ex) {
	                ex.printStackTrace();
	            }
	        }
	    });
		
		JPanel searchSection = new JPanel(new BorderLayout());
		searchSection.add(filePath, BorderLayout.CENTER);
		searchSection.add(selectFile, BorderLayout.LINE_END);
		searchSection.add(selectInfo , BorderLayout.NORTH);
	
		frame.add(searchSection, BorderLayout.NORTH);
		
		// *** format input panel & info labels border settings***
		
		JPanel formatSection = new JPanel(new BorderLayout());	
		
		Border borderForLabels = BorderFactory.createLineBorder(Color.LIGHT_GRAY, 5);
		
		// *** csv file header format ***
				
		JLabel headerNameLabel = new JLabel("Header Name:", SwingConstants.CENTER);
		headerNameLabel.setFont(new Font("Calibri", Font.BOLD, 20));
		headerNameLabel.setBorder(borderForLabels);
		
		JLabel headerCodeLabel = new JLabel("Header Code:", SwingConstants.CENTER);
		headerCodeLabel.setFont(new Font("Calibri", Font.BOLD, 20));
		headerCodeLabel.setBorder(borderForLabels);
		
		JLabel headerColumSeparatorLabel = new JLabel("Header Colum Separator:", SwingConstants.CENTER);
		headerColumSeparatorLabel.setFont(new Font("Calibri", Font.BOLD, 20));
		headerColumSeparatorLabel.setBorder(borderForLabels);
		
		JLabel headerEndOfLineLabel = new JLabel("Header End of Line:", SwingConstants.CENTER);
		headerEndOfLineLabel.setFont(new Font("Calibri", Font.BOLD, 20));
		headerEndOfLineLabel.setBorder(borderForLabels);
		
		JTextField headerName = new JTextField("Cenas123");
		headerName.setFont(new Font("Calibri", Font.BOLD, 25));
		headerName.setHorizontalAlignment(JTextField.CENTER);

		JTextField headerCode = new JTextField("1");
		headerCode.setFont(new Font("Calibri", Font.BOLD, 25));
		headerCode.setHorizontalAlignment(JTextField.CENTER);
		
		JTextField headerColumSeparator = new JTextField(",");
		headerColumSeparator.setFont(new Font("Calibri", Font.BOLD, 25));
		headerColumSeparator.setHorizontalAlignment(JTextField.CENTER);
		
		JTextField headerEndOfLine = new JTextField("$");
		headerEndOfLine.setFont(new Font("Calibri", Font.BOLD, 25));
		headerEndOfLine.setHorizontalAlignment(JTextField.CENTER);
		
		JPanel headerLabelSection = new JPanel(new GridLayout(1,4));
		headerLabelSection.add(headerNameLabel);
		headerLabelSection.add(headerCodeLabel);
		headerLabelSection.add(headerColumSeparatorLabel);
		headerLabelSection.add(headerEndOfLineLabel);
		
		JPanel headerFieldsSection = new JPanel(new GridLayout(1,4));
		headerFieldsSection.add(headerName);
		headerFieldsSection.add(headerCode);
		headerFieldsSection.add(headerColumSeparator);
		headerFieldsSection.add(headerEndOfLine);
		
		JPanel headerSection = new JPanel(new GridLayout(2,4));
		headerSection.add(headerLabelSection);
		headerSection.add(headerFieldsSection);
		
		headerSection.setPreferredSize(new Dimension(1000, 100));
		formatSection.add(headerSection, BorderLayout.NORTH);
		
		// *** csv file body format ***
		
		JLabel bodyNameLabel = new JLabel("Body Name:", SwingConstants.CENTER);
		bodyNameLabel.setFont(new Font("Calibri", Font.BOLD, 20));
		bodyNameLabel.setBorder(borderForLabels);
		
		JLabel bodyCodeLabel = new JLabel("Body Code:", SwingConstants.CENTER);
		bodyCodeLabel.setFont(new Font("Calibri", Font.BOLD, 20));
		bodyCodeLabel.setBorder(borderForLabels);
		
		JLabel bodyTypeLabel = new JLabel("Body Type:", SwingConstants.CENTER);
		bodyTypeLabel.setFont(new Font("Calibri", Font.BOLD, 20));
		bodyTypeLabel.setBorder(borderForLabels);
		
		JLabel bodyColumSeparatorLabel = new JLabel("Body Colum Separator:", SwingConstants.CENTER);
		bodyColumSeparatorLabel.setFont(new Font("Calibri", Font.BOLD, 20));
		bodyColumSeparatorLabel.setBorder(borderForLabels);
		
		JLabel bodyEndOfLineLabel = new JLabel("Body End of Line:", SwingConstants.CENTER);
		bodyEndOfLineLabel.setFont(new Font("Calibri", Font.BOLD, 20));
		bodyEndOfLineLabel.setBorder(borderForLabels);
		
		JTextField bodyName = new JTextField("Cenas123");
		bodyName.setFont(new Font("Calibri", Font.BOLD, 25));
		bodyName.setHorizontalAlignment(JTextField.CENTER);
		
		JTextField bodyCode = new JTextField("2");
		bodyCode.setFont(new Font("Calibri", Font.BOLD, 25));
		bodyCode.setHorizontalAlignment(JTextField.CENTER);
		
		JRadioButton BodyTypeC = new JRadioButton("C");
		BodyTypeC.setSelected(true);
		BodyTypeC.setFont(new Font("Calibri", Font.BOLD, 25));
		JRadioButton BodyTypeD = new JRadioButton("D");
		BodyTypeD.setFont(new Font("Calibri", Font.BOLD, 25));
		
		ButtonGroup buttonsGroup = new ButtonGroup();
		buttonsGroup.add(BodyTypeC);
		buttonsGroup.add(BodyTypeD);
		
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.add(BodyTypeC);
		buttonsPanel.add(BodyTypeD);
		
		JTextField bodyColumSeparator = new JTextField(",");
		bodyColumSeparator.setFont(new Font("Calibri", Font.BOLD, 25));
		bodyColumSeparator.setHorizontalAlignment(JTextField.CENTER);
		
		JTextField bodyEndOfLine = new JTextField("$");
		bodyEndOfLine.setFont(new Font("Calibri", Font.BOLD, 25));
		bodyEndOfLine.setHorizontalAlignment(JTextField.CENTER);
		
		JPanel bodyLabelSection = new JPanel(new GridLayout(1,5));
		bodyLabelSection.add(bodyNameLabel);
		bodyLabelSection.add(bodyCodeLabel);
		bodyLabelSection.add(bodyTypeLabel);
		bodyLabelSection.add(bodyColumSeparatorLabel);
		bodyLabelSection.add(bodyEndOfLineLabel);
		
		JPanel bodyFieldsSection = new JPanel(new GridLayout(1,5));
		bodyFieldsSection.add(bodyName);
		bodyFieldsSection.add(bodyCode);
		bodyFieldsSection.add(buttonsPanel);
		bodyFieldsSection.add(bodyColumSeparator);
		bodyFieldsSection.add(bodyEndOfLine);
		
		JPanel bodySection = new JPanel(new GridLayout(2,5));
		
		bodySection.add(bodyLabelSection);
		bodySection.add(bodyFieldsSection);
		
		bodySection.setPreferredSize(new Dimension(1000, 100));
		formatSection.add(bodySection, BorderLayout.CENTER);
		
		// *** csv file footer format ***
				
		JLabel footerNameLabel = new JLabel("Footer Name:", SwingConstants.CENTER);
		footerNameLabel.setFont(new Font("Calibri", Font.BOLD, 20));
		footerNameLabel.setBorder(borderForLabels);		
		
		JLabel footerCodeLabel = new JLabel("Footer Code:", SwingConstants.CENTER);
		footerCodeLabel.setFont(new Font("Calibri", Font.BOLD, 20));
		footerCodeLabel.setBorder(borderForLabels);
		
		JLabel footerColumSeparatorLabel = new JLabel("Footer Colum Separator:", SwingConstants.CENTER);
		footerColumSeparatorLabel.setFont(new Font("Calibri", Font.BOLD, 20));
		footerColumSeparatorLabel.setBorder(borderForLabels);
		
		JLabel footerEndOfLineLabel = new JLabel("Footer End of Line:", SwingConstants.CENTER);
		footerEndOfLineLabel.setFont(new Font("Calibri", Font.BOLD, 20));
		footerEndOfLineLabel.setBorder(borderForLabels);

		JTextField footerName = new JTextField("Cenas123");
		footerName.setFont(new Font("Calibri", Font.BOLD, 25));
		footerName.setHorizontalAlignment(JTextField.CENTER);

		JTextField footerCode = new JTextField("9");
		footerCode.setFont(new Font("Calibri", Font.BOLD, 25));
		footerCode.setHorizontalAlignment(JTextField.CENTER);

		JTextField footerColumSeparator = new JTextField(",");
		footerColumSeparator.setFont(new Font("Calibri", Font.BOLD, 25));
		footerColumSeparator.setHorizontalAlignment(JTextField.CENTER);

		JTextField footerEndOfLine = new JTextField("$");
		footerEndOfLine.setFont(new Font("Calibri", Font.BOLD, 25));
		footerEndOfLine.setHorizontalAlignment(JTextField.CENTER);

		JPanel footerLabelSection = new JPanel(new GridLayout(1,4));
		footerLabelSection.add(footerNameLabel);
		footerLabelSection.add(footerCodeLabel);
		footerLabelSection.add(footerColumSeparatorLabel);
		footerLabelSection.add(footerEndOfLineLabel);
		
		JPanel footerFieldsSection = new JPanel(new GridLayout(1, 4));
		footerFieldsSection.add(footerName);
		footerFieldsSection.add(footerCode);
		footerFieldsSection.add(footerColumSeparator);
		footerFieldsSection.add(footerEndOfLine);

		JPanel footerSection = new JPanel(new GridLayout(2, 4));
		footerSection.add(footerLabelSection);
		footerSection.add(footerFieldsSection);

		footerSection.setPreferredSize(new Dimension(1000, 100));
		formatSection.add(footerSection, BorderLayout.SOUTH);
		
		frame.add(formatSection, BorderLayout.CENTER);
		
		// *** convert button ***
			
		JButton convertFile = new JButton("Convert File !!!");
		convertFile.setFont(new Font("Calibri", Font.BOLD, 45));
		convertFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {	
				// set chosen parameters in gui for file conversion
				parser.setFilePath(filePath.getText());
				Map<String, String> parameters = new HashMap<String, String>();
				parameters.put("headerName", headerName.getText());
				parameters.put("headerCode", headerCode.getText());
				parameters.put("headerColumSeparator", headerColumSeparator.getText());
				parameters.put("headerEndOfLine", headerEndOfLine.getText());
				parameters.put("bodyName", bodyName.getText());
				parameters.put("bodyCode", bodyCode.getText());
				if(BodyTypeC.isSelected()) {
					parameters.put("bodyRecordType", "C");
				} else {
					parameters.put("bodyRecordType", "D");
				}
				parameters.put("bodyColumSeparator", bodyColumSeparator.getText());
				parameters.put("bodyEndOfLine", bodyEndOfLine.getText());
				parameters.put("footerName", footerName.getText());
				parameters.put("footerCode", footerCode.getText());
				parameters.put("footerColumSeparator", footerColumSeparator.getText());
				parameters.put("footerEndOfLine", footerEndOfLine.getText());
				parser.setParameters(parameters);
				
				// convert file
				Thread parserThread = new Thread(parser);
				parserThread.start();
			}
		});
		
		JPanel convertSection = new JPanel(new BorderLayout());
		convertSection.add(convertFile);
		parser.setSection(convertSection);
		
		frame.add(convertSection, BorderLayout.SOUTH);
		parser.setFrame(frame);

	}

    public void open() {
		frame.setVisible(true);
		frame.setResizable(false);
	}
    
    // main method
    public static void main(String[] args) {
    	GraphicInterface gui = new GraphicInterface();
    }
 
}
