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

package org.columba.addressbook.gui.tree;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.columba.addressbook.config.AdapterNode;
import org.columba.addressbook.folder.Folder;
import org.columba.addressbook.gui.frame.AddressbookFrameController;
import org.columba.addressbook.gui.tree.util.AddressbookTreeCellRenderer;
import org.columba.addressbook.gui.tree.util.EditAddressbookFolderDialog;
import org.columba.addressbook.gui.tree.util.SelectAddressbookFolderDialog;
import org.columba.core.main.MainInterface;

public class TreeView implements TreeSelectionListener
{
	private AddressbookTreeNode root;
	private JTree tree;
	//private AddressbookInterface addressbookInterface;
	private AddressbookTreeModel model;

	public JScrollPane scrollPane;

	protected AddressbookFrameController frameController;
	
	public TreeView(AddressbookFrameController frameController)
	{
		//this.addressbookInterface = i;
		this.frameController = frameController;
		

		//root = new AddressbookTreeNode("Root");

		root = generateTree();

		
		
		//model = new AddressbookTreeModel( AddressbookConfig.get("tree").getElement("/tree") );
		
		model = MainInterface.addressbookTreeModel;
		
		tree = new JTree( model );
		//tree.setPreferredSize( new Dimension( 200,300 ) );
		tree.setShowsRootHandles(true);
		tree.setRootVisible(false);
		tree.expandRow(0);
		tree.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		tree.addTreeSelectionListener(this);

		AddressbookTreeCellRenderer renderer = new AddressbookTreeCellRenderer(true);

		
		
		tree.setCellRenderer(renderer);

		update();

		scrollPane = new JScrollPane(tree);

	}

	public EditAddressbookFolderDialog getEditAddressbookFolderDialog(String name)
	{
		EditAddressbookFolderDialog dialog =
			new EditAddressbookFolderDialog(frameController.getView(), name);

		return dialog;
	}

	public SelectAddressbookFolderDialog getSelectAddressbookFolderDialog()
	{
		SelectAddressbookFolderDialog dialog =
			new SelectAddressbookFolderDialog(frameController.getView(), model);

		return dialog;
	}

	public Folder getFolder(int uid)
	{
		/*
		Folder root = getRootFolder();

		for (Enumeration e = root.depthFirstEnumeration(); e.hasMoreElements();)
		{
			Folder node = (Folder) e.nextElement();

			FolderItem item = node.getFolderItem();
			if (item == null)
				continue;

			int id = item.getUid();

			if (uid == id)
			{
				return node;
			}

		}
		*/
		return null;
	}

	public Folder getRootFolder()
	{
		return (Folder) model.getRoot();
	}

	protected Folder generateTree()
	{
		/*
		Folder root = new Folder("root");
		Folder child1 = new Folder("child1");
		root.add( child1 );
		Folder child2 = new Folder("child2");
		root.add( child2 );
		*/
		
		
/*
		TreeXmlConfig config = AddressbookConfig.getTreeConfig();

		return config.generateTree(addressbookInterface);
		*/
		
		return null;
	}

	public JTree getTree()
	{
		return tree;
	}

	public void removeAll()
	{
		root.removeAllChildren();
	}

	public void update()
	{
		/*
		removeAll();
		tree.setRootVisible( true );
		
		model = new TreeModel( root );
		
		AddressbookXmlConfig config = addressbookInterface.mainInterface.config.getAddressbookConfig();
		AdapterNode node, child;
		for ( int i=0 ; i < config.count() ; i++ )
		{
		        node = config.getAddressbookNode(i);
		        System.out.println("found addressbook");
		
		        AddressbookTreeNode treeNode = add( node);
		        
		        AddressbookItem item = config.getAddressbookItem( node );
		        Vector v = item.getGroupList();
		        
		        for ( int j=0; j<v.size(); j++ )
		        {
		            AdapterNode child2 = (AdapterNode) v.get(j);
		            String name = child2.getChild("name").getValue();
		            
		            AddressbookTreeNode n = new AddressbookTreeNode( name );
		            
		            treeNode.add( n );
		        }
		}
		
		tree.setModel( model );
		
		tree.expandRow(0);
		tree.setRootVisible( false );
		*/

	}

	public AddressbookTreeNode add(AdapterNode node)
	{
		/*
		String name = node.getChild("name").getValue();

		AddressbookTreeNode child = new AddressbookTreeNode(name);

		if (root.getChildCount() == 0)
		{
			tree.setRootVisible(true);
			root.add(child);
			tree.expandRow(0);
			tree.setRootVisible(false);
		}
		else
		{
			root.add(child);
		}

		return child;
		*/
		
		return null;
	}

	public Folder getSelectedFolder()
	{
		return (Folder) tree.getLastSelectedPathComponent();
	}

	public void valueChanged(TreeSelectionEvent e)
	{

		
		Folder folder = (Folder) tree.getLastSelectedPathComponent();
		if (folder == null)
			return;

		getFrameController().getTable().getView().setFolder(folder);
		/*
		FolderItem item = folder.getFolderItem();
		String type = item.getType();
		int uid = item.getUid();

		if (type.equals("addressbook") || type.equals("ldap"))
		{

			addressbookInterface.table.setFolder(folder);

			addressbookInterface.actionListener.changeActions();

		}
		else
		{
			addressbookInterface.table.setFolder(null);

			addressbookInterface.actionListener.changeActions();
		}
		*/
		
	}

	/**
	 * @return FrameController
	 */
	public AddressbookFrameController getFrameController() {
		return frameController;
	}

}