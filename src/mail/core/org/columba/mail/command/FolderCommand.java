package org.columba.mail.command;

import org.columba.core.command.Command;
import org.columba.core.command.DefaultCommandReference;
import org.columba.core.gui.FrameController;
import org.columba.mail.folder.Folder;
import org.columba.mail.folder.virtual.VirtualFolder;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public abstract class FolderCommand extends Command {

	/**
	 * Constructor for FolderCommand.
	 * @param frameController
	 * @param references
	 */
	public FolderCommand(
		FrameController frameController,
		DefaultCommandReference[] references) {
		super(frameController, references);
	}

	/**
	 * Returns the references.
	 * @return DefaultCommandReference[]
	 */
	public DefaultCommandReference[] getReferences() {
		FolderCommandReference[] r = (FolderCommandReference[]) super.getReferences();

		Folder folder = (Folder) r[0].getFolder();

		r = folder.getCommandReference(r);

		return r;
	}


}
