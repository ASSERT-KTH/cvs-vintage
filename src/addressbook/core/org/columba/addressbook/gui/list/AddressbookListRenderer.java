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
package org.columba.addressbook.gui.list;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.columba.addressbook.gui.util.ToolTipFactory;
import org.columba.addressbook.model.ContactModelPartial;
import org.columba.addressbook.model.GroupPartial;
import org.columba.addressbook.model.HeaderItemPartial;
import org.columba.addressbook.resourceloader.IconKeys;
import org.columba.core.resourceloader.ImageLoader;

@SuppressWarnings({"serial","serial"})
public class AddressbookListRenderer extends JLabel implements ListCellRenderer {
	ImageIcon image1 = ImageLoader.getSmallIcon(IconKeys.EDIT_CONTACT);

	ImageIcon image2 = ImageLoader.getSmallIcon(org.columba.core.resourceloader.IconKeys.USER);

	public AddressbookListRenderer() {
		setOpaque(true);
		setHorizontalAlignment(LEFT);
		setVerticalAlignment(CENTER);
	}

	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}

		HeaderItemPartial item = (HeaderItemPartial) value;

		setText(item.getName());

		if (item.isContact()) {
			setIcon(image1);
			setToolTipText(ToolTipFactory.createToolTip((ContactModelPartial)item));

		} else {
			setIcon(image2);
			setToolTipText(ToolTipFactory.createToolTip((GroupPartial)item));

		}

		return this;
	}

}