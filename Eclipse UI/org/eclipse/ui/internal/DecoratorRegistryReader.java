package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.registry.RegistryReader;
import org.eclipse.ui.internal.registry.WizardsRegistryReader;

/**
 * The DecoratorRegistryReader is the class that reads the
 * decorator descriptions from the registry
 */

class DecoratorRegistryReader extends RegistryReader {

	//The registry values are the ones read from the registry
	static Collection values;

	private static String EXTENSION_ID = "decorators"; //$NON-NLS-1$
	private static String ATT_LABEL = "label"; //$NON-NLS-1$
	private static String ATT_ADAPTABLE = "adaptable"; //$NON-NLS-1$
	private static String ATT_ID = "id"; //$NON-NLS-1$
	private static String ATT_DESCRIPTION = "description"; //$NON-NLS-1$
	private static String ATT_ICON = "icon"; //$NON-NLS-1$
	private static String ATT_QUADRANT = "quadrant"; //$NON-NLS-1$
	private static String ATT_ENABLED = "state"; //$NON-NLS-1$
	private static String CHILD_ENABLEMENT = "enablement"; //$NON-NLS-1$
	private static String P_TRUE = "true"; //$NON-NLS-1$
	private static String ATT_OBJECT_CLASS = "objectClass"; //$NON-NLS-1$
	private static String ATT_LIGHTWEIGHT = "lightweight"; //$NON-NLS-1$

	//Constants for quadrants
	public static final int TOP_LEFT = 0;
	public static final int TOP_RIGHT = 1;
	public static final int BOTTOM_LEFT = 2;
	public static final int BOTTOM_RIGHT = 3;
	public static final int UNDERLAY = 4;

	//Constants for quadrants
	private static final String TOP_LEFT_STRING = "TOP_LEFT";
	private static final String TOP_RIGHT_STRING = "TOP_RIGHT";
	private static final String BOTTOM_LEFT_STRING = "BOTTOM_LEFT";
	private static final String BOTTOM_RIGHT_STRING = "BOTTOM_RIGHT";
	private static final String UNDERLAY_STRING = "UNDERLAY";

	/**
	 * Constructor for DecoratorRegistryReader.
	 */
	protected DecoratorRegistryReader() {
		super();
	}

	/*
	 * @see RegistryReader#readElement(IConfigurationElement)
	 */
	protected boolean readElement(IConfigurationElement element) {

		String name = element.getAttribute(ATT_LABEL);

		String id = element.getAttribute(ATT_ID);

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
			element.getAttribute(WizardsRegistryReader.ATT_CLASS) == null;

		//Lightweight or Full? It is lightweight if it is declared lightweight or if there is no class
		if (P_TRUE.equals(element.getAttribute(ATT_LIGHTWEIGHT)) || noClass) {

			int quadrant =
				getQuadrantConstant(element.getAttribute(ATT_QUADRANT));
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
	Collection readRegistry(IPluginRegistry in) {
		values = new ArrayList();
		readRegistry(in, PlatformUI.PLUGIN_ID, EXTENSION_ID);
		return values;
	}

	/**
	 * Get the constant value based on the quadrant supplied.
	 * Default to bottom right.
	 */
	private int getQuadrantConstant(String quadrantDefinition) {
		if (TOP_RIGHT_STRING.equals(quadrantDefinition))
			return TOP_RIGHT;
		if (TOP_LEFT_STRING.equals(quadrantDefinition))
			return TOP_LEFT;
		if (BOTTOM_LEFT_STRING.equals(quadrantDefinition))
			return BOTTOM_LEFT;
		if (UNDERLAY_STRING.equals(quadrantDefinition))
			return UNDERLAY;
		return BOTTOM_RIGHT;

	}

}