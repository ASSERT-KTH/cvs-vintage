package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.internal.registry.*;
import java.util.*;

/**
 * A PluginActionSet is a proxy for an action set defined in XML.
 * It creates a PluginAction for each action and does the required
 * cleanup on dispose.
 */
public class PluginActionSet implements IActionSet {
	private ActionSetDescriptor desc;
	private ArrayList pluginActions = new ArrayList(4);
/**
 * PluginActionSet constructor comment.
 */
public PluginActionSet(ActionSetDescriptor desc) {
	super();
	this.desc = desc;
}
/**
 * Adds one plugin action ref to the list.
 */
public void addPluginAction(WWinPluginAction action) {
	pluginActions.add(action);
}
/**
 * Returns the list of plugin actions for the set.
 */
public IAction[] getPluginActions() {
	IAction result[] = new IAction[pluginActions.size()];
	pluginActions.toArray(result);
	return result;
}
/**
 * Disposes of this action set.
 */
public void dispose() {
	Iterator iter = pluginActions.iterator();
	while (iter.hasNext()) {
		WWinPluginAction action = (WWinPluginAction)iter.next();
		action.dispose();
	}
}
/**
 * Returns the config element.
 */
public IConfigurationElement getConfigElement() {
	return desc.getConfigElement();
}
/**
 * Returns the underlying descriptor.
 */
public ActionSetDescriptor getDesc() {
	return desc;
}
/**
 * Initializes this action set, which is expected to add it actions as required
 * to the given workbench window and action bars.
 *
 * @param window the workbench window
 * @param bars the action bars
 */
public void init(IWorkbenchWindow window, IActionBars bars) {
	PluginActionSetBuilder builder = new PluginActionSetBuilder();
	builder.readActionExtensions(this, window, bars);
}
}
