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

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.columba.core.gui.util.ImageLoader;
import org.columba.mail.config.AccountItem;
import org.columba.mail.config.IdentityItem;

/**
 * @author frd
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class AccountView extends JComboBox {
	
	ComposerModel model;
	
	public AccountView(ComposerModel model )
	{
		super();
		
		this.model = model;
		
		setRenderer(new AccountListRenderer());
		
		
			
	}
		
}

class AccountListRenderer extends JLabel implements ListCellRenderer
{
	ImageIcon image1;
	ImageIcon image2;

	public AccountListRenderer()
	{
		setOpaque(true);
		image1 = ImageLoader.getSmallImageIcon("localhost.png");
		image2 = ImageLoader.getSmallImageIcon("remotehost.png");
	}

	public Component getListCellRendererComponent(
		JList list,
		Object value,
		int index,
		boolean isSelected,
		boolean cellHasFocus)
	{
		if (isSelected)
		{
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		}
		else
		{
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}

		
		if (value != null)
		{
			AccountItem item = (AccountItem) value;
			String accountName = item.getName();
			IdentityItem identity = item.getIdentityItem();
			String address = identity.getAddress();
			String name = identity.getName();

			String result = accountName + ":   " + name + " <" + address + ">";

			setText(result);

			if (item.isPopAccount())
				setIcon(image1);
			else
				setIcon(image2);

		}
		

		return this;
	}

}

