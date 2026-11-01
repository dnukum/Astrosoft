/**
 * RahuNode.java
 *
 * Defines the Lunar Node (Rahu/Ketu) calculation mode:
 * - MEAN: Mean Node (steady, smoothed mathematical regression, strictly retrograde)
 * - TRUE: True Node (osculating instantaneous orbital intersection, includes lunar/solar perturbations)
 *
 * @author Astrosoft Team
 */
package app.astrosoft.consts;

import swisseph.SweConst;

public enum RahuNode {

    MEAN("Mean Node", SweConst.SE_MEAN_NODE),
    TRUE("True Node", SweConst.SE_TRUE_NODE);

    private final String nodeName;
    private final int swissPlanetNo;

    RahuNode(String nodeName, int swissPlanetNo) {
        this.nodeName = nodeName;
        this.swissPlanetNo = swissPlanetNo;
    }

    public String nodeName() {
        return nodeName;
    }

    public int swissPlanetNo() {
        return swissPlanetNo;
    }

    public static RahuNode getDefault() {
        return MEAN;
    }

    @Override
    public String toString() {
        return nodeName;
    }
}
