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
package org.columba.mail.gui.frame;

import org.columba.core.config.ViewItem;
import org.columba.core.gui.frame.FrameController;
import org.columba.core.gui.frame.FrameView;
import org.columba.core.gui.frame.MultiViewFrameModel;
import org.columba.core.gui.selection.SelectionListener;
import org.columba.core.gui.toolbar.ToolBar;
import org.columba.core.gui.util.DialogStore;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.config.MailConfig;
import org.columba.mail.gui.action.GlobalActionCollection;
import org.columba.mail.gui.attachment.AttachmentController;
import org.columba.mail.gui.composer.HeaderController;
import org.columba.mail.gui.frame.action.FrameActionListener;
import org.columba.mail.gui.message.MessageController;
import org.columba.mail.gui.table.FilterToolbar;
import org.columba.mail.gui.table.TableController;
import org.columba.mail.gui.table.action.ViewMessageAction;
import org.columba.mail.gui.table.selection.TableSelectionHandler;
import org.columba.mail.gui.tree.TreeController;
import org.columba.mail.gui.tree.action.ViewHeaderListAction;
import org.columba.mail.gui.tree.selection.TreeSelectionHandler;
import org.columba.mail.gui.tree.util.FolderInfoPanel;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MailFrameController extends FrameController {

	//public SelectionManager selectionManager;

	public TreeController treeController;
	public TableController tableController;
	public MessageController messageController;
	public AttachmentController attachmentController;
	public HeaderController headerController;
	public FilterToolbar filterToolbar;

	public FolderInfoPanel folderInfoPanel;

	private FrameActionListener actionListener;
	private ToolBar toolBar;
	public GlobalActionCollection globalActionCollection;

	public MailFrameController(String id, MultiViewFrameModel model) {
		super(id, model);
	}

	public FolderCommandReference[] getTableSelection() {
		FolderCommandReference[] r =
			(FolderCommandReference[]) getSelectionManager().getSelection(
				"mail.table");

		return r;
	}

	public FolderCommandReference[] getTreeSelection() {
		FolderCommandReference[] r =
			(FolderCommandReference[]) getSelectionManager().getSelection(
				"mail.tree");

		return r;
	}

	public void registerTableSelectionListener(SelectionListener l) {
		getSelectionManager().registerSelectionListener("mail.table", l);
	}

	public void registerTreeSelectionListener(SelectionListener l) {
		getSelectionManager().registerSelectionListener("mail.tree", l);
	}

	public FrameView createView() {

		MailFrameView view = new MailFrameView(this);

		view.setFolderInfoPanel(folderInfoPanel);

		view.init(
			treeController.getView(),
			tableController.getView(),
			filterToolbar,
			messageController.getView(),
			statusBar);

		view.pack();

		return view;
	}

	public FrameView getView() {
		return view;
	}

	public FrameActionListener getActionListener() {
		return actionListener;
	}

	protected void changeToolbars() {
		ViewItem item = MailConfig.getMainFrameOptionsConfig().getViewItem();

		boolean folderInfo = item.getBoolean("toolbars", "show_folderinfo");
		boolean toolbar = item.getBoolean("toolbars", "show_main");

		if (toolbar == true) {

			((MailFrameView) getView()).hideToolbar(folderInfo);
			item.set("toolbars", "show_main", false);
		} else {

			((MailFrameView) getView()).showToolbar(folderInfo);
			item.set("toolbars", "show_main", true);
		}

		if (folderInfo == true) {

			((MailFrameView) getView()).hideFolderInfo(toolbar);
			item.set("toolbars", "show_folderinfo", false);
		} else {

			((MailFrameView) getView()).showFolderInfo(toolbar);
			item.set("toolbars", "show_folderinfo", true);
		}

	}

	public void close() {
		ColumbaLogger.log.info("closing MailFrameController");

		tableController.saveColumnConfig();
		super.close();

	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.FrameController#registerSelectionHandlers()
	 */
	protected void registerSelectionHandlers() {
	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.FrameController#initInternActions()
	 */
	protected void initInternActions() {
		new ViewHeaderListAction(this);
		new ViewMessageAction(this);
	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.FrameController#init()
	 */
	protected void init() {
		treeController = new TreeController(this, MainInterface.treeModel);
		// moved this into TreeController constructor, because
		// its needed to create the context-menu 
		/*
		selectionManager.addSelectionHandler(
			new TreeSelectionHandler(treeController.getView()));
		*/
		
		tableController = new TableController(this);
		// moved this into TableController constructor, because
	    // its needed to create the context-menu
	    /* 
		selectionManager.addSelectionHandler(
			new TableSelectionHandler(tableController.getView()));
		*/

		attachmentController = new AttachmentController(this);

		messageController = new MessageController(this);

		folderInfoPanel = new FolderInfoPanel();
		filterToolbar = new FilterToolbar(tableController);

		new DialogStore((MailFrameView) view);
	}

}
