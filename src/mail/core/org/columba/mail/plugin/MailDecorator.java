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
package org.columba.mail.plugin;

import org.columba.core.xml.XmlElement;
import org.columba.mail.config.MailConfig;
import org.columba.mail.gui.composer.ComposerController;
import org.columba.mail.gui.tree.util.SelectFolderDialog;
import org.columba.core.main.MainInterface;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MailDecorator {

	/**
	 * Constructor for MailDecorator.
	 */
	public MailDecorator() {
		super();
	}

	public static XmlElement getConfigElement(String configName) {
		XmlElement root = MailConfig.get(configName);

		return root;
	}
	
	public static void openComposer()
	{
		ComposerController c = new ComposerController();
		c.showComposerWindow();
	}
	
	public SelectFolderDialog getSelectFolderDialog()
	{
		return MainInterface.treeModel.getSelectFolderDialog();
	}
	
}
