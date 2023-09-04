package ummisco.gama.chemmisol;

/**
 * Describes a solvent (such as H2O), that is a chemical component always in
 * excess, with a fixed concentration and activity.
 *
 * <p>
 * The solvent can be added to a chemical system using {@link
 * ChemicalSystem#addComponent(ChemicalComponent)}.
 */
public class Solvent extends ChemicalComponent {
	/**
	 * Initializes a new solvent.
	 *
	 * @param name Name of the solvent.
	 */
	public Solvent(String name) {
		super(name, Phase.SOLVENT, 1.0);
	}
};
