package ummisco.gama.chemmisol;

public class ChemicalSpecies {
	private String name;
	private Phase phase;

	public ChemicalSpecies(String name, Phase phase) {
		this.name = name;
		this.phase = phase;
	}

	public String getName() {
		return name;
	}

	public Phase getPhase() {
		return phase;
	}
}
