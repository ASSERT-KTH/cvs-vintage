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
package org.columba.mail.gui.table;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import org.columba.core.gui.util.AscendingIcon;
import org.columba.core.gui.util.DescendingIcon;
import org.columba.mail.gui.table.model.TableModelSorter;

/**
 * @author fdietz
 *
 * MouseListener for selecting columns with the left mouse
 * to change the sorting order.
 * 
 * 
 * Its also responsible for changing the icon in the renderer
 * 
 * 
 */
public class TableHeaderMouseListener extends MouseAdapter {

	private TableView view;
	private TableModelSorter sorter;
	private SortingStateObservable observable;
	
	/**
	 * 
	 */
	public TableHeaderMouseListener(TableView view, TableModelSorter sorter) {
		this.view = view;
		this.sorter = sorter;
		
		this.observable = sorter.getSortingStateObservable();
		
		JTableHeader th = view.getTableHeader();
		th.addMouseListener(this);
	}

	public void mouseClicked(MouseEvent e) {
		TableColumnModel columnModel = view.getColumnModel();
		int viewColumn = columnModel.getColumnIndexAtX(e.getX());
		int column = view.convertColumnIndexToModel(viewColumn);

		if (column != -1) {

			ImageIcon icon = null;
			if (sorter.getSortingOrder() == true)
				icon = new AscendingIcon();
			else
				icon = new DescendingIcon();

			// disable every icon
			// -> set appropriate icon for selected column
			for (int i = 0; i < columnModel.getColumnCount(); i++) {
				JLabel renderer =
					(JLabel) columnModel.getColumn(i).getHeaderRenderer();
				if (i == column)
					renderer.setIcon(icon);
				else
					renderer.setIcon(null);
			}

			// repaint table header
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					view.getTableHeader().repaint();

				}
			});


			// notify the model to sort the table
			sorter.sort(column);
			
			
			// notify observers
			observable.setSortingState(sorter.getSortingColumn(), sorter.getSortingOrder());
			
			
		}

	}
}
