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

import java.lang.reflect.InvocationTargetException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

/**
 * The IProgressManager is an interface to the progress manager provided by the
 * workbench. 
 * <b>NOTE</b> This interface is not intended to be implemented
 * by other plug-ins.
 * 
 * @see org.eclipse.ui.IWorkbench#getProgressService. 
 * @since 3.0
 */
public interface IProgressService extends IRunnableContext {

	/**
	 * The time at which an operation becomes considered a long
	 * operation. Used to determine when the busy cursor will 
	 * be replaced with a progress monitor.
	 * @return int 
	 * @see IProgressService#busyCursorWhile(IRunnableWithProgress)
	 */
	public int getLongOperationTime();
	
	/**
	 * Register the ImageDescriptor to be the icon used for
	 * all jobs that belong to family within the workbench.
	 * @param icon ImageDescriptor that will be used when the job is being displayed
	 * @param family The family to associate with
	 * @see Job#belongsTo(Object)
	 */
	public void registerIconForFamily(ImageDescriptor icon, Object family);
	/**
	 * Runs the given operation in the UI thread using the given runnable context.  
	 * The given scheduling rule, if any, will be acquired for the duration of the operation. 
	 * If the rule is not available when this method is called, a progress dialog will be 
	 * displayed that gives users control over the background processes that may 
	 * be blocking the runnable from proceeding.
	 * <p>
	 * This method can act as a wrapper for uses of <tt>IRunnableContext</tt>
	 * where the <tt>fork</tt> parameter was <tt>false</tt>. 
	 * <p>
	 * Note: Running long operations in the UI thread is generally not 
	 * recommended. This can result in the UI becoming unresponsive for
	 * the duration of the operation. Where possible, <tt>busyCursorWhile</tt>
	 * should be used instead.
	 * 
	 * @param context The runnable context to run the operation in
	 * @param runnable The operation to run
	 * @param rule A scheduling rule, or <code>null</code>
	 * @throws InvocationTargetException wraps any exception or error which occurs 
	 *  while running the runnable
	 * @throws InterruptedException propagated by the context if the runnable 
	 *  acknowledges cancelation by throwing this exception.
	 */
	public void runInUI(IRunnableContext context, IRunnableWithProgress runnable, ISchedulingRule rule) throws InvocationTargetException, InterruptedException;

	
	/**
	 * Get the icon that has been registered for a Job by
	 * checking if the job belongs to any of the registered 
	 * families.
	 * @param job
	 * @return Icon or <code>null</code> if there isn't one.
	 * @see IProgressService#registerIconForFamily(ImageDescriptor,Object)
	 */
	public Image getIconFor(Job job);

	/**
	 * Set the cursor to busy and run the runnable in the Thread that it
	 * is called in. After the cursor has been running for 
	 * <code>getLongOperationTime()<code> replace it with
	 * a ProgressMonitorDialog so that the user may cancel.
	 * Do not open the ProgressMonitorDialog if there is already a modal
	 * dialog open.
	 * 
	 * @param runnable The runnable to execute and show the progress for.
	 * @see IProgressService#getLongOperationTime
	 * @throws InvocationTargetException
	 * @throws InterruptedException
	 */
	public void busyCursorWhile(IRunnableWithProgress runnable)
		throws InvocationTargetException, InterruptedException;
	
	/**
	 * Open a dialog on job when it starts to run and close it 
	 * when the job is finished. Wait for LONG_OPERATION_MILLISECONDS
	 * before opening the dialog. Do not open if it is already done.
	 * 
	 * Parent the dialog from the shell.
	 * 
	 * @param shell The Shell to parent the dialog from or 
	 * <code>null</code> if the active shell is to be used.
	 * @param job The Job that will be reported in the dialog. job
	 * must not be <code>null</code>.
	 */
	public void showInDialog(Shell shell, Job job);

}
