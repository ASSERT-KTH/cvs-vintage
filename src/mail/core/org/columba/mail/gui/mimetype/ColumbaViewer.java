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

package org.columba.mail.gui.mimetype;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.JOptionPane;

import org.columba.mail.message.MimeHeader;
import org.columba.mail.parser.MimeRouter;

public class ColumbaViewer extends DefaultViewer {

	public Process openWith(MimeHeader header, File tempFile) {

		boolean save = false;
		ChooseViewerDialog viewerDialog =
			new ChooseViewerDialog(
				header.contentType,
				header.contentSubtype,
				save);
		String viewer = viewerDialog.getViewer();
		if (viewer == null)
			return null;
		save = viewerDialog.saveViewer();

		if (save)
			MimeRouter.getInstance().setViewer(header, viewer);

		//System.out.println("tempfile: "+tempFile);

		String cmd = new String(viewer + " " + tempFile.toString());

		//System.out.println("cmd: "+cmd);

		Process child = null;
		try {
			child = Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
		}
		return child;
	}

	public Process open(MimeHeader header, File tempFile) {
		String viewer = MimeRouter.getInstance().getViewer(header);
		if (viewer == null) {
			boolean save = false;
			ChooseViewerDialog viewerDialog =
				new ChooseViewerDialog(
					header.contentType,
					header.contentSubtype,
					save);
			viewer = viewerDialog.getViewer();
			if (viewer == null)
				return null;
			save = viewerDialog.saveViewer();

			if (save)
				MimeRouter.getInstance().setViewer(header, viewer);
		}
		Process child = null;
		try {
			child =
				Runtime.getRuntime().exec(viewer + " " + tempFile.toString());
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
		}
		return child;
	}

	public Process openURL(URL url) {
		boolean save = false;
		String viewer = MimeRouter.getInstance().getViewer("text", "html");
		if (viewer == null) {
			ChooseViewerDialog viewerDialog =
				new ChooseViewerDialog("text", "html", save);

			viewer = viewerDialog.getViewer();
			if (viewer == null)
				return null;
			save = viewerDialog.saveViewer();
			if (save)
				MimeRouter.getInstance().setViewer("text", "html", viewer);
		}
		Process child = null;
		try {
			child = Runtime.getRuntime().exec(viewer + " " + url);
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
		}
		return child;
	}

	public Process openWithURL(URL url) {
		boolean save = false;
		String viewer;
		ChooseViewerDialog viewerDialog =
			new ChooseViewerDialog("text", "html", save);

		viewer = viewerDialog.getViewer();
		if (viewer == null)
			return null;
		save = viewerDialog.saveViewer();
		if (save)
			MimeRouter.getInstance().setViewer("text", "html", viewer);
		Process child = null;
		try {
			child = Runtime.getRuntime().exec(viewer + " " + url);
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
		}
		return child;
	}
}
