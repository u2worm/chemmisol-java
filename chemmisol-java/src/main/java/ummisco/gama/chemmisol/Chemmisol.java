package ummisco.gama.chemmisol;

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
	private static final String LOG = "[CHEMMISOL] ";
	private static final String CHEMMISOL = "chemmisol";
	private static final String CHEMMISOL_JAVA = "chemmisol-java";

	private static void loadLibraryFromResource(
			Class<?> clazz, String resource_path, String library
			) throws IOException {
		URL lib_url = clazz.getResource(
				resource_path + System.mapLibraryName(library)
				);
		if(lib_url == null) {
			throw new IOException(
					LOG + "Cannot load native " + library + " resource from " + resource_path +
					"(class: " + clazz.getName() + ")");
		}
		Chemmisol.loadLibrary(clazz, lib_url);
	}

	public static void loadLibraryFromResource(Class<?> clazz, String resource_path)
			throws IOException{
			try {
				loadLibraryFromResource(clazz, resource_path, CHEMMISOL);
				loadLibraryFromResource(clazz, resource_path, CHEMMISOL_JAVA);
			} catch(IOException e) {
				throw e;
			}
	}

	public static void loadLibraryFromResource(Class<?> clazz)
			throws IOException{
			loadLibraryFromResource(clazz, "");
	}

	private static void loadLibrary(Class<?> clazz, URL url_to_chemmisol_library)
			throws IOException {
		System.out.println(LOG + "Loading native library from "
				+ url_to_chemmisol_library.toString() + "...");
		switch(url_to_chemmisol_library.getProtocol()) {
			case FILE_PROTOCOL:
				loadLibrary(url_to_chemmisol_library.getPath());
				break;
			case RESOURCE_BUNDLE_PROTOCOL:
				try {
					System.out.print(LOG + "Loading native library into temporary file...");
					// Creates a temporary file that will contain the unpacked
					// native library
					File temp_file = File.createTempFile(url_to_chemmisol_library.getFile() + "_", ".tmp")
						.getAbsoluteFile();
					temp_file.deleteOnExit();

					// Gets the native library as a binary stream, directly from
					// the JAR
					InputStream library_bin = url_to_chemmisol_library.openStream();

					// Unpacks the library to the temporary file
					library_bin.transferTo(new FileOutputStream(temp_file));
					System.out.println(" Done.");

					// Loads the native library from the temporary file
					loadLibrary(temp_file.getAbsolutePath());
				} catch(IOException e) {
					System.err.println();
					System.err.println(LOG + "Cannot create native library temporary file.");
					throw e;
				}
				break;
			default:
				throw new IOException(LOG + "Cannot load native library from the following URL: "
						+ url_to_chemmisol_library.toString());
		}
	}
	public static void loadLibrary(String path_to_chemmisol_library) {
		System.out.print(LOG + "Linking native library from " + path_to_chemmisol_library);
		try {
			System.load(path_to_chemmisol_library);
			System.out.println(" Done.");
		} catch(UnsatisfiedLinkError e) {
			System.err.println();
			System.err.println(LOG + "Error linking library: " + path_to_chemmisol_library);
			System.err.println(e);
		}
	}

	public static void loadLibraryFromProperties(
			String project_properties, String lib_directory_property) {
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
				).resolve(System.mapLibraryName(CHEMMISOL))
			.toAbsolutePath();
		loadLibrary(libchemmisol.toString());

		Path libchemmisoljava = FileSystems.getDefault().getPath(
				properties.getProperty(lib_directory_property)
				).resolve(System.mapLibraryName(CHEMMISOL_JAVA))
			.toAbsolutePath();
		loadLibrary(libchemmisoljava.toString());

	}

    public static void loadLibraryFromDefaultProperties() {
        // Default location set up in the ummisco.gama.chemmisol.setup artifact
        // (see the corresponding pom.xml)
        loadLibraryFromProperties("chemmisol.setup.properties", "cmake.build.directory");
    }
}
