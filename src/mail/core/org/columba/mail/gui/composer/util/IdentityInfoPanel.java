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

package org.columba.mail.gui.composer.util;

import java.awt.Color;
import java.awt.GridBagConstraints;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.columba.core.gui.util.CInfoPanel;
import org.columba.core.gui.util.ImageLoader;
import org.columba.mail.config.AccountItem;
import org.columba.mail.config.IdentityItem;

public class IdentityInfoPanel extends CInfoPanel {
	private JLabel label;
	private int exists;
	private int unseen;
	private int recent;

	private ImageIcon image1;
	private ImageIcon image2;
	private ImageIcon image3;
	private ImageIcon image4;
	private ImageIcon image5;
	private ImageIcon image6;

	public IdentityInfoPanel() {
		super();

	}

	public void initComponents() {
		super.initComponents();

		image1 = ImageLoader.getSmallImageIcon("localhost.png");
		image2 = ImageLoader.getSmallImageIcon("remotehost.png");

		gridbagConstraints.gridwidth = GridBagConstraints.RELATIVE;
		gridbagConstraints.gridx = 0;
		gridbagConstraints.weightx = 0.5;
		Box box = Box.createHorizontalBox();
		gridbagLayout.setConstraints(box, gridbagConstraints);
		panel.add(box);

		label = new JLabel("Identity");
		label.setForeground(Color.white);
		label.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		label.setFont(font);
		label.setIconTextGap(10);
		label.setIcon(image1);
		label.setText("Identity");

		gridbagConstraints.gridwidth = GridBagConstraints.REMAINDER;
		gridbagConstraints.gridx = 1;
		//gridbagConstraints.insets = new Insets(0, 0, 0, 20);
		gridbagConstraints.anchor = GridBagConstraints.EAST;
		gridbagLayout.setConstraints(label, gridbagConstraints);

		panel.add(label);

	}

	public void resetRenderer() {
		initComponents();
	}

	public void set(AccountItem item) {
		String accountName = item.getName();

		IdentityItem identity = item.getIdentityItem();
		String address = identity.getAddress();
		String name = identity.getName();

		if (item.isPopAccount())
			label.setIcon(image1);
		else
			label.setIcon(image2);

		label.setText(accountName + ":    " + name + " <" + address + ">");
		
		

	}

}
