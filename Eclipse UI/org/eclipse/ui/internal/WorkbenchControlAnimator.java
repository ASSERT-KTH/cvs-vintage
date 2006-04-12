/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ControlAnimator;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.progress.UIJob;

/**
 * Animates a control by sliding it up or down. The animation is
 * achieved using the UI's job functionality and by incrementally 
 * decreasing or increasing the control's y coordinate.
 * 
 * @since 3.2
 */
public class WorkbenchControlAnimator extends ControlAnimator {
	
	/**
	 * A constant denoting the CLOSED animation state of 
	 * a control (value is 0)
	 */
	private static final int CLOSED = 0;

	/**
	 * A constant denoting the OPENING animation state of 
	 * a control (value is 1)
	 */
	private static final int OPENING = 1;

	/**
	 * A constant denoting the OPEN animation state of 
	 * a control (value is 2)
	 */
	private static final int OPEN = 2;

	/**
	 * A constant denoting the CLOSING animation state of 
	 * a control (value is 3)
	 */
	private static final int CLOSING = 3;
	
	private UIJob slideJob;
	
	private int endY;
	
	private boolean finished;
	
	private boolean inTransition = false;
	
	private int state = CLOSED;
	
	private int LONG_DELAY = 300;
	
	private int SHORT_DELAY = 25;
	

	/**
	 * Constructs a new WorbenchControlAnimator instance and passes along the
	 * control that will be animated.
	 * 
	 * @param control the control that will be animated.
	 */
	public WorkbenchControlAnimator(Control control) {
		super(control);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.ControlAnimator#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		setAnimationState(visible ? OPENING: CLOSING);
		finished = false;

		control.setVisible(true);
		
		Rectangle parentBounds = control.getParent().getBounds();
		int bottom = parentBounds.height;
		
		if (bottom <= 0) {
			setAnimationState(OPEN);
			return;
		}
		
		endY = visible ? bottom - control.getBounds().height
				: bottom;
		
		if(slideJob != null)
			slideJob.cancel();
		
		slideJob = getSlideJob();
		control.addDisposeListener(new DisposeListener(){
			public void widgetDisposed(org.eclipse.swt.events.DisposeEvent e) {
				slideJob = null;
			}
		});		
		
		// Wait before displaying the control to allow for opening 
		// condition to change, but no waiting before closing the control.
		if(getAnimationState() == OPENING && !inTransition){
			slideJob.schedule(LONG_DELAY);
		} else {
			slideJob.schedule(SHORT_DELAY);
		}
		
	}
	
	/**
	 * Creates a job in the UI thread to display or hide the control
	 * by increasing or decreasing the y coordinate and setting the new 
	 * location. The job will continually re-schedule itself until the
	 * the control is no longer in transition and will also stop
	 * if the monitor is canceled or the control is disposed.
	 * 
	 * @return the UIJob responsible for opening or closing the control
	 */
	private UIJob getSlideJob(){
		UIJob newSlideJob = new UIJob("Sliding Message") { //$NON-NLS-1$
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if(!monitor.isCanceled() && !control.isDisposed()){
					Point loc = control.getLocation();
					switch (getAnimationState()) {
					case OPENING:
						loc.y--;
						if (loc.y >= endY) {
							control.setLocation(loc);
						} else {
							finished = true;
							setAnimationState(OPEN);
						}
						break;
					case CLOSING:
						loc.y++;
						if (loc.y <= endY) {
							control.setLocation(loc);
						} else {
							finished = true;
							setAnimationState(CLOSED);
							control.setVisible(false);
						}
						break;
					default:
						break;
					}
					if(!finished) {
						inTransition = true;
						slideJob.schedule(5);					
					} else
						inTransition = false;
					return Status.OK_STATUS;		
				}
				return Status.CANCEL_STATUS;
			}		
		};
		newSlideJob.setSystem(true);
		return newSlideJob;
	}
	
	/**
	 * Sets the state of the control and whether or not
	 * it should be visible. The value should be one of 
	 * the following: {@link #OPENING}, {@link #OPEN}, 
	 * {@link #CLOSING}, or {@link #CLOSED}
	 * 
	 * @param state	the desired state of the control
	 */
	private void setAnimationState(int state) {
		this.state = state;
	}

	/**
	 * Returns the current state of the control.
	 * 
	 * @return the current state of the control: {@link #OPENING}, 
	 * 		   {@link #OPEN}, {@link #CLOSING}, or {@link #CLOSED}
	 */
	private int getAnimationState() {
		return state;
	}

}
