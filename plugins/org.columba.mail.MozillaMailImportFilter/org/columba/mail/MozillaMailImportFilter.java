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

package org.columba.mail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Vector;
import java.util.logging.Logger;

import org.columba.core.command.WorkerStatusController;
import org.columba.core.gui.util.ExceptionDialog;
import org.columba.core.gui.util.NotifyDialog;
import org.columba.mail.folder.MessageFolder;
import org.columba.mail.folder.FolderFactory;
import org.columba.mail.folder.AbstractFolder;
import org.columba.mail.folder.mailboximport.AbstractMailboxImporter;
import org.columba.core.facade.DialogFacade;

/**
 * @author frd
 */
public class MozillaMailImportFilter extends AbstractMailboxImporter {

    /** JDK 1.4+ logging framework logger, used for logging. */
    private static final Logger LOG = Logger.getLogger("org.columba.mail");

	public MozillaMailImportFilter() {
		super();
	}

	/**
	 * @param destinationFolder
	 * @param sourceFiles
	 */
	public MozillaMailImportFilter(
		MessageFolder destinationFolder,
		File[] sourceFiles) {
		super(destinationFolder, sourceFiles);
	}

	public String getDescription() {
		return "Mozilla Mail Import filter for a complete account tree\n";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.mail.folder.mailboximport.DefaultMailboxImporter#importMailbox(java.io.File,
	 *      org.columba.core.command.WorkerStatusController)
	 */
	public void importMailboxFile(
		File file,
		WorkerStatusController worker,
		MessageFolder destFolder)
		throws Exception {

		int count = 0;
		boolean sucess = false;

		StringBuffer strbuf = new StringBuffer();

		BufferedReader in = new BufferedReader(new FileReader(file));
		String str;

		// parse line by line
		while ((str = in.readLine()) != null) {
			// if user cancelled task exit immediately
			if (worker.cancelled() == true)
				return;

			// if line doesn't start with "From" or line length is 0
			//  -> save everything in StringBuffer
			if ((str.startsWith("From ") == false) || (str.length() == 0)) {
				strbuf.append(str + "\n");
			} else {

				// line contains "-" (mozilla mbox style)
				//  -> import message in Columba
				if (str.indexOf("-") != -1) {
					if (strbuf.length() != 0) {
						// found new message

						saveMessage(strbuf.toString(), worker, destFolder);

						count++;

						sucess = true;

					}
					strbuf = new StringBuffer();
				} else {
					strbuf.append(str + "\n");
				}
			}

		}

		// save last message, because while loop aborted before being able to
		// save message
		if ((sucess == true) && (strbuf.length() > 0)) {
			saveMessage(strbuf.toString(), worker, destFolder);
		}

		in.close();
	}

	protected void generateDirectoryListing(
		File parentFolder,
		Vector v,
		MessageFolder destinationFolder,
		WorkerStatusController worker) {
		// list all files
		File[] list = parentFolder.listFiles();

		for (int i = 0; i < list.length; i++) {
			File file = list[i];
			LOG.fine("mailbox=" + file.getPath());

			// skip these config files
			if (file.getName().endsWith(".msf") || file.getName().endsWith(".dat"))
				continue;

			if (file.getName().endsWith(".sbd")) {
				// directory found

				try {

					String filename =
						file.getName().substring(
							0,
							file.getName().indexOf(".sbd"));

					if (destinationFolder.findChildWithName(filename, false)
						== null) {
						/*
						 * // folder doesn't exist -> create it
						 * destinationFolder = (Folder)
						 * destinationFolder.addFolder(filename);
						 * generateDirectoryListing( file, v,
						 * destinationFolder, worker);
						 */
					} else {
						generateDirectoryListing(
							file,
							v,
							destinationFolder,
							worker);
					}

				} catch (Exception ex) {
					//ExceptionDialog d = new ExceptionDialog();
					//d.showDialog(ex);
					DialogFacade.showExceptionDialog(ex);
				}
			}

			LOG.fine(
				"found mailbox="
					+ file.getPath()
					+ " - importing to folder="
					+ destinationFolder.getName());

			// import mailbox file
			try {

				if (destinationFolder.findChildWithName(file.getName(), false)
					== null) {

					// folder doesn't exist -> create it
					AbstractFolder child = FolderFactory.getInstance().createDefaultChild(
						destinationFolder,
						file.getName());

					importMailboxFile(
						file,
						worker,
						(MessageFolder) child);
				} else
					importMailboxFile(file, worker, destinationFolder);

			} catch (Exception ex) {
				if (ex instanceof FileNotFoundException) {
					NotifyDialog dialog = new NotifyDialog();
					dialog.showDialog("Source File not found:");
				} else {
					//ExceptionDialog dialog = new ExceptionDialog();
					//dialog.showDialog(ex);
					DialogFacade.showExceptionDialog(ex);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.mail.folder.mailboximport.DefaultMailboxImporter#importMailbox(org.columba.core.command.WorkerStatusController)
	 */
	public void importMailbox(WorkerStatusController worker) {
		File[] listing = getSourceFiles();

		//		we just want to import one profile
		//  -> so, only use the first element which
		//  -> should point to the profile-directory
		File[] files = getSourceFiles();
		File accountDirectory = files[0];

		Vector result = new Vector();
		generateDirectoryListing(
			accountDirectory,
			result,
			getDestinationFolder(),
			worker);

	}
}
