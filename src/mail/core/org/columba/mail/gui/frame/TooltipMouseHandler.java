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
package org.columba.mail.gui.frame;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractButton;
import javax.swing.Action;

import org.columba.core.gui.statusbar.StatusBar;

/**
 * @author Timo Stich (tstich@users.sourceforge.net)
 * 
 */
public class TooltipMouseHandler extends MouseAdapter {

	private StatusBar statusBar;

	/**
	 * Constructor for MouseHandler.
	 */
	public TooltipMouseHandler(StatusBar statusBar) {
		super();

		this.statusBar = statusBar;
	}

	public void mouseEntered(MouseEvent evt) {
		if (evt.getSource() instanceof AbstractButton) {
			AbstractButton button = (AbstractButton) evt.getSource();
			Action action = button.getAction(); // getAction is new in JDK 1.3
			if (action != null) {
				String message =
					(String) action.getValue(Action.LONG_DESCRIPTION);
				statusBar.displayTooltipMessage(message);
			}
		}
	}

}
