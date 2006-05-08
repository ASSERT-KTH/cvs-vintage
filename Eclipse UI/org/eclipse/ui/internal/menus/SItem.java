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

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.Util;

/**
 * <p>
 * An item in a menu, tool bar or status line. An item is characterized as a
 * button of some kind, that executes a command when clicked. An item can
 * optionally
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
public final class SItem extends MenuElement {

	/**
	 * The property for a property change event indicating that whether the
	 * command for this item has changed.
	 */
	public static final String PROPERTY_COMMAND = "COMMAND"; //$NON-NLS-1$

	/**
	 * The property for a property change event indicating that whether the menu
	 * id for this item has changed.
	 */
	public static final String PROPERTY_MENU_ID = "MENU_ID"; //$NON-NLS-1$

	/**
	 * The command that should be executed when this item is clicked. This value
	 * must not be <code>null</code>.
	 */
	private ParameterizedCommand command;

	/**
	 * The identifier of the menu attached to this item. This menu will be shown
	 * when the user performs some action. This member variable may be
	 * <code>null</code> if there is no menu associated.
	 */
	private String menuId;

	/**
	 * Constructs a new instance of <code>SItem</code>.
	 * 
	 * @param id
	 *            The identifier of the item to create; must not be
	 *            <code>null</code>
	 */
	SItem(final String id) {
		super(id);
	}

	/**
	 * <p>
	 * Defines this item by providing a command with no parameters. The location
	 * is optional. The defined property automatically becomes <code>true</code>.
	 * </p>
	 * 
	 * @param command
	 *            The fully-parameterized command to execute when this item is
	 *            triggered; must not be <code>null</code>.
	 * @param location
	 *            The location in which this item will appear; may be
	 *            <code>null</code>.
	 */
	public final void define(final Command command, final SLocation location) {
		final ParameterizedCommand parameterizedCommand = new ParameterizedCommand(
				command, null);
		define(parameterizedCommand, null, location);
	}

	/**
	 * <p>
	 * Defines this item by providing the fully-parameterized command. The
	 * location is optional. The defined property automatically becomes
	 * <code>true</code>.
	 * </p>
	 * 
	 * @param command
	 *            The fully-parameterized command to execute when this item is
	 *            triggered; must not be <code>null</code>.
	 * @param location
	 *            The location in which this item will appear; may be
	 *            <code>null</code>.
	 */
	public final void define(final ParameterizedCommand command,
			final SLocation location) {
		define(command, null, location);
	}

	/**
	 * <p>
	 * Defines this item by providing the fully-parameterized command. The
	 * location and menu identifier are optional. The defined property
	 * automatically becomes <code>true</code>.
	 * </p>
	 * 
	 * @param command
	 *            The fully-parameterized command to execute when this item is
	 *            triggered; must not be <code>null</code>.
	 * @param menuId
	 *            The identifier of the menu to display along with this item;
	 *            may be <code>null</code> if there is no such menu.
	 * @param location
	 *            The location in which this item will appear; may be
	 *            <code>null</code>.
	 */
	public final void define(final ParameterizedCommand command,
			final String menuId, final SLocation location) {
		final SLocation[] locations;
		if (location == null) {
			locations = null;
		} else {
			locations = new SLocation[] { location };
		}
		define(command, menuId, locations);
	}

	/**
	 * <p>
	 * Defines this item by providing the fully-parameterized command. The
	 * locations and menu identifier are optional. The defined property
	 * automatically becomes <code>true</code>.
	 * </p>
	 * 
	 * @param command
	 *            The fully-parameterized command to execute when this item is
	 *            triggered; must not be <code>null</code>.
	 * @param menuId
	 *            The identifier of the menu to display along with this item;
	 *            may be <code>null</code> if there is no such menu.
	 * @param locations
	 *            The locations in which this item will appear; may be
	 *            <code>null</code> or empty.
	 */
	public final void define(final ParameterizedCommand command,
			final String menuId, final SLocation[] locations) {
		if (command == null) {
			throw new NullPointerException("An item needs a command"); //$NON-NLS-1$
		}

		setCommand(command);
		setMenuId(menuId);
		setLocations(locations);
		setDefined(true);
	}

	/**
	 * Returns the fully-parameterized command that is triggered by this item.
	 * 
	 * @return The command for this item; never <code>null</code>.
	 * @throws NotDefinedException
	 *             If the handle is not currently defined.
	 */
	public final ParameterizedCommand getCommand() throws NotDefinedException {
		if (!isDefined()) {
			throw new NotDefinedException(
					"Cannot get the command from an undefined item"); //$NON-NLS-1$
		}

		return command;
	}

	/**
	 * Returns the identifier of the menu that is associated with this item.
	 * 
	 * @return The menu for this item; never <code>null</code>.
	 * @throws NotDefinedException
	 *             If the handle is not currently defined.
	 */
	public final String getMenuId() throws NotDefinedException {
		if (!isDefined()) {
			throw new NotDefinedException(
					"Cannot get the menu from an undefined item"); //$NON-NLS-1$
		}

		return menuId;
	}

	/**
	 * Sets the command for this item. This will fire a property change event if
	 * anyone cares.
	 * 
	 * @param command
	 *            The new command for this item; may be <code>null</code>.
	 */
	protected final void setCommand(final ParameterizedCommand command) {
		if (!Util.equals(this.command, command)) {
			PropertyChangeEvent event = null;
			if (isListenerAttached()) {
				event = new PropertyChangeEvent(this, PROPERTY_COMMAND,
						this.command, command);
			}
			this.command = command;
			firePropertyChangeEvent(event);
		}
	}

	/**
	 * Sets the menu id for this item. This will fire a property change event if
	 * anyone cares.
	 * 
	 * @param menuId
	 *            The new menu id for this item; may be <code>null</code>.
	 */
	protected final void setMenuId(final String menuId) {
		if (!Util.equals(this.menuId, menuId)) {
			PropertyChangeEvent event = null;
			if (isListenerAttached()) {
				event = new PropertyChangeEvent(this, PROPERTY_MENU_ID,
						this.menuId, menuId);
			}
			this.menuId = menuId;
			firePropertyChangeEvent(event);
		}
	}

	/**
	 * The string representation of this item -- for debugging purposes only.
	 * This string should not be shown to an end user.
	 * 
	 * @return The string representation; never <code>null</code>.
	 */
	public final String toString() {
		if (string == null) {
			final StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append("SItem("); //$NON-NLS-1$
			stringBuffer.append(id);
			stringBuffer.append(',');
			stringBuffer.append(command);
			stringBuffer.append(',');
			stringBuffer.append(menuId);
			stringBuffer.append(',');
			stringBuffer.append(locations);
			stringBuffer.append(',');
			stringBuffer.append(defined);
			stringBuffer.append(')');
			string = stringBuffer.toString();
		}
		return string;
	}

	/**
	 * Makes this item become undefined. This has the side effect of changing
	 * the command, menu id and locations to <code>null</code>. Notification
	 * is sent to all listeners.
	 */
	public final void undefine() {
		string = null;

		setCommand(null);
		setMenuId(null);
		setLocations(null);
		setDefined(false);
	}

}
