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
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.util.BundleUtility;
/**
 * The ProgressUtil is a class that contains static utility methods used for the
 * progress API.
 */
public class ProgressManagerUtil {
	private static String PROGRESS_VIEW_ID = "org.eclipse.ui.views.ProgressView"; //$NON-NLS-1$
	public static long SHORT_OPERATION_TIME = 250;
	private static String ellipsis = ProgressMessages
			.getString("ProgressFloatingWindow.EllipsisValue"); //$NON-NLS-1$
	/**
	 * Return a status for the exception.
	 * 
	 * @param exception
	 * @return
	 */
	static Status exceptionStatus(Throwable exception) {
		return new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.ERROR,
				exception.getMessage(), exception);
	}
	/**
	 * Log the exception for debugging.
	 * 
	 * @param exception
	 */
	static void logException(Throwable exception) {
     BundleUtility.log(PlatformUI.PLUGIN_ID, exception);
	}
	/**
	 * Sets the label provider for the viewer.
	 */
	static void initLabelProvider(ProgressTreeViewer viewer) {
		viewer.setLabelProvider(new ProgressLabelProvider());
	}
	/**
	 * Return a viewer sorter for looking at the jobs.
	 * 
	 * @return
	 */
	static ViewerSorter getProgressViewerSorter() {
		return new ViewerSorter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer,
			 *      java.lang.Object, java.lang.Object)
			 */
			public int compare(Viewer testViewer, Object e1, Object e2) {
				return ((Comparable) e1).compareTo(e2);
			}
		};
	}
	/**
	 * Open the progress view in the supplied window.
	 * 
	 * @param window
	 */
	static void openProgressView(WorkbenchWindow window) {
		IWorkbenchPage page = window.getActivePage();
		if (page == null)
			return;
		try {
			page.showView(PROGRESS_VIEW_ID);
		} catch (PartInitException exception) {
			logException(exception);
		}
	}
	static boolean useNewProgress() {
		return WorkbenchPlugin.getDefault().getPreferenceStore().getBoolean(
				"USE_NEW_PROGRESS"); //$NON-NLS-1$
	}
	/**
	 * Return whether or not the progress view is missing.
	 * 
	 * @param window
	 * @return true if there is no progress view.
	 */
	static boolean missingProgressView(WorkbenchWindow window) {
		return WorkbenchPlugin.getDefault().getViewRegistry().find(
				PROGRESS_VIEW_ID) == null;
	}
	/**
	 * Shorten the given text <code>t</code> so that its length doesn't exceed
	 * the given width. The default implementation replaces characters in the
	 * center of the original string with an ellipsis ("..."). Override if you
	 * need a different strategy.
	 * 
	 * @param
	 */
	static String shortenText(String textValue, Control control) {
		if (textValue == null)
			return null;
		Display display = control.getDisplay();
		GC gc = new GC(display);
		int maxWidth = control.getBounds().width - 5;
		if (gc.textExtent(textValue).x < maxWidth) {
			gc.dispose();
			return textValue;
		}
		int length = textValue.length();
		int ellipsisWidth = gc.textExtent(ellipsis).x;
		//Find the second space seperator and start from there
		int secondWord = findSecondWhitespace(textValue, gc, maxWidth);
		int pivot = ((length - secondWord) / 2) + secondWord;
		int start = pivot;
		int end = pivot + 1;
		while (start >= secondWord && end < length) {
			String s1 = textValue.substring(0, start);
			String s2 = textValue.substring(end, length);
			int l1 = gc.textExtent(s1).x;
			int l2 = gc.textExtent(s2).x;
			if (l1 + ellipsisWidth + l2 < maxWidth) {
				gc.dispose();
				return s1 + ellipsis + s2;
			}
			start--;
			end++;
		}
		gc.dispose();
		return textValue;
	}
	/**
	 * Find the second index of a whitespace. Return the first index if there
	 * isn't one or 0 if there is no space at all.
	 * 
	 * @param textValue
	 * @param gc.
	 *            The GC to test max length
	 * @param maxWidth.
	 *            The maximim extent
	 * @return int
	 */
	private static int findSecondWhitespace(String textValue, GC gc,
			int maxWidth) {
		int firstCharacter = 0;
		char[] chars = textValue.toCharArray();
		//Find the first whitespace
		for (int i = 0; i < chars.length; i++) {
			if (Character.isWhitespace(chars[i])) {
				firstCharacter = i;
				break;
			}
		}
		//If we didn't find it once don't continue
		if (firstCharacter == 0)
			return 0;
		//Initialize to firstCharacter in case there is no more whitespace
		int secondCharacter = firstCharacter;
		//Find the second whitespace
		for (int i = firstCharacter; i < chars.length; i++) {
			if (Character.isWhitespace(chars[i])) {
				secondCharacter = i;
				break;
			}
		}
		//Check that we haven't gone over max width. Throw
		//out an index that is too high
		if (gc.textExtent(textValue.substring(0, secondCharacter)).x > maxWidth) {
			if (gc.textExtent(textValue.substring(0, firstCharacter)).x > maxWidth)
				return 0;
			else
				return firstCharacter;
		}
		return secondCharacter;
	}
	/**
	 * If there are any modal shells open reschedule openJob to wait until they
	 * are closed. Return true if it rescheduled, false if there is nothing
	 * blocking it.
	 * 
	 * @param openJob
	 * @return boolean. true if the job was rescheduled due to modal 
	 * dialogs.
	 */
	public static boolean rescheduleIfModalShellOpen(Job openJob) {
		Shell modal = getModalShell();
		if (modal == null)
			return false;
		
		//try again in a few seconds
		openJob.schedule(PlatformUI.getWorkbench().getProgressService().getLongOperationTime());
		return true;
	}
	/**
	 * Return the modal shell that is currently open. If there isn't one then
	 * return null
	 * 
	 * @return Shell or <code>null</code>.
	 */
	public static Shell getModalShell() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		Shell[] shells = workbench.getDisplay().getShells();
		int modal = SWT.APPLICATION_MODAL | SWT.SYSTEM_MODAL
				| SWT.PRIMARY_MODAL;
		for (int i = 0; i < shells.length; i++) {
			//Do not worry about shells that will not block the user.
			if (shells[i].isVisible()) {
				int style = shells[i].getStyle();
				if ((style & modal) != 0) {
					return shells[i];
				}
			}
		}
		return null;
	}
	
	/**
	 * Utility method to get the best parenting possible for
	 * a dialog. If there is a modal shell create it so as to 
	 * avoid two modal dialogs.
	 * If not then return the shell of the active workbench window.
	 * If neither can be found return null.
	 * @return Shell or <code>null</code>
	 */
	public static Shell getDefaultParent(){
		Shell modal = getModalShell();
		if(modal != null)
			return modal;
		
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if(window != null)
			return window.getShell();
		
		return null;
	}
}