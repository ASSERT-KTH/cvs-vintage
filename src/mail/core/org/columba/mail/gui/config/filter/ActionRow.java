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


package org.columba.mail.gui.config.filter;

import org.columba.mail.config.*;
import org.columba.mail.message.*;
import org.columba.main.*;
import org.columba.mail.gui.tree.util.*;
import org.columba.mail.folder.*;
import org.columba.mail.filter.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.border.*;

import java.awt.*;
import java.awt.event.*;


import java.util.Vector;

public class ActionRow extends DefaultActionRow implements ActionListener
{

    private JButton treePathButton;

    public ActionRow( ActionList list, FilterAction action )
    {
        super( list, action );

    }

    protected void updateComponents( boolean b )
    {
        super.updateComponents( b );

        if ( b )
        {
                int uid = filterAction.getUid();
                Folder folder = (Folder) MainInterface.treeModel.getFolder( uid );
                String treePath = folder.getTreePath();

                treePathButton.setText( treePath );
        }
        else
        {
                String treePath = treePathButton.getText();
                TreeNodeList list = new TreeNodeList( treePath );
                Folder folder = (Folder) MainInterface.treeModel.getFolder( list );
                int uid = folder.getUid();
                filterAction.setUid( uid );
        }

    }


    public void initComponents()
    {
        super.initComponents();

        /*
        removeAll();

        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gridbag);


        actionComboBox = new JComboBox();
        actionComboBox.addItem( "move" );
	actionComboBox.addItem( "copy" );
	actionComboBox.addItem( "markasread" );
	actionComboBox.addItem( "delete" );

        c.fill = GridBagConstraints.NONE;
        c.weightx = 1.0;
        c.insets = new Insets(2,5,2,5);
        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = 1 ;

        gridbag.setConstraints(actionComboBox, c);
        add( actionComboBox );


        String name = filterAction.getAction();

        if ( (name.equals("move") ) || ( name.equals("copy") ) )
        {
        */

            treePathButton = new JButton();
            treePathButton.addActionListener( this );
            treePathButton.setActionCommand( "TREEPATH" );
            c.gridx = 1;
            gridbag.setConstraints( treePathButton, c);
            add( treePathButton );
            /*
        }
        else
        {

            treePathButton = null;
        }
        */

        /*
        c.gridx = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        Box box = Box.createHorizontalBox();
        gridbag.setConstraints( box, c );

        add( box );
        */



        //validate();
        //repaint();
    }


    public void actionPerformed(ActionEvent e)
    {
	String action  = e.getActionCommand();


	if ( action.equals("TREEPATH") )
        {
            SelectFolderDialog dialog = MainInterface.frameController.treeController.getSelectFolderDialog();
            dialog.setSize( new Dimension( 300,430 ) );


            if ( dialog.success() )
            {
                Folder folder = dialog.getSelectedFolder();


                String treePath = folder.getTreePath();

                treePathButton.setText( treePath );
            }

        }
	else if ( action.equals("N") )
        {
        }
    }

    /*
    public void itemStateChanged(ItemEvent e)
    {
        if (e.getStateChange() == ItemEvent.SELECTED)
        {

            String v = (String) actionComboBox.getSelectedItem();

            String s = filterAction.getAction();
            filterAction.setAction( v );


            if ( ( s.equals("move") )  || ( s.equals("copy") ) )
            {
                if ( ( actionComboBox.getSelectedIndex()==0 )  ||
                     ( actionComboBox.getSelectedIndex()==1 ) )
                {
                }
                else
                {

                    filterAction.removeUidNode();

                    initComponents();

                }


            }
            else if ( ( s.equals("markasread") ) || ( s.equals("delete") ) )
            {

                if ( ( actionComboBox.getSelectedIndex()==2 ) ||
                     ( actionComboBox.getSelectedIndex()==3 ) )
                {

                }
                else
                {


                    filterAction.addUidNode( 101 );
                    initComponents();

                }

            }
            else
            {

            }
        }
    }
    */

}












