/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.ui.progress;
/**
 * The UIJob is a Job that runs within the UI Thread via an asyncExec.
 * 
 * @since 3.0
 */
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.ui.internal.misc.Policy;
import org.eclipse.ui.internal.progress.ProgressMessages;
public abstract class UIJob extends Job {
	private Display cachedDisplay;
	/**
	 * Create a new instance of the receiver with the supplied name. The display
	 * used will be the one from the workbench if this is available. UIJobs with
	 * this constructor will determine thier display at runtime.
	 * 
	 * @param name
	 *            the job name
	 *  
	 */
	public UIJob(String name) {
		super(name);
	}
	/**
	 * Create a new instance of the receiver with the supplied Display.
	 * 
	 * @param jobDisplay
	 *            the display
	 * @param name
	 *            the job name
	 */
	public UIJob(Display jobDisplay, String name) {
		this(name);
		setDisplay(jobDisplay);
	}
	/**
	 * Convenience method to return a status for an exception.
	 * 
	 * @param exception
	 * @return IStatus an error status built from the exception
	 */
	public static IStatus errorStatus(Throwable exception) {
		return new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.ERROR,
				exception.getMessage(), exception);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 *      Note: this message is marked final. Implementors should use
	 *      runInUIThread() instead.
	 */
	public final IStatus run(final IProgressMonitor monitor) {
		Display asyncDisplay = getDisplay();
		if (asyncDisplay == null || asyncDisplay.isDisposed()) {
			return Status.CANCEL_STATUS;
		}
		asyncDisplay.asyncExec(new Runnable() {
			public void run() {
				long startTime = System.currentTimeMillis();
				IStatus result = null;
				try {
					//As we are in the UI Thread we can
					//always know what to tell the job.
					setThread(Thread.currentThread());
					result = runInUIThread(monitor);
					
					//Debug testing for instrumenting UI jobs
					if (Policy.DEBUG_LONG_UI_WARNING) {
						long elapsed = System.currentTimeMillis() - startTime;
						if (elapsed > 100) {
							WorkbenchPlugin.log(ProgressMessages.format(
									"UIJob.longJobMessage", new Object[]{ //$NON-NLS-1$
									getName(), String.valueOf(elapsed)}));
						}
					}
				} finally {
					if (result == null)
						result = new Status(IStatus.ERROR,
								PlatformUI.PLUGIN_ID, IStatus.ERROR,
								ProgressMessages.getString("Error"), //$NON-NLS-1$
								null);
					done(result);
				}
			}
		});
		return Job.ASYNC_FINISH;
	}
	/**
	 * Run the job in the UI Thread.
	 * 
	 * @param monitor
	 * @return IStatus
	 */
	public abstract IStatus runInUIThread(IProgressMonitor monitor);
	/**
	 * Sets the display to execute the asyncExec in.
	 * 
	 * @param runDisplay
	 *            Display
	 */
	public void setDisplay(Display runDisplay) {
		Assert.isNotNull(runDisplay);
		cachedDisplay = runDisplay;
	}
	/**
	 * Returns the display for use by the receiver.
	 * 
	 * @return Display or <code>null</code>.
	 */
	public Display getDisplay() {
		//If it was not set get it from the workbench
		if (cachedDisplay == null && PlatformUI.isWorkbenchRunning())
			return PlatformUI.getWorkbench().getDisplay();
		return cachedDisplay;
	}
}