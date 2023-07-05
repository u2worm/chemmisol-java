package ummisco.gama;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Properties;
import java.net.URL;
import java.lang.Class;

public class Chemmisol 
{
	private static final String FILE_PROTOCOL = "file";
	private static final String RESOURCE_BUNDLE_PROTOCOL = "bundleresource";
	private static final String OS_LIBRARY_NAME = System.mapLibraryName("chemmisol-java");

	public static void loadLibraryFromResource(Class<?> clazz, String resource_path)
			throws IOException{
			Chemmisol.loadLibrary(clazz, clazz.getResource(
						resource_path + OS_LIBRARY_NAME
						));
	}

	public static void loadLibraryFromResource(Class<?> clazz)
			throws IOException{
			loadLibraryFromResource(clazz, "");
	}

	private static void loadLibrary(Class<?> clazz, URL url_to_chemmisol_library)
			throws IOException {
		System.out.println("Loading native chemmisol-cpp library from "
				+ url_to_chemmisol_library.toString());
		switch(url_to_chemmisol_library.getProtocol()) {
			case FILE_PROTOCOL:
				loadLibrary(url_to_chemmisol_library.getPath());
				break;
			case RESOURCE_BUNDLE_PROTOCOL:
				try {
					// Creates a temporary file that will contain the unpacked
					// native library
					File temp_file = File.createTempFile(OS_LIBRARY_NAME, ".tmp")
						.getAbsoluteFile();
					temp_file.deleteOnExit();

					// Gets the native library as a binary stream, directly from
					// the JAR
					InputStream library_bin = clazz.getClassLoader()
						.getResourceAsStream(url_to_chemmisol_library.getPath());

					// Unpacks the library to the temporary file
					library_bin.transferTo(new FileOutputStream(temp_file));

					// Loads the native library from the temporary file
					loadLibrary(temp_file.getAbsolutePath());
				} catch(IOException e) {
					System.err.println("Cannot create native library temporary file.");
					throw e;
				}
				break;
			default:
				throw new IOException("Cannot load native library from the following URL: "
						+ url_to_chemmisol_library.toString());
		}
	}
	public static void loadLibrary(String path_to_chemmisol_library) {
		System.out.println(path_to_chemmisol_library);
		System.load(path_to_chemmisol_library);
	}

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
		loadLibrary(libchemmisol.toString());
	}

    public static void loadLibrary() {
        // Default location set up in the ummisco.gama.chemmisol.setup artifact
        // (see the corresponding pom.xml)
        loadLibrary("chemmisol.setup.properties", "cmake.build.directory");
    }
}
