/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.menus;

/**
 * <p>
 * A reference to an already existing menu element. A reference is used to
 * modify or extend the meaning of an existing menu element.
 * </p>
 * <p>
 * Clients may instantiate this class, but must not extend.
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
public final class SReference {

	/**
	 * The constant for a reference that refers to an unspecified type of menu
	 * element.
	 */
	public static final int TYPE_UNSPECIFIED = 0;

	/**
	 * The constant for a reference that refers to a menu.
	 */
	public static final int TYPE_MENU = 1;

	/**
	 * The constant for a reference that refers to a group.
	 */
	public static final int TYPE_GROUP = 2;

	/**
	 * The constant for a reference that refers to an item.
	 */
	public static final int TYPE_ITEM = 3;

	/**
	 * The constant for a reference that refers to a widget.
	 */
	public static final int TYPE_WIDGET = 4;

	/**
	 * The type of reference. This must be one of the type constants given in
	 * this class.
	 */
	private final int type;

	/**
	 * The identifier of the menu element this refers to. Must not be
	 * <code>null</code>.
	 */
	private final String id;

	/**
	 * Constructs a new instance of <code>SReference</code>.
	 * 
	 * @param type
	 *            The type of menu element this is making reference to. This
	 *            must be one of <code>TYPE_UNSPECIFIED</code>,
	 *            <code>TYPE_MENU</code>, <code>TYPE_GROUP</code>,
	 *            <code>TYPE_ITEM</code> or <code>TYPE_WIDGET</code>.
	 * @param id
	 *            The identifier of the menu element this refers to; must not be
	 *            <code>null</code>.
	 */
	public SReference(final int type, final String id) {
		if ((type < TYPE_UNSPECIFIED) || (type > TYPE_WIDGET)) {
			throw new IllegalArgumentException(
					"The type of reference is not understood"); //$NON-NLS-1$
		}
		if (id == null) {
			throw new NullPointerException(
					"The identifier of the menu element must be null"); //$NON-NLS-1$
		}

		this.type = type;
		this.id = id;
	}

	/**
	 * Returns the identifier of the menu element to which this refers.
	 * 
	 * @return The identifier of the menu element; never <code>null</code>.
	 */
	public final String getId() {
		return id;
	}

	/**
	 * Returns the type of reference. This will be one of the type constants in
	 * this class.
	 * 
	 * @return The type of reference.
	 */
	public final int getType() {
		return type;
	}
}
