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

package org.columba.addressbook.gui.tree.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;

import org.columba.addressbook.config.FolderItem;
import org.columba.addressbook.folder.Folder;

public class SelectAddressbookFolderDialog
	extends JDialog
	implements ActionListener, TreeSelectionListener
{
	private String name;

	//private MainInterface mainInterface;

	private boolean bool = false;

	//public SelectFolderTree tree;

	private JTree tree;

	private JButton[] buttons;

	//private TreeView treeViewer;

	private Folder selectedFolder;

	private JFrame frame;

	private TreeModel model;

	public SelectAddressbookFolderDialog(JFrame frame, TreeModel model)
	{
		super(frame, "Select Folder", true);

		this.model = model;

		//this.mainInterface = mainInterface;
		//this.treeViewer = treeViewer;
		this.frame = frame;

		name = new String("name");

		init();
	}

	public void init()
	{
		buttons = new JButton[3];

		JLabel label2 = new JLabel("Select Folder");

		buttons[0] = new JButton("Cancel");
		buttons[0].setActionCommand("CANCEL");
		buttons[0].setDefaultCapable(true);
		buttons[1] = new JButton("Ok");
		buttons[1].setEnabled(true);
		buttons[1].setActionCommand("OK");
		buttons[2] = new JButton("New Subfolder...");
		buttons[2].setActionCommand("NEW");
		buttons[2].setEnabled(false);

		getRootPane().setDefaultButton(buttons[1]);

		//tree = new SelectFolderTree( mainInterface, mainInterface.config.getFolderConfig().getRootNode()  );
		//tree.getTree().addTreeSelectionListener( this );

		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.setLayout(new BorderLayout());

		getContentPane().setLayout(new BorderLayout());

		//getContentPane().setLayout( new BoxLayout( getContentPane() , BoxLayout.Y_AXIS ) );

		//getContentPane().add(  Box.createRigidArea( new java.awt.Dimension(0,10) )  );

		JPanel centerPanel = new JPanel();
		centerPanel.setBorder(
			javax.swing.BorderFactory.createTitledBorder(
				javax.swing.BorderFactory.createEtchedBorder(),
				" Choose Folder "));
		centerPanel.setLayout(new BorderLayout());

		tree = new JTree(model);
		tree.putClientProperty("JTree.lineStyle", "Angled");
		tree.setShowsRootHandles(true);
		tree.setRootVisible(false);
		tree.addTreeSelectionListener(this);
		tree.setCellRenderer( new AddressbookTreeCellRenderer(true) );
		
		//FolderTreeCellRenderer renderer = new FolderTreeCellRenderer( true );
		//tree.setCellRenderer(renderer);

		JPanel treePanel = new JPanel();
		treePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		treePanel.setLayout(new BorderLayout());
		JScrollPane treePane = new JScrollPane(tree);
		treePanel.add(treePane, BorderLayout.CENTER);
		centerPanel.add(treePanel, BorderLayout.CENTER);

		panel.add(centerPanel, BorderLayout.CENTER);

		//getContentPane().add(  Box.createRigidArea( new java.awt.Dimension(0,10) )  );

		JPanel lowerpanel = new JPanel();
		lowerpanel.setLayout(new BoxLayout(lowerpanel, BoxLayout.X_AXIS));
		lowerpanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

		lowerpanel.add(Box.createHorizontalGlue());
		//lowerpanel.add(  Box.createRigidArea( new java.awt.Dimension(20,0) )  );
		lowerpanel.add(buttons[0]);
		lowerpanel.add(Box.createRigidArea(new java.awt.Dimension(10, 0)));
		lowerpanel.add(Box.createHorizontalGlue());
		lowerpanel.add(buttons[2]);
		lowerpanel.add(Box.createHorizontalGlue());
		lowerpanel.add(Box.createRigidArea(new java.awt.Dimension(10, 0)));
		lowerpanel.add(buttons[1]);
		//lowerpanel.add(  Box.createRigidArea( new java.awt.Dimension(20,0) )  );
		lowerpanel.add(Box.createHorizontalGlue());

		panel.add(lowerpanel, BorderLayout.SOUTH);

		getContentPane().add(panel, BorderLayout.CENTER);

		//getContentPane().add(  Box.createRigidArea( new java.awt.Dimension(0,10) )  );

		for (int i = 0; i < 3; i++)
		{
			buttons[i].addActionListener(this);
		}

		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	public boolean success()
	{
		return bool;
	}

	public Folder getSelectedFolder()
	{
		return selectedFolder;
	}

	public int getUid()
	{
		/*
		  FolderTreeNode node = tree.getSelectedNode();
		
		  FolderItem item = node.getFolderItem();
		*/
		return 101;

		//return item.getUid();

	}

	public void actionPerformed(ActionEvent e)
	{
		String action = e.getActionCommand();
		if (action.equals("OK"))
		{
			//name = textField.getText();

			bool = true;
			dispose();
		}
		else if (action.equals("CANCEL"))
		{
			bool = false;
			dispose();
		}
		else if (action.equals("NEW"))
		{
			/*
			EditFolderDialog dialog = treeViewer.getEditFolderDialog( "New Folder" );
			dialog.showDialog();
			
			String name;
			
			if ( dialog.success() == true )
			{
			      // ok pressed
			    name = dialog.getName();
			}
			else
			{
			      // cancel pressed
			    return;
			}
			
			treeViewer.getFolderTree().addUserFolder( getSelectedFolder(), name );
			
			//TreeNodeEvent updateEvent2 = new TreeNodeEvent( getSelectedFolder(), TreeNodeEvent.STRUCTURE_CHANGED );
			//treeViewer.mainInterface.crossbar.fireTreeNodeChanged(updateEvent2);
			
			*/

		}
	}

	/******************************* tree selection listener ********************************/

	public void valueChanged(TreeSelectionEvent e)
	{

		Folder node = (Folder) tree.getLastSelectedPathComponent();
		if (node == null)
			return;

		selectedFolder = node;

		FolderItem item = node.getFolderItem();

		/*
		if ( item.getType().equals("root") )
			buttons[1].setEnabled(false);
		else
			buttons[1].setEnabled(true);
		*/
		
		
		/*
		if ( item.isAddAllowed() )
		{
		    buttons[1].setEnabled( true );
		}
		else
		{
		    buttons[1].setEnabled( false );
		}
		
		if ( item.isSubfolderAllowed() )
		{
		    buttons[2].setEnabled( true );
		}
		else
		{
		    buttons[2].setEnabled( false );
		}
		*/

	}
}
