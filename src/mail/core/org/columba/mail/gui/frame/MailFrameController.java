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

import java.util.Enumeration;
import java.util.Vector;

import org.columba.core.config.ViewItem;
import org.columba.core.gui.ClipboardManager;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.gui.frame.AbstractFrameView;
import org.columba.core.gui.selection.SelectionListener;
import org.columba.core.gui.toolbar.ToolBar;
import org.columba.core.gui.util.DialogStore;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.main.MainInterface;
import org.columba.core.util.CharsetManager;
import org.columba.core.xml.XmlElement;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.command.CopyMessageCommand;
import org.columba.mail.folder.command.MoveMessageCommand;
import org.columba.mail.gui.action.GlobalActionCollection;
import org.columba.mail.gui.attachment.AttachmentController;
import org.columba.mail.gui.composer.HeaderController;
import org.columba.mail.gui.frame.action.FrameActionListener;
import org.columba.mail.gui.infopanel.FolderInfoPanel;
import org.columba.mail.gui.message.MessageController;
import org.columba.mail.gui.table.FilterToolbar;
import org.columba.mail.gui.table.TableChangedEvent;
import org.columba.mail.gui.table.TableController;
import org.columba.mail.gui.table.TableView;
import org.columba.mail.gui.table.action.ViewMessageAction;
import org.columba.mail.gui.tree.TreeController;
import org.columba.mail.gui.tree.TreeView;
import org.columba.mail.gui.tree.action.ViewHeaderListAction;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MailFrameController extends AbstractFrameController {

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

	protected static Vector list = new Vector();

	public MailFrameController(ViewItem viewItem) {
		this("Mail", viewItem);
	}

	public MailFrameController(String id, ViewItem viewItem) {
		super(id, viewItem);

		list.add(this);
	}

	public static void tableChanged(TableChangedEvent ev) throws Exception {
		for (Enumeration e = list.elements(); e.hasMoreElements();) {

			MailFrameController frame = (MailFrameController) e.nextElement();

			frame.tableController.tableChanged(ev);
		}

	}

	public FolderCommandReference[] getTableSelection() {
		FolderCommandReference[] r =
			(FolderCommandReference[]) getSelectionManager().getSelection(
				"mail.table");

		return r;
	}

	public void setTableSelection(FolderCommandReference[] r) {
		getSelectionManager().setSelection("mail.table", r);
	}

	public FolderCommandReference[] getTreeSelection() {
		FolderCommandReference[] r =
			(FolderCommandReference[]) getSelectionManager().getSelection(
				"mail.tree");

		return r;
	}

	public void setTreeSelection(FolderCommandReference[] r) {
		getSelectionManager().setSelection("mail.tree", r);
	}

	public void registerTableSelectionListener(SelectionListener l) {
		getSelectionManager().registerSelectionListener("mail.table", l);
	}

	public void registerTreeSelectionListener(SelectionListener l) {
		getSelectionManager().registerSelectionListener("mail.tree", l);
	}

	public AbstractFrameView createView() {

		MailFrameView view = new MailFrameView(this);

		view.setFolderInfoPanel(folderInfoPanel);

		view.init(
			treeController.getView(),
			tableController.getView(),
			filterToolbar,
			messageController.getView(),
			statusBar);

		//view.pack();

		return view;
	}

	public AbstractFrameView getView() {
		return view;
	}

	public FrameActionListener getActionListener() {
		return actionListener;
	}

	/*
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
	*/
	public void close() {

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
		
		setCharsetManager( new CharsetManager(null) );
		
		treeController = new TreeController(this, MainInterface.treeModel);

		tableController = new TableController(this);

		attachmentController = new AttachmentController(this);

		messageController = new MessageController(this);

		folderInfoPanel = new FolderInfoPanel();
		//treeController.getTreeSelectionManager().addFolderSelectionListener(folderInfoPanel);
		
		filterToolbar = new FilterToolbar(tableController);

		new DialogStore((MailFrameView) view);
	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.frame.AbstractFrameController#createDefaultConfiguration(java.lang.String)
	 */
	protected XmlElement createDefaultConfiguration(String id) {

		XmlElement child = super.createDefaultConfiguration(id);

		XmlElement toolbars = new XmlElement("toolbars");
		child.addElement(toolbars);
		XmlElement splitpanes = new XmlElement("splitpanes");
		splitpanes.addAttribute("main", "200");
		splitpanes.addAttribute("header", "200");
		splitpanes.addAttribute("attachment", "100");
		child.addElement(splitpanes);

		return child;
	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.frame.AbstractFrameController#saveAndClose()
	 */
	public void saveAndClose() {

		tableController.saveColumnConfig();
		super.saveAndClose();
	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.frame.AbstractFrameController#executeCopyAction()
	 */
	public void executeCopyAction() {
		ColumbaLogger.log.debug("copy action");

		TableView table = tableController.getView();

		// add current selection to clipboard

		// copy action
		MainInterface.clipboardManager.setOperation(
			ClipboardManager.COPY_ACTION);

		MainInterface.clipboardManager.setMessageSelection(getTableSelection());

	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.frame.AbstractFrameController#executeCutAction()
	 */
	public void executeCutAction() {
		ColumbaLogger.log.debug("cut action");

		TableView table = tableController.getView();

		// add current selection to clipboard

		// cut action
		MainInterface.clipboardManager.setOperation(
			ClipboardManager.CUT_ACTION);

		MainInterface.clipboardManager.setMessageSelection(getTableSelection());

	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.frame.AbstractFrameController#executePasteAction()
	 */
	public void executePasteAction() {
		ColumbaLogger.log.debug("paste action");

		TableView table = tableController.getView();
		TreeView tree = treeController.getView();

		//if ( (table.hasFocus()) || (tree.hasFocus()) ) {

		FolderCommandReference[] ref = new FolderCommandReference[2];

		FolderCommandReference[] source =
			MainInterface.clipboardManager.getMessageSelection();
		if (source == null)
			return;

		ref[0] = source[0];
		
		FolderCommandReference[] dest = getTableSelection();
		ref[1] = dest[0];

		FolderCommand c = null;

		if (MainInterface.clipboardManager.isCutAction())
			c = new MoveMessageCommand(ref);
		else
			c = new CopyMessageCommand(ref);

		MainInterface.clipboardManager.clearMessageSelection();

		MainInterface.processor.addOp(c);

		//}

	}

}
