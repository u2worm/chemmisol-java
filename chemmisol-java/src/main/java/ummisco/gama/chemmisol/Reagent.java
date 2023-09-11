package ummisco.gama.chemmisol;

/**
 * Chemical entity that represents a reagent of a reaction.
 */
public class Reagent extends ChemicalEntity {
	private int coefficient;

	/**
	 * Initializes a Reagent.
	 *
	 * @param name Name of the reagent, that corresponds to the name of a
	 * chemical species in the chemical system.
	 * @param coefficient Stoichiometric coefficient associated to this reagent
	 * in a reaction.
	 * @param phase Phase of the reagent.
	 */
	public Reagent(String name, int coefficient, Phase phase) {
		super(name, phase);
		this.coefficient = coefficient;
	}

	/**
	 * Gets the stoichiometric coefficient associated to this reagent.
	 *
	 * <p>
	 * By convention, negative coefficients are associated to products, and
	 * positive coefficients are associated to reactants.
	 *
	 * @return stoichiometric coefficient
	 */
	public int getCoefficient() {
		return coefficient;
	}

	/**
	 * Sets the stoichiometric coefficient associated to this reagent.
	 *
	 * <p>
	 * By convention, negative coefficients are associated to products, and
	 * positive coefficients are associated to reactants.
	 *
	 * @param coefficient stoichiometric coefficient
	 */
	public void setCoefficient(int coefficient) {
		this.coefficient = coefficient;
	}
}
