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

package org.columba.addressbook.gui.action;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.swing.JFileChooser;

import org.columba.addressbook.folder.ContactCard;
import org.columba.addressbook.folder.Folder;
import org.columba.addressbook.gui.frame.AddressbookFrameController;
import org.columba.addressbook.parser.VCardParser;
import org.columba.addressbook.util.AddressbookResourceLoader;
import org.columba.core.action.FrameAction;
import org.columba.core.gui.frame.FrameMediator;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class AddVCardAction extends FrameAction {

	public AddVCardAction(FrameMediator frameController) {
		super(frameController, AddressbookResourceLoader.getString(
			"menu", "mainframe", "menu_file_addvcard"));
		
		// tooltip text
		putValue(SHORT_DESCRIPTION, AddressbookResourceLoader.getString(
			"menu",
                        "mainframe",
                        "menu_file_addvcard").replaceAll("&", ""));
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		AddressbookFrameController addressbookFrameController =
			(AddressbookFrameController) frameMediator;

		Folder destinationFolder =
			(Folder) addressbookFrameController
				.getTree()
				.getView()
				.getSelectedFolder();

		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setMultiSelectionEnabled(true);
		int returnVal = fc.showOpenDialog(addressbookFrameController.getView());

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File[] files = fc.getSelectedFiles();

			for (int i = 0; i < files.length; i++) {
				try {
					StringBuffer strbuf = new StringBuffer();

					BufferedReader in =
						new BufferedReader(new FileReader(files[i]));
					String str;

					while ((str = in.readLine()) != null) {
						strbuf.append(str + "\n");
					}

					in.close();

					ContactCard card = VCardParser.parse(strbuf.toString());

					destinationFolder.add(card);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}

		addressbookFrameController.getTable().getView().setFolder(
			destinationFolder);
	}
}
