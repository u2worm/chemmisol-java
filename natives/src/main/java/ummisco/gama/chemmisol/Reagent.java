package ummisco.gama.chemmisol;

public class Reagent extends ChemicalSpecies {
	private int coefficient;

	public Reagent(String name, int coefficient, Phase phase) {
		super(name, phase);
		this.coefficient = coefficient;
	}

	int getCoefficient() {
		return coefficient;
	}
}
