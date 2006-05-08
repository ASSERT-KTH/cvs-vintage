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
 * A location element that is used to select a specific part within the
 * application. In the Eclipse workbench, this correspond with the concept of a
 * part. Within JFace, this can refer to any similar ideas or can just be
 * ignored.
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
 * 
 */
public final class SPart implements LocationElement {

	/**
	 * The constant used to indicate that the <code>part</code> is a class.
	 */
	public static final int TYPE_CLASS = 0;

	/**
	 * The constant used to indicate that the <code>part</code> is an
	 * identifier.
	 */
	public static final int TYPE_ID = 1;

	/**
	 * The location within the part to place the menu element.
	 */
	private final LeafLocationElement location;

	/**
	 * The identifier of the part to which this instance refers, or the class
	 * (i.e., class, super class, interface or super interface) of the part.
	 * This value must not be <code>null</code>.
	 */
	private final String part;

	/**
	 * The type of information carried in <code>part</code>.
	 */
	private final int type;

	/**
	 * Constructs a new instance of <code>SPart</code>.
	 * 
	 * @param part
	 *            The part to which this refers. Either its identifier or its
	 *            class. Must not be <code>null</code>.
	 * @param type
	 *            The type of data carried in <code>part</code>.
	 * @param location
	 *            The location within this part that the menu element should be
	 *            placed. Must not be <code>null</code>.
	 */
	public SPart(final String part, final int type,
			final LeafLocationElement location) {
		if (part == null) {
			throw new NullPointerException("A part needs a class or id"); //$NON-NLS-1$
		}
		if ((type < TYPE_CLASS) || (type > TYPE_ID)) {
			throw new IllegalArgumentException(
					"The part must be either a class or an identifier"); //$NON-NLS-1$
		}
		if (location == null) {
			throw new NullPointerException(
					"A part needs a location for the menu element to appear"); //$NON-NLS-1$
		}

		this.part = part;
		this.type = type;
		this.location = location;
	}

	public final LocationElement createChild(final String id) {
		final LeafLocationElement childPath = (LeafLocationElement) getLocation()
				.createChild(id);
		return new SPart(getPart(), getType(), childPath);
	}

	/**
	 * Returns the location within this part that the menu element should be
	 * placed. Must not be <code>null</code>.
	 * 
	 * @return The location in which the menu element should be placed; never
	 *         <code>null</code>.
	 */
	public final LeafLocationElement getLocation() {
		return location;
	}

	/**
	 * Returns either the class name to which this part refers or the identifier
	 * of the part. The type is indicated by a call to {@link SPart#getType()}
	 * 
	 * @return The class name or identifier; never <code>null</code>.
	 */
	public final String getPart() {
		return part;
	}

	/**
	 * Returns the type of this part. This indicates whether this part refers to
	 * an identifier or to a class.
	 * 
	 * @return Either <code>TYPE_CLASS</code> or <code>TYPE_ID</code>.
	 */
	public final int getType() {
		return type;
	}

	public final String toString() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append("SPart("); //$NON-NLS-1$
		switch (type) {
		case TYPE_CLASS:
			buffer.append("class"); //$NON-NLS-1$
			break;
		case TYPE_ID:
			buffer.append("id"); //$NON-NLS-1$
			break;
		default:
			buffer.append("unknown"); //$NON-NLS-1$
		}
		buffer.append(',');
		buffer.append(part);
		buffer.append(',');
		buffer.append(location);
		buffer.append(')');
		return buffer.toString();
	}
}
