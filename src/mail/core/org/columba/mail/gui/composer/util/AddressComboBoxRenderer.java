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
package org.columba.mail.gui.composer.util;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.columba.addressbook.gui.autocomplete.AddressCollector;
import org.columba.addressbook.gui.util.ToolTipFactory;
import org.columba.addressbook.model.ContactItem;
import org.columba.addressbook.model.GroupItem;
import org.columba.addressbook.model.HeaderItem;
import org.columba.core.gui.util.ImageLoader;

/**
 * 
 * 
 * @author fdietz
 */
public class AddressComboBoxRenderer extends JLabel implements ListCellRenderer {
	private ImageIcon contactIcon = ImageLoader
			.getSmallImageIcon("contact_small.png");

	private ImageIcon groupIcon = ImageLoader
			.getSmallImageIcon("group_small.png");

	public AddressComboBoxRenderer() {
		setOpaque(true);
	}

	/** {@inheritDoc} */
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}

		HeaderItem item = AddressCollector.getInstance().getHeaderItem((String) value);

		if (item == null) {
			setToolTipText("");
			setText((String) value);
			setIcon(null);
		} else {
			setText(item.getDisplayName());

			if (item.isContact()) {
				setIcon(contactIcon);
				setToolTipText(ToolTipFactory.createToolTip((ContactItem) item));
			} else {
				setIcon(groupIcon);
				setToolTipText(ToolTipFactory.createToolTip((GroupItem) item));
			}
		}

		return this;
	}

}