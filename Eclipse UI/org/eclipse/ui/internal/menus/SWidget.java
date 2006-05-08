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
import org.eclipse.jface.menus.IWidget;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * <p>
 * A menu element that represents some arbitrary control. This arbitrary control
 * has an id, zero or more locations, and a class providing the callback methods
 * necessary to create the arbitrary control.
 * </p>
 * <p>
 * <strong>PROVISIONAL</strong>. This class or interface has been added as part
 * of a work in progress. There is a guarantee neither that this API will work
 * nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * <p>
 * This class will eventually exist in <code>org.eclipse.jface.menus</code>.
 * </p>
 * 
 * @since 3.2
 */
public final class SWidget extends MenuElement {

	/**
	 * The property for a property change event indicating that whether the
	 * widget class has changed.
	 */
	public static final String PROPERTY_WIDGET = "WIDGET"; //$NON-NLS-1$

	/**
	 * The layout information to associate with this widget; never
	 * <code>null</code>.
	 */
	private SLayout layout;

	/**
	 * The class that will contribute widgets to the menu; never
	 * <code>null</code>.
	 */
	private IWidget thirdPartyCode;

	/**
	 * Constructs a new instance of <code>SWidget</code>.
	 * 
	 * @param id
	 *            The identifier of the widget to create; must not be
	 *            <code>null</code>
	 */
	SWidget(final String id) {
		super(id);
	}

	/**
	 * <p>
	 * Defines this widget by providing the class defining the widget. The
	 * location is optional. The defined property automatically becomes
	 * <code>true</code>.
	 * </p>
	 * 
	 * @param widget
	 *            The class that is called to contribute widgets to the given
	 *            locations; must not be <code>null</code>.
	 * @param location
	 *            The location in which this item will appear; may be
	 *            <code>null</code>.
	 */
	public final void define(final IWidget widget, final SLocation location) {
		final SLocation[] locations;
		if (location == null) {
			locations = null;
		} else {
			locations = new SLocation[] { location };
		}
		define(widget, locations);
	}

	/**
	 * <p>
	 * Defines this widget by providing the class defining the widget. The
	 * locations are optional. The defined property automatically becomes
	 * <code>true</code>.
	 * </p>
	 * 
	 * @param widget
	 *            The class that is called to contribute widgets to the given
	 *            locations; must not be <code>null</code>.
	 * @param locations
	 *            The locations in which this item will appear; may be
	 *            <code>null</code> or empty.
	 */
	public final void define(final IWidget widget, final SLocation[] locations) {
		define(widget, locations, new SLayout());
	}

	/**
	 * <p>
	 * Defines this widget by providing the class defining the widget. The
	 * locations are optional. The defined property automatically becomes
	 * <code>true</code>.
	 * </p>
	 * 
	 * @param widget
	 *            The class that is called to contribute widgets to the given
	 *            locations; must not be <code>null</code>.
	 * @param locations
	 *            The locations in which this item will appear; may be
	 *            <code>null</code> or empty.
	 * @param layout
	 *            The layout information to use for this widget
	 */
	public final void define(final IWidget widget, final SLocation[] locations,
			SLayout layout) {
		if (widget == null) {
			throw new NullPointerException(
					"A widget needs a class to contribute the widgets"); //$NON-NLS-1$
		}

		setDefined(true);
		setLocations(locations);
		setLayout(layout);
		setWidget(widget);
	}

	/**
	 * Returns the layout information associated with this widget
	 * 
	 * @return the layout information; never <code>null</code>.
	 * @throws NotDefinedException
	 */
	public final SLayout getLayout() throws NotDefinedException {
		if (!isDefined()) {
			throw new NotDefinedException(
					"Cannot get the layout from an undefined widget"); //$NON-NLS-1$
		}

		return layout;
	}

	/**
	 * Returns the class providing the widgets for this menu element.
	 * 
	 * @return The widget for this menu element; never <code>null</code>.
	 * @throws NotDefinedException
	 *             If the handle is not currently defined.
	 */
	public final IWidget getWidget() throws NotDefinedException {
		if (!isDefined()) {
			throw new NotDefinedException(
					"Cannot get the widget class from an undefined widget"); //$NON-NLS-1$
		}

		return thirdPartyCode;
	}

	/**
	 * Sets the layout information associated with this widget. This will fire a
	 * property change event if anyone cares.
	 * 
	 * @param layout
	 *            The layout information to associate with this widget; may be
	 *            <code>null</code>.
	 */
	private final void setLayout(final SLayout layout) {
		this.layout = layout;
	}

	/**
	 * Sets widget class backing this widget. This will fire a property change
	 * event if anyone cares.
	 * 
	 * @param widget
	 *            The widget class; may be <code>null</code>.
	 */
	protected final void setWidget(final IWidget widget) {
		PropertyChangeEvent event = null;
		if (isListenerAttached()) {
			event = new PropertyChangeEvent(this, PROPERTY_WIDGET,
					this.thirdPartyCode, widget);
		}
		this.thirdPartyCode = widget;
		firePropertyChangeEvent(event);
	}

	/**
	 * The string representation of this widget -- for debugging purposes only.
	 * This string should not be shown to an end user.
	 * 
	 * @return The string representation; never <code>null</code>.
	 */
	public final String toString() {
		if (string == null) {
			final StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append("SWidget("); //$NON-NLS-1$
			stringBuffer.append(id);
			stringBuffer.append(',');
			stringBuffer.append(locations);
			stringBuffer.append(',');
			try {
				stringBuffer.append(thirdPartyCode);
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
	 * Makes this widget become undefined. This has the side effect of changing
	 * the locations and widget to <code>null</code>. Notification is sent to
	 * all listeners.
	 */
	public final void undefine() {
		string = null;

		setWidget(null);
		setLocations(null);
		setLayout(null);
		setDefined(false);
	}
}
