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
package org.columba.mail.gui.config.mailboximport;

import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.columba.core.gui.util.MultiLineLabel;
import org.columba.core.gui.util.wizard.DefaultWizardPanel;
import org.columba.core.gui.util.wizard.WizardTextField;

/**
 * @version 	1.0
 * @author
 */
public class SourcePanel extends DefaultWizardPanel {
	JButton sourceButton;
	JButton destinationButton;

	public SourcePanel(
		JDialog dialog,
		ActionListener listener,
		String title,
		String description,
		ImageIcon icon) {
		super(dialog, listener, title, description, icon);
	}
	public SourcePanel(
		JDialog dialog,
		ActionListener listener,
		String title,
		String description,
		ImageIcon icon,
		boolean b) {
		super(dialog, listener, title, description, icon);

		JPanel panel = this;
		panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 20, 30));
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		MultiLineLabel label =
			new MultiLineLabel("Please choose your source and destination folder.");

		panel.add(label);

		panel.add(Box.createRigidArea(new java.awt.Dimension(0, 40)));

		WizardTextField middlePanel = new WizardTextField();
		JLabel sourceLabel = new JLabel("Source Folder:");
		middlePanel.addLabel(sourceLabel);
		sourceButton = new JButton("source folder");
		sourceButton.setActionCommand("SOURCE");
		sourceButton.addActionListener(listener);
		middlePanel.addTextField(sourceButton);
		middlePanel.addExample(new JLabel(""));

		JLabel destinationLabel = new JLabel("Destination Folder:");
		middlePanel.addLabel(destinationLabel);
		destinationButton = new JButton("destination folder");
		destinationButton.setActionCommand("DESTINATION");
		destinationButton.addActionListener(listener);
		middlePanel.addTextField(destinationButton);
		middlePanel.addExample(
			new JLabel("(Destination Folder inside Columba)"));

		panel.add(middlePanel);
	}

	public void setSource(String str) {
		sourceButton.setText(str);
	}

	public void setDestination(String str) {
		destinationButton.setText(str);
	}

}
