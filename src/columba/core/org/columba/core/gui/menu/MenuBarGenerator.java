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

package org.columba.core.gui.menu;

import java.util.ListIterator;

import javax.swing.JMenuBar;

import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.xml.XmlElement;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class MenuBarGenerator extends AbstractMenuGenerator {

	

	/**
	 * @param path
	 */
	public MenuBarGenerator(FrameMediator frameController, String path) {
		super(frameController, path);

	}

	public void createMenuBar( JMenuBar menuBar) {
		menuBar.removeAll();
		ListIterator it = getMenuRoot().getElements().listIterator();
		while (it.hasNext()) {
			menuBar.add(createMenu((XmlElement) it.next()));
		}
	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.AbstractMenuGenerator#getMenuRoot()
	 */
	public XmlElement getMenuRoot() {

		return xmlFile.getRoot().getElement("menubar");
	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.AbstractMenuGenerator#getRootElementName()
	 */
	public String getRootElementName() {
		return "menubar";
	}

	public void extendMenuFromFile(String path) {
		super.extendMenuFromFile(path);

	}

}
