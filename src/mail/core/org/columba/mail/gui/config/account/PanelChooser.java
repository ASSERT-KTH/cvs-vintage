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
