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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.gui.message;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.MouseListener;
import java.util.Observer;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.columba.core.charset.CharsetEvent;
import org.columba.core.charset.CharsetListener;
import org.columba.core.charset.CharsetOwnerInterface;
import org.columba.core.command.CommandProcessor;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.mail.gui.message.command.ViewMessageCommand;
import org.columba.mail.gui.message.viewer.Rfc822MessageViewer;

/**
 * this class shows the messagebody
 */
public class MessageController extends JScrollPane implements
		HyperlinkListener, CharsetListener, IMessageController {
	protected FrameMediator frameController;

	private MouseListener listener;

	private int active;

	private JPanel panel;

	private Rfc822MessageViewer messageViewer;

	public MessageController(FrameMediator frameMediator) {
		this.frameController = frameMediator;

		messageViewer = new Rfc822MessageViewer(
				(MailFrameMediator) frameMediator);

		getViewport().setBackground(Color.white);

		setViewportView(messageViewer);

		// FIXME
		//getViewport().getView().addMouseListener(new MyMouseListener());

		((CharsetOwnerInterface) getFrameController()).addCharsetListener(this);

	}

	public void clear() {
		messageViewer.clear();

		setViewportView(messageViewer);
	}

	public void hyperlinkUpdate(HyperlinkEvent e) {
	}

	/**
	 * Returns the mailFrameController.
	 * 
	 * @return MailFrameController
	 */
	public FrameMediator getFrameController() {
		return frameController;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.core.util.CharsetListener#charsetChanged(org.columba.core.util.CharsetEvent)
	 */
	public void charsetChanged(CharsetEvent e) {
		CommandProcessor.getInstance().addOp(
				new ViewMessageCommand(getFrameController(),
						((MailFrameMediator) getFrameController())
								.getTableSelection()));
	}

	/** ************************ CaretUpdateListener interface **************** */

	/** *********************************************************************** */

	/**
	 * Show message in messages viewer.
	 * <p>
	 * Should be called in Command.execute() or in another background thread.
	 * 
	 * @param folder
	 *            selected folder
	 * @param uid
	 *            selected message UID
	 * @throws Exception
	 */
	public void showMessage(IMailbox folder, Object uid) throws Exception {

		messageViewer.view(folder, uid, (MailFrameMediator) frameController);

	}

	/**
	 * Revalidate message viewer components.
	 * <p>
	 * Call this method after showMessage() to force a repaint():
	 *  
	 */
	public void updateGUI() throws Exception {

		messageViewer.updateGUI();

		getVerticalScrollBar().setValue(0);
	}

	/**
	 * @return Returns the messageViewer.
	 */
	public Rfc822MessageViewer getMessageViewer() {
		return messageViewer;
	}

	public MailFolderCommandReference getAttachmentSelectionReference() {
		return getMessageViewer().getAttachmentsViewer().getLocalReference();
	}

	public void addURLObserver(Observer observer) {
		getMessageViewer().getUrlObservable().addObserver(observer);
	}

	public String getSelectedText() {
		return getMessageViewer().getSelectedText();
	}

	public void createPopupMenu() {
		getMessageViewer().createPopupMenu();

	}
}