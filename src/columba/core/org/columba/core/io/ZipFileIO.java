/*
 * Created on 07.08.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
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
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ZipFileIO {

	/**
	 * 
	 */
	public ZipFileIO() {
		super();

	}

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

			//			Close the stream
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			NotifyDialog d = new NotifyDialog();
			d.showDialog(e);
		}
	}

}
