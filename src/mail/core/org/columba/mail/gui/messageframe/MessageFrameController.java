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

package org.columba.mail.gui.messageframe;

import java.awt.BorderLayout;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.columba.core.config.ViewItem;
import org.columba.core.gui.frame.ContentPane;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.MessageFolder;
import org.columba.mail.gui.attachment.selection.AttachmentSelectionHandler;
import org.columba.mail.gui.frame.AbstractMailFrameController;
import org.columba.mail.gui.frame.TableViewOwner;
import org.columba.mail.gui.frame.ThreePaneMailFrameController;
import org.columba.mail.gui.table.TableController;
import org.columba.mail.main.MailInterface;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.util.MailResourceLoader;

/**
 * Mail frame controller which contains a message viewer only.
 * <p>
 * Note that this frame depends on its parent frame controller for viewing
 * messages.
 * 
 * @see org.columba.mail.gui.action.NextMessageAction
 * @see org.columba.mail.gui.action.PreviousMessageAction
 * @see org.columba.mail.gui.action.NextUnreadMessageAction
 * @see org.columba.mail.gui.action.PreviousMessageAction
 * 
 * @author fdietz
 */
public class MessageFrameController extends AbstractMailFrameController
		implements TableViewOwner, ContentPane {

	private static final Logger LOG = Logger.getLogger("org.columba.mail.gui.messageframe");
	
	private static final int MAX_SUBJECT_LENGTH = 50;
	
	FolderCommandReference treeReference;

	FolderCommandReference tableReference;

	FixedTableSelectionHandler tableSelectionHandler;

	private ThreePaneMailFrameController parentController;

	/**
	 * @param viewItem
	 */
	public MessageFrameController() {
		super(new ViewItem(MailInterface.config.get("options").getElement(
				"/options/gui/messageframe/view")));

		tableSelectionHandler = new FixedTableSelectionHandler(tableReference);
		getSelectionManager().addSelectionHandler(tableSelectionHandler);

		AttachmentSelectionHandler attachmentHandler = new AttachmentSelectionHandler(
				attachmentController);
		getSelectionManager().addSelectionHandler(attachmentHandler);
		// attachment viewer registers interest in table selection events
		tableSelectionHandler.addSelectionListener(attachmentHandler);

		//getContainer().setContentPane(this);
	}

	/**
	 * @param parent
	 *            parent frame controller
	 */
	public MessageFrameController(ThreePaneMailFrameController parent) {
		this();

		this.parentController = parent;

	}

	/**
	 * 
	 * @see org.columba.mail.gui.frame.MailFrameInterface#getTableSelection()
	 */
	public FolderCommandReference getTableSelection() {
		return tableReference;
	}

	/**
	 * 
	 * @see org.columba.mail.gui.frame.MailFrameInterface#getTreeSelection()
	 */
	public FolderCommandReference getTreeSelection() {
		return treeReference;
	}

	/**
	 * @param references
	 */
	public void setTreeSelection(FolderCommandReference references) {
		treeReference = references;
	}

	/**
	 * @param references
	 */
	public void setTableSelection(FolderCommandReference references) {
		tableReference = references;
		
		try {
			
			// Get the subject from the cached Header
			MessageFolder folder = (MessageFolder) references.getFolder();
			ColumbaHeader header = folder.getHeaderList().get(references.getUids()[0]);
			String subject = (String)header.get("columba.subject");
			
			/*
			// Trim it to the max length of MAX_SUBJECT_LENGTH
			subject = subject.trim();
			
			if( subject.length() > MAX_SUBJECT_LENGTH ) {
				subject = subject.substring(0,MAX_SUBJECT_LENGTH-3) + "...";
			}
			*/
			
			getContainer().getFrame().setTitle(subject);
		} catch (Exception e) {
			LOG.warning(e.toString());
		}
		
		
		tableSelectionHandler.setSelection(tableReference);
	}

	/**
	 * @see org.columba.mail.gui.frame.TableViewOwner#getTableController()
	 */
	public TableController getTableController() {
		if (parentController == null)
			return null;

		// pass it along to parent frame
		return parentController.getTableController();
	}

	/**
	 * @see org.columba.core.gui.frame.ContentPane#getComponent()
	 */
	public JComponent getComponent() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		panel.add(messageController.getView(), BorderLayout.CENTER);

		ViewItem viewItem = getViewItem();

		getContainer().extendMenuFromFile(this,
				"org/columba/mail/action/messageframe_menu.xml");

		getContainer().extendToolbar(
				this,
				MailInterface.config.get("messageframe_toolbar").getElement(
						"toolbar"));
		
		attachmentController.createPopupMenu();
		
		/*
		 * if (viewItem.getBoolean("toolbars", "show_folderinfo") == true) {
		 * addToolBar(folderInfoPanel); }
		 */

		return panel;
	}

	/**
	 * @see org.columba.core.gui.frame.FrameMediator#getString(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public String getString(String sPath, String sName, String sID) {
		return MailResourceLoader.getString(sPath, sName, sID);
	}
	
	/**
	 * @see org.columba.core.gui.frame.FrameMediator#getContentPane()
	 */
	public ContentPane getContentPane() {
		return this;
	}
}