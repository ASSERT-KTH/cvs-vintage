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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.columba.mail.gui.table.FilterToolbar;
import org.columba.mail.gui.table.TableController;
import org.columba.mail.gui.table.util.TableModelFilteredView;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

public class FilterActionListener implements ActionListener {
	private TableController tableController;

	public FilterActionListener(TableController headerTableViewer) {
		this.tableController = headerTableViewer;
	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();

		try {

			if (action.equals("ALL")) {
				TableModelFilteredView model =
					tableController.getView().getTableModelFilteredView();

				model.setNewFlag(false);
				//model.setOldFlag( true );
				model.setAnsweredFlag(false);
				model.setFlaggedFlag(false);
				model.setExpungedFlag(false);
				model.setAttachmentFlag(false);
				model.setPatternString("");

				model.setDataFiltering(false);
				tableController.getHeaderTableModel().update();

				FilterToolbar toolbar = tableController.getFilterToolbar();

				// FIXME
				//toolbar.newButton.setSelected(false);
				
				

			} else if (action.equals("UNREAD")) {
				TableModelFilteredView model =
					tableController.getView().getTableModelFilteredView();

				model.setNewFlag(true);
				//model.setOldFlag( false );
				model.setAnsweredFlag(false);
				model.setFlaggedFlag(false);
				model.setExpungedFlag(false);
				model.setAttachmentFlag(false);
				model.setPatternString("");

				model.setDataFiltering(true);
				tableController.getHeaderTableModel().update();

				FilterToolbar toolbar = tableController.getFilterToolbar();

				// FIXME
				//toolbar.newButton.setSelected(true);

				//toolbar.oldButton.setSelected(false);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
}