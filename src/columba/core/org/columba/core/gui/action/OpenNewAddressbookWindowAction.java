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

package org.columba.core.gui.action;

import java.awt.event.ActionEvent;

import org.columba.addressbook.config.AddressbookConfig;
import org.columba.core.action.FrameAction;
import org.columba.core.config.ViewItem;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.gui.frame.FrameModel;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.xml.XmlElement;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class OpenNewAddressbookWindowAction extends FrameAction {

	public OpenNewAddressbookWindowAction(AbstractFrameController controller) {
			super(
				controller,
				"Addressbook Window",
				"Addressbook Window",
				"OPEN_NEW_ADDRESSBOOK_WINDOW",
				null,
				null,
				' ',
				null);
		}

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent evt) {
			
			/*
			 * *20030803, karlpeder* A new way to open adressbook
			 * window - which reads window position from options file.
			 * This fixes bug #778200 since the window now remembers its size
			 */
			////FrameModel.openView("Addressbook");
			
			// get stored view element, or create a new (backward compability!)
			XmlElement view = AddressbookConfig.get("options").
						getElement("/options/gui/view");
			if (view == null) {
				// get root for new view element (= gui element)
				XmlElement gui = AddressbookConfig.get("options").
							getElement("/options/gui");
				if (gui != null) {
					// create new view element 
					view = new XmlElement("view");
					XmlElement window = new XmlElement("window");
					window.addAttribute("width", "640");
					window.addAttribute("height", "480");
					window.addAttribute("maximized", "true");
					view.addElement(window);
					XmlElement toolbars = new XmlElement("toolbars");
					toolbars.addAttribute("main", "true");
					view.addElement(toolbars);
					gui.addElement(view);
				} else {
					// can not create a new view element due to some error !?!
					ColumbaLogger.log.debug("Can not create new view element");
				}
			}
			
			// open view
			if (view == null) {
				// we didn't succeeded in getting/creating view element
				FrameModel.openView("Addressbook");
			} else {
				// open view with view specification
				FrameModel.openView("Addressbook", new ViewItem(view));
			}
		}
}
