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

package org.columba.mail.gui.table.action;

import javax.swing.table.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.TooManyListenersException;
import java.awt.event.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import javax.swing.plaf.*;
import java.util.EventObject;

import javax.swing.text.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @version 	1.0
 * @author
 */
public class ColumbaMouseInputHandler implements MouseInputListener
{

	// Component receiving mouse events during editing.
	// May not be editorComponent.
	private Component dispatchComponent;
	private boolean selectedOnPress;

	JTable table;

	//  The Table's mouse listener methods.

	public ColumbaMouseInputHandler(JTable table)
	{
		this.table = table;
	}

	public void mouseClicked(MouseEvent e)
	{
		System.out.println("mouse clicked");
		
		if (e.isConsumed())
		{
			selectedOnPress = false;
			return;
		}
		selectedOnPress = true;
		
		adjustFocusAndSelection(e);
		

	}

	private void setDispatchComponent(MouseEvent e)
	{
		Component editorComponent = table.getEditorComponent();
		Point p = e.getPoint();
		Point p2 = SwingUtilities.convertPoint(table, p, editorComponent);
		dispatchComponent =
			SwingUtilities.getDeepestComponentAt(editorComponent, p2.x, p2.y);
	}

	private boolean repostEvent(MouseEvent e)
	{
		// Check for isEditing() in case another event has
		// caused the editor to be removed. See bug #4306499.
		if (dispatchComponent == null || !table.isEditing())
		{
			return false;
		}
		MouseEvent e2 = SwingUtilities.convertMouseEvent(table, e, dispatchComponent);
		dispatchComponent.dispatchEvent(e2);
		return true;
	}

	private void setValueIsAdjusting(boolean flag)
	{
		table.getSelectionModel().setValueIsAdjusting(flag);
		table.getColumnModel().getSelectionModel().setValueIsAdjusting(flag);
	}

	private boolean shouldIgnore(MouseEvent e)
	{
		return e.isConsumed()
			|| (!(SwingUtilities.isLeftMouseButton(e) && table.isEnabled()));
	}

	public void mousePressed(MouseEvent e)
	{
		
		System.out.println("mouse pressed");
		
		if ( e.isConsumed() ) return;
		
		/*
		if (e.isConsumed())
		{
			selectedOnPress = false;
			return;
		}
		
		selectedOnPress = true;
		
		
			
		adjustFocusAndSelection(e);
		*/	
		
		
	}

	void adjustFocusAndSelection(MouseEvent e)
	{
		if (shouldIgnore(e))
		{
			return;
		}

		Point p = e.getPoint();
		int row = table.rowAtPoint(p);
		int column = table.columnAtPoint(p);
		// The autoscroller can generate drag events outside the Table's range.
		if ((column == -1) || (row == -1))
		{
			return;
		}

		if (table.editCellAt(row, column, e))
		{
			setDispatchComponent(e);
			repostEvent(e);
		}
		else if (table.isRequestFocusEnabled())
		{
			table.requestFocus();
		}

		CellEditor editor = table.getCellEditor();
		if (editor == null || editor.shouldSelectCell(e))
		{
			boolean adjusting = (e.getID() == MouseEvent.MOUSE_PRESSED) ? true : false;
			setValueIsAdjusting(adjusting);
			table.changeSelection(row, column, e.isControlDown(), e.isShiftDown());
		}
	}

	public void mouseReleased(MouseEvent e)
	{
		System.out.println("mouse released");
		if (selectedOnPress)
		{
			if (shouldIgnore(e))
			{
				return;
			}

			repostEvent(e);
			dispatchComponent = null;
			setValueIsAdjusting(false);
		}
		else
		{
			adjustFocusAndSelection(e);
		}
		

	}

	public void mouseEntered(MouseEvent e)
	{
	}

	public void mouseExited(MouseEvent e)
	{
	}

	//  The Table's mouse motion listener methods.

	public void mouseMoved(MouseEvent e)
	{
		//System.out.println("mouse moved");
	}

	public void mouseDragged(MouseEvent e)
	{
		System.out.println("mouse draggged");
		
		if (shouldIgnore(e))
		{
			
			return;
		}

		repostEvent(e);

		CellEditor editor = table.getCellEditor();
		if (editor == null || editor.shouldSelectCell(e))
		{
			Point p = e.getPoint();
			int row = table.rowAtPoint(p);
			int column = table.columnAtPoint(p);
			// The autoscroller can generate drag events outside the Table's range.
			if ((column == -1) || (row == -1))
			{
				return;
			}

			int rowCount = table.getSelectedRowCount();
			if (rowCount == 0)
			{
				table.addRowSelectionInterval(row, row);
			}
			else if (rowCount == 1)
			{
				int selectedRow = table.getSelectedRow();
				if (selectedRow != row)
				{
					table.removeRowSelectionInterval(selectedRow, selectedRow);
					table.addRowSelectionInterval(row, row);
				}
			}
			//table.changeSelection(row, column, false, true);
		}

	}
}