package org.columba.mail.gui.tree;

import org.columba.mail.folder.IMailFolder;

public interface IFolderSelectionEvent {

	public Object getSource();
	public IMailFolder getFolder();
	
}
