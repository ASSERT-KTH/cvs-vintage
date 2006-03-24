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

import java.io.IOException;

import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.columba.chat.MainInterface;
import org.columba.chat.model.api.IBuddyStatus;
import org.columba.chat.ui.conversation.api.IMessageViewer;
import org.jivesoftware.smack.packet.Message;

/**
 * HTML view.
 * 
 * @author fdietz
 * 
 */
public class HTMLViewer extends JEditorPane implements IMessageViewer {

	private HTMLEditorKit kit;

	private HTMLDocument doc;

	private StringBuffer buffer = new StringBuffer();

	/**
	 * 
	 */
	public HTMLViewer() {
		super();

		setContentType("text/html; enctype='UTF-8'");

		kit = (HTMLEditorKit) getEditorKit();
		doc = (HTMLDocument) getDocument();

		/*
		 * Font font = UIManager.getFont("Label.font"); String name =
		 * font.getName(); String size = Integer.toString(font.getSize());
		 */

		StyleSheet myStyleSheet = new StyleSheet();
		myStyleSheet.addRule("body { font-size: 12 }");
		myStyleSheet
				.addRule("body a { color: #5B62BC; text-decoration: underline }");

		kit.setStyleSheet(myStyleSheet);
		doc = new HTMLDocument(myStyleSheet);
		setDocument(doc);
		setEditable(false);

	}

	protected void append(String html) {
		html = html.replaceAll("\\n", "<br>");

		buffer.append(html);

		try {

			kit.insertHTML(doc, doc.getLength(), html, 0, 0, null);
		} catch (BadLocationException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	/**
	 * @see org.altura.ui.conversation.view.IViewer#displayReceivedMessage(org.jivesoftware.smack.packet.Message,
	 *      org.altura.jabber.BuddyStatus)
	 */
	public void displayReceivedMessage(Message message, IBuddyStatus buddy) {
		String body = message.getBody();
		String from = message.getFrom();

		//DateFormat format = DateFormat.getTimeInstance(DateFormat.SHORT);
		// green color
		append("<font color='#348756'>" + from + "</font>: " + body);

	}

	/**
	 * @see org.altura.ui.conversation.view.IViewer#displaySendMessage(org.jivesoftware.smack.packet.Message,
	 *      org.altura.jabber.BuddyStatus)
	 */
	public void displaySendMessage(Message message, IBuddyStatus buddy) {
		String body = message.getBody();
		String to = MainInterface.config.getAccount().getId();

		//DateFormat format = DateFormat.getTimeInstance(DateFormat.SHORT);
		append("<font color='#2B3780'>" + to + "</font>: " + body);
	}

}