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
package org.columba.mail.gui.message.viewer;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import org.columba.core.xml.XmlElement;
import org.columba.mail.command.IMailFolderCommandReference;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.config.MailConfig;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.mail.gui.message.MessageController;
import org.columba.mail.gui.message.filter.PGPMessageFilter;
import org.columba.ristretto.message.MimePart;
import org.columba.ristretto.message.MimeTree;
import org.columba.ristretto.message.MimeType;

/**
 * Viewer for a complete RFC822 message.
 * 
 * @author fdietz
 */
public class MessageViewer extends JPanel implements ICustomViewer {

	private SecurityStatusViewer securityInformationController;

	private TextViewer bodytextViewer;

	private SpamStatusViewer spamStatusController;

	private HeaderViewer headerController;

	private PGPMessageFilter pgpFilter;

	private MessageController mediator;

	/**
	 * 
	 */
	public MessageViewer(MessageController mediator) {
		super();

		this.mediator = mediator;

		Border outterBorder = BorderFactory.createCompoundBorder(BorderFactory
				.createEmptyBorder(10, 10, 10, 10), new MessageBorder(
				Color.LIGHT_GRAY, 1, true));
		Border innerBorder = BorderFactory.createCompoundBorder(outterBorder,
				new LineBorder(Color.WHITE, 5, true));

		setBorder(innerBorder);

		initComponents();

		layoutComponents();

	}

	/**
	 * @see org.columba.mail.gui.message.viewer.IViewer#view(org.columba.mail.folder.IMailbox,
	 *      java.lang.Object, org.columba.mail.gui.frame.MailFrameMediator)
	 */
	public void view(IMailbox folder, Object uid, MailFrameMediator mediator)
			throws Exception {

		// if necessary decrypt/verify message
		IMailFolderCommandReference newRefs = filterMessage(folder, uid);

		// map to new reference
		if (newRefs != null) {
			folder = (IMailbox) newRefs.getSourceFolder();
			uid = newRefs.getUids()[0];
		}

		getHeaderController().view(folder, uid, mediator);

		MimeTree mimePartTree = folder.getMimePartTree(uid);
		MimePart mp = chooseBodyPart(mimePartTree);
		if (mp != null)
			getBodytextViewer().view(folder, uid, mp.getAddress(), mediator);

		getSpamStatusViewer().view(folder, uid, mediator);
		getSecurityInformationViewer().view(folder, uid, mediator);
	}

	/**
	 * @see org.columba.mail.gui.message.viewer.IViewer#updateGUI()
	 */
	public void updateGUI() throws Exception {

		getHeaderController().updateGUI();

		getBodytextViewer().updateGUI();

		getSpamStatusViewer().updateGUI();
		getSecurityInformationViewer().updateGUI();

	}

	/**
	 * @see org.columba.mail.gui.message.viewer.IViewer#getView()
	 */
	public JComponent getView() {

		return this;
	}

	/**
	 * @see org.columba.mail.gui.message.viewer.IViewer#isVisible()
	 */
	public boolean isVisible() {
		return true;
	}

	/**
	 * @return Returns the bodytextViewer.
	 */
	public TextViewer getBodytextViewer() {
		return bodytextViewer;
	}

	/**
	 * @return Returns the headerController.
	 */
	public HeaderViewer getHeaderController() {
		return headerController;
	}

	/**
	 * @return Returns the inlineAttachmentsViewer.
	 */
	// public InlineAttachmentsViewer getInlineAttachmentsViewer() {
	// return inlineAttachmentsViewer;
	// }
	/**
	 * @return Returns the pgpFilter.
	 */
	public PGPMessageFilter getPgpFilter() {
		return pgpFilter;
	}

	/**
	 * @return Returns the securityInformationController.
	 */
	public SecurityStatusViewer getSecurityInformationViewer() {
		return securityInformationController;
	}

	/**
	 * @return Returns the spamStatusController.
	 */
	public SpamStatusViewer getSpamStatusViewer() {
		return spamStatusController;
	}

	public void setAttachmentSelectionReference(MailFolderCommandReference ref) {
		// TODO
	}

	public MailFolderCommandReference getAttachmentSelectionReference() {
		// TODO
		return null;
	}

	private void initComponents() {
		spamStatusController = new SpamStatusViewer(mediator);
		bodytextViewer = new TextViewer(mediator);
		securityInformationController = new SecurityStatusViewer(mediator);
		headerController = new HeaderViewer(mediator,
				securityInformationController, spamStatusController);

		pgpFilter = new PGPMessageFilter(mediator.getFrameController(), mediator);
		pgpFilter.addSecurityStatusListener(securityInformationController);

	}

	private void layoutComponents() {

		Color backgroundColor = UIManager.getColor("TextField.background");

		setLayout(new BorderLayout());

		JPanel top = new JPanel();
		top.setBackground(backgroundColor);
		top.setLayout(new BorderLayout());

		if (spamStatusController.isVisible())
			top.add(spamStatusController.getView(), BorderLayout.NORTH);

		top.add(headerController, BorderLayout.CENTER);

		add(top, BorderLayout.NORTH);

		JPanel bottom = new JPanel();
		bottom.setBackground(backgroundColor);

		bottom.setLayout(new BorderLayout());

		JComponent c = getBodytextViewer().getView();
		c.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		c.setBackground(backgroundColor);
		bottom.add(c, BorderLayout.CENTER);

		add(bottom, BorderLayout.CENTER);
	}

	public void clear() {
		removeAll();
	}

	/**
	 * @see org.columba.mail.gui.message.IMessageController#filterMessage(org.columba.mail.folder.IMailbox,
	 *      java.lang.Object)
	 */
	public IMailFolderCommandReference filterMessage(IMailbox folder, Object uid)
			throws Exception {
		return getPgpFilter().filter(folder, uid);
	}

	public String getSelectedText() {
		return getBodytextViewer().getSelectedText();
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
	 * @param mimePartTree
	 */
	private Integer[] getBodyPartAddress(MimeTree mimePartTree) {
		MimePart bodyPart = null;
		XmlElement html = MailConfig.getInstance().getMainFrameOptionsConfig()
				.getRoot().getElement("/options/html");

		// Which Bodypart shall be shown? (html/plain)
		if ((Boolean.valueOf(html.getAttribute("prefer")).booleanValue())
				&& hasHtmlPart(mimePartTree.getRootMimeNode())) {
			bodyPart = mimePartTree.getFirstTextPart("html");
		} else {
			bodyPart = mimePartTree.getFirstTextPart("plain");
		}

		return bodyPart.getAddress();
	}

}