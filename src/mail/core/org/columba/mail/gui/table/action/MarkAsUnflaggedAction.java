//The contents of this file are subject to the Mozilla Public License Version 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.

package org.columba.mail.gui.table.action;

import java.awt.event.ActionEvent;

import org.columba.core.action.FrameAction;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.gui.selection.SelectionChangedEvent;
import org.columba.core.gui.selection.SelectionListener;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.command.MarkMessageCommand;
import org.columba.mail.gui.frame.AbstractMailFrameController;
import org.columba.mail.gui.table.selection.TableSelectionChangedEvent;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class MarkAsUnflaggedAction
	extends FrameAction
	implements SelectionListener {

	public MarkAsUnflaggedAction(AbstractFrameController frameController) {
		
		// TODO: i18n missing here
		
		super(frameController, "Not Flagged");
		setActionCommand("MARK_AS_UNFLAGGED");
		/*
		super(
			frameController,
			"Not Flagged",
			"Not Flagged",
			"MARK_AS_UNFLAGGED",
			null,
			null,
			'0',
			null);
		*/
		setEnabled(false);
		((AbstractMailFrameController) frameController).registerTableSelectionListener(
			this);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		FolderCommandReference[] r =
			((AbstractMailFrameController) getFrameController()).getTableSelection();
		r[0].setMarkVariant(MarkMessageCommand.MARK_AS_UNFLAGGED);

		MarkMessageCommand c = new MarkMessageCommand(r);

		MainInterface.processor.addOp(c);
	}
	/* (non-Javadoc)
			 * @see org.columba.core.gui.util.SelectionListener#selectionChanged(org.columba.core.gui.util.SelectionChangedEvent)
			 */
	public void selectionChanged(SelectionChangedEvent e) {
		setEnabled(((TableSelectionChangedEvent) e).getUids().length > 0);
	}
}
