/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;

/**
 * A handler that wraps a legacy handler. This provide backward compatibility
 * with the handlers release in Eclipse 3.0.
 * 
 * @since 3.1
 */
public final class LegacyHandlerWrapper implements IHandler {

	/**
	 * The wrapped handler; never <code>null</code>.
	 */
	private final org.eclipse.ui.commands.IHandler handler;

	/**
	 * Constructs a new instance of <code>HandlerWrapper</code>.
	 * 
	 * @param handler
	 *            The handler that should be wrapped; must not be
	 *            <code>null</code>.
	 */
	public LegacyHandlerWrapper(final org.eclipse.ui.commands.IHandler handler) {
		if (handler == null) {
			throw new NullPointerException(
					"A handler wrapper cannot be constructed on a null handler"); //$NON-NLS-1$
		}

		this.handler = handler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.IHandler#addHandlerListener(org.eclipse.core.commands.IHandlerListener)
	 */
	public void addHandlerListener(IHandlerListener handlerListener) {
		handler.addHandlerListener(new LegacyHandlerListenerWrapper(this,
				handlerListener));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.IHandler#dispose()
	 */
	public void dispose() {
		handler.dispose();
	}

	public final boolean equals(final Object object) {
		if (object instanceof org.eclipse.ui.commands.IHandler) {
			return this.handler == object;
		}

		if (object instanceof LegacyHandlerWrapper) {
			return this.handler == ((LegacyHandlerWrapper) object).handler;
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			return handler.execute(event.getParameters());
		} catch (final org.eclipse.ui.commands.ExecutionException e) {
			throw new ExecutionException(e.getMessage(), e.getCause());
		}
	}

	public final int hashCode() {
		return this.handler.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.IHandler#isEnabled()
	 */
	public boolean isEnabled() {
		final Object enabled = handler.getAttributeValuesByName().get(
				ILegacyAttributeNames.ENABLED);
		if (enabled instanceof Boolean) {
			return ((Boolean) enabled).booleanValue();
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.IHandler#isHandled()
	 */
	public boolean isHandled() {
		final Object handled = handler.getAttributeValuesByName().get(
				ILegacyAttributeNames.HANDLED);
		if (handled instanceof Boolean) {
			return ((Boolean) handled).booleanValue();
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.IHandler#removeHandlerListener(org.eclipse.core.commands.IHandlerListener)
	 */
	public void removeHandlerListener(IHandlerListener handlerListener) {
		handler.removeHandlerListener(new LegacyHandlerListenerWrapper(this,
				handlerListener));
	}

}
