/*
 * Created on 26.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
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
import org.columba.core.gui.frame.AbstractFrameController;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class AddVCardAction extends FrameAction {

	/**
	 * @param frameController
	 * @param name
	 * @param longDescription
	 * @param actionCommand
	 * @param small_icon
	 * @param big_icon
	 * @param mnemonic
	 * @param keyStroke
	 */
	public AddVCardAction(AbstractFrameController frameController) {
		super(
			frameController,
			AddressbookResourceLoader.getString(
				"menu",
				"mainframe",
				"menu_file_addvcard"),
			AddressbookResourceLoader.getString(
				"menu",
				"mainframe",
				"menu_file_addvcard"),
			"ADD_VCARD",
			null,
			null,
			'0',
			null);

	}
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {

		AddressbookFrameController addressbookFrameController =
			(AddressbookFrameController) frameController;

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
