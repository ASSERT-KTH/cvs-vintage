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

package org.columba.mail.gui.action;

import javax.swing.Action;
import javax.swing.KeyStroke;
//import javax.swing.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;

public class BasicAction extends JAbstractAction {
	boolean showToolbarText = true;

	public BasicAction() {
		super();

	}

	public BasicAction(
		String name,
		String longDescription,
		String actionCommand,
		ImageIcon small_icon,
		ImageIcon big_icon,
		int mnemonic,
		KeyStroke keyStroke) {
		super();
		putValue(Action.NAME, name);
		putValue(Action.LONG_DESCRIPTION, longDescription);
		putValue(Action.SMALL_ICON, small_icon);
		LARGE_ICON = big_icon;

		putValue(Action.SHORT_DESCRIPTION, longDescription);
		putValue(Action.ACTION_COMMAND_KEY, actionCommand);
		putValue(Action.ACCELERATOR_KEY, keyStroke);
		putValue(Action.MNEMONIC_KEY, new Integer(mnemonic));
		
		TOOLBAR_NAME = name;

		setReplacementIcon();
	}
	
	public BasicAction(
		String name,
		String longDescription,
		String actionCommand,
		ImageIcon small_icon,
		ImageIcon big_icon,
		int mnemonic,
		KeyStroke keyStroke,
		boolean showToolbarText) {
		super();
		putValue(Action.NAME, name);
		putValue(Action.LONG_DESCRIPTION, longDescription);
		putValue(Action.SMALL_ICON, small_icon);
		LARGE_ICON = big_icon;

		putValue(Action.SHORT_DESCRIPTION, longDescription);
		putValue(Action.ACTION_COMMAND_KEY, actionCommand);
		putValue(Action.ACCELERATOR_KEY, keyStroke);
		putValue(Action.MNEMONIC_KEY, new Integer(mnemonic));

		this.showToolbarText = showToolbarText;
		
		TOOLBAR_NAME = name;
		
		setReplacementIcon();
	}

	public BasicAction(
		String name,
		String tname,
		String longDescription,
		String actionCommand,
		ImageIcon small_icon,
		ImageIcon big_icon,
		int mnemonic,
		KeyStroke keyStroke) {
		super();
		putValue(Action.NAME, name);
		putValue(Action.LONG_DESCRIPTION, longDescription);
		putValue(Action.SHORT_DESCRIPTION, longDescription);
		putValue(Action.SMALL_ICON, small_icon);
		LARGE_ICON = big_icon;

		
		putValue(Action.ACTION_COMMAND_KEY, actionCommand);
		putValue(Action.ACCELERATOR_KEY, keyStroke);
		putValue(Action.MNEMONIC_KEY, new Integer(mnemonic));

		TOOLBAR_NAME = tname;
		
		setReplacementIcon();
		

	}
	
	public BasicAction(
		String name,
		String tname,
		String longDescription,
		String actionCommand,
		ImageIcon small_icon,
		ImageIcon big_icon,
		int mnemonic,
		KeyStroke keyStroke,
		boolean showToolbarText) {
		super();
		putValue(Action.NAME, name);
		putValue(Action.LONG_DESCRIPTION, longDescription);
		putValue(Action.SHORT_DESCRIPTION, longDescription);
		putValue(Action.SMALL_ICON, small_icon);
		LARGE_ICON = big_icon;

		
		putValue(Action.ACTION_COMMAND_KEY, actionCommand);
		putValue(Action.ACCELERATOR_KEY, keyStroke);
		putValue(Action.MNEMONIC_KEY, new Integer(mnemonic));

		TOOLBAR_NAME = tname;
		
		this.showToolbarText = showToolbarText;
		
		setReplacementIcon();

	}
	
	public boolean isShowToolbarText()
	{
		return this.showToolbarText;
	}

	protected void setReplacementIcon()
	{
		/*
		if ( getSmallIcon() == null )
		{
			// no icon found - set empty icon as replacement
			
			putValue( Action.SMALL_ICON, new Replacement() );
		}
		*/
	}
	
		
	class Replacement implements Icon {

		public int getIconWidth() {
			return 16;
		}
		public int getIconHeight() {
			return 16;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			//g.setColor(Color.black);
			//g.fillRect(c.getX(), c.getY(), c.getWidth(), c.getHeight());
		}
	}
}