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

package org.columba.core.gui.util;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

import org.columba.mail.gui.action.BasicAction;

public class CMenuItem extends JMenuItem {

	public CMenuItem() {
		super();

	}

	public CMenuItem(BasicAction a) {
		super(a);
		setAccelerator(a.getAcceleratorKey());

		/*
		if (a.getSmallIcon() == null)
			setIcon(new EmptyIcon());
		*/
	}

	public CMenuItem(String s) {
		super(s);
		//setIcon(new EmptyIcon());

	}

	public CMenuItem(String s, ImageIcon icon) {
		super(s, icon);

	}

	public CMenuItem(String s, int mnemonic) {
		super(s, mnemonic);

		//setIcon(new EmptyIcon());

	}

}