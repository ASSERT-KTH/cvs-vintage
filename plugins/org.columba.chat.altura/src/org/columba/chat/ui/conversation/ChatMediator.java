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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.columba.chat.frame.AlturaFrameMediator;
import org.columba.chat.jabber.BuddyList;
import org.columba.chat.jabber.BuddyStatus;
import org.columba.chat.ui.conversation.action.CloseAction;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * @author fdietz
 *  
 */
public class ChatMediator extends JPanel implements ActionListener {

	private Chat chat;

	private ReceivingMessageController receiving;

	private SendingMessageController sending;

	//	private SendButtonController sendButton;

	private JButton sendButton;

	private AlturaFrameMediator mediator;

	public ChatMediator(AlturaFrameMediator mediator, Chat chat) {
		super();

		this.mediator = mediator;
		this.chat = chat;

		receiving = new ReceivingMessageController(this);
		sending = new SendingMessageController(this);
		sendButton = new JButton("Send");
		sendButton.setActionCommand("SEND");
		sendButton.addActionListener(this);

		layoutComponents();
	}

	public void layoutComponents() {
		FormLayout mainLayout = new FormLayout("fill:pref:grow",
				"fill:default:grow, 3dlu, fill:default, 3dlu, fill:default");
		setLayout(mainLayout);
		//JPanel mainPanel = new JPanel(mainLayout);
		//mainPanel.setBorder(Borders.DIALOG_BORDER);

		CellConstraints cc = new CellConstraints();

		JScrollPane receivingPane = new JScrollPane(getReceiving());

		receivingPane.setPreferredSize(new Dimension(300, 250));

		add(receivingPane, cc.xy(1, 1));

		JScrollPane sendingPane = new JScrollPane(getSending());

		sendingPane.setPreferredSize(new Dimension(300, 100));

		add(sendingPane, cc.xy(1, 3));

		JButton closeButton = new JButton(new CloseAction(mediator));
		ButtonBarBuilder builder = new ButtonBarBuilder();
		builder.addGlue();
		builder.addGridded(closeButton);
		builder.addRelatedGap();
		builder.addGridded(sendButton);

		add(builder.getPanel(), cc.xy(1, 5));

	}

	/**
	 * @return Returns the chat.
	 */
	public Chat getChat() {
		return chat;
	}

	/**
	 * @return Returns the receiving.
	 */
	public ReceivingMessageController getReceiving() {
		return receiving;
	}

	/**
	 * @return Returns the sendButton.
	 */
	public JButton getSendButton() {
		return sendButton;
	}

	/**
	 * @return Returns the sending.
	 */
	public SendingMessageController getSending() {
		return sending;
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		String action = arg0.getActionCommand();

		if (action.equals("SEND")) {

			//      create message object
			Message message = getChat().createMessage();

			// set message body
			message.setBody(getSending().getText());

			String to = message.getTo();
			//      example: fdietz@jabber.org/Jabber-client
			// -> remove "/Jabber-client"
			String normalizedId = to.replaceAll("\\/.*", "");

			try {
				// send message
				getChat().sendMessage(message);

				// clear text box
				getSending().setText("");

				BuddyStatus buddyStatus = BuddyList.getInstance().getBuddy(
						normalizedId);

				//if (buddyStatus.getChatMediator() != null)
				buddyStatus.getChatMediator().getReceiving()
						.displaySendMessage(message, buddyStatus);

			} catch (XMPPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}