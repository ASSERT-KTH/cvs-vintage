/*
 * Created on Jun 13, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.messageframe;

import org.columba.core.command.ICommandReference;
import org.columba.core.gui.selection.SelectionHandler;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.command.IFolderCommandReference;
import org.columba.mail.folder.AbstractMessageFolder;
import org.columba.mail.gui.table.selection.TableSelectionChangedEvent;

/**
 * @author frd
 * 
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class FixedTableSelectionHandler extends SelectionHandler {
	IFolderCommandReference tableReference;

	/**
	 * @param id
	 */
	public FixedTableSelectionHandler(IFolderCommandReference tableReference) {
		super("mail.table");
		this.tableReference = tableReference;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.core.gui.selection.SelectionHandler#getSelection()
	 */
	public ICommandReference getSelection() {
		return tableReference;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.core.gui.selection.SelectionHandler#setSelection(org.columba.core.command.DefaultCommandReference[])
	 */
	public void setSelection(ICommandReference selection) {
		this.tableReference = (FolderCommandReference) selection;

		fireSelectionChanged(new TableSelectionChangedEvent(
				(AbstractMessageFolder) tableReference.getFolder(), tableReference
						.getUids()));
	}
}