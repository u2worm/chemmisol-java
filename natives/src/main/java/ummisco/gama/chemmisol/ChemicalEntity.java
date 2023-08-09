package ummisco.gama.chemmisol;

public class ChemicalEntity {
	private String name;
	private Phase phase;

	public ChemicalEntity(String name, Phase phase) {
		this.name = name;
		this.phase = phase;
	}

	public String getName() {
		return name;
	}

	protected void setName(String name) {
		this.name = name;
	}

	public Phase getPhase() {
		return phase;
	}
}
