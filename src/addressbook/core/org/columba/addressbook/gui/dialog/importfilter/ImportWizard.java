// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

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
			SelectAddressbookFolderDialog d = addressbookInterface.tree.getSelectAddressbookFolderDialog();
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
