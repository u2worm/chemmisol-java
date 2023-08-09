package ummisco.gama.chemmisol;

public class ChemicalSpecies extends ChemicalEntity {
	private double concentration;

	ChemicalSpecies(String name, Phase phase, double concentration) {
		super(name, phase);
		this.concentration = concentration;
	}

	public ChemicalSpecies(String name, Phase phase) {
		this(name, phase, 0.0);
	}

	public double getConcentration() {
		return concentration;
	}

	public void setConcentration(double concentration) {
		this.concentration = concentration;
	}
}
