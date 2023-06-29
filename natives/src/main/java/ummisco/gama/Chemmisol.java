package ummisco.gama;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Properties;

public class Chemmisol 
{
	public static void loadLibrary(String project_properties, String lib_directory_property) {
		// Load library
		InputStream is = Chemmisol.class.getClassLoader()
			.getResourceAsStream(project_properties);
		Properties properties = new Properties();
		try {
			properties.load(is);
		} catch (IOException e) {
			System.err.println(e);
		}
		Path libchemmisol = FileSystems.getDefault().getPath(
				properties.getProperty(lib_directory_property)
				).resolve(System.mapLibraryName("chemmisol-java"))
			.toAbsolutePath();
		System.out.println("Loading chemmisol library from " + libchemmisol.toString());
		System.load(libchemmisol.toString());
	}

    public static void loadLibrary() {
        // Default location set up in the ummisco.gama.chemmisol.setup artifact
        // (see the corresponding pom.xml)
        loadLibrary("chemmisol.setup.properties", "cmake.build.directory");
    }
}
