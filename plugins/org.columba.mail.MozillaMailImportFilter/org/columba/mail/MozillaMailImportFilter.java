/*
 * Created on 24.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Vector;

import org.columba.core.command.WorkerStatusController;
import org.columba.core.gui.util.ExceptionDialog;
import org.columba.core.gui.util.NotifyDialog;
import org.columba.core.logging.ColumbaLogger;
import org.columba.mail.folder.Folder;
import org.columba.mail.folder.mailboximport.DefaultMailboxImporter;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class MozillaMailImportFilter extends DefaultMailboxImporter {

	/**
	 * @param destinationFolder
	 * @param sourceFiles
	 */
	public MozillaMailImportFilter(
		Folder destinationFolder,
		File[] sourceFiles) {
		super(destinationFolder, sourceFiles);

	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.mailboximport.DefaultMailboxImporter#importMailbox(java.io.File, org.columba.core.command.WorkerStatusController)
	 */
	public void importMailboxFile(
		File file,
		WorkerStatusController worker,
		Folder destFolder)
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

		// save last message, because while loop aborted before being able to save message
		if ((sucess == true) && (strbuf.length() > 0)) {
			saveMessage(strbuf.toString(), worker, destFolder);
		}

		in.close();
	}

	protected void generateDirectoryListing(
		File parentFolder,
		Vector v,
		Folder destinationFolder,
		WorkerStatusController worker) {
		// list all files
		File[] list = parentFolder.listFiles();

		for (int i = 0; i < list.length; i++) {
			File file = list[i];
			ColumbaLogger.log.info("mailbox=" + file.getPath());

			if (file == null)
				continue;

			// skip these config files
			if (file.getName().endsWith(".msf"))
				continue;
			if (file.getName().endsWith(".dat"))
				continue;

			if ( file.isDirectory() )
			{
			}
			
			if (file.getName().endsWith(".sbd")) {
				// directory found

				try {

					String filename =
						file.getName().substring(
							0,
							file.getName().indexOf(".sbd"));

					if (destinationFolder.getChild(filename) == null) {
						/*
						// folder doesn't exist -> create it
						destinationFolder =
							(Folder) destinationFolder.addFolder(filename);
						generateDirectoryListing(
							file,
							v,
							destinationFolder,
							worker);
							*/
					} else {
						generateDirectoryListing(
							file,
							v,
							destinationFolder,
							worker);
					}

				} catch (Exception ex) {
					ExceptionDialog d = new ExceptionDialog();
					d.showDialog(ex);
				}
			}

			ColumbaLogger.log.info(
				"found mailbox="
					+ file.getPath()
					+ " - importing to folder="
					+ destinationFolder.getName());

			// import mailbox file
			try {
				
				if (destinationFolder.getChild(file.getName()) == null) {

					// folder doesn't exist -> create it
					
					importMailboxFile(file, worker, (Folder) destinationFolder.addFolder(file.getName()));
				}
				else
					importMailboxFile(file, worker, destinationFolder);

			} catch (Exception ex) {
				if (ex instanceof FileNotFoundException) {
					NotifyDialog dialog = new NotifyDialog();
					dialog.showDialog("Source File not found:");
				} else {
					ExceptionDialog dialog = new ExceptionDialog();
					dialog.showDialog(ex);
				}
			}

		}
	}

	/* (non-Javadoc)
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
