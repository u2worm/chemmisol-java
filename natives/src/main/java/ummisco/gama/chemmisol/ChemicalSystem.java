package ummisco.gama.chemmisol;

import java.lang.ref.Cleaner;
import java.util.Map;
import java.util.HashMap;

public class ChemicalSystem implements AutoCloseable {
	private static final Cleaner cleaner = Cleaner.create();
	private static class CleanState implements Runnable {
		private ChemicalSystem system;

		CleanState(ChemicalSystem system) {
			this.system = system;
		}

		@Override
		public void run() {
			system.dispose();
		}
	}
	private final CleanState clean_state;
	private final Cleaner.Cleanable cleanable;

	private long chemical_system_ptr;
	private Map<String, Component> tracked_components;

	public ChemicalSystem() {
		chemical_system_ptr = this.allocate();
		this.clean_state = new CleanState(this);
		this.cleanable = cleaner.register(this, clean_state);
		this.tracked_components = new HashMap<String, Component>();
	}

	private native long allocate();
	private native void dispose(long chemical_system_ptr);

	private native void addReaction(long chemical_system_ptr, Reaction reaction);
	private native void addComponent(long chemical_system_ptr, Component component);
	private native void fixPH(long chemical_system_ptr, double ph);

	private native void solve(long chemical_system_ptr);

	private native double concentration(
			long chemical_system_ptr, String component_name);

	public void addReaction(Reaction reaction) {
		this.addReaction(chemical_system_ptr, reaction);
	}

	public void track(Component component) {
		tracked_components.put(component.getName(), component);
	}

	public void addComponent(Component component) {
		this.addComponent(chemical_system_ptr, component);
		track(component);
	}

	public void fixPH(double ph) {
		this.fixPH(chemical_system_ptr, ph);
	}

	public void solve() {
		this.solve(chemical_system_ptr);
		for(Component c : tracked_components.values()) {
			c.setConcentration(concentration(chemical_system_ptr, c.getName()));
		}
	}

	public double concentration(String component_name) {
		return this.concentration(chemical_system_ptr, component_name);
	}

	protected void dispose() {
		this.dispose(chemical_system_ptr);
	}

	@Override
	public void close() {
		cleanable.clean();
	}
}
