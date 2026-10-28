package app.astrosoft.ui.dlg;

import app.astrosoft.beans.BirthData;
import app.astrosoft.beans.Place;
import app.astrosoft.beans.PlanetaryInfo;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Rasi;
import app.astrosoft.core.Horoscope;
import app.astrosoft.util.SwissHelper;
import app.astrosoft.util.TransitHelper;
import swisseph.SweDate;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.GregorianCalendar;
import java.util.Map;

public class DefaultTransitChartComputation implements TransitChartComputation {
    @Override
    public PlanetaryInfo compute(Date instant, Place place) {
        if (instant == null) {
            throw new IllegalArgumentException("instant is required");
        }
        if (place == null) {
            throw new IllegalArgumentException("place is required");
        }
        Calendar cal = new GregorianCalendar(place.astrosoftTimeZone().getTimeZone());
        cal.setTime(instant);
        BirthData birthData = new BirthData("Transit", cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.YEAR), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND), place);
        Horoscope horoscope = new Horoscope(birthData);
        return horoscope.getPlanetaryInfo();
    }

    @Override
    public Map<Planet, Date> computeSignChanges(Date instant, Place place, PlanetaryInfo planetaryInfo) {
        Map<Planet, SignTransition> transitions = computeSignTransitions(instant, place, planetaryInfo);
        Map<Planet, Date> changes = new EnumMap<Planet, Date>(Planet.class);
        for (Map.Entry<Planet, SignTransition> entry : transitions.entrySet()) {
            if (entry.getValue() != null && entry.getValue().getTransitionDate() != null) {
                changes.put(entry.getKey(), entry.getValue().getTransitionDate());
            }
        }
        return changes;
    }

    @Override
    public Map<Planet, SignTransition> computeSignTransitions(Date instant, Place place, PlanetaryInfo planetaryInfo) {
        if (instant == null || place == null) {
            return Collections.emptyMap();
        }
        double jdNow = instant.getTime() / 86400000.0 + 2440587.5;
        SwissHelper sh = new SwissHelper(new SweDate(jdNow));
        EnumMap<Planet, Double> positions = (planetaryInfo != null && planetaryInfo.getPlanetPosition() != null)
                ? planetaryInfo.getPlanetPosition()
                : sh.getPlanetaryPosition();

        Map<Planet, SignTransition> transitions = new EnumMap<Planet, SignTransition>(Planet.class);
        for (Planet p : Planet.allPlanets()) {
            Double pos = positions.get(p);
            if (pos != null) {
                SignTransition trans = getSignTransition(sh, p, pos, jdNow);
                if (trans != null) {
                    transitions.put(p, trans);
                }
            }
        }
        return transitions;
    }

    private SignTransition getSignTransition(SwissHelper sh, Planet p, double pos, double jdNow) {
        if (p == Planet.Ketu) {
            double rahuPos = (pos + 180.0) % 360.0;
            SignTransition rahuTrans = getSignTransition(sh, Planet.Rahu, rahuPos, jdNow);
            if (rahuTrans != null) {
                Rasi ketuFrom = Rasi.ofDeg(pos);
                Rasi ketuTo = rahuTrans.getToRasi().equals(rahuTrans.getFromRasi().previous())
                        ? ketuFrom.previous()
                        : ketuFrom.next();
                return new SignTransition(ketuFrom, ketuTo, rahuTrans.getTransitionDate());
            }
            return null;
        }

        Rasi currentRasi = Rasi.ofDeg(pos);
        double nextForward = ((currentRasi.ordinal() + 1) * 30.0) % 360.0;
        double nextBackward = (currentRasi.ordinal() * 30.0) % 360.0;

        TransitHelper th = sh.getTransitHelper(p);
        double searchJd = jdNow + 0.0001; // Avoid immediate re-trigger on boundary
        double tFwd = th.getTransit(nextForward, searchJd);
        double tBwd = th.getTransit(nextBackward, searchJd);

        double targetJd = -1;
        Rasi targetRasi = null;
        if (tFwd > jdNow && tBwd > jdNow) {
            if (tFwd < tBwd) {
                targetJd = tFwd;
                targetRasi = currentRasi.next();
            } else {
                targetJd = tBwd;
                targetRasi = currentRasi.previous();
            }
        } else if (tFwd > jdNow) {
            targetJd = tFwd;
            targetRasi = currentRasi.next();
        } else if (tBwd > jdNow) {
            targetJd = tBwd;
            targetRasi = currentRasi.previous();
        }

        if (targetJd > 0 && targetRasi != null) {
            return new SignTransition(currentRasi, targetRasi, SweDate.getDate(targetJd));
        }
        return null;
    }
}
