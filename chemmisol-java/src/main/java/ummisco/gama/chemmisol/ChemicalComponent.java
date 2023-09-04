package ummisco.gama.chemmisol;

/**
 * Represents a chemical component.
 *
 * <p>
 * A chemical component is a canocical chemical entity that cannot be divided,
 * from which other chemical species can be produced according to chemical
 * reactions.
 *
 * <p>
 * The total concentration of a component is defined as the sum of the
 * concentration of the chemical species associated to the component and the
 * concentrations of all the species compound from the component. This total
 * concentration is defined by the user. The chemmisol library then dispatch the
 * total concentration among all species produced from this component when
 * {@link ChemicalSystem#solve()} is called. It consequence, when the chemical
 * system is at equilibrium, the total concentration of a component generally
 * does <b>not</b> correspond the concentration of its associated chemical
 * species.
 *
 * <p>
 * The total concentration of a chemical component can be set using {@link
 * ChemicalSystem#setTotalConcentration(ChemicalComponent, double)}.
 *
 * @see <a
 * href=https://u2worm.github.io/chemmisol-cpp/classchemmisol_1_1ChemicalComponent.html>
 * chemmisol-cpp documentaion</a>
 */
public class ChemicalComponent {
	private ChemicalSpecies species;
	private double total_concentration;

	/**
	 * Initializes a chemical component associated to the specified species.
	 *
	 * @param species ChemicalSpecies associated to the new ChemicalComponent.
	 * By convention, the name of the component corresponds to the name of the
	 * associated species.
	 * @param total_concentration Initial total concentration of the component.
	 */
	public ChemicalComponent(ChemicalSpecies species, double total_concentration) {
		this.species = species;
		this.total_concentration = total_concentration;
	}

	/**
	 * Initializes a chemical component associated to a new ChemicalSpecies with
	 * the specified name and phase.
	 *
	 * @param name Name of the chemical species associated to the new
	 * chemical component. By convention, the name of the component corresponds
	 * to the name of the associated species.
	 * @param phase Phase of the chemical species associated to the new
	 * chemical component.
	 * @param total_concentration Initial total concentration of the component.
	 */
	public ChemicalComponent(String name, Phase phase, double total_concentration) {
		this(new ChemicalSpecies(name, phase, total_concentration), total_concentration);
	}

	/**
	 * Gets the species associated to this component.
	 *
	 * @return species associated to this component
	 */
	public ChemicalSpecies getSpecies() {
		return species;
	}

	/**
	 * {@summary Gets the name of the component, i.e. the name of its associated species.}
	 *
	 * @return name of this component
	 */
	public String getName() {
		return species.getName();
	}

	/**
	 * Gets the phase of the species associated to this component.
	 *
	 * @return phase of the species associated to this component
	 */
	public Phase getPhase() {
		return species.getPhase();
	}

	/**
	 * Gets the total concentration of this component.
	 *
	 * @return total concentration of this component
	 */
	public double getTotalConcentration() {
		return total_concentration;
	}

	/**
	 * Sets the total concentration of this component.
	 *
	 * This method should only be called internally, since it does not set up
	 * the internal {@code chemmisol-cpp} solver with the provided
	 * concentration: {@link
	 * ChemicalSystem#setTotalConcentration(ChemicalComponent, double)} must be
	 * used for this purpose.
	 *
	 * @param concentration total concentration of this component
	 */
	void setTotalConcentration(double concentration) {
		this.total_concentration = concentration;
	}
}
