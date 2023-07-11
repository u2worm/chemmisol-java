package ummisco.gama.chemmisol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ummisco.gama.Chemmisol;

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
		} catch (Exception e) {
			// TODO: handle exception
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
		} catch (Exception e) {
			// TODO: handle exception
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

			Component P = new Component("PO4-3", Phase.AQUEOUS, 0.1);
			system.addComponent(P);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Test
	public void fixPH()
	{
		try (ChemicalSystem system = new ChemicalSystem()) {
			system.fixPH(7.5);
			assertEquals(system.concentration("H+"), Math.pow(10, -7.5), 1e-15);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Test
	public void solve()
	{
		try (ChemicalSystem system = new ChemicalSystem()) {
			Reaction test_reaction = new Reaction("H4PO3", 13.192)
				.addReagent("H4PO3", -1, Phase.AQUEOUS)
				.addReagent("H+", 4, Phase.AQUEOUS)
				.addReagent("PO4-3", 1, Phase.AQUEOUS);
			system.addReaction(test_reaction);

			Component PO4 = new Component("PO4-3", Phase.AQUEOUS, 0.1);
			system.addComponent(PO4);
			Component H4PO3 = new Component("H4PO3", Phase.AQUEOUS, 0);
			system.addComponent(H4PO3);

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
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}

