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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.EventListenerList;

import org.columba.core.charset.CharsetEvent;
import org.columba.core.charset.CharsetListener;
import org.columba.core.charset.CharsetOwnerInterface;
import org.columba.core.command.CommandCancelledException;
import org.columba.core.command.CommandProcessor;
import org.columba.core.gui.menu.ExtendablePopupMenu;
import org.columba.core.xml.XmlElement;
import org.columba.mail.command.IMailFolderCommandReference;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.config.MailConfig;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.mail.gui.message.command.ViewMessageCommand;
import org.columba.mail.gui.message.filter.PGPMessageFilter;
import org.columba.mail.gui.message.viewer.HeaderViewer;
import org.columba.mail.gui.message.viewer.MessageBorder;
import org.columba.mail.gui.message.viewer.SecurityStatusViewer;
import org.columba.mail.gui.message.viewer.SpamStatusViewer;
import org.columba.mail.gui.message.viewer.TextViewer;
import org.columba.ristretto.message.MimePart;
import org.columba.ristretto.message.MimeTree;
import org.columba.ristretto.message.MimeType;

/**
 * Shows the message. This includes message headers, body text, attachments and
 * status.
 */
public class MessageController extends JPanel implements CharsetListener,
		IMessageController {

	private MailFrameMediator frameController;

	private SecurityStatusViewer securityStatusViewer;

	private TextViewer bodytextViewer;

	private SpamStatusViewer spamStatusViewer;

	private HeaderViewer headerController;

	private PGPMessageFilter pgpFilter;

	private ExtendablePopupMenu menu;

	private IMailbox folder;

	private Object uid;

	protected EventListenerList listenerList = new EventListenerList();
	
	public MessageController(MailFrameMediator frameMediator) {
		this.frameController = frameMediator;

		Border outterBorder = BorderFactory.createCompoundBorder(BorderFactory
				.createEmptyBorder(5, 5, 5, 5), new MessageBorder(
				Color.LIGHT_GRAY, 1, true));
		Border innerBorder = BorderFactory.createCompoundBorder(outterBorder,
				new LineBorder(Color.WHITE, 5, true));

		setBorder(innerBorder);

		initComponents();

		layoutComponents();

		((CharsetOwnerInterface) getFrameController()).addCharsetListener(this);

		addComponentListener(new ComponentListener() {

			public void componentHidden(ComponentEvent e) {
			}

			public void componentMoved(ComponentEvent e) {
			}

			public void componentResized(ComponentEvent e) {
				try {
					if (getSelectedMessageId() != null) {
						updateGUI();
						repaint();
					}

				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}

			public void componentShown(ComponentEvent e) {
			}

		});
	}

	private void initComponents() {
		spamStatusViewer = new SpamStatusViewer(this);
		bodytextViewer = new TextViewer(this);
		securityStatusViewer = new SecurityStatusViewer();
		headerController = new HeaderViewer(this, securityStatusViewer,
				spamStatusViewer);

		pgpFilter = new PGPMessageFilter(getFrameController(), this);
		pgpFilter.addSecurityStatusListener(securityStatusViewer);

	}

	private void layoutComponents() {

		Color backgroundColor = UIManager.getColor("TextField.background");

		setLayout(new BorderLayout());

		JPanel top = new JPanel();
		top.setBackground(backgroundColor);
		top.setLayout(new BorderLayout());

		if (spamStatusViewer.isVisible())
			top.add(spamStatusViewer.getView(), BorderLayout.NORTH);

		top.add(headerController, BorderLayout.CENTER);

		add(top, BorderLayout.NORTH);

		// JPanel bottom = new JPanel();
		// bottom.setBackground(backgroundColor);
		//
		// bottom.setLayout(new BorderLayout());

		JComponent c = bodytextViewer.getView();
		c.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
		c.setBackground(backgroundColor);
		// bottom.add(c, BorderLayout.CENTER);

		add(c, BorderLayout.CENTER);
	}

	public void clear() {
		// TODO implement clear()

	}

	/**
	 * @see org.columba.mail.gui.message.IMessageController#filterMessage(org.columba.mail.folder.IMailbox,
	 *      java.lang.Object)
	 */
	public IMailFolderCommandReference filterMessage(IMailbox folder, Object uid)
			throws Exception {
		return pgpFilter.filter(folder, uid);
	}

	/**
	 * Returns the mailFrameController.
	 * 
	 * @return MailFrameController
	 */
	public MailFrameMediator getFrameController() {
		return frameController;
	}

	/**
	 * @see org.columba.core.util.CharsetListener#charsetChanged(org.columba.core.util.CharsetEvent)
	 */
	public void charsetChanged(CharsetEvent e) {
		CommandProcessor.getInstance().addOp(
				new ViewMessageCommand(getFrameController(),
						((MailFrameMediator) getFrameController())
								.getTableSelection()));
	}

	/** *********************************************************************** */

	/**
	 * Show message in messages viewer. Should be called in
	 * <code>Command.execute()</code> or in another background thread.
	 * 
	 * @param folder
	 *            selected folder
	 * @param uid
	 *            selected message UID
	 * @throws Exception
	 */
	public void showMessage(IMailbox folder, Object uid) throws Exception {
		this.folder = folder;
		this.uid = uid;

		// if necessary decrypt/verify message
		IMailFolderCommandReference newRefs = filterMessage(folder, uid);

		// map to new reference
		if (newRefs != null) {
			folder = (IMailbox) newRefs.getSourceFolder();
			uid = newRefs.getUids()[0];
		}

		MimeTree mimePartTree = folder.getMimePartTree(uid);
		if (mimePartTree == null)
			throw new CommandCancelledException(
					"Message does not exist anymore.");

		MimePart mp = chooseBodyPart(mimePartTree);
		if (mp != null)
			bodytextViewer.view(folder, uid, mp.getAddress(), this
					.getFrameController());

		spamStatusViewer.view(folder, uid, this.getFrameController());
		securityStatusViewer.view(folder, uid, this.getFrameController());

		headerController.view(folder, uid, this.getFrameController());

		// notify all interested listeners
		fireMessageSelectionChangedEvent(folder, uid.toString());
	}

	/**
	 * Revalidate message viewer components.
	 * <p>
	 * Call this method after showMessage() to force a repaint():
	 * 
	 */
	public void updateGUI() throws Exception {

		bodytextViewer.updateGUI();

		spamStatusViewer.updateGUI();
		securityStatusViewer.updateGUI();

		headerController.updateGUI();

	}

	/**
	 * @see org.columba.mail.gui.message.IMessageController#getSelectedReference()
	 */
	public IMailFolderCommandReference getSelectedReference() {
		return new MailFolderCommandReference(folder, new Object[] { uid });
	}

	public JComponent getView() {
		return this;
	}

	/**
	 * @see org.columba.mail.gui.message.IMessageController#getSelectedText()
	 */
	public String getSelectedText() {
		return bodytextViewer.getSelectedText();
	}

	/**
	 * @see org.columba.mail.gui.message.IMessageController#getText()
	 */
	public String getText() {
		return bodytextViewer.getText();
	}

	public IMailbox getSelectedFolder() {
		return folder;
	}

	public Object getSelectedMessageId() {
		return uid;
	}

	private MimePart chooseBodyPart(MimeTree mimePartTree) {
		MimePart bodyPart = null;

		XmlElement html = MailConfig.getInstance().getMainFrameOptionsConfig()
				.getRoot().getElement("/options/html");

		// ensure that there is an HTML part in the email, otherwise JTextPanel
		// throws a RuntimeException

		// Which Bodypart shall be shown? (html/plain)
		if ((Boolean.valueOf(html.getAttribute("prefer")).booleanValue())
				&& hasHtmlPart(mimePartTree.getRootMimeNode())) {
			bodyPart = mimePartTree.getFirstTextPart("html");
		} else {
			bodyPart = mimePartTree.getFirstTextPart("plain");
		}

		return bodyPart;

	}

	private boolean hasHtmlPart(MimePart mimeTypes) {

		if (mimeTypes.getHeader().getMimeType().equals(
				new MimeType("text", "plain")))
			return true; // exit immediately

		java.util.List children = mimeTypes.getChilds();

		for (int i = 0; i < children.size(); i++) {
			if (hasHtmlPart(mimeTypes.getChild(i)))
				return true;
		}

		return false;

	}

	/**
	 * @see org.columba.mail.gui.message.IMessageController#setCaretPosition(int)
	 */
	public void setCaretPosition(int position) {
		bodytextViewer.setCaretPosition(position);
	}

	/**
	 * @see org.columba.mail.gui.message.IMessageController#moveCaretPosition(int)
	 */
	public void moveCaretPosition(int position) {
		bodytextViewer.moveCaretPosition(position);
	}

	public void addMessageSelectionListener(IMessageSelectionListener l) {
		listenerList.add(IMessageSelectionListener.class, l);
	}

	public void removeMessageSelectionListener(IMessageSelectionListener l) {
		listenerList.remove(IMessageSelectionListener.class, l);
	}
	
	protected void fireMessageSelectionChangedEvent(IMailbox folder, String messageId) {

		IMessageSelectionEvent e = new MessageSelectionEvent(this, folder, messageId);
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IMessageSelectionListener.class) {
				((IMessageSelectionListener) listeners[i + 1]).selectionChanged(e);
			}
		}
	}

}