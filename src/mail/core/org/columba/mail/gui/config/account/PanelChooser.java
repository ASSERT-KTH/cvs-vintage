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
package org.columba.mail.gui.config.account;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionListener;

import org.columba.core.gui.util.ImageLoader;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author frd
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class PanelChooser extends JPanel {

	JList list;

	public PanelChooser() {
		super();

		setLayout(new BorderLayout());

		setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

		Vector v = new Vector();
		v.add("identity");
		v.add("incomingserver");
		v.add("outgoingserver");
		v.add("specialfolders");
		v.add("security");

		list = new JList(v);
		list.setCellRenderer(new ItemRenderer());

		list.setSelectedIndex(0);

		JScrollPane scrollPane = new JScrollPane(list);

		add(scrollPane, BorderLayout.CENTER);

	}

	public void addListSelectionListener(ListSelectionListener l) {
		list.addListSelectionListener(l);
	}

	class ItemRenderer extends JLabel implements ListCellRenderer {

		ImageIcon identity =
			ImageLoader.getImageIcon("mail-config-druid-identity.png");
		ImageIcon incoming =
			ImageLoader.getImageIcon("mail-config-druid-receive.png");
		ImageIcon outgoing =
			ImageLoader.getImageIcon("mail-config-druid-send.png");
		ImageIcon specialfolders = ImageLoader.getImageIcon("i-directory.png");
		ImageIcon security = ImageLoader.getImageIcon("security.png");

		public ItemRenderer() {
			setOpaque(true);
			setHorizontalAlignment(CENTER);
			setVerticalAlignment(CENTER);
			
			setVerticalTextPosition( SwingConstants.BOTTOM );
			setHorizontalTextPosition(SwingConstants.CENTER);
			//setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
			setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		}

		public Component getListCellRendererComponent(
			JList list,
			Object value,
			int index,
			boolean isSelected,
			boolean cellHasFocus) {
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}

			String str = (String) value;
			ImageIcon icon = null;

			if (str.equals("identity"))
				icon = identity;
			else if (str.equals("incomingserver"))
				icon = incoming;
			else if (str.equals("outgoingserver"))
				icon = outgoing;
			else if (str.equals("specialfolders"))
				icon = specialfolders;
			else if (str.equals("security"))
				icon = security;

			setIcon(icon);
			
			String label = MailResourceLoader.getString("dialog","account", str );
			setText(label);
			
			
			return this;
		}
	}

}
