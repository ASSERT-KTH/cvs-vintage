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

import java.util.Enumeration;

import org.columba.core.gui.FrameController;
import org.columba.core.gui.MultiViewFrameModel;
import org.columba.core.xml.XmlElement;
import org.columba.mail.gui.composer.ComposerModel;
import org.columba.mail.gui.table.TableChangedEvent;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MailFrameModel extends MultiViewFrameModel {

	private ComposerModel composerModel;	

	public MailFrameModel(XmlElement viewList) {
		super(viewList);
		
		composerModel = new ComposerModel();
	}

	public FrameController createInstance(String id) {
		return new MailFrameController(id, this);
	}

	protected XmlElement createDefaultConfiguration(String key) {
		XmlElement child = super.createDefaultConfiguration(key);

		XmlElement toolbars = new XmlElement("toolbars");
		toolbars.addAttribute("show_main", "true");
		toolbars.addAttribute("show_filter", "true");
		toolbars.addAttribute("show_folderinfo", "true");
		child.addElement(toolbars);
		XmlElement splitpanes = new XmlElement("splitpanes");
		splitpanes.addAttribute("main", "200");
		splitpanes.addAttribute("header", "200");
		splitpanes.addAttribute("attachment", "100");
		child.addElement(splitpanes);

		return child;
	}

	/*
	public void saveAll() {
		
	}
	*/
	/*
	protected void saveAndExit() {
		try {
			Config.save();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		MainInterface.popServerCollection.saveAll();

		saveAllFolders();

		System.exit(0);
	}
	*/

	

	public void tableChanged(TableChangedEvent ev) throws Exception {
		for (Enumeration e = controllers.keys(); e.hasMoreElements();) {
			String key = (String) e.nextElement();

			MailFrameController frame =
				(MailFrameController) controllers.get(key);
			frame.tableController.tableChanged(ev);
		}

	}

	public void updatePop3Menu() {
		for (Enumeration e = controllers.keys(); e.hasMoreElements();) {
			String key = (String) e.nextElement();

			MailFrameController frame =
				(MailFrameController) controllers.get(key);
			frame.getMenu().updatePopServerMenu();
		}
	}

	/**
	 * @return ComposerModel
	 */
	public ComposerModel getComposerModel() {
		return composerModel;
	}

}
