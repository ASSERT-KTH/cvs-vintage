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

package org.columba.mail.gui.tree;

import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.Folder;
import org.columba.mail.folder.FolderTreeNode;
import org.columba.mail.gui.frame.MailFrameController;
import org.columba.mail.gui.tree.action.FolderTreeActionListener;
import org.columba.mail.gui.tree.action.FolderTreeMouseListener;
import org.columba.mail.gui.tree.command.FetchSubFolderListCommand;
import org.columba.mail.gui.tree.menu.FolderTreeMenu;
import org.columba.mail.gui.tree.util.EditFolderDialog;
import org.columba.mail.gui.tree.util.FolderInfoPanel;
import org.columba.mail.gui.tree.util.SelectFolderDialog;
import org.columba.main.MainInterface;

/**
 * this class shows the the folder hierarchy
 */

public class TreeController implements TreeSelectionListener, TreeWillExpandListener//implements TreeNodeChangeListener 
{
	private TreeView folderTree;
	private boolean b = false;
	private TreePath treePath;
	private JPopupMenu popup;

	private FolderInfoPanel messageFolderInfoPanel;

	private FolderTreeActionListener actionListener;

	public JScrollPane scrollPane;

	private Folder oldSelected;

	private FolderTreeMenu menu;

	private FolderTreeMouseListener mouseListener;
	
	protected TreeSelectionManager treeSelectionManager;
	
	private FolderTreeNode selectedFolder;

	private TreeModel model;
	
	private TreeView view;
	
	private MailFrameController mailFrameController;

	public TreeController( MailFrameController mailFrameController, TreeModel model ) {

		this.model = model;
		this.mailFrameController = mailFrameController;


		view = new TreeView( model );
		
		actionListener = new FolderTreeActionListener(this);

		treeSelectionManager = new TreeSelectionManager();	
		
			
			
		view.addTreeSelectionListener(this);
		
		//folderTreeActionListener = new FolderTreeActionListener(this);
		view.addTreeWillExpandListener(this);
		
		mouseListener = new FolderTreeMouseListener(this);
		
		view.addMouseListener(mouseListener);
		
		
		
		FolderTreeDnd dnd = new FolderTreeDnd(this);
		
		//scrollPane = new JScrollPane(getFolderTree().getTree());
		
		
		
		menu = new FolderTreeMenu(this);
		
		//MainInterface.focusManager.registerComponent( new TreeFocusOwner(this) );
		
	}
	
	public void treeWillExpand(TreeExpansionEvent e)
		throws ExpandVetoException {

		System.out.println("treeWillExpand=" + e.getPath().toString());

		FolderTreeNode treeNode =
			(FolderTreeNode) e.getPath().getLastPathComponent();
			
		
		FolderCommandReference[] cr = new FolderCommandReference[1];
		cr[0] = new FolderCommandReference(treeNode);
		
		MainInterface.processor.addOp(
					new FetchSubFolderListCommand(mailFrameController, cr));

	}

	public void treeWillCollapse(TreeExpansionEvent e) {
	}

	public TreeModel getModel() {
		return model;
	}

	public TreeView getView() {
		
		
		
		
		return view;
	}
	
	public FolderTreeActionListener getActionListener()
	{
		return actionListener;
	}

	// this method is called when the user selects another folder
	
	
	
	public void valueChanged(TreeSelectionEvent e) {
		
		// BUGFIX but don't know why that bug occurs 
		if( view.getLastSelectedPathComponent() == null ) return;
		
		System.out.println("foldertree->valueChanged");
		
		System.out.println("treeController->selectFolderPath="+e.getNewLeadSelectionPath().toString() );
		System.out.println("treeController->selectFolder="+view.getLastSelectedPathComponent() );
		
		
		treeSelectionManager.fireFolderSelectionEvent( selectedFolder, (FolderTreeNode) view.getLastSelectedPathComponent() );
		
		selectedFolder = (FolderTreeNode) view.getLastSelectedPathComponent();
		
		getActionListener().changeActions();
		
		
		//if (selectedFolder == null) return;
	}

	public SelectFolderDialog getSelectFolderDialog() {
		
		SelectFolderDialog dialog =
			new SelectFolderDialog(MainInterface.frameController.getView(), this);
		return dialog;
		
	}
	
	

	/*
	public void addUserFolder( String name )
	{
	    addUserFolder( getSelected(), name );
	
	}
	*/

	/*    
	  public FolderTreeNode addVirtualFolder( String name )
	{
	    FolderTreeNode parentFolder = getSelected();
	
	    return addVirtualFolder( parentFolder, name );
	
	
	}
	*/

	public EditFolderDialog getEditFolderDialog(String name) {
		EditFolderDialog dialog =
			new EditFolderDialog(getMailFrameController().getView(), name);

		return dialog;
	}

	
	public JPopupMenu getPopupMenu() {
		return menu.getPopupMenu();
	}

	/*
	public DefaultTreeModel getModel() {
		return getFolderTree().getModel();
	}
	*/

	/*
	public void setSelected(FolderTreeNode folder) {
		getFolderTree().setSelected(folder);
	
		MainInterface.headerTableViewer.setFolder(folder);
	
		GuiOperation op = new GuiOperation(Operation.HEADERLIST, 4, folder);
		MainInterface.crossbar.operate(op);
	
	}
	*/

	/*
	public FolderTreeNode getSelected() {
	
		FolderTreeNode folder =
			(FolderTreeNode) getFolderTree().getTree().getLastSelectedPathComponent();
	
		return folder;
	}
	*/

	public FolderTreeNode getSelected()
	{
		return selectedFolder;
	}
	
	public void selectFolder() {
		
		/*
		treeSelectionManager.fireFolderSelectionEvent( selectedFolder, (FolderTreeNode) view.getLastSelectedPathComponent() );
		
		selectedFolder = (FolderTreeNode) view.getLastSelectedPathComponent();
		
		getActionListener().changeActions();
		*/
		
		//MainInterface.processor.addOp( new ViewHeaderListCommand( treeSelectionManager.getSelection() ), Command.FIRST_EXECUTION );
		
		/*
		FolderTreeNode folder = getSelected();
		if (folder == null)
			return;
	
		if (folder.equals(oldSelected))
			return;
		else
			oldSelected = folder;
	
		FolderItem item = folder.getFolderItem();
		if (item == null)
			return;
	
		getActionListener().changeActions();
	
		//MessageFolderInfo info = folder.getMessageFolderInfo();
		//MainInterface.folderInfoPanel.set(item, info);
	
		if (item.isMessageFolder()) {
			MainInterface.headerTableViewer.setFolder(folder);
			GuiOperation op =
				new GuiOperation(Operation.HEADERLIST, 20, folder);
	
			MainInterface.crossbar.operate(op);
		} else {
			MainInterface.headerTableViewer.setHeaderList(null);
		}
		*/
	}
	

	
	public void expandImapRootFolder() {
		/*
		FolderTreeNode folder = getSelected();
		if (folder == null)
			return;
	
		FolderItem item = folder.getFolderItem();
	
		if (item.getType().equals("imaproot")) {
			ImapRootFolder root = (ImapRootFolder) folder;
			FolderOperation op =
				new FolderOperation(Operation.FOLDER_LIST, 20, root);
			MainInterface.crossbar.operate(op);
		}
		*/
	}
	

	/*
	public void treeNodeChanged(TreeNodeEvent e) {
		final TreeNodeEvent event = e;
	
		Runnable run = new Runnable() {
			public void run() {
				getFolderTree().treeNodeChanged(event);
	
				if (getSelected().equals(event.getSource())) {
					if (MainInterface.folderInfoPanel != null) {
						FolderTreeNode folder = (FolderTreeNode) event.getSource();
						FolderItem item = folder.getFolderItem();
						MessageFolderInfo info = folder.getMessageFolderInfo();
	
						MainInterface.folderInfoPanel.set(item, info);
						//MainInterface.folderInfoPanel.update();
					}
				}
			}
		};
		try {
			if (!SwingUtilities.isEventDispatchThread())
				SwingUtilities.invokeAndWait(run);
			else
				SwingUtilities.invokeLater(run);
	
		} catch (Exception ex) {
		}
	
	}
	*/
	/**
	 * Returns the treeSelectionManager.
	 * @return TreeSelectionManager
	 */
	public TreeSelectionManager getTreeSelectionManager() {
		return treeSelectionManager;
	}

	/**
	 * Returns the mailFrameController.
	 * @return MailFrameController
	 */
	public MailFrameController getMailFrameController() {
		return mailFrameController;
	}

}