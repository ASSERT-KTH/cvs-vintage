package org.columba.mail.gui.tree;

import java.util.EventListener;

public interface IFolderSelectionListener extends EventListener{

	public void selectionChanged(IFolderSelectionEvent e);
}
