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
package org.columba.addressbook.gui.table.renderer;

import java.awt.Component;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

/**
 * @author fdietz
 */
public class DefaultHeaderRenderer
	extends JButton
	implements TableCellRenderer {
	private Border unselectedBorder = null;
	private Border selectedBorder = null;
	private boolean isBordered = true;
	private String name;

	public DefaultHeaderRenderer(String name) {
		super();

		this.name = name;

		setHorizontalAlignment(SwingConstants.LEFT);
		setHorizontalTextPosition(SwingConstants.LEFT);

		setOpaque(true); //MUST do this for background to show up.

		setBorder(UIManager.getBorder("TableHeader.cellBorder"));
	}

	public void updateUI() {
		super.updateUI();

		setBorder(UIManager.getBorder("TableHeader.cellBorder"));
	}

	public Component getTableCellRendererComponent(
		JTable table,
		Object str,
		boolean isSelected,
		boolean hasFocus,
		int row,
		int column) {

		setText(this.name);

		return this;
	}
}
