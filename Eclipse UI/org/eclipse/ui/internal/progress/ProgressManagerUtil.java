/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import org.eclipse.ui.PlatformUI;

/**
 * The ProgressUtil is a class that contains static utility methods used for the progress
 * API.
 */
class ProgressManagerUtil {

	/**
	 * Return a status for the exception.
	 * @param exception
	 * @return
	 */
	static Status exceptionStatus(Throwable exception) {
		return new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.ERROR, exception.getMessage(), exception);
	}

	/**
	 * Log the exception for debugging.
	 * @param exception
	 */
	static void logException(Throwable exception) {
		Platform.getPlugin(PlatformUI.PLUGIN_ID).getLog().log(exceptionStatus(exception));
	}

	/**
	 * Sets the label provider for the viewer.
	 */
	static void initLabelProvider(ProgressTreeViewer viewer) {
		viewer.setLabelProvider(new ProgressLabelProvider());

	}
	
	/**
	 * Return a viewer sorter for looking at the jobs.
	 * @return
	 */
	static ViewerSorter getProgressViewerSorter() {
		return new ViewerSorter() {
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
			 */
			public int compare(Viewer testViewer, Object e1, Object e2) {
				return ((Comparable) e1).compareTo(e2);
			}
		};
	}
	

}
