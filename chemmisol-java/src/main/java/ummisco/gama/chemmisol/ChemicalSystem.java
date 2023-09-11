package ummisco.gama.chemmisol;

import java.lang.ref.Cleaner;
import java.util.Map;
import java.util.HashMap;

/**
 * Describes a chemical system, where components reacts to form other chemical
 * species according to chemical reactions.
 *
 * <p>
 * This class is responsible for most of the native calls to the <a
 * href=https://github.com/u2worm/chemmisol-cpp>chemmisol-cpp</a> library.
 *
 * <p>
 * In order to guarantee a proper release of the C++ memory resources,
 * ChemicalSystem implements the AutoClosable interface so that it can be used
 * in a <a
 * href=https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html>try-with-resources</a>
 * block:
 * <pre>
 * try(ChemicalSystem system = new ChemicalSystem()) {
 * 	...
 * }
 * </pre>
 *
 * All quantities must currently be specified in <b>core units</b> of the
 * <a href=https://u2worm.github.io/chemmisol-cpp/index.html>chemmisol unit system</a>.
 */
public class ChemicalSystem implements AutoCloseable {
	private static final Cleaner cleaner = Cleaner.create();
	private static class CleanState implements Runnable {
		private long chemical_system_ptr;

		CleanState(long chemical_system_ptr) {
			this.chemical_system_ptr = chemical_system_ptr;
		}

		@Override
		public void run() {
			System.out.println("[CHEMMISOL] Clean ChemicalSystem " + chemical_system_ptr);
			ChemicalSystem.dispose(chemical_system_ptr);
		}
	}
	private final CleanState clean_state;
	private final Cleaner.Cleanable cleanable;

	/**
	 * Wrapper around exceptions thrown by the native {@code chemmisol-cpp} library.
	 *
	 * <p>
	 * The exception message contains the message of the original native
	 * exception.
	 */
	public static class ChemmisolCoreException extends java.lang.Exception {
		/**
		 * Initializes a new ChemmisolCoreException from the message of the
		 * native {@code chemmisol-cpp} exception.
		 *
		 * <p>
		 * Such object is likely to be created from the native JNI code.
		 *
		 * @param chemmisol_message Message of the original native exception.
		 */
		ChemmisolCoreException(String chemmisol_message) {
			super("Exception thrown by the chemmisol core library: "
					+ chemmisol_message);
		}
	};

	private long chemical_system_ptr;
	private Map<String, ChemicalSpecies> tracked_species;

	private ChemicalSystem(long chemical_system_ptr) {
		this.chemical_system_ptr = chemical_system_ptr;
		this.clean_state = new CleanState(this.chemical_system_ptr);
		this.cleanable = cleaner.register(this, clean_state);
		this.tracked_species = new HashMap<String, ChemicalSpecies>();
	}

	/**
	 * Initializes a default chemical system.
	 */
	public ChemicalSystem() {
		this(ChemicalSystem.allocate());
	}

	/**
	 * Initializes a mineral chemical system.
	 *
	 * Example units are provided for each parameter for a better understanding,
	 * but values must at least be specified so that the value of
	 * <code>solid_concentration * specific_surface_area *
	 * site_concentration</code> is expressed in mol/l. The simplest way is to
	 * only use chemmisol core units, i.e. g/l, m2/g and mol/m2.
	 *
	 * @param solid_concentration Quantity of mineral in suspension in
	 * the solution, expressed in g/l.
	 * @param specific_surface_area Surface of the solid in contact with
	 * the solution per unit of mass, usually expressed in m2/g.
	 * @param site_concentration Quantity of sites per unit of surface
	 * in contact with the solution, usually expressed as entities/nm2.
	 * @param surface_complex Name of the surface complex (for example, =SOH). A
	 * mineral component with this name is automatically added.
	 */
	public ChemicalSystem(
			double solid_concentration,
			double specific_surface_area,
			double site_concentration,
			String surface_complex) {
		this(ChemicalSystem.allocate(
					solid_concentration, specific_surface_area, site_concentration,
					surface_complex
					));
			}

	private native static long allocate();
	private native static long allocate(
			double solid_concentration,
			double specific_surface_area,
			double site_concentration,
			String surface_complex);
	private native static void dispose(long chemical_system_ptr);

	private native static void addReaction(long chemical_system_ptr, Reaction reaction);
	private native static void addComponent(long chemical_system_ptr, ChemicalComponent component);
	private native static void fixPH(long chemical_system_ptr, double ph, String string);
	private native static void setTotalConcentration(long chemical_system_ptr, String component, double concentration);
	private native static void setUp(long chemical_system_ptr) throws ChemmisolCoreException;
	private native static void solve(long chemical_system_ptr) throws ChemmisolCoreException;

	private native static double concentration(
			long chemical_system_ptr, String component_name);
	private native static double reactionQuotient(
			long chemical_system_ptr, String reaction_name);

	/**
	 * Adds a new reaction to the chemical system.
	 *
	 * <p>
	 * Each reaction must have a unique name within this chemical system.
	 *
	 * <p>
	 * It is the responsability of the user to properly define reactions and
	 * components so that reagents of the reaction all correspond to chemical
	 * components, except the produced species (see {@link Reaction}).
	 *
	 * <p>
	 * For a reaction to be valid, components only need to be defined when
	 * {@link setUp()} is called.
	 *
	 * @param reaction Reaction to add to this chemical system.
	 */
	public void addReaction(Reaction reaction) {
		ChemicalSystem.addReaction(chemical_system_ptr, reaction);
	}

	/**
	 * Adds a component to this chemical system.
	 *
	 * <p>
	 * Components are canonical components used to specify reactions. The
	 * species associated to the component is automatically added to this
	 * chemical system.
	 *
	 * @param component Component to add to this chemical system.
	 *
	 * @throws ChemmisolCoreException if an exception occurs while setting up
	 * the chemical system in the native {@code chemmisol-cpp} library.
	 *
	 * @see addSpecies(ChemicalSpecies)
	 */
	public void addComponent(ChemicalComponent component) throws ChemmisolCoreException {
		ChemicalSystem.addComponent(chemical_system_ptr, component);
		addSpecies(component.getSpecies());
	}

	/**
	 * Adds a species to this chemical system.
	 *
	 * <p>
	 * Any concentration change resulting from the equilibrium solving is
	 * automatically reported in the provided ChemicalSpecies instance. The
	 * concentration of species <b>not</b> added with addSpecies() can still be
	 * queried using the {@link concentration(String)} method.
	 *
	 * @param species Species to add to this chemical system.
	 *
	 * @see solve()
	 */
	public void addSpecies(ChemicalSpecies species) {
		tracked_species.put(species.getName(), species);
	}

	/**
	 * Fixes the pH, using the provided species as the species representing
	 * the H+ ions.
	 *
	 * @param ph pH value.
	 * @param h_species Chemical species instance representing the H+ ions.
	 */
	private void fixPH(double ph, ChemicalSpecies h_species) {
		fixPH(chemical_system_ptr, ph, h_species.getName());
		h_species.setConcentration(
				concentration(chemical_system_ptr, h_species.getName())
				);
	}

	/**
	 * Fixes the pH, using the provided component as the chemical component
	 * representing the H+ ions.
	 *
	 * <p>
	 * The concentration of the associated component is set to {@code 10^-pH}.
	 *
	 * @param ph pH value.
	 * @param h_component Chemical component instance representing the H+ ions.
	 */
	public void fixPH(double ph, ChemicalComponent h_component) {
		fixPH(ph, h_component.getSpecies());
	}

	/**
	 * Fixes the pH in a default component named "H+".
	 *
	 * <p>
	 * If a species name H+ is already defined in the chemical system, this
	 * species is used as the species representing H+ ions. Else, the pH value
	 * is only set internally in the {@code chemmisol-cpp} library, but can be
	 * queried with {@link concentration(String) concentration("H+")}.
	 *
	 * @param ph pH value
	 */
	public void fixPH(double ph) {
		ChemicalSpecies h_species = tracked_species.get("H+");
		if(h_species != null) {
			fixPH(ph, h_species);
		} else {
			fixPH(chemical_system_ptr, ph, "H+");
		}
	}

	/**
	 * Sets the total concentration of the provided component.
	 *
	 * <p>
	 * This updates the value returned by
	 * ChemicalComponent#getTotalConcentration(), and sets the total
	 * concentration of the component within the internal {@code chemmisol-cpp}
	 * solver instance.
	 *
	 * The total concentration must be expressed in mol/l for aqueous species,
	 * and as a molar fraction (without unit) for mineral species.
	 *
	 * @param component Chemical component to set with the specified total
	 * concentration.
	 * @param total_concentration Total concentration of the chemical component.
	 */
	public void setTotalConcentration(ChemicalComponent component, double total_concentration) {
		setTotalConcentration(chemical_system_ptr, component.getName(), total_concentration);
		component.setTotalConcentration(total_concentration);
	}

	/**
	 * Sets up the system so that it is ready to be solved.
	 *
	 * <p>
	 * This method must be called before {@link solve()} once all reactions and
	 * components have been added, and each time the total concentration of a
	 * component is updated.
	 *
	 * @throws ChemmisolCoreException if an exception occurs while setting up
	 * the chemical system in the native {@code chemmisol-cpp} library.
	 *
	 * @see <a
	 * href=https://u2worm.github.io/chemmisol-cpp/classchemmisol_1_1ChemicalSystem.html#ad9e33028299d87c93093dad0717bef63>chemmisol-cpp
	 * documentation</a>
	 */
	public void setUp() throws ChemmisolCoreException {
		setUp(chemical_system_ptr);
	}

	/**
	 * Solves the equilibrium state of this chemical system using the native <a
	 * href=https://u2worm.github.io/chemmisol-cpp/classchemmisol_1_1ChemicalSystem.html#a36ceca64f849c2b410657d3c203f0189>chemmisol-cpp
	 * solver</a>.
	 *
	 * <p>
	 * The concentration of all species added explicitly with {@link
	 * addSpecies(ChemicalSpecies) addSpecies()} or implicitly with {@link
	 * addComponent(ChemicalComponent) addComponent()} are updated according to
	 * the solved equilibrium state, and is available using the {@link
	 * concentration(String)} method.
	 *
	 * @throws ChemmisolCoreException if an exception occurs within the native
	 * {@code chemmisol-cpp} solver.
	 */
	public void solve() throws ChemmisolCoreException {
		solve(chemical_system_ptr);
		for(ChemicalSpecies c : tracked_species.values()) {
			c.setConcentration(concentration(chemical_system_ptr, c.getName()));
		}
	}

	/**
	 * Gets the concentration of the species with the specified name directly
	 * from the native {@code chemmisol-cpp} library.
	 *
	 * <p>
	 * This method might be useful to retrieve the concentration of a species
	 * that exists in the system but not added explicitly with {@link
	 * addSpecies(ChemicalSpecies) addSpecies()} or implicitly with {@link
	 * addComponent(ChemicalComponent) addComponent()}.
	 *
	 * @param species_name Name of a chemical species.
	 * @return Internal concentration of the species named species_name.
	 */
	public double concentration(String species_name) {
		return concentration(chemical_system_ptr, species_name);
	}

	/**
	 * Returns the reaction quotient of the specified reaction.
	 *
	 * <p>
	 * The reaction must have been added to the system with {@link
	 * addReaction(Reaction)}.
	 *
	 * <p>
	 * For a reaction defined as
	 * <pre>
	 *     {@code n A + m B <-> k C + l D}
	 * </pre>
	 * the reaction quotient is computed as
	 * <pre>
	 *    {@code Q = ([C]^k * [D]^l) / ([A]^n * [B]^m)}
	 * </pre>
	 * 
	 * @param reaction Reaction instance within this chemical system.
	 * @return Reaction quotient of the reaction.
	 */
	public double reactionQuotient(Reaction reaction) {
		return reactionQuotient(chemical_system_ptr, reaction.getName());
	}

	/**
	 * Returns the reaction quotient of the reaction named {@code reaction_name}
	 * in this chemical system.
	 *
	 * <p>
	 * A reaction with the specified name must have been added to the system
	 * with {@link addReaction(Reaction)}.
	 * 
	 * @param reaction_name Name of a reaction in this chemical system.
	 * @return Reaction quotient of the reaction.
	 *
	 * @see reactionQuotient(Reaction)
	 */
	public double reactionQuotient(String reaction_name) {
		return reactionQuotient(chemical_system_ptr, reaction_name);
	}

	/**
	 * Releases C++ memory resources used by this ChemicalSystem instance.
	 */
	protected void dispose() {
		ChemicalSystem.dispose(chemical_system_ptr);
	}

	@Override
	public void close() {
		cleanable.clean();
	}
}
