/*
 * Created on Jun 10, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.frame;

import java.awt.Rectangle;

import org.columba.core.config.ViewItem;
import org.columba.core.gui.ClipboardManager;
import org.columba.core.gui.frame.AbstractFrameView;
import org.columba.core.gui.util.DialogStore;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.Folder;
import org.columba.mail.folder.command.CopyMessageCommand;
import org.columba.mail.folder.command.MoveMessageCommand;
import org.columba.mail.gui.attachment.AttachmentSelectionHandler;
import org.columba.mail.gui.composer.HeaderController;
import org.columba.mail.gui.infopanel.FolderInfoPanel;
import org.columba.mail.gui.table.FilterToolbar;
import org.columba.mail.gui.table.TableChangedEvent;
import org.columba.mail.gui.table.TableController;
import org.columba.mail.gui.table.TableView;
import org.columba.mail.gui.table.selection.TableSelectionHandler;
import org.columba.mail.gui.tree.TreeController;
import org.columba.mail.gui.tree.TreeView;
import org.columba.mail.gui.tree.selection.TreeSelectionHandler;
import org.columba.mail.message.HeaderList;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ThreePaneMailFrameController
	extends AbstractMailFrameController
	implements TableOwnerInterface {

	public TreeController treeController;
	public TableController tableController;

	public HeaderController headerController;
	public FilterToolbar filterToolbar;

	public FolderInfoPanel folderInfoPanel;

	/**
	 * @param viewItem
	 */
	public ThreePaneMailFrameController(ViewItem viewItem) {
		super("ThreePaneMail", viewItem);

		TableUpdater.add(this);

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

	public void close() {

		tableController.saveColumnConfig();

		super.close();

	}

	protected void init() {
		super.init();

		treeController = new TreeController(this, MainInterface.treeModel);

		tableController = new TableController(this);

		folderInfoPanel = new FolderInfoPanel();
		//treeController.getTreeSelectionManager().addFolderSelectionListener(folderInfoPanel);

		filterToolbar = new FilterToolbar(tableController);

		new DialogStore((MailFrameView) view);

		getSelectionManager().addSelectionHandler(
			new TableSelectionHandler(tableController.getView()));
			
		getSelectionManager().addSelectionHandler(
			new TreeSelectionHandler(treeController.getView()));
		getSelectionManager().addSelectionHandler(
			new AttachmentSelectionHandler(attachmentController.getView()));
			
		tableController.createPopupMenu();
		treeController.createPopupMenu();
		attachmentController.createPopupMenu();
		
	}

	public void saveAndClose() {

		tableController.saveColumnConfig();
		super.saveAndClose();
	}

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

	/* (non-Javadoc)
	 * @see org.columba.mail.gui.frame.AbstractMailFrameController#hasTable()
	 */
	public boolean hasTable() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.gui.frame.ViewHeaderListInterface#showHeaderList(org.columba.mail.folder.Folder, org.columba.mail.message.HeaderList)
	 */
	public void showHeaderList(Folder folder, HeaderList headerList)
		throws Exception {
		tableController.getHeaderTableModel().setHeaderList(headerList);

		boolean enableThreadedView =
			folder.getFolderItem().getBoolean(
				"property",
				"enable_threaded_view",
				false);

		tableController.getView().enableThreadedView(enableThreadedView);

		tableController.getView().getTableModelThreadedView().toggleView(
			enableThreadedView);

		TableChangedEvent ev =
			new TableChangedEvent(TableChangedEvent.UPDATE, folder);

		TableUpdater.tableChanged(ev);

		boolean ascending = tableController.isAscending();

		tableController.getView().clearSelection();
		tableController.getView().scrollRectToVisible(
			new Rectangle(0, 0, 0, 0));

	}

	/* (non-Javadoc)
	 * @see org.columba.mail.gui.frame.ViewHeaderListInterface#getTableController()
	 */
	public TableController getTableController() {
		return tableController;
	}

}
