// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

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
