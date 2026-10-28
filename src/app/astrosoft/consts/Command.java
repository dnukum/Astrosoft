/**
 * Command.java
 *
 * Created on August 04, 2005, 7:16 PM
 *
 * @author  E. Rajasekar
 */
package app.astrosoft.consts;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.EnumSet;

import javax.swing.KeyStroke;

import app.astrosoft.ui.view.ViewManager.View;

class KeyMaskHelper {
	static final int MENU_MASK;
	static final int SHIFT_MASK = InputEvent.SHIFT_DOWN_MASK;
	static {
		int mask;
		try {
			mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
		} catch (Throwable t) {
			mask = ActionEvent.CTRL_MASK;
		}
		MENU_MASK = mask;
	}
}

public enum Command {

	NEW ("New",KeyEvent.VK_N, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyMaskHelper.MENU_MASK)),
	OPEN ("Open",KeyEvent.VK_O, KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyMaskHelper.MENU_MASK )),
	OPEN_COMPATIBILITY ("Open Compatibility", KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyMaskHelper.MENU_MASK | KeyMaskHelper.SHIFT_MASK)),
	SAVE ("Save",KeyEvent.VK_S,KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyMaskHelper.MENU_MASK)),
	SAVE_AS ("Save As...", KeyEvent.VK_A, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyMaskHelper.MENU_MASK | KeyMaskHelper.SHIFT_MASK)),
	SAVE_COMPATIBILITY("Save Compatibility", KeyEvent.VK_C,KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyMaskHelper.MENU_MASK | KeyMaskHelper.SHIFT_MASK)),
	PRINT ("Print",KeyEvent.VK_P,KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyMaskHelper.MENU_MASK)),
	EXIT ("Exit",KeyEvent.VK_Q,KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyMaskHelper.MENU_MASK)),

	EDIT_CHART ("Chart Data",KeyEvent.VK_C),
	EDIT_COMPATIBILITY ("Compatibility",KeyEvent.VK_M),
	CHART_VIEW (View.CHART_VIEW.displayVal(),KeyEvent.VK_C),
	DASAS_VIEW (View.DASAS_VIEW.displayVal(),KeyEvent.VK_D),
	PLANET_POS_VIEW (View.PLANET_POS_VIEW.displayVal(),KeyEvent.VK_P),
	BHAVA_POS_VIEW (View.BHAVA_POS_VIEW.displayVal(),KeyEvent.VK_B),
	DIV_CHART_VIEW (View.DIV_CHART_VIEW.displayVal(),KeyEvent.VK_I),
	ASHTAVARGA_VIEW (View.ASHTAVARGA_VIEW.displayVal(),KeyEvent.VK_A),
	SHADBALA_VIEW (View.SHADBALA_VIEW.displayVal(),KeyEvent.VK_S),
	YOGA_COMBINATIONS_VIEW (View.YOGA_COMBINATIONS_VIEW.displayVal(),KeyEvent.VK_Y),
	EPHEMERIS_VIEW (View.EPHEMERIS_VIEW.displayVal(),KeyEvent.VK_E),
	PANCHANG_VIEW (View.PANCHANG_VIEW.displayVal(),KeyEvent.VK_G),
		MY_MUHURTA_VIEW (View.MY_MUHURTA_VIEW.displayVal(),KeyEvent.VK_M),
	TRANSIT_CHART ("Transit Chart", KeyEvent.VK_T),
	COMPATIBILITY_VIEW (View.COMPATIBILITY_VIEW.displayVal(),KeyEvent.VK_M),
	MARRIAGE_MATCH_VIEW (View.MARRIAGE_MATCH_VIEW.displayVal(),KeyEvent.VK_R),
	MUHURTHA_VIEW (View.MUHURTHA_VIEW.displayVal()),
	//
	//CMP_COMP ("Marriage Compatibility",KeyEvent.VK_M),
	//CMP_MUHURTHA ("Muhurtha"),

	AY_LAH (Ayanamsa.LAHARI.ayaName()),
	AY_RAM (Ayanamsa.RAMAN.ayaName()),
	AY_KM (Ayanamsa.KRISHNAMURTHI.ayaName()),
	LANG_EN (Language.ENGLISH.name()),
	LANG_TA (Language.TAMIL.name()),
	Place (DisplayStrings.DEF_LOC_STR),
	EphCalcTime(DisplayStrings.EPH_TIME_STR),
	PanCalcTime(DisplayStrings.PAN_TIME_STR),

	//Numerlogy Commands
	FIND_NAME_VIEW(View.FIND_NAME_VIEW.displayVal()),
	COMPUTE_NUMBER(DisplayStrings.COMPUTE_NUMBER_STR),
	EXPORT_NAME_DB(DisplayStrings.EXPORT_NAME_DB_STR),
	IMPORT_NAME_DB(DisplayStrings.IMPORT_NAME_DB_STR),
	IMPORT_NAME(DisplayStrings.IMPORT_NAME_STR),
	EXPORT_HOROSCOPE_TO_PDF(DisplayStrings.EXPORT_HOROSCOPE_TO_PDF_STR),
	EXPORT_COMPATIBILITY_TO_PDF(DisplayStrings.EXPORT_COMPATIBILITY_TO_PDF_STR),

	ABOUT("About AstroSoft", KeyEvent.VK_A),
	NOTES("Notes", KeyEvent.VK_T, KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyMaskHelper.MENU_MASK))
	;

	// Backward compatibility aliases
	public static final Command OPEN_COMPACTIBILITY = OPEN_COMPATIBILITY;
	public static final Command SAVE_COMPACTIBILITY = SAVE_COMPATIBILITY;
	public static final Command EDIT_COMPACTIBILITY = EDIT_COMPATIBILITY;
	public static final Command COMPACTIBILITY_VIEW = COMPATIBILITY_VIEW;
	public static final Command EXPORT_COMPACTIBILITY_TO_PDF = EXPORT_COMPATIBILITY_TO_PDF;

	private String action;
	private int mnemonic = -1;
	private KeyStroke key = null;

	private Command(Enum action){
		this(action.toString());
	}

	private Command(String action) {
		this.action = action;
	}

	private Command(String action, int mnemonic) {
		this.action = action;
		this.mnemonic = mnemonic;
	}
	
	private Command(String action, int mnemonic, KeyStroke key) {
		this.action = action;
		this.mnemonic = mnemonic;
		this.key = key;
	}
	
	private Command(String action, KeyStroke key) {
		this.action = action;
		this.key = key;
	}

	public String action(){
		return action;
	}

	public int mnemonic(){
		return mnemonic;
	}
	
	public KeyStroke key(){
		return key;
	}

	public static EnumSet<Command> ayanamsaOptions(){
		return EnumSet.range(AY_LAH, AY_KM);
	}

	public static EnumSet<Command> langOptions(){
		return EnumSet.range(LANG_EN, LANG_TA);
	}

	public static EnumSet<Command> fileMenuItems(){
		return EnumSet.range(NEW, EXIT);
	}

	public static EnumSet<Command> editMenuItems(){
		return EnumSet.range(EDIT_CHART, EDIT_COMPATIBILITY);
	}

	public static EnumSet<Command> viewMenuItems(){
		return EnumSet.range(CHART_VIEW, TRANSIT_CHART);
	}

	public static EnumSet<Command> computeMenuItems(){
		return EnumSet.range(COMPATIBILITY_VIEW, MUHURTHA_VIEW);
	}

	public static EnumSet<Command> optionMenuItems(){
		return EnumSet.range(Place, PanCalcTime);
	}

	public static EnumSet<Command> numeroMenuItems(){
		return EnumSet.range(FIND_NAME_VIEW, IMPORT_NAME);
	}

	public static EnumSet<Command> exportMenuItems(){
		return EnumSet.range(EXPORT_HOROSCOPE_TO_PDF, EXPORT_COMPATIBILITY_TO_PDF);
	}

	public static EnumSet<Command> toolBarItems(){
		return EnumSet.of(Command.NEW ,
				Command.OPEN ,
				Command.SAVE ,
				Command.PRINT ,
				Command.DIV_CHART_VIEW ,
				Command.PLANET_POS_VIEW ,
				Command.BHAVA_POS_VIEW ,
				Command.DASAS_VIEW,
				Command.CHART_VIEW ,
				Command.ASHTAVARGA_VIEW ,
				Command.SHADBALA_VIEW ,
				Command.EPHEMERIS_VIEW ,
				Command.PANCHANG_VIEW ,
				Command.NOTES);
	}
}
