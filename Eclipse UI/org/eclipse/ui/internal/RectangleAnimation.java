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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.progress.UIJob;

/**
 * This job creates an animated rectangle that moves from a source rectangle to
 * a target in a fixed amount of time. To begin the animation, instantiate this
 * object then call schedule().
 *  
 * @since 3.0
 */
public class RectangleAnimation extends Job {
	private Rectangle start;
	private int elapsed;
	private int duration;
	private long startTime = 0;
	private Rectangle end;
	private Rectangle last;
	private boolean done = false;
	private Canvas canvas;
	
	private static Rectangle interpolate(Rectangle start, Rectangle end, double amount) {
		double initialWeight = 1.0 - amount;
		
		Rectangle result = new Rectangle(
				(int) (start.x * initialWeight + end.x * amount),
				(int) (start.y * initialWeight + end.y * amount),
				(int) (start.width * initialWeight + end.width * amount),
				(int) (start.height * initialWeight + end.height * amount));
		
		return result;
	}
	
	private UIJob paintJob = new UIJob(WorkbenchMessages.getString("RectangleAnimation.Animating_Rectangle")) { //$NON-NLS-1$

		public IStatus runInUIThread(IProgressMonitor monitor) {
			canvas.redraw();
			
			return Status.OK_STATUS;
		}
		
	};

	private void draw(GC gc) {
		if (startTime == 0) {
			return;
		}
		
		long currentTime = System.currentTimeMillis();
		
		double amount = (double)(currentTime - startTime) / (double)duration;
		
		if (amount > 1.0) {
			amount = 1.0;
			done = true;
		}
		
		Rectangle toPaint = interpolate(start, end, amount);
		
		gc.setLineWidth(2);
		Color color = canvas.getDisplay().getSystemColor(SWT.COLOR_WHITE);
		gc.setForeground(color);
		
		gc.setXORMode(true);
		if (last != null) {
			if (last.equals(toPaint)) {
				return;
			}
			gc.drawRectangle(Geometry.toControl(canvas, last));
		}
		gc.drawRectangle(Geometry.toControl(canvas, toPaint));
		last = toPaint;
	}

	
	/**
	 * Creates an animation that will morph the start rectangle to the end rectangle in the
	 * given number of milliseconds. The animation will take the given number of milliseconds to
	 * complete.
	 * 
	 * Note that this is a Job, so you must invoke schedule() before the animation will begin 
	 * 
	 * @param whereToDraw 
	 * @param start initial rectangle (display coordinates)
	 * @param end final rectangle (display coordinates)
	 * @param duration number of milliseconds over which the animation will run 
	 */
	public RectangleAnimation(Composite whereToDraw, Rectangle start, Rectangle end, int duration) {
		super(WorkbenchMessages.getString("RectangleAnimation.Animating_Rectangle")); //$NON-NLS-1$
		this.duration = duration;
		this.start = start;
		this.end = end;
		this.canvas = new Canvas(whereToDraw, SWT.NO_BACKGROUND);
		setSystem(true);
		canvas.setBounds(whereToDraw.getClientArea());
		
		canvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent event) {
				draw(event.gc);
			}
		});
		
		canvas.moveAbove(null);

		paintJob.setPriority(Job.INTERACTIVE);
		paintJob.setSystem(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IStatus run(IProgressMonitor monitor) {
		
		startTime = System.currentTimeMillis();
		
		while (!done) {
			paintJob.schedule();
			try {
				paintJob.join();
			} catch (InterruptedException e) {
				break;
			}
		}

		canvas.getDisplay().syncExec(new Runnable() {
			public void run() {
				canvas.dispose();
			}
		});
		
		return Status.OK_STATUS;
	}
}
