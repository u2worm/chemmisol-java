package ummisco.gama.chemmisol;

public class ChemicalComponent {
	private ChemicalSpecies species;
	private double total_concentration;

	public ChemicalComponent(ChemicalSpecies species, double total_concentration) {
		this.species = species;
		this.total_concentration = total_concentration;
	}

	public ChemicalComponent(String name, Phase phase, double total_concentration) {
		this(new ChemicalSpecies(name, phase, total_concentration), total_concentration);
	}

	public ChemicalSpecies getSpecies() {
		return species;
	}

	public String getName() {
		return species.getName();
	}

	public Phase getPhase() {
		return species.getPhase();
	}

	public double getConcentration() {
		return species.getConcentration();
	}

	public double getTotalConcentration() {
		return total_concentration;
	}

	void setTotalConcentration(double concentration) {
		this.total_concentration = concentration;
	}
}
