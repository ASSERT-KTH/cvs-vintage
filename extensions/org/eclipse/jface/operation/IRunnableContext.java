package org.eclipse.jface.operation;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.lang.reflect.InvocationTargetException;

/**
 * Interface for UI components which can execute a long-running operation
 * in the form of an <code>IRunnableWithProgress</code>.
 * The context is responsible for displaying a progress indicator and Cancel
 * button to the end user while the operation is in progress; the context
 * supplies a progress monitor to be used from code running inside the operation.
 * Note that an <code>IRunnableContext</code> is not a runnable itself.
 * <p>
 * For examples of UI components which implement this interface,
 * see <code>ApplicationWindow</code>, <code>ProgressMonitorDialog</code>,
 * and <code>WizardDialog</code>.
 * </p>
 *
 * @see IRunnableWithProgress
 * @see org.eclipse.jface.window.ApplicationWindow
 * @see org.eclipse.jface.dialogs.ProgressMonitorDialog
 * @see org.eclipse.jface.wizard.WizardDialog
 */
public interface IRunnableContext {
/**
 * Runs the given <code>IRunnableWithProgress</code> in this context.
 * For example, if this is a <code>ProgressMonitorDialog</code> then the runnable
 * is run using this dialog's progress monitor.
 *
 * @param fork <code>true</code> if the runnable should be run in a separate thread,
 *  and <code>false</code> to run in the same thread
 * @param cancelable <code>true</code> to enable the cancelation, and
 *  <code>false</code> to make the operation uncancellable
 * @param runnable the runnable to run
 *
 * @exception InvocationTargetException wraps any exception or error which occurs 
 *  while running the runnable
 * @exception InterruptedException propagated by the context if the runnable 
 *  acknowledges cancelation by throwing this exception.  This should not be thrown
 *  if cancelable is <code>false</code>.
 */
public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException;
}
