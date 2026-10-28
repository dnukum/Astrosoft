/**
 * AstrosoftFileFilter.java
 * Created On 2006, May 26, 2006 7:44:43 PM
 * @author E. Rajasekar
 */

package app.astrosoft.util;

import javax.swing.filechooser.FileFilter;

public class AstrosoftFileFilter extends FileFilter {

	public static final AstrosoftFileFilter ALL_ASTROSOFT = new AstrosoftFileFilter(
		new String[]{".ash", ".asm", ".asc"},
		"All AstroSoft Files (*.ash, *.asm, *.asc)"
	);

	public static final AstrosoftFileFilter HOROSCOPE_EXTN = new AstrosoftFileFilter(".ash", "AstroSoft Horoscopes (*.ash)");

	public static final AstrosoftFileFilter MUHURTA_EXTN = new AstrosoftFileFilter(".asm", "AstroSoft Muhurtas (*.asm)");

	public static final AstrosoftFileFilter COMPATIBILITY_EXTN = new AstrosoftFileFilter(".asc", "AstroSoft Compatibility (*.asc)");
	public static final AstrosoftFileFilter COMPACTIBILITY_EXTN = COMPATIBILITY_EXTN; // Backward compatibility alias

	public static final AstrosoftFileFilter PDF_EXTN = new AstrosoftFileFilter(".pdf", "PDF Documents (*.pdf)");

	public static final AstrosoftFileFilter MD_EXTN = new AstrosoftFileFilter(".md", "Markdown Documents (*.md)");

	public static final AstrosoftFileFilter XML_EXTN = new AstrosoftFileFilter(".xml", "XML Documents (*.xml)");

	public static final AstrosoftFileFilter ALL_FILES = new AstrosoftFileFilter("*", "All Files (*.*)");

	private String extension;

	private String[] extensions;

	private String desc;

    public AstrosoftFileFilter(String extension, String desc) {
    	this.extension = extension;
    	this.desc = desc;
    }

    public AstrosoftFileFilter(String[] extensions, String desc) {
    	this.extensions = extensions;
    	this.extension = (extensions != null && extensions.length > 0) ? extensions[0] : "*";
    	this.desc = desc;
    }

    public boolean accept( java.io.File file ) {

        if ( file.isDirectory() || extension.equals("*") ) {

            return true;
        } else if (extensions != null) {
            String name = file.getName().toLowerCase();
            for (String ext : extensions) {
                if (name.endsWith(ext.toLowerCase())) {
                    return true;
                }
            }
            return false;
        } else {

            return ( file.getName(  ) ).toLowerCase().endsWith( extension.toLowerCase() );
        }

    }

    public String getDescription(  ) {

        return desc;

    }

    public String getExtension() {
		return extension;
	}

}

