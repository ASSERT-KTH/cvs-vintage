package org.eclipse.ui.internal.registry;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchConstants;

/**
 * This class is used to read action definitions from the platform registry.
 * Action definitions are stored in an ActionDefinitionRegistry once read.
 */
public class ActionDefinitionRegistryReader extends RegistryReader {
	private static final String TAG_ACTION_DEF = "actionDefinition"; //$NON-NLS-1$
	private static final String ATT_ID = "id"; //$NON-NLS-1$
	private static final String ATT_LABEL = "label"; //$NON-NLS-1$
	private static final String ATT_ICON = "icon"; //$NON-NLS-1$
	private static final String ATT_MENUBAR_PATH = "menubarPath"; //$NON-NLS-1$
	private static final String ATT_TOOLBAR_PATH = "toolbarPath"; //$NON-NLS-1$
	private static final String ATT_TOOLTIP = "tooltip"; //$NON-NLS-1$
	private static final String ATT_HELP_CONTEXT_ID = "helpContextId"; //$NON-NLS-1$
	private static final String ATT_STATE = "state"; //$NON-NLS-1$
	
	private ActionDefinitionRegistry actionDefinitions;

	/* (non-Javadoc)
	 * Method declared in RegistryReader.
	 */	
	protected boolean readElement(IConfigurationElement element) {
		if (!element.getName().equals(TAG_ACTION_DEF))
			return false;
		String id = element.getAttribute(ATT_ID);
		String label = element.getAttribute(ATT_LABEL);
		String icon = element.getAttribute(ATT_ICON);
		String menubarPath = element.getAttribute(ATT_MENUBAR_PATH);
		String toolbarPath = element.getAttribute(ATT_TOOLBAR_PATH);
		String tooltip = element.getAttribute(ATT_TOOLTIP);
		String helpContextId = element.getAttribute(ATT_HELP_CONTEXT_ID);
		String state = element.getAttribute(ATT_STATE);
		
		if (id==null) {
			logMissingAttribute(element, ATT_ID);
		}
		if (label==null) {
			logMissingAttribute(element, ATT_LABEL);
		}
		if (tooltip==null) {
			logMissingAttribute(element, ATT_TOOLTIP);
		}
		if (helpContextId==null) {
			logMissingAttribute(element, ATT_HELP_CONTEXT_ID);
		}
		
		ActionDefinition a = new ActionDefinition(id, label, icon, menubarPath,
			toolbarPath, tooltip, helpContextId, state);
		actionDefinitions.add(a);
		return true;
	}

	/**
	 * Reads the action definition extensions within the plugin registry and stores
	 * the results in the action definition registry.
	 */	
	public void readActionDefinitions(IPluginRegistry registry, ActionDefinitionRegistry out) {
		actionDefinitions = out;
		readRegistry(registry, PlatformUI.PLUGIN_ID, IWorkbenchConstants.PL_ACTION_DEFINITIONS);
	}
}
