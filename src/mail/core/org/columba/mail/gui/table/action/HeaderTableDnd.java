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

import org.columba.core.logging.ColumbaLogger;
import org.columba.mail.gui.table.TableController;

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
	private TableController table;
	private DragSource dragSource;

	public HeaderTableDnd(TableController table) {
		this.table = table;

		dragSource = DragSource.getDefaultDragSource();
		// creating the recognizer is all that?s necessary - it
		// does not need to be manipulated after creation

		dragSource.createDefaultDragGestureRecognizer(table.getView(),
		// component where drag originates
		DnDConstants.ACTION_COPY_OR_MOVE, // actions
		this); // drag gesture listener
	}

	public void dragGestureRecognized(DragGestureEvent e) {
		InputEvent event = e.getTriggerEvent();
		int mod = event.getModifiers();

		if ((mod & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
			//System.out.println("middle button pressed");

			if (table.getView().getSelectedRowCount() > 0) {
				//mainInterface.treeViewer.saveTreePath();
				
				Object[] uids = table.getTableSelectionManager().getOldUids();
				for ( int i=0; i<uids.length; i++ )
				{
					ColumbaLogger.log.debug("uid["+i+"]="+uids[i]);
				}
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
				
				
				table.setSelection(uids);
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