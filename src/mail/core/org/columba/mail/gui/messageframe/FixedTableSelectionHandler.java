/*
 * Created on Jun 13, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.messageframe;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.gui.selection.SelectionHandler;

import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.MessageFolder;
import org.columba.mail.gui.table.selection.TableSelectionChangedEvent;


/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class FixedTableSelectionHandler extends SelectionHandler {
    FolderCommandReference[] tableReference;

    /**
 * @param id
 */
    public FixedTableSelectionHandler(FolderCommandReference[] tableReference) {
        super("mail.table");
        this.tableReference = tableReference;
    }

    /* (non-Javadoc)
 * @see org.columba.core.gui.selection.SelectionHandler#getSelection()
 */
    public DefaultCommandReference[] getSelection() {
        return tableReference;
    }

    /* (non-Javadoc)
 * @see org.columba.core.gui.selection.SelectionHandler#setSelection(org.columba.core.command.DefaultCommandReference[])
 */
    public void setSelection(DefaultCommandReference[] selection) {
        this.tableReference = (FolderCommandReference[]) selection;

        fireSelectionChanged(new TableSelectionChangedEvent(
                (MessageFolder) tableReference[0].getFolder(),
                tableReference[0].getUids()));
    }
}
