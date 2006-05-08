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
 * A location element referring to a specific location within one or more popup
 * menus in the application. If the <code>id</code> is <code>null</code>,
 * then this location element refers to all popup menus. If it is not
 * <code>null</code>, then it refers to all popup menus with that id. Popups
 * menus are also known as context menus. They are typically opened by clicking
 * the right mouse button.
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
public final class SPopup extends LeafLocationElement {

	/**
	 * The identifier of the popup in which the menu element should appear. If
	 * this value is <code>null</code>, then it applies to all popup menus.
	 */
	private final String id;

	/**
	 * Constructs a new instance of <code>SPopup</code>.
	 * 
	 * @param id
	 *            The identifier of the popup in which the menu should appear;
	 *            may be <code>null</code> if this applies to all context
	 *            menus.
	 * @param path
	 *            The path to the final location. If this value is
	 *            <code>null</code>, it means that it should be inserted at
	 *            the top-level of the context menu.
	 */
	public SPopup(final String id, final String path) {
		super(path);

		this.id = id;
	}

	public final LocationElement createChild(final String id) {
		final String parentPath = getPath();
		final String path;
		if (parentPath == null) {
			path = id;
		} else {
			path = parentPath + PATH_SEPARATOR + id;
		}
		return new SPopup(getId(), path);
	}

	/**
	 * Returns the identifier of the popup. If the identifier is
	 * <code>null</code>, then this location element refers to all context
	 * menus.
	 * 
	 * @return The identifier of the popup; may be <code>null</code>.
	 */
	public final String getId() {
		return id;
	}

	public final ILocationElementTokenizer getTokenizer() {
		return new ILocationElementTokenizer() {
			String remainingPath = getPath();

			String parsedPath = null;

			public final LocationElementToken nextToken() {
				final SLocation location = new SLocation(new SPopup(getId(),
						parsedPath));
				final int separator = remainingPath
						.indexOf(LeafLocationElement.PATH_SEPARATOR);
				final String id;
				if (separator == -1) {
					id = remainingPath;
					remainingPath = null;
				} else {
					id = remainingPath.substring(0, separator);
					remainingPath = remainingPath.substring(separator + 1);
				}
				parsedPath = parsedPath + id;
				return new LocationElementToken(location, id);
			}

			public final boolean hasMoreTokens() {
				return remainingPath != null;
			}
		};
	}

	public final String toString() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append("SPopup("); //$NON-NLS-1$
		buffer.append(id);
		buffer.append(',');
		buffer.append(getPath());
		buffer.append(')');
		return buffer.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.menus.LeafLocationElement#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this==obj) {
			return true;
		}
		if (obj instanceof SPopup) {
			SPopup popup = (SPopup) obj;
			return Util.equals(id, popup.id) && super.equals(obj);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.menus.LeafLocationElement#hashCode()
	 */
	public int hashCode() {
		return Util.hashCode(id) + super.hashCode();
	}
}
