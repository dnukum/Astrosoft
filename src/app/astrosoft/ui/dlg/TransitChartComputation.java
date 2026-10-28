package app.astrosoft.ui.dlg;

import app.astrosoft.beans.Place;
import app.astrosoft.beans.PlanetaryInfo;

import java.util.Date;

public interface TransitChartComputation {
    PlanetaryInfo compute(Date instant, Place place);
}
