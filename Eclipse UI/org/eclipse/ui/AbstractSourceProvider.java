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

package org.eclipse.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.commands.util.Tracing;
import org.eclipse.ui.internal.misc.Policy;

/**
 * <p>
 * An implementation of <code>ISourceProvider</code> that provides listener
 * support. Subclasses need only call <code>fireSourceChanged</code> whenever
 * appropriate.
 * </p>
 * 
 * @since 3.1
 */
public abstract class AbstractSourceProvider implements ISourceProvider {

	/**
	 * Whether source providers should print out debugging information to the
	 * console when events arrive.
	 * 
	 * @since 3.2
	 */
	protected static boolean DEBUG = Policy.DEBUG_SOURCES;

	/**
	 * The listeners to this source provider. This value is <code>null</code>
	 * if there are no listeners.
	 */
	private Collection listeners = null;

	public final void addSourceProviderListener(
			final ISourceProviderListener listener) {
		if (listener == null) {
			throw new NullPointerException("The listener cannot be null"); //$NON-NLS-1$
		}

		if (listeners == null) {
			listeners = new ArrayList(1);
		} else if (listeners.contains(listener)) {
			return;
		}

		listeners.add(listener);
	}

	/**
	 * Notifies all listeners that a single source has changed.
	 * 
	 * @param sourcePriority
	 *            The source priority that has changed.
	 * @param sourceName
	 *            The name of the source that has changed; must not be
	 *            <code>null</code>.
	 * @param sourceValue
	 *            The new value for the source; may be <code>null</code>.
	 */
	protected final void fireSourceChanged(final int sourcePriority,
			final String sourceName, final Object sourceValue) {
		if ((listeners != null) && (!listeners.isEmpty())) {
			final Iterator listenerItr = listeners.iterator();
			while (listenerItr.hasNext()) {
				final ISourceProviderListener listener = (ISourceProviderListener) listenerItr
						.next();
				listener.sourceChanged(sourcePriority, sourceName, sourceValue);
			}
		}
	}

	/**
	 * Notifies all listeners that multiple sources have changed.
	 * 
	 * @param sourcePriority
	 *            The source priority that has changed.
	 * @param sourceValuesByName
	 *            The map of source names (<code>String</code>) to source
	 *            values (<code>Object</code>) that have changed; must not
	 *            be <code>null</code>. The names must not be
	 *            <code>null</code>, but the values may be <code>null</code>.
	 */
	protected final void fireSourceChanged(final int sourcePriority,
			final Map sourceValuesByName) {
		if ((listeners != null) && (!listeners.isEmpty())) {
			final Iterator listenerItr = listeners.iterator();
			while (listenerItr.hasNext()) {
				final ISourceProviderListener listener = (ISourceProviderListener) listenerItr
						.next();
				listener.sourceChanged(sourcePriority, sourceValuesByName);
			}
		}
	}

	/**
	 * Logs a debugging message in an appropriate manner. If the message is
	 * <code>null</code> or the <code>DEBUG</code> is <code>false</code>,
	 * then this method does nothing.
	 * 
	 * @param message
	 *            The debugging message to log; if <code>null</code>, then
	 *            nothing is logged.
	 * @since 3.2
	 */
	protected final void logDebuggingInfo(final String message) {
		if (DEBUG && (message != null)) {
			Tracing.printTrace("SOURCES", message); //$NON-NLS-1$
		}
	}

	public final void removeSourceProviderListener(
			final ISourceProviderListener listener) {
		if (listener == null) {
			throw new NullPointerException("The listener cannot be null"); //$NON-NLS-1$
		}

		if (listeners != null) {
			listeners.remove(listener);
			if (listeners.isEmpty()) {
				listeners = null;
			}
		}
	}

}
