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

package org.columba.addressbook.config;

import java.io.File;
import java.util.Vector;

import org.columba.addressbook.folder.AddressbookFolder;
import org.columba.addressbook.folder.Folder;
import org.columba.addressbook.main.AddressbookInterface;
import org.columba.core.config.AdapterNode;
import org.columba.core.config.DefaultXmlConfig;

/**
 * @version 	1.0
 * @author
 */
public class TreeXmlConfig extends DefaultXmlConfig
{
	private File file;

	public TreeXmlConfig(File file)
	{
		super(file);
	}

	public AdapterNode getRootNode()
	{
		AdapterNode node = new AdapterNode(getDocument());

		AdapterNode rootNode = node.getChild(0);

		return rootNode;
	}

	protected void generateSubfolder(
		Folder parent,
		AdapterNode parentNode,
		AddressbookInterface addressbookInterface)
	{
		for (int i = 0; i < parentNode.getChildCount(); i++)
		{
			AdapterNode childNode = parentNode.getChild(i);
			if (childNode.getName().equals("folder"))
			{
				AdapterNode typechild = childNode.getChild("type");

				if ( (typechild.getValue().equalsIgnoreCase("addressbook")) || (typechild.getValue().equalsIgnoreCase("system")) )
				{
					FolderItem item = getFolderItem(childNode);
					AddressbookFolder child = new AddressbookFolder(item, addressbookInterface);
					parent.add(child);

					generateSubfolder(child, childNode, addressbookInterface);
				}
				else
				{
					// instance ldap-folder here
				}
			}
		}

	}

	public Folder generateTree(AddressbookInterface addressbookInterface)
	{
		//AddressbookFolder root = new AddressbookFolder(null, addressbookInterface);

		AdapterNode parent = getRootNode().getChild(0);
		
		FolderItem item = getFolderItem(parent);
		AddressbookFolder root = new AddressbookFolder(item, addressbookInterface);

		generateSubfolder(root, getRootNode(), addressbookInterface);

		return root;
	}

	/*
	public DomToTreeModelAdapter getTreeModel()
	{
	    DomToTreeModelAdapter treeModel = new DomToTreeModelAdapter( getDocument() );
	
	    return treeModel;
	}
	*/

	// create uid list from all accounts
	protected void getUids(Vector v, AdapterNode parent)
	{

		int childCount = parent.getChildCount();

		if (childCount > 0)
		{
			for (int i = 0; i < childCount; i++)
			{

				AdapterNode child = parent.getChild(i);

				getUids(v, child);

				//System.out.println("name: "+ child.getName() );

				if (child.getName().equals("folder"))
				{
					AdapterNode uidNode = child.getChild("uid");

					Integer j = new Integer(uidNode.getValue());

					v.add(j);
				}

			}
		}
	}

	// find a free uid for a new account
	protected String createUid()
	{
		Vector v = new Vector();

		AdapterNode rootNode = new AdapterNode(getDocument());
		AdapterNode treeNode = rootNode.getChild(0);

		getUids(v, treeNode);

		int result = -1;
		boolean hit;
		boolean exit = false;

		while (exit == false)
		{
			hit = false;
			result++;
			for (int i = 0; i < v.size(); i++)
			{
				Integer j = (Integer) v.get(i);

				if (j.intValue() == result)
				{
					hit = true;
				}
			}
			if (hit == false)
				exit = true;
		}

		Integer newUid = new Integer(result);

		return newUid.toString();
	}

	public FolderItem getFolderItem(AdapterNode node)
	{
		if (node != null)
		{
			if (node.getName().equals("folder"))
			{
				FolderItem folderItem = new FolderItem(node, getDocument());

				return folderItem;
			}

		}

		return null;
	}
}