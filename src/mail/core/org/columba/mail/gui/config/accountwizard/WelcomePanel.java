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
package org.columba.mail.gui.config.accountwizard;

import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;

import org.columba.core.gui.util.MultiLineLabel;
import org.columba.core.gui.util.wizard.DefaultWizardPanel;
import org.columba.mail.util.MailResourceLoader;

public class WelcomePanel extends DefaultWizardPanel {
	private JLabel welcomeLabel;
	private JLabel label2;
	private JLabel label3;

	public WelcomePanel(
		JDialog dialog,
		ActionListener listener,
		String title,
		String description,
		ImageIcon icon) {
		super(dialog, listener, title, description, icon);

	
		setBorder(BorderFactory.createEmptyBorder(30, 30, 20, 30));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		MultiLineLabel label = new MultiLineLabel(MailResourceLoader.getString("dialog", "accountwizard", "welcome_to_columba")); //$NON-NLS-1$

		add(label);

		add(Box.createRigidArea(new java.awt.Dimension(0, 140)));
	}

	/*
	protected JPanel createPanel(ActionListener listener) {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 20, 30));
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		MultiLineLabel label = new MultiLineLabel(MailResourceLoader.getString("dialog", "accountwizard", "welcome_to_columba")); //$NON-NLS-1$

		panel.add(label);

		panel.add(Box.createRigidArea(new java.awt.Dimension(0, 140)));

		return panel;
	}
	*/
}
