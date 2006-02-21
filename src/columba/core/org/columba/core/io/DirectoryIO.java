/*
 * Created on 08.08.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.core.io;

import java.io.File;

/**
 * @author fdietz
 * 
 * <class>DirectoryIO</class> contains useful methods concerning directory
 * operations
 * 
 */
public class DirectoryIO {
	/**
	 * 
	 * recursivly delete directory
	 * 
	 * @param directory
	 */
	public static boolean delete(File directory) {
		boolean result = false;

		if (directory.isDirectory()) {
			File[] files = directory.listFiles();

			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					delete(files[i]);
				}

				files[i].delete();
			}

			result = directory.delete();
		}

		return result;
	}
}
