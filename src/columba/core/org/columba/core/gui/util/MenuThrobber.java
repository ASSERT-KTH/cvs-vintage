// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.core.gui.util;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.Box;
import javax.swing.JMenuBar;
import javax.swing.JPanel;


/**
 * Utility class to add a throbber into the right-hand side corner
 * of a JMenuBar.
 * 
 * @author fdietz
 */
public class MenuThrobber {

	public static void setThrobber(JMenuBar menuBar) {
		if ( menuBar == null ) throw new IllegalArgumentException("menuBar == null");
		
		Component box = Box.createHorizontalGlue();
		menuBar.add(box);
		
		JPanel throbberPanel = new JPanel();	
		throbberPanel.setLayout(new BorderLayout());
		throbberPanel.add(new ThrobberIcon(), BorderLayout.EAST);
		
		menuBar.add(throbberPanel);
	}
}
