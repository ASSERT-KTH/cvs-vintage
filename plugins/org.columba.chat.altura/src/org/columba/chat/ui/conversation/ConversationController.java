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
package org.columba.chat.ui.conversation;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JTabbedPane;

import org.columba.chat.AlturaComponent;
import org.columba.chat.api.IAlturaFrameMediator;
import org.columba.chat.api.IChatMediator;
import org.columba.chat.api.IConversationController;
import org.jivesoftware.smack.Chat;

/**
 * Handles all chat panels.
 * 
 * @author fdietz
 */
public class ConversationController extends JTabbedPane implements
		IConversationController, ActionListener {

	private Map chatMap;

	private IAlturaFrameMediator mediator;

	/**
	 * 
	 */
	public ConversationController(IAlturaFrameMediator mediator) {
		super();

		this.mediator = mediator;

		chatMap = new HashMap();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.chat.ui.conversation.IConversationController#addChat(java.lang.String)
	 */
	public IChatMediator addChat(String jabberId) {

		ChatMediator m = null;
		if (chatMap.containsKey(jabberId)) {
			m = (ChatMediator) chatMap.get(jabberId);
		} else {
			// create chat connection
			Chat chat = AlturaComponent.connection.createChat(jabberId);

			m = new ChatMediator(mediator, chat);
			m.registerCloseActionListener(this);

			chatMap.put(jabberId, m);
		}

		addTab("Chatting with " + m.getChat().getParticipant(), m);

		return m;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.chat.ui.conversation.IConversationController#getSelected()
	 */
	public IChatMediator getSelected() {
		int index = getSelectedIndex();

		return get(index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.chat.ui.conversation.IConversationController#get(int)
	 */
	public IChatMediator get(int index) {
		return null;

		// return (ChatMediator) chatList.get(index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.chat.ui.conversation.IConversationController#closeSelected()
	 */
	public void closeSelected() {
		int index = getSelectedIndex();
		remove(index);
	}

	public void actionPerformed(ActionEvent event) {
		if (event.getActionCommand().equals("CLOSE")) {
			closeSelected();
		}

	}
}