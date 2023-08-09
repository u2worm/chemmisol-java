package ummisco.gama.chemmisol;

public class ChemicalComponent {
	private ChemicalSpecies species;

	public ChemicalComponent(String name, Phase phase, double total_concentration) {
		this.species = new ChemicalSpecies(name, phase, total_concentration);
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
}
