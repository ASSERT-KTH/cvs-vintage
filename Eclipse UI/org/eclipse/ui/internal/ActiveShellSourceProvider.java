/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.contexts.IWorkbenchContextSupport;

/**
 * A provider of notifications for when the active shell changes.
 * 
 * @since 3.1
 */
public final class ActiveShellSourceProvider extends AbstractSourceProvider {

	/**
	 * The display on which this provider is working.
	 */
	private final Display display;

	/**
	 * The last shell seen as active by this provider. This value may be
	 * <code>null</code> if the last call to
	 * <code>Display.getActiveShell()</code> returned <code>null</code>.
	 */
	private Shell lastActiveShell = null;

	/**
	 * The last workbench window shell seen as active by this provider. This
	 * value may be <code>null</code> if the last call to
	 * <code>workbench.getActiveWorkbenchWindow()</code> returned
	 * <code>null</code>.
	 */
	private Shell lastActiveWorkbenchWindow = null;

	/**
	 * The listener to shell activations on the display.
	 */
	private final Listener listener = new Listener() {
		/**
		 * Notifies all listeners that the source has changed.
		 */
		public final void handleEvent(final Event event) {
			final Map currentState = getCurrentState();
			final Shell newActiveShell = (Shell) currentState
					.get(ISources.ACTIVE_SHELL_NAME);
			final Shell newActiveWorkbenchWindowShell = (Shell) currentState
					.get(ISources.ACTIVE_WORKBENCH_WINDOW_NAME);

			// Figure out which variables have changed.
			final boolean shellChanged = newActiveShell != lastActiveShell;
			final boolean windowChanged = newActiveWorkbenchWindowShell != lastActiveWorkbenchWindow;

			// Fire an event for those sources that have changed.
			if (shellChanged && windowChanged) {
				final Map sourceValuesByName = new HashMap(4);
				sourceValuesByName.put(ISources.ACTIVE_SHELL_NAME,
						newActiveShell);
				sourceValuesByName.put(ISources.ACTIVE_WORKBENCH_WINDOW_NAME,
						newActiveWorkbenchWindowShell);
				fireSourceChanged(ISources.ACTIVE_SHELL
						| ISources.ACTIVE_WORKBENCH_WINDOW, sourceValuesByName);
			} else if (shellChanged) {
				fireSourceChanged(ISources.ACTIVE_SHELL,
						ISources.ACTIVE_SHELL_NAME, newActiveShell);
			} else if (windowChanged) {
				fireSourceChanged(ISources.ACTIVE_WORKBENCH_WINDOW,
						ISources.ACTIVE_WORKBENCH_WINDOW_NAME,
						newActiveWorkbenchWindowShell);
			}

			// Update the member variables.
			lastActiveShell = newActiveShell;
			lastActiveWorkbenchWindow = newActiveWorkbenchWindowShell;
		}
	};

	/**
	 * The workbench on which to work; never <code>null</code>.
	 */
	private final IWorkbench workbench;

	/**
	 * Constructs a new instance of <code>ShellSourceProvider</code>.
	 * 
	 * @param workbench
	 *            The workbench on which to monitor shell activations; must not
	 *            be <code>null</code>.
	 */
	public ActiveShellSourceProvider(final IWorkbench workbench) {
		this.workbench = workbench;
		this.display = workbench.getDisplay();
		this.display.addFilter(SWT.Activate, listener);
	}

	public final void dispose() {
		display.removeFilter(SWT.Activate, listener);
	}

	public final Map getCurrentState() {
		final Map currentState = new HashMap(4);

		final Shell newActiveShell = display.getActiveShell();
		currentState.put(ISources.ACTIVE_SHELL_NAME, newActiveShell);

		/*
		 * We will fallback to the workbench window, but only if a dialog is not
		 * open.
		 */
		final int shellType = workbench.getContextSupport().getShellType(
				newActiveShell);
		if (shellType != IWorkbenchContextSupport.TYPE_DIALOG) {
			final IWorkbenchWindow newActiveWorkbenchWindow = workbench
					.getActiveWorkbenchWindow();
			final Shell newActiveWorkbenchWindowShell;
			if (newActiveWorkbenchWindow == null) {
				newActiveWorkbenchWindowShell = null;
			} else {
				newActiveWorkbenchWindowShell = newActiveWorkbenchWindow
						.getShell();
			}
			currentState.put(ISources.ACTIVE_WORKBENCH_WINDOW_NAME,
					newActiveWorkbenchWindowShell);
		}

		return currentState;
	}
}
