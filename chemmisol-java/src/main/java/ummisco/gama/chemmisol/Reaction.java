package ummisco.gama.chemmisol;

import java.util.List;
import java.util.LinkedList;

/**
 * Describes a reaction that transforms <em>reactants</em> into
 * <em>products</em>.
 *
 * <p>
 * A reaction can be fully described by:
 * <ul>
 *   <li>a list of reactives and products (reagents)</li>
 *   <li>stoichiometric coefficients</li>
 *   <li>a log K value</li>
 * </ul>
 *
 * <p>
 * Let's consider the following example reaction:
 * <pre>
 *     {@code n A + m B <-> k C + l D}
 * </pre>
 * A and B are reactants, C and D are products and n, m, k and l are
 * corresponding stoichiometric coefficients.
 *
 * <p>
 * By convention, the reaction is rewritten as
 * <pre>
 *     {@code n A + m B - k C - l D <-> 0}
 * </pre>
 * so that stoichiometric coefficients of reactants are <b>positive</b> and
 * stoichiometric coefficients of products are <b>negative</b>.
 *
 * <p>
 * The <a href=https://en.wikipedia.org/wiki/Chemical_equilibrium>equilibrium
 * constant</a> K is then defined according to the <a
 * href=https://en.wikipedia.org/wiki/Law_of_mass_action>law of mass action</a>
 * as:
 * <pre>
 *    {@code ([C]^k * [D]^l) / ([A]^n * [B]^m) = K}
 * </pre>
 * where the bracket notation denotes the activity of each component <b>at
 * equilibrium</b>. By convention, the products form the numerator.
 * 
 * <p>
 * The Chemmisol convention is compliant with the conventions used by the
 * reference <a href=https://vminteq.com/>VMinteq</a> software, so that values
 * used in the VMinteq database can be reused as is in Chemmisol.
 *
 * Reactions must be specified so that all reagents are defined in the chemical
 * system as <b>components</b>, except <b>one reagent</b>, that represents the
 * <b>produced species</b> of the reaction.
 *
 * <h2>Examples</h2>
 *
 * <pre>
 *	try (ChemicalSystem system = new ChemicalSystem()) {
 *		{@code // Defines the reaction PO4-3 + 4 H+ <-> H4PO3}
 *		Reaction reaction = new Reaction("H4PO3", 13.192)
 *			.addReagent("H4PO3", -1, Phase.AQUEOUS)
 *			.addReagent("H+", 4, Phase.AQUEOUS)
 *			.addReagent("PO4-3", 1, Phase.AQUEOUS);
 *		system.addReaction(reaction);
 *
 *		// Implicitly defines H+ as a component
 *		system.fixPH(7);
 *		// Defines PO4-3 as a component
 *		system.addComponent(new ChemicalComponent(
 *			"PO4-3", Phase.AQUEOUS, 0.1
 *			);
 *
 *		// The H4PO3 species is automatically added to the system, as the
 *		produced species of the reaction
 *		system.setUp();
 *	}
 * </pre>
 *
 * @see ChemicalSystem#addComponent(ChemicalComponent)
 * @see ChemicalSystem#addReaction(Reaction)
 */

public class Reaction {
	private String name;
	private double log_K;
	private List<Reagent> reagents;

	/**
	 * Initializes a new Reaction.
	 *
	 * @param name Name of the reaction. By convention, generally corresponds to
	 * the name of the produced species of the reaction.
	 * @param log_K Value of log(K) where K is the equilibrium constant of the
	 * reaction.
	 */
	public Reaction(String name, double log_K) {
		this.name = name;
		this.log_K = log_K;
		this.reagents = new LinkedList<Reagent>();
	}

	/**
	 * Gets the name of this reaction.
	 *
	 * By convention, the name of the reaction generally corresponds to the name
	 * of its produced species. The name of each reaction must be unique within
	 * each chemical system.
	 *
	 * @return name of the reaction
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the base 10 logarithm of this reaction.
	 *
	 * <p>
	 * Let's consider the following example reaction:
	 * <pre>
	 *     {@code n A + m B <-> k C + l D}
	 * </pre>
	 *
	 * The log(K) value is such that at equilibrium:
	 * <pre>
	 *    {@code ([C]^k * [D]^l) / ([A]^n * [B]^m) = K}
	 * </pre>
	 *
	 * @return log(K) value of this reaction
	 */
	public double getLogK() {
		return log_K;
	}

	/**
	 * Adds a reagent to this reaction.
	 *
	 * Equivalent to {@code addReagent(new Reagent(name, coefficient, phase)}.
	 *
	 * @param name Name of the reagent.
	 * @param coefficient Stoichiometric coefficient associated to the reagent
	 * in this reaction.
	 * @param phase Phase of the reagent.
	 * @return This reaction.
	 *
	 * @see addReagent(Reagent)
	 */
	public Reaction addReagent(String name, int coefficient, Phase phase) {
		addReagent(new Reagent(name, coefficient, phase));
		return this;
	}

	/**
	 * Adds a reagent to this reaction.
	 *
	 * <p>
	 * The name of the reagent must correspond to the name of the species in the
	 * chemical system to which the reaction will be added. The corresponding
	 * species does not require to be already defined in the chemical system,
	 * but all reagents must be added to the reaction before {@link
	 * ChemicalSystem#addReaction(Reaction)} is called. While reagents
	 * corresponding to components must be defined explicitly in the chemical
	 * system, the produced species is automatically defined when the chemical
	 * system is set up.
	 *
	 * <p>
	 * By convention, negative coefficients are associated to products and
	 * positive coefficients are associated to reactants.
	 *
	 * @param reagent Reagent to add to this reaction.
	 * @return This reaction.
	 */
	public Reaction addReagent(Reagent reagent) {
		reagents.add(reagent);
		return this;
	}

	/**
	 * Gets the current list of reagents of this reaction.
	 *
	 * @return list of reagents
	 */
	public List<Reagent> getReagents() {
		return reagents;
	}
}
