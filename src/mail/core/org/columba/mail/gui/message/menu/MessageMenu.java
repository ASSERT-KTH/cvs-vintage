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

package org.columba.mail.gui.message.menu;

import org.columba.core.gui.util.*;
import org.columba.main.*;
import org.columba.mail.gui.message.*;
import org.columba.core.gui.statusbar.*;
import org.columba.mail.gui.util.*;

import javax.swing.*;
import java.awt.event.*;

public class MessageMenu {
	private MessageController messageViewer;
	private JPopupMenu popup;

	public MessageMenu(MessageController messageViewer) {
		this.messageViewer = messageViewer;
		initContextMenu();
	}

	public JPopupMenu getPopupMenu() {
		return popup;
	}

	protected void initContextMenu() {

		/*
		popup  = new JPopupMenu();
		
		
		    //textPane.addMouseListener(popupListener);
		
		MouseAdapter handler = messageViewer.getMailFrameController().getMouseTooltipHandler();
		    JMenuItem menuItem;
		
		menuItem = new CMenuItem( frameController.globalActionCollection.copyAction );
		menuItem.addMouseListener( handler );
		popup.add( menuItem );
		
		    popup.addSeparator();
		
		menuItem = new CMenuItem( frameController.globalActionCollection.selectAllAction );
		menuItem.addMouseListener( handler );
		popup.add( menuItem );
		
		popup.addSeparator();
		
		menuItem = new CMenuItem( messageViewer.getActionListener().dictAction );
		menuItem.addMouseListener(handler);
		popup.add( menuItem );
		*/
	}
}