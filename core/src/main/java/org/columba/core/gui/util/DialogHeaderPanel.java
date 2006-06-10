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
package org.columba.core.gui.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;

import org.columba.core.gui.base.SingleSideEtchedBorder;
import org.columba.core.resourceloader.IconKeys;
import org.columba.core.resourceloader.ImageLoader;

/**
 * @author fdietz
 *  
 */

public class DialogHeaderPanel extends JPanel {

	public DialogHeaderPanel(String title, String description) {
		this(title, description, null);
		
	}
	public DialogHeaderPanel(String title, String description, ImageIcon icon) {

		setLayout( new BorderLayout());
		
		setBackground(Color.white);
		setPreferredSize(new Dimension(300, 60));
		setBorder(new CompoundBorder(new SingleSideEtchedBorder(
				SwingConstants.BOTTOM), BorderFactory.createEmptyBorder(10, 10,
				10, 10)));

		JPanel leftPanel = new JPanel();
		leftPanel.setBackground(Color.white);

		GridBagLayout layout = new GridBagLayout();
		leftPanel.setLayout(layout);

		GridBagConstraints c = new GridBagConstraints();

		JLabel titleLabel = new JLabel(title);

		//titleLabel.setAlignmentY(0);
		Font font = UIManager.getFont("Label.font");
		font = font.deriveFont(Font.BOLD);
		titleLabel.setFont(font);
		c.gridy = 0;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(titleLabel, c);
		leftPanel.add(titleLabel);

		c.gridy = 1;
		c.insets = new Insets(0, 20, 0, 0);

		JLabel descriptionLabel = new JLabel(description);
		layout.setConstraints(descriptionLabel, c);
		leftPanel.add(descriptionLabel);

		add(leftPanel, BorderLayout.WEST);

		if (icon == null)
			icon = ImageLoader.getIcon(IconKeys.PREFERENCES);

		JLabel iconLabel = new JLabel(icon);

		add(iconLabel, BorderLayout.EAST);

	}
}