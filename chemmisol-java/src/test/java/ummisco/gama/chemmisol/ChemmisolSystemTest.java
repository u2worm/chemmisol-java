package ummisco.gama.chemmisol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.IOException;

import ummisco.gama.chemmisol.ChemicalSystem.ChemmisolCoreException;

/**
 * Unit test for Chemmisol.
 */
public class ChemmisolSystemTest 
{
	static {
		try {
			Chemmisol.loadChemmisolLibrariesFromProperties(
					ChemmisolSystemTest.class,
					// Test properties, see the pom.xml in setup directory
					"chemmisol.setup.properties",
					"cmake.build.directory"
					);
		} catch(IOException e) {
			System.err.println(e);
		}
	}

	/**
	 * 4 ULP's comparison (see
	 * https://randomascii.wordpress.com/2012/02/25/comparing-floating-point-numbers-2012-edition/)
	 */
	public static void assertDoubleEquals(double expected, double actual) {
		assertTrue(
				"expected:<" + expected + "> but was:<" + actual + ">",
				Math.abs(
					Double.doubleToLongBits(expected)-
					Double.doubleToLongBits(actual)) < 4
				);
	}

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
	public void fixPHinDefaultComponent()
	{
		try (ChemicalSystem system = new ChemicalSystem()) {
			system.fixPH(7.5);
			assertEquals(system.concentration("H+"), Math.pow(10, -7.5), 1e-15);
		}
	}

	@Test
	public void fixPHinComponent()
	{
		try (ChemicalSystem system = new ChemicalSystem()) {
			ChemicalComponent h = new ChemicalComponent("h", Phase.AQUEOUS, 0);
			system.addComponent(h);
			system.fixPH(7.5, h);
			assertEquals(h.getSpecies().getConcentration(), Math.pow(10, -7.5), 1e-15);
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

			assertDoubleEquals(
					PO4.getTotalConcentration(), 0.1
					);
			assertDoubleEquals(
					PO4.getSpecies().getConcentration() + H4PO3.getConcentration(),
					PO4.getTotalConcentration()
					);
			assertDoubleEquals(
					Math.pow(10, 13.192),
					H4PO3.getConcentration()/(
						PO4.getSpecies().getConcentration()*Math.pow(system.concentration("H+"), 4)
						)
					);
		}
	}

	@Test
	public void setTotalConcentration() throws ChemmisolCoreException {
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
			// Solves the system to reach a first equilibrium with a total P
			// concentration of 0.1
			system.solve();

			// Sets the total concentration of P to 0.27 and solves the new
			// equilibrium
			system.setTotalConcentration(PO4, 0.27);
			system.solve();

			assertDoubleEquals(
					PO4.getTotalConcentration(), 0.27
					);
			assertDoubleEquals(
					PO4.getSpecies().getConcentration() + H4PO3.getConcentration(),
					PO4.getTotalConcentration()
					);
			assertDoubleEquals(
					Math.pow(10, 13.192),
					H4PO3.getConcentration()/(
						PO4.getSpecies().getConcentration()*Math.pow(system.concentration("H+"), 4)
						)
					);
		}

	}

	@Test
	public void reactionQuotient() throws ChemmisolCoreException
	{
		try (ChemicalSystem system = new ChemicalSystem()) {
			Reaction test_reaction = new Reaction("H4PO3", 13.192)
				.addReagent("H4PO3", -1, Phase.AQUEOUS)
				.addReagent("H", 4, Phase.AQUEOUS)
				.addReagent("PO4-3", 1, Phase.AQUEOUS);
			system.addReaction(test_reaction);

			ChemicalComponent PO4 = new ChemicalComponent("PO4-3", Phase.AQUEOUS, 0.1);
			system.addComponent(PO4);
			ChemicalSpecies H4PO3 = new ChemicalSpecies("H4PO3", Phase.AQUEOUS);
			system.addSpecies(H4PO3);
			ChemicalComponent H = new ChemicalComponent("H", Phase.AQUEOUS, 0.0);
			
			system.fixPH(7.5, H);

			system.solve();

			assertDoubleEquals(
					system.reactionQuotient("H4PO3"),
					H4PO3.getConcentration() /
					(PO4.getSpecies().getConcentration() * Math.pow(H.getSpecies().getConcentration(), 4))
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

