package ummisco.gama.chemmisol;

/**
 * Represents a chemical entity with a name and a phase.
 */
public class ChemicalEntity {
	private String name;
	private Phase phase;

	/**
	 * Initializes a Chemical entity.
	 *
	 * @param name Name of the chemical entity.
	 * @param phase Phase of the chemical entity.
	 */
	protected ChemicalEntity(String name, Phase phase) {
		this.name = name;
		this.phase = phase;
	}

	/**
	 * Gets the name of the chemical entity.
	 *
	 * @return name of the chemical entity
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the chemical entity.
	 *
	 * @param name name of the chemical entity
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the phase of the chemical entity.
	 *
	 * @return phase of this chemical entity
	 */
	public Phase getPhase() {
		return phase;
	}
}
