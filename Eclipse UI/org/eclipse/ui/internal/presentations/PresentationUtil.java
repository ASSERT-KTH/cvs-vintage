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
package org.eclipse.ui.internal.presentations;

import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * Contains various utility methods for Presentation authors
 * 
 * @since 3.0
 */
public class PresentationUtil {
	private static Control dragControl;
	private static Point anchor;
	private final static int HYSTERESIS = 10;
	private final static String LISTENER_ID = PresentationUtil.class.getName() + ".dragListener"; //$NON-NLS-1$
	private static Event dragEvent;
	
	private static Listener dragListener = new Listener() {
		public void handleEvent(Event event) {
			dragEvent = event;
		}
	};
		
	/**
	 * Returns whether the mouse has moved enough to warrant
	 * opening a tracker.
	 */
	private static boolean hasMovedEnough(Event event) {		
		return Geometry.distanceSquared(getEventLoc(event), anchor) 
			>= HYSTERESIS * HYSTERESIS; 		
	}
	
	/**
	 * Returns the location of the given event, in display coordinates
	 * @return
	 */
	private static Point getEventLoc(Event event) {
		Control ctrl = (Control)event.widget;
		return ctrl.toDisplay(new Point(event.x, event.y));		
	}
	
	private static Listener moveListener = new Listener() {
		public void handleEvent(Event event) {
			handleMouseMove(event);
		}
	};
	
	private static Listener clickListener = new Listener() {
		public void handleEvent(Event e) {
			handleMouseClick(e);
		}
	};

	private static Listener mouseDownListener = new Listener() {
		public void handleEvent(Event event) {
			dragControl = (Control)event.widget;
			anchor = getEventLoc(event);	
		}
	};
	
	private static void handleMouseClick(Event event) {
		cancelDrag();
	}
	
	private static void handleMouseMove(Event e) {
		if (dragControl != null && dragEvent != null && hasMovedEnough(e)) {
			cancelDrag();
			Listener l = (Listener)dragControl.getData(LISTENER_ID);
			if (l != null) {
				dragControl.setCapture(true);
				l.handleEvent(dragEvent);
				dragControl.setCapture(false);
			}
		}
	}	
	
	private static void cancelDrag() {
		if (dragControl != null) {
			dragControl = null;
		}
		
		dragEvent = null;
	}
	
	/**
	 * Adds a drag listener to the given control. The behavior is very similar
	 * to control.addListener(SWT.DragDetect, dragListener), however the listener
	 * attached by this method is less sensitive. The drag event is only fired
	 * once the user moves the cursor more than HYSTERESIS pixels. 
	 * <p>
	 * This is useful for registering a listener that will trigger an editor or
	 * view drag, since an overly sensitive drag listener can cause users to accidentally
	 * drag views when trying to select a tab.</p>
	 * <p>
	 * Currently, only one such drag listener can be registered at a time. </p> 
	 * 
	 * @param control the control containing the drag listener
	 * @param dragListener the drag listener to attach
	 */
	public static void addDragListener(Control control, Listener dragListener) {
		control.addListener(SWT.DragDetect, dragListener);
		control.addListener(SWT.MouseUp, clickListener);
		control.addListener(SWT.MouseDoubleClick, clickListener);
		control.addListener(SWT.MouseDown, mouseDownListener);
		control.addListener(SWT.MouseMove, moveListener);
		control.setData(LISTENER_ID, dragListener);
	}
	
	/**
	 * Removes a drag listener that was previously attached using addDragListener
	 * 
	 * @param control the control containing the drag listener
	 * @param dragListener the drag listener to remove
	 */
	public static void removeDragListener(Control control, Listener dragListener) {
		control.removeListener(SWT.DragDetect, dragListener);
		control.removeListener(SWT.MouseUp, clickListener);
		control.removeListener(SWT.MouseDoubleClick, clickListener);
		control.removeListener(SWT.MouseDown, mouseDownListener);
		control.removeListener(SWT.MouseMove, moveListener);
		control.setData(LISTENER_ID, null);
	}
		
}
