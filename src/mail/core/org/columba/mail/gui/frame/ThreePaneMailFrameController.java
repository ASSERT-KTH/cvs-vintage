/*
 * Created on Jun 10, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.frame;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.columba.core.config.ViewItem;
import org.columba.core.gui.frame.AbstractFrameView;
import org.columba.core.gui.util.DialogStore;
import org.columba.core.main.MainInterface;
import org.columba.mail.folder.Folder;
import org.columba.mail.gui.attachment.AttachmentSelectionHandler;
import org.columba.mail.gui.composer.HeaderController;
import org.columba.mail.gui.infopanel.FolderInfoPanel;
import org.columba.mail.gui.table.FilterToolbar;
import org.columba.mail.gui.table.TableChangedEvent;
import org.columba.mail.gui.table.TableController;
import org.columba.mail.gui.table.action.CopyAction;
import org.columba.mail.gui.table.action.CutAction;
import org.columba.mail.gui.table.action.DeleteAction;
import org.columba.mail.gui.table.action.PasteAction;
import org.columba.mail.gui.table.selection.TableSelectionHandler;
import org.columba.mail.gui.tree.TreeController;
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

	protected void initActions() {
		tableController.getView().getInputMap().put(
			KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK),
			"COPY");
		tableController.getView().getActionMap().put(
			"COPY",
			new CopyAction(this));

		tableController.getView().getInputMap().put(
			KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK),
			"CUT");
		tableController.getView().getActionMap().put(
			"CUT",
			new CutAction(this));

		tableController.getView().getInputMap().put(
			KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK),
			"PASTE");
		tableController.getView().getActionMap().put(
			"PASTE",
			new PasteAction(this));

		tableController.getView().getInputMap().put(
			KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
			"DELETE");
		tableController.getView().getActionMap().put(
			"DELETE",
			new DeleteAction(this));

		treeController.getView().getInputMap().put(
			KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK),
			"PASTE");
		treeController.getView().getActionMap().put(
			"PASTE",
			new PasteAction(this));

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

		initActions();

	}

	public void saveAndClose() {

		tableController.saveColumnConfig();
		super.saveAndClose();
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

		// FIXME
		//boolean ascending = tableController.isAscending();

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
