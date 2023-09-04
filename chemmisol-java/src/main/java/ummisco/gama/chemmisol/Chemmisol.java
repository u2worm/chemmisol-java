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

/**
 * Main Chemmisol helper class, that defines static methods to load the native
 * chemmisol-cpp library from different places.
 *
 * <p>
 * In order to properly load the chemmisol library, it is necessary to identify
 * the location of the compiled {@code chemmisol} and {@code chemmisol-java}
 * libraries. The native {@code chemmisol} library corresponds to the base <a
 * href=https://github.com/u2worm/chemmisol-cpp>chemmisol-cpp</a> C++ library,
 * while {@code chemmisol-java} corresponds to JNI code contained in the {@code
 * chemmisol-java/src/main/cpp} directory of the <a href=
 * https://github.com/u2worm/chemmisol-java>chemmisol-java</a> repository. See
 * the README file of the <a href=
 * https://github.com/u2worm/chemmisol-java>chemmisol-java</a> project for
 * details about how to build and find those libraries.
 * </p>
 *
 * <p>
 * Library files are typically named {@code libchemmisol.so} and {@code
 * libchemmisol-java.so} on UNIX platforms and {@code libchemmisol.dll} and
 * {@code libchemmisol-java.dll} on Windows platforms.
 * </p>
 *
 * <p>
 * Both libraries can be loaded either from the file system or from <a
 * href=https://docs.oracle.com/javase/8/docs/technotes/guides/lang/resources.html>
 * Java resources</a>. How resources are bundled is project and build system
 * dependent (see for example <a
 * href=https://maven.apache.org/plugins/maven-jar-plugin>maven-jar-plugin</a>
 * or how eclipse manages the <a
 * href="https://help.eclipse.org/2023-06/index.jsp?topic=%2Forg.eclipse.jdt.doc.user%2Freference%2Fref-properties-build-path.htm">Java
 * Build Path</a>).
 * </p>
 *
 * <p>
 * However, how resources are accessed is "location independent". This means
 * that for methods that load libraries from resources, the paths to resources
 * can be specified relatively to the class from which they are loaded.
 * </p>
 *
 * <h2>Examples</h2>
 *
 * The simplest way to load the Chemmisol libraries is generally to call one of
 * the provided method from a {@code static { }} block.
 *
 * <h3>Loading from system files</h3>
 * Loads the {@code chemmisol} and {@code chemmisol-java} libraries from the
 * {@code /usr/local/lib} system directory:
 * <pre>
 * import ummisco.gama.chemmisol.Chemmisol;
 *
 * public class MyClass {
 * 	static {
 * 		try {
 * 			Chemmisol.loadChemmisolLibrariesFromFiles("/usr/local/lib");
 * 		} catch (UnsatisfiedLinkError e) {
 * 			System.err.println(e);
 * 		}
 * 	}
 * }
 * </pre>
 *
 * <h3>Loading from class resources</h3>
 *
 * Assuming the project is set up as a Maven project with the following <a
 * href=https://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html>
 * standard directory layout</a>:
 * <pre>
 * - src
 *   - main
 *     - java
 *       - my
 *         - package
 *           - MyClass.java
 *     - resources
 *       - my
 *         - package
 *            - lib
 *              - libchemmisol.so
 *              - libchemmisol-java.so
 * </pre>
 * The library can be loaded from project resources as follows:
 * <pre>
 * package my.package;
 *
 * public static class MyClass {
 * 	static {
 * 		try {
 * 			Chemmisol.loadChemmisolLibrariesFromResource("lib");
 * 		} catch (UnsatisfiedLinkError e) {
 * 			System.err.println(e);
 * 		}
 * 	}
 * }
 *  </pre>
 */
public class Chemmisol 
{
	private static final String FILE_PROTOCOL = "file";
	private static final String RESOURCE_BUNDLE_PROTOCOL = "bundleresource";
	private static final String LOG = "[CHEMMISOL] ";
	/**
	 * chemmisol library name.
	 */
	public static final String CHEMMISOL = "chemmisol";
	/**
	 * chemmisol-java library name.
	 */
	public static final String CHEMMISOL_JAVA = "chemmisol-java";

	private static void loadLibraryFromFile(String path_to_chemmisol_library) throws UnsatisfiedLinkError {
		System.out.println(LOG + "Loading native library from " + path_to_chemmisol_library);
		System.load(path_to_chemmisol_library);
	}

	/**
	 * Loads the library from the relative resource_path of the specified class.
	 *
	 * <p>
	 * The full library name is computed using System.mapLibraryName().
	 */
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

	private static void loadLibrary(Class<?> clazz, URL url_to_chemmisol_library)
			throws IOException {
		System.out.println(LOG + "Loading native library from "
				+ url_to_chemmisol_library.toString() + "...");
		switch(url_to_chemmisol_library.getProtocol()) {
			case FILE_PROTOCOL:
				// Loads the library from an external file
				loadLibraryFromFile(url_to_chemmisol_library.getPath());
				break;
			case RESOURCE_BUNDLE_PROTOCOL:
				try {
					System.out.println(LOG + "Loading native library into temporary file...");
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
					System.out.print(" Done.");

					// Loads the native library from the temporary file
					loadLibraryFromFile(temp_file.getAbsolutePath());
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

	/**
	 * Loads the {@code chemmisol} library from the {@code
	 * resource_path_to_chemmisol} resource folder of the {@code clazz1} class,
	 * and loads the {@code chemmisol-java} library from the {@code
	 * resource_path_to_chemmisol_java} resource folder of the {@code clazz2}
	 * class.
	 *
	 * <p>
	 * Libraries are loaded from {@code resource/path/<library_name>} where
	 * {@code <library_name>} is the system dependent file name obtained from
	 * {@link java.lang.System#mapLibraryName(String)
	 * System.mapLibraryName("chemmisol")} and {@link
	 * java.lang.System#mapLibraryName(String)
	 * System.mapLibraryName("chemmisol-java")}.
	 *
	 * @param clazz1 Class from which the {@code chemmisol} library is loaded.
	 * @param resource_path_to_chemmisol Path to the resource folder containing
	 * the {@code chemmisol} library. Should generally be specified as a
	 * relative path from {@code clazz1}.
	 * @param clazz2 Class from which the {@code chemmisol-java} library is loaded.
	 * @param resource_path_to_chemmisol_java Path to the resource folder
	 * containing the {@code chemmisol-java} library. Should generally be
	 * specified as a relative path from {@code clazz2}.
	 *
	 * @throws IOException if one of the native library cannot be loaded from
	 * the specified resource paths.
	 */
	public static void loadChemmisolLibrariesFromResource(
			Class<?> clazz1, String resource_path_to_chemmisol,
			Class<?> clazz2, String resource_path_to_chemmisol_java) throws IOException {
		loadLibraryFromResource(clazz1, resource_path_to_chemmisol, CHEMMISOL);
		loadLibraryFromResource(clazz2, resource_path_to_chemmisol_java, CHEMMISOL_JAVA);
	}

	/**
	 * Loads the {@code chemmisol} and {@code chemmisol-java} libraries from the {@code
	 * resource_path} resource folder of the {@code clazz} class.
	 *
	 * <p>
	 * Libraries are loaded from {@code resource/path/<library_name>} where
	 * {@code <library_name>} is the system dependent file name obtained from
	 * {@link java.lang.System#mapLibraryName(String)
	 * System.mapLibraryName("chemmisol")} and {@link
	 * java.lang.System#mapLibraryName(String)
	 * System.mapLibraryName("chemmisol-java")}.
	 *
	 * @param clazz Class from which the {@code chemmisol} and {@code
	 * chemmisol-java} libraries are loaded.
	 * @param resource_path Path to the resource folder containing the {@code
	 * chemmisol} and {@code chemmisol-java} libraries. Should generally be
	 * specified as a relative path from {@code clazz}.
	 *
	 * @throws IOException if one of the native library cannot be loaded from
	 * the specified resource path.
	 */
	public static void loadChemmisolLibrariesFromResource(Class<?> clazz, String resource_path)
			throws IOException {
			loadChemmisolLibrariesFromResource(
					clazz, resource_path, clazz, resource_path
					);
	}

	/**
	 * Loads the {@code chemmisol} and {@code chemmisol-java} libraries from the
	 * resources of the specified class.
	 *
	 * <p>
	 * Libraries are loaded from the file name {@code <library_name>}, obtained
	 * from {@link java.lang.System#mapLibraryName(String)
	 * System.mapLibraryName("chemmisol")} and {@link
	 * java.lang.System#mapLibraryName(String)
	 * System.mapLibraryName("chemmisol-java")}.
	 *
	 * @param clazz Class from which the {@code chemmisol} and {@code
	 * chemmisol-java} libraries are loaded.
	 *
	 * @throws IOException if one of the native library cannot be loaded from
	 * the resources of the class.
	 */
	public static void loadChemmisolLibrariesFromResource(Class<?> clazz)
			throws IOException{
			loadChemmisolLibrariesFromResource(clazz, "");
	}

	/**
	 * Loads the {@code chemmisol} and {@code chemmisol-java} libraries from the
	 * file system.
	 *
	 * Libraries are loaded from {@code /resource/path/<library_name>} where
	 * {@code <library_name>} is the system dependent file name obtained from
	 * {@link java.lang.System#mapLibraryName(String) System.mapLibraryName("chemmisol")} and
	 * {@link java.lang.System#mapLibraryName(String) System.mapLibraryName("chemmisol-java")}.
	 *
	 * @param path_to_chemmisol_library Path to the folder containing the {@code
	 * chemmisol} library.
	 * @param path_to_chemmisol_java_library Path to the folder containing the
	 * {@code chemmisol-java} library.
	 *
	 * @throws UnsatisfiedLinkError if an error occurs when loading the native
	 * library.
	 */
	public static void loadChemmisolLibrariesFromFiles(
			Path path_to_chemmisol_library,
			Path path_to_chemmisol_java_library
			) throws UnsatisfiedLinkError {
		loadLibraryFromFile(path_to_chemmisol_library
				.resolve(System.mapLibraryName(CHEMMISOL))
				.toAbsolutePath().toString());
		loadLibraryFromFile(path_to_chemmisol_java_library
				.resolve(System.mapLibraryName(CHEMMISOL_JAVA))
				.toAbsolutePath().toString());
			}

	/**
	 * Loads the {@code chemmisol} and {@code chemmisol-java} libraries from the
	 * file system.
	 *
	 * <p>
	 * Libraries are loaded from {@code /resource/path/<library_name>} where
	 * {@code <library_name>} is the system dependent file name obtained from
	 * {@link java.lang.System#mapLibraryName(String)
	 * System.mapLibraryName("chemmisol")} and {@link
	 * java.lang.System#mapLibraryName(String)
	 * System.mapLibraryName("chemmisol-java")}.
	 *
	 * @param path_to_chemmisol Path to the folder containing the {@code
	 * chemmisol} and {@code chemmisol-java} libraries.
	 *
	 * @throws UnsatisfiedLinkError if an error occurs when loading the native
	 * library.
	 */
	public static void loadChemmisolLibrariesFromFile(
			Path path_to_chemmisol
			) throws UnsatisfiedLinkError {
		loadChemmisolLibrariesFromFiles(path_to_chemmisol, path_to_chemmisol);
			}

	/**
	 * Loads the {@code chemmisol} and {@code chemmisol-java} libraries from the
	 * path represented by the property named {@code lib_directory_property} in
	 * the {@code project_properties} file loaded from the specified class.
	 *
	 * @param clazz Class from which the project_properties file is loaded as a
	 * resource.
	 * @param project_properties Name of the project properties resource file.
	 * @param lib_directory_property Name of the property representing the
	 * folder containing the {@code chemmisol} and {@code chemmisol-java}
	 * libraries.
	 *
	 * @throws IOException if an error occurs when loading the properties file.
	 * @throws UnsatisfiedLinkError if an error occurs when loading the native
	 * library.
	 */
	public static void loadChemmisolLibrariesFromProperties(
			Class<?> clazz,
			String project_properties, String lib_directory_property) throws IOException, UnsatisfiedLinkError {
		// Load library
		InputStream is = clazz.getClassLoader()
			.getResourceAsStream(project_properties);
		Properties properties = new Properties();
		properties.load(is);

		loadChemmisolLibrariesFromFile(FileSystems.getDefault().getPath(
				properties.getProperty(lib_directory_property)
				).toAbsolutePath());
	}
}
