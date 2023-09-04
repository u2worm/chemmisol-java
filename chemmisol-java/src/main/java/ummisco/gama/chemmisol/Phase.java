package ummisco.gama.chemmisol;

/**
 * Describes the phase of a chemical entity.
 */
public enum Phase {
	/**
	 * A solvent is always in excess and has a fixed concentration and activity.
	 */
	SOLVENT,
	/**
	 * An aqueous chemical entity lives in a solution with a given volume.
	 */
	AQUEOUS,
	/**
	 * A mineral chemical entity is a surface complex fixed at the surface of a
	 * mineral.
	 */
	MINERAL
}
