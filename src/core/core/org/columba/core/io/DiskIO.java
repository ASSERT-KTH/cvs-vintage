// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

package org.columba.core.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;

import org.columba.core.config.ConfigPath;
import org.columba.core.logging.ColumbaLogger;

/**
 * The startup default for installation directory is the system propertry "user.dir".
 *
 */
public class DiskIO {

	private static String installationDirectory =
		System.getProperties().getProperty("user.dir");

	private static String resourceFolder = "";

	/** Ensures the existence of the directory specified by the parameter. If the
	 *  directory does not exist, an attempt is performed to create it including all
	 *  necessary parent directories that may be implied by the specification.
	 *  ### HELPME : against what should a relative pathname be made absolute? ###
	 *  ### We need to set the installation directory somewhere! ####
	 *  @param path String specifying the intended directory name; if the specified
	 *  name is a relative path, it is always made absolute against the program's
	 *  <b>installationDirectory</b>.
	 *  @return <b>true</b> if and only if the specified file exists and
	 *  is a directory
	 */
	public static boolean ensureDirectory(File dir) {
		boolean success = true;

		if (dir == null)
			throw new IllegalArgumentException("dir = null");

		if (!dir.isAbsolute())
			dir = new File(ConfigPath.getConfigDirectory(), dir.getPath());

		if (!dir.isDirectory()) {
			success = !dir.isFile() && dir.mkdirs();
			if (success) {
				System.out.println("created directory: " + dir.toString());
				// the same goes to the logging-factory
				ColumbaLogger.log.debug("created directory: " + dir.toString());
			} else {
				System.err.println(
					"failed while trying to create directory: "
						+ dir.toString());
				// the same goes to the logging-factory
				ColumbaLogger.log.error(
					"failed while trying to create directory: "
						+ dir.toString());
			}
		}

		return success;
	} // ensureDirectory

	public static boolean ensureDirectory(String path) {
		return ensureDirectory(new File(path));
	}

	public static void saveStringInFile(File toFile, String insertString)
		throws IOException {
		int lineNumber = 0;
		BufferedWriter out;

		out = new BufferedWriter(new FileWriter(toFile));
		out.write(insertString);
		out.flush();
		out.close();

	} // saveStringInFile

	/** Deletes the directory specified by the parameter and all of its contents.
	 *  This does recurse into subdirectories. Function reports errors. If the
	 *  parameter does not denote a directory, <b>false</b> is always returned.
	 *  @param dir a File representing the directory to be delete
	 *  @return <b>true</b> if and only if the directory does not exist on termination;
	 *  a return value <b>false</b> does not imply that there were no files deleted
	 *  @throws IllegalArgumentException if the parameter is <b>null</b>
	 */
	public static boolean deleteDirectory(File dir) {
		File[] files;
		File f;
		int i;
		boolean success = true;

		if (dir == null)
			throw new IllegalArgumentException("dir = null");

		if (!dir.isDirectory())
			return false;

		files = dir.listFiles();
		for (i = 0; i < files.length; i++) {
			if ((f = files[i]).isDirectory()) {
				deleteDirectory(f);
			} else if (!f.delete()) {
				System.err.println(
					"*** failed to delete file " + f.getAbsolutePath());
				// the same goes to the logging-factory
				ColumbaLogger.log.error(
					"*** failed to delete file " + f.getAbsolutePath());
			}
		}

		success = dir.delete();

		if (!success) {
			System.err.println(
				"*** failed to delete directory " + dir.getAbsolutePath());
			// the same goes to the logging-factory
			ColumbaLogger.log.error(
				"*** failed to delete directory " + dir.getAbsolutePath());
		}

		return success;
	} // deleteDirectory

	/** Deletes the contents of an existing directory. (May be applied to
	 *  non-existing files without causing error.)
	 * @return <b>true</b> if and only if on termination the directory exists
	 * and is empty
	 */
	public static boolean emptyDirectory(File dir) {
		boolean success;

		if (dir == null)
			throw new IllegalArgumentException("dir = null");

		if ((success = dir.exists() && deleteDirectory(dir)))
			dir.mkdirs();

		return success && dir.exists();
	} // emptyDirectory

	/** Delete a single disk file. Function reports errors. */
	public static boolean deleteFile(String path) {
		File f = new File(path);
		boolean success;

		if (!(success = f.delete())) {
			System.err.println("*** failed to delete file " + path);
			// the same goes to the logging-factory
			ColumbaLogger.log.error("*** failed to delete file " + path);
		}

		return success;
	} // deleteFile

	/** General use columba resource InputStream getter.
	 * @param path the full path and filename of the resource requested. If
	 * <code>path</code> begins with "#" it is resolved against the program's
	 * standard resource folder after removing "#"
	 * @return an InputStream to read the resource data, or <b>null</b> if the resource
	 * could not be obtained
	 * @throws java.io.IOException if there was an error opening the input stream
	 */
	public static InputStream getResourceStream(String path)
		throws java.io.IOException {
		URL url;

		if ((url = getResourceURL(path)) == null)
			return null;
		else
			return url.openStream();
	} // getResourceStream

	/** General use columba resource URL getter.
	 * @param path the full path and filename of the resource requested. If
	 * <code>path</code> begins with "#" it is resolved against the program's
	 * standard resource folder after removing "#"
	 * @return an URL instance, or <b>null</b> if the resource could not be obtained
	 * @throws java.io.IOException if there was an error opening the input stream
	 */
	public static URL getResourceURL(String path) //throws java.io.IOException
	{
		URL url;

		if (path == null)
			throw new IllegalArgumentException("path = null");

		if (path.startsWith("#"))
			path = resourceFolder + path.substring(1);

		//url = ClassLoader.getSystemResource(path);
		url = org.columba.core.io.DiskIO.class.getResource("/" + path);
		if (url == null) {
			System.err.println("*** failed locating resource: " + path);
			if (ColumbaLogger.log != null) {
				// the same goes to the logging-factory
				ColumbaLogger.log.error(
					"*** failed locating resource: " + path);
			}
			return null;
		}

		if (ColumbaLogger.log != null) {
			//System.out.println( "resource: " +url.getProtocol() + ", url:"+url.getPath() );
			//ColumbaLogger.log.debug("resource: " + url.getProtocol() + ", url:" + url.getPath());
		}

		return url;
	} // getResourceURL

	public static void setResourceRoot(String path) {
		if (path == null)
			resourceFolder = "";

		else {
			if (!path.endsWith("/"))
				path += "/";

			resourceFolder = path;
		}
	} // setResourceRoot

	public static String getResourceRoot() {
		return resourceFolder;
	}

	/** Copies the contents of any disk file to the specified output file.
	 *  The output file will be overwritten if it exist.
	 *  Function reports errors.
	 *  @param inputFile a File object
	 *  @param outputFile a File object
	 *  @throws java.io.IOException if the function could not be completed
	 *  because of an IO error
	 */
	public static void copyFile(File inputFile, File outputFile)
		throws java.io.IOException {

		FileInputStream in;
		FileOutputStream out;

		byte[] buffer = new byte[512];
		int len;

		try {
			out = new FileOutputStream(outputFile);
			in = new FileInputStream(inputFile);

			while ((len = in.read(buffer)) != -1)
				out.write(buffer, 0, len);

			in.close();
			out.close();
		} catch (IOException e) {
			System.err.println(
				"*** error during file copy: " + outputFile.getAbsolutePath());
			System.err.println(e);
			// the same goes to the logging-factory
			ColumbaLogger.log.debug(
				"*** error during file copy: " + outputFile.getAbsolutePath(),
				e);
			throw e;
		}

	} // copyFile

	/** Copies a system resource to the specified output file. The output file will
	 *  be overwritten if it exist, so the calling routine has to take care about
	 *  unwanted deletions of content.  Function reports errors.
	 *  @param resource a full resource path. If
	 *  the value begins with "#", it is resolved against the program's
	 *  standard resource folder after removing "#"
	 *  @return <b>true</b> if and only if the operation was successful, <b>false</b>
	 *  if the resource was not found
	 *  @throws java.io.IOException if there was an IO error
	 */
	public static boolean copyResource(String resource, File outputFile)
		throws java.io.IOException {
		InputStream in;
		FileOutputStream out;
		byte[] buffer = new byte[512];
		int len;

		// attempt
		try {

			if ((in = DiskIO.getResourceStream(resource)) == null)
				return false;

			out = new FileOutputStream(outputFile);

			while ((len = in.read(buffer)) != -1)
				out.write(buffer, 0, len);

			out.close();
			in.close();

			System.out.println("created : " + outputFile.getAbsolutePath());
			if (ColumbaLogger.log != null) {

				// the same goes to the logging-factory
				ColumbaLogger.log.info(
					"created : " + outputFile.getAbsolutePath());
			}
		} catch (IOException e) {
			System.err.println(
				"*** error during res. file copy: "
					+ outputFile.getAbsolutePath());
			System.err.println(e);
			if (ColumbaLogger.log != null) {
				// the same goes to the logging-factory
				ColumbaLogger.log.error(
					"*** error during res. file copy: "
						+ outputFile.getAbsolutePath(),
					e);
			}
			throw e;
		}

		return true;
	} // copyResource

	public static String readStringFromResource(String resource)
		throws java.io.IOException {
		InputStream in;

		byte[] buffer = new byte[512];
		int len;
		StringBuffer result = new StringBuffer();
		// attempt
		try {

			if ((in = DiskIO.getResourceStream(resource)) == null)
				return "";

			BufferedReader reader =
				new BufferedReader(new InputStreamReader(in));

			String nextLine = reader.readLine();

			while (nextLine != null) {
				result.append(nextLine);
				result.append("\n");
				nextLine = reader.readLine();
			}

			in.close();

		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}

		return result.toString();
	}

	/** Results equal <code>copyResource ( String resource, new File (outputFile) )</code>.
	 */
	public static boolean copyResource(String resource, String outputFile)
		throws java.io.IOException {
		return copyResource(resource, new File(outputFile));
	}

	public static String readFileInString(File input) throws IOException {
		StringBuffer result = new StringBuffer();

		BufferedReader reader = new BufferedReader(new FileReader(input));

		String nextLine = reader.readLine();

		while (nextLine != null) {
			result.append(nextLine);
			result.append("\n");
			nextLine = reader.readLine();
		}

		return result.toString();
	}
}
