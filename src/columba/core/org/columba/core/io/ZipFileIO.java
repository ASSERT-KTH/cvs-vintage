//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the 
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.
package org.columba.core.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.columba.core.gui.util.NotifyDialog;

/**
 * @author fdietz
 *
 * zip archive operations are handled by this class
 * 
 */
public class ZipFileIO {

	/**
	 * default constructor
	 */
	public ZipFileIO() {
		super();

	}

	/**
	 * 
	 * extract zip file to destination folder
	 * 
	 * @param file			zip file to extract
	 * @param destination	destinatin folder 
	 */
	public static void extract(File file, File destination) {
		try {
			// Open the ZIP file

			ZipInputStream in = new ZipInputStream(new FileInputStream(file));

			// Get the first entry
			ZipEntry entry = null;

			while ((entry = in.getNextEntry()) != null) {

				String outFilename = entry.getName();

				// Open the output file
				if (entry.isDirectory()) {
					new File(destination, outFilename).mkdirs();
				} else {

					OutputStream out =
						new FileOutputStream(
							new File(destination, outFilename));

					// Transfer bytes from the ZIP file to the output file
					byte[] buf = new byte[1024];
					int len;
					while ((len = in.read(buf)) > 0) {
						out.write(buf, 0, len);
					}

					// Close the stream
					out.close();
				}
			}

			// Close the stream
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			NotifyDialog d = new NotifyDialog();
			d.showDialog(e);
		}
	}

	/**
	 * 
	 * Return the first directory of this archive
	 * 
	 * This is needed to determine the plugin directory.
	 * 
	 * 
	 * @param zipFile
	 * @return	<class>File</class> containing the first entry of this archive
	 */
	public static File getFirstFile(File zipFile) {
		try {
			// Open the ZIP file

			ZipInputStream in = new ZipInputStream(new FileInputStream(zipFile));

			// Get the first entry
			ZipEntry entry = null;

			while ((entry = in.getNextEntry()) != null) {

				String outFilename = entry.getName();

				if (entry.isDirectory()) {
					return new File(outFilename);
				}
			}
			// Close the stream
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			NotifyDialog d = new NotifyDialog();
			d.showDialog(e);
		}
		
		return null;
	}

}
