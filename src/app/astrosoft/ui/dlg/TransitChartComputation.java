package app.astrosoft.ui.dlg;

import app.astrosoft.beans.Place;
import app.astrosoft.beans.PlanetaryInfo;
import app.astrosoft.consts.Planet;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

public interface TransitChartComputation {
    PlanetaryInfo compute(Date instant, Place place);

    default Map<Planet, Date> computeSignChanges(Date instant, Place place, PlanetaryInfo planetaryInfo) {
        return Collections.emptyMap();
    }
}
