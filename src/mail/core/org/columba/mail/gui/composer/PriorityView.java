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

package org.columba.mail.gui.composer;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

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
public class PriorityView extends JComboBox {

	ComposerModel model;

	private static final String[] priorities = { MailResourceLoader.getString("dialog", "composer", "highest"), MailResourceLoader.getString("dialog","composer", "high"), MailResourceLoader.getString("dialog","composer", "normal"), MailResourceLoader.getString("dialog","composer", "low"), MailResourceLoader.getString("dialog","composer", "lowest")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

	public PriorityView(ComposerModel model) {
		super(priorities);
		this.model = model;

		setRenderer(new ComboBoxRenderer());

		setSelectedIndex(2);

	}

	public void installListener(PriorityController controller) {
		addItemListener(controller);
	}

	class ComboBoxRenderer extends JLabel implements ListCellRenderer {

		private ImageIcon image1 =
			ImageLoader.getSmallImageIcon("priority-high.png");
		private ImageIcon image2 = null;
		private ImageIcon image3 = null;
		private ImageIcon image4 =
			ImageLoader.getSmallImageIcon("priority-low.png");

		public ComboBoxRenderer() {
			setOpaque(true);

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

			String p = (String) value;
			if (p == null)
				return this;

			if (p.equals("Highest"))
				setIcon(image1);
			/*
			else if ( p.equals("High") )
			  setIcon( image2 );
			*/

			/*
			else if ( p.equals("Low") )
			    setIcon( image3 );      
			 */
			else if (p.equals("Lowest"))
				setIcon(image4);
			else
				setIcon(null);

			if (getIcon() == null) {
				setBorder(
					BorderFactory.createEmptyBorder(
						0,
						image1.getIconWidth() + getIconTextGap(),
						0,
						0));
			} else {
				setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			}

			setText((String) value);
			return this;
		}
	}

}
