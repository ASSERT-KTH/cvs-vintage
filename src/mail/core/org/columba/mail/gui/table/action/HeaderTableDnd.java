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

import java.awt.datatransfer.StringSelection;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.InputEvent;

import javax.swing.JTable;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

public class HeaderTableDnd
	implements DragGestureListener, DragSourceListener {
	private JTable table;
	private DragSource dragSource;

	public HeaderTableDnd(JTable table) {
		this.table = table;

		dragSource = DragSource.getDefaultDragSource();
		// creating the recognizer is all that?s necessary - it
		// does not need to be manipulated after creation

		dragSource.createDefaultDragGestureRecognizer(table,
		// component where drag originates
		DnDConstants.ACTION_COPY_OR_MOVE, // actions
		this); // drag gesture listener
	}

	public void dragGestureRecognized(DragGestureEvent e) {
		InputEvent event = e.getTriggerEvent();
		int mod = event.getModifiers();

		if ((mod & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
			//System.out.println("middle button pressed");

			if (table.getSelectedRowCount() > 0) {
				//mainInterface.treeViewer.saveTreePath();
				//messageNodes = getSelectedNodes();
				if ((mod & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK) {

					//setSelection( getUidList() );
					e.startDrag(DragSource.DefaultCopyDrop, // cursor
					new StringSelection("message"), // transferable
					this); // drag source listener

				} else {

					e.startDrag(DragSource.DefaultMoveDrop, // cursor
					new StringSelection("message"), // transferable
					this); // drag source listener

				}

				//setSelection( oldMessageNodes );

			}

		}

		//System.out.println("draggesturerecognized");
	}

	public void dragDropEnd(DragSourceDropEvent e) {
		System.out.println("dragdropend");
	}

	public void dragEnter(DragSourceDragEvent e) {
		System.out.println("start enter");
	}

	public void dragExit(DragSourceEvent e) {
		System.out.println("dragexit");
	}
	public void dragOver(DragSourceDragEvent e) {
		System.out.println("dragover");
	}
	public void dropActionChanged(DragSourceDragEvent e) {
		System.out.println("dragactioncahnged");
	}
}