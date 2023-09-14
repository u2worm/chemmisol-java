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
	public void addComponent() throws ChemmisolCoreException
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
	public void fixPHinComponent() throws ChemmisolCoreException
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

	@Test(expected = ChemmisolCoreException.class)
	public void addInvalidMineralComponent() throws ChemmisolCoreException {
		try (ChemicalSystem system = new ChemicalSystem()) {
			system.addComponent(new ChemicalComponent("=SOH", Phase.MINERAL, 1.0));
		}
	}

	@Test
	public void sitesQuantity() {
		try (ChemicalSystem system = new ChemicalSystem(
					2.5, // g/l
					24.2, // m2/g
					0.8 * 1e18 / 6.02214076e23 // 0.8 entitities/nm2
					)) {
			// Temporary
			double system_volume = 1.0;
			assertDoubleEquals(
					2.5 * 24.2 * 0.8 * 1e18 / 6.02214076e23 * system_volume,
					system.sitesQuantity());
					}
	}

	@Test
	public void solveMineralEquilibrium() throws ChemmisolCoreException {
		try (ChemicalSystem system = new ChemicalSystem(
					2.5, // g/l
					24.2, // m2/g
					0.8 * 1e18 / 6.02214076e23 // 0.8 entitities/nm2
					)) {

			ChemicalComponent surface_complex
				= new ChemicalComponent("=SOH", Phase.MINERAL, 1.0);
			system.addComponent(surface_complex);
			system.addComponent(new Solvent("H2O"));

			system.addReaction(new Reaction("HO-", -14)
					.addReagent("HO-", -1, Phase.AQUEOUS)
					.addReagent("H", -1, Phase.AQUEOUS)
					.addReagent("H2O", 1, Phase.AQUEOUS));
			system.addReaction(new Reaction("=SOH2", 3.46)
					.addReagent("=SOH2", -1, Phase.MINERAL)
					.addReagent("=SOH", 1, Phase.AQUEOUS)
					.addReagent("H", 1, Phase.AQUEOUS));

			ChemicalComponent H = new ChemicalComponent("H", Phase.AQUEOUS, 0.0);
			system.addComponent(H);
			ChemicalSpecies SOH2 = new ChemicalSpecies("=SOH2", Phase.MINERAL, 0.0);
			system.addSpecies(SOH2);
			system.fixPH(7, H);
			system.solve();

			assertDoubleEquals(
					system.reactionQuotient("=SOH2"),
					SOH2.getConcentration() /
					(surface_complex.getSpecies().getConcentration() * H.getSpecies().getConcentration())
					);
		}
	}
}

