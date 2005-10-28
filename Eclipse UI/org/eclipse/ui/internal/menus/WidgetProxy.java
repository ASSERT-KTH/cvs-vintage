/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.menus;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.menus.IWidget;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * <p>
 * A proxy for a widget that has been defined in XML. This delays the class
 * loading until the widget is really asked to fill a menu collection. Asking
 * the widget for anything will instantiate the class.
 * </p>
 * 
 * @since 3.2
 */
final class WidgetProxy implements IWidget {

	/**
	 * The configuration element from which the widget can be created. This
	 * value will exist until the element is converted into a real class -- at
	 * which point this value will be set to <code>null</code>.
	 */
	private IConfigurationElement configurationElement;

	/**
	 * The real widget. This value is <code>null</code> until the proxy is
	 * forced to load the real widget. At this point, the configuration element
	 * is converted, nulled out, and this widget gains a reference.
	 */
	private IWidget widget = null;

	/**
	 * The name of the configuration element attribute which contains the
	 * information necessary to instantiate the real widget.
	 */
	private final String widgetAttributeName;

	/**
	 * Constructs a new instance of <code>WidgetProxy</code> with all the
	 * information it needs to try to load the class at a later point in time.
	 * 
	 * @param configurationElement
	 *            The configuration element from which the real class can be
	 *            loaded at run-time; must not be <code>null</code>.
	 * @param widgetAttributeName
	 *            The name of the attibute or element containing the widget
	 *            executable extension; must not be <code>null</code>.
	 */
	public WidgetProxy(final IConfigurationElement configurationElement,
			final String widgetAttributeName) {
		if (configurationElement == null) {
			throw new NullPointerException(
					"The configuration element backing a widget proxy cannot be null"); //$NON-NLS-1$
		}

		if (widgetAttributeName == null) {
			throw new NullPointerException(
					"The attribute containing the widget class must be known"); //$NON-NLS-1$
		}

		this.configurationElement = configurationElement;
		this.widgetAttributeName = widgetAttributeName;
	}

	public final void dispose() {
		if (loadWidget()) {
			widget.dispose();
		}
	}

	public final void fill(final Composite parent) {
		if (loadWidget()) {
			widget.fill(parent);
		}
	}

	public final void fill(final CoolBar parent, final int index) {
		if (loadWidget()) {
			widget.fill(parent, index);
		}
	}

	public final void fill(final Menu parent, final int index) {
		if (loadWidget()) {
			widget.fill(parent, index);
		}
	}

	public final void fill(final ToolBar parent, final int index) {
		if (loadWidget()) {
			widget.fill(parent, index);
		}
	}

	/**
	 * Loads the widget, if possible. If the widget is loaded, then the member
	 * variables are updated accordingly.
	 * 
	 * @return <code>true</code> if the widget is now non-null;
	 *         <code>false</code> otherwise.
	 */
	private final boolean loadWidget() {
		if (widget == null) {
			// Load the handler.
			try {
				widget = (IWidget) configurationElement
						.createExecutableExtension(widgetAttributeName);
				configurationElement = null;
				return true;
				
			} catch (final ClassCastException e) {
				final String message = "The proxied widget was the wrong class"; //$NON-NLS-1$
				final IStatus status = new Status(IStatus.ERROR,
						WorkbenchPlugin.PI_WORKBENCH, 0, message, e);
				WorkbenchPlugin.log(message, status);
				return false;

			} catch (final CoreException e) {
				final String message = "The proxied widget for '" + configurationElement.getAttribute(widgetAttributeName) //$NON-NLS-1$
						+ "' could not be loaded"; //$NON-NLS-1$
				IStatus status = new Status(IStatus.ERROR,
						WorkbenchPlugin.PI_WORKBENCH, 0, message, e);
				WorkbenchPlugin.log(message, status);
				return false;
			}
		}

		return true;
	}

	public final String toString() {
		if (widget == null) {
			return configurationElement.getAttribute(widgetAttributeName);
		}

		return widget.toString();
	}
}
