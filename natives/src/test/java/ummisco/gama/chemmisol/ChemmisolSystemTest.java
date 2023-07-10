package ummisco.gama.chemmisol;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ummisco.gama.Chemmisol;

/**
 * Unit test for Chemmisol.
 */
public class ChemmisolSystemTest 
{
	/**
	 * Rigorous Test :-)
	 */
	@Test
	public void loadChemmisolLibrary()
	{
		Chemmisol.loadLibraryFromDefaultProperties();
		try (ChemicalSystem system = new ChemicalSystem()) {
			Reaction test_reaction = new Reaction("H4PO3", 13.192)
				.addReagent("H+", 4, Phase.AQUEOUS)
				.addReagent("PO4-3", 1, Phase.AQUEOUS);
			system.addReaction(test_reaction);

			Component P = new Component("PO4-3", Phase.AQUEOUS, 0.1);
			system.addComponent(P);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}

