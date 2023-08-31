package ummisco.gama.chemmisol;

import java.util.List;
import java.util.LinkedList;

public class Reaction {
	private String name;
	private double log_K;
	private List<Reagent> reagents;

	public Reaction(String name, double log_K) {
		this.name = name;
		this.log_K = log_K;
		this.reagents = new LinkedList<Reagent>();
	}

	public String getName() {
		return name;
	}

	public double getLogK() {
		return log_K;
	}

	public Reaction addReagent(String name, int coefficient, Phase phase) {
		addReagent(new Reagent(name, coefficient, phase));
		return this;
	}

	public Reaction addReagent(Reagent reagent) {
		reagents.add(reagent);
		return this;
	}

	public List<Reagent> getReagents() {
		return reagents;
	}
}
