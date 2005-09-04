// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.gui.composer.util;

import java.awt.Font;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.columba.core.desktop.ColumbaDesktop;
import org.columba.core.gui.util.FontProperties;
import org.columba.core.util.TempFileStore;
import org.columba.mail.gui.composer.AbstractEditorController;
import org.columba.mail.util.MailResourceLoader;
import org.columba.ristretto.message.MimeHeader;

public class ExternalEditor {
	String Cmd;

	public ExternalEditor() {
	}

	// END public ExternalEditor()
	public ExternalEditor(String EditorCommand) {
	}

	private File writeToFile(final AbstractEditorController editController) {
		MimeHeader myHeader = new MimeHeader("text", "plain");
		File tmpFile = TempFileStore.createTempFileWithSuffix("txt");
		FileWriter FO;

		try {
			FO = new FileWriter(tmpFile);
		} catch (java.io.IOException ex) {
			JOptionPane.showMessageDialog(null,
					"Error: Cannot write to temp file needed "
							+ "for external editor.");
			return null;
		}

		try {

			String M = editController.getViewText();
			if (M != null)
				FO.write(M);

			FO.close();
		} catch (java.io.IOException ex) {
			JOptionPane.showMessageDialog(null,
					"Error: Cannot write to temp file needed "
							+ "for external editor:\n" + ex.getMessage());
			return null;
		}

		return tmpFile;

	}

	private String readFromFile(File tmpFile) {
		FileReader FI;
		try {
			FI = new FileReader(tmpFile);
		} catch (java.io.FileNotFoundException ex) {
			JOptionPane.showMessageDialog(null,
					"Error: Cannot read from temp file used "
							+ "by external editor.");
			return "";
		}

		char[] buf = new char[1000];
		int i;
		String message = "";

		try {
			while ((i = FI.read(buf)) >= 0)
				message += new String(buf, 0, i);

			FI.close();
		} catch (java.io.IOException ex) {
			JOptionPane.showMessageDialog(null,
					"Error: Cannot read from temp file used "
							+ "by external editor.");
			return "";
		}

		return message;
	} // END public ExternalEditor(String EditorCommand)

	public boolean startExternalEditor(
			final AbstractEditorController editController) throws IOException {
		/*
		 * *20030906, karlpeder* Method signature changed to take an
		 * AbstractEditorController (instead of an TextEditorView) as parameter
		 * since the view is no longer directly available
		 */

		// write text to file
		File tmpFile = writeToFile(editController);

		// remember old font properties
		final Font OldFont = editController.getViewFont();

		// create big size font to display in the composer textfield
		Font font = FontProperties.getTextFont();
		font = font.deriveFont(30);
		editController.setViewFont(font);
		editController.setViewText(MailResourceLoader.getString("menu",
				"composer", "extern_editor_using_msg"));

		// execute application, enabling blocking
		ColumbaDesktop.getInstance().openAndWait(tmpFile);

		// rafter the user saved the file and closed the
		// external text editor, we read the new text from the file
		final String message = readFromFile(tmpFile);

		// set old font properties
		editController.setViewFont(OldFont);
		// set new text
		editController.setViewText(message);

		return true;
	}

	// END public boolean startExternalEditor()
}

// END public class ExternalEditor
