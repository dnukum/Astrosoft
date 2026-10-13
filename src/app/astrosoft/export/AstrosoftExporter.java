/**
 * AstrosoftExporter.java
 * Created On 2007, Aug 31, 2007 3:18:16 PM
 * @author E. Rajasekar
 */

package app.astrosoft.export;

import java.io.File;
import java.util.concurrent.FutureTask;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

public class AstrosoftExporter {

	public static enum Type {

		Horosocope {
			@Override
			public Exporter getExporter(String xmlFile) {

				return new HoroscopeExporter(xmlFile);
			}

			@Override
			public String getTemplate() {

				return Type.class.getResource("/resources/export/horoscope2pdf.xsl").toString();
			}
		},
		Compactibility{
			@Override
			public Exporter getExporter(String xmlFile) {

				return new CompactibilityExporter(xmlFile);
			}

			@Override
			public String getTemplate() {

				return Type.class.getResource("/resources/export/compactibility2pdf.xsl").toString();
			}
		},
		Panchang {
			@Override
			public Exporter getExporter(String xmlFile) {

				return new PanchangExporter(xmlFile);
			}

			@Override
			public String getTemplate() {

				return Type.class.getResource("/resources/export/panchang2pdf.xsl").toString();
			}
		};

		public abstract Exporter getExporter(String xmlFile);

		public abstract String getTemplate();


	};

	private static final Logger log = Logger.getLogger(AstrosoftExporter.class.getName());

	public static FutureTask<Object> export(final Type exporterType, final Exportable exportable, final String baseOutputFile, final boolean exportPdf, final boolean exportMd, final boolean exportXml) {

		Runnable r = new Runnable () {
			
			public void run() {
				String basePath = (baseOutputFile != null) ? baseOutputFile.replaceAll("(?i)\\.(pdf|md|xml)$", "") : "output";
				String pdfFile = basePath + ".pdf";
				String mdFile = basePath + ".md";
				String xmlFile = basePath + ".xml";

				File tempXml = null;
				String workingXmlFile = null;

				try {
					if (exportXml) {
						workingXmlFile = xmlFile;
						export2Xml(exporterType, exportable, workingXmlFile);
						log.info("XML File: " + workingXmlFile);
					} else if (exportPdf || exportMd) {
						tempXml = File.createTempFile("astrosoft_export_", ".xml");
						tempXml.deleteOnExit();
						workingXmlFile = tempXml.getAbsolutePath();
						export2Xml(exporterType, exportable, workingXmlFile);
					}

					if (exportPdf && workingXmlFile != null) {
						log.info("PDF File: " + pdfFile);
						FOPTransformer.exportToPDF(workingXmlFile, exporterType.getTemplate(), pdfFile);
					}

					if (exportMd && workingXmlFile != null) {
						log.info("MD File: " + mdFile);
						AstrosoftMarkdownExporter.exportToMarkdown(exporterType, workingXmlFile, mdFile);
					}
				} catch (Exception e) {
					log.log(java.util.logging.Level.SEVERE, "Export failed", e);
					throw new RuntimeException(e);
				} finally {
					if (tempXml != null && tempXml.exists()) {
						tempXml.delete();
					}
				}
			}
		};

		FutureTask<Object> task = new FutureTask<Object>(r, null);
		new Thread(task).start();
		return task;
	}

	public static FutureTask<Object> export2Pdf(final Type exporterType, final Exportable exportable, final String pdfFile){
		return export(exporterType, exportable, pdfFile, true, false, false);
	}

	public static FutureTask<Object> export2Markdown(final Type exporterType, final Exportable exportable, final String mdFile){
		return export(exporterType, exportable, mdFile, false, true, false);
	}

	public static FutureTask<Object> export2XmlTask(final Type exporterType, final Exportable exportable, final String xmlFile){
		return export(exporterType, exportable, xmlFile, false, false, true);
	}

	public static FutureTask<Object> export(final Type exporterType, final Exportable exportable, final String outputFile){
		if (outputFile != null && outputFile.toLowerCase().endsWith(".md")) {
			return export(exporterType, exportable, outputFile, false, true, false);
		} else if (outputFile != null && outputFile.toLowerCase().endsWith(".xml")) {
			return export(exporterType, exportable, outputFile, false, false, true);
		} else {
			return export(exporterType, exportable, outputFile, true, false, false);
		}
	}

	public static void export2Xml(Type exporterType, Exportable exportable, String xmlFile){

		exporterType.getExporter(xmlFile).export2Xml(exportable);

	}

}
