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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import org.columba.mail.gui.table.TableController;
import org.columba.mail.gui.table.util.MessageNode;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

public class HeaderTableMouseListener extends MouseAdapter {
	private TableController headerTableViewer;

	public HeaderTableMouseListener(TableController headerTableViewer) {
		super();

		this.headerTableViewer = headerTableViewer;
	}

	protected void processPopup(MouseEvent event) {
		MessageNode[] nodes = headerTableViewer.getView().getSelectedNodes();
		
		headerTableViewer.getActionListener().changeMessageActions();
		
		if (nodes.length == 0) {
			// select node

			int row =
				headerTableViewer.getView().getTree().getRowForLocation(
					event.getX(),
					event.getY());
			headerTableViewer.getView().setRowSelectionInterval(row, row);

		} else if (nodes.length == 1) {
			// select node
			headerTableViewer.getView().clearSelection();

			int row =
				headerTableViewer.getView().getTree().getRowForLocation(
					event.getX(),
					event.getY());
			headerTableViewer.getView().setRowSelectionInterval(row, row);
		}

		headerTableViewer.getPopupMenu().show(
			event.getComponent(),
			event.getX(),
			event.getY());
	}

	public void mousePressed(MouseEvent event) {

		if (event.isPopupTrigger()) {
			processPopup(event);

		}

	}

	public void mouseReleased(MouseEvent event) {

		if (event.isPopupTrigger()) {
			processPopup(event);

		}
	}

	public void mouseClicked(MouseEvent event) {
		headerTableViewer.getActionListener().changeMessageActions();
		headerTableViewer.showMessage();

	}

	private void processDoubleClick() {

	}
}
