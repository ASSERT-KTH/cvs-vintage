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

package org.columba.mail.gui.tree;

import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ToolTipManager;

import org.columba.mail.folder.Folder;

/**
 * this class does all the dirty work for the TreeController
 */
public class TreeView extends JTree 
//, TreeNodeChangeListener
{

	//protected AdapterNode rootNode;

	//public FolderModel treeModel;

	// protected JTree tree;

	private String selectedLeaf = new String();

	//private Folder selectedFolder;

	private JTextField textField;

	//private TreeModel model;

	public TreeView(TreeModel model) {
		super(model);
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
