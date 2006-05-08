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

import org.eclipse.jface.util.Util;

/**
 * <p>
 * A location for a menu element. A location carries with it information about
 * the group in which is should appear. The location can also specify a style of
 * image to associate with the menu element and a mnemonic.
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
public final class SLocation {

	/**
	 * The constant to use if there is no mnemonic for this location.
	 */
	public static final char MNEMONIC_NONE = 0;

	/**
	 * The style of image to use in this location. This value may be
	 * <code>null</code> if the default image style should be used.
	 */
	private final String imageStyle;

	/**
	 * The mnemonic to use in this location. This value may be <code>null</code>.
	 */
	private final char mnemonic;

	/**
	 * The ordering constraint for this menu element with respect to other menu
	 * elements. This value may be <code>null</code>.
	 */
	private final SOrder ordering;

	/**
	 * The location element specifying the path for this location. This
	 * indicates which menu or tool bar this location indicates.
	 */
	private final LocationElement path;

	/**
	 * Constructs a new instance of <code>SLocation</code> -- indicating that
	 * the item should appear at the top-level of the menu bar, with no mnemonic
	 * or ordering constraints.
	 */
	public SLocation() {
		this(new SBar());
	}

	/**
	 * Constructs a new instance of <code>SLocation</code>.
	 * 
	 * @param path
	 *            The location element specifying the path for this location;
	 *            must not be null.
	 */
	public SLocation(final LocationElement path) {
		this(path, MNEMONIC_NONE);
	}

	/**
	 * Constructs a new instance of <code>SLocation</code> that is the child
	 * of the given location with the given id appended to the path. The new
	 * location will have no ordering, mnemonic or image style. It will only
	 * have a path.
	 * 
	 * @param parent
	 *            The parent location to which the id should be appended; must
	 *            not be <code>null</code>.
	 * @param id
	 *            The identifier to append to the location; must not be
	 *            <code>null</code>.
	 */
	public SLocation(final SLocation parent, final String id) {
		this(parent, id, MNEMONIC_NONE);
	}
	
	/**
	 * Constructs a new instance of <code>SLocation</code> that is the child
	 * of the given location with the given id appended to the path. The new
	 * location will have no ordering, mnemonic or image style. It will only
	 * have a path.
	 * 
	 * @param parent
	 *            The parent location to which the id should be appended; must
	 *            not be <code>null</code>.
	 * @param id
	 *            The identifier to append to the location; must not be
	 *            <code>null</code>.
	 * @param mnemonic
	 *            The mnemonic to set
	 */
	public SLocation(final SLocation parent, final String id, final char mnemonic) {

		if (parent == null) {
			throw new NullPointerException("The parent cannot be null"); //$NON-NLS-1$
		}

		if (id == null) {
			throw new NullPointerException("The id cannot be null"); //$NON-NLS-1$
		}

		final LocationElement parentPath = parent.getPath();
		final LocationElement childPath = parentPath.createChild(id);

		this.imageStyle = null;
		this.mnemonic = mnemonic;
		this.ordering = null;
		this.path = childPath;
	}

	/**
	 * Constructs a new instance of <code>SLocation</code>.
	 * 
	 * @param path
	 *            The location element specifying the path for this location;
	 *            must not be null.
	 * @param mnemonic
	 *            The mnemonic to use in this particular location. The mnemonic
	 *            should be translated. If there is no mnemonic, then send
	 *            <code>MNEMONIC_NONE</code>.
	 */
	public SLocation(final LocationElement path, final char mnemonic) {
		this(path, null, mnemonic);
	}

	/**
	 * Constructs a new instance of <code>SLocation</code>.
	 * 
	 * @param path
	 *            The location element specifying the path for this location;
	 *            must not be null.
	 * @param ordering
	 *            The ordering constraints for this menu element with respect to
	 *            other menu elements. This value may be <code>null</code>.
	 */
	public SLocation(final LocationElement path, final SOrder ordering) {
		this(path, ordering, MNEMONIC_NONE);
	}

	/**
	 * Constructs a new instance of <code>SLocation</code>.
	 * 
	 * @param path
	 *            The location element specifying the path for this location;
	 *            must not be null.
	 * @param ordering
	 *            The ordering constraint for this menu element with respect to
	 *            other menu elements. This value may be <code>null</code>.
	 * @param mnemonic
	 *            The mnemonic to use in this particular location. The mnemonic
	 *            should be translated. If there is no mnemonic, then send
	 *            <code>MNEMONIC_NONE</code>.
	 */
	public SLocation(final LocationElement path, final SOrder ordering,
			final char mnemonic) {
		this(path, ordering, mnemonic, null);

	}

	/**
	 * Constructs a new instance of <code>SLocation</code>.
	 * 
	 * @param path
	 *            The location element specifying the path for this location;
	 *            must not be null.
	 * @param ordering
	 *            The ordering constraint for this menu element with respect to
	 *            other menu elements. This value may be <code>null</code>.
	 * @param mnemonic
	 *            The mnemonic to use in this particular location. The mnemonic
	 *            should be translated. If there is no mnemonic, then send
	 *            <code>MNEMONIC_NONE</code>.
	 * @param imageStyle
	 *            The style of image to use in this location. If this value is
	 *            <code>null</code>, then the default image style is used.
	 */
	public SLocation(final LocationElement path, final SOrder ordering,
			final char mnemonic, String imageStyle) {
		if ((imageStyle != null) && (imageStyle.length() == 0)) {
			imageStyle = null;
		}

		if (path == null) {
			throw new NullPointerException(
					"The path for a location must not be null"); //$NON-NLS-1$
		}

		this.mnemonic = mnemonic;
		this.imageStyle = imageStyle;
		this.ordering = ordering;
		this.path = path;
	}

	/**
	 * Returns the image style for this location.
	 * 
	 * @return The image style. If the default image style, then
	 *         <code>null</code>.
	 */
	public final String getImageStyle() {
		return imageStyle;
	}

	/**
	 * Returns the mnemonic for this location. The mnemonic should be
	 * translated.
	 * 
	 * @return The mnemonic. If no mnemonic, then <code>MNEMONIC_NONE</code>.
	 */
	public final char getMnemonic() {
		return mnemonic;
	}

	/**
	 * Returns the ordering for this location.
	 * 
	 * @return The ordering. If no ordering, then <code>null</code>.
	 */
	public final SOrder getOrdering() {
		return ordering;
	}

	/**
	 * Returns the path for this location element.
	 * 
	 * @return The path; never <code>null</code>.
	 */
	public final LocationElement getPath() {
		return path;
	}

	public final String toString() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append("SLocation("); //$NON-NLS-1$
		buffer.append(path);
		buffer.append(',');
		if (mnemonic != MNEMONIC_NONE) {
			buffer.append(mnemonic);
			buffer.append(',');
		}
		buffer.append(imageStyle);
		buffer.append(',');
		buffer.append(ordering);
		buffer.append(')');
		return buffer.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof SLocation) {
			SLocation loc = (SLocation) obj;
			return path.equals(loc.path) && mnemonic == loc.mnemonic
					&& Util.equals(ordering, loc.ordering)
					&& Util.equals(imageStyle, loc.imageStyle);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return path.hashCode() + Util.hashCode(imageStyle)
				+ Util.hashCode(ordering);
	}
}
