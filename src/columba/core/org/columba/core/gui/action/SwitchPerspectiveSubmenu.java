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
package org.columba.core.gui.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButtonMenuItem;

import org.columba.core.action.IMenu;
import org.columba.core.gui.frame.Container;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.main.MainInterface;
import org.columba.core.plugin.PluginLoadingFailedException;

/**
 * @author fdietz
 *  
 */
public class SwitchPerspectiveSubmenu extends IMenu implements ActionListener {

	private JRadioButtonMenuItem mailMenu;

	private JRadioButtonMenuItem addressbookMenu;

	/**
	 * @param controller
	 * @param caption
	 */
	public SwitchPerspectiveSubmenu(FrameMediator controller) {
		super(controller, "Perspective");

		String id = getFrameMediator().getViewItem().get("id");

		ButtonGroup group = new ButtonGroup();
		JRadioButtonMenuItem mailMenu = new JRadioButtonMenuItem("Mail");
		//mailMenu.setIcon(ImageLoader.getSmallImageIcon("mail-new.png"));
		group.add(mailMenu);
		mailMenu.setActionCommand("MAIL");
		mailMenu.addActionListener(this);
		add(mailMenu);

		addressbookMenu = new JRadioButtonMenuItem("Addressbook");
		//addressbookMenu.setIcon(ImageLoader.getSmallImageIcon("stock_book-16.png"));
		group.add(addressbookMenu);
		addressbookMenu.setActionCommand("ADDRESSBOOK");
		addressbookMenu.addActionListener(this);
		add(addressbookMenu);

		if (id.equals("ThreePaneMail"))
			mailMenu.setSelected(true);
		else
			addressbookMenu.setSelected(true);
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		String action = arg0.getActionCommand();

		FrameMediator mediator = getFrameMediator();

		Container container = mediator.getContainer();

		if (action.equals("MAIL")) {
			try {
				MainInterface.frameModel.openView(container, "ThreePaneMail");
			} catch (PluginLoadingFailedException e) {
				e.printStackTrace();
			}
		} else if (action.equals("ADDRESSBOOK")) {
			try {
				MainInterface.frameModel.openView(container, "Addressbook");
			} catch (PluginLoadingFailedException e) {
				e.printStackTrace();
			}
		}

	}

}