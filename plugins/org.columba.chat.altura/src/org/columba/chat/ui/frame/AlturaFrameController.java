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
package org.columba.chat.ui.frame;

import java.awt.BorderLayout;
import java.io.InputStream;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.columba.api.gui.frame.IContainer;
import org.columba.chat.ui.conversation.ConversationController;
import org.columba.chat.ui.conversation.api.IConversationController;
import org.columba.chat.ui.frame.api.IChatFrameMediator;
import org.columba.chat.ui.presence.PresenceComboBox;
import org.columba.chat.ui.presence.api.IPresenceController;
import org.columba.chat.ui.roaster.RoasterTree;
import org.columba.chat.ui.roaster.api.IRoasterController;
import org.columba.core.config.ViewItem;
import org.columba.core.gui.docking.DockableView;
import org.columba.core.gui.frame.DockFrameController;
import org.flexdock.docking.DockingConstants;

/**
 * @author fdietz
 * 
 */
public class AlturaFrameController extends DockFrameController implements
		IChatFrameMediator {

	private RoasterTree tree;

	private PresenceComboBox presence;

	private ConversationController conversation;

	private DockableView treePanel;

	private DockableView conversationPanel;

	/**
	 * @param c
	 * @param viewItem
	 */
	public AlturaFrameController(ViewItem viewItem) {
		super(viewItem);

		tree = new RoasterTree(this);
		presence = new PresenceComboBox(this);
		conversation = new ConversationController();

		registerDockables();

		// connect to server
		// new ConnectAction(this).actionPerformed(null);

	}

	public String[] getDockableIds() {
		return new String[] { "roaster_tree", "conversation_view" };
	}

	public void registerDockables() {
		// init dockable panels
		treePanel = new DockableView("roaster_tree", "Roaster");

		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BorderLayout());

		JScrollPane treeScrollPane = new JScrollPane(tree);
		treeScrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		leftPanel.add(treeScrollPane, BorderLayout.CENTER);
		leftPanel.add(presence, BorderLayout.NORTH);

		treePanel.setContentPane(leftPanel);

		conversationPanel = new DockableView("conversation_view",
				"Conversation");

		conversationPanel.setContentPane(conversation);

	}

	/**
	 * @see org.columba.core.gui.frame.DockFrameController#loadDefaultPosition()
	 */
	public void loadDefaultPosition() {

		super.dock(conversationPanel, DockingConstants.CENTER_REGION);
		conversationPanel.dock(treePanel, DockingConstants.WEST_REGION, 0.3f);

		super.setSplitProportion(conversationPanel, 0.35f);
	}

	/**
	 * @see org.columba.chat.ui.frame.api.IChatFrameMediator#getRoasterTree()
	 */
	public IRoasterController getRoasterTree() {
		return tree;
	}

	/**
	 * @see org.columba.chat.ui.frame.api.IChatFrameMediator#getPresenceController()
	 */
	public IPresenceController getPresenceController() {
		return presence;
	}

	/**
	 * @see org.columba.chat.ui.frame.api.IChatFrameMediator#getConversationController()
	 */
	public IConversationController getConversationController() {
		return conversation;
	}

	/** *********************** container callbacks ************* */

	public void extendMenu(IContainer container) {

		InputStream is = this.getClass().getResourceAsStream(
				"/org/columba/chat/action/menu.xml");
		container.extendMenu(this, is);

	}

	public void extendToolBar(IContainer container) {

		InputStream is2 = this.getClass().getResourceAsStream(
				"/org/columba/chat/action/toolbar.xml");
		container.extendToolbar(this, is2);

	}

}