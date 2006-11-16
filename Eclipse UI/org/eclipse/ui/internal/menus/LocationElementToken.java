/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.menus;

/**
 * <p>
 * This records enough information to create a child layout node. It is created
 * by {@link LocationElementTokenizer}.
 * </p>
 * <p>
 * Only intended for use within the <code>org.eclipse.jface.menus</code>
 * package.
 * </p>
 * <p>
 * <strong>PROVISIONAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * <p>
 * This class will eventually exist in <code>org.eclipse.jface.menus</code>.
 * </p>
 * 
 * @since 3.2
 */
final class LocationElementToken {

	/**
	 * The location at which the child element should be inserted; may be
	 * <code>null</code>.
	 */
	private final SLocation location;

	/**
	 * The identifier of the child element to be created; never
	 * <code>null</code>.
	 */
	private final String id;

	/**
	 * Constructs a new <code>LocationElementToken</code>.
	 * 
	 * @param location
	 *            The location leading up to this location element; may be
	 *            <code>null</code>.
	 * @param id
	 *            The identifier of this location element; must not be
	 *            <code>null</code>.
	 */
	LocationElementToken(final SLocation location, final String id) {
		if (id == null) {
			throw new NullPointerException("The id cannot be null"); //$NON-NLS-1$
		}

		this.location = location;
		this.id = id;
	}

	/**
	 * Returns the identifier for this token.
	 * 
	 * @return The identifier for this token; never <code>null</code>.
	 */
	public final String getId() {
		return id;
	}

	/**
	 * Returns the location for this token.
	 * 
	 * @return The location for this token; may be <code>null</code>.
	 */
	public final SLocation getLocation() {
		return location;
	}
}

