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

package org.columba.addressbook.gui.dialog.importfilter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JDialog;
import javax.swing.JFileChooser;

import org.columba.addressbook.folder.Folder;
import org.columba.addressbook.gui.tree.util.SelectAddressbookFolderDialog;
import org.columba.addressbook.main.AddressbookInterface;
import org.columba.addressbook.util.AddressbookResourceLoader;
import org.columba.core.gui.util.DialogStore;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.gui.util.NotifyDialog;
import org.columba.core.gui.util.wizard.DefaultWizardPanel;
import org.columba.core.main.MainInterface;

public class ImportWizard implements ActionListener
{
	public ListPanel listPanel;
	public SourcePanel sourcePanel;

	private JDialog dialog;
	private File sourceFile;

	AddressbookInterface addressbookInterface;

	private Folder destFolder;
	private Boolean cancel = Boolean.TRUE;

	public ImportWizard(AddressbookInterface addressbookInterface)
	{
		this.addressbookInterface = addressbookInterface;
		destFolder = addressbookInterface.tree.getFolder(101);
		init();
	}

	protected void init()
	{
		dialog = DialogStore.getDialog(AddressbookResourceLoader.getString("dialog","importwizard","dialog_title"));
		String panelTitle=AddressbookResourceLoader.getString("dialog","importwizard","panel_title");
		listPanel = new ListPanel(
				dialog,	this,
				panelTitle,
				AddressbookResourceLoader.getString("dialog","importwizard","listpanel_description"),
				ImageLoader.getImageIcon("stock_preferences.png"),
				true);

		sourcePanel =
			new SourcePanel(
				dialog,	this,
				panelTitle,
				AddressbookResourceLoader.getString("dialog","importwizard","sourcepanel_description"),
				ImageLoader.getImageIcon("stock_preferences.png"),
				true);

		//listPanel.setNext(sourcePanel);
		//sourcePanel.setPrev(listPanel);

		/*
		progressPanel =
			new ProgressPanel(
				dialog,
				this,
				"Import Mailbox",
				"Choose destination folder",
				ImageLoader.getImageIcon("preferences", "Preferences24"),
				true);
		progressPanel.setPrev(sourcePanel);
		sourcePanel.setNext(progressPanel);
		*/

		setPanel(listPanel);

		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}

	public void setPanel(DefaultWizardPanel panel)
	{
		dialog.getContentPane().add(panel);
		dialog.validate();
		dialog.pack();
	}

	public void actionPerformed(ActionEvent e)
	{
		String action = e.getActionCommand();

		if (action.equals("FINISH"))
		{
			if (sourceFile == null)
			{
				NotifyDialog dialog = new NotifyDialog();
				dialog.showDialog(AddressbookResourceLoader.getString("dialog","importwizard","error_no_sourcefile"));
			}
			else
			{
				dialog.setVisible(false);
				finish();
			}
		}
		else if (action.equals("SOURCE"))
		{
			JFileChooser fc = new JFileChooser();
			if (fc.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION)
			{
				sourceFile = fc.getSelectedFile();
				//this is where a real application would open the file.
				sourcePanel.setSource(sourceFile.toString());
			}
		}
		else if (action.equals("DESTINATION"))
		{
			SelectAddressbookFolderDialog d = MainInterface.addressbookTreeModel.getSelectAddressbookFolderDialog();
			if (d.success())
			{
				destFolder = d.getSelectedFolder();
				sourcePanel.setDestination( destFolder.getName() );

				/*
				String path = destFolder.getTreePath();
				sourcePanel.setDestination(path);
				*/
			}
		}
	}

	public void finish()
	{
		// FIXME
		/*
		String className = listPanel.getSelection();

		DefaultAddressbookImporter importer = null;

		Class actClass = null;

		try
		{

			actClass =
				Class.forName("org.columba.modules.addressbook.folder.importfilter." + className + "Importer");

			importer = (DefaultAddressbookImporter) actClass.newInstance();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return;
		}

		importer.init();
		importer.setDestinationFolder(destFolder);
		importer.setSourceFile(sourceFile);
		importer.setAddressbookInterface( addressbookInterface );
		importer.register(MainInterface.taskManager);
		MainInterface.taskManager.register(importer, 30);
		importer.start();
		*/
	}
}
