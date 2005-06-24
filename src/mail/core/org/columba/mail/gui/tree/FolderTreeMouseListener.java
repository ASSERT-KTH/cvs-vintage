// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.gui.tree;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import org.columba.mail.gui.tree.action.ViewHeaderListAction;

/**
 * Mouse mouselistener responsible for opening the context menu.
 * 
 * 
 * @author fdietz
 */
public class FolderTreeMouseListener extends MouseAdapter {
	private TreeController treeController;

	public FolderTreeMouseListener(TreeController t) {
		this.treeController = t;

	}

	protected JPopupMenu getPopupMenu() {
		return treeController.getPopupMenu();
	}

	// Use PopUpTrigger in both mousePressed and mouseReleasedMethods due to
	// different handling of *nix and windows
	public void mousePressed(MouseEvent e) {
		maybeShowPopup(e);
	}

	public void mouseReleased(MouseEvent e) {
		maybeShowPopup(e);
	}

	public void mouseClicked(MouseEvent event) {
	}

	private void maybeShowPopup(MouseEvent e) {
		final MouseEvent event = e;

		if (e.isPopupTrigger()) {
			Point point = e.getPoint();
			TreePath path = treeController.getView().getClosestPathForLocation(
					point.x, point.y);

			treeController.getView().removeTreeSelectionListener(treeController);
			
			treeController.getView().setSelectionPath(path);

			treeController.getView().addTreeSelectionListener(treeController);
			
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					getPopupMenu().show(event.getComponent(), event.getX(),
							event.getY());
				}
			});

		}
	}
}