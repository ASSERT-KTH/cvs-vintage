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

package org.columba.mail.gui.composer.util;

import java.awt.Font;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import javax.swing.JOptionPane;

import org.columba.core.config.Config;
import org.columba.core.util.TempFileStore;
import org.columba.mail.gui.composer.EditorView;
import org.columba.mail.gui.mimetype.MimeTypeViewer;
import org.columba.mail.message.MimeHeader;
import org.columba.mail.util.MailResourceLoader;

public class ExternalEditor {

	String Cmd;

	public ExternalEditor() {
	} // END public ExternalEditor()

	public ExternalEditor(String EditorCommand) {
	} // END public ExternalEditor(String EditorCommand)

	public boolean startExternalEditor(EditorView EditView) {
		MimeHeader myHeader = new MimeHeader("text", "plain");
		MimeTypeViewer viewer = new MimeTypeViewer();
		TempFileStore tmpFileStore = new TempFileStore();
		File TmpFile = TempFileStore.createTempFileWithSuffix("extern_edit");
		FileWriter FO;
		FileReader FI;

		try {
			FO = new FileWriter(TmpFile);
		} catch (java.io.IOException ex) {
			JOptionPane.showMessageDialog(
				null,
				"Error: Cannot write to temp file needed "
					+ "for external editor.");
			return false;
		}
		try {
			String M = EditView.getText();
			if (M != null) {
				FO.write(M);
			}
			FO.close();
		} catch (java.io.IOException ex) {
			JOptionPane.showMessageDialog(
				null,
				"Error: Cannot write to temp file needed "
					+ "for external editor.");
			return false;
		}

		Font OldFont = EditView.getFont();

		System.out.println("Setting Font to REALLY BIG!!! :-)");

		/*
		// Why doesn't this work???
		EditView.setFont(
			new Font(Config.getOptionsConfig().getThemeItem().getTextFontName(), Font.BOLD, 30));
		*/
		Font font = Config.getOptionsConfig().getGuiItem().getTextFont();
		font = font.deriveFont(30);
		
		EditView.setFont(font);
		
		EditView.setText(
			MailResourceLoader.getString(
				"menu",
				"composer",
				"extern_editor_using_msg"));

		Process Child = viewer.open(myHeader, TmpFile);

		try {
			// Wait for external editor to quit
			Child.waitFor();
		} catch (InterruptedException ex) {
			JOptionPane.showMessageDialog(
				null,
				"Error: External editor exited " + "abnormally.");
			return false;
		}

		EditView.setFont(OldFont);

		try {
			FI = new FileReader(TmpFile);
		} catch (java.io.FileNotFoundException ex) {
			JOptionPane.showMessageDialog(
				null,
				"Error: Cannot read from temp file used "
					+ "by external editor.");
			return false;
		}
		//      int i = FI.available();
		char[] buf = new char[1000];
		int i;
		String Message = new String("");
		try {
			while ((i = FI.read(buf)) >= 0) {
				//System.out.println( "*>"+String.copyValueOf(buf)+"<*");
				Message += new String(buf, 0, i);
				//System.out.println( "-->"+Message+"<--");
			}
			FI.close();
		} catch (java.io.IOException ex) {
			JOptionPane.showMessageDialog(
				null,
				"Error: Cannot read from temp file used "
					+ "by external editor.");
			return false;
		}

		//System.out.println( "++>"+Message+"<++");
		//System.out.println( Message.length());

		EditView.setText(Message);

		return true;
	} // END public boolean startExternalEditor()
} // END public class ExternalEditor
