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

	private static final String MAIL_PERSPECTIVE = "ThreePaneMail";
	private static final String ADDRESSBOOK_PERSPECTIVE = "Addressbook";
	private static final String CHAT_PERSPECTIVE = "AlturaFrame";
	
	private JRadioButtonMenuItem mailMenu;

	private JRadioButtonMenuItem addressbookMenu;

	private JRadioButtonMenuItem chatMenu;
	
	/**
	 * @param controller
	 * @param caption
	 */
	public SwitchPerspectiveSubmenu(FrameMediator controller) {
		super(controller, "Show View");

		String id = getFrameMediator().getViewItem().get("id");

		// check if this is a management frame instance
		// -> if so create submenu to switch perspectives
		// -> otherwise, don't create submenu
		boolean isManagedFrame = false;
		if ( id.equals(MAIL_PERSPECTIVE)) isManagedFrame = true;
		if ( id.equals(ADDRESSBOOK_PERSPECTIVE)) isManagedFrame = true;
		if ( id.equals(CHAT_PERSPECTIVE)) isManagedFrame = true;
		
		if ( !isManagedFrame ) return;
		
		ButtonGroup group = new ButtonGroup();
		JRadioButtonMenuItem mailMenu = new JRadioButtonMenuItem("Mail");
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

		
		chatMenu = new JRadioButtonMenuItem("Chat");
		//chatMenu.setIcon(ImageLoader.getSmallImageIcon("stock_book-16.png"));
		group.add(chatMenu);
		chatMenu.setActionCommand("CHAT");
		chatMenu.addActionListener(this);
		add(chatMenu);

		
		if (id.equals(MAIL_PERSPECTIVE))
			mailMenu.setSelected(true);
		else if ( id.equals(ADDRESSBOOK_PERSPECTIVE))
			addressbookMenu.setSelected(true);
		else
			chatMenu.setSelected(true);
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
				MainInterface.frameModel.switchView(container, MAIL_PERSPECTIVE);
			} catch (PluginLoadingFailedException e) {
				e.printStackTrace();
			}
		} else if (action.equals("ADDRESSBOOK")) {
			try {
				MainInterface.frameModel.switchView(container, ADDRESSBOOK_PERSPECTIVE);
			} catch (PluginLoadingFailedException e) {
				e.printStackTrace();
			}
		} else if (action.equals("CHAT")) {
			try {
				MainInterface.frameModel.switchView(container, CHAT_PERSPECTIVE);
			} catch (PluginLoadingFailedException e) {
				e.printStackTrace();
			}
		}

	}

}