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

package org.columba.mail.gui.table.menu;

import org.columba.mail.gui.util.*;
import org.columba.core.gui.util.*;
import org.columba.mail.gui.table.*;
import org.columba.mail.gui.table.action.*;
import org.columba.core.gui.statusbar.*;
import org.columba.mail.util.*;

import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

/**
 * menu for the tableviewer
 */


public class HeaderTableMenu
{
    private JPopupMenu popupMenu;
    private TableController headerTableViewer;

    public HeaderTableMenu( TableController headerTableViewer )
    {
        this.headerTableViewer = headerTableViewer;

        initPopupMenu();
    }

    public JPopupMenu getPopupMenu()
    {
        return popupMenu;
    }

    protected HeaderTableMouseListener getMouseListener()
    {
        return headerTableViewer.getHeaderTableMouseListener();
    }

    protected HeaderTableActionListener getActionListener()
    {
        return headerTableViewer.getActionListener();
    }

    protected void initPopupMenu()
        {
           
	    popupMenu  = new JPopupMenu();
            //MouseListener popupListener = new PopupListener();
            headerTableViewer.getView().addMouseListener( getMouseListener() );

	    MouseAdapter handler = headerTableViewer.getMailFrameController().getMouseTooltipHandler();
            JMenuItem menuItem;
	    JMenu subMenu;

		menuItem = new CMenuItem( getActionListener().openMessageWithComposerAction );
	    menuItem.addMouseListener( handler );
	    popupMenu.add( menuItem );
	    
        

            menuItem = new CMenuItem( getActionListener().printAction );
	    menuItem.addMouseListener( handler );
	    popupMenu.add( menuItem );
	    
	        menuItem = new CMenuItem( getActionListener().saveAction );
	    menuItem.addMouseListener( handler );
	    popupMenu.add( menuItem );
	    

            popupMenu.addSeparator();

	    menuItem = new CMenuItem( getActionListener().replyAction );
	    menuItem.addMouseListener( handler );
	    popupMenu.add( menuItem );

			
	    /*
	    subMenu = new JMenu( GlobalResourceLoader.getString("menu","mainframe","menu_message_replysubmenu"));
		popupMenu.add( subMenu );
		*/
		
	    menuItem = new CMenuItem( getActionListener().replyToAllAction );
	    menuItem.addMouseListener( handler );
	    popupMenu.add( menuItem );
	    
	    menuItem = new CMenuItem( getActionListener().replyToAction );
	    menuItem.addMouseListener( handler );
	    popupMenu.add( menuItem );
	    
	    menuItem = new CMenuItem( getActionListener().replyAsAttachmentAction );
	    menuItem.addMouseListener( handler );
	    popupMenu.add( menuItem );

	    menuItem = new CMenuItem( getActionListener().forwardAction );
	    menuItem.addMouseListener( handler );
	    popupMenu.add( menuItem );

            menuItem = new CMenuItem( getActionListener().forwardInlineAction );
	    menuItem.addMouseListener( handler );
	    popupMenu.add( menuItem );

	  	// add the bounce-action 
			// bounce is reply with an error message
			menuItem = new CMenuItem( getActionListener().bounceAction );
	    menuItem.addMouseListener( handler );
	    popupMenu.add( menuItem );

	    popupMenu.addSeparator();

	    menuItem = new CMenuItem( getActionListener().moveMessageAction );
	    menuItem.addMouseListener( handler );
	    popupMenu.add( menuItem );

	    menuItem = new CMenuItem( getActionListener().copyMessageAction );
	    menuItem.addMouseListener( handler );
	    popupMenu.add( menuItem );

            menuItem = new CMenuItem( getActionListener().deleteMessageAction );
	    menuItem.addMouseListener( handler );
	    popupMenu.add( menuItem );

            popupMenu.addSeparator();

            menuItem = new CMenuItem( getActionListener().addSenderAction );
	    menuItem.addMouseListener( handler );
	    popupMenu.add( menuItem );

            menuItem = new CMenuItem( getActionListener().addAllSendersAction );
	    menuItem.addMouseListener( handler );
	    popupMenu.add( menuItem );

	    popupMenu.addSeparator();

	    subMenu = new CMenu( MailResourceLoader.getString("menu","mainframe", "menu_message_mark") );
	    subMenu.setMnemonic( KeyEvent.VK_K );

	    menuItem = new org.columba.core.gui.util.CMenuItem(getActionListener().markAsReadAction);
	    menuItem.addMouseListener( handler );
	    subMenu.add( menuItem );
	    menuItem = new CMenuItem( "menu_message_markthreadasread", KeyEvent.VK_T);
	    menuItem.setEnabled(false);
	    subMenu.add( menuItem );
	    menuItem = new CMenuItem( "menu_message_markallasread", KeyEvent.VK_A);
	    menuItem.setAccelerator( KeyStroke.getKeyStroke("A") );
	    menuItem.setEnabled(false);
	    subMenu.add( menuItem );
	    subMenu.addSeparator();
	    menuItem = new org.columba.core.gui.util.CMenuItem(getActionListener().markAsFlaggedAction);
	    menuItem.addMouseListener( handler );
	    subMenu.add( menuItem );
	    menuItem = new org.columba.core.gui.util.CMenuItem(getActionListener().markAsExpungedAction);
	    menuItem.addMouseListener( handler );
	    subMenu.add( menuItem );



	    popupMenu.add( subMenu );


        }

}
