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
package org.columba.mail.gui.table;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.TreePath;

import org.columba.core.gui.focus.FocusOwner;
import org.columba.core.gui.util.treetable.Tree;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.Folder;
import org.columba.mail.folder.FolderTreeNode;
import org.columba.mail.folderoptions.FolderOptionsController;
import org.columba.mail.gui.frame.AbstractMailFrameController;
import org.columba.mail.gui.frame.ThreePaneMailFrameController;
import org.columba.mail.gui.table.action.CopyAction;
import org.columba.mail.gui.table.action.CutAction;
import org.columba.mail.gui.table.action.DeleteAction;
import org.columba.mail.gui.table.action.PasteAction;
import org.columba.mail.gui.table.dnd.HeaderTableDnd;
import org.columba.mail.gui.table.dnd.MessageTransferHandler;
import org.columba.mail.gui.table.model.HeaderTableModel;
import org.columba.mail.gui.table.model.MessageNode;
import org.columba.mail.gui.table.model.TableModelChangedEvent;
import org.columba.mail.gui.table.model.TableModelFilter;
import org.columba.mail.gui.table.model.TableModelSorter;
import org.columba.mail.gui.table.model.TableModelThreadedView;
import org.columba.mail.gui.table.model.TableModelUpdateManager;
import org.columba.mail.gui.table.util.MarkAsReadTimer;
import org.columba.mail.message.HeaderList;

/**
 * Shows the message list. By default, this is the read/unread state
 * if a message, Subject:, Date:, From: and Size headerfields.
 * <p>
 * Folder-specific configuration options are handled by
 * {@link FolderOptionsController} and can be configured
 * by the user in the Folder Options Dialog.
 *
 * @author fdietz
 */
public class TableController implements FocusOwner, ListSelectionListener {

    /**
     * table model
     */
    private HeaderTableModel headerTableModel;

    /**
     * mouse listener responsible for sorting actions fired
     * by the user selecting the column headers of the table
     */
    private HeaderTableMouseListener headerTableMouseListener;

    /**
     * Drag'n'drop handling of messages
     */
    private HeaderTableDnd headerTableDnd;

    /**
     * filter action which should be accessible from the menu 
     * only
     */
    private FilterActionListener filterActionListener;

    /**
     * table view
     */
    protected TableView view;

    /**
     * reference to mail framemediator
     */
    protected AbstractMailFrameController mailFrameController;

    /**
     * timer which marks a message as read after a certain amount
     * of time.
     */
    protected MarkAsReadTimer markAsReadTimer;

    /**
     * table view context menu
     */
    protected TableMenu menu;

    /**
     * filter model
     */
    protected TableModelFilter tableModelFilteredView;

    /**
     * sorting model
     */
    protected TableModelSorter tableModelSorter;

    /**
     * threaded-view model
     */
    protected TableModelThreadedView tableModelThreadedView;

    /**
     * update manager should handle all update requests
     * <p>
     * Don't update the models directly.
     */
    protected TableModelUpdateManager updateManager;

    /**
     * previously selected rows
     */
    protected int[] previouslySelectedRows;

    /**
     * previously selected folder
     */
    private Folder previouslySelectedFolder;

    /**
     * Constructor
     * 
     * @param mailFrameController      mail framemediator
     */
    public TableController(AbstractMailFrameController mailFrameController) {
        this.mailFrameController = mailFrameController;

        // init table model
        headerTableModel = new HeaderTableModel();

        // init filter model
        tableModelFilteredView = new TableModelFilter(headerTableModel);

        // init threaded-view model
        tableModelThreadedView =
            new TableModelThreadedView(tableModelFilteredView);

        // init sorting model
        tableModelSorter = new TableModelSorter(tableModelThreadedView);

        // now, init update manager
        // -> sorting is applied at the end after all other
        // -> operations like filtering
        updateManager = new TableModelUpdateManager(tableModelSorter);

        // init view
        view = new TableView(headerTableModel);

        // pass tree to model, used by the threaded-view
        headerTableModel.setTree((Tree) view.getTree());

        // init mouse listener for the column header
        headerTableMouseListener = new HeaderTableMouseListener(this);
        view.addMouseListener(headerTableMouseListener);

        // not used currently
        filterActionListener = new FilterActionListener(this);

        // create a new markAsReadTimer
        markAsReadTimer = new MarkAsReadTimer(this);

        // not used currently
        getView().setTransferHandler(new MessageTransferHandler(this));
        getView().setDragEnabled(false);

        // MouseListener sorts table when clicking on a column header
        new TableHeaderMouseListener(getView(), getTableModelSorter());

        // register at focus manager
        MainInterface.focusManager.registerComponent(this);

        // we need this for the focus manager
        getView().getSelectionModel().addListSelectionListener(this);
    }

    /**
     * Get view of table controller
     * 
     * @return table view
     */
    public TableView getView() {
        return view;
    }

    /**
     * Get table model
     * 
     * @return      table model
     */
    public HeaderTableModel getHeaderTableModel() {
        return headerTableModel;
    }

    /**
     * Select messages with UIDs.
     * <p>
     * Message UIDs are converted to {@link MessageNode}
     * objects.
     * 
     * @param uids      array of message UIDs
     */
    public void setSelected(Object[] uids) {
        MessageNode[] nodes = new MessageNode[uids.length];

        for (int i = 0; i < uids.length; i++) {
            nodes[i] = getHeaderTableModel().getMessageNode(uids[i]);
        }

        TreePath[] paths = new TreePath[nodes.length];

        for (int i = 0; i < nodes.length; i++) {
            paths[i] = new TreePath(nodes[i].getPath());
        }

        view.getTree().setSelectionPaths(paths);
    }

    /************************** actions ********************************/
    /**
     * create the PopupMenu
     */
    public void createPopupMenu() {
        menu = new TableMenu(mailFrameController);
    }

    /**
     * Get popup menu
     * @return      popup menu
     */
    public JPopupMenu getPopupMenu() {
        return menu;
    }

    /**
     * Method is called if folder data changed.
     * <p>
     * It is responsible for updating the correct underlying model.
     * 
     * @param event     update event
     */
    public void tableChanged(TableModelChangedEvent event) throws Exception {
        // selected rows before updating the model
        // -> used later to restore the selection
        previouslySelectedRows = view.getSelectedRows();

        // folder in which the update occurs
        FolderTreeNode folder = event.getSrcFolder();

        ColumbaLogger.log.info("source folder=" + folder.getName());

        // get current selection
        FolderCommandReference[] r =
            (FolderCommandReference[]) mailFrameController
                .getSelectionManager()
                .getSelection("mail.table");
        Folder srcFolder = (Folder) r[0].getFolder();

        // its always possible that no folder is currenlty selected
        if (srcFolder != null) {
            ColumbaLogger.log.info("selected folder=" + srcFolder.getName());
        }

        // make tree visible
        if (getMailFrameController() instanceof ThreePaneMailFrameController) {
            if (srcFolder != null) {
                ((ThreePaneMailFrameController) getMailFrameController())
                    .treeController
                    .getView()
                    .makeVisible(srcFolder.getSelectionTreePath());
            }
        }

        // update infopanel (gray panel below the toolbar)
        // showing total/unread/recent messages count
        if (getMailFrameController() instanceof ThreePaneMailFrameController) {
            if (srcFolder != null) {
                ((ThreePaneMailFrameController) getMailFrameController())
                    .folderInfoPanel
                    .setFolder(srcFolder);
            }
        }

        // only update table if, this folder is the same
        // as the currently selected
        if (!folder.equals(srcFolder)) {
            return;
        }

        //System.out.println("headertableviewer->folderChanged");
        switch (event.getEventType()) {
            case TableModelChangedEvent.SET :
                {
                    updateManager.set(event.getHeaderList());

                    break;
                }

            case TableModelChangedEvent.UPDATE :
                {
                    updateManager.update();

                    break;
                }

            case TableModelChangedEvent.REMOVE :
                {
                    updateManager.remove(event.getUids());

                    break;
                }

            case TableModelChangedEvent.MARK :
                {
                    updateManager.modify(event.getUids());

                    break;
                }
        }

        // re-select previous selection
        if (previouslySelectedRows != null) {
            // only re-select if only a single row was formerly selected
            if (previouslySelectedRows.length == 1) {
                view.selectRow(previouslySelectedRows[0]);
            }
        }
    }

    /**
     * Returns the mailFrameController.
     * @return MailFrameController
     */
    public AbstractMailFrameController getMailFrameController() {
        return mailFrameController;
    }

    /**
     * Returns the markAsReadTimer.
     * @return MarkAsReadTimer
     */
    public MarkAsReadTimer getMarkAsReadTimer() {
        return markAsReadTimer;
    }

    /**
     * Save folder properties of currently selected folder.
     *
     */
    public void saveProperties() {
        if (previouslySelectedFolder != null) {
            getMailFrameController().getFolderOptionsController().save(
                previouslySelectedFolder);
        }
    }

    /**
     * @see org.columba.mail.gui.frame.ViewHeaderListInterface#showHeaderList(org.columba.mail.folder.Folder, org.columba.mail.message.HeaderList)
     */
    public void showHeaderList(Folder folder, HeaderList headerList)
        throws Exception {
        // save previously selected folder options
        if (previouslySelectedFolder != null) {
            getMailFrameController().getFolderOptionsController().save(
                previouslySelectedFolder);
        }

        // load options of newly selected folder
        getMailFrameController().getFolderOptionsController().load(folder, FolderOptionsController.STATE_BEFORE);
             
        // send an update notification to the table model
        TableModelChangedEvent ev =
            new TableModelChangedEvent(
                TableModelChangedEvent.SET,
                folder,
                headerList);
        tableChanged(ev);

        // load options of newly selected folder
        getMailFrameController().getFolderOptionsController().load(folder, FolderOptionsController.STATE_AFTER);

        // remember previously selected folder
        previouslySelectedFolder = folder;
    }

    /************* implement getter/setter methods *********************/
    /**
     * return the table model sorter
     */
    public TableModelSorter getTableModelSorter() {
        return tableModelSorter;
    }

    /**
     * return the threaded view model
     */
    public TableModelThreadedView getTableModelThreadedView() {
        return tableModelThreadedView;
    }

    /**
     * return the filtered view model
     */
    public TableModelFilter getTableModelFilteredView() {
        return tableModelFilteredView;
    }

    /**
     * @return
     */
    public TableModelUpdateManager getUpdateManager() {
        return updateManager;
    }

    /********************* FocusOwner interface ************************/

    /* (non-Javadoc)
     * @see org.columba.core.gui.focus.FocusOwner#copy()
     */
    public void copy() {
        new CopyAction(getMailFrameController()).actionPerformed(null);
    }

    /* (non-Javadoc)
     * @see org.columba.core.gui.focus.FocusOwner#cut()
     */
    public void cut() {
        new CutAction(getMailFrameController()).actionPerformed(null);
    }

    /* (non-Javadoc)
     * @see org.columba.core.gui.focus.FocusOwner#delete()
     */
    public void delete() {
        new DeleteAction(getMailFrameController()).actionPerformed(null);
    }

    /* (non-Javadoc)
     * @see org.columba.core.gui.focus.FocusOwner#getComponent()
     */
    public JComponent getComponent() {
        return getView();
    }

    /* (non-Javadoc)
     * @see org.columba.core.gui.focus.FocusOwner#isCopyActionEnabled()
     */
    public boolean isCopyActionEnabled() {
        if (getView().getSelectedNodes().length > 0) {
            return true;
        }

        return false;
    }

    /* (non-Javadoc)
     * @see org.columba.core.gui.focus.FocusOwner#isCutActionEnabled()
     */
    public boolean isCutActionEnabled() {
        if (getView().getSelectedNodes().length > 0) {
            return true;
        }

        return false;
    }

    /* (non-Javadoc)
     * @see org.columba.core.gui.focus.FocusOwner#isDeleteActionEnabled()
     */
    public boolean isDeleteActionEnabled() {
        if (getView().getSelectedNodes().length > 0) {
            return true;
        }

        return false;
    }

    /* (non-Javadoc)
     * @see org.columba.core.gui.focus.FocusOwner#isPasteActionEnabled()
     */
    public boolean isPasteActionEnabled() {
        if (MainInterface.clipboardManager.getMessageSelection() == null) {
            return false;
        }

        if (MainInterface.clipboardManager.getMessageSelection().length > 0) {
            return true;
        }

        return false;
    }

    /* (non-Javadoc)
     * @see org.columba.core.gui.focus.FocusOwner#isRedoActionEnabled()
     */
    public boolean isRedoActionEnabled() {
        // action not supported
        return false;
    }

    /* (non-Javadoc)
     * @see org.columba.core.gui.focus.FocusOwner#isSelectAllActionEnabled()
     */
    public boolean isSelectAllActionEnabled() {
        if (getView().getRowCount() > 0) {
            return true;
        }

        return false;
    }

    /* (non-Javadoc)
     * @see org.columba.core.gui.focus.FocusOwner#isUndoActionEnabled()
     */
    public boolean isUndoActionEnabled() {
        // action not supported
        return false;
    }

    /* (non-Javadoc)
     * @see org.columba.core.gui.focus.FocusOwner#paste()
     */
    public void paste() {
        new PasteAction(getMailFrameController()).actionPerformed(null);
    }

    /* (non-Javadoc)
     * @see org.columba.core.gui.focus.FocusOwner#redo()
     */
    public void redo() {
        // action not supported
    }

    /* (non-Javadoc)
     * @see org.columba.core.gui.focus.FocusOwner#selectAll()
     */
    public void selectAll() {
        getView().selectAll();
    }

    /* (non-Javadoc)
     * @see org.columba.core.gui.focus.FocusOwner#undo()
     */
    public void undo() {
        // todo is not supported
    }

    /*************************** ListSelectionListener interface **************/

    /* (non-Javadoc)
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    public void valueChanged(ListSelectionEvent arg0) {
        MainInterface.focusManager.updateActions();
    }
}
