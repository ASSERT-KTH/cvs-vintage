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

import org.columba.core.action.InternAction;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.gui.selection.SelectionChangedEvent;
import org.columba.core.gui.selection.SelectionListener;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.main.MainInterface;
import org.columba.mail.gui.message.command.ViewMessageCommand;
import org.columba.mail.gui.table.selection.TableSelectionChangedEvent;

public class ViewMessageAction
	extends InternAction
	implements SelectionListener {

	protected Object[] oldUids;
	

	/**
	 * @param controller
	 */
	public ViewMessageAction(AbstractFrameController controller) {
		super(controller);
		controller.getSelectionManager().registerSelectionListener(
			"mail.table",
			this);
		
		oldUids = new Object[1];
		oldUids[0] = "-1";
	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.util.SelectionListener#selectionChanged(org.columba.core.gui.util.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent e) {
		Object[] uids = ((TableSelectionChangedEvent) e).getUids();
		if (uids.length == 1) {

			if (oldUids[0] == uids[0]) {
				ColumbaLogger.log.debug(
					"this message was already selected, don't fire any event");
				return;
			}

			oldUids = uids;

			MainInterface.processor.addOp(
				new ViewMessageCommand(
					getFrameController(),
					getFrameController().getSelectionManager().getSelection(
						"mail.table")));

		}
	}

}
