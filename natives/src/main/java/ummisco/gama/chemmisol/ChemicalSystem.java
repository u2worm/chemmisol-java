package ummisco.gama.chemmisol;

public class ChemicalSystem {
	private long chemical_system_ptr;

	public ChemicalSystem() {
		chemical_system_ptr = this.allocate();
	}

	private native long allocate();
	private native void dispose(long chemical_system_ptr);
	public void dispose() {
		this.dispose(chemical_system_ptr);
	}
}
