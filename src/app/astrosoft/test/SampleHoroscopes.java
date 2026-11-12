/**
 * SampleHoroscopes.java
 * Created On 2007, Oct 16, 2007 2:25:30 PM
 * @author E. Rajasekar
 */

package app.astrosoft.test;

import java.io.File;
import app.astrosoft.core.Horoscope;

public class SampleHoroscopes {

	public static Horoscope getHoroscope(String file) {
		File f = new File(file);
		if (!f.exists()) {
			File rel = new File("samples", f.getName());
			if (rel.exists()) {
				file = rel.getPath();
			}
		}
		return Horoscope.createFromFile(file);
	}
	
	public static Horoscope getMyHoroscope(){
		return getHoroscope("tests/charts/sample_chart_1.ash");
	}
	
	public static Horoscope getElangoHoroscope(){
		return getHoroscope("tests/charts/sample_chart_4.ash");
	}
	
	public static Horoscope getMuthuHoroscope(){
		return getHoroscope("tests/charts/sample_chart_3.ash");
	}
	
	public static Horoscope getSubaHoroscope(){
		return getHoroscope("tests/charts/sample_chart_2.ash");
	}
	
	public static Horoscope getManiHoroscope(){
		return getHoroscope("tests/charts/sample_chart_1.ash");
	}
}
