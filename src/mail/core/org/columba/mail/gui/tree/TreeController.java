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
package org.columba.mail.gui.tree;

import java.util.logging.Logger;

import javax.swing.JPopupMenu;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.main.MainInterface;
import org.columba.core.xml.XmlElement;
import org.columba.mail.config.FolderItem;
import org.columba.mail.folder.AbstractFolder;
import org.columba.mail.folder.MessageFolder;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.mail.gui.table.command.ViewHeaderListCommand;
import org.columba.mail.gui.tree.util.FolderTreeCellRenderer;


/**
 * this class shows the the folder hierarchy
 */
public class TreeController implements TreeWillExpandListener {

    /** JDK 1.4+ logging framework logger, used for logging. */
    private static final Logger LOG = Logger.getLogger("org.columba.mail.gui.tree");

    private FolderTreeMouseListener mouseListener;
    private AbstractFolder selectedFolder;
    private TreeView view;
    private FrameMediator frameController;
    private TreeMenu menu;

    /**
     * Constructor for tree controller.
     * @param controller the parent controller.
     * @param model the tree model to display.
     */
    public TreeController(FrameMediator controller, TreeModel model) {
        frameController = controller;

        view = new TreeView(model);
        view.setSortingEnabled(false);

        view.addTreeWillExpandListener(this);

        mouseListener = new FolderTreeMouseListener(this);

        view.addMouseListener(mouseListener);

        FolderTreeCellRenderer renderer = new FolderTreeCellRenderer();
        view.setCellRenderer(renderer);

        getView().setTransferHandler(new TreeViewTransferHandler());
        getView().setDragEnabled(true);

        /*
        getView().getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0),
                "RENAME");
        RenameFolderAction action = new RenameFolderAction(mailFrameController);
        getView().getActionMap().put("RENAME", action);
        */
    }

    /**
     * Returns the tree view.
     * @return the tree view.
     */
    public TreeView getView() {
        return view;
    }

    /**
     * Set the specified folder as seleceted.
     * @param folder the new selected folder.
     */
    public void setSelected(MessageFolder folder) {
        view.clearSelection();

        TreePath path = folder.getSelectionTreePath();

        view.requestFocus();
        view.setLeadSelectionPath(path);
        view.setAnchorSelectionPath(path);
        view.expandPath(path);

        this.selectedFolder = folder;

        MainInterface.processor.addOp(new ViewHeaderListCommand(
                getFrameController(),
                ((MailFrameMediator) getFrameController()).getTreeSelection()));
    }

    /**
     * Creates a Popup menu.
     */
    public void createPopupMenu() {
        menu = new TreeMenu(frameController);
    }

    /**
     * Returns the pop up menu for the controller.
     * @return the pop up menu.
     */
    public JPopupMenu getPopupMenu() {
        return menu;
    }

    /**
     * Returns the selected folder.
     * @return the selected folder.
     */
    public AbstractFolder getSelected() {
        return selectedFolder;
    }

    /**
     * Returns the mailFrameController.
     * @return MailFrameController
     */
    public FrameMediator getFrameController() {
        return frameController;
    }

    /******************** TreeWillExpand Interface *******************************/

    /** {@inheritDoc} */
    public void treeWillExpand(TreeExpansionEvent e) throws ExpandVetoException {
        LOG.info("treeWillExpand=" + e.getPath().toString());

        AbstractFolder treeNode = (AbstractFolder) e.getPath()
                                                    .getLastPathComponent();

        if (treeNode == null) {
            return;
        }

        /*
// fetch new sub folder list
// -> this is a hack for imap folder:
// -> when expanding the IMAPRootFolder the
// -> list of folders gets synchronized
FolderCommandReference[] cr = new FolderCommandReference[1];
cr[0] = new FolderCommandReference(treeNode);

MainInterface.processor.addOp(new FetchSubFolderListCommand(cr));
*/
        // save expanded state
        saveExpandedState(treeNode, e.getPath());
    }

    /** {@inheritDoc} */
    public void treeWillCollapse(TreeExpansionEvent e) {
        AbstractFolder treeNode = (AbstractFolder) e.getPath()
                                                    .getLastPathComponent();

        if (treeNode == null) {
            return;
        }

        // save expanded state
        saveExpandedState(treeNode, e.getPath());
    }

    /**
     * Saves the tree expanded state.
     * @param folder the folder to get the configuration for.
     * @param path the tree path in the tree view.
     */
    private void saveExpandedState(AbstractFolder folder, TreePath path) {
        FolderItem item = folder.getConfiguration();

        XmlElement property = item.getElement("property");

        // Note: we negate the expanded state because this is
        //       a will-expand/collapse listener
        if (!getView().isExpanded(path)) {
            property.addAttribute("expanded", "true");
        } else {
            property.addAttribute("expanded", "false");
        }
    }
}
