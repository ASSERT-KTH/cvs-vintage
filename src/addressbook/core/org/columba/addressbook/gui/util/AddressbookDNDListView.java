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

package org.columba.addressbook.gui.util;

import java.awt.Point;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.image.BufferedImage;
import java.lang.reflect.Array;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.columba.addressbook.folder.HeaderItem;
//import sun.security.krb5.internal.i;
//import sun.security.krb5.internal.crypto.b;


/**
 * @version 	1.0
 * @author
 */
public class AddressbookDNDListView
	extends AddressbookListView
	implements
		DropTargetListener,
		DragSourceListener,
		DragGestureListener,
		ListSelectionListener
{
	/**
	* enables this component to be a dropTarget
	*/

	DropTarget dropTarget = null;

	/**
	 * enables this component to be a Drag Source
	 */
	DragSource dragSource = null;

	boolean acceptDrop = true;

	//private static Object[] headerItems;

	private static AddressbookDNDListView source;

	private HeaderItem[] selection1;
	private HeaderItem[] selection2;

	int index = -1;

	private boolean dndAction = false;

	private BufferedImage _imgGhost; // The 'drag image'
	private Point _ptOffset = new Point();
	// Where, in the drag image, the mouse was clicked

	public AddressbookDNDListView()
	{
		super();

		addListSelectionListener(this);



		dropTarget = new DropTarget(this, this);
		dragSource = DragSource.getDefaultDragSource();

		if (acceptDrop == true)
			dragSource.createDefaultDragGestureRecognizer(
				this,
				DnDConstants.ACTION_COPY_OR_MOVE,
				this);
		else
			dragSource.createDefaultDragGestureRecognizer(
				this,
				DnDConstants.ACTION_COPY,
				this);
	}

	public void setAcceptDrop(boolean b)
	{
		acceptDrop = b;
	}

	public AddressbookDNDListView(AddressbookListModel model)
	{
		super(model);

		addListSelectionListener(this);

		dropTarget = new DropTarget(this, this);
		dragSource = new DragSource();

		if (acceptDrop == true)
			dragSource.createDefaultDragGestureRecognizer(
				this,
				DnDConstants.ACTION_COPY_OR_MOVE,
				this);
		else
			dragSource.createDefaultDragGestureRecognizer(
				this,
				DnDConstants.ACTION_COPY,
				this);

	}

	/**
	* is invoked when you are dragging over the DropSite
	*
	*/

	public void dragEnter(DropTargetDragEvent event)
	{

		// debug messages for diagnostics

		if (acceptDrop == true)
			event.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
		else
			event.acceptDrag(DnDConstants.ACTION_COPY);
	}

	/**
	 * is invoked when you are exit the DropSite without dropping
	 *
	 */

	public void dragExit(DropTargetEvent event)
	{


	}

	/**
	 * is invoked when a drag operation is going on
	 *
	 */

	public void dragOver(DropTargetDragEvent event)
	{



	}

	/**
	 * a drop has occurred
	 *
	 */

	public void drop(DropTargetDropEvent event)
	{
		if (acceptDrop == false)
		{
			event.rejectDrop();

			clearSelection();

			return;
		}

		Transferable transferable = event.getTransferable();

		HeaderItem[] items = HeaderItemDNDManager.getInstance().getHeaderItemList();

		for (int i = 0; i < items.length; i++)
		{
			addElement( (HeaderItem) ((HeaderItem) items[i]).clone() );
		}

		event.getDropTargetContext().dropComplete(true);

		clearSelection();

	}

	/**
	 * is invoked if the use modifies the current drop gesture
	 *
	 */

	public void dropActionChanged(DropTargetDragEvent event)
	{
	}

	/**
	 * a drag gesture has been initiated
	 *
	 */

	public void dragGestureRecognized(DragGestureEvent event)
	{

		if (dndAction == false)
		{
			/*
				HeaderItem[] items = new HeaderItem[selection1.length];
				items = selection1;
				HeaderItemDNDManager.getInstance().setHeaderItemList(items);
			*/
			
		
			if ( selection1 == null )
			{
				HeaderItem[] items = new HeaderItem[1];
				items[0] = (HeaderItem) getSelectedValue();
				
				HeaderItemDNDManager.getInstance().setHeaderItemList(items);
			}
			else if (selection1.length != 0)
			{
				HeaderItem[] items = new HeaderItem[selection1.length];
				items = selection1;
				HeaderItemDNDManager.getInstance().setHeaderItemList(items);
			}
			
			/*
			else
			{
				
				HeaderItem[] items = new HeaderItem[1];
				items[0] = (HeaderItem) getSelectedValue();
				HeaderItemDNDManager.getInstance().setHeaderItemList(items);
			}
			*/
		}
		else
		{
			/*
			HeaderItem[] items = new HeaderItem[selection2.length];
				items = selection2;
				HeaderItemDNDManager.getInstance().setHeaderItemList(items);
			*/
			
			
			if (selection2.length != 0)
			{
				HeaderItem[] items = new HeaderItem[selection2.length];
				items = selection2;
				HeaderItemDNDManager.getInstance().setHeaderItemList(items);
				
			}
			else
			{
				HeaderItem[] items = new HeaderItem[1];
				items[0] = (HeaderItem) getSelectedValue();
				
				HeaderItemDNDManager.getInstance().setHeaderItemList(items);
				
			}
			

		}



		source = this;

		/*
		dragSource.startDrag(
			event,
			new Cursor(Cursor.DEFAULT_CURSOR),
			ImageLoader.getImageIcon("contact_small","Add16").getImage(),
			new Point(5, 5),
			new StringSelection("contact"),
			this);
		*/



		StringSelection text = new StringSelection("contact");

		dragSource.startDrag(event, DragSource.DefaultMoveDrop, text, this);


		clearSelection();


	}

	/**
	 * this message goes to DragSourceListener, informing it that the dragging
	 * has ended
	 *
	 */

	public void dragDropEnd(DragSourceDropEvent event)
	{
		if (event.getDropSuccess())
		{
			if (acceptDrop == true)
			{
				HeaderItem[] items = HeaderItemDNDManager.getInstance().getHeaderItemList();
				for (int i = 0; i < items.length; i++)
				{
					((AddressbookListModel) getModel()).removeElement(items[i]);
				}
				//removeElement();
			}
		}
	}

	/**
	 * this message goes to DragSourceListener, informing it that the dragging
	 * has entered the DropSite
	 *
	 */

	public void dragEnter(DragSourceDragEvent event)
	{

	}

	/**
	 * this message goes to DragSourceListener, informing it that the dragging
	 * has exited the DropSite
	 *
	 */

	public void dragExit(DragSourceEvent event)
	{


	}

	/**
	 * this message goes to DragSourceListener, informing it that the dragging is currently
	 * ocurring over the DropSite
	 *
	 */

	public void dragOver(DragSourceDragEvent event)
	{


	}

	/**
	 * is invoked when the user changes the dropAction
	 *
	 */

	public void dropActionChanged(DragSourceDragEvent event)
	{

	}

	/**
	 * adds elements to itself
	 *
	 */

	/**
	 * removes an element from itself
	 */

	public void removeElement()
	{
		((AddressbookListModel) getModel()).removeElement(getSelectedValue());
	}

	

	public void valueChanged(ListSelectionEvent e)
	{


		if (dndAction == true)
		{
			Object[] list = getSelectedValues();
			
			selection1 = new HeaderItem[ list.length ];
			for ( int i=0; i<list.length; i++ )
			{
				selection1[i] = (HeaderItem) list[i];
			}
			
			

			dndAction = false;
		}
		else
		{
			Object[] list = getSelectedValues();
			
			selection2 = new HeaderItem[ list.length ];
			for ( int i=0; i<list.length; i++ )
			{
				selection2[i] = (HeaderItem) list[i];
			}

			dndAction = true;
		}

	}


}