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
package org.eclipse.ui;

/**
 * <p>
 * A service that responds to changes in one or more sources. These sources can
 * be plugged into the service.
 * </p>
 * <p>
 * This interface should not be implemented or extended by clients.
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public interface IServiceWithSources {

	/**
	 * Adds a source provider to this service. A source provider will notify the
	 * service when the source it provides changes. An example of a source might
	 * be an active editor or the current selection. This amounts to a pluggable
	 * state tracker for the service.
	 * 
	 * @param provider
	 *            The provider to add; must not be <code>null</code>.
	 */
	public void addSourceProvider(ISourceProvider provider);

	/**
	 * Removes a source provider from this service. Most of the time, this
	 * method call is not required as source providers typically share the same
	 * life span as the workbench itself.
	 * 
	 * @param provider
	 *            The provider to remove; must not be <code>null</code>.
	 */
	public void removeSourceProvider(ISourceProvider provider);
}
