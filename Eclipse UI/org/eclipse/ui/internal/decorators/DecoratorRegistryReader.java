/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.decorators;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ActionExpression;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.registry.RegistryReader;

/**
 * The DecoratorRegistryReader is the class that reads the
 * decorator descriptions from the registry
 */

public class DecoratorRegistryReader extends RegistryReader {

	//The registry values are the ones read from the registry
	private Collection values = new ArrayList();;
	private Collection ids = new HashSet();

	/* package */ static final String ATT_CLASS = "class";//$NON-NLS-1$
	private static final String ATT_LABEL = "label"; //$NON-NLS-1$
	private static final String ATT_ADAPTABLE = "adaptable"; //$NON-NLS-1$
	private static final String ATT_ID = "id"; //$NON-NLS-1$
	private static final String ATT_DESCRIPTION = "description"; //$NON-NLS-1$
	private static final String ATT_ICON = "icon"; //$NON-NLS-1$
	private static final String ATT_QUADRANT = "quadrant"; //$NON-NLS-1$
	private static final String ATT_LOCATION = "location"; //$NON-NLS-1$
	private static final String ATT_ENABLED = "state"; //$NON-NLS-1$
	private static final String CHILD_ENABLEMENT = "enablement"; //$NON-NLS-1$
	private static final String P_TRUE = "true"; //$NON-NLS-1$
	private static final String ATT_OBJECT_CLASS = "objectClass"; //$NON-NLS-1$
	private static final String ATT_LIGHTWEIGHT = "lightweight"; //$NON-NLS-1$

	//Constants for quadrants
	public static final int TOP_LEFT = 0;
	public static final int TOP_RIGHT = 1;
	public static final int BOTTOM_LEFT = 2;
	public static final int BOTTOM_RIGHT = 3;
	public static final int UNDERLAY = 4;

	//Constants for quadrants
	private static final String TOP_LEFT_STRING = "TOP_LEFT"; //$NON-NLS-1$
	private static final String TOP_RIGHT_STRING = "TOP_RIGHT"; //$NON-NLS-1$
	private static final String BOTTOM_LEFT_STRING = "BOTTOM_LEFT"; //$NON-NLS-1$
	private static final String UNDERLAY_STRING = "UNDERLAY"; //$NON-NLS-1$

	/**
	 * Constructor for DecoratorRegistryReader.
	 */
	public DecoratorRegistryReader() {
		super();
	}

	/*
	 * @see RegistryReader#readElement(IConfigurationElement)
	 */
	public boolean readElement(IConfigurationElement element) {

		String name = element.getAttribute(ATT_LABEL);

		String id = element.getAttribute(ATT_ID);
		if(ids.contains(id)){
			logDuplicateId(element);
			return false;
		}
		ids.add(id);

		String description = ""; //$NON-NLS-1$

		IConfigurationElement[] descriptions =
			element.getChildren(ATT_DESCRIPTION);

		if (descriptions.length > 0)
			description = descriptions[0].getValue();

		boolean adaptable = P_TRUE.equals(element.getAttribute(ATT_ADAPTABLE));

		boolean enabled = P_TRUE.equals(element.getAttribute(ATT_ENABLED));

		ActionExpression enablementExpression;

		IConfigurationElement[] enablement =
			element.getChildren(CHILD_ENABLEMENT);
		if (enablement.length == 0) {
			String className = element.getAttribute(ATT_OBJECT_CLASS);
			if (className == null) {
				logMissingElement(element, CHILD_ENABLEMENT);
				return false;
			} else
				enablementExpression =
					new ActionExpression(ATT_OBJECT_CLASS, className);
		} else
			enablementExpression = new ActionExpression(enablement[0]);

		boolean noClass =
			element.getAttribute(ATT_CLASS) == null;

		//Lightweight or Full? It is lightweight if it is declared lightweight or if there is no class
		if (P_TRUE.equals(element.getAttribute(ATT_LIGHTWEIGHT)) || noClass) {

			int quadrant =
				getLocationConstant(element.getAttribute(ATT_LOCATION),element);
			String iconPath = element.getAttribute(ATT_ICON);
			
			if (noClass && iconPath == null) {
				logMissingElement(element, ATT_ICON);
				return false;
			}

			values.add(
				new LightweightDecoratorDefinition(
					id,
					name,
					description,
					enablementExpression,
					adaptable,
					enabled,
					quadrant,
					iconPath,
					element));
		} else {
			values.add(
				new FullDecoratorDefinition(
					id,
					name,
					description,
					enablementExpression,
					adaptable,
					enabled,
					element));
		}

		return true;

	}

	/**
	 * Read the decorator extensions within a registry and set 
	 * up the registry values.
	 */
	Collection readRegistry(IExtensionRegistry in) {
		values.clear();
		ids.clear();
		readRegistry(in, PlatformUI.PLUGIN_ID, IWorkbenchConstants.PL_DECORATORS);
		return values;
	}
	
	public Collection getValues() {
		return values;
	}

	/**
	 * Get the constant value based on the location supplied. Default to bottom
	 * right.
	 */
	private int getLocationConstant(String locationDefinition, IConfigurationElement element) {
		
		//Backwards compatibility
		if(locationDefinition == null)
			locationDefinition = element.getAttribute(ATT_QUADRANT);
			
		if (TOP_RIGHT_STRING.equals(locationDefinition))
			return TOP_RIGHT;
		if (TOP_LEFT_STRING.equals(locationDefinition))
			return TOP_LEFT;
		if (BOTTOM_LEFT_STRING.equals(locationDefinition))
			return BOTTOM_LEFT;
		if (UNDERLAY_STRING.equals(locationDefinition))
			return UNDERLAY;
		return BOTTOM_RIGHT;

	}
	
	/**
	 * Logs a registry error when the configuration element is unknown.
	 */
	protected void logDuplicateId(IConfigurationElement element) {
		logError(element, "Duplicate id found: " + element.getAttribute(ATT_ID));//$NON-NLS-1$
	}

}
