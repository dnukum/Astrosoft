/**
 * YogasTest.java
 * Created On 2007, Oct 16, 2007 2:13:32 PM
 * @author E. Rajasekar
 */

package app.astrosoft.xps.yoga.test;

import app.astrosoft.core.Horoscope;
import app.astrosoft.test.SampleHoroscopes;
import app.astrosoft.xps.yoga.YogaFinder;
import app.astrosoft.xps.yoga.YogaResults;

public class YogasTest {

	public static final void main(final String[] args) throws Exception {

		System.out.println("Raja --> " + findYogas(SampleHoroscopes.getMyHoroscope()));

		System.out.println("Elango --> " + findYogas(SampleHoroscopes.getElangoHoroscope()));

		System.out.println("Mani --> " + findYogas(SampleHoroscopes.getManiHoroscope()));

		System.out.println("Muthu --> " + findYogas(SampleHoroscopes.getMuthuHoroscope()));

		System.out.println("Suba --> " + findYogas(SampleHoroscopes.getSubaHoroscope()));
	}

	public static YogaResults findYogas(Horoscope h) {
		return YogaFinder.getInstance().findYogas(h);
	}
}
