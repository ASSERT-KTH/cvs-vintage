/*
 * Created on 14.04.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.command;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.gui.frame.FrameMediator;
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
	public IMAPFolderCommand(IMAPRootFolder rootFolder,
			DefaultCommandReference reference) {
		super(reference);
		this.rootFolder = rootFolder;
	}

	/**
	 * @param frame
	 * @param references
	 */
	public IMAPFolderCommand(FrameMediator frame,
			DefaultCommandReference reference) {
		super(frame, reference);
	}

	/**
	 * @return
	 */
	public IMAPRootFolder getIMAPRootFolder() {
		return rootFolder;
	}
}