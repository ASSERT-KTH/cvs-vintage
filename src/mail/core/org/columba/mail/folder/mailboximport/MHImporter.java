/*
 * Created on 24.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.folder.mailboximport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.columba.core.command.WorkerStatusController;
import org.columba.mail.folder.Folder;
import org.columba.mail.folder.mh.MHMessageFileFilter;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class MHImporter extends DefaultMailboxImporter {

	/**
	 * @param destinationFolder
	 * @param sourceFile
	 */
	public MHImporter(Folder destinationFolder, File[] sourceFiles) {
		super(destinationFolder, sourceFiles);

	}

	public int getType() {
		return TYPE_DIRECTORY;
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.mailboximport.DefaultMailboxImporter#importMailbox(java.io.File, org.columba.core.command.WorkerStatusController)
	 */
	public void importMailboxFile(File directory, WorkerStatusController worker, Folder destFolder)
		throws Exception {

		File[] list = directory.listFiles(MHMessageFileFilter.getInstance());

		for (int i = 0; i < list.length; i++) {
			File file = list[i];

			String name = file.getName();

			if (name.equals(".") || name.equals(".."))
				continue;
			if (name.startsWith("."))
				continue;

			if ((file.exists()) && (file.length() > 0)) {
				importMessage(file, worker);
			}
		}
	}

	protected void importMessage(File file, WorkerStatusController worker)
		throws Exception {


		StringBuffer strbuf = new StringBuffer();

		BufferedReader in = new BufferedReader(new FileReader(file));
		String str;
		strbuf = new StringBuffer();

		while ((str = in.readLine()) != null) {
			strbuf.append(str + "\n");
		}

		in.close();

		saveMessage(strbuf.toString(), worker, getDestinationFolder());
	}
}
