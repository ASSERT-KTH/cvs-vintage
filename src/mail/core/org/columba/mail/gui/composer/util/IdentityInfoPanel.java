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
		String address = identity.get("address");
		String name = identity.get("name");

		if (item.isPopAccount())
			label.setIcon(image1);
		else
			label.setIcon(image2);

		label.setText(accountName + ":    " + name + " <" + address + ">");
		
		

	}

}
