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
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.gui.message;

import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;

import org.columba.core.charset.CharsetEvent;
import org.columba.core.charset.CharsetListener;
import org.columba.core.charset.CharsetOwnerInterface;
import org.columba.core.config.Config;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.main.MainInterface;
import org.columba.mail.coder.CoderRouter;
import org.columba.mail.coder.Decoder;
import org.columba.mail.folder.Folder;
import org.columba.mail.gui.attachment.AttachmentController;
import org.columba.mail.gui.frame.AbstractMailFrameController;
import org.columba.mail.gui.message.action.MessageActionListener;
import org.columba.mail.gui.message.action.MessageFocusListener;
import org.columba.mail.gui.message.action.MessagePopupListener;
import org.columba.mail.gui.message.command.ViewMessageCommand;
import org.columba.mail.gui.table.selection.MessageSelectionListener;
import org.columba.mail.gui.util.URLController;
import org.columba.mail.message.HeaderInterface;
import org.columba.mail.message.MimePart;
import org.columba.mail.message.MimePartTree;

/**
 * this class shows the messagebody
 */

public class MessageController
	implements
		MessageSelectionListener,
		HyperlinkListener,
		MouseListener,
		CharsetListener {

	private Folder folder;
	private Object uid;

	private MessageMenu menu;

	private MessageFocusListener focusListener;

	private MessagePopupListener popupListener;

	private JButton button;

	private String activeCharset;

	private MessageView view;
	private MessageActionListener actionListener;

	protected AbstractMailFrameController abstractFrameController;
	protected AttachmentController attachmentController;
	//private MessageSelectionManager messageSelectionManager;

	public MessageController(
		AbstractMailFrameController abstractFrameController,
		AttachmentController attachmentController) {

		this.abstractFrameController = abstractFrameController;
		this.attachmentController = attachmentController;
		activeCharset = "auto";

		view = new MessageView(this, attachmentController.getView());
		//view.addHyperlinkListener(this);
		view.addMouseListener(this);

		actionListener = new MessageActionListener(this);

		((CharsetOwnerInterface) getFrameController())
			.getCharsetManager()
			.addCharsetListener(this);

		Font mainFont = Config.getOptionsConfig().getGuiItem().getMainFont();

		

	}

	public void messageSelectionChanged(Object[] newUidList) {
		
	}

	public MessageActionListener getActionListener() {
		return actionListener;
	}

	public MessageView getView() {


		return view;
	}

	/**
		* return the PopupMenu for the table
		*/
	public JPopupMenu getPopupMenu() {
		if (menu == null)
			menu = new MessageMenu(abstractFrameController);
		return menu;
	}

	public void setViewerFont(Font font) {
		//textPane.setFont( font );
	}

	public Object getUid() {
		return uid;
	}

	public Folder getFolder() {
		return folder;
	}

	public void setFolder(Folder f) {
		this.folder = f;
	}

	public void setUid(Object o) {
		this.uid = o;
	}

	public void showMessage(
		HeaderInterface header,
		MimePart bodyPart,
		MimePartTree mimePartTree)
		throws Exception {

		if (header == null || bodyPart == null) {
			return;
		}

		// Which Charset shall we use ?

		String charset;

		if (activeCharset.equals("auto")) {
			charset = bodyPart.getHeader().getContentParameter("charset");

			((CharsetOwnerInterface) getFrameController())
				.getCharsetManager()
				.displayCharset(charset);

		} else {
			charset = activeCharset;
		}

		Decoder decoder =
			CoderRouter.getDecoder(
				bodyPart.getHeader().contentTransferEncoding);

		// Shall we use the HTML-Viewer?

		boolean htmlViewer =
			bodyPart.getHeader().contentSubtype.equalsIgnoreCase("html");

		String decodedBody = null;

		// Decode the Text using the specified Charset
		try {
			decodedBody = decoder.decode(bodyPart.getBody(), charset);
		} catch (UnsupportedEncodingException ex) {
			// If Charset not supported fall back to standard Charset
			ColumbaLogger.log.info(
				"charset "
					+ charset
					+ " isn't supported, falling back to default...");

			try {
				decodedBody = decoder.decode(bodyPart.getBody(), null);
			} catch (UnsupportedEncodingException never) {
				never.printStackTrace();
			}
		}

		boolean hasAttachments = false;

		if ((mimePartTree.count() > 1)
			|| (!mimePartTree.get(0).getHeader().contentType.equals("text")))
			hasAttachments = true;

		attachmentController.setMimePartTree(mimePartTree);

		getView().setDoc(header, decodedBody, htmlViewer, hasAttachments);

		getView().getVerticalScrollBar().setValue(0);

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
		
		if (!SwingUtilities.isLeftMouseButton(event))
			return;
		
		URL url = extractURL(event);
		if (url == null)
			return;
		URLController c = new URLController();
		if (url.getProtocol().equalsIgnoreCase("mailto"))
			c.compose(url.getFile());
		else
			c.open(url);
			
	}

	

	protected URL extractURL(MouseEvent event) {
		JEditorPane pane = (JEditorPane) event.getSource();
		HTMLDocument doc = (HTMLDocument) pane.getDocument();

		Element e = doc.getCharacterElement(pane.viewToModel(event.getPoint()));
		AttributeSet a = e.getAttributes();
		AttributeSet anchor = (AttributeSet) a.getAttribute(HTML.Tag.A);
		
		if (anchor == null)
			return null;

		URL url = null;
		try {
			url = new URL((String) anchor.getAttribute(HTML.Attribute.HREF));
		} catch (MalformedURLException mue) {
			return null;
		}
		return url;
	}

	protected void processPopup(MouseEvent event) {
		URL url = extractURL(event);
		if (url == null)
		{
			// no URL, this means opening the default context menu
			// with actions like reply/forward/delete/etc.
			
			// TODO: open table-view contextmenu here
			// -> problem: actions listen for table-selection events
			// ->          not message selection
			
			/*
			getPopupMenu().show((JEditorPane)event.getSource(), event.getX(), event.getY());
			*/
			return;
		}
			

		// open context-menu with open/open with actions
		URLController c = new URLController();
		JPopupMenu menu = c.createMenu(url);
		menu.show(getView(), event.getX(), event.getY());
	}

	/********************* context menu *******************************************/

	/**
	 * Returns the mailFrameController.
	 * @return MailFrameController
	 */
	public AbstractFrameController getFrameController() {
		return abstractFrameController;
	}

	/* (non-Javadoc)
	 * @see org.columba.core.util.CharsetListener#charsetChanged(org.columba.core.util.CharsetEvent)
	 */
	public void charsetChanged(CharsetEvent e) {
		activeCharset = e.getValue();

		MainInterface.processor.addOp(
			new ViewMessageCommand(
				getFrameController(),
				((AbstractMailFrameController) getFrameController())
					.getTableSelection()));
	}

}
