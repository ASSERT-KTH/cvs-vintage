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

package org.columba.mail.gui.composer;

import java.awt.Component;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.columba.core.gui.util.ImageLoader;

/**
 * @author frd
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class AttachmentMenu extends JPopupMenu{
	
	JMenuItem menuItem;
	
	AttachmentController controller;
	
	public AttachmentMenu( AttachmentController c )
	{
		super();
		
		this.controller = c;
		
		initComponents( c);
	}
	
	protected void initComponents( AttachmentController c )
	{
		
    	menuItem = new JMenuItem("Attach File..", ImageLoader.getSmallImageIcon("stock_attach-16.png") );
    	menuItem.setActionCommand("ADD");
    	menuItem.addActionListener( c.getActionListener() );
    	add( menuItem );
    	addSeparator();
    	menuItem = new JMenuItem("Remove Selected Attachments", ImageLoader.getSmallImageIcon("stock_delete-16.png"));
    	menuItem.setActionCommand("REMOVE");
    	menuItem.addActionListener( c.getActionListener() );
    	add( menuItem );
    	   	
	}
	
	
}
