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

package org.columba.addressbook.gui.tree.util;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.columba.addressbook.gui.tree.AddressbookTreeNode;
import org.columba.core.gui.util.ImageLoader;

public class AddressbookTreeCellRenderer extends DefaultTreeCellRenderer
{
	Border unselectedBorder = null;
	Border selectedBorder = null;
	boolean isBordered = true;
	boolean bool;
	ImageIcon image;

	private ImageIcon image1;
	private ImageIcon image2;
	private ImageIcon image3;

	private String fontName;
	private int fontSize;

	public AddressbookTreeCellRenderer(boolean bool)
	{
		super();

		this.bool = bool;

		image1 = ImageLoader.getSmallImageIcon("stock_book-16.png");
		
		
		image2 = ImageLoader.getSmallImageIcon("localhost.png");
		

		image3 = ImageLoader.getSmallImageIcon("remotehost.png");
		

		//setOpaque(true); //MUST do this for background to show up.
	}

	public Component getTreeCellRendererComponent(
		JTree tree,
		Object value,
		boolean isSelected,
		boolean expanded,
		boolean leaf,
		int row,
		boolean hasFocus)
	{
		super.getTreeCellRendererComponent(
			tree,
			value,
			isSelected,
			expanded,
			leaf,
			row,
			hasFocus);

		AddressbookTreeNode folder = (AddressbookTreeNode) value;
		if ( folder==null ) return this;
		
		setText( folder.getName() );
		setIcon( folder.getIcon() );
		
		/*
		FolderItem item = folder.getFolderItem();
		if ( item==null ) return this;
		
		//int uid = item.getUid();
		int uid = 100;
		
		if ( uid == 100 )
		{
			setIcon(image2);
		}
		else if ( uid == 200 )
		{
			setIcon(image3);
		}
		else
		{
			setIcon(image1);
		}
		*/
		
		/*
		if (value instanceof AddressbookFolder)
		{
			AddressbookFolder folder = (AddressbookFolder) value;

			FolderItem item = folder.getFolderItem();
			if (item != null)
			{
				String name = item.getName();
				String type = item.getType();

				if ( type == "addressbook") )
					setIcon(image1);
				else
					setIcon(image2);
				

			}
			
		
			setText(folder.getName());	
			

		}
		else
		{
			
			
		}
		*/
		


			return this;
		}

	}