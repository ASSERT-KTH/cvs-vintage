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
package org.eclipse.ui.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.util.Geometry;

import org.eclipse.ui.internal.dnd.AbstractDropTarget;
import org.eclipse.ui.internal.dnd.CompatibilityDragTarget;
import org.eclipse.ui.internal.dnd.IDragOverListener;
import org.eclipse.ui.internal.dnd.IDropTarget;

/**
 */
/*package*/ class TrimDropTarget implements IDragOverListener {

	private TrimLayout layout;
	private Composite windowComposite;
		
	public TrimDropTarget(Composite someComposite) {
		layout = (TrimLayout)someComposite.getLayout();
		windowComposite = someComposite;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.dnd.IDragOverListener#drag(org.eclipse.swt.widgets.Control, java.lang.Object, org.eclipse.swt.graphics.Point, org.eclipse.swt.graphics.Rectangle)
	 */
	public IDropTarget drag(Control currentControl, Object draggedObject, Point position, final Rectangle dragRectangle) {

		if (draggedObject instanceof IWindowTrim) {
			final IWindowTrim draggedTrim = (IWindowTrim)draggedObject;
			
			Control trimControl = draggedTrim.getControl();

			if (trimControl.getParent() == windowComposite) {
				Control targetTrim = getTrimControl(currentControl);
				
				if (targetTrim != null) {
					int side = layout.getTrimLocation(targetTrim);
					
					if (side == SWT.DEFAULT) {
						if (targetTrim == layout.getCenterControl()) {
							side = CompatibilityDragTarget.getRelativePosition(targetTrim, position);
							if (side == SWT.CENTER) {
								side = SWT.DEFAULT;
							}
							
							targetTrim = null;
						}
					}
					
					if (side != SWT.DEFAULT && (targetTrim != trimControl) 
							&& (targetTrim != null || side != layout.getTrimLocation(trimControl)) 
							&& ((side & draggedTrim.getValidSides()) != 0)) {
						final int dropSide = side; 
						final Control insertionPoint = targetTrim;
						
						return new AbstractDropTarget() {
							public void drop() {
								draggedTrim.dock(dropSide);
							}

							public Cursor getCursor() {
								return DragCursors.getCursor(DragCursors.positionToDragCursor(dropSide));
							}
							
							public Rectangle getSnapRectangle() {
								
								int smaller = Math.min(dragRectangle.width, dragRectangle.height);
								
								return Geometry.toDisplay(windowComposite, Geometry.getExtrudedEdge(windowComposite.getClientArea(), 
									smaller, dropSide));							
							}
						};
					}
				}
			}			
		}
		
		return null;
	}

	private Control getTrimControl(Control searchSource) {
		if (searchSource == null) {
			return null;
		}
		
		if (searchSource.getParent() == windowComposite) {
			return searchSource;
		}
		
		return getTrimControl(searchSource.getParent());
	}	
}
