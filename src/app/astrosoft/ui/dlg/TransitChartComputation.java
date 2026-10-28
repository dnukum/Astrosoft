package app.astrosoft.ui.dlg;

import app.astrosoft.beans.Place;
import app.astrosoft.beans.PlanetaryInfo;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Rasi;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

public interface TransitChartComputation {
    PlanetaryInfo compute(Date instant, Place place);

    default Map<Planet, Date> computeSignChanges(Date instant, Place place, PlanetaryInfo planetaryInfo) {
        return Collections.emptyMap();
    }

    default Map<Planet, SignTransition> computeSignTransitions(Date instant, Place place, PlanetaryInfo planetaryInfo) {
        return Collections.emptyMap();
    }

    public static class SignTransition {
        private final Rasi fromRasi;
        private final Rasi toRasi;
        private final Date transitionDate;

        public SignTransition(Rasi fromRasi, Rasi toRasi, Date transitionDate) {
            this.fromRasi = fromRasi;
            this.toRasi = toRasi;
            this.transitionDate = transitionDate;
        }

        public Rasi getFromRasi() {
            return fromRasi;
        }

        public Rasi getToRasi() {
            return toRasi;
        }

        public Date getTransitionDate() {
            return transitionDate;
        }

        public String getTransitionString() {
            String from = fromRasi != null ? fromRasi.toString() : "—";
            String to = toRasi != null ? toRasi.toString() : "—";
            return from + " -> " + to;
        }
    }
}
