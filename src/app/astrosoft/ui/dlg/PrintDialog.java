/**
 * PrintDialog.java
 * Created On 2007, Sep 7, 2007 2:42:13 PM
 * @author E. Rajasekar
 */

package app.astrosoft.ui.dlg;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.FutureTask;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JWindow;
import javax.swing.SpringLayout;
import javax.swing.border.EtchedBorder;
import javax.swing.plaf.basic.BasicArrowButton;

import app.astrosoft.core.PanchangList;
import app.astrosoft.core.Ephemeris.Mode;
import app.astrosoft.exception.AstrosoftException;
import app.astrosoft.export.AstrosoftExporter;
import app.astrosoft.ui.AstroSoft;
import app.astrosoft.ui.comp.CalendarSpinner;
import app.astrosoft.ui.comp.DateListener;
import app.astrosoft.ui.comp.FileChooserPanel;
import app.astrosoft.ui.comp.ProgressBarPanel;
import app.astrosoft.ui.comp.ProgressListener;
import app.astrosoft.ui.util.SpringUtilities;
import app.astrosoft.ui.util.UIConsts;
import app.astrosoft.ui.util.UIUtil;
import app.astrosoft.util.AstroUtil;
import app.astrosoft.util.AstrosoftFileFilter;
import app.astrosoft.util.FileOps;
import app.astrosoft.export.AstrosoftExporter.Type;
import static app.astrosoft.export.AstrosoftExporter.*;

public class PrintDialog extends AstrosoftDialog {

	private static final Logger log = Logger.getLogger(PrintDialog.class.getName());
	
	private static final Dimension dlgSize = new Dimension(450,290);
	
	//private static final Dimension dlgSize = new Dimension(440,320);
	
	private static final Dimension extDlgSize = new Dimension(450,390);
	
	private static final Dimension fileChooserSize = new Dimension(dlgSize.width - 60,60);
	private static final Dimension acrobatSize = new Dimension(dlgSize.width - 60,60);
	
	private static final Dimension progressSize = new Dimension(dlgSize.width - 60,25);
	
	private static final Dimension printPanelSize = new Dimension(dlgSize.width - 60,dlgSize.height - 40);
	
	private static final String defaultPath = AstroSoft.getPreferences().getAstrosoftFilesDir();
	
	private static SimpleDateFormat df = new SimpleDateFormat("MMM");
	
	private FileChooserPanel acrobatPanel;
	
	private FileChooserPanel outputFileChooser;
	
	JPanel printPanel;

	private JComboBox printTypes;

	private JRadioButton radioPdf = new JRadioButton("PDF", true);
	private JRadioButton radioMd = new JRadioButton("Markdown (.md)", false);
	private JRadioButton radioXml = new JRadioButton("XML (.xml)", false);
	private ButtonGroup formatGroup = new ButtonGroup();

	private JButton print;

	private JButton cancel;

	private Window panInputWindow;
	
	private boolean windowShown = false;

	private JCheckBox fullYear = new JCheckBox("Full Year", false);

	private CalendarSpinner spinner;

	/*private JButton arrow = new BasicArrowButton( 
	    5, UIConsts.THEME_CLR, new Color( 0, 0, 0 ),
	    new Color( 0, 0, 0 ), new Color( 0, 0, 0 ) );*/
	
	private JButton arrow = new JButton(UIUtil.createImageIcon("calendar"));
	
	public PrintDialog(AstroSoft parent){
		super(parent, "Print ", dlgSize);
		addComponents();
		setVisible(true);
	}

	private void addComponents() {
		//dlgPanel.setLayout(new SpringLayout());
		
		dlgPanel.setLayout(new FlowLayout());
	
		printPanel = createPrintPanel();
		dlgPanel.add(printPanel);
		
		add(dlgPanel);
			
	    setBackground(UIConsts.THEME_CLR);
	    
	    //Allow user to directly open if previous printed file already exists.
	    if (new File(outputFileChooser.getFilePath()).exists()){
	    	printCompleted();
	    }
	}

	private JPanel createPrintPanel() {
		
		JPanel printPanel = new JPanel(new SpringLayout());
		printPanel.setPreferredSize(printPanelSize);
		
		printPanel.add(createPrintTypePanel());
		
		boolean isPrintPanchang = ((app.astrosoft.export.AstrosoftExporter.Type)printTypes.getSelectedItem()).equals(AstrosoftExporter.Type.Panchang);
		
		if (isPrintPanchang){
			arrow.setEnabled(true);
		}else{
			arrow.setEnabled(false);
		}
		
		outputFileChooser = new FileChooserPanel(fileChooserSize, getDefaultFile(),"Output File " , FileOps.FileDialogMode.SAVE, AstrosoftFileFilter.PDF_EXTN, AstrosoftFileFilter.MD_EXTN, AstrosoftFileFilter.XML_EXTN );
		printPanel.add(outputFileChooser);
		
		JPanel buttonPanel = new JPanel();
		
		print = new JButton("Print");
		print.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				
				printClicked();
				
			}
	    	
	    });
		
		cancel = new JButton("Cancel");
		cancel.addActionListener(closeListener);
		
		buttonPanel.add(print);
		buttonPanel.add(cancel);
		
		printPanel.add(buttonPanel);
		
		
		SpringUtilities.makeCompactGrid(printPanel, 3, 1, 10,8,10,12);
		
		printPanel.setBorder(UIConsts.getTitleBorder("Print"));
		
		return printPanel;
	}

	

	protected void printClicked() {
		
		String outputFile = outputFileChooser.getFilePath();
		
		if (outputFile == null || outputFile.isEmpty()){
			OptionDialog.showDialog("Please choose valid file " , JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (radioXml.isSelected() && !outputFile.toLowerCase().endsWith(".xml")) {
			outputFile = outputFile.replaceAll("(?i)\\.(pdf|md)$", "") + ".xml";
			outputFileChooser.setFilePath(outputFile);
		} else if (radioMd.isSelected() && !outputFile.toLowerCase().endsWith(".md")) {
			outputFile = outputFile.replaceAll("(?i)\\.(pdf|xml)$", "") + ".md";
			outputFileChooser.setFilePath(outputFile);
		} else if (radioPdf.isSelected() && !outputFile.toLowerCase().endsWith(".pdf")) {
			outputFile = outputFile.replaceAll("(?i)\\.(md|xml)$", "") + ".pdf";
			outputFileChooser.setFilePath(outputFile);
		}
		
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		print.setEnabled(false);
		cancel.setEnabled(false);
		
		AstrosoftExporter.Type type = (AstrosoftExporter.Type)printTypes.getSelectedItem();
		
		FutureTask<Object> printTask = null;
		
		if (type.equals(AstrosoftExporter.Type.Horosocope)){
			printTask = AstrosoftExporter.export(AstrosoftExporter.Type.Horosocope, parent.getHoroscope(), outputFile);
		}else if (type.equals(AstrosoftExporter.Type.Compactibility)){
			printTask = AstrosoftExporter.export(AstrosoftExporter.Type.Compactibility, parent.getCompactibility(), outputFile);
		}else if (type.equals(AstrosoftExporter.Type.Panchang)){
			
			closePanInputWindow();
			PanchangList panchang = null;
			Calendar panCal = (spinner != null) ? AstroUtil.getCalendar(spinner.getSelectedDate()) : AstroUtil.getCalendar();
			
			if (fullYear.isSelected()){
				panchang = new PanchangList(panCal.get(Calendar.YEAR));
			}else{
				panchang = new PanchangList(panCal.get(Calendar.YEAR), panCal.get(Calendar.MONTH));
			}
			
			printTask = AstrosoftExporter.export(type, panchang, outputFile);
		}
		
		ProgressListener listener = new ProgressListener(){

			public void completed() {
				printCompleted();	
			}
			
		};
		
		ProgressBarPanel pbarPanel = new ProgressBarPanel(progressSize, printTask, "Printing", listener);
		
		refresh(pbarPanel);
	}

	protected void printCompleted() {
		
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		
		print.setEnabled(true);
		cancel.setEnabled(true);
		
		JPanel p = new JPanel(new SpringLayout());
		
		boolean isPdf = outputFileChooser.getFilePath().toLowerCase().endsWith(".pdf");
		
		if (isPdf) {
			acrobatPanel = new FileChooserPanel(acrobatSize,AstroSoft.getPreferences().getAcrobatExecutable(), "Acrobat Executable", FileOps.FileDialogMode.OPEN);
			p.add(acrobatPanel);
		} else {
			acrobatPanel = null;
		}
		
		JButton open = new JButton("Open");
		JButton close = new JButton("Close");
		
		open.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				
				openClicked();
				
			}
	    	
	    });
		
		close.addActionListener(closeListener);
		
		JPanel buttonPanel = new JPanel();
		
		buttonPanel.add(open);
		buttonPanel.add(close);
		
		p.add(buttonPanel);
		
		if (!isPdf) {
			SpringUtilities.makeCompactGrid(p, 1, 1, 10,10,10,10);
		} else {
			SpringUtilities.makeCompactGrid(p, 2, 1, 10,10,10,10);
		}
		
		refresh(p);
		
	}

	private void openClicked() {
		
		try {
			if (acrobatPanel != null && acrobatPanel.getFilePath() != null && !acrobatPanel.getFilePath().isEmpty()){
				AstroSoft.getPreferences().setAcrobatExecutable(acrobatPanel.getFilePath());
			}
			FileOps.openDocument(outputFileChooser.getFilePath());
		}catch(AstrosoftException e){
			
			OptionDialog.showDialog(e.getMessage(),JOptionPane.ERROR_MESSAGE);
		}
		
	}

	private JPanel createPrintTypePanel(){
		
		JPanel typePanel = new JPanel(new SpringLayout());
		
		printTypes = new JComboBox();
		
		printTypes.addItemListener(new ItemListener(){

			public void itemStateChanged(ItemEvent e) {
				
				if (((app.astrosoft.export.AstrosoftExporter.Type)printTypes.getSelectedItem()).equals(AstrosoftExporter.Type.Panchang)){
					arrow.setEnabled(true);
				}else{
					arrow.setEnabled(false);
				}
				
				if (outputFileChooser != null ) {
					String outputFile = getDefaultFile();
					outputFileChooser.setFilePath(outputFile);
					
					if(new File(outputFile).exists()){
						printCompleted();
					}else{
						refresh();
					}
				}
			}
			
		});
		
		if (parent.getHoroscope() != null){
			printTypes.addItem(AstrosoftExporter.Type.Horosocope);
		}
		
		if(parent.getCompactibility() != null) {
			printTypes.addItem(AstrosoftExporter.Type.Compactibility);
		}
		
		printTypes.addItem(AstrosoftExporter.Type.Panchang);
		
		arrow.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				arrowClicked();
			}
			
		});
		
		JPanel comboPanel = new JPanel(new BorderLayout());
		
		comboPanel.add(printTypes, BorderLayout.CENTER);
		comboPanel.add(arrow, BorderLayout.EAST);

		formatGroup.add(radioPdf);
		formatGroup.add(radioMd);
		formatGroup.add(radioXml);
		radioPdf.setOpaque(false);
		radioMd.setOpaque(false);
		radioXml.setOpaque(false);

		ActionListener formatListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				formatChanged();
			}
		};
		radioPdf.addActionListener(formatListener);
		radioMd.addActionListener(formatListener);
		radioXml.addActionListener(formatListener);

		JPanel formatPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		formatPanel.setOpaque(false);
		formatPanel.add(radioPdf);
		formatPanel.add(radioMd);
		formatPanel.add(radioXml);
		
		typePanel.add(new JLabel("Print "));
		typePanel.add(comboPanel);
		typePanel.add(new JLabel("Format "));
		typePanel.add(formatPanel);
		
		SpringUtilities.makeCompactGrid(typePanel, 2, 2, 0,0,5,6);
		
		return typePanel;
	}

	private void formatChanged() {
		if (outputFileChooser != null) {
			String currentPath = outputFileChooser.getFilePath();
			String targetExt = radioXml.isSelected() ? ".xml" : (radioMd.isSelected() ? ".md" : ".pdf");
			currentPath = currentPath.replaceAll("(?i)\\.(pdf|md|xml)$", "") + targetExt;
			outputFileChooser.setFilePath(currentPath);
			if (new File(currentPath).exists()) {
				printCompleted();
			} else {
				refresh();
			}
		}
	}
	
	protected void arrowClicked() {
		
		if (!windowShown) {
			panInputWindow = createPanchangInputWindow();
			UIUtil.setWindowLocation(panInputWindow, printTypes);
			panInputWindow.setVisible(true);
			windowShown = true;
		}else {
			closePanInputWindow();
		}
		
	}

	private void closePanInputWindow(){
		
		if (panInputWindow != null){
			panInputWindow.dispose();
			windowShown = false;
		}
		
	}
	
	private void refresh(JPanel panel){
		
		dlgPanel.removeAll();
		
		dlgPanel.add(printPanel);
		
		if (panel != null) {
			dlgPanel.add(panel);
			resetSize(extDlgSize);
		}else {
			resetSize(dlgSize);
		}
		setBackground();
		
		repaint();
		setVisible(true);
	}
	
	private void refresh(){
		refresh(null);
	}
	
	private String getDefaultFile(){
		
	    app.astrosoft.export.AstrosoftExporter.Type type = (app.astrosoft.export.AstrosoftExporter.Type)printTypes.getSelectedItem();
		String file = null;
		if (type != null) {
			if (type.equals(AstrosoftExporter.Type.Horosocope)){
				file = parent.getHoroscope().createDocumentName();
			}else if (type.equals(AstrosoftExporter.Type.Compactibility)){
				file = parent.getCompactibility().createDocumentName();
			}else if (type.equals(AstrosoftExporter.Type.Panchang)) {
				file = getPanchangOutputFile(null);
			}
		}
		
		if (file != null) {
			String targetExt = (radioXml != null && radioXml.isSelected()) ? ".xml" :
			                   ((radioMd != null && radioMd.isSelected()) ? ".md" : ".pdf");
			file = file.replaceAll("(?i)\\.pdf$", targetExt);
		}
		
		return file;
	}
	
	private Window createPanchangInputWindow(){
		
		JWindow window = new JWindow(this);
		
		Container windowPane = window.getContentPane(  );
		
		windowPane.setLayout(new BorderLayout());
		
		spinner = new CalendarSpinner(CalendarSpinner.FMT_MONTH_YEAR);
		
		spinner.addDateListener( new DateListener () {

			public void dateChanged(Date date) {
				
				outputFileChooser.setFilePath(getPanchangOutputFile(date));
			}
			
		});
		
		JPanel panChooser = spinner.getChooser();
		JPanel p = new JPanel();
		
		p.add(panChooser);
		p.add(fullYear);
		
		p.setBackground(UIConsts.CAL_COMBO_BACKGROUND);
		p.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		
		windowPane.add(p, BorderLayout.CENTER);
		
		window.pack();
		
		fullYear.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				outputFileChooser.setFilePath(getPanchangOutputFile(spinner.getSelectedDate()));
			}
		});
		return window;
	}
	
	private String getPanchangOutputFile(Date date){
		
		StringBuilder sb = new StringBuilder(defaultPath);
		sb.append("Panchang");
		
		Calendar cal = (date != null) ? AstroUtil.getCalendar(date) : AstroUtil.getCalendar();
			
		if (!fullYear.isSelected()){
			sb.append("_");
			sb.append(df.format(cal.getTime()));
		}
		
		sb.append("_");
		sb.append(cal.get(Calendar.YEAR));
		
		String targetExt = (radioXml != null && radioXml.isSelected()) ? ".xml" :
		                   ((radioMd != null && radioMd.isSelected()) ? ".md" : ".pdf");
		sb.append(targetExt);
		return sb.toString();
		
	}
}
