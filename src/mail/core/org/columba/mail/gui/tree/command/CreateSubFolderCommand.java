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
package org.columba.mail.gui.tree.command;

import java.text.MessageFormat;
import java.util.Hashtable;

import javax.swing.JOptionPane;

import org.columba.api.command.ICommandReference;
import org.columba.api.command.IWorkerStatusController;
import org.columba.core.command.Command;
import org.columba.core.command.Worker;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.folder.FolderCreationException;
import org.columba.mail.folder.FolderFactory;
import org.columba.mail.folder.IMailFolder;
import org.columba.mail.util.MailResourceLoader;

/**
 * Create subfolder command.
 * 
 * @author Timo Stich (tstich@users.sourceforge.net)
 * @author fdietz
 */
public class CreateSubFolderCommand extends Command {

	private IMailFolder parentFolder;

	private Hashtable attributes;

	/**
	 * Constructor for CreateSubFolderCommand.
	 * 
	 * @param references
	 */
	public CreateSubFolderCommand(ICommandReference reference) {
		super(reference);
	}

	/**
	 * @see org.columba.api.command.Command#execute(Worker)
	 */
	public void execute(IWorkerStatusController worker) throws Exception {
		parentFolder = (IMailFolder) ((MailFolderCommandReference) getReference()).getSourceFolder();

		String name = ((MailFolderCommandReference) getReference()).getFolderName();
		String type = ((MailFolderCommandReference) getReference()).getFolderType();

		try {
			if( type == null ) {
				IMailFolder subFolder = FolderFactory.getInstance()
				.createDefaultChild(parentFolder, name);				
			} else {
				IMailFolder subFolder = FolderFactory.getInstance()
				.createChild(parentFolder, name, type);
			}
		} catch (FolderCreationException ex) {
			// show error message 
			JOptionPane.showMessageDialog(null, 
					MessageFormat.format( MailResourceLoader.getString("dialog", "folder",
							"error_no_subfolder_allowed"), new String[] {parentFolder.getName()} ),MailResourceLoader.getString("dialog", "folder",
							"error_title"), JOptionPane.ERROR_MESSAGE);
		}
	}

}