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
package org.columba.mail.gui.attachment.selection;

import java.util.logging.Logger;

import org.columba.core.command.ICommandReference;
import org.columba.core.gui.selection.ISelectionListener;
import org.columba.core.gui.selection.SelectionChangedEvent;
import org.columba.core.gui.selection.SelectionHandler;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.folder.AbstractMessageFolder;
import org.columba.mail.folder.IMailFolder;
import org.columba.mail.gui.attachment.AttachmentController;
import org.columba.mail.gui.attachment.AttachmentView;
import org.columba.mail.gui.attachment.IAttachmentController;
import org.columba.mail.gui.frame.TableViewOwner;
import org.columba.mail.gui.table.TableController;
import org.columba.mail.gui.table.selection.TableSelectionChangedEvent;
import org.frapuccino.iconpanel.IconPanelSelectionListener;

public class AttachmentSelectionHandler extends SelectionHandler implements
		IconPanelSelectionListener, ISelectionListener {

	/** JDK 1.4+ logging framework logger, used for logging. */
	private static final Logger LOG = Logger
			.getLogger("org.columba.mail.gui.attachment");

	private AbstractMessageFolder folder;

	private Object messageUid;

	private AttachmentView view;

	private Integer[] address;

	private boolean useLocalSelection;

	private IAttachmentController controller;

	public AttachmentSelectionHandler(AttachmentController c) {
		super("mail.attachment");
		this.view = c.getView();

		TableController tableController = ((TableController)((TableViewOwner) c
				.getFrameController()).getTableController());

		view.addIconPanelSelectionListener(this);

		useLocalSelection = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.core.gui.selection.SelectionHandler#getSelection()
	 */
	public ICommandReference getSelection() {
		return new MailFolderCommandReference(folder, new Object[] { messageUid },
				address);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.core.gui.selection.SelectionHandler#setSelection(org.columba.core.command.DefaultCommandReference[])
	 */
	public void setSelection(ICommandReference selection) {
		LOG.warning("Not yet implemented!");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.mail.gui.attachment.util.IconPanelSelectionListener#selectionChanged(int[])
	 */
	public void selectionChanged(int[] newselection) {
		useLocalSelection = false;

		if (newselection.length > 0) {
			address = view.getSelectedMimePart().getAddress();
		} else {
			address = null;
		}

		fireSelectionChanged(new AttachmentSelectionChangedEvent(folder,
				messageUid, address));
	}

	public void setLocalReference(MailFolderCommandReference r) {
		// set selection
		this.folder = (AbstractMessageFolder) r.getSourceFolder();
		this.messageUid = r.getUids()[0];

		useLocalSelection = true;
	}

	/**
	 * 
	 * @see org.columba.core.gui.util.ISelectionListener#selectionChanged(org.columba.core.gui.util.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent e) {
		IMailFolder folder = ((TableSelectionChangedEvent) e).getFolder();
		Object[] uids = ((TableSelectionChangedEvent) e).getUids();

		if (uids.length != 0) {
			this.folder = (AbstractMessageFolder) folder;
			this.messageUid = uids[0];
		} else {
			this.folder = null;
			this.messageUid = null;
		}
	}
}