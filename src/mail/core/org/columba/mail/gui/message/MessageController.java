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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;

import org.columba.core.charset.CharsetEvent;
import org.columba.core.charset.CharsetListener;
import org.columba.core.charset.CharsetOwnerInterface;
import org.columba.core.command.CommandProcessor;
import org.columba.core.gui.focus.FocusManager;
import org.columba.core.gui.focus.FocusOwner;
import org.columba.core.gui.frame.DefaultContainer;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.gui.menu.ColumbaPopupMenu;
import org.columba.core.gui.mimetype.MimeTypeViewer;
import org.columba.core.gui.util.FontProperties;
import org.columba.mail.command.IMailFolderCommandReference;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.gui.composer.ComposerController;
import org.columba.mail.gui.composer.ComposerModel;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.mail.gui.message.command.ViewMessageCommand;
import org.columba.mail.gui.message.filter.PGPMessageFilter;
import org.columba.mail.gui.message.util.ColumbaURL;
import org.columba.mail.gui.message.viewer.AttachmentsViewer;
import org.columba.mail.gui.message.viewer.BodyTextViewer;
import org.columba.mail.gui.message.viewer.EncryptionStatusViewer;
import org.columba.mail.gui.message.viewer.HeaderViewer;
import org.columba.mail.gui.message.viewer.InlineAttachmentsViewer;
import org.columba.mail.gui.message.viewer.SpamStatusViewer;

/**
 * this class shows the messagebody
 */
public class MessageController extends JScrollPane implements
		HyperlinkListener, MouseListener, CharsetListener, FocusOwner,
		CaretListener, IMessageController {

	private IMailbox folder;

	private Object uid;

	private ColumbaPopupMenu menu;

	private JButton button;

	private URLObservable urlObservable;

	//    protected AbstractMailFrameController abstractFrameController;
	protected FrameMediator frameController;

	protected AttachmentsViewer attachmentsViewer;

	private EncryptionStatusViewer securityInformationController;

	private BodyTextViewer bodytextViewer;

	private SpamStatusViewer spamStatusController;

	private HeaderViewer headerController;

	private InlineAttachmentsViewer inlineAttachmentsViewer;
	
	private PGPMessageFilter pgpFilter;

	public static final int VIEWER_HTML = 1;

	public static final int VIEWER_SIMPLE = 0;

	private MouseListener listener;

	private int active;

	private JPanel panel;

	//private MessageSelectionManager messageSelectionManager;
	public MessageController(FrameMediator frameMediator) {
		this.frameController = frameMediator;
		
		spamStatusController = new SpamStatusViewer(
				(MailFrameMediator) frameController);
		bodytextViewer = new BodyTextViewer();
		securityInformationController = new EncryptionStatusViewer();
		headerController = new HeaderViewer();

		headerController.getHeaderTextPane().addHyperlinkListener(this);
		bodytextViewer.addHyperlinkListener(this);
		headerController.getHeaderTextPane().addMouseListener(this);
		bodytextViewer.addMouseListener(this);
		bodytextViewer.addCaretListener(this);

		attachmentsViewer = new AttachmentsViewer((MailFrameMediator)frameMediator);
		
		inlineAttachmentsViewer = new InlineAttachmentsViewer();
		
		pgpFilter = new PGPMessageFilter((MailFrameMediator)frameMediator, this);
		pgpFilter.addSecurityStatusListener(securityInformationController);
		pgpFilter.addSecurityStatusListener(headerController.getStatusPanel());

		getViewport().setBackground(Color.white);

		layoutComponents();
		//view.addHyperlinkListener(this);
		getViewport().getView().addMouseListener(this);

		((CharsetOwnerInterface) getFrameController()).addCharsetListener(this);

		FocusManager.getInstance().registerComponent(this);

		urlObservable = new URLObservable();
	}

	public void clear() {
		panel = new MessagePanel();
		setViewportView(panel);
	}

	private void layoutComponents() {

		panel = new MessagePanel();

		panel.setLayout(new BorderLayout());

		setViewportView(panel);

		active = VIEWER_SIMPLE;

		JPanel top = new JPanel();
		top.setLayout(new BorderLayout());

		if (spamStatusController.isVisible())
			top.add(spamStatusController.getView(), BorderLayout.NORTH);

		if (headerController.isVisible())
			top.add(headerController.getView(), BorderLayout.CENTER);

		panel.add(top, BorderLayout.NORTH);

		panel.add(bodytextViewer, BorderLayout.CENTER);

		JPanel bottom = new JPanel();
		bottom.setLayout(new BorderLayout());

		if (securityInformationController.isVisible())
			bottom.add(securityInformationController.getView(),
					BorderLayout.NORTH);

		bottom.add(attachmentsViewer, BorderLayout.CENTER);
		//bottom.add(inlineAttachmentsViewer, BorderLayout.CENTER);

		panel.add(bottom, BorderLayout.SOUTH);
	}

	class MessagePanel extends JPanel implements Scrollable {
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
		public int getScrollableBlockIncrement(Rectangle arg0, int arg1,
				int arg2) {
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

	public void createPopupMenu() {
		menu = new ColumbaPopupMenu(frameController,
				"org/columba/mail/action/message_contextmenu.xml");
	}

	/**
	 * return the PopupMenu for the message viewer
	 */
	public JPopupMenu getPopupMenu() {
		return menu;
	}

	public void hyperlinkUpdate(HyperlinkEvent e) {
	}

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

	/**
	 * ******************* context menu
	 * ******************************************
	 */
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

	/** ***************** FocusOwner interface ********************** */

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.core.gui.focus.FocusOwner#copy()
	 */
	public void copy() {
		getBodytextViewer().copy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.core.gui.focus.FocusOwner#cut()
	 */
	public void cut() {
		// not supported
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.core.gui.focus.FocusOwner#delete()
	 */
	public void delete() {
		// not supported
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.core.gui.focus.FocusOwner#getComponent()
	 */
	public JComponent getComponent() {
		return getBodytextViewer();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.core.gui.focus.FocusOwner#isCopyActionEnabled()
	 */
	public boolean isCopyActionEnabled() {
		if (getBodytextViewer().getSelectedText() == null) {
			return false;
		}

		if (getBodytextViewer().getSelectedText().length() > 0) {
			return true;
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.core.gui.focus.FocusOwner#isCutActionEnabled()
	 */
	public boolean isCutActionEnabled() {
		// action not support
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.core.gui.focus.FocusOwner#isDeleteActionEnabled()
	 */
	public boolean isDeleteActionEnabled() {
		// action not supported
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.core.gui.focus.FocusOwner#isPasteActionEnabled()
	 */
	public boolean isPasteActionEnabled() {
		// action not supported
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.core.gui.focus.FocusOwner#isRedoActionEnabled()
	 */
	public boolean isRedoActionEnabled() {
		// action not supported
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.core.gui.focus.FocusOwner#isSelectAllActionEnabled()
	 */
	public boolean isSelectAllActionEnabled() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.core.gui.focus.FocusOwner#isUndoActionEnabled()
	 */
	public boolean isUndoActionEnabled() {
		// action not supported
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.core.gui.focus.FocusOwner#paste()
	 */
	public void paste() {
		// action not supported
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.core.gui.focus.FocusOwner#redo()
	 */
	public void redo() {
		// action not supported
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.core.gui.focus.FocusOwner#selectAll()
	 */
	public void selectAll() {
		getBodytextViewer().selectAll();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.core.gui.focus.FocusOwner#undo()
	 */
	public void undo() {

	}

	/** ************************ CaretUpdateListener interface **************** */

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.CaretListener#caretUpdate(javax.swing.event.CaretEvent)
	 */
	public void caretUpdate(CaretEvent arg0) {
		FocusManager.getInstance().updateActions();
	}

	/**
	 * @return
	 */
	public URLObservable getUrlObservable() {
		return urlObservable;
	}

	/** *********************************************************************** */

	/**
	 * @return
	 */
	public EncryptionStatusViewer getPgp() {
		return securityInformationController;
	}

	/**
	 * @return Returns the bodytextViewer.
	 */
	public BodyTextViewer getBodytextViewer() {
		return bodytextViewer;
	}
	
	public String getSelectedText() {
		return getBodytextViewer().getSelectedText();
	}

	/**
	 * @return Returns the spamStatus.
	 */
	public SpamStatusViewer getSpamStatusViewer() {
		return spamStatusController;
	}

	/**
	 * @return Returns the headerView.
	 */
	public HeaderViewer getHeaderController() {
		return headerController;
	}

	/**
	 * @return Returns the securityInformationViewer.
	 */
	public EncryptionStatusViewer getSecurityInformationViewer() {
		return securityInformationController;
	}

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

		getBodytextViewer().view(folder, uid,
				(MailFrameMediator) frameController);
		getHeaderController().view(folder, uid,
				(MailFrameMediator) frameController);
		attachmentsViewer.view(folder, uid,
				(MailFrameMediator) frameController);
		inlineAttachmentsViewer.view(folder, uid, (MailFrameMediator) frameController);
		
		getSpamStatusViewer().view(folder, uid,
				(MailFrameMediator) frameController);
		getSecurityInformationViewer().view(folder, uid,
				(MailFrameMediator) frameController);

	}

	/**
	 * Revalidate message viewer components.
	 * <p>
	 * Call this method after showMessage() to force a repaint():
	 *  
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
	 * @return Returns the pgpFilter.
	 */
	public PGPMessageFilter getPgpFilter() {
		return pgpFilter;
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

	/**
	 * @return Returns the attachmentsViewer.
	 */
	public AttachmentsViewer getAttachmentsViewer() {
		return attachmentsViewer;
	}
	
	public MailFolderCommandReference getAttachmentSelectionReference() {
		return getAttachmentsViewer().getLocalReference();
	}
	
	public void setAttachmentSelectionReference(MailFolderCommandReference ref) {
		getAttachmentsViewer().setLocalReference(ref);
	}
}