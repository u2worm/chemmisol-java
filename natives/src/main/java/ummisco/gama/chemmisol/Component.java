package ummisco.gama.chemmisol;

public class Component extends ChemicalSpecies {
	private double concentration;

	protected Component(String name, Phase phase, double concentration) {
		super(name, phase);
		this.concentration = concentration;
	}

	public double getConcentration() {
		return concentration;
	}

	public void setConcentration(double concentration) {
		this.concentration = concentration;
	}
}
