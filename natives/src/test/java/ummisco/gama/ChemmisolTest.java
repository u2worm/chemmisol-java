package ummisco.gama;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ummisco.gama.chemmisol.ChemicalSystem;

/**
 * Unit test for Chemmisol.
 */
public class ChemmisolTest 
{
	/**
	 * Rigorous Test :-)
	 */
	@Test
	public void loadChemmisolLibrary()
	{
		Chemmisol.loadLibrary("chemmisol.java.properties", "cmake.build.directory");
		ChemicalSystem system = new ChemicalSystem();
		system.dispose();
	}
}
