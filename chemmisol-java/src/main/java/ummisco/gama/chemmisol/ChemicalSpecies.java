package ummisco.gama.chemmisol;

/**
 * Represents a chemical species living in a chemical system.
 *
 * <p>
 * A chemical species is an entity that represents an actual entity within a
 * chemical system, associated to a concentration. The interpretation of the
 * concentration depends on the phase of the species (mol/l for aqueous species,
 * molar fraction for mineral species).
 * </p>
 */
public class ChemicalSpecies extends ChemicalEntity {
	private double concentration;

	/**
	 * Initializes a ChemicalSpecies.
	 *
	 * @param name Name of the chemical species.
	 * @param phase Phase of the chemical species.
	 * @param concentration Initial concentration of the chemical species,
	 * depending on its phase (mol/l for aqueous species, molar fraction for
	 * mineral species).
	 */
	ChemicalSpecies(String name, Phase phase, double concentration) {
		super(name, phase);
		this.concentration = concentration;
	}

	/**
	 * Initializes a ChemicalSpecies with an initial null concentration.
	 *
	 * @param name Name of the chemical species.
	 * @param phase Phase of the chemical species.
	 */
	public ChemicalSpecies(String name, Phase phase) {
		this(name, phase, 0.0);
	}

	/**
	 * Gets the current concentration of this chemical species.
	 *
	 * @return concentration of the chemical species
	 */
	public double getConcentration() {
		return concentration;
	}

	/**
	 * Sets the current concentration of the chemical species.
	 *
	 * @param concentration concentration of the chemical species
	 */
	public void setConcentration(double concentration) {
		this.concentration = concentration;
	}
}
