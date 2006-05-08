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

import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.Util;

/**
 * <p>
 * A menu in a menu bar, a context menu or a pulldown menu attached to a tool
 * item.
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
public final class SMenu extends MenuContainer {

	/**
	 * The property for a property change event indicating that whether the
	 * label for this menu has changed.
	 */
	public static final String PROPERTY_LABEL = "LABEL"; //$NON-NLS-1$

	/**
	 * The label for this menu. This label is displayed to the user. It may be
	 * <code>null</code>, if it appears only as an icon.
	 */
	private String label;

	/**
	 * Constructs a new instance of <code>SMenu</code>.
	 * 
	 * @param id
	 *            The identifier of the menu to create; must not be
	 *            <code>null</code>
	 */
	SMenu(final String id) {
		super(id);
	}

	/**
	 * <p>
	 * Defines this menu by indicating the label. The location is optional. The
	 * defined property automatically becomes <code>true</code>.
	 * </p>
	 * 
	 * @param label
	 *            The label for this menu; may be <code>null</code>.
	 * @param location
	 *            The location in which this menu will appear; may be
	 *            <code>null</code>.
	 */
	public final void define(final String label, SLocation location) {
		define(label, location, null);
	}

	/**
	 * <p>
	 * Defines this menu by indicating the label. The locations and dynamic menu
	 * are optional. The defined property automatically becomes
	 * <code>true</code>.
	 * </p>
	 * 
	 * @param label
	 *            The label for this menu; may be <code>null</code>.
	 * @param location
	 *            The location in which this menu will appear; may be
	 *            <code>null</code>.
	 * @param dynamic
	 *            The class providing dynamic menu elements to this group; may
	 *            be <code>null</code>.
	 */
	public final void define(final String label, SLocation location,
			final IDynamicMenu dynamic) {
		final SLocation[] locations;
		if (location == null) {
			locations = null;
		} else {
			locations = new SLocation[] { location };
		}
		define(label, locations, dynamic);
	}

	/**
	 * <p>
	 * Defines this menu by indicating the label. The locations and dynamic menu
	 * are optional. The defined property automatically becomes
	 * <code>true</code>.
	 * </p>
	 * 
	 * @param label
	 *            The label for this menu; may be <code>null</code>.
	 * @param locations
	 *            The locations in which this menu will appear; may be
	 *            <code>null</code> or empty.
	 * @param dynamic
	 *            The class providing dynamic menu elements to this group; may
	 *            be <code>null</code>.
	 */
	public final void define(final String label, SLocation[] locations,
			final IDynamicMenu dynamic) {
		if ((locations != null) && (locations.length == 0)) {
			locations = null;
		}

		setDefined(true);
		setLocations(locations);
		setDynamic(dynamic);
		setLabel(label);
	}

	/**
	 * Returns the label for this menu. A menu does not need a label if its is
	 * simply a pulldown menu on a tool item.
	 * 
	 * @return The label for this menu; may be <code>null</code>.
	 * @throws NotDefinedException
	 *             If the handle is not currently defined.
	 */
	public final String getLabel() throws NotDefinedException {
		if (!isDefined()) {
			throw new NotDefinedException(
					"Cannot get the label from an undefined menu"); //$NON-NLS-1$
		}

		return label;
	}

	/**
	 * Sets the label that should be displayed for this menu. This will fire a
	 * property change event if anyone cares.
	 * 
	 * @param label
	 *            The new label for this menu; may be <code>null</code>.
	 */
	protected final void setLabel(final String label) {
		if (!Util.equals(this.label, label)) {
			PropertyChangeEvent event = null;
			if (isListenerAttached()) {
				event = new PropertyChangeEvent(this, PROPERTY_LABEL,
						this.label, label);
			}
			this.label = label;
			firePropertyChangeEvent(event);
		}
	}

	/**
	 * The string representation of this menu -- for debugging purposes only.
	 * This string should not be shown to an end user.
	 * 
	 * @return The string representation; never <code>null</code>.
	 */
	public final String toString() {
		if (string == null) {
			final StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append("SMenu("); //$NON-NLS-1$
			stringBuffer.append(id);
			stringBuffer.append(',');
			stringBuffer.append(label);
			stringBuffer.append(',');
			try {
				stringBuffer.append(dynamic);
			} catch (final Exception e) {
				// A bogus toString() in third-party code. Ignore.
				stringBuffer.append(e.getClass().getName());
			}
			stringBuffer.append(',');
			stringBuffer.append(defined);
			stringBuffer.append(')');
			string = stringBuffer.toString();
		}
		return string;
	}

	/**
	 * Makes this menu become undefined. This has the side effect of changing
	 * the label, locations and dynamic class to <code>null</code>.
	 * Notification is sent to all listeners.
	 */
	public final void undefine() {
		string = null;

		setLabel(null);
		setDynamic(null);
		setLocations(null);
		setDefined(false);
	}
}
