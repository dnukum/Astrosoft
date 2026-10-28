/**
 * CompactibilityExporter.java
 * Created On 2007, Jul 5, 2007 12:12:11 PM
 * @author E. Rajasekar
 */

package app.astrosoft.export;

import java.util.logging.Level;
import java.util.logging.Logger;

import app.astrosoft.beans.PlanetChartData;
import app.astrosoft.consts.Ayanamsa;
import app.astrosoft.consts.Language;
import app.astrosoft.consts.Varga;
import app.astrosoft.consts.XmlConsts;
import app.astrosoft.core.Compatibility;
import app.astrosoft.core.Horoscope;
import app.astrosoft.pref.AstrosoftPref;
import app.astrosoft.ui.AstroSoft;

public class CompatibilityExporter extends AbstractExporter {

	private static final Logger log = Logger.getLogger(CompatibilityExporter.class.getName());

	public CompatibilityExporter(String file) {
		super(file);
	}

	public void export(Compatibility compatibility) {

		try {

			//Create document and write horoscope tag
			xmlWriter.add(xmlef.createStartDocument());
			xmlWriter.add(xmlef.createStartElement(XmlConsts.ASTROSOFT_TAG, null,null));
			xmlWriter.add(xmlef.createNamespace("xsi",XmlConsts.XML_NS));
			xmlWriter.add(xmlef.createStartElement(XmlConsts.COMPATIBILITY_TAG, null,null));
			xmlWriter.add(xmlef.createAttribute(XmlConsts.Title, compatibility.getTitle()));
			xmlWriter.add(xmlef.createAttribute(XmlConsts.hasHoroscope, String.valueOf(compatibility.hasHoroscope())));

			xmlWriter.add(xmlef.createStartElement(XmlConsts.BOY_TAG, null,null));
			exportTableData(compatibility.getBoyInfo(), XmlConsts.HOROSCOPE_INFO_TAG, XmlConsts.INFO_TAG);

			if(compatibility.hasHoroscope()) {
				export(new PlanetChartData(Varga.Rasi, compatibility.getBoyPlanetaryInfo()));
				export(new PlanetChartData(Varga.Navamsa, compatibility.getBoyPlanetaryInfo()));
			}
			xmlWriter.add(xmlef.createEndElement(XmlConsts.BOY_TAG, null));

			xmlWriter.add(xmlef.createStartElement(XmlConsts.GIRL_TAG, null,null));
			exportTableData(compatibility.getGirlInfo(), XmlConsts.HOROSCOPE_INFO_TAG, XmlConsts.INFO_TAG);
			if(compatibility.hasHoroscope()) {
				export(new PlanetChartData(Varga.Rasi, compatibility.getGirlPlanetaryInfo()));
				export(new PlanetChartData(Varga.Navamsa, compatibility.getGirlPlanetaryInfo()));
			}
			xmlWriter.add(xmlef.createEndElement(XmlConsts.GIRL_TAG, null));


			exportTableData(compatibility.getKutaTableData(), Compatibility.getKutaTableColumnMetaData(), XmlConsts.KUTA_ANALYSIS_TAG, XmlConsts.KUTA_MATCH_TAG);

			if (compatibility.hasHoroscope()){
				exportTableData(compatibility.getDoshaTableData(), Compatibility.getDoshaTableColumnMetaData(), XmlConsts.DOSHA_ANALYSIS_TAG, XmlConsts.PLANET_DOSHA_TAG);
			}

			if (compatibility.getNotes() != null && !compatibility.getNotes().trim().isEmpty()) {
				xmlWriter.add(xmlef.createStartElement(new javax.xml.namespace.QName(XmlConsts.XML_NS, XmlConsts.Notes), null, null));
				xmlWriter.add(xmlef.createCData(compatibility.getNotes()));
				xmlWriter.add(xmlef.createEndElement(new javax.xml.namespace.QName(XmlConsts.XML_NS, XmlConsts.Notes), null));
			}

			xmlWriter.add(xmlef.createEndElement(XmlConsts.COMPATIBILITY_TAG, null));
			xmlWriter.add(xmlef.createEndDocument());

			xmlWriter.close();

		}catch(Exception e){
			log.log(Level.SEVERE, "Exception in writing compatibility xml document", e);
		}
	}

	public static void main(String[] args) {


		AstrosoftPref pref = AstroSoft.getPreferences();
		pref.setAyanamsa(Ayanamsa.KRISHNAMURTHI);

		Language currLang = pref.getLanguage();
		pref.setLanguage(Language.ENGLISH);

		Horoscope b = new Horoscope("Elango", 17, 4, 1957, 7, 10,
				77 + (44.00 / 60.00), 11 + (22.00 / 60.00), 5.5, "Erode");

		Horoscope g = new Horoscope("Mani", 10, 8, 1960, 5, 30,
				77 + (44.00 / 60.00), 11 + (22.00 / 60.00), 5.5, "Erode");

		Compatibility c = new Compatibility(b, g);

		//Compatibility c = new Compatibility("a" , "b", Nakshathra.Ashwini, Nakshathra.Bharani, Rasi.Mesha, Rasi.Mesha);

		String file = "c:/astrosoft/resources/export/compactibility.xml";

		CompatibilityExporter compactibilityExporter = new CompatibilityExporter(file);

		compactibilityExporter.export2Xml(c);

		FOPTransformer.exportToPDF(file, "C:/AstroSoft/resources/export/compactibility2pdf.xsl", "C:/AstroSoft/resources/export/compactibility.pdf");

		pref.setLanguage(currLang);
	}
}
