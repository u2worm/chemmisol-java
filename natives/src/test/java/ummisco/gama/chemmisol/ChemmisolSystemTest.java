package ummisco.gama.chemmisol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ummisco.gama.Chemmisol;
import ummisco.gama.chemmisol.ChemicalSystem.ChemmisolCoreException;

/**
 * Unit test for Chemmisol.
 */
public class ChemmisolSystemTest 
{
	static {
		Chemmisol.loadLibraryFromDefaultProperties();
	}

	public static void assertDoubleEquals(double expected, double actual) {
		assertTrue(
				"expected:<" + expected + "> but was:<" + actual + ">",
				Math.abs(
					Double.doubleToLongBits(expected)-
					Double.doubleToLongBits(actual)) < 4
				);
	}

	/**
	 * Rigorous Test :-)
	 */
	@Test
	public void loadChemmisolLibrary()
	{
		try (ChemicalSystem system = new ChemicalSystem()) {
			assertNotNull(system);
		}
	}

	@Test
	public void addReaction()
	{
		try (ChemicalSystem system = new ChemicalSystem()) {
			Reaction test_reaction = new Reaction("H4PO3", 13.192)
				.addReagent("H4PO3", -1, Phase.AQUEOUS)
				.addReagent("H+", 4, Phase.AQUEOUS)
				.addReagent("PO4-3", 1, Phase.AQUEOUS);
			system.addReaction(test_reaction);
		}
	}

	@Test
	public void addComponent()
	{
		try (ChemicalSystem system = new ChemicalSystem()) {
			Reaction test_reaction = new Reaction("H4PO3", 13.192)
				.addReagent("H4PO3", -1, Phase.AQUEOUS)
				.addReagent("H+", 4, Phase.AQUEOUS)
				.addReagent("PO4-3", 1, Phase.AQUEOUS);
			system.addReaction(test_reaction);

			ChemicalComponent P = new ChemicalComponent(
					"PO4-3", Phase.AQUEOUS, 0.1
					);
			system.addComponent(P);
			system.addComponent(new Solvent("H2O"));
		}
	}

	@Test
	public void fixPH()
	{
		try (ChemicalSystem system = new ChemicalSystem()) {
			system.fixPH(7.5);
			assertEquals(system.concentration("H+"), Math.pow(10, -7.5), 1e-15);
		}
	}

	@Test
	public void solve() throws ChemmisolCoreException
	{
		try (ChemicalSystem system = new ChemicalSystem()) {
			Reaction test_reaction = new Reaction("H4PO3", 13.192)
				.addReagent("H4PO3", -1, Phase.AQUEOUS)
				.addReagent("H+", 4, Phase.AQUEOUS)
				.addReagent("PO4-3", 1, Phase.AQUEOUS);
			system.addReaction(test_reaction);

			ChemicalComponent PO4 = new ChemicalComponent("PO4-3", Phase.AQUEOUS, 0.1);
			system.addComponent(PO4);
			ChemicalSpecies H4PO3 = new ChemicalSpecies("H4PO3", Phase.AQUEOUS);
			system.addSpecies(H4PO3);

			system.fixPH(7.5);

			system.solve();

			// 4 ULP's comparison (see
			// https://randomascii.wordpress.com/2012/02/25/comparing-floating-point-numbers-2012-edition/)
			assertDoubleEquals(
					Math.pow(10, 13.192),
					H4PO3.getConcentration()/(
						PO4.getConcentration()*Math.pow(system.concentration("H+"), 4)
						)
					);
		}
	}

	@Test(expected = ChemmisolCoreException.class)
	public void solveMissingSpeciesInReactionException() throws ChemmisolCoreException {
		try (ChemicalSystem system = new ChemicalSystem()) {
			// Too many components specified, so that the reaction has no
			// "produced species".
			system.addComponent(new Solvent("H2O"));
			system.addComponent(new ChemicalComponent("H2", Phase.AQUEOUS, 0.1));
			system.addComponent(new ChemicalComponent("O2", Phase.AQUEOUS, 0.1));

			system.addReaction(new Reaction("O2", 13.12)
				.addReagent("H2O", 2, Phase.AQUEOUS)
				.addReagent("H2", -2, Phase.AQUEOUS)
				.addReagent("O2", -1, Phase.AQUEOUS));

			system.solve();
		}
	}

	@Test(expected = ChemmisolCoreException.class)
	public void solveInvalidSpeciesInReactionException() throws ChemmisolCoreException {
		try (ChemicalSystem system = new ChemicalSystem()) {
			// Missing components, so that the reaction has too many "produced
			// species".
			system.addComponent(new Solvent("H2O"));

			system.addReaction(new Reaction("O2", 13.12)
				.addReagent("H2O", 2, Phase.AQUEOUS)
				.addReagent("H2", -2, Phase.AQUEOUS)
				.addReagent("O2", -1, Phase.AQUEOUS));

			system.solve();
		}
	}
}

