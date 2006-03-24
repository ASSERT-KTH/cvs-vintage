// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.gui.config.folder;

import java.awt.Point;
import java.awt.event.MouseEvent;

import org.columba.mail.util.MailResourceLoader;
import org.frapuccino.checkablelist.CheckableList;


/**
 * Show tooltips on mouse movements over cells.
 * 
 * @author fdietz
 *
 */
 class CheckableTooltipList extends CheckableList {
	public CheckableTooltipList() {
		super();
	}

	public String getToolTipText(MouseEvent event) {
		int row = rowAtPoint(event.getPoint());
		String s = MailResourceLoader.getString("dialog", "folderoptions",
				FolderOptionsDialog.tooltips[row]+"_tooltip");

		return s;
	}

	public Point getToolTipLocation(MouseEvent event) {
		int row = rowAtPoint(event.getPoint());
		int col = columnAtPoint(event.getPoint());
		Object o = getValueAt(row, col);

		if (o == null) {
			return null;
		}

		if (o.toString().equals("")) {
			return null;
		}

		Point pt = getCellRect(row, col, true).getLocation();
		pt.translate(-1, -2);

		return pt;
	}
}