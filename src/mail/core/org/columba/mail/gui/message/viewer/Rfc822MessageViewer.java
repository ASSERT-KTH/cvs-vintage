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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Observer;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;

import org.columba.core.gui.frame.DefaultContainer;
import org.columba.core.gui.menu.ColumbaPopupMenu;
import org.columba.core.gui.mimetype.MimeTypeViewer;
import org.columba.core.gui.util.FontProperties;
import org.columba.mail.command.IMailFolderCommandReference;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.folder.AbstractMessageFolder;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.gui.composer.ComposerController;
import org.columba.mail.gui.composer.ComposerModel;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.mail.gui.message.URLObservable;
import org.columba.mail.gui.message.filter.PGPMessageFilter;
import org.columba.mail.gui.message.util.ColumbaURL;

/**
 * Viewer for a complete RFC822 message.
 * 
 * @author fdietz
 */
public class Rfc822MessageViewer extends JPanel implements Viewer, Scrollable {

	protected AttachmentsViewer attachmentsViewer;

	private EncryptionStatusViewer securityInformationController;

	private BodyTextViewer bodytextViewer;

	private SpamStatusViewer spamStatusController;

	private HeaderViewer headerController;

	private InlineAttachmentsViewer inlineAttachmentsViewer;

	private PGPMessageFilter pgpFilter;

	private URLObservable urlObservable;

	private ColumbaPopupMenu menu;

	private MailFrameMediator mediator;

	/**
	 *  
	 */
	public Rfc822MessageViewer(MailFrameMediator mediator) {
		super();

		this.mediator = mediator;

		initComponents();

		layoutComponents();

	}

	/**
	 * @see org.columba.mail.gui.message.viewer.Viewer#view(org.columba.mail.folder.IMailbox,
	 *      java.lang.Object, org.columba.mail.gui.frame.MailFrameMediator)
	 */
	public void view(IMailbox folder, Object uid, MailFrameMediator mediator)
			throws Exception {

		//		 if necessary decrypt/verify message
		IMailFolderCommandReference newRefs = filterMessage(folder, uid);

		// pass work along to MessageController
		if (newRefs != null) {
			folder = (AbstractMessageFolder) newRefs.getSourceFolder();
			uid = newRefs.getUids()[0];
			//mimePartTree = srcFolder.getMimePartTree(uid);
		}

		getBodytextViewer().view(folder, uid, mediator);
		getHeaderController().view(folder, uid, mediator);
		attachmentsViewer.view(folder, uid, mediator);
		inlineAttachmentsViewer.view(folder, uid, mediator);

		getSpamStatusViewer().view(folder, uid, mediator);
		getSecurityInformationViewer().view(folder, uid, mediator);
	}

	/**
	 * @see org.columba.mail.gui.message.viewer.Viewer#updateGUI()
	 */
	public void updateGUI() throws Exception {
		getBodytextViewer().updateGUI();
		getHeaderController().updateGUI();
		attachmentsViewer.updateGUI();
		inlineAttachmentsViewer.updateGUI();
		getSpamStatusViewer().updateGUI();
		getSecurityInformationViewer().updateGUI();

		layoutComponents();

	}

	/**
	 * @see org.columba.mail.gui.message.viewer.Viewer#getView()
	 */
	public JComponent getView() {

		return null;
	}

	/**
	 * @see org.columba.mail.gui.message.viewer.Viewer#isVisible()
	 */
	public boolean isVisible() {
		return true;
	}

	/**
	 * @return Returns the attachmentsViewer.
	 */
	public AttachmentsViewer getAttachmentsViewer() {
		return attachmentsViewer;
	}

	/**
	 * @return Returns the bodytextViewer.
	 */
	public BodyTextViewer getBodytextViewer() {
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
	public InlineAttachmentsViewer getInlineAttachmentsViewer() {
		return inlineAttachmentsViewer;
	}

	/**
	 * @return Returns the pgpFilter.
	 */
	public PGPMessageFilter getPgpFilter() {
		return pgpFilter;
	}

	/**
	 * @return Returns the securityInformationController.
	 */
	public EncryptionStatusViewer getSecurityInformationViewer() {
		return securityInformationController;
	}

	/**
	 * @return Returns the spamStatusController.
	 */
	public SpamStatusViewer getSpamStatusViewer() {
		return spamStatusController;
	}

	/**
	 * return the PopupMenu for the message viewer
	 */
	public JPopupMenu getPopupMenu() {
		return menu;
	}

	public void setAttachmentSelectionReference(MailFolderCommandReference ref) {
		getAttachmentsViewer().setLocalReference(ref);
	}

	public MailFolderCommandReference getAttachmentSelectionReference() {
		return getAttachmentsViewer().getLocalReference();
	}

	private void initComponents() {
		spamStatusController = new SpamStatusViewer(mediator);
		bodytextViewer = new BodyTextViewer(mediator);
		securityInformationController = new EncryptionStatusViewer(mediator);
		headerController = new HeaderViewer(mediator);

		headerController.getHeaderTextPane().addMouseListener(
				new MyMouseListener());
		bodytextViewer.addMouseListener(new MyMouseListener());

		attachmentsViewer = new AttachmentsViewer(mediator);

		inlineAttachmentsViewer = new InlineAttachmentsViewer();

		pgpFilter = new PGPMessageFilter(mediator, this);
		pgpFilter.addSecurityStatusListener(securityInformationController);
		pgpFilter.addSecurityStatusListener(headerController.getStatusPanel());

		urlObservable = new URLObservable();
	}

	private void layoutComponents() {

		removeAll();

		setLayout(new BorderLayout());

		JPanel top = new JPanel();
		top.setLayout(new BorderLayout());

		if (spamStatusController.isVisible())
			top.add(spamStatusController.getView(), BorderLayout.NORTH);

		if (headerController.isVisible())
			top.add(headerController.getView(), BorderLayout.CENTER);

		add(top, BorderLayout.NORTH);

		add(bodytextViewer, BorderLayout.CENTER);

		JPanel bottom = new JPanel();
		bottom.setLayout(new BorderLayout());

		if (securityInformationController.isVisible())
			bottom.add(securityInformationController.getView(),
					BorderLayout.NORTH);

		bottom.add(attachmentsViewer, BorderLayout.CENTER);
		//bottom.add(inlineAttachmentsViewer, BorderLayout.CENTER);

		add(bottom, BorderLayout.SOUTH);
	}

	public void clear() {
		removeAll();
	}

	public void createPopupMenu() {
		if ( menu == null)
		menu = new ColumbaPopupMenu(mediator,
				"org/columba/mail/action/message_contextmenu.xml");
	}

	protected void processPopup(MouseEvent ev) {
		//        final URL url = extractURL(ev);
		ColumbaURL mailto = extractMailToURL(ev);
		urlObservable.setUrl(mailto);

		final MouseEvent event = ev;
		// open context-menu
		// -> this has to happen in the awt-event dispatcher thread
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				getPopupMenu().show(event.getComponent(), event.getX(),
						event.getY());
			}
		});
	}

	protected URL extractURL(MouseEvent event) {
		JEditorPane pane = (JEditorPane) event.getSource();
		HTMLDocument doc = (HTMLDocument) pane.getDocument();

		Element e = doc.getCharacterElement(pane.viewToModel(event.getPoint()));
		AttributeSet a = e.getAttributes();
		AttributeSet anchor = (AttributeSet) a.getAttribute(HTML.Tag.A);

		if (anchor == null) {
			return null;
		}

		URL url = null;

		try {
			url = new URL((String) anchor.getAttribute(HTML.Attribute.HREF));
		} catch (MalformedURLException mue) {
			return null;
		}

		return url;
	}

	/**
	 * this method extracts any url, but if URL's protocol is mailto: then this
	 * method also extracts the corresponding recipient name whatever it may be.
	 * <br>
	 * This "kind of" superseeds the previous extractURL(MouseEvent) method.
	 */
	private ColumbaURL extractMailToURL(MouseEvent event) {

		ColumbaURL url = new ColumbaURL(extractURL(event));
		if (url.getRealURL() == null)
			return null;

		if (!url.getRealURL().getProtocol().equalsIgnoreCase("mailto"))
			return url;

		JEditorPane pane = (JEditorPane) event.getSource();
		HTMLDocument doc = (HTMLDocument) pane.getDocument();

		Element e = doc.getCharacterElement(pane.viewToModel(event.getPoint()));
		AttributeSet a = e.getAttributes();
		AttributeSet anchor = (AttributeSet) a.getAttribute(HTML.Tag.A);

		try {
			url.setSender(doc.getText(e.getStartOffset(), (e.getEndOffset() - e
					.getStartOffset())));
		} catch (BadLocationException e1) {
			url.setSender("");
		}

		return url;
	}

	class MyMouseListener implements MouseListener {

		public void mousePressed(MouseEvent event) {
			if (event.isPopupTrigger()) {
				processPopup(event);
			}
		}

		public void mouseReleased(MouseEvent event) {
			if (event.isPopupTrigger()) {
				processPopup(event);
			}
		}

		public void mouseEntered(MouseEvent event) {
		}

		public void mouseExited(MouseEvent event) {
		}

		public void mouseClicked(MouseEvent event) {
			if (!SwingUtilities.isLeftMouseButton(event)) {
				return;
			}

			URL url = extractURL(event);

			if (url == null) {
				return;
			}

			getUrlObservable().setUrl(new ColumbaURL(url));

			//URLController c = new URLController();

			if (url.getProtocol().equalsIgnoreCase("mailto")) {
				// open composer
				ComposerController controller = new ComposerController();
				new DefaultContainer(controller);

				ComposerModel model = new ComposerModel();
				model.setTo(url.getFile());

				// apply model
				controller.setComposerModel(model);

				controller.updateComponents(true);
			} else {
				// open url
				new MimeTypeViewer().openURL(url);
			}
		}
	}

	/**
	 * @return Returns the urlObservable.
	 */
	public URLObservable getUrlObservable() {
		return urlObservable;
	}

	/**
	 * @see org.columba.mail.gui.message.IMessageController#filterMessage(org.columba.mail.folder.IMailbox,
	 *      java.lang.Object)
	 */
	public IMailFolderCommandReference filterMessage(IMailbox folder, Object uid)
			throws Exception {
		return getPgpFilter().filter(folder, uid);
	}

	/**
	 * @see org.columba.mail.gui.message.IMessageController#addURLObserver(java.util.Observer)
	 */
	public void addURLObserver(Observer observer) {
		getUrlObservable().addObserver(observer);
	}

	public String getSelectedText() {
		return getBodytextViewer().getSelectedText();
	}

	/** ************** Scrollable interface ******************** */

	/**
	 * @see javax.swing.Scrollable#getPreferredScrollableViewportSize()
	 */
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	/**
	 * @see javax.swing.Scrollable#getScrollableUnitIncrement(java.awt.Rectangle,
	 *      int, int)
	 */
	public int getScrollableUnitIncrement(Rectangle arg0, int arg1, int arg2) {
		Font textFont = FontProperties.getTextFont();

		return textFont.getSize() * 3;
	}

	/**
	 * @see javax.swing.Scrollable#getScrollableBlockIncrement(java.awt.Rectangle,
	 *      int, int)
	 */
	public int getScrollableBlockIncrement(Rectangle arg0, int arg1, int arg2) {
		Font textFont = FontProperties.getTextFont();

		return textFont.getSize() * 10;
	}

	/**
	 * @see javax.swing.Scrollable#getScrollableTracksViewportWidth()
	 */
	public boolean getScrollableTracksViewportWidth() {
		return true;
	}

	/**
	 * @see javax.swing.Scrollable#getScrollableTracksViewportHeight()
	 */
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

}