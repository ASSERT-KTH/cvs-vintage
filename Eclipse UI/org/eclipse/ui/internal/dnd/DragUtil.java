/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dnd;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tracker;
import org.eclipse.ui.internal.DragCursors;


/**
 * Provides the methods for attaching drag-and-drop listeners to SWT controls. 
 */
public class DragUtil {
	private static final String DROP_TARGET_ID = "org.eclipse.ui.internal.dnd.dropTarget"; //$NON-NLS-1$
	
	/**
	 * Singleton drag listener
	 */
	private static DragListener listener = new DragListener();
	
	/**
	 * List of IDragOverListener
	 */
	private static List defaultTargets = new ArrayList();
	
	/**
	 * Sets the drop target for the given control. It is possible to add one or more 
	 * targets for a "null" control. This becomes a default target that is used if no
	 * other targets are found (for example, when dragging objects off the application
	 * window). 
	 * 
	 * @param control the control that should be treated as a drag target, or null
	 * to indicate the default target
	 * @param target the drag target to handle the given control
	 */
	public static void addDragTarget(Control control, IDragOverListener target) {
		if (control == null) {
			defaultTargets.add(target);
		} else {
			control.setData(DROP_TARGET_ID, target);
		}
	}
	
	/**
	 * Removes a drop target from the given control.
	 * 
	 * @param control
	 * @param target
	 */
	public static void removeDragTarget(Control control, IDragOverListener target) {
		if (control == null) {
			defaultTargets.remove(target);
		} else {
			control.setData(DROP_TARGET_ID, null);
		}
	}
	
	/**
	 * Shorthand method. Returns the bounding rectangle for the given control, in
	 * display coordinates. 
	 * 
	 * @param draggedItem
	 * @param boundsControl
	 * @return
	 */
	public static Rectangle getDisplayBounds(Control boundsControl) {
		Control parent = boundsControl.getParent();
		if (parent == null) {
			return boundsControl.getBounds();
		}
		
		return Geometry.toDisplay(parent, boundsControl.getBounds());
	}
	
    public static boolean performDrag(final Object draggedItem, Rectangle sourceBounds) {
		IDropTarget target = dragToTarget(draggedItem, sourceBounds,
				Display.getDefault().getCursorLocation(), false);
		
		if (target == null) {
			return false;
		}
		
		target.drop();
		
		return true;
	}
	
	/**
	 * Drags the given item, given an initial bounding rectangle in display coordinates.
	 * Due to a quirk in the Tracker class, changing the tracking rectangle when using the
	 * keyboard will also cause the mouse cursor to move. Since "snapping" causes the tracking
	 * rectangle to change based on the position of the mouse cursor, it is impossible to do
	 * drag-and-drop with the keyboard when snapping is enabled.    
	 * 
	 * @param draggedItem object being dragged
	 * @param sourceBounds initial bounding rectangle for the dragged item
	 * @param initialLocation initial position of the mouse cursor
	 * @param allowSnapping true iff the rectangle should snap to the drop location. This must
	 * be false if the user might be doing drag-and-drop using the keyboard. 
	 *  
	 * @return
	 */
    /* package */ static IDropTarget dragToTarget(final Object draggedItem, final Rectangle sourceBounds, 
    		final Point initialLocation, final boolean allowSnapping) {
		final Display display = Display.getDefault();
		// Create a tracker.  This is just an XOR rect on the screen.
		// As it moves we notify the drag listeners.
		final Tracker tracker = new Tracker(display, SWT.NULL);
				
		tracker.setStippled(true);
		
		tracker.addListener(SWT.Move, new Listener() {
			public void handleEvent(final Event event) {
				display.syncExec(new Runnable() {
					public void run() {
						Point location = new Point(event.x, event.y);
														
						Control targetControl = display.getCursorControl();
						
						IDropTarget target = getDropTarget(targetControl, draggedItem, location, 
								tracker.getRectangles()[0]); 
						
						Rectangle snapTarget = null;
						
						if (target != null) {
							snapTarget = target.getSnapRectangle();  
							
							tracker.setCursor(target.getCursor());
						} else {
							tracker.setCursor(DragCursors.getCursor(DragCursors.INVALID));
						}	
						
						if (allowSnapping) {
							
							if (snapTarget == null) {
								snapTarget = new Rectangle(sourceBounds.x + location.x - initialLocation.x,
									sourceBounds.y + location.y - initialLocation.y,
									sourceBounds.width, 
									sourceBounds.height); 					
							}
							
							// Try to prevent flicker: don't change the rectangles if they're already in
							// the right location
							
							Rectangle[] currentRectangles = tracker.getRectangles();
							
							if (!(currentRectangles.length == 1 && currentRectangles[0].equals(snapTarget))) {
								tracker.setRectangles(new Rectangle[] {snapTarget});
							}
						}
					}
				});
			}
		});
		
		if (sourceBounds != null) {
			tracker.setRectangles(new Rectangle[] { new Rectangle(sourceBounds.x, sourceBounds.y, sourceBounds.width, sourceBounds.height) });
		}
		
		// HACK:
		// Some control needs to capture the mouse during the drag or other 
		// controls will interfere with the cursor
		Control startControl = display.getCursorControl();
		if (startControl != null) {
			startControl.setCapture(true);
		}
		
		// Run tracker until mouse up occurs or escape key pressed.
		boolean trackingOk = tracker.open();

		// HACK:
		// Release the mouse now
		if (startControl != null) {
			startControl.setCapture(false);
		}
		
		Point finalLocation = display.getCursorLocation();
		
		IDropTarget dropTarget = null;
		if (trackingOk) {
			Control targetControl = display.getCursorControl();
			
			dropTarget = getDropTarget(targetControl, draggedItem, finalLocation, tracker.getRectangles()[0]);			
		}
				
		// Cleanup.
		tracker.dispose();
		
		return dropTarget;
	}
	
	/**
	 * Flags the given control as draggable
	 * 
	 * @param control
	 */
	public static void addDragSource(Control control, IDragSource source) {
		listener.attach(control, source);
	}
	
	public static void removeDragSource(Control control) {
		listener.detach(control);
	} 
	
	/**
	 * Returns the drag target for the given control or null if none. 
	 * 
	 * @param toSearch
	 * @param e
	 * @return
	 */
	public static IDropTarget getDropTarget(Control toSearch, Object draggedObject, Point position, Rectangle dragRectangle) {		
		for (Control current = toSearch; current != null; current = current.getParent()) {
			IDragOverListener target = (IDragOverListener)current.getData(DROP_TARGET_ID);
						
			if (target != null) {
				IDropTarget dropTarget = target.drag(toSearch, draggedObject, position, dragRectangle);
				
				if (dropTarget != null) {
					return dropTarget;
				}
			}
			
			// Don't look to parent shells for drop targets
			if (current instanceof Shell) {
				break;
			}
		}
		
		// No controls could handle this event -- check for default targets
		Iterator iter = defaultTargets.iterator();
		while (iter.hasNext()) {
			IDragOverListener next = (IDragOverListener)iter.next();

			IDropTarget dropTarget = next.drag(toSearch, draggedObject, position, dragRectangle);
			
			if (dropTarget != null) {
				return dropTarget;
			}			
		}
		
		// No default targets found either.
		
		return null;
	}
	
}
