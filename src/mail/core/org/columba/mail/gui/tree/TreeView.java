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
import org.columba.mail.gui.tree.util.FolderTreeCellRenderer;

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

		FolderTreeCellRenderer renderer = new FolderTreeCellRenderer(true);
		setCellRenderer(renderer);

		setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));

		expand();

	}

	public Folder getSelected() {
		return null;
	}

	

	/*
	public Folder getRootFolder()
	{
	    return rootFolder;
	}
	*/

	public void expand() {
		int rowCount = getRowCount();

		expandRow(0);
		rowCount = getRowCount();

		expandRow(1);

		repaint();

	}

	/*
	public void addTreeNodeChangeListener( Folder parent )
	{
	    int count = parent.getChildCount();
	
	    for ( int i=0; i<count; i++ )
	    {
	        Folder child = (Folder) parent.getChildAt( i );
	        //child.addTreeNodeListener( this );
	        addTreeNodeChangeListener( child );
	    }
	}
	
	
	
	
	public Folder getSelected()
	{
	    //return selectedFolder;
	    return (Folder) getTree().getLastSelectedPathComponent();
	}
	
	
	public void setSelected( Folder f )
	{
	    TreePath treePath = f.getSelectionTreePath();
	    getTree().getSelectionModel().setSelectionPath( treePath );
	
	
	}
	
	
	
	
	public DefaultTreeModel getModel()
	{
	    return treeModel;
	}
	
	
	
	public JTree getTree()
	    {
	        return tree;
	    }
	*/

	/************************ tree structure methods ********************************/

	/*
	
	
	public Folder findFolder( Folder parentFolder, String str )
	{
	
	int count = parentFolder.getChildCount();
	    Folder child;
	    Folder folder;
	
	    for (Enumeration e = parentFolder.children() ; e.hasMoreElements() ;)
	    {
	        child = (Folder) e.nextElement();
	        //System.out.println( "child: "+child.getName());
	    if ( child.getName().equals( str ) ) return child;
	}
	
	return null;
	}
	
	
	
	public void treeNodeChanged( TreeNodeEvent e )
	{
	
	    int mode = e.getMode();
	
	
	    Folder folder = (Folder) e.getSource();
	    if ( folder == null ) return;
	
	    if ( mode == TreeNodeEvent.UPDATE )
	       getModel().nodeChanged( folder );
	    else if ( mode == TreeNodeEvent.STRUCTURE_CHANGED )
	       getModel().nodeStructureChanged( folder );
	
	}
	
	class FolderTreeCellEditor extends DefaultCellEditor
	{
	
	
	  public FolderTreeCellEditor( JTextField textField )
	  {
	      super( textField );
	      setClickCountToStart( 2 );
	
	  }
	
	  public Component getTreeCellEditorComponent( JTree tree, Object value,
	         boolean isSelected, boolean expanded, boolean leaf, int row )
	  {
	    JTextField textField = (JTextField) editorComponent;
	
	    Folder folder = (Folder) value;
	
	    textField.setText( folder.getName() );
	
	    return textField;
	  }
	}
	
	
	
	public Folder getFolder( int uid )
	{
	    Folder root = getRootFolder();
	
	    for (Enumeration e = root.getPreorderEnumeration() ; e.hasMoreElements() ;)
	    {
	        Folder node = (Folder) e.nextElement();
	
	        FolderItem item = node.getFolderItem();
	        if ( item == null ) continue;
	
	        int id = item.getUid();
	
	        if ( uid == id )
	        {
	            return node;
	        }
	
	    }
	    return null;
	
	}
	
	public Folder getImapFolder( int accountUid )
	{
	
	    Folder root = (Folder) getRootFolder();
	
	    for (Enumeration e = root.getPreorderEnumeration() ; e.hasMoreElements() ;)
	    {
	        Folder node = (Folder) e.nextElement();
	
	        FolderItem item = node.getFolderItem();
	        if ( item == null ) continue;
	
	        if ( item.getType().equals("imaproot") )
	        {
	            int account = item.getAccountUid();
	
	            if ( account == accountUid )
	            {
	                int uid = item.getUid();
	
	                return getFolder( uid );
	            }
	
	        }
	
	
	
	
	    }
	    return null;
	
	}
	
	
	
	public Folder getFolder( TreeNodeList list )
	{
	
	    Folder folder = null;
	
	    Folder parentFolder = getRootFolder() ;
	
	    if ( list == null ) return parentFolder;
	
	     if ( list.count() == 0 )
	     {
	         System.out.println("list count == null ");
	
	         return parentFolder;
	     }
	
	Folder child = parentFolder;
	for ( int i=0; i < list.count(); i++)
	    {
		String str = list.get(i);
	            System.out.println( "str: "+ str );
	            child = findFolder( child, str );
	    }
	
	    return child;
	
	}
	*/

	/**
	 *
	 * return Folder where to save the messages from the specified popserver
	 *
	 *
	 **/

	/*
	public Folder getFolder( POP3Server server )
	{
	  String uidString = server.getAccountItem().getSpecialFoldersItem().getInbox();
	  int uid = new Integer(uidString).intValue();
	
	  //int uid = server.getPopItem().getUid();
	
	  Folder node = getFolder( uid );
	
	  return node;
	}
	*/
}
