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
package org.eclipse.ui.internal.progress;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;

import org.eclipse.ui.internal.misc.Assert;
/**
 * The ProgressViewer is the viewer used by progress windows. It displays text
 * on the canvas.
 */
public class ProgressViewer extends StructuredViewer {
	Canvas canvas;
	Object[] displayedItems = new Object[0];
	
	private final static List EMPTY_LIST = new ArrayList();

	/**
	 * Font metrics to use for determining pixel sizes.
	 */
	private FontMetrics fontMetrics;	

	private int numShowItems = 1;
	
	/**
	 * Create a new instance of the receiver with the supplied
	 * parent and style bits.
	 * @param parent The composite the Canvas is created in
	 * @param style style bits for the canvas
	 * @param itemsToShow the number of items this will show
	 */
	ProgressViewer(Composite parent,int style, int itemsToShow) {
		super();
		numShowItems = itemsToShow;
		canvas = new Canvas(parent, style);
		hookControl(canvas);
		// Compute and store a font metric
		GC gc = new GC(canvas);
		gc.setFont(JFaceResources.getDefaultFont());
		fontMetrics = gc.getFontMetrics();
		gc.dispose();
		initializeListeners();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.StructuredViewer#doFindInputItem(java.lang.Object)
	 */
	protected Widget doFindInputItem(Object element) {
		return null; // No widgets associated with items
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.StructuredViewer#doFindItem(java.lang.Object)
	 */
	protected Widget doFindItem(Object element) {
		return null; // No widgets associated with items
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.StructuredViewer#doUpdateItem(org.eclipse.swt.widgets.Widget,
	 *      java.lang.Object, boolean)
	 */
	protected void doUpdateItem(Widget item, Object element, boolean fullMap) {
		canvas.redraw();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.StructuredViewer#getSelectionFromWidget()
	 */
	protected List getSelectionFromWidget() {
		//No selection on a Canvas
		return EMPTY_LIST; 
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.StructuredViewer#internalRefresh(java.lang.Object)
	 */
	protected void internalRefresh(Object element) {
		displayedItems = getSortedChildren(getRoot());
		canvas.redraw();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.StructuredViewer#reveal(java.lang.Object)
	 */
	public void reveal(Object element) {
		//Nothing to do here as we do not scroll
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.StructuredViewer#setSelectionToWidget(java.util.List,
	 *      boolean)
	 */
	protected void setSelectionToWidget(List l, boolean reveal) {
		//Do nothing as there is no selection
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.Viewer#getControl()
	 */
	public Control getControl() {
		return canvas;
	}
	private void initializeListeners() {
		canvas.addPaintListener(new PaintListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
			 */
			public void paintControl(PaintEvent event) {
				
				GC gc = event.gc;
				ILabelProvider labelProvider = (ILabelProvider) getLabelProvider();
				for (int i = 0; i < displayedItems.length; i++) {
					String string = labelProvider.getText(displayedItems[i]);
					gc.drawString(string,0,i * fontMetrics.getHeight(),true);
				}
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ContentViewer#setLabelProvider(org.eclipse.jface.viewers.IBaseLabelProvider)
	 */
	public void setLabelProvider(IBaseLabelProvider labelProvider) {
		Assert.isTrue(labelProvider instanceof ILabelProvider);
		super.setLabelProvider(labelProvider);
	}
	
	/**
	 * Get the size hints for the receiver. These are used for 
	 * layout data.
	 * @return Point - the preferred x and y coordinates
	 */
	public Point getSizeHints() {
		
		Display display = canvas.getDisplay();
		
		GC gc = new GC(display);
		FontMetrics fm = gc.getFontMetrics();
		int charWidth = fm.getAverageCharWidth();
		int charHeight = fm.getHeight();
		int maxWidth = display.getBounds().width / 3;
		int maxHeight = display.getBounds().height / 6;
		int fontWidth = charWidth * 36;
		int fontHeight = charHeight * numShowItems;
		if (maxWidth < fontWidth)
			fontWidth = maxWidth;
		if (maxHeight < fontHeight)
			fontHeight = maxHeight;
		gc.dispose();
		return new Point(fontWidth, fontHeight);
	}
	
	/**
	 * Get the number of items that this viewer is 
	 * designed to show.
	 * @return the number of items we are showing
	 */
	public int getNumShowItems() {
		return numShowItems;
	}
}
