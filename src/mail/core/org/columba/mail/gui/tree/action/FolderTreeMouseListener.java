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

package org.columba.mail.gui.tree.action;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import org.columba.mail.gui.tree.TreeController;

public class FolderTreeMouseListener extends MouseAdapter
{
    private TreeController treeController;

    public FolderTreeMouseListener( TreeController t )
    {
        this.treeController = t;
    }

    protected JPopupMenu getPopupMenu()
    {
        return treeController.getPopupMenu();
    }

	// Use PopUpTrigger in both mousePressed and mouseReleasedMethods due to
	// different handling of *nix and windows

    public void mousePressed(MouseEvent e)
    {
         if ( e.isPopupTrigger() )
            {
                java.awt.Point point = e.getPoint();
                TreePath path = treeController.getView().getClosestPathForLocation( point.x, point.y );

                treeController.getView().clearSelection();
                treeController.getView().addSelectionPath( path );

                treeController.getActionListener().changeActions();


                getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
            }

    }

    public void mouseReleased(MouseEvent e)
    {
         if ( e.isPopupTrigger() )
            {
                java.awt.Point point = e.getPoint();
                TreePath path = treeController.getView().getClosestPathForLocation( point.x, point.y );

                treeController.getView().clearSelection();
                treeController.getView().addSelectionPath( path );

                treeController.getActionListener().changeActions();


                getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
            }
    }

    public void mouseClicked(MouseEvent e)
    {
    	if ( SwingUtilities.isLeftMouseButton(e) ) treeController.selectFolder();
    	/*
        if ( e.getClickCount() == 1 )
        {
            treeController.selectFolder();
        }
        else if ( e.getClickCount() == 2 )
        {
            treeController.expandImapRootFolder();
        }
        */

    }

    /*
    private void maybeShowPopup(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            getPopupMenu().show(e.getComponent(),
                       e.getX(), e.getY());
        }
    }
    */
}
