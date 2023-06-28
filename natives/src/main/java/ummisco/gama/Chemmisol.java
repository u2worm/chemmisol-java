package ummisco.gama;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Properties;

public class Chemmisol 
{
	static {
		// Load library
		InputStream is = Chemmisol.class.getClassLoader()
			.getResourceAsStream("chemmisol.java.properties");
		Properties properties = new Properties();
		try {
			properties.load(is);
		} catch (IOException e) {
			System.err.println(e);
		}
		Path jsoncpp_library = FileSystems.getDefault().getPath(
				properties.getProperty("cmake.build.directory")
				).resolve("lib").resolve(System.mapLibraryName("jsoncpp-java"))
			.toAbsolutePath();
		System.out.println("Loading jsoncpp library from " + jsoncpp_library.toString());
		System.load(jsoncpp_library.toString());
	}
}
