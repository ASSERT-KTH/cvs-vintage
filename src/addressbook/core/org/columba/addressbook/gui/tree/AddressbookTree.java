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

package org.columba.addressbook.gui.tree;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.columba.addressbook.config.AdapterNode;
import org.columba.addressbook.config.AddressbookConfig;
import org.columba.addressbook.folder.Folder;
import org.columba.addressbook.gui.tree.util.AddressbookTreeCellRenderer;
import org.columba.addressbook.gui.tree.util.EditAddressbookFolderDialog;
import org.columba.addressbook.gui.tree.util.SelectAddressbookFolderDialog;
import org.columba.addressbook.main.AddressbookInterface;

public class AddressbookTree implements TreeSelectionListener
{
	private AddressbookTreeNode root;
	private JTree tree;
	private AddressbookInterface addressbookInterface;
	private TreeModel model;

	public JScrollPane scrollPane;

	public AddressbookTree(AddressbookInterface i)
	{
		this.addressbookInterface = i;

		//root = new AddressbookTreeNode("Root");

		root = generateTree();

		
		
		model = new TreeModel( AddressbookConfig.get("tree").getElement("/tree") );

		tree = new JTree(model);
		//tree.setPreferredSize( new Dimension( 200,300 ) );
		tree.setShowsRootHandles(true);
		tree.setRootVisible(false);
		tree.expandRow(0);
		tree.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		tree.addTreeSelectionListener(this);

		AddressbookTreeCellRenderer renderer = new AddressbookTreeCellRenderer(true);

		// FIXME
		
		//tree.setCellRenderer(renderer);

		update();

		scrollPane = new JScrollPane(tree);

	}

	public EditAddressbookFolderDialog getEditAddressbookFolderDialog(String name)
	{
		EditAddressbookFolderDialog dialog =
			new EditAddressbookFolderDialog(addressbookInterface.frame, name);

		return dialog;
	}

	public SelectAddressbookFolderDialog getSelectAddressbookFolderDialog()
	{
		SelectAddressbookFolderDialog dialog =
			new SelectAddressbookFolderDialog(addressbookInterface.frame, model);

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

		/*
		Folder folder = (Folder) tree.getLastSelectedPathComponent();
		if (folder == null)
			return;

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

}