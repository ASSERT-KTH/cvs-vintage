/*
 * Created on 07.09.2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.columba.mail.folder.command;

import java.io.File;
import java.io.FileOutputStream;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.Worker;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandAdapter;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.Folder;

/**
 * Export all selected folders to a single MBOX mailbox file.
 * 
 * MBOX mailbox format:
 *  http://www.qmail.org/qmail-manual-html/man5/mbox.html
 *
 * @author fdietz
 */
public class ExportFolderCommand extends FolderCommand {

	protected FolderCommandAdapter adapter;

	protected Object[] destUids;

	/**
	 * @param references
	 */
	public ExportFolderCommand(DefaultCommandReference[] references) {
		super(references);

	}

	/**
	 * @param frame
	 * @param references
	 */
	public ExportFolderCommand(
		AbstractFrameController frame,
		DefaultCommandReference[] references) {
		super(frame, references);

	}

	/* (non-Javadoc)
	 * @see org.columba.core.command.Command#execute(org.columba.core.command.Worker)
	 */
	public void execute(Worker worker) throws Exception {
		FolderCommandReference[] references =
			(FolderCommandReference[]) getReferences();
		adapter = new FolderCommandAdapter(references);
	
		FolderCommandReference[] r = adapter.getSourceFolderReferences();

		File destFile = r[0].getDestFile();
		
		FileOutputStream os = new FileOutputStream(destFile);

		int counter = 0;
		
		// for every source folder
		for (int i = 0; i < r.length; i++) {
			
			Folder srcFolder = (Folder) r[i].getFolder();
			worker.setDisplayText("Exporting "+srcFolder.getName()+" "+(i+1)+"/"+r.length+"...");
		 	
			Object[] uids = srcFolder.getUids();
			
			worker.setProgressBarMaximum(uids.length);
			worker.setProgressBarValue(0);
			
			// for every message in folder i
			for (int j = 0; j < uids.length; j++) {
				// get message source from folder
				String source = srcFolder.getMessageSource(uids[j]);
				
				// prepend From line
				// 
				os.write(new String("From \r\n").getBytes());
				
				// write message source to file
				os.write(source.getBytes());
				
				// append newline
				os.write(new String("\r\n").getBytes());
				
				os.flush();
				
				worker.setProgressBarValue(j);
				counter++;
				
				if ( worker.cancelled() ) break;
			}
			
			if ( worker.cancelled() ) break;
		}

		os.close();
		
		worker.setDisplayText("Exported "+counter+" messages successfully");
	}

}
