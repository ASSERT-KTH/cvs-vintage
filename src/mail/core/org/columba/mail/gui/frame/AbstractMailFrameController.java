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
package org.columba.mail.gui.frame;

import java.nio.charset.Charset;

import javax.swing.event.EventListenerList;

import org.columba.core.charset.CharsetEvent;
import org.columba.core.charset.CharsetListener;
import org.columba.core.charset.CharsetOwnerInterface;
import org.columba.core.config.ViewItem;
import org.columba.core.gui.frame.Container;
import org.columba.core.gui.frame.DefaultFrameController;
import org.columba.core.gui.selection.SelectionListener;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folderoptions.FolderOptionsController;
import org.columba.mail.gui.attachment.AttachmentController;
import org.columba.mail.gui.message.MessageController;

/**
 * @author fdietz
 *  
 */
public class AbstractMailFrameController extends DefaultFrameController
		implements MailFrameMediator, MessageViewOwner, AttachmentViewOwner,
		CharsetOwnerInterface {

	public MessageController messageController;

	public AttachmentController attachmentController;

	private FolderOptionsController folderOptionsController;

	protected EventListenerList listenerList = new EventListenerList();

	// needs to be private so that subclasses won't forget calling
	// fireCharsetChanged
	private Charset charset;

	/**
	 * @param id
	 * @param viewItem
	 */
	public AbstractMailFrameController(Container c, ViewItem viewItem) {
		super(c, viewItem);

		attachmentController = new AttachmentController(this);

		messageController = new MessageController(this, attachmentController);

		folderOptionsController = new FolderOptionsController(this);

	}

	/*
	 * protected XmlElement createDefaultConfiguration(String id) { XmlElement
	 * child = super.createDefaultConfiguration(id);
	 * 
	 * XmlElement splitpanes = new XmlElement("splitpanes");
	 * splitpanes.addAttribute("main", "200"); splitpanes.addAttribute("header",
	 * "200"); splitpanes.addAttribute("attachment", "100");
	 * child.addElement(splitpanes);
	 * 
	 * return child; }
	 */

	public FolderCommandReference getTableSelection() {
		FolderCommandReference r = (FolderCommandReference) getSelectionManager()
				.getSelection("mail.table");

		return r;
	}

	public void setTableSelection(FolderCommandReference r) {
		getSelectionManager().setSelection("mail.table", r);
	}

	public FolderCommandReference getTreeSelection() {
		FolderCommandReference r = (FolderCommandReference) getSelectionManager()
				.getSelection("mail.tree");

		return r;
	}

	public void setTreeSelection(FolderCommandReference r) {
		getSelectionManager().setSelection("mail.tree", r);
	}

	public FolderCommandReference getAttachmentSelection() {
		FolderCommandReference r = (FolderCommandReference) getSelectionManager()
				.getSelection("mail.attachment");

		return r;
	}

	public void setAttachmentSelection(FolderCommandReference r) {
		getSelectionManager().setSelection("mail.attachment", r);
	}

	public void registerTableSelectionListener(SelectionListener l) {
		getSelectionManager().registerSelectionListener("mail.table", l);
	}

	public void registerTreeSelectionListener(SelectionListener l) {
		getSelectionManager().registerSelectionListener("mail.tree", l);
	}

	public void registerAttachmentSelectionListener(SelectionListener l) {
		getSelectionManager().registerSelectionListener("mail.attachment", l);
	}

	/**
	 * @see org.columba.mail.gui.frame.MailFrameMediator#getFolderOptionsController()
	 */
	public FolderOptionsController getFolderOptionsController() {
		return folderOptionsController;
	}

	/**
	 * @see org.columba.mail.gui.frame.MessageViewOwner#getMessageController()
	 */
	public MessageController getMessageController() {
		return messageController;
	}

	/**
	 * @see org.columba.mail.gui.frame.AttachmentViewOwner#getAttachmentController()
	 */
	public AttachmentController getAttachmentController() {
		return attachmentController;
	}

	public void setCharset(Charset charset) {
		this.charset = charset;
		fireCharsetChanged(new CharsetEvent(this, charset));
	}

	public void removeCharsetListener(CharsetListener l) {
		listenerList.remove(CharsetListener.class, l);
	}

	public Charset getCharset() {
		return charset;
	}

	public void addCharsetListener(CharsetListener l) {
		listenerList.add(CharsetListener.class, l);
	}

	protected void fireCharsetChanged(CharsetEvent e) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == CharsetListener.class) {
				((CharsetListener) listeners[i + 1]).charsetChanged(e);
			}
		}
	}

}