/*
 * Created on 14.04.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.command;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.mail.folder.imap.IMAPRootFolder;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public abstract class IMAPFolderCommand extends FolderCommand {

	protected IMAPRootFolder rootFolder;
	/**
	 * @param references
	 */
	public IMAPFolderCommand(IMAPRootFolder rootFolder, DefaultCommandReference[] references) {
		super(references);
		this.rootFolder = rootFolder;
		
		
	}

	/**
	 * @param frame
	 * @param references
	 */
	public IMAPFolderCommand(
		AbstractFrameController frame,
		DefaultCommandReference[] references) {
		super(frame, references);
		
	}

	


	/**
	 * @return
	 */
	public IMAPRootFolder getIMAPRootFolder() {
		return rootFolder;
	}

}
