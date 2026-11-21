/**
 * HoroscopeExporter.java
 * Created On 2007, Jun 12, 2007 12:51:36 PM
 * @author E. Rajasekar
 */

package app.astrosoft.export;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Writer;
import java.text.Format;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.events.Attribute;

import app.astrosoft.beans.AshtaVargaChartData;
import app.astrosoft.beans.Degree;
import app.astrosoft.beans.HousePosition;
import app.astrosoft.beans.PlanetCharacter;
import app.astrosoft.beans.PlanetChartData;
import app.astrosoft.beans.PlanetStatus;
import app.astrosoft.beans.PlanetaryInfo;
import app.astrosoft.consts.AshtavargaName;
import app.astrosoft.consts.AstroConsts;
import app.astrosoft.consts.AstrosoftTableColumn;
import app.astrosoft.consts.Ayanamsa;
import app.astrosoft.consts.Dignity;
import app.astrosoft.consts.DisplayStrings;
import app.astrosoft.consts.FunctionalRole;
import app.astrosoft.consts.Language;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Rasi;
import app.astrosoft.consts.SignRelationship;
import app.astrosoft.consts.Varga;
import app.astrosoft.consts.XmlConsts;
import app.astrosoft.core.Ashtavarga;
import app.astrosoft.core.Dasa;
import app.astrosoft.core.Horoscope;
import app.astrosoft.core.PlanetStatusCalculator;
import app.astrosoft.core.ShadBala;
import app.astrosoft.core.Vimshottari;
import app.astrosoft.pref.AstrosoftPref;
import app.astrosoft.ui.AstroSoft;
import app.astrosoft.ui.table.ColumnMetaData;
import app.astrosoft.ui.table.Table;
import app.astrosoft.ui.table.TableData;
import app.astrosoft.ui.table.TableDataFactory;
import app.astrosoft.ui.table.TableRowData;
import app.astrosoft.util.AstroUtil;


/**
 * HoroscopeExporter exports Horoscope as XML data. 
 * This class is designed based on visitor pattern.
 * This class implements Exporter ( Visitor ) interface.
 * Each class that needs to be exported implements Exportable interface. 
 * 
 * This class implements Exporter interface and visit() method
 * writes passed object to xml.
 * 
 * @author Raja
 *
 */
public class HoroscopeExporter extends AbstractExporter {
	
	private static final Logger log = Logger.getLogger(HoroscopeExporter.class.getName());
	
	public HoroscopeExporter(String file) {
		
		super(file);
	}
	
	public void export(AshtaVargaChartData ashtavargaChart) {
		
		try {
			
			xmlWriter.add(xmlef.createStartElement(XmlConsts.ASHTVARGA_CHART_TAG, null,null));
			
			xmlWriter.add(xmlef.createAttribute(XmlConsts.Name, ashtavargaChart.getChartName()));
			
			EnumMap<Rasi, Integer> varga = ashtavargaChart.getVarga();
			
			for(Rasi rasi : varga.keySet()){
				
				xmlWriter.add(xmlef.createStartElement(XmlConsts.ASHTVARGA_HOUSE_TAG, null,null));
				
				xmlWriter.add(xmlef.createAttribute(XmlConsts.Number, String.valueOf(rasi.ordinal()+1)));
				
				xmlWriter.add(xmlef.createCharacters(varga.get(rasi).toString()));
				
				xmlWriter.add(xmlef.createEndElement(XmlConsts.ASHTVARGA_HOUSE_TAG, null));
				
			}
			
			xmlWriter.add(xmlef.createEndElement(XmlConsts.ASHTVARGA_CHART_TAG, null));
			
		}catch(Exception e){
			log.log(Level.SEVERE, "Exception in writing chart xml ", e);
		}
		
	}
	
	public void export(PlanetaryInfo planetaryInfo) {
		
		exportTableData(planetaryInfo.getPlanateryInfoTableData(), planetaryInfo.getPlanateryInfoColumnMetaData(), XmlConsts.PLANETARY_POS_TAG, XmlConsts.PLANET_POS_TAG);
	}

	private void exportPlanetaryInfoWithProperties(PlanetaryInfo planetaryInfo, Rasi ascRasi) {
		try {
			TableData<? extends TableRowData> data = planetaryInfo.getPlanateryInfoTableData();
			ColumnMetaData colMetaData = planetaryInfo.getPlanateryInfoColumnMetaData();
			EnumMap<Planet, Rasi> planetRasi = planetaryInfo.getPlanetRasi();
			EnumMap<Planet, Boolean> planetDir = planetaryInfo.getPlanetDirection();
			EnumMap<Planet, Boolean> planetCombust = planetaryInfo.getPlanetCombustion();

			xmlWriter.add(xmlef.createStartElement(XmlConsts.PLANETARY_POS_TAG, null, null));

			for (int i = 0; i < data.getRowCount(); i++) {
				TableRowData row = data.getRow(i);
				xmlWriter.add(xmlef.createStartElement(XmlConsts.PLANET_POS_TAG, null, null));

				for (AstrosoftTableColumn col : colMetaData.getVisibleColumns()) {
					QName colTag = new QName(XmlConsts.XML_NS, col.name());
					Object value = row.getColumnData(col);
					writeElement(colTag, value != null ? value.toString() : null);
				}

				Object planetObj = row.getColumnData(AstrosoftTableColumn.Planet);
				Planet p = (planetObj instanceof Planet) ? (Planet) planetObj : null;
				Rasi rasi = (p != null && planetRasi != null) ? planetRasi.get(p) : null;

				writeElement(XmlConsts.RETROGRADE_TAG, formatRetrograde(p, planetDir));
				writeElement(XmlConsts.COMBUST_TAG, formatCombust(p, planetCombust));
				writeElement(XmlConsts.DIGNITY_TAG, formatDignity(p, rasi));
				writeElement(XmlConsts.FUNCTIONAL_STATUS_TAG, formatFunctionalStatus(p, ascRasi));
				writeElement(XmlConsts.ASPECTS_TAG, formatAspects(p, rasi, ascRasi));

				xmlWriter.add(xmlef.createEndElement(XmlConsts.PLANET_POS_TAG, null));
			}

			xmlWriter.add(xmlef.createEndElement(XmlConsts.PLANETARY_POS_TAG, null));
		} catch (Exception e) {
			log.log(Level.SEVERE, "Exception in writing planetary position xml", e);
		}
	}

	private void exportChartProperties(Rasi ascRasi) {
		try {
			xmlWriter.add(xmlef.createStartElement(XmlConsts.CHART_PROPERTIES_TAG, null, null));

			writeNamedProperty("Kendra Lords (1,4,7,10)", formatPlanetList(PlanetCharacter.getKendraLords(ascRasi)));
			writeNamedProperty("Trikona Lords (1,5,9)", formatPlanetList(PlanetCharacter.getTrikonaLords(ascRasi)));

			Planet yogakaraka = PlanetCharacter.getYogakaraka(ascRasi);
			writeNamedProperty("Yogakaraka", yogakaraka != null ? yogakaraka.toString() : "None");

			writeNamedProperty("Functional Benefics", formatPlanetList(PlanetCharacter.getFunctionalBenefics(ascRasi)));
			writeNamedProperty("Functional Neutrals", formatPlanetList(PlanetCharacter.getFunctionalNeutrals(ascRasi)));
			writeNamedProperty("Functional Malefics", formatPlanetList(PlanetCharacter.getFunctionalMalefics(ascRasi)));

			xmlWriter.add(xmlef.createEndElement(XmlConsts.CHART_PROPERTIES_TAG, null));
		} catch (Exception e) {
			log.log(Level.SEVERE, "Exception in writing chart properties xml", e);
		}
	}

	private void writeElement(QName tag, String text) throws XMLStreamException {
		xmlWriter.add(xmlef.createStartElement(tag, null, null));
		if (text != null) {
			xmlWriter.add(xmlef.createCharacters(text));
		}
		xmlWriter.add(xmlef.createEndElement(tag, null));
	}

	private void writeNamedProperty(String name, String value) throws XMLStreamException {
		xmlWriter.add(xmlef.createStartElement(XmlConsts.PROPERTY_TAG, null, null));
		xmlWriter.add(xmlef.createAttribute(XmlConsts.Name, name));
		xmlWriter.add(xmlef.createCharacters(value != null ? value : "None"));
		xmlWriter.add(xmlef.createEndElement(XmlConsts.PROPERTY_TAG, null));
	}

	private static String formatRetrograde(Planet p, EnumMap<Planet, Boolean> planetDir) {
		if (p == null || p.isAsc()) {
			return "—";
		}
		return (planetDir != null && Boolean.TRUE.equals(planetDir.get(p))) ? "Yes" : "No";
	}

	private static String formatCombust(Planet p, EnumMap<Planet, Boolean> planetCombust) {
		if (p == null || p.isAsc() || p == Planet.Sun || p.isNode()) {
			return "—";
		}
		return (planetCombust != null && Boolean.TRUE.equals(planetCombust.get(p))) ? "Yes" : "No";
	}

	private static String formatDignity(Planet p, Rasi rasi) {
		if (p == null || p.isAsc() || p.isNode() || rasi == null) {
			return "—";
		}
		if (PlanetStatusCalculator.dignity(p, rasi) == Dignity.MOOLATRIKONA) {
			return "Moolatrikona";
		}
		SignRelationship rel = PlanetStatusCalculator.signRelationship(p, rasi);
		return rel != null ? rel.label() : "—";
	}

	private static String formatFunctionalStatus(Planet p, Rasi ascRasi) {
		if (p == null || p.isAsc() || p.isNode() || ascRasi == null) {
			return "—";
		}
		FunctionalRole role = PlanetCharacter.getFunctionalRole(ascRasi, p);
		StringBuilder sb = new StringBuilder();
		if (role.isBenefic()) {
			sb.append("Benefic");
		} else if (role.isMalefic()) {
			sb.append("Malefic");
		} else {
			sb.append("Neutral");
		}
		if (PlanetCharacter.isYogakaraka(ascRasi, p)) {
			sb.append(", Yogakaraka");
		}
		if (PlanetCharacter.isMaraka(ascRasi, p)) {
			sb.append(", Maraka");
		}
		return sb.toString();
	}

	private static String formatAspects(Planet p, Rasi rasi, Rasi ascRasi) {
		if (p == null || p.isAsc() || p.ordinal() >= 9 || rasi == null || ascRasi == null) {
			return "—";
		}
		int fromHouse = rasi.bhava(ascRasi);
		int[] targets = AstroConsts.PLANET_ASPECT_TARGETS[p.ordinal()][fromHouse];
		if (targets == null || targets.length == 0) {
			return "—";
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < targets.length; i++) {
			if (i > 0) sb.append(", ");
			sb.append("H").append(targets[i]);
		}
		return sb.toString();
	}

	private static String formatPlanetList(Collection<Planet> planets) {
		if (planets == null || planets.isEmpty()) {
			return "None";
		}
		List<Planet> list = new ArrayList<Planet>(planets);
		Collections.sort(list);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < list.size(); i++) {
			if (i > 0) sb.append(", ");
			sb.append(list.get(i).toString());
		}
		return sb.toString();
	}
	
	public void export(HousePosition housePosition) {
		
		exportTableData(housePosition.getBhavaTableData(), housePosition.getBhavaTableColumnData(), XmlConsts.BHAVA_POSITIONS_TAG, XmlConsts.BHAVA_POS_TAG);
	}
	
	public void export(Ashtavarga ashtavarga){
		
		try {
			
			xmlWriter.add(xmlef.createStartElement(XmlConsts.ASHTAVARGAS_TAG, null,null));
			
			for(AshtavargaName name : AshtavargaName.ashtavargas()){
				
				xmlWriter.add(xmlef.createStartElement(XmlConsts.ASHTAVARGA_TAG, null,null));
				xmlWriter.add(xmlef.createAttribute(XmlConsts.Name, name.name()));
				
				new AshtaVargaChartData(name, ashtavarga.getAshtavarga(name)).doExport(this);
				new AshtaVargaChartData(AshtavargaName.Trikona, ashtavarga.getTrikona(name)).doExport(this);
				new AshtaVargaChartData(AshtavargaName.Ekathipathya, ashtavarga.getEkathipathiya(name)).doExport(this);
				
				if(name != AshtavargaName.SarvaAshtavarga) {
					exportTableData(ashtavarga.getGunaTable(name).getTableData(), XmlConsts.GUNAHARAS_TAG, XmlConsts.GUNAHARA_TAG);
				}
				
				xmlWriter.add(xmlef.createEndElement(XmlConsts.ASHTAVARGA_TAG, null));
			}
			
			xmlWriter.add(xmlef.createEndElement(XmlConsts.ASHTAVARGAS_TAG, null));
			
		}catch(Exception e){
			log.log(Level.SEVERE, "Exception in writing ashtavarga xml ", e);
		}
		
	}
	
	public void export(ShadBala shadBala){
		
		try {
			
			xmlWriter.add(xmlef.createStartElement(XmlConsts.SHADBALAS_TAG, null,null));
			
			exportTableData(TableDataFactory.getReversedTable(shadBala.getPlanetBalaTableData(), shadBala.getPlanetBalaColumnMetaData(), AstrosoftTableColumn.Planet, AstrosoftTableColumn.Name), XmlConsts.SHADBALA, XmlConsts.BALA_TAG);
			
			exportTableData(TableDataFactory.getReversedTable(shadBala.getPlanetBalaTableData(), shadBala.getSthanaBalaColumnMetaData(), AstrosoftTableColumn.Planet, AstrosoftTableColumn.Name), XmlConsts.STHANABALA_TAG, XmlConsts.BALA_TAG);
			exportTableData(TableDataFactory.getReversedTable(shadBala.getPlanetBalaTableData(), shadBala.getKalaBalaColumnMetaData(), AstrosoftTableColumn.Planet, AstrosoftTableColumn.Name), XmlConsts.KALABALA_TAG, XmlConsts.BALA_TAG);
			
			exportTableData(shadBala.getBhavaBalaTableData(), shadBala.getBhavaBalaColumnMetaData(), XmlConsts.BHAVABALA_TAG, XmlConsts.BALA_TAG);
			
			xmlWriter.add(xmlef.createEndElement(XmlConsts.SHADBALAS_TAG, null));
			
			
		}catch(Exception e){
			log.log(Level.SEVERE, "Exception in writing shadbala xml ", e);
		}
		
	}
	
	private void exportVargaCharts(PlanetaryInfo planetaryInfo) {
		
		try {
			
			xmlWriter.add(xmlef.createStartElement(XmlConsts.VARGA_CHART_TAG, null,null));
			
			for(Varga varga : Varga.values()) {
				
				new PlanetChartData(varga,planetaryInfo).doExport(this);
				
			}
			xmlWriter.add(xmlef.createEndElement(XmlConsts.VARGA_CHART_TAG, null));
			
		}catch(Exception e){
			log.log(Level.SEVERE, "Exception in writing varga charts", e);
		}
		
		
	}

	public void export(Vimshottari v) {
		
		try {
			
			xmlWriter.add(xmlef.createStartElement(XmlConsts.VIM_DASA_TAG, null,null));
			
			EnumMap<Planet, Dasa> dasa = v.getDasa();
			
			StringBuilder attrVal = null;
			
			for(Planet p : Planet.dasaLords(v.getStartLord())){
				
				Dasa d = dasa.get(p);
				
				xmlWriter.add(xmlef.createStartElement(XmlConsts.MAJOR_DASA_TAG, null,null));
				
				xmlWriter.add(xmlef.createAttribute(XmlConsts.Dasa, p.name() + " " + XmlConsts.Dasa));
				
				attrVal = new StringBuilder(" ( ");
				//attrVal.append(" " + XmlConsts.Dasa);
				attrVal.append(d.getStartDate());
				attrVal.append(" ~ ");
				attrVal.append(d.getEndDate());
				attrVal.append(" )");
				
				xmlWriter.add(xmlef.createAttribute(XmlConsts.Period, attrVal.toString()));
				
				for(Dasa sd : d.subDasas()) {
					
					attrVal = new StringBuilder(sd.fullDasa());
					attrVal.append(" ( ");
					attrVal.append(sd.getStartDate());
					attrVal.append(" ~ ");
					attrVal.append(sd.getEndDate());
					attrVal.append(" )");
					
					Attribute subDasa = xmlef.createAttribute(XmlConsts.Dasa, attrVal.toString() );
					
					exportTableData(v.getVimDasaTableData(sd),Vimshottari.getVimDasaTableColumnMetaData(), XmlConsts.SUB_DASA_TAG, subDasa, XmlConsts.ANTHARA_DASA_TAG);
					
				}
				xmlWriter.add(xmlef.createEndElement(XmlConsts.MAJOR_DASA_TAG, null));
			}
			
			xmlWriter.add(xmlef.createEndElement(XmlConsts.VIM_DASA_TAG, null));
			
		}catch(Exception e){
			log.log(Level.SEVERE, "Exception in writing vimshottari dasa", e);
		}
	}
	
	public void export(Horoscope horoscope) {
		
		try {
			
			//Create document and write horoscope tag
			xmlWriter.add(xmlef.createStartDocument());
			xmlWriter.add(xmlef.createStartElement(XmlConsts.ASTROSOFT_TAG, null,null));
			xmlWriter.add(xmlef.createNamespace("xsi",XmlConsts.XML_NS));
			xmlWriter.add(xmlef.createNamespace("av", XmlConsts.ASHTAVARGA_XML_NS));
			xmlWriter.add(xmlef.createStartElement(XmlConsts.HOROSCOPE_TAG, null,null));
			xmlWriter.add(xmlef.createAttribute(XmlConsts.Title, horoscope.getTitle()));
			
			
			exportTableData(horoscope.getInfoTableData(), XmlConsts.HOROSCOPE_INFO_TAG, XmlConsts.INFO_TAG);
			exportPlanetaryInfoWithProperties(horoscope.getPlanetaryInfo(), horoscope.getAscendant());
			exportChartProperties(horoscope.getAscendant());

			exportVargaCharts(horoscope.getPlanetaryInfo());
			
			horoscope.getAshtaVarga().doExport(this);
			horoscope.getShadBala().doExport(this);
			horoscope.getVimshottariDasa().doExport(this);
			
			//End horoscope tag and document
			xmlWriter.add(xmlef.createEndElement(XmlConsts.HOROSCOPE_TAG, null));
			xmlWriter.add(xmlef.createEndDocument());
			
			xmlWriter.close();
			
		}catch(Exception e){
			log.log(Level.SEVERE, "Exception in writing horosocope xml document", e);
		}
	}
	
	

	
	public static void main(String[] args) {
		
		AstroSoft.getPreferences().setAyanamsa(Ayanamsa.KRISHNAMURTHI);
		AstroSoft.getPreferences().setLanguage(Language.TAMIL);
		
		Horoscope h = new Horoscope("Raja", 11, 12, 1980, 1, 44,
				77 + (44.00 / 60.00), 11 + (22.00 / 60.00), 5.5, "Erode");
		
		/*Horoscope h = new Horoscope("Mani", 10, 8, 1960, 5, 30,
				77 + (44.00 / 60.00), 11 + (22.00 / 60.00), 5.5, "Erode");*/
		
		/*Horoscope h = new Horoscope("Elango", 17, 4, 1957, 7, 10,
				77 + (44.00 / 60.00), 11 + (22.00 / 60.00), 5.5, "Erode");*/
		
		String file = "c:/astrosoft/resources/export/astrosoft.xml";
		
		HoroscopeExporter horoscopeExporter = new HoroscopeExporter(file);
		
		horoscopeExporter.export2Xml(h);
		
		
		FOPTransformer.exportToPDF(file, "C:/AstroSoft/resources/export/horoscope2pdf.xsl", "C:/AstroSoft/resources/export/astrosoft.pdf");
	}

	
}