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
package org.eclipse.ui.commands;

import java.util.Collection;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IExecutionListener;

/**
 * <p>
 * Provides services related to the command architecture within the workbench.
 * This service can be used to access the set of commands and command
 * categories.
 * </p>
 * <p>
 * This interface should not be implemented or extended by clients.
 * </p>
 * 
 * @since 3.1
 */
public interface ICommandService {

	/**
	 * Adds an execution listener to the command service. This listener will be
	 * notified as commands are executed.
	 * 
	 * @param listener
	 *            The listener to add; must not be <code>null</code>.
	 */
	public void addExecutionListener(IExecutionListener listener);

	/**
	 * Retrieves the category with the given identifier. If no such category
	 * exists, then an undefined category with the given id is created.
	 * 
	 * @param categoryId
	 *            The identifier to find; must not be <code>null</code>.
	 * @return A category with the given identifier, either defined or
	 *         undefined.
	 */
	public Category getCategory(String categoryId);

	/**
	 * Retrieves the command with the given identifier. If no such command
	 * exists, then an undefined command with the given id is created.
	 * 
	 * @param commandId
	 *            The identifier to find; must not be <code>null</code>.
	 * @return A command with the given identifier, either defined or undefined.
	 */
	public Command getCommand(String commandId);

	/**
	 * Returns the collection of the identifiers for all of the defined
	 * categories in the workbench.
	 * 
	 * @return The collection of category identifiers (<code>String</code>)
	 *         that are defined; never <code>null</code>, but may be empty.
	 */
	public Collection getDefinedCategoryIds();

	/**
	 * Returns the collection of the identifiers for all of the defined commands
	 * in the workbench.
	 * 
	 * @return The collection of command identifiers (<code>String</code>)
	 *         that are defined; never <code>null</code>, but may be empty.
	 */
	public Collection getDefinedCommandIds();

	/**
	 * <p>
	 * Reads the command information from the registry and the preferences. This
	 * will overwrite any of the existing information in the command service.
	 * This method is intended to be called during start-up. When this method
	 * completes, this command service will reflect the current state of the
	 * registry and preference store.
	 * </p>
	 */
	public void readRegistry();

	/**
	 * Removes an execution listener from the command service.
	 * 
	 * @param listener
	 *            The listener to remove; must not be <code>null</code>.
	 */
	public void removeExecutionListener(IExecutionListener listener);
}
