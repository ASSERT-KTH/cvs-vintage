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

package org.columba.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.Enumeration;

import javax.swing.JFrame;
import javax.swing.JProgressBar;

import org.columba.addressbook.main.AddressbookExitWorker;
import org.columba.core.config.Config;
import org.columba.core.util.SwingWorker;
import org.columba.mail.folder.Folder;
import org.columba.mail.folder.FolderTreeNode;
import org.columba.mail.folder.imap.IMAPFolder;
import org.columba.mail.folder.virtual.VirtualFolder;

public class ExitWorker extends SwingWorker {
	private JProgressBar bar;
	private int value = 0;
	private int count = 0;

	public Object construct() {
		showDialog();

		/*
		MainInterface.mainFrame.saveWindowPosition();
		MainInterface.headerTableViewer.saveColumnConfig();
		*/

		MainInterface.frameController.getView().setVisible(false);

		Config.save();

		initProgressBar();

		// FIXME
		/*
		savePop3();
		*/
		saveAllFolders();

		AddressbookExitWorker w =
			new AddressbookExitWorker(MainInterface.addressbookInterface);
		w.saveAllAddressbooks();

		System.exit(0);

		//unregister();

		return null;
	}

	public void savePop3() throws Exception{
		/*
		POP3Server server;
		for (int i = 0; i < MainInterface.popServerCollection.count(); i++) {
			server = MainInterface.popServerCollection.get(i);
			server.save();
			bar.setValue(value++);
		}
		*/
	}

	public void showDialog() {
		JFrame dialog = new JFrame("Saving Folders...");
		//JButton label = new JButton("Saving Folders...");
		//dialog.getContentPane().setLayout( new BorderLayout() );
		//dialog.getContentPane().add( label, BorderLayout.CENTER );
		//dialog.getContentPane().add( label );
		bar = new JProgressBar();
		bar.setValue(0);
		bar.setStringPainted(true);

		dialog.getContentPane().add(bar, BorderLayout.CENTER);
		dialog.pack();

		java.awt.Dimension dim = new Dimension(300, 50);
		dialog.setSize(dim);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		dialog.setLocation(
			screenSize.width / 2 - dim.width / 2,
			screenSize.height / 2 - dim.height / 2);

		dialog.setVisible(true);

		/*
		dialog.addWindowListener(
		        new WindowAdapter()
		            {
		                public void windowClosing(WindowEvent e)
		                    {
		
		                        System.exit(0);
		
		                    }
		            }
		        );
		*/
	}

	protected void initProgressBar() {
		Folder rootFolder = (Folder) MainInterface.treeModel.getRoot();
		int c = getFolderCount(rootFolder);

		int result = c + MainInterface.popServerCollection.count();
		bar.setMaximum(result);

		//System.out.println("folder count: "+ result );

		bar.setValue(value++);
	}

	protected int getFolderCount(Folder parent) {

		for (Enumeration e = parent.children(); e.hasMoreElements();) {
			Folder child = (Folder) e.nextElement();

			getFolderCount(child);

			if (child.getChanged() == true)
				count++;
		}

		return count;
	}

	public void saveAllFolders() {

		FolderTreeNode rootFolder =
			(FolderTreeNode) MainInterface.treeModel.getRoot();

		//timer.start();
		saveFolder(rootFolder);

	}

	public void saveFolder(FolderTreeNode parentFolder) {

		int count = parentFolder.getChildCount();
		FolderTreeNode child;
		FolderTreeNode folder;

		for (Enumeration e = parentFolder.children(); e.hasMoreElements();) {

			child = (FolderTreeNode) e.nextElement();

			if (child != null) {
				if (child instanceof VirtualFolder) {
				}
				if (child instanceof IMAPFolder) {
					/*
					IMAPFolder imapFolder = (IMAPFolder) child;

					if (imapFolder.getChanged() == true) {
						bar.setValue(value++);
						try {
							//child.expunge();
							imapFolder.save();
						} catch (Exception ex) {
							System.out.println(
								"Error while saving folder: "
									+ ex.getMessage());
							ex.printStackTrace();
						}

					}
					*/
				} else {
					/*
					try {
						child.save();
					} catch (Exception ex) {
						System.out.println(
							"Error while saving folder: " + ex.getMessage());
						ex.printStackTrace();
					}
					*/

					/*
					if (child.getChanged() == true)
					{
						bar.setValue(value++);
					
						
						try
						{
							child.unmarkRecent();
						}
						catch (Exception ex)
						{
							ex.printStackTrace();
						}
						
					
						Object[] uids = child.getUids();
					
						for (int i = 0; i < uids.length; i++)
						{
					
							try
							{
								Message message = child.getMessage(uids[i]);
								Flags flags = message.getFlags();
								boolean expunged = flags.getDeleted();
					
								if (expunged == true)
								{
									Object recents[] = new Object[1];
									recents[0] = uids[i];
									// FIXME
									
									//child.workerViewRemove(recents);
									//child.workerRemove(uids[i]);
									
								}
							}
							catch (Exception ex)
							{
								//System.out.println("Exception: "+ ex.getMessage() );
								ex.printStackTrace();
							}
					
						}
					
						try
						{
							child.save();
						}
						catch (Exception ex)
						{
							System.out.println("Error while saving folder: " + ex.getMessage());
							ex.printStackTrace();
						}
					
					}
					*/
				}

			}

			saveFolder(child);

		}

	}

}