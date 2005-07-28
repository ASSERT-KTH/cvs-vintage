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
package org.columba.chat.frame;

import java.awt.BorderLayout;
import java.io.InputStream;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.columba.chat.ui.conversation.ConversationController;
import org.columba.chat.ui.presence.PresenceComboBox;
import org.columba.chat.ui.roaster.RoasterTree;
import org.columba.core.config.ViewItem;
import org.columba.core.gui.frame.ContentPane;
import org.columba.core.gui.frame.DefaultFrameController;
import org.columba.core.xml.XmlElement;
import org.columba.core.xml.XmlIO;

/**
 * @author fdietz
 * 
 */
public class AlturaFrameController extends DefaultFrameController implements
		AlturaFrameMediator, ContentPane {

	private RoasterTree tree;

	private PresenceComboBox presence;

	private ConversationController conversation;

	/**
	 * @param c
	 * @param viewItem
	 */
	public AlturaFrameController(ViewItem viewItem) {
		super(viewItem);

		tree = new RoasterTree(this);
		presence = new PresenceComboBox(this);
		conversation = new ConversationController(this);

		// connect to server
		// new ConnectAction(this).actionPerformed(null);

	}

	/**
	 * @see org.columba.core.gui.frame.ContentPane#getComponent()
	 */
	public JComponent getComponent() {
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setDividerLocation(200);

		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BorderLayout());

		splitPane.add(leftPanel, JSplitPane.LEFT);

		leftPanel.add(tree, BorderLayout.CENTER);
		leftPanel.add(presence, BorderLayout.SOUTH);

		splitPane.add(conversation, JSplitPane.RIGHT);

		InputStream is = this.getClass().getResourceAsStream(
				"/org/columba/chat/action/menu.xml");
		getContainer().extendMenuFromURL(this, is);

		InputStream is2 = this.getClass().getResourceAsStream(
				"/org/columba/chat/action/toolbar.xml");
		XmlIO xmlFile = new XmlIO();
		xmlFile.load(is2);

		XmlElement toolbar = xmlFile.getRoot().getElement("/toolbar");

		getContainer().extendToolbar(this, toolbar);

		return splitPane;
	}

	/**
	 * @see org.columba.chat.frame.AlturaFrameMediator#getBuddyTree()
	 */
	public RoasterTree getBuddyTree() {
		return tree;
	}

	/**
	 * @see org.columba.chat.frame.AlturaFrameMediator#getPresenceController()
	 */
	public PresenceComboBox getPresenceController() {
		return presence;
	}

	/**
	 * @see org.columba.core.gui.frame.FrameMediator#getContentPane()
	 */
	public ContentPane getContentPane() {
		return this;
	}

	/**
	 * @see org.columba.chat.frame.AlturaFrameMediator#getConversationController()
	 */
	public ConversationController getConversationController() {
		return conversation;
	}
}