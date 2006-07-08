package org.columba.mail.gui.tree;

import java.util.EventObject;

import org.columba.mail.folder.IMailFolder;

public class FolderSelectionEvent extends EventObject implements
		IFolderSelectionEvent {

	private IMailFolder folder;
	
	public FolderSelectionEvent(Object source, IMailFolder folder) {
		super(source);
		
		this.folder = folder;
	}

	public IMailFolder getFolder() {
		return folder;
	}

}
