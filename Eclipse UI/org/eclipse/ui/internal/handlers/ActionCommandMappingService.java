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

package org.eclipse.ui.internal.handlers;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * A service which holds mappings between retarget action identifiers and
 * command identifiers (aka: action definition ids). This implementation does
 * not clean up in the case of dynamic plug-ins.
 * </p>
 * <p>
 * This class is not intended for use outside of the
 * <code>org.eclipse.ui.workbench</code> plug-in.
 * </p>
 * 
 * @since 3.2
 */
public final class ActionCommandMappingService implements
		IActionCommandMappingService {

	/**
	 * The map of action identifiers ({@link String}) to command identifiers ({@link String}).
	 * This value is never <code>null</code>.
	 */
	private final Map mapping = new HashMap();

	public final String getCommandId(final String actionId) {
		if (actionId == null) {
			throw new NullPointerException(
					"Cannot get the command identifier for a null action id"); //$NON-NLS-1$
		}

		return (String) mapping.get(actionId);
	}

	public final void map(final String actionId, final String commandId) {
		if (actionId == null) {
			throw new NullPointerException("The action id cannot be null"); //$NON-NLS-1$
		}

		if (commandId == null) {
			throw new NullPointerException("The command id cannot be null"); //$NON-NLS-1$
		}

		mapping.put(actionId, commandId);
	}
}

