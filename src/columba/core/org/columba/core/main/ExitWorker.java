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
package org.columba.core.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JProgressBar;

import org.columba.core.util.SwingWorker;

public class ExitWorker extends SwingWorker {
	private JProgressBar bar;
	private int value = 0;
	private int count = 0;

	public Object construct() {
		showDialog();

		//MainInterface.shutdownManager.shutdown();

		//initProgressBar();

		

		return null;
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

		
	}

	/*
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
	*/
	
	
	/**
	 * @return
	 */
	public JProgressBar getBar() {
		return bar;
	}

}