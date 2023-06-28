package ummisco.gama.chemmisol;

class ChemicalSystem {
  private long chemical_system_ptr;

  public ChemicalSystem() {
  }

  private native long allocate();
  private native void dispose(long chemical_system_ptr);
}
