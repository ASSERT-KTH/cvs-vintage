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
package org.columba.mail.gui.config.mailboximport;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JDialog;
import javax.swing.JFileChooser;

import org.columba.core.gui.util.DialogStore;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.gui.util.NotifyDialog;
import org.columba.core.gui.util.wizard.DefaultWizardPanel;
import org.columba.core.main.MainInterface;
import org.columba.mail.folder.Folder;
import org.columba.mail.folder.mailboximport.DefaultMailboxImporter;

public class ImportWizard implements ActionListener
{
	public ListPanel listPanel;
	public SourcePanel sourcePanel;
	//public ProgressPanel progressPanel;

	private JDialog dialog;

	private File sourceFile;

	private Folder destFolder;

	private Boolean cancel = Boolean.TRUE;

	public ImportWizard()
	{
		destFolder = (Folder) MainInterface.treeModel.getFolder(101);

		init();
	}

	public void init()
	{
		dialog = DialogStore.getDialog("Import Mailbox...");

		listPanel =
			new ListPanel(
				dialog,
				this,
				"Import Mailbox",
				"Choose mailbox type",
				ImageLoader.getSmallImageIcon("stock_preferences.png"),
				true);

		sourcePanel =
			new SourcePanel(
				dialog,
				this,
				"Import Mailbox",
				"Choose source/destination folder",
				ImageLoader.getSmallImageIcon("stock_preferences.png"),
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

		/*
		java.awt.Dimension dim = dialog.getSize();

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		dialog.setLocation(
			screenSize.width / 2 - dim.width / 2,
			screenSize.height / 2 - dim.height / 2);
		*/
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
				dialog.showDialog("You have to specify a source File!");

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

			int returnVal = fc.showOpenDialog(dialog);

			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				sourceFile = fc.getSelectedFile();
				//this is where a real application would open the file.
				sourcePanel.setSource(sourceFile.toString());
			}

		}
		else if (action.equals("DESTINATION"))
		{
			/*
			SelectFolderDialog dialog =
				MainInterface
					.frameController
					.treeController
					.getSelectFolderDialog();

			if (dialog.success()) {

				destFolder = dialog.getSelectedFolder();
				String path = destFolder.getTreePath();

				sourcePanel.setDestination(path);
			}
			*/
			

		}
	}

	public void finish()
	{
		// FIXME
		
		
		String className = listPanel.getSelection();

		DefaultMailboxImporter importer = null;

		Class actClass = null;

		try
		{

			actClass =
				Class.forName("org.columba.mail.folder.mailboximport." + className + "Importer");

			importer = (DefaultMailboxImporter) actClass.newInstance();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return;
		}

		importer.init();
		importer.setDestinationFolder(destFolder);
		importer.setSourceFile(sourceFile);
		/*
		importer.register(MainInterface.taskManager);
		MainInterface.taskManager.register(importer, 30);
		importer.start();
        destFolder.workerUpdate();
        */
        
	}

}
