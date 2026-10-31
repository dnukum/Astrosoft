package app.astrosoft.beans;

import app.astrosoft.consts.Dignity;

/**
 * Immutable planetary status for display: whole-sign dignity plus Vargottama flag.
 */
public final class PlanetStatus {

	private final Dignity dignity;
	private final boolean vargottama;

	public PlanetStatus(Dignity dignity, boolean vargottama) {
		if (dignity == null) {
			throw new IllegalArgumentException("dignity must not be null");
		}
		this.dignity = dignity;
		this.vargottama = vargottama;
	}

	public Dignity getDignity() {
		return dignity;
	}

	public boolean isVargottama() {
		return vargottama;
	}

	public boolean isEmpty() {
		return dignity == Dignity.NONE && !vargottama;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof PlanetStatus)) return false;
		PlanetStatus other = (PlanetStatus) o;
		return dignity == other.dignity && vargottama == other.vargottama;
	}

	@Override
	public int hashCode() {
		return dignity.hashCode() * 31 + (vargottama ? 1 : 0);
	}
}
