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
package org.columba.mail.gui.mimetype;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.JOptionPane;

import org.columba.core.main.MainInterface;
import org.columba.core.mimetype.MimeRouter;
import org.columba.ristretto.message.MimeHeader;

/**
 * Extracts mimetypes from options.xml and executes
 * the process.
 * 
 * @author fdietz
 *
 */
public class ColumbaViewer extends AbstractViewer {

	public Process openWith(MimeHeader header, File tempFile, boolean blocking) {
		boolean save = false;
		
		String viewer = promptForViewer(header);

		String cmd = new String(viewer + " " + tempFile.toString());

		Process p = execProcess(tempFile, viewer);
		if ( p == null ) {
			viewer = promptForViewer(header);
			if ( viewer == null ) return null;
			
			p = execProcess(tempFile, viewer);
		}
		
		return p;
	}

	public Process open(MimeHeader header, File tempFile, boolean blocking) {
		File viewerFile = null;
		String viewer = MimeRouter.getInstance().getViewer(header);
		if (viewer != null) {
			viewerFile = new File(viewer);
		}

		// if viewer is not yet specified
		if (viewer == null) {
			boolean save = false;
			viewer = promptForViewer(header);
		}

		Process p = execProcess(tempFile, viewer);
		if ( p == null ) {
			viewer = promptForViewer(header);
			if ( viewer == null ) return null;
			p = execProcess(tempFile, viewer);
		}
		
		return p;
	}

	/**
	 * @param tempFile
	 * @param viewer
	 * @return
	 */
	private Process execProcess(File tempFile, String viewer) {
		Process child = null;

		try {
			child = Runtime.getRuntime().exec(
					viewer + " " + tempFile.toString());
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
			
			return null;
		}

		return child;
	}

	private String promptForViewer(MimeHeader header) {
		String viewer = null;
		boolean save = false;

		ChooseViewerDialog viewerDialog = new ChooseViewerDialog(header
				.getMimeType().getType(), header.getMimeType().getSubtype(),
				save);
		viewer = viewerDialog.getViewer();

		if (viewer == null) {
			return null;
		}

		save = viewerDialog.saveViewer();

		if (save) {
			MimeRouter.getInstance().setViewer(header, viewer);
		}

		return viewer;
	}

	private String promptForHTMLViewer() {
		String viewer = null;
		boolean save = false;

		ChooseViewerDialog viewerDialog = new ChooseViewerDialog("text",
				"html", save);

		viewer = viewerDialog.getViewer();

		if (viewer == null) {
			return null;
		}

		save = viewerDialog.saveViewer();

		if (save) {
			MimeRouter.getInstance().setViewer("text", "html", viewer);
		}

		return viewer;
	}

	public Process openURL(URL url) {
		boolean save = false;
		File viewerFile = null;
		String viewer = MimeRouter.getInstance().getViewer("text", "html");
		if (viewer != null) {
			viewerFile = new File(viewer);
		}

		// if viewer is not yet specified
		if (viewer == null) {
			viewer = promptForHTMLViewer();

		}

		Process p = execProcess(url, viewer);
		if ( p == null ) {
			viewer = promptForHTMLViewer();
			if ( viewer == null ) return null;
			p = execProcess(url, viewer);
		}
		
		return p;
		
	}

	/**
	 * @param url
	 * @param viewer
	 * @return
	 */
	private Process execProcess(URL url, String viewer) {
		Process child = null;

		try {
			child = Runtime.getRuntime().exec(viewer + " " + url);
		} catch (Exception ex) {
			if ( MainInterface.DEBUG)
				ex.printStackTrace();
			
			JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
			
			return null;
		}

		return child;
	}

	public Process openWithURL(URL url) {
		boolean save = false;
		String viewer = promptForHTMLViewer();

		Process p = execProcess(url, viewer);
		if ( p == null ) {
			viewer = promptForHTMLViewer();
			if ( viewer == null ) return null;
			p = execProcess(url, viewer);
		}
		
		return p;
	}
}