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
package org.columba.mail.gui.tree;

import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.ToolTipManager;

import org.columba.mail.folder.Folder;
import org.columba.mail.gui.frame.MailFrameController;

/**
 * this class does all the dirty work for the TreeController
 */
public class TreeView extends DndTree 
//, TreeNodeChangeListener
{

	//protected AdapterNode rootNode;

	//public FolderModel treeModel;

	// protected JTree tree;

	private String selectedLeaf = new String();

	//private Folder selectedFolder;

	private JTextField textField;

	//private TreeModel model;

	public TreeView(MailFrameController frameController, TreeModel model) {
		super(frameController, model);
		//this.model = model;            

		//tree = new JTree( treeModel );

		ToolTipManager.sharedInstance().registerComponent(this);

		putClientProperty("JTree.lineStyle", "Angled");

		setShowsRootHandles(true);
		setRootVisible(false);

		

		/*
		    //rootFolder.addTreeNodeListener( this );
		    addTreeNodeChangeListener( rootFolder );
		*/

		
		
		
		setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));

		expand();

	}

	public Folder getSelected() {
		return null;
	}

	


	public void expand() {
		int rowCount = getRowCount();

		expandRow(0);
		rowCount = getRowCount();

		expandRow(1);

		repaint();

	}

}
