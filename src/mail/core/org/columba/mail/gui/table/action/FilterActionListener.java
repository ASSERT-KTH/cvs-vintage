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