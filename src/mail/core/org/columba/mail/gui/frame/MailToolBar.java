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
package org.columba.mail.gui.frame;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.util.ResourceBundle;

import javax.swing.Box;
import javax.swing.JToolBar;

import org.columba.core.gui.util.ToolbarButton;

public class MailToolBar extends JToolBar {

	ResourceBundle toolbarLabels;
	GridBagConstraints gridbagConstraints;
	GridBagLayout gridbagLayout;
	int i;

	MailFrameController frame;

	public MailToolBar(MailFrameController f) {
		super();
		this.frame = f;

		addCButtons();
		putClientProperty("JToolBar.isRollover", Boolean.TRUE);
		
		
		//setMargin( new Insets(0,0,0,0) );
		
		setFloatable(false);
	}

	public void addButton(ToolbarButton button) {

		button.setRolloverEnabled(true);

		add(button);

	}

	public void addCButtons() {

		MouseAdapter handler = frame.getMouseTooltipHandler();
		ToolbarButton button;

		i = 0;

		button = new ToolbarButton(frame.globalActionCollection.newMessageAction);

		addButton(button);

		button = new ToolbarButton(frame.globalActionCollection.receiveSendAction);

		addButton(button);

		addSeparator();

		button =
			new ToolbarButton(frame.tableController.getActionListener().replyAction);
		addButton(button);

		button =
			new ToolbarButton(
				frame.tableController.getActionListener().forwardAction);
		addButton(button);

		addSeparator();

		button =
			new ToolbarButton(
				frame.tableController.getActionListener().copyMessageAction);
		addButton(button);

		button =
			new ToolbarButton(
				frame.tableController.getActionListener().moveMessageAction);
		addButton(button);

		button =
			new ToolbarButton(
				frame.tableController.getActionListener().deleteMessageAction);
		addButton(button);

		addSeparator();

		button = new ToolbarButton(frame.getStatusBar().getCancelAction());

		addButton(button);

		add(Box.createHorizontalGlue());

		/*
		Dimension d = new Dimension( 16,16 );
		System.out.println("dim="+d);
				
		frame.getStatusBar().getImageSequenceTimer().setScaling(d);
		*/
		add(frame.getStatusBar().getImageSequenceTimer());

	}

}
