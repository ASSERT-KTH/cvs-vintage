/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.contexts;

import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.ui.contexts.IContext;
import org.eclipse.ui.contexts.IContextListener;
import org.eclipse.ui.contexts.NotDefinedException;

/**
 * This implements the old <code>IContext</code> interface based on the new
 * context implementation in <code>org.eclipse.ui.contexts</code>. This is a
 * wrapper.
 * 
 * @since 3.1
 */
public class ContextWrapper implements IContext {

	/**
	 * The context manager that maintains the set of active contexts; must not
	 * be <code>null</code>.
	 */
	private final ContextManager contextManager;
	
	/**
	 * The wrapped instance of context. This value will never be
	 * <code>null</code>.
	 */
	private final Context wrappedContext;

	/**
	 * Constructs a new instance of <code>ContextWrapper</code>.
	 * 
	 * @param context
	 *            The context to wrapper; must not be <code>null</code>.
	 * @param contextManager
	 *            The context manager that maintains the set of active contexts;
	 *            must not be <code>null</code>.
	 */
	public ContextWrapper(final Context context,
			final ContextManager contextManager) {
		if (context == null) {
			throw new NullPointerException(
					"A wrapper cannot be created on a null context"); //$NON-NLS-1$
		}
		
		if (contextManager == null) {
			throw new NullPointerException(
					"A wrapper cannot be created with a null manager"); //$NON-NLS-1$
		}

		wrappedContext = context;
		this.contextManager = contextManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.contexts.IContext#addContextListener(org.eclipse.ui.contexts.IContextListener)
	 */
	public void addContextListener(IContextListener contextListener) {
        wrappedContext.addContextListener(new ContextListenerWrapper(
                contextListener, contextManager));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(T)
	 */
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.contexts.IContext#getId()
	 */
	public String getId() {
		return wrappedContext.getId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.contexts.IContext#getName()
	 */
	public String getName() throws NotDefinedException {
		try {
			return wrappedContext.getName();
		} catch (final org.eclipse.core.commands.common.NotDefinedException e) {
			throw new NotDefinedException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.contexts.IContext#getParentId()
	 */
	public String getParentId() throws NotDefinedException {
		try {
			return wrappedContext.getParentId();
		} catch (final org.eclipse.core.commands.common.NotDefinedException e) {
			throw new NotDefinedException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.contexts.IContext#isDefined()
	 */
	public boolean isDefined() {
		return wrappedContext.isDefined();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.contexts.IContext#isEnabled()
	 */
	public boolean isEnabled() {
		return contextManager.getActiveContextIds().contains(
				wrappedContext.getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.contexts.IContext#removeContextListener(org.eclipse.ui.contexts.IContextListener)
	 */
	public void removeContextListener(IContextListener contextListener) {
        wrappedContext.removeContextListener(new ContextListenerWrapper(
                contextListener, contextManager));
	}

}
