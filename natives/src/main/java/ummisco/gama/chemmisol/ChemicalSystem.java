package ummisco.gama.chemmisol;

import java.lang.ref.Cleaner;
import java.util.Map;
import java.util.HashMap;

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

	public static class ChemmisolCoreException extends java.lang.Exception {
		ChemmisolCoreException(String chemmisol_message) {
			super("Exception thrown by the chemmisol core library: "
					+ chemmisol_message);
		}
	};

	private long chemical_system_ptr;
	private Map<String, ChemicalSpecies> tracked_species;

	public ChemicalSystem() {
		chemical_system_ptr = ChemicalSystem.allocate();
		this.clean_state = new CleanState(chemical_system_ptr);
		this.cleanable = cleaner.register(this, clean_state);
		this.tracked_species = new HashMap<String, ChemicalSpecies>();
	}

	private native static long allocate();
	private native static void dispose(long chemical_system_ptr);

	private native static void addReaction(long chemical_system_ptr, Reaction reaction);
	private native static void addComponent(long chemical_system_ptr, ChemicalComponent component);
	private native static void fixPH(long chemical_system_ptr, double ph);

	private native static void solve(long chemical_system_ptr) throws ChemmisolCoreException;

	private native static double concentration(
			long chemical_system_ptr, String component_name);

	public void addReaction(Reaction reaction) {
		ChemicalSystem.addReaction(chemical_system_ptr, reaction);
	}

	public void addComponent(ChemicalComponent component) {
		ChemicalSystem.addComponent(chemical_system_ptr, component);
		addSpecies(component.getSpecies());
	}

	public void addSpecies(ChemicalSpecies species) {
		tracked_species.put(species.getName(), species);
	}

	public void fixPH(double ph) {
		ChemicalSystem.fixPH(chemical_system_ptr, ph);
	}

	public void solve() throws ChemmisolCoreException {
		ChemicalSystem.solve(chemical_system_ptr);
		for(ChemicalSpecies c : tracked_species.values()) {
			c.setConcentration(concentration(chemical_system_ptr, c.getName()));
		}
	}

	public double concentration(String species_name) {
		return ChemicalSystem.concentration(chemical_system_ptr, species_name);
	}

	protected void dispose() {
		ChemicalSystem.dispose(chemical_system_ptr);
	}

	@Override
	public void close() {
		cleanable.clean();
	}
}
