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

package org.columba.core.gui.themes.thincolumba;

import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTableUI;

public class ThinTableUI extends BasicTableUI {

	public static ComponentUI createUI(JComponent c) {
		return new ThinTableUI();
	}

	/**
	* Creates a MouseInputListener that replaces that in the
	* javax.swing.plaf.basic.BasicListUI so that DnD is
	* handled better
	*/
	protected MouseInputListener createMouseInputListener() {
		return new MyListUIMouseInputHandler();

	}

	/**
	* Changes the MouseInputHandler so that a mouse click
	selects
	* rather than a mouse press.
	*/
	class MyListUIMouseInputHandler extends MouseInputHandler {
		
		public void mouseDragged(MouseEvent e) {
//			int row = table.rowAtPoint(e.getPoint());
//			if( table.isRowSelected(row)) return;

			//super.mouseDragged(e);
		}
		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
		public void mouseClicked(MouseEvent e) {
			//super.mouseClicked(e);
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
		 */
		public void mousePressed(MouseEvent e) {
			//super.mousePressed(e);
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
		 */
		public void mouseReleased(MouseEvent e) {
			super.mouseReleased(e);
		}

	}
}
