/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.contexts;

/**
 * An instance of this interface represents a binding between an context and
 * an context.
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 * @see IContext
 */
public interface IContextContextBinding extends Comparable {

	/**
	 * Returns the identifier of the child context represented in this
	 * binding.
	 * 
	 * @return the identifier of the child context represented in this
	 *         binding. Guaranteed not to be <code>null</code>.
	 */
	String getChildContextId();

	/**
	 * Returns the identifier of the parent context represented in this
	 * binding.
	 * 
	 * @return the identifier of the parent context represented in this
	 *         binding. Guaranteed not to be <code>null</code>.
	 */
	String getParentContextId();
}
