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

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.core.expressions.Expression;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;

/**
 * <p>
 * Provides services related to contexts in the Eclipse workbench. This provides
 * access to contexts.
 * </p>
 * 
 * @since 3.1
 */
public final class ContextService implements IContextService {

	/**
	 * The central authority for determining which context we should use.
	 */
	private final ContextAuthority contextAuthority;

	/**
	 * The context manager that supports this service. This value is never
	 * <code>null</code>.
	 */
	private final ContextManager contextManager;

	/**
	 * Constructs a new instance of <code>ContextService</code> using a
	 * context manager.
	 * 
	 * @param contextManager
	 *            The context manager to use; must not be <code>null</code>.
	 */
	public ContextService(final ContextManager contextManager) {
		if (contextManager == null) {
			throw new NullPointerException(
					"Cannot create a context service with a null manager"); //$NON-NLS-1$
		}
		this.contextManager = contextManager;
		this.contextAuthority = new ContextAuthority(contextManager, this);
	}

	public final IContextActivation activateContext(final String contextId) {
		final IContextActivation activation = new ContextActivation(contextId,
				null, ISources.WORKBENCH, this);
		contextAuthority.activateContext(activation);
		return activation;
	}

	public final IContextActivation activateContext(final String contextId,
			final Expression expression, final int sourcePriority) {
		if (expression == null) {
			throw new NullPointerException("The expression cannot be null"); //$NON-NLS-1$
		}
		final IContextActivation activation = new ContextActivation(contextId,
				expression, sourcePriority, this);
		contextAuthority.activateContext(activation);
		return activation;
	}

	public final void addSourceProvider(final ISourceProvider provider) {
		contextAuthority.addSourceProvider(provider);
	}

	public final void deactivateContext(final IContextActivation activation) {
		if (activation.getContextService() == this) {
			contextAuthority.deactivateContext(activation);
		}
	}

	public final void deactivateContexts(final Collection activations) {
		final Iterator activationItr = activations.iterator();
		while (activationItr.hasNext()) {
			final IContextActivation activation = (IContextActivation) activationItr
					.next();
			deactivateContext(activation);
		}
	}

	public final Context getContext(final String contextId) {
		/*
		 * TODO Need to put in place protection against the context being
		 * changed.
		 */
		return contextManager.getContext(contextId);
	}

	public final Collection getDefinedContextIds() {
		return contextManager.getDefinedContextIds();
	}

	public final int getShellType(final Shell shell) {
		return contextAuthority.getShellType(shell);
	}

	public final void readRegistry() {
		ContextPersistence.read(contextManager);
	}

	public final boolean registerShell(final Shell shell, final int type) {
		return contextAuthority.registerShell(shell, type);
	}

	public final void removeSourceProvider(final ISourceProvider provider) {
		contextAuthority.removeSourceProvider(provider);
	}

	public final boolean unregisterShell(final Shell shell) {
		return contextAuthority.unregisterShell(shell);
	}
}
