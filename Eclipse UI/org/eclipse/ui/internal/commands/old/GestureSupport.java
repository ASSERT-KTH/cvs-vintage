/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.commands.old;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import org.eclipse.ui.internal.util.Util;

public final class GestureSupport {

	private final static ResourceBundle resourceBundle = ResourceBundle.getBundle(GestureSupport.class.getName());

	private final static String DOWN = "2"; //$NON-NLS-1$
	private final static String LEFT = "4"; //$NON-NLS-1$
	private final static String RIGHT = "6"; //$NON-NLS-1$
	private final static String STROKE_SEPARATOR = " "; //$NON-NLS-1$
	private final static String UNKNOWN = "?"; //$NON-NLS-1$
	private final static String UP = "8"; //$NON-NLS-1$

	public final static Stroke STROKE_DOWN = Stroke.create(2);
	public final static Stroke STROKE_LEFT = Stroke.create(4);
	public final static Stroke STROKE_RIGHT = Stroke.create(6);  
	public final static Stroke STROKE_UP = Stroke.create(8);

	public static String formatSequence(Sequence sequence, boolean localize) {
		if (sequence == null)
			throw new NullPointerException();
			
		int i = 0;
		Iterator iterator = sequence.getStrokes().iterator();
		StringBuffer stringBuffer = new StringBuffer();
		
		while (iterator.hasNext()) {
			if (i != 0)
				stringBuffer.append(STROKE_SEPARATOR);

			stringBuffer.append(formatStroke((Stroke) iterator.next(), localize));
			i++;
		}

		return stringBuffer.toString();
	}

	public static String formatStroke(Stroke stroke, boolean localize) {
		if (stroke == null)
			throw new NullPointerException();

		if (STROKE_DOWN.equals(stroke))
			return localize ? Util.getString(resourceBundle, DOWN) : DOWN;
		else if (STROKE_LEFT.equals(stroke))
			return localize ? Util.getString(resourceBundle, LEFT) : LEFT;
		else if (STROKE_RIGHT.equals(stroke))
			return localize ? Util.getString(resourceBundle, RIGHT) : RIGHT;
		else if (STROKE_UP.equals(stroke))
			return localize ? Util.getString(resourceBundle, UP) : UP;
		else 
			return localize ? Util.getString(resourceBundle, UNKNOWN) : UNKNOWN;	
	}
	
	public static Sequence parseSequence(String string) {
		if (string == null)
			throw new NullPointerException();

		List strokes = new ArrayList();
		StringTokenizer stringTokenizer = new StringTokenizer(string);
				
		while (stringTokenizer.hasMoreTokens())
			strokes.add(parseStroke(stringTokenizer.nextToken()));
			
		return Sequence.create(strokes);
	}	
	
	public static Stroke parseStroke(String string) {
		if (string == null)
			throw new NullPointerException();
		
		if (DOWN.equals(string))
			return STROKE_DOWN;
		else if (LEFT.equals(string))
			return STROKE_LEFT;
		else if (RIGHT.equals(string))
			return STROKE_RIGHT;
		else if (UP.equals(string))
			return STROKE_UP;
		else 
			return Stroke.create(0);
	}

	public static Sequence recognize(Point[] points, int sensitivity) {
		Stroke stroke = null;
		List strokes = new ArrayList();
		int x0 = 0;
		int y0 = 0;

		for (int i = 0; i < points.length; i++) {
			Point point = points[i];

			if (i == 0) {
				x0 = point.getX();
				y0 = point.getY();
				continue;
			}

			int x1 = point.getX();
			int y1 = point.getY();
			int dx = (x1 - x0) / sensitivity;
			int dy = (y1 - y0) / sensitivity;

			if ((dx != 0) || (dy != 0)) {
				if (dx > 0 && !STROKE_RIGHT.equals(stroke)) {
					strokes.add(stroke = STROKE_RIGHT);
				} else if (dx < 0 && !STROKE_LEFT.equals(stroke)) {
					strokes.add(stroke = STROKE_LEFT);
				} else if (dy > 0 && !STROKE_DOWN.equals(stroke)) {
					strokes.add(stroke = STROKE_DOWN);
				} else if (dy < 0 && !STROKE_UP.equals(stroke)) {
					strokes.add(stroke = STROKE_UP);
				}

				x0 = x1;
				y0 = y1;
			}
		}

		return Sequence.create(strokes);
	}

	private GestureSupport() {
		super();
	}
}
