package ummisco.gama.chemmisol;

import java.lang.ref.Cleaner;

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

	public ChemicalSystem() {
		chemical_system_ptr = this.allocate();
		this.clean_state = new CleanState(this);
		this.cleanable = cleaner.register(this, clean_state);
	}

	private native long allocate();
	private native void dispose(long chemical_system_ptr);

	private native void addReaction(long chemical_system_ptr, Reaction reaction);
	private native void addComponent(long chemical_system_ptr, Component component);

	public void addReaction(Reaction reaction) {
		this.addReaction(chemical_system_ptr, reaction);
	}

	public void addComponent(Component component) {
		this.addComponent(chemical_system_ptr, component);
	}

	protected void dispose() {
		this.dispose(chemical_system_ptr);
	}

	@Override
	public void close() {
		cleanable.clean();
	}
}
