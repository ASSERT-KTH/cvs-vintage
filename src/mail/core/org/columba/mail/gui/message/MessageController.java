// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

package org.columba.mail.gui.message;

import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPopupMenu;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;

import org.columba.core.config.Config;
import org.columba.mail.coder.CoderRouter;
import org.columba.mail.coder.Decoder;
import org.columba.mail.folder.Folder;
import org.columba.mail.gui.frame.MailFrameController;
import org.columba.mail.gui.message.action.MessageActionListener;
import org.columba.mail.gui.message.action.MessageFocusListener;
import org.columba.mail.gui.message.action.MessagePopupListener;
import org.columba.mail.gui.message.menu.MessageMenu;
import org.columba.mail.gui.table.MessageSelectionListener;
import org.columba.mail.gui.util.URLController;
import org.columba.mail.message.HeaderInterface;
import org.columba.mail.message.MimePart;

/**
 * this class shows the messagebody
 */

public class MessageController
	implements
		MessageSelectionListener,
		HyperlinkListener,
		MouseListener //implements CharsetListener
{

	private Folder folder;
	private Object uid;

	private MessageMenu menu;

	private MessageFocusListener focusListener;

	private MessagePopupListener popupListener;

	private JButton button;

	private String activeCharset;

	private MessageView view;
	private MessageActionListener actionListener;

	protected MailFrameController mailFrameController;
	
	protected URL url;

	//private MessageSelectionManager messageSelectionManager;

	public MessageController(MailFrameController mailFrameController) {

		this.mailFrameController = mailFrameController;

		activeCharset = "auto";

		view = new MessageView(this);
		view.addHyperlinkListener(this);
		view.addMouseListener(this);

		//messageSelectionManager = new MessageSelectionManager();

		actionListener = new MessageActionListener(this);

		/*
		String[] keys = new String[4];
		keys[0] = new String("Subject");
		keys[1] = new String("From");
		keys[2] = new String("Date");
		keys[3] = new String("To");
		*/

		Font mainFont =
			new Font(
				Config.getOptionsConfig().getThemeItem().getMainFontName(),
				Font.PLAIN,
				Config.getOptionsConfig().getThemeItem().getMainFontSize());

		menu = new MessageMenu(this);

	}

	public void messageSelectionChanged(Object[] newUidList) {
		//System.out.println("received new message-selection changed event");

		/*
		FolderCommandReference[] reference = (FolderCommandReference[]) MainInterface.frameController.tableController.getTableSelectionManager().getSelection();
		
		FolderTreeNode treeNode = reference[0].getFolder();
		Object[] uids = reference[0].getUids();
		
		// this is no message-viewing action,
		// but a selection of multiple messages
		if ( uids.length > 1 ) return;
		
		MainInterface.frameController.attachmentController.getAttachmentSelectionManager().setFolder(treeNode);
		MainInterface.frameController.attachmentController.getAttachmentSelectionManager().setUids(uids);
		
		MainInterface.processor.addOp(
			new ViewMessageCommand(
				mailFrameController,
				reference));
		*/

		/*
		MainInterface.crossbar.operate(
				new GuiOperation(Operation.MESSAGEBODY, 4, (Folder) selectionManager.getFolder(), newUidList[0]));
				*/
	}

	public MessageActionListener getActionListener() {
		return actionListener;
	}

	public MessageView getView() {

		//new MessageActionListener( view );

		return view;
	}

	public JPopupMenu getPopupMenu() {
		return menu.getPopupMenu();
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

	public void showMessage(HeaderInterface header, MimePart bodyPart)
		throws Exception {

		boolean htmlViewer = false;

		// Which Charset shall we use ?

		String charset;

		if (activeCharset.equals("auto"))
			charset = bodyPart.getHeader().getContentParameter("charset");
		else
			charset = activeCharset;

		Decoder decoder =
			CoderRouter.getDecoder(
				bodyPart.getHeader().contentTransferEncoding);

		// Shall we use the HTML-Viewer?

		htmlViewer =
			bodyPart.getHeader().contentSubtype.equalsIgnoreCase("html");

		// Update the MessageHeaderPane
		/*
		messageHeader.setValues(message);
		*/

		String decodedBody = null;

		// Decode the Text using the specified Charset				
		try {
			decodedBody = decoder.decode(bodyPart.getBody(), charset);
		} catch (UnsupportedEncodingException ex) {
			// If Charset not supported fall back to standard Charset

			try {
				decodedBody = decoder.decode(bodyPart.getBody(), null);
			} catch (UnsupportedEncodingException never) {

			}
		}

		getView().setDoc(header, decodedBody, htmlViewer);

		getView().getVerticalScrollBar().setValue(0);

	}

	public void showMessageSource(String rawText) throws Exception {
		getView().setDoc(null, rawText, false);

		getView().getVerticalScrollBar().setValue(0);

	}
	/*
	 * 
	public MessageActionListener getActionListener()
	{
		return actionListener;
	}
	
	public MessageFocusListener getFocusListener()
	{
		return focusListener;
	}
	
	public String getAddress()
	{
		HyperlinkTextViewer viewer =
			(HyperlinkTextViewer) view.getViewer(MessageView.ADVANCED);
	
		if (viewer != null)
			return viewer.getAddress();
		else
			return new String();
	}
	
	public String getLink()
	{
		HyperlinkTextViewer viewer =
			(HyperlinkTextViewer) view.getViewer(MessageView.ADVANCED);
	
		if (viewer != null)
			return viewer.getLink();
		else
			return new String();
	}
	*/

	/*	
	public void charsetChanged( CharsetEvent e ) {
		activeCharset = e.getValue();				
	}
	*/

	public void hyperlinkUpdate(HyperlinkEvent e) {
		/*
		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			JEditorPane pane = (JEditorPane) e.getSource();
			if (e instanceof HTMLFrameHyperlinkEvent) {
				HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent) e;
				HTMLDocument doc = (HTMLDocument) pane.getDocument();
				doc.processHTMLFrameHyperlinkEvent(evt);
			} else {
				URL url = e.getURL();
				if (url != null) {
		
					if (url.getProtocol().equalsIgnoreCase("mailto")) {
						// found email address
						URLController c = new URLController();
						JPopupMenu menu = c.createContactMenu(url.getFile());
						menu.setVisible(true);
		
					} else {
		
						URLController c = new URLController();
						c.open(url);
					}
				}
			}
		}
		*/

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
		if (event.getButton() != MouseEvent.BUTTON1)
			return;

		String s = extractURL(event);

		try {
			url = new URL(s);
			if (url.getProtocol().equalsIgnoreCase("mailto")) {

				URLController c = new URLController();
				c.compose(url.getFile());
			} else {
				URLController c = new URLController();
				c.open(url);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/*
        private String getMapHREF(JEditorPane html, HTMLDocument hdoc,
                                  Element elem, AttributeSet attr, int offset,
                                  int x, int y) {
            Object useMap = attr.getAttribute(HTML.Attribute.USEMAP);
            if (useMap != null && (useMap instanceof String)) {
                Map m = hdoc.getMap((String)useMap);
                if (m != null && offset < hdoc.getLength()) {
                    Rectangle bounds;
                    TextUI ui = html.getUI();
                    try {
                        Shape lBounds = ui.modelToView(html, offset,
                                                   Position.Bias.Forward);
                        Shape rBounds = ui.modelToView(html, offset + 1,
                                                   Position.Bias.Backward);
                        bounds = lBounds.getBounds();
                        bounds.add((rBounds instanceof Rectangle) ?
                                    (Rectangle)rBounds : rBounds.getBounds());
                    } catch (BadLocationException ble) {
                        bounds = null;
                    }
                    if (bounds != null) {
                        AttributeSet area = m.getArea(x - bounds.x,
                                                      y - bounds.y,
                                                      bounds.width,
                                                      bounds.height);
                        if (area != null) {
                            return (String)area.getAttribute(HTML.Attribute.
                                                             HREF);
                        }
                    }
                }
            }
            return null;
        }
    */

	protected String extractURL(MouseEvent event) {
		JEditorPane pane = (JEditorPane) event.getSource();
		HTMLDocument doc = (HTMLDocument) pane.getDocument();

		Element e = doc.getCharacterElement(pane.viewToModel(event.getPoint()));
		AttributeSet a = e.getAttributes();
		AttributeSet anchor = (AttributeSet) a.getAttribute(HTML.Tag.A);

		String s = null;
		/*
		if ( anchor == null )
			s = getMapHREF(pane, doc, e, a, pane.viewToModel(event.getPoint()), event.getX(), event.getY() );
		else	
		*/
			
		s = (String) anchor.getAttribute(HTML.Attribute.HREF);
		
		return s;
	}

	protected void processPopup(MouseEvent event) {
		String s = extractURL(event);

		try {
			URL url = new URL(s);
			URLController c = new URLController();
			JPopupMenu menu = c.createMenu(url);
			menu.show(getView(),event.getX(), event.getY() );

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	/********************* context menu *******************************************/

	/**
	 * Returns the mailFrameController.
	 * @return MailFrameController
	 */
	public MailFrameController getMailFrameController() {
		return mailFrameController;
	}

}