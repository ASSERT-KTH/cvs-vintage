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

package org.eclipse.ui.application;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * Public base class for configuring the workbench.
 * <p>
 * Note that the workbench adviser object is created in advance of creating the
 * workbench. However, by the time the workbench starts calling methods on this
 * class, <code>PlatformUI.getWorkbench</code> is guaranteed to have been
 * properly initialized.
 * </p>
 * <p>
 * Example of creating and running a workbench (in an
 * <code>IPlatformRunnable</code>):
 * <pre>
 * <code>
 * public class MyApplication implements IPlatformRunnable {
 *   public Object run(Object args) {
 *     WorkbenchAdviser workbenchAdviser = new MyWorkbenchAdviser();
 *     Display display = PlatformUI.createDisplay();
 *     int returnCode = PlatformUI.createAndRunWorkbench(display, workbenchAdviser);
 *     if (returnCode == PlatformUI.RETURN_RESTART) {
 *        return IPlatformRunnable.EXIT_RESTART;
 *     } else {
 *        return IPlatformRunnable.EXIT_OK;
 *   }
 * }
 * </code>
 * </pre>
 * </p>
 * <p>
 * An application should declare a subclass of <code>WorkbenchAdviser</code>
 * and override methods to configure the workbench to suit the needs of the
 * particular application.
 * </p>
 * <p>
 * The following advisor methods are called at strategic points in the
 * workbench's lifecycle (all occur within the dynamic scope of the call
 * to {@link PlatformUI#createAndRunWorkbench PlatformUI.createAndRunWorkbench}):
 * <ul>
 * <li><code>initialize</code> - called first; before any windows; use to
 * register things</li>
 * <li><code>preStartup</code> - called second; after initialize but
 * before first window is opened; use to temporarily disable things during
 * startup or restore</li>
 * <li><code>postStartup</code> - called third; after first window is
 * opened; use to reenable things temporarily disabled in previous step</li>
 * <li><code>postRestore</code> - called after the workbench and its windows
 * has been recreated from a previously saved state; use to adjust the
 * restored workbench</li>
 * <li><code>preWindowOpen</code> - called as each window is being opened; 
 *  use to configure aspects of the window other than actions bars </li>
 * <li><code>fillActionBars</code> - called after <code>preWindowOpen</code> to
 * configure a window's action bars</li>
 * <li><code>postWindowRestore</code> - called after a window has been
 * recreated from a previously saved state; use to adjust the restored
 * window</li>
 * <li><code>postWindowOpen</code> - called after a window has been
 * opened; use to hook window listeners, etc.</li>
 * <li><code>preWindowShellClose</code> - called when a window's shell
 * is closed by the user; use to pre-screen window closings</li>
 * <li><code>eventLoopException</code> - called to handle the case where the
 * event loop has crashed; use to inform the user that things are not well</li>
 * <li><code>eventLoopIdle</code> - called when there are currently no more
 * events to be processed; use to perform other work or to yield until new
 * events enter the queue</li>
 * <li><code>preShutdown</code> - called just after event loop has terminated
 * but before any windows have been closed; use to deregister things registered
 * during initialize</li>
 * <li><code>postShutdown</code> - called last; after event loop has terminated
 * and all windows have been closed; use to deregister things registered during
 * initialize</li>
 * </ul>
 * </p>
 * 
 * @since 3.0
 */
public abstract class WorkbenchAdviser {
	
	/**
	 * Bit flag for {@link #fillActionBars fillActionBars} indicating that the
	 * operation is not filling the action bars of an actual workbench window,
	 * but rather a proxy (used for perspective customization).
	 */
	public static final int FILL_PROXY = 0x01;

	/**
	 * Bit flag for {@link #fillActionBars fillActionBars} indicating that the
	 * operation is supposed to fill (or describe) the workbench window's menu
	 * bar.
	 */
	public static final int FILL_MENU_BAR = 0x02;
	
	/**
	 * Bit flag for {@link #fillActionBars fillActionBars} indicating that the
	 * operation is supposed to fill (or describe) the workbench window's tool
	 * bar.
	 */
	public static final int FILL_TOOL_BAR = 0x04;

	/**
	 * Bit flag for {@link #fillActionBars fillActionBars} indicating that the
	 * operation is supposed to fill (or describe) the workbench window's status
	 * line.
	 */
	public static final int FILL_STATUS_LINE = 0x08;

	/**
	 * Creates and initializes a new workbench adviser instance.
	 */
	protected WorkbenchAdviser() {
		// do nothing
	}

	/**
	 * Performs arbitrary initialization before the workbench starts running.
	 * <p>
	 * This method is called during workbench initialization prior to any
	 * windows being opened. 
	 * Clients must not call this method directly (although super calls are okay).
	 * The default implementation does nothing. Subclasses may override. 
	 * Typical clients will use the configurer passed in to tweak the
	 * workbench, and hang on to the configurer if further tweaking may be
	 * required in the future.
	 * </p>
	 * 
	 * @param configurer an object for configuring the workbench
	 */
	public void initialize(IWorkbenchConfigurer configurer) {
		// do nothing
	}

	/**
	 * Performs arbitrary actions just before the first workbench window is
	 * opened (or restored).
	 * <p>
	 * This method is called after the workbench has been initialized and
	 * just before the first window is about to be opened.
	 * Clients must not call this method directly (although super calls are okay).
	 * The default implementation does nothing. Subclasses may override.
	 * </p>
	 */
	public void preStartup() {
		// do nothing
	}

	/**
	 * Performs arbitrary actions after the workbench windows have been
	 * opened (or restored), but before the main event loop is run.
	 * <p>
	 * This method is called just after the windows have been opened.
	 * Clients must not call this method directly (although super calls are okay).
	 * The default implementation does nothing. Subclasses may override.
	 * It is okay to <code>IWorkbench.close()</code> from this method.
	 * </p>
	 */
	public void postStartup() {
		// do nothing
	}

	/**
	 * Performs arbitrary actions after the workbench and its windows have been
	 * restored, but before the main event loop is run.
	 * <p>
	 * This method is called after previously-saved windows have been recreated,
	 * and before <code>postStartup</code>. This method is not called when the
	 * workbench is started for the very first time, or if workbench state is
	 * not saved or restored.
	 * Clients must not call this method directly (although super calls are okay).
	 * The default implementation does nothing. Subclasses may override.
	 * It is okay to <code>IWorkbench.close()</code> from this method.
	 * </p>
	 */
	public void postRestore() {
		// do nothing
	}

	/**
	 * Performs arbitrary finalization before the workbench is about to
	 * shut down.
	 * <p>
	 * This method is called immediately prior to workbench shutdown before any
	 * windows have been closed.
	 * Clients must not call this method directly (although super calls are okay).
	 * The default implementation does nothing. Subclasses may override.
	 * </p>
	 * 
	 * @issue veto?
	 * @issue about to close last window - window closing
	 */
	public void preShutdown() {
		// do nothing
	}
	
	/**
	 * Performs arbitrary finalization after the workbench stops running.
	 * <p>
	 * This method is called during workbench shutdown after all windows
	 * have been closed.
	 * Clients must not call this method directly (although super calls are okay).
	 * The default implementation does nothing. Subclasses may override.
	 * </p>
	 */
	public void postShutdown() {
		// do nothing
	}
	
	/**
	 * Performs arbitrary actions when the event loop crashes (the code that
	 * handles a UI event throws an exception that is not caught).
	 * <p>
	 * This method is called when the code handling a UI event throws an
	 * exception. In a perfectly functioning application, this method would
	 * never be called. In practice, it comes into play when there is bugs
	 * in the code that trigger unchecked runtime exceptions. It is also
	 * activated when the system runs short of memory, etc. 
	 * Fatal errors (ThreadDeath) are not passed on to this method, as there
	 * is nothing that could be done.
	 * </p>
	 * <p>
	 * Clients must not call this method directly (although super calls are okay).
	 * The default implementation logs the problem so that it does not go
	 * unnoticed. Subclasses may override or extend this method. It is generally
	 * a bad idea to override with an empty method, and you should be
	 * especially careful when handling Errors.
	 * </p>
	 * 
	 * @param exception the uncaught exception that was thrown inside the UI
	 * event loop
	 */
	public void eventLoopException(Throwable exception) {
		// Protection from client doing super(null) call
		if (exception == null) {
			return;
		}
		
		try {
			// Log the exception
			String msg = exception.getMessage();
			if (msg == null) {
				msg = exception.toString();
			}
			WorkbenchPlugin.log(
				"Unhandled event loop exception", //$NON-NLS-1$
				new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, msg, exception));
	
			// Handle nested exception from SWT (see bug 6312)
			Throwable nested = null;
			if (exception instanceof SWTException) {
				nested = ((SWTException)exception).throwable;
			} else if (exception instanceof SWTError) {
				nested = ((SWTError)exception).throwable;
			}
			if (nested != null) {
				msg = nested.getMessage();
				if (msg == null) {
					msg = nested.toString();
				}
				WorkbenchPlugin.log(
					"*** SWT nested exception", //$NON-NLS-1$
					new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, msg, nested));
			}
			
			// Print it onto the console if debugging
			if (WorkbenchPlugin.DEBUG) {
				exception.printStackTrace();
			}
		} catch (Throwable e) {
			// One of the log listeners probably failed. Core should have logged the
			// exception since its the first listener.
			System.err.println("Error while logging event loop exception:"); //$NON-NLS-1$
			exception.printStackTrace();
			System.err.println("Logging exception:"); //$NON-NLS-1$
			e.printStackTrace();
		}
	}

	/**
	 * Performs arbitrary work or yields when there are no events to be processed.
	 * <p>
	 * This method is called when there are currently no more events on the queue
	 * to be processed at the moment. 
	 * </p><p>
	 * Clients must not call this method directly (although super calls are okay).
	 * The default implementation yields until new events enter the queue.
	 * Subclasses may override or extend this method. It is generally
	 * a bad idea to override with an empty method. 
	 * It is okay to call <code>IWorkbench.close()</code> from this method.
	 * </p>
	 * @param display the main display of the workbench UI
	 */
	public void eventLoopIdle(Display display) {
		// default: yield cpu until new events enter the queue
		display.sleep();
	}
	
	/**
	 * Performs arbitrary actions before the given workbench window is
	 * opened.
	 * <p>
	 * This method is called before the window's controls have been created.
	 * Clients must not call this method directly (although super calls are okay).
	 * The default implementation does nothing. Subclasses may override.
	 * Typical clients will use the configurer passed in to tweak the
	 * workbench window in an application-specific way; however, filling the
	 * window's menu bar, tool bar, and status line must be done in 
	 * {@link #fillActionBars fillActionBars}, which is called immediately
	 * after this method is called.
	 * </p>
	 * 
	 * @param configurer an object for configuring the particular workbench
	 * window being opened
	 */
	public void preWindowOpen(IWorkbenchWindowConfigurer configurer) {
		// do nothing
	}
	
	/**
	 * Configures the action bars using the given action bar configurer.
	 * Under normal circumstances, <code>flags</code> does not include
	 * <code>FILL_PROXY</code>, meaning this is a request to fill the actions\
	 * bars of the given workbench window; the
	 * remaining flags indicate which combination of
	 * the menu bar (<code>FILL_MENU_BAR</code>),
	 * the tool bar (<code>FILL_TOOL_BAR</code>),
	 * and the status line (<code>FILL_STATUS_LINE</code>) are to be filled.
	 * <p>
	 * If <code>flags</code> does include <code>FILL_PROXY</code>, then this
	 * is a request to describe the actions bars of the given workbench window
	 * (which will already have been filled);
	 * again, the remaining flags indicate which combination of the menu bar,
	 * the tool bar, and the status line are to be described.
	 * The actions included in the proxy action bars can be the same instances
	 * as in the actual window's action bars. Calling <code>ActionFactory</code>
	 * to create new action instances it is not recommended, because these
	 * actions interally register listeners with the window and there is no
	 * opportunity to dispose of these actions.
	 * </p>
	 * <p>
	 * This method is called just after {@link #preWindowOpen preWindowOpen}.
	 * Clients must not call this method directly (although super calls are okay).
	 * The default implementation does nothing. Subclasses may override.
	 * </p>
	 * 
	 * @param window the workbench window
	 * @param configurer the action bar configurer object
	 * @param flags bit mask composed from the constants
	 * {@link #FILL_MENU_BAR FILL_MENU_BAR}</code>,
	 * {@link #FILL_TOOL_BAR FILL_TOOL_BAR}</code>,
	 * {@link #FILL_STATUS_LINE FILL_STATUS_LINE}</code>,
	 * and {@link #FILL_PROXY FILL_PROXY}</code>
	 * @issue should 1st param be IWorkbenchWindowConfigurer to be more consistent with other methods?
	 * @issue suggest adding ActionBuilder as API, to encapsulate the action building outside 
	 *   of the adviser, and to handle the common pattern of hanging onto the action builder
	 *   in order to properly handle FILL_PROXY 
	 */
	public void fillActionBars(IWorkbenchWindow window, IActionBarConfigurer configurer, int flags) {
		  // do nothing by default
	}

	/**
	 * Performs arbitrary actions after the given workbench window has been
	 * restored, but before it is opened.
	 * <p>
	 * This method is called after a previously-saved window have been
	 * recreated. This method is not called when a new window is created from
	 * scratch. This method is never called when a workbench is started for the
	 * very first time, or when workbench state is not saved or restored.
	 * Clients must not call this method directly (although super calls are okay).
	 * The default implementation does nothing. Subclasses may override.
	 * It is okay to <code>IWorkbench.close()</code> from this method.
	 * </p>
	 * 
	 * @param configurer an object for configuring the particular workbench
	 * window just restored
     * @issue document checked exception
	 */
	public void postWindowRestore(IWorkbenchWindowConfigurer configurer) throws WorkbenchException {
		// do nothing
	}

	/**
	 * Performs arbitrary actions after the given workbench window has been
	 * opened (possibly after being restored).
	 * <p>
	 * This method is called after a window have been opened. This method is 
	 * called after a new window has been created from scratch, or when
	 * a previously-saved window has been restored.
	 * Clients must not call this method directly (although super calls are okay).
	 * The default implementation does nothing. Subclasses may override.
	 * </p>
	 * 
	 * @param configurer an object for configuring the particular workbench
	 * window just opened
	 */
	public void postWindowOpen(IWorkbenchWindowConfigurer configurer) {
		// do nothing
	}
	
	/**
	 * Performs arbitrary actions as the given workbench window's shell is being
	 * closed directly, and possibly veto the close.
	 * <p>
	 * This method is called from a ShellListener associated with the workbench
	 * window. It is not called when the window is being closed for other reasons.
	 * Clients must not call this method directly (although super calls are okay).
	 * The default implementation does nothing. Subclasses may override.
	 * Typical clients may use the configurer passed in to access the
	 * workbench window being closed. If this method
	 * returns <code>false</code>, then the user's request to close the shell is
	 * ignored. This gives the workbench adviser an opportunity to query the user
	 * and/or veto the closing of a window under some circumstances.
	 * </p>
	 * 
	 * @param configurer an object for configuring the particular workbench
	 * window whose shell is being closed
	 * @return <code>true</code> to allow the window to close, 
	 * and <code>false</code> to prevent the window from closing
	 * @see org.eclipse.ui.IWorkbenchWindow#close
	 */
	public boolean preWindowShellClose(IWorkbenchWindowConfigurer configurer) {
		// do nothing, but allow the close() to proceed
		return true;
	}
	
	/**
	 * Performs arbitrary actions after the given workbench window is
	 * closed.
	 * <p>
	 * This method is called after the window's controls have been disposed.
	 * Clients must not call this method directly (although super calls are okay).
	 * The default implementation does nothing. Subclasses may override.
	 * Typical clients will use the configurer passed in to tweak the
	 * workbench window in an application-specific way.
	 * </p>
	 * 
	 * @param configurer an object for configuring the particular workbench
	 * window being closed
	 */
	public void postWindowClose(IWorkbenchWindowConfigurer configurer) {
		// do nothing
	}

	/**
	 * Returns whether the menu with the given id is an application menu of the
	 * given window. This is used during OLE "in place" editing.  Application
	 * menus should be preserved during menu merging. All other menus may be
	 * removed from the window.
	 * <p>
	 * The default implementation returns false. Subclasses may override.
	 * </p>
	 * 
	 * @param configurer an object for configuring the workbench window
	 * @param menuId the menu id
	 * @return <code>true</code> for application menus, and <code>false</code>
	 * for part-specific menus
	 */
	public boolean isApplicationMenu(IWorkbenchWindowConfigurer configurer, String menuId) {
		// default: not an application menu
		return false;
	}
	
	/**
	 * Returns the default input for newly created workbench pages.
	 * <p>
	 * The default implementation returns <code>null</code>.
	 * Subclasses may override.
	 * </p>
	 * 
	 * @return the default input for a new workbench window page, or
	 * <code>null</code> if none
	 */
	public IAdaptable getDefaultWindowInput() {
		// default: no input
		return null;
	}
	
	/**
	 * Returns the id of the initial perspective for new workbench windows.
	 * <p>
	 * This method is called during startup when the workbench is restoring
	 * the window(s) or creating a new window. The default implementation
	 * returns <code>null</code>. Subclasses may override.
	 * </p>
	 * 
	 * @return the id of the initial perspective, or <code>null</code> if none
	 * @issue if this returns null, we're in trouble?
	 */
	public String getInitialWindowPerspectiveId() {
		// default: no initial perspective
		return null;
	}
}

